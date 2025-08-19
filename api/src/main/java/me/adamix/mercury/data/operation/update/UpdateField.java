package me.adamix.mercury.data.operation.update;

import me.adamix.mercury.data.codec.Codec;
import me.adamix.mercury.data.key.Key;
import org.jetbrains.annotations.NotNull;

public record UpdateField<T>(@NotNull Key key, @NotNull Codec<T> codec, @NotNull T value) {
	public static <T> @NotNull UpdateField<T> of(@NotNull Key key, @NotNull Codec<T> codec, @NotNull T value) {
		return new UpdateField<>(key, codec, value);
	}

	public static <T> @NotNull UpdateField<T> of(@NotNull String key, @NotNull Codec<T> codec, @NotNull T value) {
		return new UpdateField<>(Key.parse(key), codec, value);
	}
}
