import me.adamix.mercury.data.MercuryCollection;
import me.adamix.mercury.data.MercuryDatabase;
import me.adamix.mercury.data.codec.Codec;
import me.adamix.mercury.data.codec.StructCodec;
import me.adamix.mercury.data.key.Key;
import me.adamix.mercury.data.module.StoreModule;
import me.adamix.mercury.data.operation.update.UpdateField;
import me.adamix.mercury.data.query.QueryResult;
import me.adamix.mercury.data.redis.module.RedisStoreModule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RedisCollectionTest {
	record TestData(String name, int intNumber, long longNumber, List<String> stringList, Map<String, String> map, Set<Long> set) {
		public static final Codec<TestData> CODEC = StructCodec.struct(
				"name", Codec.STRING, TestData::name,
				"int_number", Codec.INT, TestData::intNumber,
				"long_number", Codec.LONG, TestData::longNumber,
				"string_list", Codec.STRING.list(), TestData::stringList,
				"map", Codec.STRING.map(Codec.STRING), TestData::map,
				"set", Codec.LONG.set(), TestData::set,
				TestData::new
		);
	}

	private MercuryDatabase database;
	private MercuryCollection collection;

	@BeforeAll
	void startRedis() {
		StoreModule storeModule = new RedisStoreModule("localhost", 6379);
		database = MercuryDatabase.create(storeModule);

		collection = database.getCollection("test_collection");
	}

	@AfterAll
	void shutdown() {
		if (database != null) {
			database.close();
		}
	}

	@AfterEach
	void clearCollection() {
		collection.clear();
	}

	private TestData createData() {
		return new TestData(
				"adamix",
				42,
				123456789L,
				List.of("item1", "item2"),
				Map.of("key1", "value1", "key2", "value2"),
				Set.of(20L)
		);
	}

	@Test
	void testSetAndGet() {
		TestData data = createData();
		collection.setSync(Key.of("first_value"), TestData.CODEC, data);

		Optional<TestData> fetched = collection.getSync(Key.of("first_value"), TestData.CODEC);
		assertTrue(fetched.isPresent());
		assertEquals(data, fetched.get());
	}

	@Test
	void testSetAndGetEmptyList() {
		TestData data = new TestData("John Doe", -52, 2L, List.of(), Map.of("key1", "value1"), Set.of());

		collection.setSync(Key.of("first_value"), TestData.CODEC, data);
		Optional<TestData> fetched = collection.getSync(Key.of("first_value"), TestData.CODEC);

		assertTrue(fetched.isPresent());
		assertEquals(data, fetched.get());

	}

	@Test
	void testSetAndGetField() {
		TestData data = createData();
		collection.setSync(Key.of("first_value"), TestData.CODEC, data);

		collection.record(Key.of("first_value"))
				.setFieldSync(Key.of("name"), Codec.STRING, "new_name");

		Optional<String> nameField = collection.record(Key.of("first_value")).getFieldSync(Key.of("name"), Codec.STRING);
		assertTrue(nameField.isPresent());
		assertEquals("new_name", nameField.get());
	}

	@Test
	void testUpdateField() {
		TestData data = createData();
		collection.setSync(Key.of("first_value"), TestData.CODEC, data);

		collection.record(Key.of("first_value"))
				.setFieldSync(Key.of("name"), Codec.STRING, "Hello, World");

		Optional<TestData> fetched = collection.getSync(Key.of("first_value"), TestData.CODEC);
		assertTrue(fetched.isPresent());
		assertEquals("Hello, World", fetched.get().name());
		assertEquals(data.intNumber(), fetched.get().intNumber());
	}

	@Test
	void testUpdateMapField() {
		TestData data = createData();
		collection.setSync(Key.of("first_value"), TestData.CODEC, data);

		collection.record(Key.of("first_value"))
				.setFieldSync(
						Key.of("map", "key420"),
						Codec.STRING,
						"value69"
				);

		Optional<Map<String, String>> fetched = collection.record(Key.of("first_value"))
				.getFieldSync(Key.of("map"), Codec.STRING.map(Codec.STRING));
		System.out.println("Fetched: " + fetched);
		assertEquals("value69", fetched.get().get("key420"));
	}

	@Test
	void testRemove() {
		TestData data = createData();
		collection.setSync(Key.of("first_value"), TestData.CODEC, data);

		collection.removeSync(Key.of("first_value"));
		Optional<TestData> fetched = collection.getSync(Key.of("first_value"), TestData.CODEC);
		assertTrue(fetched.isEmpty());
	}

	@Test
	void testFindQuery() {
		TestData data1 = new TestData("first_name", 151, 525232L, List.of("first", "second"), Map.of(), Set.of(100L, 5245L));
		TestData data2 = new TestData("second_name", -52, 2L, List.of(), Map.of("key1", "value1"), Set.of(100L, -32L));

		collection.setSync(Key.of("first_value"), TestData.CODEC, data1);
		collection.setSync(Key.of("second_value"), TestData.CODEC, data2);

		QueryResult<TestData> result = collection.find(TestData.CODEC)
				.where(Key.of("int_number"), Codec.INT, bal -> bal > 150)
				.execute();

		assertEquals(1, result.collection().size());
		assertTrue(result.getFirst().stream().anyMatch(d -> d.name().equals("first_name")));
	}

	@Test
	void testConcurrentUpdateAndClear() throws InterruptedException {
		collection.setSync(Key.of("first_value"), TestData.CODEC, createData());

		CountDownLatch pushDone = new CountDownLatch(1);
		CountDownLatch clearStart = new CountDownLatch(1);

		Thread addItemThread = new Thread(() -> {
			collection.record(Key.of("first_value"))
					.list(Key.of("string_list"))
					.pushBackSync(Codec.STRING, "ExampleItem");
			pushDone.countDown();
			try {
				clearStart.await();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}, "AddItemThread");

		Thread clearInventoryThread = new Thread(() -> {
			try {
				pushDone.await();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			collection.record(Key.of("first_value"))
					.list(Key.of("string_list"))
					.clearSync();
			clearStart.countDown();
		}, "ClearInventoryThread");

		addItemThread.start();
		clearInventoryThread.start();

		addItemThread.join();
		clearInventoryThread.join();

		Optional<TestData> fetched = collection.getSync(Key.of("first_value"), TestData.CODEC);

		assertTrue(fetched.isPresent(), "Data should still exist after concurrent operations");
		assertEquals(List.of(), fetched.get().stringList, "List should be cleared after concurrent update and clear");
	}
}
