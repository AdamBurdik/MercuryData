package me.adamix.mercury.data.redis.query;

import me.adamix.mercury.data.key.Key;
import me.adamix.mercury.data.query.QueryResult;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public record RedisQueryResult<T>(@NotNull Collection<Entry<T>> collection) implements QueryResult<T> {

	@Override
	public boolean isEmpty() {
		return collection.isEmpty();
	}

	@Override
	public @NotNull Optional<T> getFirst() {
		return collection.stream().findFirst().map(Entry::value);
	}

	@Override
	public @NotNull Optional<Key> getFirstKey() {
		return collection.stream().findFirst().map(Entry::key);
	}

	@Override
	public @NotNull Collection<Key> keys() {
		return collection.stream().map(Entry::key).toList();
	}

	@Override
	public @NotNull Collection<T> values() {
		return collection.stream().map(Entry::value).toList();
	}

	public static <T> @NotNull RedisQueryResult<T> empty() {
		return new RedisQueryResult<>(List.of());
	}
}
