package me.adamix.mercury.data.redis.scope;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.adamix.mercury.data.key.Key;
import me.adamix.mercury.data.redis.utils.JsonUtils;
import me.adamix.mercury.data.scope.RecordScope;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

public class RedisRecordScope implements RecordScope {
	private static final Logger LOGGER = LoggerFactory.getLogger(RedisRecordScope.class);
	public final @NotNull ReentrantLock lock = new ReentrantLock();
	private final @NotNull JedisPool jedisPool;
	private final @NotNull String collectionName;
	private final @NotNull Key fullKey;

	public RedisRecordScope(
			@NotNull JedisPool jedisPool,
			@NotNull String collectionName,
			@NotNull Key fullKey
	) {
		this.jedisPool = jedisPool;
		this.fullKey = fullKey;
		this.collectionName = collectionName;
	}

	@Override
	public @NotNull RecordScope setFieldJsonSync(@NotNull Key key, @NotNull JsonElement value) {
		LOGGER.debug("Redis write field operation - key: {}, value: {}", key, value);
		lock.lock();
		try (Jedis jedis = jedisPool.getResource()) {
			if (value.isJsonPrimitive()) {
				jedis.hset(fullKey.withCollectionName(collectionName), key.toString(), value.getAsString());
			} else if (value.isJsonObject()) {
				hsetSync(jedis, key, Key.empty(), value.getAsJsonObject());
			} else if (value.isJsonArray()) {
				// Ideal would be to fix this.
				throw new IllegalArgumentException("Cannot store array at root. A key is required before any index");
			} else {
				LOGGER.error("Invalid value for key {} in RedisRecordScope", key);
			}

		} catch (Exception e) {
			LOGGER.error("Exception occurred setting a field to redis collection", e);
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
				jedis.hset(key.withCollectionName(collectionName), childKey.addPart(elementKey).toString(), jsonElement.toString());
			}
		}
	}

	private void hsetSyncArray(@NotNull Jedis jedis, @NotNull Key key, @NotNull Key childKey, @NotNull JsonArray array) {
		int index = 0;
		for (JsonElement element : array.asList()) {

			Key indexedKey = childKey.addPart(String.valueOf(index), ':');

			if (element.isJsonPrimitive()) {
				jedis.hset(key.withCollectionName(collectionName), indexedKey.toString(), element.getAsString());
			} else if (element.isJsonObject()) {
				hsetSync(jedis, key, indexedKey, element.getAsJsonObject());
			} else if (element.isJsonArray()) {
				hsetSyncArray(jedis, key, indexedKey, element.getAsJsonArray());
			}

			index++;
		}
	}

	@Override
	public @NotNull Optional<JsonElement> getFieldJsonSync(@NotNull Key key) {
		LOGGER.debug("Redis read field operation - key {} ({}.{})", key, collectionName, fullKey);
		lock.lock();

		try (Jedis jedis = jedisPool.getResource()) {
			Map<String, String> map = jedis.hgetAll(fullKey.withCollectionName(collectionName));
			if (map == null) {
				return Optional.empty();
			}

			JsonObject jsonObject = new JsonObject();

			String base = key.toString();

			for (String childKey : map.keySet()) {
				String value = map.get(childKey);
				if (childKey.equals(base)) {
					return Optional.ofNullable(JsonParser.parseString(value));
				}
				if (childKey.startsWith(base)) {
					JsonUtils.createNestedObject(jsonObject, childKey, JsonParser.parseString(value));
				}
			}

			JsonElement element = jsonObject;
			for (Key.KeyPart part : key.getParts()) {
				if (element.isJsonObject()) {
					element = element.getAsJsonObject().get(part.getValue());
				} else if (element.isJsonArray()) {
					element = element.getAsJsonArray().get(Integer.parseInt(part.getValue()));
				}
				if (element == null) {
					return Optional.empty();
				}
			}

			return Optional.of(element);

		} catch (Exception e) {
			LOGGER.error("Exception occurred while reading an field from redis collection", e);
			throw e;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public @NotNull RecordScope removeFieldJsonSync(@NotNull Key key) {
		LOGGER.debug("Redis remove field operation - key: {}", key);
		lock.lock();
		try (Jedis jedis = jedisPool.getResource()) {

			Map<String, String> map = jedis.hgetAll(fullKey.withCollectionName(collectionName));
			if (map == null) {
				return this;
			}

			JsonObject jsonObject = new JsonObject();

			String base = key.toString();

			for (String childKey : map.keySet()) {
				if (childKey.equals(base) || childKey.startsWith(base)) {
					jedis.hdel(fullKey.withCollectionName(collectionName), childKey);
				}
			}


		} catch (Exception e) {
			LOGGER.error("Exception occurred while removing an field from redis collection", e);
			throw e;
		} finally {
			lock.unlock();
		}
		return this;
	}

	@Override
	public boolean fieldJsonExistsSync(@NotNull Key key) {
		return false;
	}

	@Override
	public @NotNull RecordScope clearSync() {
		return this;
	}

	@Override
	public @NotNull Collection<Key> listFieldsSync() {
		return List.of();
	}
}
