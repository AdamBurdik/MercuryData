package me.adamix.mercury.data.codec;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class StructCodec {
	private static <T, E> void addToJson(
			@NotNull JsonObject json,
			@NotNull String name,
			@NotNull Codec<E> codec,
			Function<T, E> getter,
			@NotNull T value
	) {
		E field = getter.apply(value);

		if (field == null) {
			throw new NullPointerException("Trying to encode '%s' field with null value".formatted(name));
		}

		json.add(name, codec.encode(field));
	}

	public static <T, P1> Codec<T> struct(
			String name1, Codec<P1> codec1, Function<T, P1> getter1,
			Functions.F1<P1, T> constructor
	) {
		return new Codec<T>() {
			@Override
			public JsonElement encode(T value) {
				JsonObject json = new JsonObject();
				addToJson(json, name1, codec1, getter1, value);
				return json;
			}

			@Override
			public T decode(JsonElement json) {
				JsonObject jsonObject = json.getAsJsonObject();
				return constructor.apply(
						codec1.decode(jsonObject.get(name1))
				);
			}
		};
	}

	public static <T, P1, P2> Codec<T> struct(
			String name1, Codec<P1> codec1, Function<T, P1> getter1,
			String name2, Codec<P2> codec2, Function<T, P2> getter2,
			Functions.F2<P1, P2, T> constructor
	) {
		return new Codec<T>() {
			@Override
			public JsonElement encode(T value) {
				JsonObject json = new JsonObject();
				addToJson(json, name1, codec1, getter1, value);
				addToJson(json, name2, codec2, getter2, value);
				return json;
			}

			@Override
			public T decode(JsonElement json) {
				JsonObject jsonObject = json.getAsJsonObject();
				return constructor.apply(
						codec1.decode(jsonObject.get(name1)),
						codec2.decode(jsonObject.get(name2))
				);
			}
		};
	}

	public static <T, P1, P2, P3> Codec<T> struct(
			String name1, Codec<P1> codec1, Function<T, P1> getter1,
			String name2, Codec<P2> codec2, Function<T, P2> getter2,
			String name3, Codec<P3> codec3, Function<T, P3> getter3,
			Functions.F3<P1, P2, P3, T> constructor
	) {
		return new Codec<T>() {
			@Override
			public JsonElement encode(T value) {
				JsonObject json = new JsonObject();
				addToJson(json, name1, codec1, getter1, value);
				addToJson(json, name2, codec2, getter2, value);
				addToJson(json, name3, codec3, getter3, value);
				return json;
			}

			@Override
			public T decode(JsonElement json) {
				JsonObject jsonObject = json.getAsJsonObject();
				return constructor.apply(
						codec1.decode(jsonObject.get(name1)),
						codec2.decode(jsonObject.get(name2)),
						codec3.decode(jsonObject.get(name3))
				);
			}
		};
	}

	public static <T, P1, P2, P3, P4> Codec<T> struct(
			String name1, Codec<P1> codec1, Function<T, P1> getter1,
			String name2, Codec<P2> codec2, Function<T, P2> getter2,
			String name3, Codec<P3> codec3, Function<T, P3> getter3,
			String name4, Codec<P4> codec4, Function<T, P4> getter4,
			Functions.F4<P1, P2, P3, P4, T> constructor
	) {
		return new Codec<T>() {
			@Override
			public JsonElement encode(T value) {
				JsonObject json = new JsonObject();
				addToJson(json, name1, codec1, getter1, value);
				addToJson(json, name2, codec2, getter2, value);
				addToJson(json, name3, codec3, getter3, value);
				addToJson(json, name4, codec4, getter4, value);
				return json;
			}

			@Override
			public T decode(JsonElement json) {
				JsonObject jsonObject = json.getAsJsonObject();
				return constructor.apply(
						codec1.decode(jsonObject.get(name1)),
						codec2.decode(jsonObject.get(name2)),
						codec3.decode(jsonObject.get(name3)),
						codec4.decode(jsonObject.get(name4))
				);
			}
		};
	}

	public static <T, P1, P2, P3, P4, P5> Codec<T> struct(
			String name1, Codec<P1> codec1, Function<T, P1> getter1,
			String name2, Codec<P2> codec2, Function<T, P2> getter2,
			String name3, Codec<P3> codec3, Function<T, P3> getter3,
			String name4, Codec<P4> codec4, Function<T, P4> getter4,
			String name5, Codec<P5> codec5, Function<T, P5> getter5,
			Functions.F5<P1, P2, P3, P4, P5, T> constructor
	) {
		return new Codec<T>() {
			@Override
			public JsonElement encode(T value) {
				JsonObject json = new JsonObject();
				addToJson(json, name1, codec1, getter1, value);
				addToJson(json, name2, codec2, getter2, value);
				addToJson(json, name3, codec3, getter3, value);
				addToJson(json, name4, codec4, getter4, value);
				addToJson(json, name5, codec5, getter5, value);
				return json;
			}

			@Override
			public T decode(JsonElement json) {
				JsonObject jsonObject = json.getAsJsonObject();
				return constructor.apply(
						codec1.decode(jsonObject.get(name1)),
						codec2.decode(jsonObject.get(name2)),
						codec3.decode(jsonObject.get(name3)),
						codec4.decode(jsonObject.get(name4)),
						codec5.decode(jsonObject.get(name5))
				);
			}
		};
	}

	public static <T, P1, P2, P3, P4, P5, P6> Codec<T> struct(
			String name1, Codec<P1> codec1, Function<T, P1> getter1,
			String name2, Codec<P2> codec2, Function<T, P2> getter2,
			String name3, Codec<P3> codec3, Function<T, P3> getter3,
			String name4, Codec<P4> codec4, Function<T, P4> getter4,
			String name5, Codec<P5> codec5, Function<T, P5> getter5,
			String name6, Codec<P6> codec6, Function<T, P6> getter6,
			Functions.F6<P1, P2, P3, P4, P5, P6, T> constructor
	) {
		return new Codec<T>() {
			@Override
			public JsonElement encode(T value) {
				JsonObject json = new JsonObject();
				addToJson(json, name1, codec1, getter1, value);
				addToJson(json, name2, codec2, getter2, value);
				addToJson(json, name3, codec3, getter3, value);
				addToJson(json, name4, codec4, getter4, value);
				addToJson(json, name5, codec5, getter5, value);
				addToJson(json, name6, codec6, getter6, value);
				return json;
			}

			@Override
			public T decode(JsonElement json) {
				JsonObject jsonObject = json.getAsJsonObject();
				return constructor.apply(
						codec1.decode(jsonObject.get(name1)),
						codec2.decode(jsonObject.get(name2)),
						codec3.decode(jsonObject.get(name3)),
						codec4.decode(jsonObject.get(name4)),
						codec5.decode(jsonObject.get(name5)),
						codec6.decode(jsonObject.get(name6))
				);
			}
		};
	}

	public static <T, P1, P2, P3, P4, P5, P6, P7> Codec<T> struct(
			String name1, Codec<P1> codec1, Function<T, P1> getter1,
			String name2, Codec<P2> codec2, Function<T, P2> getter2,
			String name3, Codec<P3> codec3, Function<T, P3> getter3,
			String name4, Codec<P4> codec4, Function<T, P4> getter4,
			String name5, Codec<P5> codec5, Function<T, P5> getter5,
			String name6, Codec<P6> codec6, Function<T, P6> getter6,
			String name7, Codec<P7> codec7, Function<T, P7> getter7,
			Functions.F7<P1, P2, P3, P4, P5, P6, P7, T> constructor
	) {
		return new Codec<T>() {
			@Override
			public JsonElement encode(T value) {
				JsonObject json = new JsonObject();
				addToJson(json, name1, codec1, getter1, value);
				addToJson(json, name2, codec2, getter2, value);
				addToJson(json, name3, codec3, getter3, value);
				addToJson(json, name4, codec4, getter4, value);
				addToJson(json, name5, codec5, getter5, value);
				addToJson(json, name6, codec6, getter6, value);
				addToJson(json, name7, codec7, getter7, value);
				return json;
			}

			@Override
			public T decode(JsonElement json) {
				JsonObject jsonObject = json.getAsJsonObject();
				return constructor.apply(
						codec1.decode(jsonObject.get(name1)),
						codec2.decode(jsonObject.get(name2)),
						codec3.decode(jsonObject.get(name3)),
						codec4.decode(jsonObject.get(name4)),
						codec5.decode(jsonObject.get(name5)),
						codec6.decode(jsonObject.get(name6)),
						codec7.decode(jsonObject.get(name7))
				);
			}
		};
	}

	public static <T, P1, P2, P3, P4, P5, P6, P7, P8> Codec<T> struct(
			String name1, Codec<P1> codec1, Function<T, P1> getter1,
			String name2, Codec<P2> codec2, Function<T, P2> getter2,
			String name3, Codec<P3> codec3, Function<T, P3> getter3,
			String name4, Codec<P4> codec4, Function<T, P4> getter4,
			String name5, Codec<P5> codec5, Function<T, P5> getter5,
			String name6, Codec<P6> codec6, Function<T, P6> getter6,
			String name7, Codec<P7> codec7, Function<T, P7> getter7,
			String name8, Codec<P8> codec8, Function<T, P8> getter8,
			Functions.F8<P1, P2, P3, P4, P5, P6, P7, P8, T> constructor
	) {
		return new Codec<T>() {
			@Override
			public JsonElement encode(T value) {
				JsonObject json = new JsonObject();
				addToJson(json, name1, codec1, getter1, value);
				addToJson(json, name2, codec2, getter2, value);
				addToJson(json, name3, codec3, getter3, value);
				addToJson(json, name4, codec4, getter4, value);
				addToJson(json, name5, codec5, getter5, value);
				addToJson(json, name6, codec6, getter6, value);
				addToJson(json, name7, codec7, getter7, value);
				addToJson(json, name8, codec8, getter8, value);
				return json;
			}

			@Override
			public T decode(JsonElement json) {
				JsonObject jsonObject = json.getAsJsonObject();
				return constructor.apply(
						codec1.decode(jsonObject.get(name1)),
						codec2.decode(jsonObject.get(name2)),
						codec3.decode(jsonObject.get(name3)),
						codec4.decode(jsonObject.get(name4)),
						codec5.decode(jsonObject.get(name5)),
						codec6.decode(jsonObject.get(name6)),
						codec7.decode(jsonObject.get(name7)),
						codec8.decode(jsonObject.get(name8))
				);
			}
		};
	}

	public static <T, P1, P2, P3, P4, P5, P6, P7, P8, P9> Codec<T> struct(
			String name1, Codec<P1> codec1, Function<T, P1> getter1,
			String name2, Codec<P2> codec2, Function<T, P2> getter2,
			String name3, Codec<P3> codec3, Function<T, P3> getter3,
			String name4, Codec<P4> codec4, Function<T, P4> getter4,
			String name5, Codec<P5> codec5, Function<T, P5> getter5,
			String name6, Codec<P6> codec6, Function<T, P6> getter6,
			String name7, Codec<P7> codec7, Function<T, P7> getter7,
			String name8, Codec<P8> codec8, Function<T, P8> getter8,
			String name9, Codec<P9> codec9, Function<T, P9> getter9,
			Functions.F9<P1, P2, P3, P4, P5, P6, P7, P8, P9, T> constructor
	) {
		return new Codec<T>() {
			@Override
			public JsonElement encode(T value) {
				JsonObject json = new JsonObject();
				addToJson(json, name1, codec1, getter1, value);
				addToJson(json, name2, codec2, getter2, value);
				addToJson(json, name3, codec3, getter3, value);
				addToJson(json, name4, codec4, getter4, value);
				addToJson(json, name5, codec5, getter5, value);
				addToJson(json, name6, codec6, getter6, value);
				addToJson(json, name7, codec7, getter7, value);
				addToJson(json, name8, codec8, getter8, value);
				addToJson(json, name9, codec9, getter9, value);
				json.add(name9, codec9.encode(getter9.apply(value)));
				return json;
			}

			@Override
			public T decode(JsonElement json) {
				JsonObject jsonObject = json.getAsJsonObject();
				return constructor.apply(
						codec1.decode(jsonObject.get(name1)),
						codec2.decode(jsonObject.get(name2)),
						codec3.decode(jsonObject.get(name3)),
						codec4.decode(jsonObject.get(name4)),
						codec5.decode(jsonObject.get(name5)),
						codec6.decode(jsonObject.get(name6)),
						codec7.decode(jsonObject.get(name7)),
						codec8.decode(jsonObject.get(name8)),
						codec9.decode(jsonObject.get(name9))
				);
			}
		};
	}

	public static <T, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10> Codec<T> struct(
			String name1, Codec<P1> codec1, Function<T, P1> getter1,
			String name2, Codec<P2> codec2, Function<T, P2> getter2,
			String name3, Codec<P3> codec3, Function<T, P3> getter3,
			String name4, Codec<P4> codec4, Function<T, P4> getter4,
			String name5, Codec<P5> codec5, Function<T, P5> getter5,
			String name6, Codec<P6> codec6, Function<T, P6> getter6,
			String name7, Codec<P7> codec7, Function<T, P7> getter7,
			String name8, Codec<P8> codec8, Function<T, P8> getter8,
			String name9, Codec<P9> codec9, Function<T, P9> getter9,
			String name10, Codec<P10> codec10, Function<T, P10> getter10,
			Functions.F10<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, T> constructor
	) {
		return new Codec<T>() {
			@Override
			public JsonElement encode(T value) {
				JsonObject json = new JsonObject();
				addToJson(json, name1, codec1, getter1, value);
				addToJson(json, name2, codec2, getter2, value);
				addToJson(json, name3, codec3, getter3, value);
				addToJson(json, name4, codec4, getter4, value);
				addToJson(json, name5, codec5, getter5, value);
				addToJson(json, name6, codec6, getter6, value);
				addToJson(json, name7, codec7, getter7, value);
				addToJson(json, name8, codec8, getter8, value);
				addToJson(json, name9, codec9, getter9, value);
				addToJson(json, name10, codec10, getter10, value);
				return json;
			}

			@Override
			public T decode(JsonElement json) {
				JsonObject jsonObject = json.getAsJsonObject();
				return constructor.apply(
						codec1.decode(jsonObject.get(name1)),
						codec2.decode(jsonObject.get(name2)),
						codec3.decode(jsonObject.get(name3)),
						codec4.decode(jsonObject.get(name4)),
						codec5.decode(jsonObject.get(name5)),
						codec6.decode(jsonObject.get(name6)),
						codec7.decode(jsonObject.get(name7)),
						codec8.decode(jsonObject.get(name8)),
						codec9.decode(jsonObject.get(name9)),
						codec10.decode(jsonObject.get(name10))
				);
			}
		};
	}

	public static <T, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11> Codec<T> struct(
			String name1, Codec<P1> codec1, Function<T, P1> getter1,
			String name2, Codec<P2> codec2, Function<T, P2> getter2,
			String name3, Codec<P3> codec3, Function<T, P3> getter3,
			String name4, Codec<P4> codec4, Function<T, P4> getter4,
			String name5, Codec<P5> codec5, Function<T, P5> getter5,
			String name6, Codec<P6> codec6, Function<T, P6> getter6,
			String name7, Codec<P7> codec7, Function<T, P7> getter7,
			String name8, Codec<P8> codec8, Function<T, P8> getter8,
			String name9, Codec<P9> codec9, Function<T, P9> getter9,
			String name10, Codec<P10> codec10, Function<T, P10> getter10,
			String name11, Codec<P11> codec11, Function<T, P11> getter11,
			Functions.F11<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, T> constructor
	) {
		return new Codec<T>() {
			@Override
			public JsonElement encode(T value) {
				JsonObject json = new JsonObject();
				addToJson(json, name1, codec1, getter1, value);
				addToJson(json, name2, codec2, getter2, value);
				addToJson(json, name3, codec3, getter3, value);
				addToJson(json, name4, codec4, getter4, value);
				addToJson(json, name5, codec5, getter5, value);
				addToJson(json, name6, codec6, getter6, value);
				addToJson(json, name7, codec7, getter7, value);
				addToJson(json, name8, codec8, getter8, value);
				addToJson(json, name9, codec9, getter9, value);
				addToJson(json, name10, codec10, getter10, value);
				addToJson(json, name11, codec11, getter11, value);
				return json;
			}

			@Override
			public T decode(JsonElement json) {
				JsonObject jsonObject = json.getAsJsonObject();
				return constructor.apply(
						codec1.decode(jsonObject.get(name1)),
						codec2.decode(jsonObject.get(name2)),
						codec3.decode(jsonObject.get(name3)),
						codec4.decode(jsonObject.get(name4)),
						codec5.decode(jsonObject.get(name5)),
						codec6.decode(jsonObject.get(name6)),
						codec7.decode(jsonObject.get(name7)),
						codec8.decode(jsonObject.get(name8)),
						codec9.decode(jsonObject.get(name9)),
						codec10.decode(jsonObject.get(name10)),
						codec11.decode(jsonObject.get(name11))
				);
			}
		};
	}

	public static <T, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12> Codec<T> struct(
			String name1, Codec<P1> codec1, Function<T, P1> getter1,
			String name2, Codec<P2> codec2, Function<T, P2> getter2,
			String name3, Codec<P3> codec3, Function<T, P3> getter3,
			String name4, Codec<P4> codec4, Function<T, P4> getter4,
			String name5, Codec<P5> codec5, Function<T, P5> getter5,
			String name6, Codec<P6> codec6, Function<T, P6> getter6,
			String name7, Codec<P7> codec7, Function<T, P7> getter7,
			String name8, Codec<P8> codec8, Function<T, P8> getter8,
			String name9, Codec<P9> codec9, Function<T, P9> getter9,
			String name10, Codec<P10> codec10, Function<T, P10> getter10,
			String name11, Codec<P11> codec11, Function<T, P11> getter11,
			String name12, Codec<P12> codec12, Function<T, P12> getter12,
			Functions.F12<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, T> constructor
	) {
		return new Codec<T>() {
			@Override
			public JsonElement encode(T value) {
				JsonObject json = new JsonObject();
				addToJson(json, name1, codec1, getter1, value);
				addToJson(json, name2, codec2, getter2, value);
				addToJson(json, name3, codec3, getter3, value);
				addToJson(json, name4, codec4, getter4, value);
				addToJson(json, name5, codec5, getter5, value);
				addToJson(json, name6, codec6, getter6, value);
				addToJson(json, name7, codec7, getter7, value);
				addToJson(json, name8, codec8, getter8, value);
				addToJson(json, name9, codec9, getter9, value);
				addToJson(json, name10, codec10, getter10, value);
				addToJson(json, name11, codec11, getter11, value);
				addToJson(json, name12, codec12, getter12, value);
				return json;
			}

			@Override
			public T decode(JsonElement json) {
				JsonObject jsonObject = json.getAsJsonObject();
				return constructor.apply(
						codec1.decode(jsonObject.get(name1)),
						codec2.decode(jsonObject.get(name2)),
						codec3.decode(jsonObject.get(name3)),
						codec4.decode(jsonObject.get(name4)),
						codec5.decode(jsonObject.get(name5)),
						codec6.decode(jsonObject.get(name6)),
						codec7.decode(jsonObject.get(name7)),
						codec8.decode(jsonObject.get(name8)),
						codec9.decode(jsonObject.get(name9)),
						codec10.decode(jsonObject.get(name10)),
						codec11.decode(jsonObject.get(name11)),
						codec12.decode(jsonObject.get(name12))
				);
			}
		};
	}

	public static <T, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13> Codec<T> struct(
			String name1, Codec<P1> codec1, Function<T, P1> getter1,
			String name2, Codec<P2> codec2, Function<T, P2> getter2,
			String name3, Codec<P3> codec3, Function<T, P3> getter3,
			String name4, Codec<P4> codec4, Function<T, P4> getter4,
			String name5, Codec<P5> codec5, Function<T, P5> getter5,
			String name6, Codec<P6> codec6, Function<T, P6> getter6,
			String name7, Codec<P7> codec7, Function<T, P7> getter7,
			String name8, Codec<P8> codec8, Function<T, P8> getter8,
			String name9, Codec<P9> codec9, Function<T, P9> getter9,
			String name10, Codec<P10> codec10, Function<T, P10> getter10,
			String name11, Codec<P11> codec11, Function<T, P11> getter11,
			String name12, Codec<P12> codec12, Function<T, P12> getter12,
			String name13, Codec<P13> codec13, Function<T, P13> getter13,
			Functions.F13<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, T> constructor
	) {
		return new Codec<T>() {
			@Override
			public JsonElement encode(T value) {
				JsonObject json = new JsonObject();
				addToJson(json, name1, codec1, getter1, value);
				addToJson(json, name2, codec2, getter2, value);
				addToJson(json, name3, codec3, getter3, value);
				addToJson(json, name4, codec4, getter4, value);
				addToJson(json, name5, codec5, getter5, value);
				addToJson(json, name6, codec6, getter6, value);
				addToJson(json, name7, codec7, getter7, value);
				addToJson(json, name8, codec8, getter8, value);
				addToJson(json, name9, codec9, getter9, value);
				addToJson(json, name10, codec10, getter10, value);
				addToJson(json, name11, codec11, getter11, value);
				addToJson(json, name12, codec12, getter12, value);
				addToJson(json, name13, codec13, getter13, value);
				return json;
			}

			@Override
			public T decode(JsonElement json) {
				JsonObject jsonObject = json.getAsJsonObject();
				return constructor.apply(
						codec1.decode(jsonObject.get(name1)),
						codec2.decode(jsonObject.get(name2)),
						codec3.decode(jsonObject.get(name3)),
						codec4.decode(jsonObject.get(name4)),
						codec5.decode(jsonObject.get(name5)),
						codec6.decode(jsonObject.get(name6)),
						codec7.decode(jsonObject.get(name7)),
						codec8.decode(jsonObject.get(name8)),
						codec9.decode(jsonObject.get(name9)),
						codec10.decode(jsonObject.get(name10)),
						codec11.decode(jsonObject.get(name11)),
						codec12.decode(jsonObject.get(name12)),
						codec13.decode(jsonObject.get(name13))
				);
			}
		};
	}

	public static <T, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14> Codec<T> struct(
			String name1, Codec<P1> codec1, Function<T, P1> getter1,
			String name2, Codec<P2> codec2, Function<T, P2> getter2,
			String name3, Codec<P3> codec3, Function<T, P3> getter3,
			String name4, Codec<P4> codec4, Function<T, P4> getter4,
			String name5, Codec<P5> codec5, Function<T, P5> getter5,
			String name6, Codec<P6> codec6, Function<T, P6> getter6,
			String name7, Codec<P7> codec7, Function<T, P7> getter7,
			String name8, Codec<P8> codec8, Function<T, P8> getter8,
			String name9, Codec<P9> codec9, Function<T, P9> getter9,
			String name10, Codec<P10> codec10, Function<T, P10> getter10,
			String name11, Codec<P11> codec11, Function<T, P11> getter11,
			String name12, Codec<P12> codec12, Function<T, P12> getter12,
			String name13, Codec<P13> codec13, Function<T, P13> getter13,
			String name14, Codec<P14> codec14, Function<T, P14> getter14,
			Functions.F14<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, T> constructor
	) {
		return new Codec<T>() {
			@Override
			public JsonElement encode(T value) {
				JsonObject json = new JsonObject();
				addToJson(json, name1, codec1, getter1, value);
				addToJson(json, name2, codec2, getter2, value);
				addToJson(json, name3, codec3, getter3, value);
				addToJson(json, name4, codec4, getter4, value);
				addToJson(json, name5, codec5, getter5, value);
				addToJson(json, name6, codec6, getter6, value);
				addToJson(json, name7, codec7, getter7, value);
				addToJson(json, name8, codec8, getter8, value);
				addToJson(json, name9, codec9, getter9, value);
				addToJson(json, name10, codec10, getter10, value);
				addToJson(json, name11, codec11, getter11, value);
				addToJson(json, name12, codec12, getter12, value);
				addToJson(json, name13, codec13, getter13, value);
				addToJson(json, name14, codec14, getter14, value);
				return json;
			}

			@Override
			public T decode(JsonElement json) {
				JsonObject jsonObject = json.getAsJsonObject();
				return constructor.apply(
						codec1.decode(jsonObject.get(name1)),
						codec2.decode(jsonObject.get(name2)),
						codec3.decode(jsonObject.get(name3)),
						codec4.decode(jsonObject.get(name4)),
						codec5.decode(jsonObject.get(name5)),
						codec6.decode(jsonObject.get(name6)),
						codec7.decode(jsonObject.get(name7)),
						codec8.decode(jsonObject.get(name8)),
						codec9.decode(jsonObject.get(name9)),
						codec10.decode(jsonObject.get(name10)),
						codec11.decode(jsonObject.get(name11)),
						codec12.decode(jsonObject.get(name12)),
						codec13.decode(jsonObject.get(name13)),
						codec14.decode(jsonObject.get(name14))
				);
			}
		};
	}

	public static <T, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15> Codec<T> struct(
			String name1, Codec<P1> codec1, Function<T, P1> getter1,
			String name2, Codec<P2> codec2, Function<T, P2> getter2,
			String name3, Codec<P3> codec3, Function<T, P3> getter3,
			String name4, Codec<P4> codec4, Function<T, P4> getter4,
			String name5, Codec<P5> codec5, Function<T, P5> getter5,
			String name6, Codec<P6> codec6, Function<T, P6> getter6,
			String name7, Codec<P7> codec7, Function<T, P7> getter7,
			String name8, Codec<P8> codec8, Function<T, P8> getter8,
			String name9, Codec<P9> codec9, Function<T, P9> getter9,
			String name10, Codec<P10> codec10, Function<T, P10> getter10,
			String name11, Codec<P11> codec11, Function<T, P11> getter11,
			String name12, Codec<P12> codec12, Function<T, P12> getter12,
			String name13, Codec<P13> codec13, Function<T, P13> getter13,
			String name14, Codec<P14> codec14, Function<T, P14> getter14,
			String name15, Codec<P15> codec15, Function<T, P15> getter15,
			Functions.F15<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, T> constructor
	) {
		return new Codec<T>() {
			@Override
			public JsonElement encode(T value) {
				JsonObject json = new JsonObject();
				addToJson(json, name1, codec1, getter1, value);
				addToJson(json, name2, codec2, getter2, value);
				addToJson(json, name3, codec3, getter3, value);
				addToJson(json, name4, codec4, getter4, value);
				addToJson(json, name5, codec5, getter5, value);
				addToJson(json, name6, codec6, getter6, value);
				addToJson(json, name7, codec7, getter7, value);
				addToJson(json, name8, codec8, getter8, value);
				addToJson(json, name9, codec9, getter9, value);
				addToJson(json, name10, codec10, getter10, value);
				addToJson(json, name11, codec11, getter11, value);
				addToJson(json, name12, codec12, getter12, value);
				addToJson(json, name13, codec13, getter13, value);
				addToJson(json, name14, codec14, getter14, value);
				addToJson(json, name15, codec15, getter15, value);
				return json;
			}

			@Override
			public T decode(JsonElement json) {
				JsonObject jsonObject = json.getAsJsonObject();
				return constructor.apply(
						codec1.decode(jsonObject.get(name1)),
						codec2.decode(jsonObject.get(name2)),
						codec3.decode(jsonObject.get(name3)),
						codec4.decode(jsonObject.get(name4)),
						codec5.decode(jsonObject.get(name5)),
						codec6.decode(jsonObject.get(name6)),
						codec7.decode(jsonObject.get(name7)),
						codec8.decode(jsonObject.get(name8)),
						codec9.decode(jsonObject.get(name9)),
						codec10.decode(jsonObject.get(name10)),
						codec11.decode(jsonObject.get(name11)),
						codec12.decode(jsonObject.get(name12)),
						codec13.decode(jsonObject.get(name13)),
						codec14.decode(jsonObject.get(name14)),
						codec15.decode(jsonObject.get(name15))
				);
			}
		};
	}

	public static <T, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16> Codec<T> struct(
			String name1, Codec<P1> codec1, Function<T, P1> getter1,
			String name2, Codec<P2> codec2, Function<T, P2> getter2,
			String name3, Codec<P3> codec3, Function<T, P3> getter3,
			String name4, Codec<P4> codec4, Function<T, P4> getter4,
			String name5, Codec<P5> codec5, Function<T, P5> getter5,
			String name6, Codec<P6> codec6, Function<T, P6> getter6,
			String name7, Codec<P7> codec7, Function<T, P7> getter7,
			String name8, Codec<P8> codec8, Function<T, P8> getter8,
			String name9, Codec<P9> codec9, Function<T, P9> getter9,
			String name10, Codec<P10> codec10, Function<T, P10> getter10,
			String name11, Codec<P11> codec11, Function<T, P11> getter11,
			String name12, Codec<P12> codec12, Function<T, P12> getter12,
			String name13, Codec<P13> codec13, Function<T, P13> getter13,
			String name14, Codec<P14> codec14, Function<T, P14> getter14,
			String name15, Codec<P15> codec15, Function<T, P15> getter15,
			String name16, Codec<P16> codec16, Function<T, P16> getter16,
			Functions.F16<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, T> constructor
	) {
		return new Codec<T>() {
			@Override
			public JsonElement encode(T value) {
				JsonObject json = new JsonObject();
				addToJson(json, name1, codec1, getter1, value);
				addToJson(json, name2, codec2, getter2, value);
				addToJson(json, name3, codec3, getter3, value);
				addToJson(json, name4, codec4, getter4, value);
				addToJson(json, name5, codec5, getter5, value);
				addToJson(json, name6, codec6, getter6, value);
				addToJson(json, name7, codec7, getter7, value);
				addToJson(json, name8, codec8, getter8, value);
				addToJson(json, name9, codec9, getter9, value);
				addToJson(json, name10, codec10, getter10, value);
				addToJson(json, name11, codec11, getter11, value);
				addToJson(json, name12, codec12, getter12, value);
				addToJson(json, name13, codec13, getter13, value);
				addToJson(json, name14, codec14, getter14, value);
				addToJson(json, name15, codec15, getter15, value);
				addToJson(json, name16, codec16, getter16, value);
				return json;
			}

			@Override
			public T decode(JsonElement json) {
				JsonObject jsonObject = json.getAsJsonObject();
				return constructor.apply(
						codec1.decode(jsonObject.get(name1)),
						codec2.decode(jsonObject.get(name2)),
						codec3.decode(jsonObject.get(name3)),
						codec4.decode(jsonObject.get(name4)),
						codec5.decode(jsonObject.get(name5)),
						codec6.decode(jsonObject.get(name6)),
						codec7.decode(jsonObject.get(name7)),
						codec8.decode(jsonObject.get(name8)),
						codec9.decode(jsonObject.get(name9)),
						codec10.decode(jsonObject.get(name10)),
						codec11.decode(jsonObject.get(name11)),
						codec12.decode(jsonObject.get(name12)),
						codec13.decode(jsonObject.get(name13)),
						codec14.decode(jsonObject.get(name14)),
						codec15.decode(jsonObject.get(name15)),
						codec16.decode(jsonObject.get(name16))
				);
			}
		};
	}

	public static <T, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17> Codec<T> struct(
			String name1, Codec<P1> codec1, Function<T, P1> getter1,
			String name2, Codec<P2> codec2, Function<T, P2> getter2,
			String name3, Codec<P3> codec3, Function<T, P3> getter3,
			String name4, Codec<P4> codec4, Function<T, P4> getter4,
			String name5, Codec<P5> codec5, Function<T, P5> getter5,
			String name6, Codec<P6> codec6, Function<T, P6> getter6,
			String name7, Codec<P7> codec7, Function<T, P7> getter7,
			String name8, Codec<P8> codec8, Function<T, P8> getter8,
			String name9, Codec<P9> codec9, Function<T, P9> getter9,
			String name10, Codec<P10> codec10, Function<T, P10> getter10,
			String name11, Codec<P11> codec11, Function<T, P11> getter11,
			String name12, Codec<P12> codec12, Function<T, P12> getter12,
			String name13, Codec<P13> codec13, Function<T, P13> getter13,
			String name14, Codec<P14> codec14, Function<T, P14> getter14,
			String name15, Codec<P15> codec15, Function<T, P15> getter15,
			String name16, Codec<P16> codec16, Function<T, P16> getter16,
			String name17, Codec<P17> codec17, Function<T, P17> getter17,
			Functions.F17<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, T> constructor
	) {
		return new Codec<T>() {
			@Override
			public JsonElement encode(T value) {
				JsonObject json = new JsonObject();
				addToJson(json, name1, codec1, getter1, value);
				addToJson(json, name2, codec2, getter2, value);
				addToJson(json, name3, codec3, getter3, value);
				addToJson(json, name4, codec4, getter4, value);
				addToJson(json, name5, codec5, getter5, value);
				addToJson(json, name6, codec6, getter6, value);
				addToJson(json, name7, codec7, getter7, value);
				addToJson(json, name8, codec8, getter8, value);
				addToJson(json, name9, codec9, getter9, value);
				addToJson(json, name10, codec10, getter10, value);
				addToJson(json, name11, codec11, getter11, value);
				addToJson(json, name12, codec12, getter12, value);
				addToJson(json, name13, codec13, getter13, value);
				addToJson(json, name14, codec14, getter14, value);
				addToJson(json, name15, codec15, getter15, value);
				addToJson(json, name16, codec16, getter16, value);
				addToJson(json, name17, codec17, getter17, value);
				return json;
			}

			@Override
			public T decode(JsonElement json) {
				JsonObject jsonObject = json.getAsJsonObject();
				return constructor.apply(
						codec1.decode(jsonObject.get(name1)),
						codec2.decode(jsonObject.get(name2)),
						codec3.decode(jsonObject.get(name3)),
						codec4.decode(jsonObject.get(name4)),
						codec5.decode(jsonObject.get(name5)),
						codec6.decode(jsonObject.get(name6)),
						codec7.decode(jsonObject.get(name7)),
						codec8.decode(jsonObject.get(name8)),
						codec9.decode(jsonObject.get(name9)),
						codec10.decode(jsonObject.get(name10)),
						codec11.decode(jsonObject.get(name11)),
						codec12.decode(jsonObject.get(name12)),
						codec13.decode(jsonObject.get(name13)),
						codec14.decode(jsonObject.get(name14)),
						codec15.decode(jsonObject.get(name15)),
						codec16.decode(jsonObject.get(name16)),
						codec17.decode(jsonObject.get(name17))
				);
			}
		};
	}

	public static <T, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18> Codec<T> struct(
			String name1, Codec<P1> codec1, Function<T, P1> getter1,
			String name2, Codec<P2> codec2, Function<T, P2> getter2,
			String name3, Codec<P3> codec3, Function<T, P3> getter3,
			String name4, Codec<P4> codec4, Function<T, P4> getter4,
			String name5, Codec<P5> codec5, Function<T, P5> getter5,
			String name6, Codec<P6> codec6, Function<T, P6> getter6,
			String name7, Codec<P7> codec7, Function<T, P7> getter7,
			String name8, Codec<P8> codec8, Function<T, P8> getter8,
			String name9, Codec<P9> codec9, Function<T, P9> getter9,
			String name10, Codec<P10> codec10, Function<T, P10> getter10,
			String name11, Codec<P11> codec11, Function<T, P11> getter11,
			String name12, Codec<P12> codec12, Function<T, P12> getter12,
			String name13, Codec<P13> codec13, Function<T, P13> getter13,
			String name14, Codec<P14> codec14, Function<T, P14> getter14,
			String name15, Codec<P15> codec15, Function<T, P15> getter15,
			String name16, Codec<P16> codec16, Function<T, P16> getter16,
			String name17, Codec<P17> codec17, Function<T, P17> getter17,
			String name18, Codec<P18> codec18, Function<T, P18> getter18,
			Functions.F18<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, T> constructor
	) {
		return new Codec<T>() {
			@Override
			public JsonElement encode(T value) {
				JsonObject json = new JsonObject();
				addToJson(json, name1, codec1, getter1, value);
				addToJson(json, name2, codec2, getter2, value);
				addToJson(json, name3, codec3, getter3, value);
				addToJson(json, name4, codec4, getter4, value);
				addToJson(json, name5, codec5, getter5, value);
				addToJson(json, name6, codec6, getter6, value);
				addToJson(json, name7, codec7, getter7, value);
				addToJson(json, name8, codec8, getter8, value);
				addToJson(json, name9, codec9, getter9, value);
				addToJson(json, name10, codec10, getter10, value);
				addToJson(json, name11, codec11, getter11, value);
				addToJson(json, name12, codec12, getter12, value);
				addToJson(json, name13, codec13, getter13, value);
				addToJson(json, name14, codec14, getter14, value);
				addToJson(json, name15, codec15, getter15, value);
				addToJson(json, name16, codec16, getter16, value);
				addToJson(json, name17, codec17, getter17, value);
				addToJson(json, name18, codec18, getter18, value);
				return json;
			}

			@Override
			public T decode(JsonElement json) {
				JsonObject jsonObject = json.getAsJsonObject();
				return constructor.apply(
						codec1.decode(jsonObject.get(name1)),
						codec2.decode(jsonObject.get(name2)),
						codec3.decode(jsonObject.get(name3)),
						codec4.decode(jsonObject.get(name4)),
						codec5.decode(jsonObject.get(name5)),
						codec6.decode(jsonObject.get(name6)),
						codec7.decode(jsonObject.get(name7)),
						codec8.decode(jsonObject.get(name8)),
						codec9.decode(jsonObject.get(name9)),
						codec10.decode(jsonObject.get(name10)),
						codec11.decode(jsonObject.get(name11)),
						codec12.decode(jsonObject.get(name12)),
						codec13.decode(jsonObject.get(name13)),
						codec14.decode(jsonObject.get(name14)),
						codec15.decode(jsonObject.get(name15)),
						codec16.decode(jsonObject.get(name16)),
						codec17.decode(jsonObject.get(name17)),
						codec18.decode(jsonObject.get(name18))
				);
			}
		};
	}

	public static <T, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19> Codec<T> struct(
			String name1, Codec<P1> codec1, Function<T, P1> getter1,
			String name2, Codec<P2> codec2, Function<T, P2> getter2,
			String name3, Codec<P3> codec3, Function<T, P3> getter3,
			String name4, Codec<P4> codec4, Function<T, P4> getter4,
			String name5, Codec<P5> codec5, Function<T, P5> getter5,
			String name6, Codec<P6> codec6, Function<T, P6> getter6,
			String name7, Codec<P7> codec7, Function<T, P7> getter7,
			String name8, Codec<P8> codec8, Function<T, P8> getter8,
			String name9, Codec<P9> codec9, Function<T, P9> getter9,
			String name10, Codec<P10> codec10, Function<T, P10> getter10,
			String name11, Codec<P11> codec11, Function<T, P11> getter11,
			String name12, Codec<P12> codec12, Function<T, P12> getter12,
			String name13, Codec<P13> codec13, Function<T, P13> getter13,
			String name14, Codec<P14> codec14, Function<T, P14> getter14,
			String name15, Codec<P15> codec15, Function<T, P15> getter15,
			String name16, Codec<P16> codec16, Function<T, P16> getter16,
			String name17, Codec<P17> codec17, Function<T, P17> getter17,
			String name18, Codec<P18> codec18, Function<T, P18> getter18,
			String name19, Codec<P19> codec19, Function<T, P19> getter19,
			Functions.F19<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, T> constructor
	) {
		return new Codec<T>() {
			@Override
			public JsonElement encode(T value) {
				JsonObject json = new JsonObject();
				addToJson(json, name1, codec1, getter1, value);
				addToJson(json, name2, codec2, getter2, value);
				addToJson(json, name3, codec3, getter3, value);
				addToJson(json, name4, codec4, getter4, value);
				addToJson(json, name5, codec5, getter5, value);
				addToJson(json, name6, codec6, getter6, value);
				addToJson(json, name7, codec7, getter7, value);
				addToJson(json, name8, codec8, getter8, value);
				addToJson(json, name9, codec9, getter9, value);
				addToJson(json, name10, codec10, getter10, value);
				addToJson(json, name11, codec11, getter11, value);
				addToJson(json, name12, codec12, getter12, value);
				addToJson(json, name13, codec13, getter13, value);
				addToJson(json, name14, codec14, getter14, value);
				addToJson(json, name15, codec15, getter15, value);
				addToJson(json, name16, codec16, getter16, value);
				addToJson(json, name17, codec17, getter17, value);
				addToJson(json, name18, codec18, getter18, value);
				addToJson(json, name19, codec19, getter19, value);
				return json;
			}

			@Override
			public T decode(JsonElement json) {
				JsonObject jsonObject = json.getAsJsonObject();
				return constructor.apply(
						codec1.decode(jsonObject.get(name1)),
						codec2.decode(jsonObject.get(name2)),
						codec3.decode(jsonObject.get(name3)),
						codec4.decode(jsonObject.get(name4)),
						codec5.decode(jsonObject.get(name5)),
						codec6.decode(jsonObject.get(name6)),
						codec7.decode(jsonObject.get(name7)),
						codec8.decode(jsonObject.get(name8)),
						codec9.decode(jsonObject.get(name9)),
						codec10.decode(jsonObject.get(name10)),
						codec11.decode(jsonObject.get(name11)),
						codec12.decode(jsonObject.get(name12)),
						codec13.decode(jsonObject.get(name13)),
						codec14.decode(jsonObject.get(name14)),
						codec15.decode(jsonObject.get(name15)),
						codec16.decode(jsonObject.get(name16)),
						codec17.decode(jsonObject.get(name17)),
						codec18.decode(jsonObject.get(name18)),
						codec19.decode(jsonObject.get(name19))
				);
			}
		};
	}

	public static <T, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20> Codec<T> struct(
			String name1, Codec<P1> codec1, Function<T, P1> getter1,
			String name2, Codec<P2> codec2, Function<T, P2> getter2,
			String name3, Codec<P3> codec3, Function<T, P3> getter3,
			String name4, Codec<P4> codec4, Function<T, P4> getter4,
			String name5, Codec<P5> codec5, Function<T, P5> getter5,
			String name6, Codec<P6> codec6, Function<T, P6> getter6,
			String name7, Codec<P7> codec7, Function<T, P7> getter7,
			String name8, Codec<P8> codec8, Function<T, P8> getter8,
			String name9, Codec<P9> codec9, Function<T, P9> getter9,
			String name10, Codec<P10> codec10, Function<T, P10> getter10,
			String name11, Codec<P11> codec11, Function<T, P11> getter11,
			String name12, Codec<P12> codec12, Function<T, P12> getter12,
			String name13, Codec<P13> codec13, Function<T, P13> getter13,
			String name14, Codec<P14> codec14, Function<T, P14> getter14,
			String name15, Codec<P15> codec15, Function<T, P15> getter15,
			String name16, Codec<P16> codec16, Function<T, P16> getter16,
			String name17, Codec<P17> codec17, Function<T, P17> getter17,
			String name18, Codec<P18> codec18, Function<T, P18> getter18,
			String name19, Codec<P19> codec19, Function<T, P19> getter19,
			String name20, Codec<P20> codec20, Function<T, P20> getter20,
			Functions.F20<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, T> constructor
	) {
		return new Codec<T>() {
			@Override
			public JsonElement encode(T value) {
				JsonObject json = new JsonObject();
				addToJson(json, name1, codec1, getter1, value);
				addToJson(json, name2, codec2, getter2, value);
				addToJson(json, name3, codec3, getter3, value);
				addToJson(json, name4, codec4, getter4, value);
				addToJson(json, name5, codec5, getter5, value);
				addToJson(json, name6, codec6, getter6, value);
				addToJson(json, name7, codec7, getter7, value);
				addToJson(json, name8, codec8, getter8, value);
				addToJson(json, name9, codec9, getter9, value);
				addToJson(json, name10, codec10, getter10, value);
				addToJson(json, name11, codec11, getter11, value);
				addToJson(json, name12, codec12, getter12, value);
				addToJson(json, name13, codec13, getter13, value);
				addToJson(json, name14, codec14, getter14, value);
				addToJson(json, name15, codec15, getter15, value);
				addToJson(json, name16, codec16, getter16, value);
				addToJson(json, name17, codec17, getter17, value);
				addToJson(json, name18, codec18, getter18, value);
				addToJson(json, name19, codec19, getter19, value);
				addToJson(json, name20, codec20, getter20, value);
				return json;
			}

			@Override
			public T decode(JsonElement json) {
				JsonObject jsonObject = json.getAsJsonObject();
				return constructor.apply(
						codec1.decode(jsonObject.get(name1)),
						codec2.decode(jsonObject.get(name2)),
						codec3.decode(jsonObject.get(name3)),
						codec4.decode(jsonObject.get(name4)),
						codec5.decode(jsonObject.get(name5)),
						codec6.decode(jsonObject.get(name6)),
						codec7.decode(jsonObject.get(name7)),
						codec8.decode(jsonObject.get(name8)),
						codec9.decode(jsonObject.get(name9)),
						codec10.decode(jsonObject.get(name10)),
						codec11.decode(jsonObject.get(name11)),
						codec12.decode(jsonObject.get(name12)),
						codec13.decode(jsonObject.get(name13)),
						codec14.decode(jsonObject.get(name14)),
						codec15.decode(jsonObject.get(name15)),
						codec16.decode(jsonObject.get(name16)),
						codec17.decode(jsonObject.get(name17)),
						codec18.decode(jsonObject.get(name18)),
						codec19.decode(jsonObject.get(name19)),
						codec20.decode(jsonObject.get(name20))
				);
			}
		};
	}
}
