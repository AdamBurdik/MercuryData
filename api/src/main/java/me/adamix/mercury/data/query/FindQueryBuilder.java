package me.adamix.mercury.data.query;

import me.adamix.mercury.data.codec.Codec;
import me.adamix.mercury.data.key.Key;
import me.adamix.mercury.data.query.filter.FieldFilter;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public interface FindQueryBuilder<T> {
	<U> @NotNull FindQueryBuilder<T> where(@NotNull Key fieldKey, @NotNull Codec<U> fieldCodec, @NotNull Predicate<U> filter);
	<U> @NotNull FindQueryBuilder<T> withFieldFilter(@NotNull FieldFilter<U> filter);
	@NotNull FindQueryBuilder<T> limit(int limit);

	@NotNull QueryResult<T> execute();
	@NotNull CompletableFuture<@NotNull QueryResult<T>> executeAsync();
}
