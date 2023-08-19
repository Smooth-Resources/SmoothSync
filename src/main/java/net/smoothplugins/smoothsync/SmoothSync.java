package net.smoothplugins.smoothsync;

import com.google.inject.Guice;
import com.google.inject.Injector;
import net.smoothplugins.smoothbase.configuration.Configuration;
import net.smoothplugins.smoothbase.connection.MongoConnection;
import net.smoothplugins.smoothbase.connection.RedisConnection;
import net.smoothplugins.smoothsync.module.ConfigurationModule;
import net.smoothplugins.smoothsync.module.ConnectionModule;
import net.smoothplugins.smoothsync.module.SmoothSyncModule;
import org.bukkit.plugin.java.JavaPlugin;

public final class SmoothSync extends JavaPlugin {

    private static Injector injector;

    @Override
    public void onEnable() {
        // Plugin startup logic
        Configuration config = new Configuration(this, "config");
        Configuration messages = new Configuration(this, "messages");

        String uri = config.getString("mongo.uri");
        String databaseName = config.getString("mongo.database");
        MongoConnection mongoConnection = new MongoConnection(uri, databaseName);

        String redisHost = config.getString("redis.host");
        int redisPort = config.getInt("redis.port");
        String redisPassword = config.getString("redis.password");
        String redisPrefix = config.getString("redis.cluster");
        RedisConnection redisConnection = new RedisConnection(redisHost, redisPort, redisPassword, redisPrefix);

        injector = Guice.createInjector(
                new SmoothSyncModule(this),
                new ConfigurationModule(config, messages),
                new ConnectionModule(mongoConnection, redisConnection)
        );
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static Injector getInjector() {
        return injector;
    }
}
