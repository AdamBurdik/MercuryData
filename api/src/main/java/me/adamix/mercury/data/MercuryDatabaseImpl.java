package me.adamix.mercury.data;

import me.adamix.mercury.data.module.StoreModule;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class MercuryDatabaseImpl implements MercuryDatabase {
	private static final Logger LOGGER = LoggerFactory.getLogger(MercuryDatabaseImpl.class);
	private final @NotNull Map<String, MercuryCollection> collectionMap = new HashMap<>();
	private final @NotNull StoreModule module;

	public MercuryDatabaseImpl(@NotNull StoreModule module) {
		this.module = module;
		LOGGER.info("Database initialized with module: {}", module.getClass().getSimpleName());
	}

	@Override
	public @NotNull MercuryCollection getCollection(@NotNull String name) {
		return collectionMap.computeIfAbsent(name, module::createCollection);
	}

	@Override
	public void close() {
		LOGGER.info("Closing database");
		module.close();
		collectionMap.clear();
	}
}
