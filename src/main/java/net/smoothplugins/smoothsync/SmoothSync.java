package net.smoothplugins.smoothsync;

import com.google.inject.Guice;
import com.google.inject.Injector;
import net.smoothplugins.smoothbase.configuration.Configuration;
import net.smoothplugins.smoothbase.connection.MongoConnection;
import net.smoothplugins.smoothbase.connection.RedisConnection;
import net.smoothplugins.smoothsync.loader.MainLoader;
import net.smoothplugins.smoothsync.module.*;
import net.smoothplugins.smoothusersapi.SmoothUsersAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
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
                new SmoothSyncModule(this, getSmoothUsersAPI()),
                new ConfigurationModule(config, messages),
                new ConnectionModule(mongoConnection, redisConnection),
                new StorageModule(),
                new UserModule(),
                new SerializerModule(),
                new MessengerModule()
        );

        injector.getInstance(MainLoader.class).load();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        injector.getInstance(MainLoader.class).unload();
    }

    public static Injector getInjector() {
        return injector;
    }

    private SmoothUsersAPI getSmoothUsersAPI() {
        if (Bukkit.getPluginManager().getPlugin("SmoothUsers") != null) {
            RegisteredServiceProvider<SmoothUsersAPI> rsp = Bukkit.getServicesManager().getRegistration(SmoothUsersAPI.class);
            return rsp.getProvider();
        }

        return null;
    }
}
