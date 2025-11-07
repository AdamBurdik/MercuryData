package me.adamix.mercury.data.redis.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import me.adamix.mercury.data.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JsonUtils {
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
}
