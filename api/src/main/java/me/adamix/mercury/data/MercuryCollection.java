package me.adamix.mercury.data;


import com.google.gson.JsonElement;
import me.adamix.mercury.data.key.Key;
import me.adamix.mercury.data.scope.RecordScope;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface MercuryCollection {

	// SET ENTITY
	@NotNull
	MercuryCollection setJsonSync(@NotNull Key key, @NotNull JsonElement value);
	default @NotNull CompletableFuture<@NotNull MercuryCollection> setJson(@NotNull Key key, @NotNull JsonElement value) {
		return CompletableFuture.supplyAsync(() -> setJsonSync(key, value));
	}

	// GET ENTITY
	@NotNull
	Optional<JsonElement> getJsonSync(@NotNull Key key);
	default @NotNull CompletableFuture<Optional<JsonElement>> getJson(@NotNull Key key) {
		return CompletableFuture.supplyAsync(() -> getJsonSync(key));
	}

	// REMOVE ENTITY
	@NotNull
	MercuryCollection removeJsonSync(@NotNull Key key);
	default @NotNull CompletableFuture<@NotNull MercuryCollection> removeJson(@NotNull Key key) {
		return CompletableFuture.supplyAsync(() -> removeJsonSync(key));
	}

	// CHECK IF ENTITY EXISTS
	boolean jsonExistsSync(@NotNull Key key);
	default @NotNull CompletableFuture<Boolean> jsonExists(@NotNull Key key) {
		return CompletableFuture.supplyAsync(() -> jsonExistsSync(key));
	}

	// CLEAR COLLECTION
	@NotNull
	MercuryCollection clearSync();
	default @NotNull CompletableFuture<@NotNull MercuryCollection> clear() {
		return CompletableFuture.supplyAsync(this::clearSync);
	}

	// RECORD SCOPE
	@NotNull
	RecordScope record(@NotNull String key);

}
