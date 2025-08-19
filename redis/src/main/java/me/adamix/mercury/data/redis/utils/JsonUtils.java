package me.adamix.mercury.data.redis.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

public class JsonUtils {
	public static void addNestedProperty(@NotNull JsonObject obj, @NotNull String dottedKey, @NotNull JsonElement value, @NotNull String regex) {
		String[] parts = dottedKey.split(regex);
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
}
