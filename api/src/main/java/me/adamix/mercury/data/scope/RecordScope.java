package me.adamix.mercury.data.scope;

import com.google.gson.JsonElement;
import me.adamix.mercury.data.codec.Codec;
import me.adamix.mercury.data.exception.MissingFieldException;
import me.adamix.mercury.data.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public interface RecordScope {

	// SET FIELD JSON
	@NotNull
	RecordScope setFieldJsonSync(@NotNull Key key, @NotNull JsonElement value);
	default @NotNull CompletableFuture<@NotNull RecordScope> setFieldJson(@NotNull Key key, @NotNull JsonElement value) {
		return CompletableFuture.supplyAsync(() -> setFieldJsonSync(key, value));
	}
	default <T> @NotNull RecordScope setFieldSync(@NotNull Key key, @NotNull Codec<T> codec, @NotNull T value) {
		return setFieldJsonSync(key, codec.encode(value));
	}

	default <T> @NotNull CompletableFuture<@NotNull RecordScope> setField(@NotNull Key key, @NotNull Codec<T> codec, @NotNull T value) {
		return setFieldJson(key, codec.encode(value));
	}

	// GET FIELD JSON
	@NotNull
	Optional<JsonElement> getFieldJsonSync(@NotNull Key key);
	default @NotNull CompletableFuture<Optional<JsonElement>> getFieldJson(@NotNull Key key) {
		return CompletableFuture.supplyAsync(() -> getFieldJsonSync(key));
	}
	default <T> @NotNull Optional<T> getFieldSync(@NotNull Key key, @NotNull Codec<T> codec) {
		return getFieldJsonSync(key).flatMap(json -> {
			try {
				return Optional.ofNullable(codec.decode(json));
			} catch (MissingFieldException e) {
				return Optional.empty();
			}
		});
	}

	default <T> @NotNull CompletableFuture<Optional<T>> getField(@NotNull Key key, @NotNull Codec<T> codec) {
		return getFieldJson(key).thenApply(opt -> opt.flatMap(json -> {
            try {
                return Optional.ofNullable(codec.decode(json));
            } catch (MissingFieldException e) {
                return Optional.empty();
            }
        }));
	}

	default <T> @NotNull T getFieldOrDefaultSync(@NotNull Key key, @NotNull Codec<T> codec, @NotNull T defaultValue) {
		return getFieldSync(key, codec).orElse(defaultValue);
	}

	default <T> @NotNull CompletableFuture<T> getFieldJsonOrDefault(@NotNull Key key, @NotNull Codec<T> codec, @NotNull T defaultValue) {
		return getField(key, codec).thenApply(opt -> opt.orElse(defaultValue));
	}

	default <T> @NotNull T getFieldOrComputeSync(@NotNull Key key, @NotNull Codec<T> codec, @NotNull Supplier<T> defaultSupplier) {
		return getFieldSync(key, codec).orElseGet(defaultSupplier);
	}

	default <T> @NotNull CompletableFuture<T> getFieldOrCompute(@NotNull Key key, @NotNull Codec<T> codec, @NotNull Supplier<T> defaultSupplier) {
		return getField(key, codec).thenApply(opt -> opt.orElseGet(defaultSupplier));
	}

	// UPDATE FIELD IF PRESENT
	default <T> @NotNull RecordScope updateFieldIfPresentSync(@NotNull Key key, @NotNull Codec<T> codec, @NotNull UnaryOperator<T> updater) {
		// ToDO Remake with native implementation
		Optional<T> current = getFieldSync(key, codec);
		if (current.isPresent()) {
			T updated = updater.apply(current.get());
			setFieldSync(key, codec, updated);
		}
		return this;
	}

	default <T> @NotNull CompletableFuture<@NotNull RecordScope> updateFieldIfPresent(@NotNull Key key, @NotNull Codec<T> codec, @NotNull UnaryOperator<T> updater) {
		// ToDO Remake with native implementation
		return getField(key, codec).thenCompose(opt -> {
			if (opt.isPresent()) {
				T updated = updater.apply(opt.get());
				return setField(key, codec, updated);
			}
			return CompletableFuture.completedFuture(this);
		});
	}

	// COMPUTE FIELD IF ABSENT
	default <T> @NotNull T computeFieldJsonIfAbsentSync(@NotNull Key key, @NotNull Codec<T> codec, @NotNull Supplier<T> supplier) {
		// ToDO Remake with native implementation
		Optional<T> existing = getFieldSync(key, codec);
		if (existing.isPresent()) {
			return existing.get();
		}
		T value = supplier.get();
		setFieldSync(key, codec, value);
		return value;
	}

	default <T> @NotNull CompletableFuture<T> computeFieldJsonIfAbsent(@NotNull Key key, @NotNull Codec<T> codec, @NotNull Supplier<T> supplier) {
		// ToDO Remake with native implementation
		return getField(key, codec).thenCompose(opt -> {
			if (opt.isPresent()) {
				return CompletableFuture.completedFuture(opt.get());
			}
			T value = supplier.get();
			return setField(key, codec, value).thenApply(scope -> value);
		});
	}

	// REMOVE FIELD JSON
	@NotNull
	RecordScope removeFieldJsonSync(@NotNull Key key);
	default @NotNull CompletableFuture<@NotNull RecordScope> removeFieldJson(@NotNull Key key) {
		return CompletableFuture.supplyAsync(() -> removeFieldJsonSync(key));
	}


	// CHECK IF FIELD JSON EXISTS
	boolean fieldJsonExistsSync(@NotNull Key key);
	default @NotNull CompletableFuture<Boolean> fieldJsonExists(@NotNull Key key) {
		return CompletableFuture.supplyAsync(() -> fieldJsonExistsSync(key));
	}

	// CLEAR RECORD
	@NotNull RecordScope clearSync();
	default @NotNull CompletableFuture<@NotNull RecordScope> clear() {
		return CompletableFuture.supplyAsync(this::clearSync);
	}

	// LIST FIELD JSON KEYS
	@NotNull
	Collection<String> listFieldsSync(boolean recursive);
	default @NotNull CompletableFuture<@NotNull Collection<String>> listFields(boolean recursive) {
		return CompletableFuture.supplyAsync(()  -> listFieldsSync(recursive));
	}

	// LIST
	@NotNull ListScope list(@NotNull Key key);
}