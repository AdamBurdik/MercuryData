package me.adamix.mercury.data.query;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;

public interface QueryResult<T> {
	boolean isEmpty();
	@NotNull Optional<T> getFirst();
	@NotNull Collection<T> collection();
}
