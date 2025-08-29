package me.adamix.mercury.data.query;

import me.adamix.mercury.data.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;

public interface QueryResult<V> {
	boolean isEmpty();
	@NotNull Optional<V> getFirst();
	@NotNull Optional<Key> getFirstKey();
	@NotNull Collection<Entry<V>> collection();
	@NotNull Collection<Key> keys();
	@NotNull Collection<V> values();

	record Entry<V>(@NotNull Key key, @NotNull V value) {}
}
