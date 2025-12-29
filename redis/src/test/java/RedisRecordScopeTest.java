import me.adamix.mercury.data.MercuryCollection;
import me.adamix.mercury.data.MercuryDatabase;
import me.adamix.mercury.data.codec.Codec;
import me.adamix.mercury.data.codec.StructCodec;
import me.adamix.mercury.data.key.Key;
import me.adamix.mercury.data.module.StoreModule;
import me.adamix.mercury.data.redis.module.RedisStoreModule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RedisRecordScopeTest {
	record Value(int number, Map<String, String> map) {
		public static final Codec<Value> CODEC = StructCodec.struct(
				"number", Codec.INT, Value::number,
				"map", Codec.STRING.map(Codec.STRING), Value::map,
				Value::new
		);
	}

	record Person(String firstName, String lastName, Map<Integer, Value> map) {
		public static final Codec<Person> CODEC = StructCodec.struct(
				"first_name", Codec.STRING, Person::firstName,
				"last_name", Codec.STRING, Person::lastName,
				"map", Codec.INT.map(Value.CODEC), Person::map,
				Person::new
		);
	}

	private MercuryDatabase database;
	private MercuryCollection collection;
	private Person data;

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
		Map<Integer, Value> map = new HashMap<>();
		map.put(0, new Value(69, Map.of("ping", "pong")));
		map.put(3, new Value(420, Map.of("key", "value")));
		map.put(1000, new Value(-523, Map.of()));

		data = new Person("John", "Doe", map);
		collection.setSync(entityKey, Person.CODEC, data);
	}

	@Test
	void testGetField() {
		Optional<String> newNameOpt = collection.record(entityKey)
				.getFieldSync(Key.of("first_name"), Codec.STRING);

		assertTrue(newNameOpt.isPresent());
		assertEquals("John", newNameOpt.get());
	}

	@Test
	void testSetField() {
		Optional<String> newNameOpt = collection.record(entityKey)
				.setFieldSync(Key.of("first_name"), Codec.STRING, "Hello, World!")
				.getFieldSync(Key.of("first_name"), Codec.STRING);

		assertTrue(newNameOpt.isPresent());
		assertEquals("Hello, World!", newNameOpt.get());
	}

	@Test
	void testUpdateField() {
		Optional<String> newNameOpt = collection.record(entityKey)
				.updateFieldIfPresentSync(Key.of("last_name"), Codec.STRING, value -> value + " Mark")
				.getFieldSync(Key.of("last_name"), Codec.STRING);

		assertTrue(newNameOpt.isPresent());
		assertEquals("Doe Mark", newNameOpt.get());
	}

	@Test
	void testComputeIfAbsent() {
		String middleName = collection.record(entityKey)
				.computeFieldJsonIfAbsentSync(Key.of("middle_name"), Codec.STRING, () -> "Mark");

		assertEquals("Mark", middleName);
	}

	@Test
	void testRemoveField() {
		collection.record(entityKey)
				.removeFieldJsonSync(Key.of("first_name"));

		Optional<String> newNameOpt = collection.record(entityKey)
				.getFieldSync(Key.of("first_name"), Codec.STRING);

		assertTrue(newNameOpt.isEmpty());
	}

	@Test
	void testExistsField() {
		boolean exists = collection.record(entityKey)
				.fieldJsonExistsSync(Key.of("first_name"));

		assertTrue(exists);

		collection.record(entityKey)
				.removeFieldJsonSync(Key.of("first_name"));

		exists = collection.record(entityKey)
				.fieldJsonExistsSync(Key.of("first_name"));

		assertFalse(exists);
	}

	@Test
	void testClear() {
		Optional<String> firstNameOpt = collection.record(entityKey)
				.getFieldSync(Key.of("first_name"), Codec.STRING);

		Optional<Map<Integer, Value>> mapOpt = collection.record(entityKey)
				.getFieldSync(Key.of("map"), Codec.INT.map(Value.CODEC));

		assertTrue(firstNameOpt.isPresent());
		assertTrue(mapOpt.isPresent());

		collection.record(entityKey)
				.clearSync();

		firstNameOpt = collection.record(entityKey)
				.getFieldSync(Key.of("first_name"), Codec.STRING);

		mapOpt = collection.record(entityKey)
				.getFieldSync(Key.of("map"), Codec.INT.map(Value.CODEC));

		assertTrue(firstNameOpt.isEmpty());
		assertTrue(mapOpt.isEmpty());
	}
}
