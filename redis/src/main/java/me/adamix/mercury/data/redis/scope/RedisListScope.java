package me.adamix.mercury.data.redis.scope;

import com.google.gson.JsonElement;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
				ListOperations.insert(jedis, fieldKey, key, collectionName, element, size);
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
		try {
			return acquireLockAndJedisFunc(jedis -> {
				int size = ListOperations.getSize(jedis, fieldKey, key, collectionName);
				// Validate: fromIndex must be non-negative
				if (fromIndex < 0) {
					throw new IndexOutOfBoundsException(
							"fromIndex must be >= 0, but was " + fromIndex +
									" for list at key: " + key +
									" in collection: " + collectionName
					);
				}

				// Validate: fromIndex must not exceed toIndex
				if (fromIndex > toIndex) {
					throw new IndexOutOfBoundsException(
							"fromIndex (" + fromIndex + ") must not exceed toIndex (" + toIndex + ")" +
									" for list at key: " + key +
									" in collection: " + collectionName
					);
				}

				// Validate: toIndex must not exceed list size
				if (toIndex > size) {
					throw new IndexOutOfBoundsException(
							"toIndex (" + toIndex + ") exceeds list size (" + size + ")" +
									" at key: " + key +
									" in collection: " + collectionName
					);
				}

				// TODO Rewrite to use just just hgetall call
				Collection<JsonElement> elements =  new ArrayList<>();
				for (int i = fromIndex; i < toIndex; i++) {
					elements.add(ListOperations.get(jedis, fieldKey, key, collectionName, i));
				}

				return elements;
			});
		} catch (IndexOutOfBoundsException e) {
			throw e;
		} catch (Exception e) {
			LOGGER.error("Exception occurred while getting all element in range from a list in redis collection", e);
		}

		return Collections.emptyList();
	}

	@Override
	public @NotNull Collection<JsonElement> getAllJsonSync() {
		try {
			return acquireLockAndJedisFunc(jedis -> {
				int size = ListOperations.getSize(jedis, fieldKey, key, collectionName);

				// TODO Rewrite to use just just hgetall call
				Collection<JsonElement> elements =  new ArrayList<>();
				for (int i = 0; i < size; i++) {
					elements.add(ListOperations.get(jedis, fieldKey, key, collectionName, i));
				}

				return elements;
			});
		} catch (Exception e) {
			LOGGER.error("Exception occurred while getting all element from a list in redis collection", e);
		}

		return Collections.emptyList();
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
		acquireLockAndJedis(jedis -> {
			ListOperations.ensureValidList(jedis, fieldKey, key, collectionName);

			String fullKey = fieldKey.withCollectionName(collectionName);
			Map<String, String> map = jedis.hgetAll(fullKey);
			if (map == null) {
				throw new IllegalStateException("List scope does not exist: " + fieldKey.withCollectionName(collectionName) + " - " + key);
			}

			String base = key.toString();

			for (String childKey : map.keySet()) {
				if (childKey.startsWith(base)) {
					jedis.hdel(fullKey, childKey);
				}
			}

			ListOperations.setSize(jedis, fieldKey, key, collectionName, 0);
		});
		return this;
	}

	@Override
	public @NotNull ListScope list(@NotNull Key resolve) {
		return new RedisListScope(jedisPool, collectionName, fieldKey, key.addKey(resolve));
	}

	@Override
	public @NotNull ListScope list(int index) {
		return new RedisListScope(jedisPool, collectionName, fieldKey, key.addPart(String.valueOf(index), ':'));
	}
}
