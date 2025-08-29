package me.adamix.mercury.data.redis.module;

import me.adamix.mercury.data.MercuryCollection;
import me.adamix.mercury.data.module.StoreModule;
import me.adamix.mercury.data.redis.RedisCollection;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisStoreModule implements StoreModule {
	private static final Logger LOGGER = LoggerFactory.getLogger(RedisStoreModule.class);
	private final @NotNull JedisPool pool;

	public RedisStoreModule(@NotNull String address, int port) {
		this(new JedisPool(address, port));
		LOGGER.debug("Redis store module [{}:{}] created successfully", address, port);
	}

	public RedisStoreModule(@NotNull JedisPool pool) {
		this.pool = pool;
		ping();
	}

	public void ping() {
		try (Jedis jedis = pool.getResource()) {
			String response = jedis.ping();
			if ("PONG".equals(response)) {
				LOGGER.info("Redis has been connected");
			} else {
				LOGGER.error("Unexpected Redis ping response: {}", response);
			}
		} catch (Exception e) {
			LOGGER.error("Failed to connect to Redis", e);
		}
	}

	@Override
	public @NotNull MercuryCollection createCollection(@NotNull String name) {
		return new RedisCollection(name, pool);
	}

	@Override
	public void close() {
		pool.close();
	}
}
