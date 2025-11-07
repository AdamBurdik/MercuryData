package me.adamix.mercury.data.scope;

import com.google.gson.JsonElement;
import me.adamix.mercury.data.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface RecordScope {

	// SET FIELD JSON
	@NotNull
	RecordScope setFieldJsonSync(@NotNull Key key, @NotNull JsonElement value);
	default @NotNull CompletableFuture<@NotNull RecordScope> setFieldJson(@NotNull Key key, @NotNull JsonElement value) {
		return CompletableFuture.supplyAsync(() -> setFieldJsonSync(key, value));
	}

	// GET FIELD JSON
	@NotNull
	Optional<JsonElement> getFieldJsonSync(@NotNull Key key);
	default @NotNull CompletableFuture<Optional<JsonElement>> getFieldJson(@NotNull Key key) {
		return CompletableFuture.supplyAsync(() -> getFieldJsonSync(key));
	}

	// REMOVE FIELD JSON
	@NotNull
	RecordScope removeFieldJsonSync(@NotNull String key);
	default @NotNull CompletableFuture<@NotNull RecordScope> removeFieldJson(@NotNull String key) {
		return CompletableFuture.supplyAsync(() -> removeFieldJsonSync(key));
	}

	// CHECK IF FIELD JSON EXISTS
	boolean fieldJsonExistsSync(@NotNull String key);
	default @NotNull CompletableFuture<Boolean> fieldJsonExists(@NotNull String key) {
		return CompletableFuture.supplyAsync(() -> fieldJsonExistsSync(key));
	}

	// CLEAR RECORD
	@NotNull RecordScope clearSync();
	default @NotNull CompletableFuture<@NotNull RecordScope> clear() {
		return CompletableFuture.supplyAsync(this::clearSync);
	}

	// LIST FIELD JSON KEYS
	@NotNull
	Collection<String> listFieldsSync();
	default @NotNull CompletableFuture<@NotNull Collection<String>> listFields() {
		return CompletableFuture.supplyAsync(this::listFieldsSync);
	}
}