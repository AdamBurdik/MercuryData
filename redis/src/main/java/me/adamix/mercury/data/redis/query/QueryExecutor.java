package me.adamix.mercury.data.redis.query;

import me.adamix.mercury.data.query.QueryResult;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface QueryExecutor<T> {
    @NotNull QueryResult<T> apply(RedisFindQueryBuilder<T> builder) throws Exception;
}