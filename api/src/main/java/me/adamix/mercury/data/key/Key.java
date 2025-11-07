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

	public Key addKey(@NotNull Key key) {
		List<KeyPart> newParts = new ArrayList<>(parts);
		newParts.addAll(key.parts);
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

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < parts.size(); i++) {
			if (i > 0) {
				// Use the separator stored WITH the current part (it represents what comes before it)
				sb.append(parts.get(i).getSeparator());
			}
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
		String remaining = string;
		char previousSeparator = '.'; // Default for first part

		while (!remaining.isEmpty()) {
			int earliestIndex = -1;
			char foundSeparator = '.';

			// Find the earliest separator in the remaining string
			for (String regex : regexes) {
				// Extract the actual separator character from the regex (e.g., "\\." -> '.')
				char separator = regex.replace("\\", "").charAt(0);
				int index = remaining.indexOf(separator);

				if (index != -1 && (earliestIndex == -1 || index < earliestIndex)) {
					earliestIndex = index;
					foundSeparator = separator;
				}
			}

			if (earliestIndex == -1) {
				// No more separators, add the rest as final part
				parts.add(new KeyPart(remaining, previousSeparator));
				break;
			} else {
				// Add part before separator
				String value = remaining.substring(0, earliestIndex);
				parts.add(new KeyPart(value, previousSeparator));
				previousSeparator = foundSeparator; // Store for next part
				remaining = remaining.substring(earliestIndex + 1);
			}
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