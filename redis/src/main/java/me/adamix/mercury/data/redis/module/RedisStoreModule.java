package me.adamix.mercury.data.redis.module;

import me.adamix.mercury.data.MercuryCollection;
import me.adamix.mercury.data.module.StoreModule;
import me.adamix.mercury.data.redis.RedisCollection;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;

public class RedisStoreModule implements StoreModule {
	private static final Logger LOGGER = LoggerFactory.getLogger(RedisStoreModule.class);
	private final @NotNull JedisPool pool;

	public RedisStoreModule(@NotNull String address, int port) {
		LOGGER.debug("Redis store module [{}:{}] created successfully", address, port);
		this.pool = new JedisPool(address, port);
	}

	public RedisStoreModule(@NotNull JedisPool pool) {
		this.pool = pool;
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
