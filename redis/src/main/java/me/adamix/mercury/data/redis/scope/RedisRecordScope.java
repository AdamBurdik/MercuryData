package me.adamix.mercury.data.redis.scope;

import com.google.gson.JsonElement;
import me.adamix.mercury.data.scope.RecordScope;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

public class RedisRecordScope implements RecordScope {
	private static final Logger LOGGER = LoggerFactory.getLogger(RedisRecordScope.class);
	public final @NotNull ReentrantLock lock = new ReentrantLock();
	private final @NotNull JedisPool jedisPool;

	public RedisRecordScope(
			@NotNull JedisPool jedisPool
	) {
		this.jedisPool = jedisPool;
	}

	@Override
	public @NotNull RecordScope setFieldSync(@NotNull String key, @NotNull JsonElement value) {
		return this;
	}

	@Override
	public @NotNull Optional<JsonElement> getFieldSync(@NotNull String key) {
		return Optional.empty();
	}

	@Override
	public @NotNull RecordScope removeFieldSync(@NotNull String key) {
		return this;
	}

	@Override
	public boolean fieldExistsSync(@NotNull String key) {
		return false;
	}

	@Override
	public @NotNull RecordScope clearSync() {
		return this;
	}

	@Override
	public @NotNull Collection<String> listFieldsSync() {
		return List.of();
	}
}
