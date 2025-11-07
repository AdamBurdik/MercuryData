package me.adamix.mercury.data.key;


import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Key {

	public static class KeyPart {
		private final String value;
		private final char separator;

		public KeyPart(@NotNull String value, char separator) {
			this.value = value;
			this.separator = separator;
		}

		public String getValue() {
			return value;
		}

		public char getSeparator() {
			return separator;
		}

		@Override
		public String toString() {
			return value;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			KeyPart keyPart = (KeyPart) o;
			return separator == keyPart.separator && Objects.equals(value, keyPart.value);
		}

		@Override
		public int hashCode() {
			return Objects.hash(value, separator);
		}
	}

	private final List<KeyPart> parts;

	private Key(@NotNull List<KeyPart> parts) {
		this.parts = parts;
	}

	public Key addPart(@NotNull String part) {
		return addPart(part, '.'); // default separator
	}

	public Key addPart(@NotNull String part, char separator) {
		List<KeyPart> newParts = new ArrayList<>(parts);
		newParts.add(new KeyPart(part, separator));
		return new Key(newParts);
	}

	public List<KeyPart> getParts() {
		return parts;
	}

	public @NotNull String withCollectionName(@NotNull String collectionName) {
		List<KeyPart> newParts = new ArrayList<>();
		newParts.add(new KeyPart(collectionName, '.'));
		newParts.addAll(parts);
		return new Key(newParts).toString();
	}

	public @NotNull Key stripCollectionName() {
		if (parts.size() <= 1) return Key.empty();
		return new Key(parts.subList(1, parts.size()));
	}

	@Override
	public String toString() {
		if (parts.isEmpty()) return "";
		StringBuilder sb = new StringBuilder(parts.get(0).getValue());
		for (int i = 1; i < parts.size(); i++) {
			sb.append(parts.get(i).getSeparator());
			sb.append(parts.get(i).getValue());
		}
		return sb.toString();
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
		return new Key(Arrays.stream(string.split(regex))
				.map(p -> new KeyPart(p, '.')) // default separator
				.toList());
	}

	public static Key parse(@NotNull String string, @NotNull String... regexes) {
		if (regexes.length == 0) {
			return parse(string); // default to dot
		}

		List<KeyPart> parts = new ArrayList<>();
		parts.add(new KeyPart(string, '.')); // start with whole string

		for (String regex : regexes) {
			List<KeyPart> newParts = new ArrayList<>();
			for (KeyPart part : parts) {
				// split only if part contains the separator
				if (part.getValue().contains(regex.replaceAll("\\\\", ""))) {
					String[] split = part.getValue().split(regex);
					for (int i = 0; i < split.length; i++) {
						char sep = (i < split.length - 1) ? regex.charAt(regex.length() - 1) : '.'; // guess separator
						newParts.add(new KeyPart(split[i], sep));
					}
				} else {
					newParts.add(part);
				}
			}
			parts = newParts;
		}

		return new Key(parts);
	}

	public static Key of(Object... parts) {
		List<KeyPart> list = Arrays.stream(parts)
				.map(Object::toString)
				.map(s -> new KeyPart(s, '.'))
				.toList();
		return new Key(list);
	}

	public static Key empty() {
		return new Key(new ArrayList<>());
	}
}