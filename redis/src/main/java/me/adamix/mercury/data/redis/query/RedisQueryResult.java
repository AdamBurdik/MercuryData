package me.adamix.mercury.data.redis.query;

import me.adamix.mercury.data.query.QueryResult;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public record RedisQueryResult<T>(@NotNull Collection<T> collection) implements QueryResult<T> {

	@Override
	public boolean isEmpty() {
		return collection.isEmpty();
	}

	@Override
	public @NotNull Optional<T> getFirst() {
		return collection.stream().findFirst();
	}

	public static <T> @NotNull RedisQueryResult<T> empty() {
		return new RedisQueryResult<>(List.of());
	}
}
