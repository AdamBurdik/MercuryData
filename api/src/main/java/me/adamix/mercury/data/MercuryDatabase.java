package me.adamix.mercury.data;

import me.adamix.mercury.data.module.StoreModule;
import org.jetbrains.annotations.NotNull;

public interface MercuryDatabase {

	@NotNull MercuryCollection getCollection(@NotNull String name);
	void close();

	static @NotNull MercuryDatabase create(@NotNull StoreModule storeModule) {
		return new MercuryDatabaseImpl(storeModule);
	}
}
