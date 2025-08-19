package me.adamix.mercury.data;

import com.google.gson.JsonElement;
import me.adamix.mercury.data.codec.Codec;
import me.adamix.mercury.data.key.Key;
import me.adamix.mercury.data.operation.update.UpdateField;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface MercuryCollection {
	@NotNull CompletableFuture<Void> set(@NotNull Key key, @NotNull JsonElement jsonElement);
	void setSync(@NotNull Key key, @NotNull JsonElement jsonElement);
	default <T> void setSync(@NotNull Key key, @NotNull Codec<T> codec, @NotNull T value) {
		setSync(key, codec.encode(value));
	}

	@NotNull CompletableFuture<Optional<JsonElement>> get(@NotNull Key key);
	@NotNull Optional<JsonElement> getSync(@NotNull Key key);
	default <T> @NotNull Optional<T> getSync(@NotNull Key key, @NotNull Codec<T> codec) {
		return getSync(key)
				.filter(e -> !(e.isJsonObject() && e.getAsJsonObject().isEmpty()))
				.map(codec::decode);
	}

	<T> @NotNull CompletableFuture<Void> updateField(@NotNull Key key, @NotNull UpdateField<T> field, boolean insertIfAbsent);
	default <T> @NotNull CompletableFuture<Void> updateField(@NotNull Key key, @NotNull UpdateField<T> field) {
		return updateField(key, field, false);
	}

	<T> void updateFieldSync(@NotNull Key key, @NotNull UpdateField<T> field, boolean insertIfAbsent);
	default <T> void updateFieldSync(@NotNull Key key, @NotNull UpdateField<T> field) {
		updateFieldSync(key, field, false);
	}

	@NotNull CompletableFuture<Boolean> remove(@NotNull Key key);
	boolean removeSync(@NotNull Key key);
}
