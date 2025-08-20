[![](https://www.jitpack.io/v/adamBurdik/MercuryData.svg)](https://www.jitpack.io/#adamBurdik/MercuryData)

# MercuryData
Simple library to easily access and manage databases.

# How To Use
1. Add MercuryData as a dependency to your project, using jitpack
```kotlin
maven { url = uri("https://www.jitpack.io") }

dependencies {
    implementation("com.github.AdamBurdik:MercuryData:api:Tag")
    
    // And database implementation
    // For example, redis
    implementation("com.github.AdamBurdik:MercuryData:redis:Tag")
}
```
2. Create store module (For example, redis)
```java
RedisStoreModule storeModule = new RedisStoreModule("localhost", 6379);
```
3. Create a database with previously created store module
```java
MercuryDatabase database = MercuryDatabase.create(storeModule);
```
4. Create a collection
```java
MercuryCollection collection = database.getCollection("example_collection");
```

# Examples
Library is heavily designed to use codecs.
You can use built-in codecs or define your own.

```java
record Person(String name, int balance, boolean isAdult) {
    public static Codec<Person> CODEC = StructCodec.struct(
            "name", Codec.STRING, Person::name,
            "balance", Codec.INT, Person::balance,
            "is_adult", Codec.BOOLEAN, Person::isAdult,
            Person::new
    );
}
```
With defined codec, you can use database calls much easily

Currently, there are five different operations you can execute:
- Set
- Get
- Remove
- UpdateField
- Find

You can execute operations either synchronously or asynchronously using CompletableFuture.


```java
MercuryCollection collection = database.getCollection("example_collection");

var person = new Person("John", 1000, true);

// Setting value by key
collection.setSync(Key.of("example_key"), Person.CODEC, person);

// Getting value by key
Optional<Person> result = collection.getSync(Key.of("example_key"), Person.CODEC);

// Removing value by key
collection.removeSync(Key.of("example_key"));

// Updating value field
// This example changes field name from "John" to "Doe"
collection.updateFieldSync(
		Key.of("example_key"),
        UpdateField.of(Key.of("name"), Codec.STRING, "Doe")
);

// Finding
// This example gets all people with balance greater than 150
QueryResult<Person> result = collection.find(Person.CODEC)
		.where(Key.of("balance"), Codec.INT, bal -> bal > 150)
		.execute();
```

Even the library is designed to use codecs, you don't need to.
You can pass JsonElement to most of the operations.
```java
JsonElement jsonElement = ...;

collection.setSync(Key.of("example_key"), jsonElement);

JsonElement found = collection.getSync(Key.of("example_key"));

collection.removeSync(Key.of("example_key"))
```

# How To Build
1. Clone the repository
```bash
   git clone https://github.com/AdamBurdik/MercuryData.git
```
2. Navigate to the directory
```bash
   cd MercuryData
```
3. Build the module
```bash
   gradlew build
```

# License
MercuryData is licensed under the MIT License, except for the codec implementation,  
which is derived from Minestom and licensed under the Apache License 2.0.  
See the [LICENSE](LICENSE) file for details.