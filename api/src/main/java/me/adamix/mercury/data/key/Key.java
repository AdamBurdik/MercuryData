package me.adamix.mercury.data.key;


import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Key {
	private final List<String> parts;

	private Key(List<String> parts) {
		this.parts = parts;
	}

	public Key addPart(@NotNull String part) {
		var list = new ArrayList<>(parts);
		list.add(part);
		return new Key(list);
	}

	public List<String> getParts() {
		return parts;
	}

	@Override
	public String toString() {
		return String.join(".", parts);
	}

	public static Key parse(@NotNull String string) {
		return parse(string, "\\.");
	}

	public static Key parse(@NotNull String string, @NotNull String regex) {
		return new Key(Arrays.stream(string.split(regex)).toList());
	}

	public static Key of(Object... parts) {
		return new Key(Arrays.stream(parts)
				.map(Object::toString)
				.toList());
	}

	public static Key empty() {
		return new Key(new ArrayList<>());
	}
}
