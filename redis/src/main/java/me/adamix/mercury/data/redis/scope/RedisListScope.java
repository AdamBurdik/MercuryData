package me.adamix.mercury.data.redis.scope;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.adamix.mercury.data.codec.Codec;
import me.adamix.mercury.data.key.Key;
import me.adamix.mercury.data.metadata.Metadata;
import me.adamix.mercury.data.redis.utils.JsonUtils;
import me.adamix.mercury.data.redis.utils.ListOperations;
import me.adamix.mercury.data.scope.ListScope;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class RedisListScope implements ListScope {
	private static final Logger LOGGER = LoggerFactory.getLogger(RedisListScope.class);
	public final @NotNull ReentrantLock lock = new ReentrantLock();
	private final @NotNull JedisPool jedisPool;
	private final @NotNull String collectionName;
	private final @NotNull Key fieldKey;

	private final @NotNull Key key;

	public RedisListScope(
			@NotNull JedisPool jedisPool,
			@NotNull String collectionName,
			@NotNull Key fieldKey,
			@NotNull Key key
	) {
		this.jedisPool = jedisPool;
		this.collectionName = collectionName;
		this.fieldKey = fieldKey;
		this.key = key;
	}

	private <U> @NotNull U acquireLockAndJedisFunc(@NotNull Function<Jedis, U> function) {
		lock.lock();
		try (Jedis jedis = jedisPool.getResource()) {
			return function.apply(jedis);
		} finally {
			lock.unlock();
		}
	}

	private void acquireLockAndJedis(@NotNull Consumer<Jedis> jedisConsumer) {
		lock.lock();
		try (Jedis jedis = jedisPool.getResource()) {
			jedisConsumer.accept(jedis);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public int sizeSync() {
		return acquireLockAndJedisFunc(jedis -> ListOperations.getSize(jedis, fieldKey, key, collectionName));
	}

	@Override
	public @NotNull ListScope pushBackJsonSync(@NotNull JsonElement element) {
		try {
			acquireLockAndJedis(jedis -> {
				int size = ListOperations.getSize(jedis, fieldKey, key, collectionName);

				JsonUtils.hsetSync(
						jedis,
						fieldKey,
						key.addPart(String.valueOf(size), ':'),
						element,
						collectionName
				);

				jedis.hset(
						fieldKey.withCollectionName(collectionName),
						key.addPart(Metadata.LIST_LENGTH.value(), ':').toString(),
						String.valueOf(size + 1)
				);
			});
		} catch (Exception e) {
			LOGGER.error("Exception occurred while pushing element to the back of a list in redis collection", e);
		}

		return this;
	}

	@Override
	public @NotNull ListScope pushFrontJsonSync(@NotNull JsonElement element) {
		acquireLockAndJedis(jedis -> ListOperations.insert(jedis, fieldKey, key, collectionName, element, 0));
		return this;
	}

	@Override
	public @NotNull Optional<JsonElement> getJsonSync(int index) {
		try {
			return acquireLockAndJedisFunc(jedis -> {
				JsonElement element = ListOperations.get(jedis, fieldKey, key, collectionName, index);
				return Optional.ofNullable(element);
			});
		} catch (IndexOutOfBoundsException e) {
			throw e;
		} catch (Exception e) {
			LOGGER.error("Exception occurred while getting an element from a list in redis collection", e);
		}

		return Optional.empty();
	}

	@Override
	public @NotNull Optional<JsonElement> getLastJsonSync() {
		try {
			return acquireLockAndJedisFunc(jedis -> {
				int size = ListOperations.getSize(jedis, fieldKey, key, collectionName);
				JsonElement element = ListOperations.get(jedis, fieldKey, key, collectionName, size - 1);
				return Optional.ofNullable(element);
			});
		} catch (IndexOutOfBoundsException e) {
			throw e;
		} catch (Exception e) {
			LOGGER.error("Exception occurred while getting the last element from a list in redis collection", e);
		}

		return Optional.empty();
	}

	@Override
	public @NotNull Collection<JsonElement> getRangeJsonSync(int fromIndex, int toIndex) {
		return List.of();
	}

	@Override
	public @NotNull Collection<JsonElement> getAllJsonSync() {
		return List.of();
	}

	@Override
	public @NotNull ListScope insertJsonSync(int index, @NotNull JsonElement element) {
		return null;
	}

	@Override
	public @NotNull ListScope removeSync(int index) {
		return null;
	}

	@Override
	public @NotNull ListScope removeValueJsonSync(@NotNull JsonElement element) {
		return null;
	}

	@Override
	public @NotNull ListScope removeAllJsonSync(@NotNull JsonElement element) {
		return null;
	}

	@Override
	public @NotNull ListScope removeIfJsonSync(@NotNull Predicate<JsonElement> predicate) {
		return null;
	}

	@Override
	public @NotNull Optional<JsonElement> popBackJsonSync() {
		return Optional.empty();
	}

	@Override
	public @NotNull Optional<JsonElement> popFrontJsonSync() {
		return Optional.empty();
	}

	@Override
	public boolean containsJsonSync(@NotNull JsonElement element) {
		return false;
	}

	@Override
	public int indexOfJsonSync(@NotNull JsonElement element) {
		return 0;
	}

	@Override
	public boolean isEmptySync() {
		return acquireLockAndJedisFunc(jedis -> {
			int size = ListOperations.getSize(jedis, fieldKey, key, collectionName);
			return size == 0;
		});
	}

	@Override
	public int countJsonSync(@NotNull JsonElement element) {
		return 0;
	}

	@Override
	public @NotNull ListScope clearSync() {
		return null;
	}

	@Override
	public @NotNull ListScope list(@NotNull Key resolve) {
		return new RedisListScope(jedisPool, collectionName, fieldKey, key.addKey(resolve));
	}
}
