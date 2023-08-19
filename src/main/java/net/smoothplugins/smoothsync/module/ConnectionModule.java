package net.smoothplugins.smoothsync.module;

import com.google.inject.AbstractModule;
import net.smoothplugins.smoothbase.connection.MongoConnection;
import net.smoothplugins.smoothbase.connection.RedisConnection;

public class ConnectionModule extends AbstractModule {

    private final MongoConnection mongoConnection;
    private final RedisConnection redisConnection;

    public ConnectionModule(MongoConnection mongoConnection, RedisConnection redisConnection) {
        this.mongoConnection = mongoConnection;
        this.redisConnection = redisConnection;
    }

    @Override
    protected void configure() {
        bind(MongoConnection.class).toInstance(mongoConnection);
        bind(RedisConnection.class).toInstance(redisConnection);
    }
}