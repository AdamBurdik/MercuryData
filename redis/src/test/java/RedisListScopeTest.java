import me.adamix.mercury.data.MercuryCollection;
import me.adamix.mercury.data.MercuryDatabase;
import me.adamix.mercury.data.codec.Codec;
import me.adamix.mercury.data.codec.StructCodec;
import me.adamix.mercury.data.key.Key;
import me.adamix.mercury.data.module.StoreModule;
import me.adamix.mercury.data.redis.module.RedisStoreModule;
import me.adamix.mercury.data.scope.ListScope;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.text.CollationKey;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RedisListScopeTest {
	record Value(String key, List<String> values) {
		public static final Codec<Value> CODEC = StructCodec.struct(
				"key", Codec.STRING, Value::key,
				"value", Codec.STRING.list(), Value::values,
				Value::new
		);
	}

	record TestData(
			String name,
			Set<String> stringList,
			Set<Value> valueList

	) {
		public static final Codec<TestData> CODEC = StructCodec.struct(
				"name", Codec.STRING, TestData::name,
				"string_list", Codec.STRING.set(), TestData::stringList,
				"nested_list", Value.CODEC.set(), TestData::valueList,
				TestData::new
		);
	}

	private MercuryDatabase database;
	private MercuryCollection collection;
	private TestData data;

	private final Key entityKey = Key.of("entity_key");

	@BeforeAll
	void startRedis() {
		StoreModule storeModule = new RedisStoreModule("localhost", 6379);
		database = MercuryDatabase.create(storeModule);

		collection = database.getCollection("test_collection");
	}

	@AfterAll
	void shutdown() {
		if (database != null) {
			collection.clearSync();
			database.close();
		}
	}

	@AfterEach
	void clearCollection() {
		collection.clear();
	}

	@BeforeEach
	void setData() {
		data = new TestData(
				"John Doe",
				new LinkedHashSet<>(List.of("first_element", "second_element", "last_element")),
				Set.of(
						new Value(
								"key1",
								List.of("key1=1", "key1=2", "key1=2")
						),
						new Value(
								"key2",
								List.of("key2=1", "key2=2", "key2=2")
						),
						new Value(
								"key3",
								List.of("key3=1", "key3=2", "key3=2")
						)
				)
		);

		collection.setSync(entityKey, TestData.CODEC, data);
	}

	@Test
	void testClear() {
		int size = collection.record(entityKey)
				.list(Key.of("string_list"))
				.sizeSync();

		assertEquals(3, size);
		collection.record(entityKey)
				.list(Key.of("string_list"))
				.clearSync();

		size = collection.record(entityKey)
				.list(Key.of("string_list"))
				.sizeSync();

		assertEquals(0, size);
	}

	@Test
	void testPushFront() {
		int size = collection.record(entityKey)
				.list(Key.of("string_list"))
				.sizeSync();

		assertEquals(3, size);

		collection.record(entityKey)
				.list(Key.of("string_list"))
				.pushFrontSync(Codec.STRING, "Hello, World!");

		size = collection.record(entityKey)
				.list(Key.of("string_list"))
				.sizeSync();

		assertEquals(4, size);

		var fetched = collection.record(entityKey)
				.list(Key.of("string_list"))
				.getSync(0, Codec.STRING);

		assertTrue(fetched.isPresent());

		assertEquals("Hello, World!", fetched.get());
	}

	@Test
	void testPushBack() {
		int size = collection.record(entityKey)
				.list(Key.of("string_list"))
				.sizeSync();

		assertEquals(3, size);

		collection.record(entityKey)
				.list(Key.of("string_list"))
				.pushBackSync(Codec.STRING, "Bye, World!");

		size = collection.record(entityKey)
				.list(Key.of("string_list"))
				.sizeSync();

		assertEquals(4, size);

		var fetched = collection.record(entityKey)
				.list(Key.of("string_list"))
				.getSync(3, Codec.STRING);

		assertTrue(fetched.isPresent());

		assertEquals("Bye, World!", fetched.get());
	}

	@Test
	void testPushBackToEmptyList() {
		collection.record(entityKey)
				.list(Key.of("string_list"))
				.clearSync();

		int size = collection.record(entityKey)
				.list(Key.of("string_list"))
				.sizeSync();

		assertEquals(0, size);

		collection.record(entityKey)
				.list(Key.of("string_list"))
				.pushBackSync(Codec.STRING, "Bye, World!");

		size = collection.record(entityKey)
				.list(Key.of("string_list"))
				.sizeSync();

		assertEquals(1, size);
	}

	@Test
	void testRemove() {
		Collection<String> list = collection.record(entityKey)
				.list(Key.of("string_list"))
				.getAllSync(Codec.STRING);

		assertEquals(
				List.of("first_element", "second_element", "last_element"),
				List.copyOf(list)
		);

		collection.record(entityKey)
				.list(Key.of("string_list"))
				.removeSync(1);

		list = collection.record(entityKey)
				.list(Key.of("string_list"))
				.getAllSync(Codec.STRING);

		assertEquals(
				List.of("first_element", "last_element"),
				List.copyOf(list)
		);
	}

	@Test
	void testRemoveLast() {
		Collection<String> list = collection.record(entityKey)
				.list(Key.of("string_list"))
				.getAllSync(Codec.STRING);

		assertEquals(
				List.of("first_element", "second_element", "last_element"),
				List.copyOf(list)
		);

		collection.record(entityKey)
				.list(Key.of("string_list"))
				.removeSync(2);

		list = collection.record(entityKey)
				.list(Key.of("string_list"))
				.getAllSync(Codec.STRING);

		assertEquals(
				List.of("first_element", "second_element"),
				List.copyOf(list)
		);
	}

	@Test
	void testRemoveIf() {
		Collection<String> list = collection.record(entityKey)
				.list(Key.of("string_list"))
				.getAllSync(Codec.STRING);

		assertEquals(
				List.of("first_element", "second_element", "last_element"),
				List.copyOf(list)
		);

		collection.record(entityKey)
				.list(Key.of("string_list"))
				.removeIfSync(Codec.STRING, s -> s.equals("second_element"));

		list = collection.record(entityKey)
				.list(Key.of("string_list"))
				.getAllSync(Codec.STRING);

		assertEquals(
				List.of("first_element", "last_element"),
				List.copyOf(list)
		);
	}
}
