package net.smoothplugins.smoothsync.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import net.smoothplugins.smoothbase.connection.MongoConnection;
import net.smoothplugins.smoothbase.connection.RedisConnection;
import net.smoothplugins.smoothbase.storage.MongoStorage;
import net.smoothplugins.smoothbase.storage.RedisStorage;
public class StorageModule extends AbstractModule {

    @Provides @Named("user") @Singleton
    public MongoStorage provideUserMongoStorage(MongoConnection mongoConnection) {
        return new MongoStorage(mongoConnection.getDatabase().getCollection("users"));
    }

    @Provides
    @Named("user") @Singleton
    public RedisStorage provideUserRedisStorage(RedisConnection redisConnection) {
        return new RedisStorage(redisConnection, "user");
    }
}
