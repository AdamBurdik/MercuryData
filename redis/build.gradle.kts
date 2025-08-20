plugins {
    id("java")
}

group = "me.adamix.mercury.data.redis"
version = "1.0.0"

repositories {
}

dependencies {
    implementation("redis.clients:jedis:6.0.0")
    implementation(project(":api"))
}
