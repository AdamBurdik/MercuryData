package me.adamix.mercury.data.redis;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.adamix.mercury.data.MercuryCollection;
import me.adamix.mercury.data.codec.Codec;
import me.adamix.mercury.data.exception.MissingFieldException;
import me.adamix.mercury.data.key.Key;
import me.adamix.mercury.data.metadata.Metadata;
import me.adamix.mercury.data.query.FindQueryBuilder;
import me.adamix.mercury.data.query.QueryResult;
import me.adamix.mercury.data.query.filter.FieldFilter;
import me.adamix.mercury.data.redis.query.RedisFindQueryBuilder;
import me.adamix.mercury.data.redis.query.RedisQueryResult;
import me.adamix.mercury.data.redis.scope.RedisRecordScope;
import me.adamix.mercury.data.redis.utils.JsonUtils;
import me.adamix.mercury.data.scope.RecordScope;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class RedisCollection implements MercuryCollection {
	private static final Logger LOGGER = LoggerFactory.getLogger(RedisCollection.class);
	private final @NotNull String name;
	private final @NotNull JedisPool jedisPool;
	private final ReentrantLock lock = new ReentrantLock();

	public RedisCollection(
			@NotNull String name,
			@NotNull JedisPool jedisPool
	) {
		this.name = name;
		this.jedisPool = jedisPool;
	}

	@Override
	public @NotNull MercuryCollection setJsonSync(@NotNull Key key, @NotNull JsonElement value) {
		LOGGER.debug("Redis write operation - key: {}, value: {}", key, value);
		lock.lock();

		try (Jedis jedis = jedisPool.getResource()) {

			if (value.isJsonPrimitive()) {
				jedis.set(key.withCollectionName(this.name), value.getAsString());
			} else if (value.isJsonObject()) {
				hsetSync(jedis, key, Key.empty(), value.getAsJsonObject());
			} else if (value.isJsonArray()) {
				// Ideal would be to fix this.
				throw new IllegalArgumentException("Cannot store array at root. A key is required before any index");
			} else {
				LOGGER.error("Invalid value for key {} in RedisCollection", key);
			}

		} catch (Exception e) {
			LOGGER.error("Exception occurred while writing an entity to redis collection", e);
			throw e;
		} finally {
			lock.unlock();
		}

		return this;
	}

	private void hsetSync(@NotNull Jedis jedis, @NotNull Key key, @NotNull Key childKey, @NotNull JsonObject jsonObject) {
		if (jsonObject.isEmpty()) {
			jedis.hset(key.withCollectionName(this.name), childKey.addPart(Metadata.EMPTY.value(), '.').toString(), "true");
			return;
		}
		for (String elementKey : jsonObject.keySet()) {
			JsonElement jsonElement = jsonObject.get(elementKey);

			if (jsonElement.isJsonObject()) {
				hsetSync(jedis, key, childKey.addPart(elementKey), jsonElement.getAsJsonObject());
			} else if (jsonElement.isJsonArray()) {
				hsetSyncArray(jedis, key, childKey.addPart(elementKey, '.'), jsonElement.getAsJsonArray());
			} else {
				String rawValue = JsonUtils.getRawValue(jsonElement);
				if (rawValue == null) rawValue = Metadata.NULL.value();
				jedis.hset(key.withCollectionName(this.name), childKey.addPart(elementKey).toString(), rawValue);
			}
		}
	}

	private void hsetSyncArray(@NotNull Jedis jedis, @NotNull Key key, @NotNull Key childKey, @NotNull JsonArray array) {
		int index = 0;
		for (JsonElement element : array.asList()) {

			Key indexedKey = childKey.addPart(String.valueOf(index), ':');

			if (element.isJsonPrimitive()) {
				jedis.hset(key.withCollectionName(this.name), indexedKey.toString(), element.getAsString());
			} else if (element.isJsonObject()) {
				hsetSync(jedis, key, indexedKey, element.getAsJsonObject());
			} else if (element.isJsonArray()) {
				hsetSyncArray(jedis, key, indexedKey, element.getAsJsonArray());
			}

			index++;
		}
		jedis.hset(key.withCollectionName(this.name), childKey.addPart(Metadata.LIST_LENGTH.value(), ':').toString(), String.valueOf(array.size()));
	}

	@Override
	public @NotNull Optional<JsonElement> getJsonSync(@NotNull Key key) {
		LOGGER.debug("Redis read operation - key: {}", key);
		lock.lock();

		try (Jedis jedis = jedisPool.getResource()) {
			String fullKey = key.withCollectionName(this.name);

			String type = jedis.type(fullKey);

			return switch (type) {
				case "hash" -> {
					Map<String, String> map = jedis.hgetAll(fullKey);
					if (map == null) {
						yield Optional.empty();
					}

					JsonObject jsonObject = new JsonObject();

					for (String childKey : map.keySet()) {
						String value = map.get(childKey);
						JsonElement element = me.adamix.mercury.data.utils.JsonUtils.parseString(value);
						JsonUtils.createNestedObject(jsonObject, childKey, element);
					}

					yield Optional.of(jsonObject);
				}
				case "string" -> {
					String value = jedis.get(fullKey);
					yield Optional.of(
							JsonUtils.parseString(value)
					);
				}
				case "none" -> Optional.empty();
				default -> {
					LOGGER.error("Unsupported key type: {}", type);
					yield Optional.empty();
				}
			};

		} catch (Exception e) {
			LOGGER.error("Exception occurred while reading an entity from redis collection", e);
			throw e;
		} finally {
			lock.unlock();
		}
	}


	@Override
	public @NotNull MercuryCollection removeSync(@NotNull Key key) {
		LOGGER.debug("Redis remove operation - key: {}", key);
		lock.lock();
		try (Jedis jedis = jedisPool.getResource()) {
			String fullKey = key.withCollectionName(this.name);

			String type = jedis.type(fullKey);

			switch (type) {
				case "hash" -> {
					Map<String, String> map = jedis.hgetAll(fullKey);
					if (map == null) {
						break;
					}

					for (String childKey : map.keySet()) {
						jedis.hdel(fullKey, childKey);
					}
				}
				case "string" -> jedis.del(fullKey);
			}


		} catch (Exception e) {
			LOGGER.error("Exception occurred while removing an entity from redis collection", e);
			throw e;
		} finally {
			lock.unlock();
		}
		return this;
	}

	@Override
	public boolean existsSync(@NotNull Key key) {
		LOGGER.debug("Redis exists operation - key: {}", key);
		lock.lock();
		try (Jedis jedis = jedisPool.getResource()) {
			String fullKey = key.withCollectionName(this.name);

			String type = jedis.type(fullKey);

			return !type.equals("none");
		} catch (Exception e) {
			LOGGER.error("Exception occurred while checking if an entity exists in redis collection", e);
			throw e;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public @NotNull MercuryCollection clearSync() {
		LOGGER.debug("Redis clear operation");
		lock.lock();
		try (Jedis jedis = jedisPool.getResource()) {
			String cursor = "0";
			do {
				var scan = jedis.scan(cursor, new ScanParams().match(this.name + ".*"));
				for (String key : scan.getResult()) {
					jedis.del(key);
				}
				cursor = scan.getCursor();
			} while (!cursor.equals("0"));
		} catch (Exception e) {
			LOGGER.error("Exception occurred while clearing an redis collection", e);
			throw e;
		} finally {
			lock.unlock();
		}

		return this;
	}

	@Override
	public @NotNull RecordScope record(@NotNull Key key) {
		return new RedisRecordScope(jedisPool, this.name, key);
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
						JsonUtils.createNestedObject(jsonObject, childKey, JsonUtils.parseString(value));
					});

					Optional<T> opt = codec.decodeOptional(jsonObject);
					if (opt.isEmpty()) {
						continue;
					}

					AtomicBoolean filtersPassed = new AtomicBoolean(true);

					map.forEach((fieldKey, fieldValue) -> {
						List<FieldFilter<?>> filters = query.getFieldFilter(Key.parse(fieldKey));

						// Apply filters. If any filters fail, the lambda will be exited
						for (FieldFilter<?> filter : filters) {
							if (!applyFilter(filter, JsonUtils.parseString(fieldValue))) {
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
		try {
			T value = filter.codec().decode(jsonElement);
			return filter.test(value);
		} catch (MissingFieldException e) {
			return false;
		}
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
			LOGGER.error("Exception occurred while getting all keys from redis collection", e);
			throw e;
		}

		return allKeys;
	}

	@Override
	public @NotNull Set<Key> keysSync() {
		Set<String> keys = new HashSet<>();

		lock.lock();
		try (Jedis jedis = jedisPool.getResource()) {
			String cursor = ScanParams.SCAN_POINTER_START;
			ScanParams params = new ScanParams().match(this.name + ".*").count(Integer.MAX_VALUE);

			do {
				// Gets all keys from redis from this collection
				ScanResult<String> scanResult = jedis.scan(cursor, params);
				keys.addAll(scanResult.getResult());
				cursor = scanResult.getCursor();

			} while (!cursor.equals(ScanParams.SCAN_POINTER_START));

		} catch (Exception e) {
			LOGGER.error("Exception occurred while getting keys from redis collection", e);
			throw e;
		} finally {
			lock.unlock();
		}

		return keys.stream().map(s -> s.replaceFirst(this.name, "")).map(Key::parse).collect(Collectors.toSet());
	}
}
