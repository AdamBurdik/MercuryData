package me.adamix.mercury.data.codec;

import com.google.gson.JsonElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface Codec<T> {
	Codec<String> STRING = CodecImpl.STRING;
	Codec<Integer> INT = CodecImpl.INT;
	Codec<Double> DOUBLE = CodecImpl.DOUBLE;
	Codec<Float> FLOAT = CodecImpl.FLOAT;
	Codec<Long> LONG = CodecImpl.LONG;
	Codec<Short> SHORT = CodecImpl.SHORT;
	Codec<Byte> BYTE = CodecImpl.BYTE;
	Codec<Boolean> BOOLEAN = CodecImpl.BOOLEAN;
	Codec<Character> CHAR = CodecImpl.CHAR;

	Codec<UUID> UUID = CodecImpl.UUID;

	JsonElement encode(T value);
	T decode(JsonElement json);

	default @NotNull Optional<T> decodeOptional(@NotNull JsonElement jsonElement) {
		return Optional.ofNullable(decode(jsonElement));
	}

	default @NotNull Codec<T> optional() {
		return new CodecImpl.OptionalCodec<>(this, null);
	}

	default @NotNull Codec<T> optional(@NotNull T defaultValue) {
		return new CodecImpl.OptionalCodec<>(this, defaultValue);
	}

	default @NotNull Codec<List<T>> list() {
		return new CodecImpl.ListCodec<>(this);
	}

	default <V> @NotNull Codec<Map<T, V>> map(@NotNull Codec<V> valueCodec) {
		return new CodecImpl.MapCodec<>(this, valueCodec);
	}
}
