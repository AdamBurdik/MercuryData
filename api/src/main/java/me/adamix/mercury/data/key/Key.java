package me.adamix.mercury.data.key;


import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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

	public @NotNull String withCollectionName(@NotNull String collectionName) {
		var list = new ArrayList<>(parts);
		list.addFirst(collectionName);
		return new Key(list).toString();
	}

	@Override
	public String toString() {
		return String.join(".", parts);
	}

	@Override
	public boolean equals(Object object) {
		if (object == null || getClass() != object.getClass()) return false;
		Key key = (Key) object;
		return Objects.equals(parts, key.parts);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(parts);
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
