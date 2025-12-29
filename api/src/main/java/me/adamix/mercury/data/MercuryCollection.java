package me.adamix.mercury.data;


import com.google.gson.JsonElement;
import me.adamix.mercury.data.codec.Codec;
import me.adamix.mercury.data.exception.MissingFieldException;
import me.adamix.mercury.data.key.Key;
import me.adamix.mercury.data.query.FindQueryBuilder;
import me.adamix.mercury.data.scope.RecordScope;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface MercuryCollection {

	// SET ENTITY
	@NotNull MercuryCollection setJsonSync(@NotNull Key key, @NotNull JsonElement value);
	default @NotNull CompletableFuture<@NotNull MercuryCollection> setJson(@NotNull Key key, @NotNull JsonElement value) {
		return CompletableFuture.supplyAsync(() -> setJsonSync(key, value));
	}
	default <T> @NotNull MercuryCollection setSync(@NotNull Key key, @NotNull Codec<T> codec, @NotNull T value) {
		return setJsonSync(key, codec.encode(value));
	}
	default <T> @NotNull CompletableFuture<@NotNull MercuryCollection> set(@NotNull Key key, @NotNull Codec<T> codec, @NotNull T value) {
		return setJson(key, codec.encode(value));
	}

	// GET ENTITY
	@NotNull Optional<JsonElement> getJsonSync(@NotNull Key key);
	default @NotNull CompletableFuture<Optional<JsonElement>> getJson(@NotNull Key key) {
		return CompletableFuture.supplyAsync(() -> getJsonSync(key));
	}
	default <T> @NotNull Optional<T> getSync(@NotNull Key key, @NotNull Codec<T> codec) {
		return getJsonSync(key).flatMap(json -> {
			try {
				return Optional.ofNullable(codec.decode(json));
			} catch (MissingFieldException e) {
				return Optional.empty();
			}
		});
	}

	default <T> @NotNull CompletableFuture<Optional<T>> get(@NotNull Key key, @NotNull Codec<T> codec) {
		return getJson(key).thenApply(opt -> opt.flatMap(json -> {
            try {
                return Optional.ofNullable(codec.decode(json));
            } catch (MissingFieldException e) {
                return Optional.empty();
            }
        }));
	}
	default <T> @NotNull T getOrDefaultSync(@NotNull Key key, @NotNull Codec<T> codec, @NotNull T defaultValue) {
		return getSync(key, codec).orElse(defaultValue);
	}

	default <T> @NotNull CompletableFuture<T> getOrDefault(@NotNull Key key, @NotNull Codec<T> codec, @NotNull T defaultValue) {
		return get(key, codec).thenApply(opt -> opt.orElse(defaultValue));
	}

	// REMOVE ENTITY
	@NotNull MercuryCollection removeSync(@NotNull Key key);
	default @NotNull CompletableFuture<@NotNull MercuryCollection> remove(@NotNull Key key) {
		return CompletableFuture.supplyAsync(() -> removeSync(key));
	}

	// CHECK IF ENTITY EXISTS
	boolean existsSync(@NotNull Key key);
	default @NotNull CompletableFuture<Boolean> exists(@NotNull Key key) {
		return CompletableFuture.supplyAsync(() -> existsSync(key));
	}

	// CLEAR COLLECTION
	@NotNull MercuryCollection clearSync();
	default @NotNull CompletableFuture<@NotNull MercuryCollection> clear() {
		return CompletableFuture.supplyAsync(this::clearSync);
	}

	// RECORD SCOPE
	@NotNull RecordScope record(@NotNull Key key);

	// UPDATE IF PRESENT
	default <T> @NotNull MercuryCollection updateIfPresentSync(@NotNull Key key, @NotNull Codec<T> codec, @NotNull java.util.function.UnaryOperator<T> updater) {
		Optional<T> current = getSync(key, codec);
		if (current.isPresent()) {
			T updated = updater.apply(current.get());
			setSync(key, codec, updated);
		}
		return this;
	}

	default <T> @NotNull CompletableFuture<@NotNull MercuryCollection> updateIfPresent(@NotNull Key key, @NotNull Codec<T> codec, @NotNull java.util.function.UnaryOperator<T> updater) {
		return get(key, codec).thenCompose(opt -> {
			if (opt.isPresent()) {
				T updated = updater.apply(opt.get());
				return set(key, codec, updated);
			}
			return CompletableFuture.completedFuture(this);
		});
	}

	// COMPUTE IF ABSENT
	default <T> @NotNull T computeIfAbsentSync(@NotNull Key key, @NotNull Codec<T> codec, @NotNull java.util.function.Supplier<T> supplier) {
		Optional<T> existing = getSync(key, codec);
		if (existing.isPresent()) {
			return existing.get();
		}
		T value = supplier.get();
		setSync(key, codec, value);
		return value;
	}

	default <T> @NotNull CompletableFuture<T> computeIfAbsent(@NotNull Key key, @NotNull Codec<T> codec, @NotNull java.util.function.Supplier<T> supplier) {
		return get(key, codec).thenCompose(opt -> {
			if (opt.isPresent()) {
				return CompletableFuture.completedFuture(opt.get());
			}
			T value = supplier.get();
			return set(key, codec, value).thenApply(col -> value);
		});
	}

	// FIND
	<T> @NotNull FindQueryBuilder<T> find(@NotNull Codec<T> codec);

	@NotNull Set<Key> keysSync();
	default @NotNull CompletableFuture<@NotNull Set<Key>> keys() {
		return CompletableFuture.supplyAsync(this::keysSync);
	}
}
