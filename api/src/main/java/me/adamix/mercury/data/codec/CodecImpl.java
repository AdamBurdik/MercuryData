package me.adamix.mercury.data.codec;

import com.google.gson.*;
import me.adamix.mercury.data.utils.JsonUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CodecImpl {
	protected static final Codec<String> STRING = new Codec<String>() {
		@Override
		public JsonElement encode(String value) {
			return new JsonPrimitive(value);
		}

		@Override
		public String decode(JsonElement json) {
			return json.getAsString();
		}
	};

	protected static final Codec<Integer> INT = new Codec<Integer>() {
		@Override
		public JsonElement encode(Integer value) {
			return new JsonPrimitive(value);
		}

		@Override
		public Integer decode(JsonElement json) {
			return json.getAsInt();
		}
	};

	protected static final Codec<Double> DOUBLE = new Codec<Double>() {
		@Override
		public JsonElement encode(Double value) {
			return new JsonPrimitive(value);
		}

		@Override
		public Double decode(JsonElement json) {
			return json.getAsDouble();
		}
	};

	protected static final Codec<Float> FLOAT = new Codec<Float>() {
		@Override
		public JsonElement encode(Float value) {
			return new JsonPrimitive(value);
		}

		@Override
		public Float decode(JsonElement json) {
			return json.getAsFloat();
		}
	};

	protected static final Codec<Long> LONG = new Codec<Long>() {
		@Override
		public JsonElement encode(Long value) {
			return new JsonPrimitive(value);
		}

		@Override
		public Long decode(JsonElement json) {
			return json.getAsLong();
		}
	};

	protected static final Codec<Short> SHORT = new Codec<Short>() {
		@Override
		public JsonElement encode(Short value) {
			return new JsonPrimitive(value);
		}

		@Override
		public Short decode(JsonElement json) {
			return json.getAsShort();
		}
	};

	protected static final Codec<Byte> BYTE = new Codec<Byte>() {
		@Override
		public JsonElement encode(Byte value) {
			return new JsonPrimitive(value);
		}

		@Override
		public Byte decode(JsonElement json) {
			return json.getAsByte();
		}
	};

	protected static final Codec<Boolean> BOOLEAN = new Codec<Boolean>() {
		@Override
		public JsonElement encode(Boolean value) {
			return new JsonPrimitive(value);
		}

		@Override
		public Boolean decode(JsonElement json) {
			return json.getAsBoolean();
		}
	};

	protected static final Codec<Character> CHAR = new Codec<Character>() {
		@Override
		public JsonElement encode(Character value) {
			return new JsonPrimitive(value.toString());
		}

		@Override
		public Character decode(JsonElement json) {
			return json.getAsString().charAt(0);
		}
	};

	protected static final Codec<UUID> UUID = new Codec<UUID>() {
		@Override
		public JsonElement encode(UUID value) {
			return new JsonPrimitive(value.toString());
		}

		@Override
		public UUID decode(JsonElement json) {
			return java.util.UUID.fromString(json.getAsString());
		}
	};

	record OptionalCodec<T>(@NotNull Codec<T> inner, @Nullable T defaultValue) implements Codec<T> {
		@Override
		public JsonElement encode(T value) {
			if (value == null || Objects.equals(value, defaultValue)) {
				return JsonNull.INSTANCE;
			}
			return inner.encode(value);
		}

		@Override
		public T decode(JsonElement json) {
			if (json == null || json.isJsonNull()) {
				return defaultValue;
			}
			T decoded = inner.decode(json);
			if (decoded == null) {
				return defaultValue;
			}
			return decoded;
		}
	}

	record ListCodec<T>(@NotNull Codec<T> inner) implements Codec<List<T>> {

		@Override
		public JsonElement encode(List<T> list) {
			JsonArray array = new JsonArray();
			for (T value : list) {
				array.add(inner.encode(value));
			}
			return array;
		}

		@Override
		public List<T> decode(JsonElement json) {
			JsonArray array = json.getAsJsonArray();
			List<T> list = new ArrayList<>(array.size());
			for (JsonElement element : array) {
				list.add(inner.decode(element));
			}

			return list;
		}
	}

	record SetCodec<T>(@NotNull Codec<T> inner) implements Codec<Set<T>> {

		@Override
		public JsonElement encode(Set<T> set) {
			JsonArray array = new JsonArray();
			for (T value : set) {
				array.add(inner.encode(value));
			}
			return array;
		}

		@Override
		public Set<T> decode(JsonElement json) {
			JsonArray array = json.getAsJsonArray();
			Set<T> set = new HashSet<>(array.size());
			for (JsonElement element : array) {
				set.add(inner.decode(element));
			}

			return set;
		}
	}

	record MapCodec<K, V>(@NotNull Codec<K> keyCodec, @NotNull Codec<V> valueCodec) implements Codec<Map<K, V>> {

		@Override
		public JsonElement encode(Map<K, V> map) {
			JsonObject jsonObject = new JsonObject();
			map.forEach((key, value) -> {
				jsonObject.add(JsonUtils.getRawValue(keyCodec.encode(key)), valueCodec.encode(value));
			});

			return jsonObject;
		}

		@Override
		public Map<K, V> decode(JsonElement json) {
			if (!json.isJsonObject()) {
				return null;
			}

			Map<K, V> decodedMap = new HashMap<>();

			JsonObject jsonObject = json.getAsJsonObject();

			for (String key : jsonObject.keySet()) {
				JsonElement keyJson = JsonParser.parseString(key);
				JsonElement valueJson = jsonObject.get(key);

				decodedMap.put(keyCodec.decode(keyJson), valueCodec.decode(valueJson));
			}

			return decodedMap;
		}
	}
}
