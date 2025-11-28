package me.adamix.mercury.data.redis.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import me.adamix.mercury.data.key.Key;
import me.adamix.mercury.data.metadata.Metadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.Jedis;

public class JsonUtils {

	public static String getRawValue(JsonElement element) {
		return me.adamix.mercury.data.utils.JsonUtils.getRawValue(element);
	}

	public static @NotNull JsonElement parseString(@Nullable String input) {
		return me.adamix.mercury.data.utils.JsonUtils.parseString(input);
	}


	public static void addNestedProperty(
			@NotNull JsonObject obj,
			@NotNull String dottedKey,
			@NotNull JsonElement value,
			@NotNull String... regexes
	) {
		if (regexes.length == 0) {
			regexes = new String[]{"\\."}; // default to dot
		}

		// Combine regexes into one pattern for splitting
		String combinedRegex = String.join("|", regexes);

		String[] parts = dottedKey.split(combinedRegex);
		JsonObject current = obj;

		for (int i = 0; i < parts.length - 1; i++) {
			String part = parts[i];
			if (!current.has(part) || !current.get(part).isJsonObject()) {
				current.add(part, new JsonObject());
			}
			current = current.getAsJsonObject(part);
		}

		current.add(parts[parts.length - 1], value);
	}

	public static @Nullable JsonObject getObject(
			@NotNull JsonObject jsonObject,
			@NotNull Key key
	) {
		JsonElement current = jsonObject;
		for (Key.KeyPart part : key.getParts()) {
			if (current == null || current.isJsonNull()) return null;

			if (part.getSeparator() == '.') {
				current = current.getAsJsonObject().get(part.getValue());

			} else if (part.getSeparator() == ':') {
				current = current.getAsJsonArray().get(Integer.parseInt(part.getValue()));
			}
		}
		if (current == null) {
			return null;
		}
		return current.getAsJsonObject();
	}

	public static void createNestedObject(
			@NotNull JsonObject obj,
			@NotNull String dottedKey,
			@NotNull JsonElement value
	) {
		Key key = Key.parse(dottedKey, "\\.", "\\:");
		JsonElement current = obj;

		for (int i = 0; i < key.getParts().size(); i++) {
			Key.KeyPart part = key.getParts().get(i);
			boolean isLast = (i == key.getParts().size() - 1);

			if (isLast) {
				setValueAtPart(current, part.getValue(), value);
			} else {
				Key.KeyPart next = key.getParts().get(i + 1);
				boolean nextIsArray = (next.getSeparator() == ':');
				current = ensureAndNavigate(current, part.getValue(), nextIsArray);
			}
		}
	}

	private static void setValueAtPart(JsonElement current, String partValue, JsonElement value) {
		if (current.isJsonObject()) {
			current.getAsJsonObject().add(partValue, value);
		} else if (current.isJsonArray()) {
			JsonArray array = current.getAsJsonArray();
			if (Metadata.isMetadata(partValue)) {
				return;
			}
			int index = Integer.parseInt(partValue);
			ensureArraySize(array, index + 1);
			array.set(index, value);
		}
	}

	private static JsonElement ensureAndNavigate(JsonElement current, String partValue, boolean nextIsArray) {
		if (current.isJsonObject()) {
			return ensureAndNavigateFromObject(current.getAsJsonObject(), partValue, nextIsArray);
		} else if (current.isJsonArray()) {
			return ensureAndNavigateFromArray(current.getAsJsonArray(), partValue, nextIsArray);
		}
		// This probably will cause some errors. Enjoy my future self!
		if (current.getAsString().equals(Metadata.NULL.value())) {
			return JsonNull.INSTANCE;
		}
		return current;
	}

	private static JsonElement ensureAndNavigateFromObject(JsonObject obj, String key, boolean nextIsArray) {
		JsonElement newElement = nextIsArray ? new JsonArray() : new JsonObject();

		if (!obj.has(key) || !isCorrectType(obj.get(key), nextIsArray)) {
			obj.add(key, newElement);
		}

		return obj.get(key);
	}

	private static JsonElement ensureAndNavigateFromArray(JsonArray array, String indexStr, boolean nextIsArray) {
		int index = Integer.parseInt(indexStr);
		ensureArraySize(array, index + 1);

		JsonElement newElement = nextIsArray ? new JsonArray() : new JsonObject();

		if (!isCorrectType(array.get(index), nextIsArray)) {
			array.set(index, newElement);
		}

		return array.get(index);
	}

	private static void ensureArraySize(JsonArray array, int minSize) {
		while (array.size() < minSize) {
			array.add(JsonNull.INSTANCE);
		}
	}

	private static boolean isCorrectType(JsonElement element, boolean shouldBeArray) {
		return shouldBeArray ? element.isJsonArray() : element.isJsonObject();
	}

	public static void hsetSync(
			@NotNull Jedis jedis,
			@NotNull Key key,
			@NotNull Key childKey,
			@NotNull JsonElement jsonElement,
			@NotNull String collectionName
	) {
		if (jsonElement.isJsonObject()) {
			hsetSync(jedis, key, childKey, jsonElement.getAsJsonObject(), collectionName);
		} else if (jsonElement.isJsonArray()) {
			hsetSync(jedis, key, childKey, jsonElement.getAsJsonArray(), collectionName);
		} else {
			jedis.hset(key.withCollectionName(collectionName), childKey.toString(), jsonElement.getAsString());
		}
	}

	public static void hsetObjectSync(
			@NotNull Jedis jedis,
			@NotNull Key key,
			@NotNull Key childKey,
			@NotNull JsonObject jsonObject,
			@NotNull String collectionName
	) {
		for (String elementKey : jsonObject.keySet()) {
			JsonElement jsonElement = jsonObject.get(elementKey);

			if (jsonElement.isJsonObject()) {
				hsetObjectSync(jedis, key, childKey.addPart(elementKey), jsonElement.getAsJsonObject(), collectionName);
			} else if (jsonElement.isJsonArray()) {
				hsetSyncArray(jedis, key, childKey.addPart(elementKey, '.'), jsonElement.getAsJsonArray(), collectionName);
			} else {
				jedis.hset(key.withCollectionName(collectionName), childKey.addPart(elementKey).toString(), jsonElement.toString());
			}
		}
	}

	public static void hsetSyncArray(
			@NotNull Jedis jedis,
			@NotNull Key key,
			@NotNull Key childKey,
			@NotNull JsonArray array,
			@NotNull String collectionName
	) {
		int index = 0;
		for (JsonElement element : array.asList()) {

			Key indexedKey = childKey.addPart(String.valueOf(index), ':');

			if (element.isJsonPrimitive()) {
				jedis.hset(key.withCollectionName(collectionName), indexedKey.toString(), element.getAsString());
			} else if (element.isJsonObject()) {
				hsetObjectSync(jedis, key, indexedKey, element.getAsJsonObject(), collectionName);
			} else if (element.isJsonArray()) {
				hsetSyncArray(jedis, key, indexedKey, element.getAsJsonArray(), collectionName);
			}

			index++;
		}
		jedis.hset(key.withCollectionName(collectionName), childKey.addPart("__length__", ':').toString(), String.valueOf(array.size()));
	}
}
