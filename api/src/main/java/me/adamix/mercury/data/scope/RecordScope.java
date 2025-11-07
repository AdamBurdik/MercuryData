package me.adamix.mercury.data.scope;

import com.google.gson.JsonElement;
import me.adamix.mercury.data.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface RecordScope {

	// SET FIELD
	@NotNull
	RecordScope setFieldSync(@NotNull Key key, @NotNull JsonElement value);
	default @NotNull CompletableFuture<@NotNull RecordScope> setField(@NotNull Key key, @NotNull JsonElement value) {
		return CompletableFuture.supplyAsync(() -> setFieldSync(key, value));
	}

	// GET FIELD
	@NotNull
	Optional<JsonElement> getFieldSync(@NotNull Key key);
	default @NotNull CompletableFuture<Optional<JsonElement>> getField(@NotNull Key key) {
		return CompletableFuture.supplyAsync(() -> getFieldSync(key));
	}

	// REMOVE FIELD
	@NotNull
	RecordScope removeFieldSync(@NotNull String key);
	default @NotNull CompletableFuture<@NotNull RecordScope> removeField(@NotNull String key) {
		return CompletableFuture.supplyAsync(() -> removeFieldSync(key));
	}

	// CHECK IF FIELD EXISTS
	boolean fieldExistsSync(@NotNull String key);
	default @NotNull CompletableFuture<Boolean> fieldExists(@NotNull String key) {
		return CompletableFuture.supplyAsync(() -> fieldExistsSync(key));
	}

	// CLEAR RECORD
	@NotNull RecordScope clearSync();
	default @NotNull CompletableFuture<@NotNull RecordScope> clear() {
		return CompletableFuture.supplyAsync(this::clearSync);
	}

	// LIST FIELDS
	@NotNull
	Collection<String> listFieldsSync();
	default @NotNull CompletableFuture<@NotNull Collection<String>> listFields() {
		return CompletableFuture.supplyAsync(this::listFieldsSync);
	}
}