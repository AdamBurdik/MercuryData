package me.adamix.mercury.data.scope;

import com.google.gson.JsonElement;
import me.adamix.mercury.data.codec.Codec;
import me.adamix.mercury.data.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public interface ListScope {

	// PUSH JSON
	@NotNull ListScope pushBackJsonSync(@NotNull JsonElement element);
	default @NotNull CompletableFuture<ListScope> pushBackJson(@NotNull JsonElement element) {
		return CompletableFuture.supplyAsync(() -> pushBackJsonSync(element));
	}

	@NotNull ListScope pushFrontJsonSync(@NotNull JsonElement element);
	default @NotNull CompletableFuture<ListScope> pushFrontJson(@NotNull JsonElement element) {
		return CompletableFuture.supplyAsync(() -> pushFrontJsonSync(element));
	}

	// PUSH (codec)
	default <T> @NotNull ListScope pushBackSync(@NotNull Codec<T> codec, @NotNull T value) {
		return pushBackJsonSync(codec.encode(value));
	}
	default <T> @NotNull CompletableFuture<ListScope> pushBack(@NotNull Codec<T> codec, @NotNull T value) {
		return pushBackJson(codec.encode(value));
	}

	default @NotNull <T> ListScope pushFrontSync(@NotNull Codec<T> codec, @NotNull T value) {
		return pushFrontJsonSync(codec.encode(value));
	}
	default @NotNull <T> CompletableFuture<ListScope> pushFront(@NotNull Codec<T> codec, @NotNull T value) {
		return pushFrontJson(codec.encode(value));
	}

	// SIZE
	int sizeSync();
	default @NotNull CompletableFuture<Integer> size() {
		return CompletableFuture.supplyAsync(this::sizeSync);
	}

	// GET JSON
	@NotNull Optional<JsonElement> getJsonSync(int index);
	default @NotNull CompletableFuture<Optional<JsonElement>> getJson(int index) {
		return CompletableFuture.supplyAsync(() -> getJsonSync(index));
	}

	// GET (codec)
	default <T> @NotNull Optional<T> getSync(int index, @NotNull Codec<T> codec) {
		return getJsonSync(index).map(codec::decode);
	}
	default <T> @NotNull CompletableFuture<Optional<T>> get(int index, @NotNull Codec<T> codec) {
		return getJson(index).thenApply(opt -> opt.map(codec::decode));
	}

	// GET FIRST/LAST JSON (without removing)
	default @NotNull Optional<JsonElement> getFirstJsonSync() {
		return getJsonSync(0);
	}
	default @NotNull CompletableFuture<Optional<JsonElement>> getFirstJson() {
		return CompletableFuture.supplyAsync(this::getFirstJsonSync);
	}

	@NotNull Optional<JsonElement> getLastJsonSync();
	default @NotNull CompletableFuture<Optional<JsonElement>> getLastJson() {
		return CompletableFuture.supplyAsync(this::getLastJsonSync);
	}

	// GET FIRST/LAST (codec)
	default <T> @NotNull Optional<T> getFirstSync(@NotNull Codec<T> codec) {
		return getFirstJsonSync().map(codec::decode);
	}
	default <T> @NotNull CompletableFuture<Optional<T>> getFirst(@NotNull Codec<T> codec) {
		return getFirstJson().thenApply(opt -> opt.map(codec::decode));
	}

	default <T> @NotNull Optional<T> getLastSync(@NotNull Codec<T> codec) {
		return getLastJsonSync().map(codec::decode);
	}
	default <T> @NotNull CompletableFuture<Optional<T>> getLast(@NotNull Codec<T> codec) {
		return getLastJson().thenApply(opt -> opt.map(codec::decode));
	}

	// GET RANGE JSON (sublist)
	@NotNull Collection<JsonElement> getRangeJsonSync(int fromIndex, int toIndex);
	default @NotNull CompletableFuture<Collection<JsonElement>> getRangeJson(int fromIndex, int toIndex) {
		return CompletableFuture.supplyAsync(() -> getRangeJsonSync(fromIndex, toIndex));
	}

	// GET RANGE (codec)
	default <T> @NotNull Collection<T> getRangeSync(int fromIndex, int toIndex, @NotNull Codec<T> codec) {
		return getRangeJsonSync(fromIndex, toIndex).stream()
				.map(codec::decode)
				.collect(java.util.stream.Collectors.toList());
	}
	default <T> @NotNull CompletableFuture<Collection<T>> getRange(int fromIndex, int toIndex, @NotNull Codec<T> codec) {
		return getRangeJson(fromIndex, toIndex).thenApply(col -> col.stream()
				.map(codec::decode)
				.collect(java.util.stream.Collectors.toList()));
	}

	// GET ALL JSON
	@NotNull Collection<JsonElement> getAllJsonSync();
	default @NotNull CompletableFuture<Collection<JsonElement>> getAllJson() {
		return CompletableFuture.supplyAsync(this::getAllJsonSync);
	}

	// GET ALL (codec)
	default <T> @NotNull Collection<T> getAllSync(@NotNull Codec<T> codec) {
		return getAllJsonSync().stream()
				.map(codec::decode)
				.collect(java.util.stream.Collectors.toList());
	}
	default <T> @NotNull CompletableFuture<Collection<T>> getAll(@NotNull Codec<T> codec) {
		return getAllJson().thenApply(col -> col.stream()
				.map(codec::decode)
				.collect(java.util.stream.Collectors.toList()));
	}

	// INSERT JSON
	@NotNull ListScope insertJsonSync(int index, @NotNull JsonElement element);
	default @NotNull CompletableFuture<ListScope> insertJson(int index, @NotNull JsonElement element) {
		return CompletableFuture.supplyAsync(() -> insertJsonSync(index, element));
	}

	// INSERT (codec)
	default <T> @NotNull ListScope insertSync(int index, @NotNull Codec<T> codec, @NotNull T value) {
		return insertJsonSync(index, codec.encode(value));
	}
	default <T> @NotNull CompletableFuture<ListScope> insert(int index, @NotNull Codec<T> codec, @NotNull T value) {
		return insertJson(index, codec.encode(value));
	}

	// REMOVE JSON
	@NotNull ListScope removeSync(int index);
	default @NotNull CompletableFuture<ListScope> remove(int index) {
		return CompletableFuture.supplyAsync(() -> removeSync(index));
	}

	// REMOVE BY VALUE JSON (removes first occurrence)
	@NotNull ListScope removeValueJsonSync(@NotNull JsonElement element);
	default @NotNull CompletableFuture<ListScope> removeValueJson(@NotNull JsonElement element) {
		return CompletableFuture.supplyAsync(() -> removeValueJsonSync(element));
	}

	// REMOVE BY VALUE (codec)
	default <T> @NotNull ListScope removeValueSync(@NotNull Codec<T> codec, @NotNull T value) {
		return removeValueJsonSync(codec.encode(value));
	}
	default <T> @NotNull CompletableFuture<ListScope> removeValue(@NotNull Codec<T> codec, @NotNull T value) {
		return removeValueJson(codec.encode(value));
	}

	// REMOVE ALL OCCURRENCES JSON
	@NotNull ListScope removeAllJsonSync(@NotNull JsonElement element);
	default @NotNull CompletableFuture<ListScope> removeAllJson(@NotNull JsonElement element) {
		return CompletableFuture.supplyAsync(() -> removeAllJsonSync(element));
	}

	// REMOVE ALL OCCURRENCES (codec)
	default <T> @NotNull ListScope removeAllSync(@NotNull Codec<T> codec, @NotNull T value) {
		return removeAllJsonSync(codec.encode(value));
	}
	default <T> @NotNull CompletableFuture<ListScope> removeAll(@NotNull Codec<T> codec, @NotNull T value) {
		return removeAllJson(codec.encode(value));
	}

	// REMOVE IF JSON (filter-based removal)
	@NotNull ListScope removeIfJsonSync(@NotNull Predicate<JsonElement> predicate);
	default @NotNull CompletableFuture<ListScope> removeIfJson(@NotNull Predicate<JsonElement> predicate) {
		return CompletableFuture.supplyAsync(() -> removeIfJsonSync(predicate));
	}

	// REMOVE IF (codec)
	default <T> @NotNull ListScope removeIfSync(@NotNull Codec<T> codec, @NotNull Predicate<T> predicate) {
		return removeIfJsonSync(json -> predicate.test(codec.decode(json)));
	}
	default <T> @NotNull CompletableFuture<ListScope> removeIf(@NotNull Codec<T> codec, @NotNull Predicate<T> predicate) {
		return removeIfJson(json -> predicate.test(codec.decode(json)));
	}

	// POP JSON
	@NotNull Optional<JsonElement> popBackJsonSync();
	default @NotNull CompletableFuture<Optional<JsonElement>> popBackJson() {
		return CompletableFuture.supplyAsync(this::popBackJsonSync);
	}

	@NotNull Optional<JsonElement> popFrontJsonSync();
	default @NotNull CompletableFuture<Optional<JsonElement>> popFrontJson() {
		return CompletableFuture.supplyAsync(this::popFrontJsonSync);
	}

	// POP (codec)
	default <T> @NotNull Optional<T> popBackSync(@NotNull Codec<T> codec) {
		return popBackJsonSync().map(codec::decode);
	}
	default <T> @NotNull CompletableFuture<Optional<T>> popBack(@NotNull Codec<T> codec) {
		return popBackJson().thenApply(opt -> opt.map(codec::decode));
	}

	default <T> @NotNull Optional<T> popFrontSync(@NotNull Codec<T> codec) {
		return popFrontJsonSync().map(codec::decode);
	}
	default <T> @NotNull CompletableFuture<Optional<T>> popFront(@NotNull Codec<T> codec) {
		return popFrontJson().thenApply(opt -> opt.map(codec::decode));
	}

	// CONTAINS JSON
	boolean containsJsonSync(@NotNull JsonElement element);
	default @NotNull CompletableFuture<Boolean> containsJson(@NotNull JsonElement element) {
		return CompletableFuture.supplyAsync(() -> containsJsonSync(element));
	}

	// CONTAINS (codec)
	default <T> boolean containsSync(@NotNull Codec<T> codec, @NotNull T value) {
		return containsJsonSync(codec.encode(value));
	}
	default <T> @NotNull CompletableFuture<Boolean> contains(@NotNull Codec<T> codec, @NotNull T value) {
		return containsJson(codec.encode(value));
	}

	// INDEX OF JSON (find position)
	int indexOfJsonSync(@NotNull JsonElement element);
	default @NotNull CompletableFuture<Integer> indexOfJson(@NotNull JsonElement element) {
		return CompletableFuture.supplyAsync(() -> indexOfJsonSync(element));
	}

	// INDEX OF (codec)
	default <T> int indexOfSync(@NotNull Codec<T> codec, @NotNull T value) {
		return indexOfJsonSync(codec.encode(value));
	}
	default <T> @NotNull CompletableFuture<Integer> indexOf(@NotNull Codec<T> codec, @NotNull T value) {
		return indexOfJson(codec.encode(value));
	}

	// IS EMPTY JSON
	boolean isEmptySync();
	default @NotNull CompletableFuture<Boolean> isEmpty() {
		return CompletableFuture.supplyAsync(this::isEmptySync);
	}

	// COUNT JSON (count occurrences of element)
	int countJsonSync(@NotNull JsonElement element);
	default @NotNull CompletableFuture<Integer> countJson(@NotNull JsonElement element) {
		return CompletableFuture.supplyAsync(() -> countJsonSync(element));
	}

	// COUNT (codec)
	default <T> int countSync(@NotNull Codec<T> codec, @NotNull T value) {
		return countJsonSync(codec.encode(value));
	}
	default <T> @NotNull CompletableFuture<Integer> count(@NotNull Codec<T> codec, @NotNull T value) {
		return countJson(codec.encode(value));
	}

	// CLEAR JSON
	@NotNull ListScope clearSync();
	default @NotNull CompletableFuture<ListScope> clear() {
		return CompletableFuture.supplyAsync(this::clearSync);
	}

	// LIST (Sub lists)
	@NotNull ListScope list(@NotNull Key key);
}