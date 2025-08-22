package me.adamix.mercury.data.utils;

import com.google.gson.JsonElement;

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
}
