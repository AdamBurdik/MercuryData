package me.adamix.mercury.data.redis.query;

import me.adamix.mercury.data.codec.Codec;
import me.adamix.mercury.data.key.Key;
import me.adamix.mercury.data.query.FindQueryBuilder;
import me.adamix.mercury.data.query.QueryResult;
import me.adamix.mercury.data.query.filter.FieldFilter;
import me.adamix.mercury.data.query.filter.QueryFilter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;

public class RedisFindQueryBuilder<T> implements FindQueryBuilder<T> {
	private final @NotNull Codec<T> codec;
	private final @NotNull Function<RedisFindQueryBuilder<T>, QueryResult<T>> executor;
	private final @NotNull List<@NotNull FieldFilter<?>> fieldFilters = new ArrayList<>();
	private int limit = Integer.MAX_VALUE;

	public RedisFindQueryBuilder(@NotNull Codec<T> codec, @NotNull Function<RedisFindQueryBuilder<T>, QueryResult<T>> executor) {
		this.codec = codec;
		this.executor = executor;
	}

	@Override
	public @NotNull <U> FindQueryBuilder<T> where(@NotNull Key fieldKey, @NotNull Codec<U> fieldCodec, @NotNull Predicate<U> filter) {
		fieldFilters.add(new FieldFilter<>(fieldKey, fieldCodec, filter));
		return this;
	}

	@Override
	public @NotNull <U> FindQueryBuilder<T> withFieldFilter(@NotNull FieldFilter<U> filter) {
		fieldFilters.add(filter);
		return this;
	}

	@Override
	public @NotNull FindQueryBuilder<T> limit(int limit) {
		this.limit = limit;
		return this;
	}

	@Override
	public @NotNull QueryResult<T> execute() {
		return executor.apply(this);
	}

	@Override
	public @NotNull CompletableFuture<@NotNull QueryResult<T>> executeAsync() {
		return CompletableFuture.supplyAsync(this::execute);
	}

	public @NotNull List<FieldFilter<?>> getFieldFilter(@NotNull Key key) {
		List<FieldFilter<?>> list = new ArrayList<>();
		for (FieldFilter<?> fieldFilter : fieldFilters) {
			if (fieldFilter.key().equals(key)) {
				list.add(fieldFilter);
			}
		}
		return list;
	}

	public @NotNull Codec<T> codec() {
		return codec;
	}

	public @NotNull Function<RedisFindQueryBuilder<T>, QueryResult<T>> executor() {
		return executor;
	}

	public @NotNull List<@NotNull FieldFilter<?>> fieldFilters() {
		return fieldFilters;
	}

	public int limit() {
		return limit;
	}
}
