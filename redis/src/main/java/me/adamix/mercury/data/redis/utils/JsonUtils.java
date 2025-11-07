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

	// Converts string to complex object
 	//
 	// Example:
	//     people:0.address.lines:0 -> {"people":[{"address":{"lines":["Prague 5"]}}]}
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

			if (part.getSeparator() == '.') {
				if (current.isJsonObject()) {
					JsonObject currentObject = current.getAsJsonObject();
					if (isLast) {
						currentObject.add(part.getValue(), value);
						continue;
					}

					if (!currentObject.has(part.getValue()) || !currentObject.get(part.getValue()).isJsonObject()) {
						currentObject.add(part.getValue(), new JsonObject());
					}
					current = currentObject.getAsJsonObject(part.getValue());
				}
				else if (current.isJsonArray()) {
					int index = Integer.parseInt(part.getValue());
					JsonArray currentArray = current.getAsJsonArray();

					// Fill array with nulls, so we can put our desired value at specific index
					while (index >= currentArray.size()) {
						currentArray.add(JsonNull.INSTANCE);
					}
					if (isLast) {
						currentArray.set(index, value);
						current = currentArray.get(index);
					} else {
						//  tags:0.name
						//  : 0
						JsonElement child = currentArray.get(index);
						if (child.isJsonObject()) {
							current = child.getAsJsonObject();
						} else if (child.isJsonNull()) {
							currentArray.set(index, new JsonObject());
							current = currentArray.get(index).getAsJsonObject();
						}

//						current = currentArray.get(index);
//						if (current ==  null || current.isJsonObject()) {
//							currentArray.set(index, new JsonObject());
//							current = currentArray.get(index);
//						}
					}
				}
			} else if (part.getSeparator() == ':') {
				if (isLast) {
					if (current.isJsonObject()) {
						current.getAsJsonObject().add(part.getValue(), value);
					} else if (current.isJsonArray()) {
						current.getAsJsonArray().set(Integer.parseInt(part.getValue()), value);
					}
				} else if (current.isJsonObject()) {
					JsonObject currentObject = current.getAsJsonObject();
					if (!currentObject.has(part.getValue()) || !currentObject.get(part.getValue()).isJsonArray()) {
						currentObject.add(part.getValue(), new JsonArray());
					}
					current = currentObject.get(part.getValue()).getAsJsonArray();
				} else if (current.isJsonArray()) {
					int index = Integer.parseInt(part.getValue());
					JsonArray currentArray = current.getAsJsonArray();

					// Fill array with nulls, so we can put our desired value at specific index
					while (index >= currentArray.size()) {
						currentArray.add(JsonNull.INSTANCE);
					}

					currentArray.set(index, new JsonArray());
					current = currentArray.get(index);
				}
			}
		}
	}

}
