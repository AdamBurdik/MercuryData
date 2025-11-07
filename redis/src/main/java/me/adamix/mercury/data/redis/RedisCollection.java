package me.adamix.mercury.data.redis;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.adamix.mercury.data.MercuryCollection;
import me.adamix.mercury.data.key.Key;
import me.adamix.mercury.data.redis.scope.RedisRecordScope;
import me.adamix.mercury.data.redis.utils.JsonUtils;
import me.adamix.mercury.data.scope.RecordScope;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.ScanParams;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

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
		for (String elementKey : jsonObject.keySet()) {
			JsonElement jsonElement = jsonObject.get(elementKey);

			if (jsonElement.isJsonObject()) {
				hsetSync(jedis, key, childKey.addPart(elementKey), jsonElement.getAsJsonObject());
			} else if (jsonElement.isJsonArray()) {
				hsetSyncArray(jedis, key, childKey.addPart(elementKey, '.'), jsonElement.getAsJsonArray());
			} else {
				jedis.hset(key.withCollectionName(this.name), childKey.addPart(elementKey).toString(), jsonElement.toString());
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
						yield  Optional.empty();
					}

					JsonObject jsonObject = new JsonObject();

					for (String childKey : map.keySet()) {
						String value = map.get(childKey);
						JsonUtils.createNestedObject(jsonObject, childKey, JsonParser.parseString(value));
					}

					yield  Optional.of(jsonObject);
				}
				case "string" -> {
					String value = jedis.get(fullKey);
					yield Optional.of(
							me.adamix.mercury.data.utils.JsonUtils.parseString(value)
					);
				}
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
		try  (Jedis jedis = jedisPool.getResource()) {

			String fullKey = key.withCollectionName(this.name);

			String type = jedis.type(fullKey);

			switch (type) {
				case "hash" -> {
					Map<String, String> map = jedis.hgetAll(fullKey);
					if (map == null) {
						break;
					}

					for (String childKey : map.keySet()) {
						String value = map.get(childKey);
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
		try  (Jedis jedis = jedisPool.getResource()) {

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
		try  (Jedis jedis = jedisPool.getResource()) {
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
}
