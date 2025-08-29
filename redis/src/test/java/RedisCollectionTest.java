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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;

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
		collection.find(TestData.CODEC).execute().collection()
				.forEach(data -> {
					collection.remove(data.key());
				});
	}

	private TestData createData() {
		return new TestData(
				"adamix",
				42,
				123456789L,
				List.of("alpha", "beta", "gamma"),
				Map.of("key1", "value1", "key2", "value2"),
				Set.of(10L, 20L, 30L)
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
	void testUpdateField() {
		TestData data = createData();
		collection.setSync(Key.of("first_value"), TestData.CODEC, data);

		collection.updateFieldSync(Key.of("first_value"),
				UpdateField.of(Key.of("name"), Codec.STRING, "Hello, World"),
				true);

		Optional<TestData> fetched = collection.getSync(Key.of("first_value"), TestData.CODEC);
		assertTrue(fetched.isPresent());
		assertEquals("Hello, World", fetched.get().name());
		assertEquals(data.intNumber(), fetched.get().intNumber());
	}

	@Test
	void testRemove() {
		TestData data = createData();
		collection.setSync(Key.of("first_value"), TestData.CODEC, data);

		boolean removed = collection.removeSync(Key.of("first_value"));
		assertTrue(removed);

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

		CountDownLatch latch = new CountDownLatch(1);

		Thread addItemThread = new Thread(() -> {
			collection.updateFieldSync(Key.of("first_value"),
					UpdateField.of("string_list", Codec.STRING.list(), (Function<List<String>, List<String>>) l -> {
						List<String> list = new ArrayList<>(l);
						list.add("ExampleItem");
						try {
							Thread.sleep(50); // simulate delay
							latch.countDown();
						} catch (InterruptedException e) {
							throw new RuntimeException(e);
						}
						return list;
					}), true);
		});

		Thread clearInventoryThread = new Thread(() -> {
			try {
				latch.await();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			collection.updateFieldSync(Key.of("first_value"),
					UpdateField.of("string_list", Codec.STRING.list(), List.of()));
		});

		System.out.println("AddItem: " + addItemThread.getName());
		System.out.println("Clear: " + clearInventoryThread.getName());

		addItemThread.start();

		clearInventoryThread.start();

		addItemThread.join();
		clearInventoryThread.join();

		Optional<TestData> fetched = collection.getSync(Key.of("first_value"), TestData.CODEC);

		assertTrue(fetched.isPresent());
		assertEquals(List.of(), fetched.get().stringList);
	}
}
