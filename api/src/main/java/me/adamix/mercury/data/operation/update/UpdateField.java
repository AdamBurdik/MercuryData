package me.adamix.mercury.data.operation.update;

import me.adamix.mercury.data.codec.Codec;
import me.adamix.mercury.data.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public record UpdateField<T>(@NotNull Key key, @NotNull Codec<T> codec, @NotNull Function<T, T> function, boolean isConstant) {
	public static <T> @NotNull UpdateField<T> of(@NotNull Key key, @NotNull Codec<T> codec, @NotNull T value) {
		return new UpdateField<>(key, codec, v -> value, true);
	}

	public static <T> @NotNull UpdateField<T> of(@NotNull String key, @NotNull Codec<T> codec, @NotNull T value) {
		return new UpdateField<>(Key.parse(key), codec, v -> value, true);
	}

	public static <T> @NotNull UpdateField<T> of(@NotNull Key key, @NotNull Codec<T> codec, @NotNull Function<T, T> func) {
		return new UpdateField<>(key, codec, func, false);
	}

	public static <T> @NotNull UpdateField<T> of(@NotNull String key, @NotNull Codec<T> codec, @NotNull Function<T, T> func) {
		return new UpdateField<>(Key.parse(key), codec, func, false);
	}
}
