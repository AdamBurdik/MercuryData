package me.adamix.mercury.data.redis;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.adamix.mercury.data.MercuryCollection;
import me.adamix.mercury.data.key.Key;
import me.adamix.mercury.data.operation.update.UpdateField;
import me.adamix.mercury.data.redis.utils.JsonUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class RedisCollection implements MercuryCollection {
	private static final Logger LOGGER = LoggerFactory.getLogger(RedisCollection.class);
	private final @NotNull String name;
	private final @NotNull JedisPool jedisPool;

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

		try (Jedis jedis = jedisPool.getResource()) {
			if (!jsonElement.isJsonObject()) {
				jedis.set(key.withCollectionName(name), jsonElement.toString());
			} else {
				hsetSync(jedis, key, Key.empty(), jsonElement.getAsJsonObject());
			}
		} catch (Exception e) {
			LOGGER.error("Exception occurred while writing to redis collection", e);
			throw e;
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
		try (Jedis jedis = jedisPool.getResource()) {
			Map<String, String> map = jedis.hgetAll(key.toString());
			if (map == null) {
				return Optional.empty();
			}

			JsonObject jsonObject = new JsonObject();

			map.forEach((childKey, value) -> {
				JsonUtils.addNestedProperty(jsonObject, childKey, JsonParser.parseString(value), "\\:");
			});

			return Optional.of(jsonObject);
		} catch (Exception e) {
			LOGGER.error("Exception occurred while reading from redis collection", e);
			throw e;
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
		try (Jedis jedis = jedisPool.getResource()) {
			JsonElement jsonElement = field.codec().encode(field.value());
			final String stringKey = key.withCollectionName(this.name);
			final String fieldKey = field.key().toString();

			if (!jedis.hexists(stringKey, fieldKey) && !insertIfAbsent) {
				return;
			}

			jedis.hset(stringKey, field.key().toString(), jsonElement.toString());
		}
	}

	@Override
	public @NotNull CompletableFuture<Boolean> remove(@NotNull Key key) {
		return CompletableFuture.supplyAsync(() -> removeSync(key));
	}

	@Override
	public boolean removeSync(@NotNull Key key) {
		try (Jedis jedis = jedisPool.getResource()) {
			final String stringKey = key.withCollectionName(this.name);

			Map<String, String> map = jedis.hgetAll(stringKey);
			if (map == null) {
				return false;
			}

			for (String elementKey : map.keySet()) {
				jedis.hdel(stringKey, elementKey);
			}
		}

		return true;
	}
}
