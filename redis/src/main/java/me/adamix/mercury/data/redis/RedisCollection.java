package me.adamix.mercury.data.redis;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.adamix.mercury.data.MercuryCollection;
import me.adamix.mercury.data.codec.Codec;
import me.adamix.mercury.data.key.Key;
import me.adamix.mercury.data.operation.update.UpdateField;
import me.adamix.mercury.data.query.FindQueryBuilder;
import me.adamix.mercury.data.query.QueryResult;
import me.adamix.mercury.data.query.filter.FieldFilter;
import me.adamix.mercury.data.redis.query.RedisFindQueryBuilder;
import me.adamix.mercury.data.redis.query.RedisQueryResult;
import me.adamix.mercury.data.redis.utils.JsonUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

public class RedisCollection implements MercuryCollection {
	private static final Logger LOGGER = LoggerFactory.getLogger(RedisCollection.class);
	private final @NotNull String name;
	private final @NotNull JedisPool jedisPool;
	public final @NotNull ReentrantLock lock = new ReentrantLock();

	public RedisCollection(@NotNull String name, @NotNull JedisPool jedisPool) {
		this.name = name;
		this.jedisPool = jedisPool;
	}

	@Override
	public @NotNull CompletableFuture<Void> set(@NotNull Key key, @NotNull JsonElement jsonElement) {
		return CompletableFuture.runAsync(() -> {
			setSync(key, jsonElement);
		});
	}

	@Override
	public void setSync(@NotNull Key key, @NotNull JsonElement jsonElement) {
		LOGGER.debug("Redis write operation - key: {}, value: {}", key, jsonElement);
		lock.lock();

		try (Jedis jedis = jedisPool.getResource()) {
			if (!jsonElement.isJsonObject()) {
				jedis.set(key.withCollectionName(name), jsonElement.toString());
			} else {
				hsetSync(jedis, key, Key.empty(), jsonElement.getAsJsonObject());
			}
		} catch (Exception e) {
			LOGGER.error("Exception occurred while writing to redis collection", e);
			throw e;
		} finally {
			lock.unlock();
		}
	}

	private void hsetSync(@NotNull Jedis jedis, @NotNull Key key, @NotNull Key childKey, @NotNull JsonObject jsonObject) {
		for (String elementKey : jsonObject.keySet()) {
			JsonElement jsonElement = jsonObject.get(elementKey);

			if (!jsonElement.isJsonObject()) {
				jedis.hset(key.withCollectionName(name), childKey.addPart(elementKey).toString(), jsonElement.toString());
			} else {
				hsetSync(jedis, key, childKey.addPart(elementKey), jsonElement.getAsJsonObject());
			}
		}
	}

	@Override
	public @NotNull CompletableFuture<Optional<JsonElement>> get(@NotNull Key key) {
		return CompletableFuture.supplyAsync(() -> getSync(key));
	}

