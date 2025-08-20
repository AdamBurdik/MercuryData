package me.adamix.mercury.data.query.filter;

import me.adamix.mercury.data.codec.Codec;
import me.adamix.mercury.data.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public record FieldFilter<T>(@NotNull Key key, @NotNull Codec<T> codec, @NotNull Predicate<T> filter) implements QueryFilter {
	public boolean test(@NotNull T value) {
		return filter.test(value);
	}
}
