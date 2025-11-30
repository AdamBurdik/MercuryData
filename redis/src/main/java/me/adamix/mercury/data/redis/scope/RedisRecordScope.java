package me.adamix.mercury.data.redis.scope;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.adamix.mercury.data.key.Key;
import me.adamix.mercury.data.redis.utils.JsonUtils;
import me.adamix.mercury.data.scope.ListScope;
import me.adamix.mercury.data.scope.RecordScope;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.ScanParams;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
				JsonUtils.hsetObjectSync(jedis, fullKey, key, value.getAsJsonObject(), collectionName);
			} else if (value.isJsonArray()) {
				if (key.toString().isEmpty()) {
					// Ideal would be to fix this.
					throw new IllegalArgumentException("Cannot store array at root. A key is required before any index");
				}
				JsonUtils.hsetSyncArray(jedis, fullKey, key, value.getAsJsonArray(), collectionName);
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
						return Optional.of(JsonUtils.parseString(value));
				}
				if (childKey.startsWith(base)) {
					JsonUtils.createNestedObject(jsonObject, childKey, me.adamix.mercury.data.utils.JsonUtils.parseString(value));
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
			LOGGER.error("Exception occurred while reading a field from redis collection", e);
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
			LOGGER.error("Exception occurred while removing a field from redis collection", e);
			throw e;
		} finally {
			lock.unlock();
		}
		return this;
	}

	@Override
	public boolean fieldJsonExistsSync(@NotNull Key key) {
		LOGGER.debug("Redis check if field exists operation - key {} ({}.{})", key, collectionName, fullKey);
		lock.lock();

		try (Jedis jedis = jedisPool.getResource()) {
			Map<String, String> map = jedis.hgetAll(fullKey.withCollectionName(collectionName));
			if (map == null) {
				return false;
			}

			String base = key.toString();

			for (String childKey : map.keySet()) {
				if (childKey.equals(base) || childKey.startsWith(base)) {
					return true;
				}
			}

		} catch (Exception e) {
			LOGGER.error("Exception occurred while checking if field exists in redis collection", e);
			throw e;
		} finally {
			lock.unlock();
		}
		return false;
	}

	@Override
	public @NotNull RecordScope clearSync() {
		LOGGER.debug("Redis clear field operation");
		lock.lock();
		try  (Jedis jedis = jedisPool.getResource()) {
			String cursor = "0";
			do {
				var scan = jedis.scan(cursor, new ScanParams().match(fullKey.withCollectionName(collectionName) + "*"));
				for (String key : scan.getResult()) {
					jedis.del(key);
				}
				cursor = scan.getCursor();
			} while (!cursor.equals("0"));
		} catch (Exception e) {
			LOGGER.error("Exception occurred while clearing a redis collection", e);
			throw e;
		} finally {
			lock.unlock();
		}

		return this;
	}

	@Override
	public @NotNull Collection<String> listFieldsSync(boolean recursive) {
		Set<String> set = new HashSet<>();

		LOGGER.debug("Redis list field keys operation");
		lock.lock();
		try  (Jedis jedis = jedisPool.getResource()) {
			Map<String, String> map = jedis.hgetAll(fullKey.withCollectionName(collectionName));
			if (map == null) {
				return set;
			}

			for (String childKey : map.keySet()) {
				Key key = Key.parse(childKey, "\\.", "\\:");
				if (recursive) {
					for (Key.KeyPart part : key.getParts()) {

						// We ignore indices of lists, because we care just about keys (I hope so)
						if (part.getSeparator() != ':') {
							set.add(part.getValue());
						}
					}
				} else {
					set.add(key.getParts().getFirst().getValue());
				}
			}


		} catch (Exception e) {
			LOGGER.error("Exception occurred while getting field keys from redis collection", e);
			throw e;
		} finally {
			lock.unlock();
		}

		return set;
	}

	@Override
	public @NotNull ListScope list(@NotNull Key key) {
		return new RedisListScope(jedisPool, collectionName, fullKey, key);
	}
}