	@Override
	public @NotNull Optional<JsonElement> getSync(@NotNull Key key) {
		lock.lock();
		try (Jedis jedis = jedisPool.getResource()) {
			Map<String, String> map = jedis.hgetAll(key.withCollectionName(this.name));
			if (map == null) {
				return Optional.empty();
			}

			JsonObject jsonObject = new JsonObject();

			map.forEach((childKey, value) -> {
				JsonUtils.addNestedProperty(jsonObject, childKey, JsonParser.parseString(value), "\\.");
			});

			return Optional.of(jsonObject);
		} catch (Exception e) {
			LOGGER.error("Exception occurred while reading from redis collection", e);
			throw e;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public @NotNull <T> CompletableFuture<Void> updateField(@NotNull Key key, @NotNull UpdateField<T> field, boolean insertIfAbsent) {
		return CompletableFuture.runAsync(() -> {
			updateFieldSync(key, field, insertIfAbsent);
		});
	}

	@Override
	public <T> void updateFieldSync(@NotNull Key key, @NotNull UpdateField<T> field, boolean insertIfAbsent) {
		lock.lock();
		try (Jedis jedis = jedisPool.getResource()) {
			T value;
			if (field.isConstant()) {
				 value = field.function().apply(null);
			} else {
				Map<String, String> map = jedis.hgetAll(key.withCollectionName(this.name));
				if (map == null) {
					return;
				}

				JsonObject jsonObject = new JsonObject();

				map.forEach((childKey, v) -> {
					JsonUtils.addNestedProperty(jsonObject, childKey, JsonParser.parseString(v), "\\.");
				});

				String rawJson = map.get(field.key().toString());

				value = field.function().apply(field.codec().decode(JsonParser.parseString(rawJson)));
			}

			JsonElement jsonElement = field.codec().encode(value);
			final String stringKey = key.withCollectionName(this.name);
			final String fieldKey = field.key().toString();

			if (!jedis.hexists(stringKey, fieldKey) && !insertIfAbsent) {
				return;
			}

			jedis.hset(stringKey, field.key().toString(), jsonElement.toString());
		} catch (Exception e) {
			LOGGER.error("Exception occurred while updating field redis collection", e);
			throw e;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public @NotNull CompletableFuture<Boolean> remove(@NotNull Key key) {
		return CompletableFuture.supplyAsync(() -> removeSync(key));
	}

	@Override
	public boolean removeSync(@NotNull Key key) {
		lock.lock();
		try (Jedis jedis = jedisPool.getResource()) {
			final String stringKey = key.withCollectionName(this.name);

			Map<String, String> map = jedis.hgetAll(stringKey);
			if (map == null) {
				return false;
			}

			for (String elementKey : map.keySet()) {
				jedis.hdel(stringKey, elementKey);
			}
		} catch (Exception e) {
			LOGGER.error("Exception occurred while removing from redis collection", e);
			throw e;
		} finally {
			lock.unlock();
		}

		return true;
	}

	@Override
	public <T> @NotNull FindQueryBuilder<T> find(@NotNull Codec<T> codec) {
		return new RedisFindQueryBuilder<>(codec, query -> {
			lock.lock();
			try (Jedis jedis = jedisPool.getResource()) {
				ScanParams params = new ScanParams().match(this.name + ".*").count(Integer.MAX_VALUE);

				Collection<QueryResult.Entry<T>> collection = new ArrayList<>();
				for (String rawKey : getAllKeys(jedis, params)) {
					Map<String, String> map = jedis.hgetAll(rawKey);
					if (map == null) {
						continue;
					}

					JsonObject jsonObject = new JsonObject();

					map.forEach((childKey, value) -> {
						JsonUtils.addNestedProperty(jsonObject, childKey, JsonParser.parseString(value), "\\.");
					});

					Optional<T> opt = codec.decodeOptional(jsonObject);
					if (opt.isEmpty()) {
						continue;
					}

					AtomicBoolean filtersPassed = new AtomicBoolean(true);

					map.forEach((fieldKey, fieldValue) -> {
						List<FieldFilter<?>> filters = query.getFieldFilter(Key.of(fieldKey));

						// Apply filters. If any filters fail, the lambda will be exited
						for (FieldFilter<?> filter : filters) {
							if (!applyFilter(filter, JsonParser.parseString(fieldValue))) {
								filtersPassed.set(false);
								return;
							}
						}
					});

					if (!filtersPassed.get()) {
						continue;
					}

					Key key = Key.parse(rawKey).stripCollectionName();

					collection.add(new QueryResult.Entry<>(key, opt.get()));
				}

				return new RedisQueryResult<>(collection);
			} catch (Exception e) {
				LOGGER.error("Exception occurred while searching in redis collection", e);
				throw e;
			} finally {
				lock.unlock();
			}
		});
	}

	private <T> boolean applyFilter(@NotNull FieldFilter<T> filter, @NotNull JsonElement jsonElement) {
		return filter.test(filter.codec().decode(jsonElement));
	}

	private @NotNull List<String> getAllKeys(@NotNull Jedis jedis, @NotNull ScanParams params) {
		String cursor = ScanParams.SCAN_POINTER_START;

		List<String> allKeys = new ArrayList<>();

		try {
			do {
				// Gets all keys from redis from this collection
				ScanResult<String> scanResult = jedis.scan(cursor, params);
				allKeys.addAll(scanResult.getResult());
				cursor = scanResult.getCursor();

			} while (!cursor.equals(ScanParams.SCAN_POINTER_START));
		} catch (Exception e) {
			LOGGER.error("Exception occurred while getting all keys in redis collection", e);
			throw e;
		}

		return allKeys;
	}
}
