package me.adamix.mercury.data.module;

import me.adamix.mercury.data.MercuryCollection;
import org.jetbrains.annotations.NotNull;

public interface StoreModule {
	@NotNull MercuryCollection createCollection(@NotNull String name);
	void close();
}
