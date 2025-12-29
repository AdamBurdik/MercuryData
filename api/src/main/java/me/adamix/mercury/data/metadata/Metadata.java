package me.adamix.mercury.data.metadata;

import org.jetbrains.annotations.NotNull;

public enum Metadata {
	LIST_LENGTH("__length__"),
	NULL("__NULL__"),
	EMPTY("__EMPTY__"),;

	private final @NotNull String value;
	Metadata(@NotNull String value) {
		this.value = value;
	}

	public @NotNull String value() {
		return value;
	}

	public static boolean isMetadata(@NotNull String value) {
		for (Metadata m : Metadata.values()) {
			if (m.value.equals(value)) return true;
		}
		return false;
	}
}
