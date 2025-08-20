[![](https://www.jitpack.io/v/adamBurdik/MercuryData.svg)](https://www.jitpack.io/#adamBurdik/MercuryData)

# MercuryData
Simple library to easily access and manage databases.

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
2. Create store module
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