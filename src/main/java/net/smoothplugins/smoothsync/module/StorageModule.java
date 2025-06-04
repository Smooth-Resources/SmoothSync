package net.smoothplugins.smoothsync.module;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import net.smoothplugins.smoothbase.common.database.nosql.MongoDBDatabase;
import net.smoothplugins.smoothbase.common.database.nosql.RedisDatabase;
public class StorageModule extends AbstractModule {

    private final RedisDatabase redisDatabase;
    private final MongoDBDatabase mongoDBDatabase;

    public StorageModule(RedisDatabase redisDatabase, MongoDBDatabase mongoDBDatabase) {
        this.redisDatabase = redisDatabase;
        this.mongoDBDatabase = mongoDBDatabase;
    }

    @Override
    protected void configure() {
        bind(RedisDatabase.class).annotatedWith(Names.named("user")).toInstance(redisDatabase);
        bind(MongoDBDatabase.class).annotatedWith(Names.named("user")).toInstance(mongoDBDatabase);
    }
}
