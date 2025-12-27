package me.adamix.mercury.data.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import me.adamix.mercury.data.metadata.Metadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JsonUtils {
	public static String getRawValue(JsonElement element) {
		if (element == null || element.isJsonNull()) {
			return null;
		}
		if (element.isJsonPrimitive()) {
			return element.getAsString();
		} else {
			return element.toString();
		}
	}

	public static @NotNull JsonElement parseString(@Nullable String input) {
		if (input == null || input.equals(Metadata.NULL.value())) return JsonNull.INSTANCE;

		input = input.trim();

		if ((input.startsWith("{") && input.endsWith("}")) ||
				(input.startsWith("[") && input.endsWith("]"))) {
			return JsonParser.parseString(input);
		}

		if ("true".equalsIgnoreCase(input) || "false".equalsIgnoreCase(input)) {
			return new JsonPrimitive(Boolean.parseBoolean(input));
		}
		try {
			if (input.contains(".")) {
				return new JsonPrimitive(Double.parseDouble(input));
			} else {
				return new JsonPrimitive(Long.parseLong(input));
			}
		} catch (NumberFormatException ignored) {
		}

		return new JsonPrimitive(input);
	}
}
