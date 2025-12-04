package me.adamix.mercury.data.redis.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.adamix.mercury.data.key.Key;
import me.adamix.mercury.data.metadata.Metadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.Jedis;

import java.util.Map;

public class ListOperations {
	// Checks if length metadata is present
	public static @NotNull String ensureValidList(
			@NotNull Jedis jedis,
			@NotNull Key fieldKey,
			@NotNull Key key,
			@NotNull String collectionName
	) {
		String valueKey = key.addPart(Metadata.LIST_LENGTH.value(), ':').toString();

		String rawLength = jedis.hget(
				fieldKey.withCollectionName(collectionName),
				valueKey
		);
		if (rawLength == null) {
			// No length metadata = not a list!
			throw new IllegalStateException(
					"Field '" + key + "' is not a list. " +
							"Cannot perform list operations on non-list field."
			);
		}
		return rawLength;
	}

	public static void ensureIndex(
			@NotNull Jedis jedis,
			@NotNull Key fieldKey,
			@NotNull Key key,
			@NotNull String collectionName,
			int index
	) {
		if (index < 0) {
			throw new IllegalArgumentException("Index must be non-negative, but was " + index);
		}

		int size = getSize(jedis, fieldKey, key, collectionName);
		if (index >= size) {
			throw new IndexOutOfBoundsException(
					"Index " + index + " is out of bounds for list of size " + size +
							" at key: " + key +
							" in collection: " + collectionName
			);
		}
	}

	public static int getSize(
			@NotNull Jedis jedis,
			@NotNull Key fieldKey,
			@NotNull Key key,
			@NotNull String collectionName
	) {


		return Integer.parseInt(ensureValidList(jedis, fieldKey, key, collectionName));
	}

	public static void setSize(
			@NotNull Jedis jedis,
			@NotNull Key fieldKey,
			@NotNull Key key,
			@NotNull String collectionName,
			int size
	) {
		jedis.hset(
				fieldKey.withCollectionName(collectionName),
				key.addPart(Metadata.LIST_LENGTH.value(), ':').toString(),
				String.valueOf(size)
		);
	}

	public static void insert(
			@NotNull Jedis jedis,
			@NotNull Key fieldKey,
			@NotNull Key key,
			@NotNull String collectionName,
			@NotNull JsonElement element,
			int offset
	) {
		int size = getSize(jedis, fieldKey, key, collectionName);
		if (size > offset) {
			size = shift(jedis, fieldKey, key, collectionName, offset, 1);
		} else {
			size++;
		}

		JsonUtils.hsetSync(jedis, fieldKey, key.addPart(String.valueOf(offset), ':'), element, collectionName);
		setSize(jedis, fieldKey, key, collectionName, size);
	}

	public static @Nullable JsonElement get(
			@NotNull Jedis jedis,
			@NotNull Key fieldKey,
			@NotNull Key key,
			@NotNull String collectionName,
			int index
	) {
		ensureIndex(jedis, fieldKey, key, collectionName, index);

		Map<String, String> map = jedis.hgetAll(fieldKey.withCollectionName(collectionName));
		if (map == null) {
			return null;
		}

		String base = key.toString() + ":" + index;
		JsonObject jsonObject =  new JsonObject();

		for (String childKey : map.keySet()) {
			String value = map.get(childKey);

			if (childKey.equals(base)) {
				return JsonUtils.parseString(value);
			} else if (childKey.startsWith(base)) {
				JsonUtils.createNestedObject(jsonObject, childKey, JsonUtils.parseString(value));
			}
		}

		JsonElement element = jsonObject.getAsJsonObject();
		for (Key.KeyPart part : key.addPart(String.valueOf(index), ':').getParts()) {
			if (element.isJsonObject()) {
				element = element.getAsJsonObject().get(part.getValue());
			} else if (element.isJsonArray()) {
				element = element.getAsJsonArray().get(Integer.parseInt(part.getValue()));
			}
			if (element == null) {
				return null;
			}
		}

		return element;
	}

	public static int shift(
			@NotNull Jedis jedis,
			@NotNull Key fieldKey,
			@NotNull Key key,
			@NotNull String collectionName,
			int startingIndex,
			int shiftAmount
	) {
		int size = getSize(jedis, fieldKey, key, collectionName);
		if (shiftAmount == 0) return size;

		if (startingIndex >= size) {
			throw new IllegalArgumentException("Index out of range: " + startingIndex + " - " + size);
		}

		Map<String, String> map = jedis.hgetAll(fieldKey.withCollectionName(collectionName));
		if (map == null) {
			throw new IllegalStateException("List scope does not exist: " + fieldKey.withCollectionName(collectionName) + " - " + key);
		}

		// Replicate the values
		for (String childKey : map.keySet()) {
			String value = map.get(childKey);

			String base = key.toString();

			if (childKey.startsWith(base)) {
				// Gets an index from child key
				// Example:    childKey =   user.friends:0.name
				//                          user.friends: is removed, and remaining is 0.name
				String remainingRawKey = childKey.substring(base.length() + 1);
				String path = childKey.replace(remainingRawKey, "");
				Key remainingKey = Key.parse(remainingRawKey);

				// We can get the first part
				Key.KeyPart part = remainingKey.getParts().getFirst();

				if (Metadata.isMetadata(part.getValue())) continue;

				// and parse it as integer
				int index = Integer.parseInt(part.getValue());
				if (index < startingIndex) continue;

				jedis.hset(fieldKey.withCollectionName(collectionName), path + (index + shiftAmount), value);
			}
		}

		int newSize = size;

		if (shiftAmount > 0) {
			// Delete the part in between
			newSize += shiftAmount;
			for (int i = startingIndex; i < startingIndex + shiftAmount; i++) {
				for (String childKey : map.keySet()) {
					String base = key.toString() + ":" + i;

					if (childKey.startsWith(base)) {
						jedis.hdel(fieldKey.withCollectionName(collectionName), childKey);
					}
				}
			}
		} else {
			// Delete the tail
			int absAmount = Math.abs(shiftAmount);
			newSize -= absAmount;

			for (int i = size - absAmount; i < size; i++) {
				for (String childKey : map.keySet()) {
					String base = key.toString() + ":" + i;

					if (childKey.startsWith(base)) {
						jedis.hdel(fieldKey.withCollectionName(collectionName), childKey);
					}
				}
			}
		}

		// Adjust size
		setSize(jedis, fieldKey, key, collectionName, newSize);
		return newSize;
	}

	public static void remove(
			@NotNull Jedis jedis,
			@NotNull Key fieldKey,
			@NotNull Key key,
			@NotNull String collectionName,
			int index
	) {
		ensureIndex(jedis, fieldKey, key, collectionName, index);

		Map<String, String> map = jedis.hgetAll(fieldKey.withCollectionName(collectionName));
		if (map == null) {
			return;
		}

		int size = getSize(jedis, fieldKey, key, collectionName);

		// If removing the last one, we need to manually delete it. Otherwise, it will be replaced by shifting
		if (index + 1 >= size) {
			String base = key.toString() + ":" + index;
			JsonObject jsonObject =  new JsonObject();

			for (String childKey : map.keySet()) {
				if (childKey.startsWith(base)) {
					jedis.hdel(fieldKey.withCollectionName(collectionName), childKey);
				}
			}
		}

		shift(jedis, fieldKey, key, collectionName, index + 1, -1);
	}
}
