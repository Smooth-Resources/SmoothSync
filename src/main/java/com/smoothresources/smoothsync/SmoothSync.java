package com.smoothresources.smoothsync;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.smoothresources.smoothsync.module.*;
import io.leangen.geantyref.TypeToken;
import com.smoothresources.smoothbase.common.connection.RedisConnection;
import com.smoothresources.smoothbase.common.database.nosql.MongoDBDatabase;
import com.smoothresources.smoothbase.common.database.nosql.RedisDatabase;
import com.smoothresources.smoothbase.common.file.YAMLFile;
import com.smoothresources.smoothbase.common.messenger.Messenger;
import com.smoothresources.smoothbase.common.messenger.interceptor.InterceptorManager;
import com.smoothresources.smoothbase.common.messenger.redis.RedisMessenger;
import com.smoothresources.smoothbase.common.serializer.Serializer;
import com.smoothresources.smoothbase.common.task.TaskManager;
import com.smoothresources.smoothbase.paper.file.PaperYAMLFile;
import com.smoothresources.smoothbase.paper.serializer.PaperSerializer;
import com.smoothresources.smoothbase.paper.task.PaperTaskManager;
import com.smoothresources.smoothsync.api.DefaultSmoothSyncAPI;
import com.smoothresources.smoothsync.loader.MainLoader;
import com.smoothresources.smoothsync.message.user.apply.ApplyUserDataMessage;
import com.smoothresources.smoothsync.message.user.apply.ApplyUserDataMessageInterceptor;
import com.smoothresources.smoothsync.message.user.quit.QuitNotificationMessage;
import com.smoothresources.smoothsync.message.user.quit.QuitNotificationMessageInterceptor;
import com.smoothresources.smoothsync.message.user.updated.UpdatedUserConversationInterceptor;
import com.smoothresources.smoothsync.message.user.updated.UpdatedUserRequest;
import com.smoothresources.smoothsync.module.*;
import com.smoothresources.smoothsync.serializer.adapter.AdvancementAdapter;
import com.smoothresources.smoothsync.serializer.adapter.AdvancementCollectionAdapter;
import com.smoothresources.smoothsync.serializer.adapter.PotionEffectAdapter;
import com.smoothresources.smoothsyncapi.SmoothSyncAPI;
import com.smoothresources.smoothusersapi.SmoothUsersAPI;
import org.bukkit.Bukkit;
import org.bukkit.advancement.Advancement;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;

import java.util.Collection;
import java.util.HashMap;

public final class SmoothSync extends JavaPlugin {

    private static Injector injector;

    @Override
    public void onEnable() {
        // Plugin startup logic
        YAMLFile config = new PaperYAMLFile(this, "config");
        YAMLFile messages = new PaperYAMLFile(this, "messages");

        String redisHost = config.getString("redis", "host");
        String redisPassword = config.getString("redis", "password");
        int redisPort = config.getInt("redis", "port");
        RedisConnection redisConnection = new RedisConnection(redisHost, redisPassword, redisPort, getLogger());
        redisConnection.connect();

        String redisPrefix = config.getString("redis", "cluster");
        RedisDatabase redisDatabase = new RedisDatabase(redisConnection, redisPrefix + ":user:");

        String mongoUri = config.getString("mongo", "uri");
        String mongoDatabaseName = config.getString("mongo", "database");
        MongoDBDatabase mongoDBDatabase = new MongoDBDatabase(mongoUri, mongoDatabaseName, "users");
        mongoDBDatabase.connect();

        Serializer serializer = new PaperSerializer.Builder()
                .registerDefaultAdapters()
                .registerTypeHierarchyAdapter(Advancement.class, new AdvancementAdapter())
                .registerTypeAdapter(PotionEffect.class, new PotionEffectAdapter())
                .registerTypeAdapter(new TypeToken<HashMap<Advancement, Collection<String>>>(){}.getType(), new AdvancementCollectionAdapter())
                .build();

        TaskManager taskManager = new PaperTaskManager(this);
        InterceptorManager interceptorManager = new InterceptorManager();
        Messenger messenger = new RedisMessenger(taskManager, getLogger(), serializer, interceptorManager, redisConnection, redisPrefix + ":channel");

        injector = Guice.createInjector(
                new SmoothSyncModule(this, getSmoothUsersAPI()),
                new ConfigurationModule(config, messages),
                new StorageModule(redisDatabase, mongoDBDatabase),
                new UserModule(),
                new SerializerModule(serializer),
                new MessengerModule(messenger),
                new CommandModule()
        );

        ApplyUserDataMessageInterceptor applyUserDataMessageInterceptor = injector.getInstance(ApplyUserDataMessageInterceptor.class);
        QuitNotificationMessageInterceptor quitNotificationMessageInterceptor = injector.getInstance(QuitNotificationMessageInterceptor.class);
        UpdatedUserConversationInterceptor updatedUserConversationInterceptor = injector.getInstance(UpdatedUserConversationInterceptor.class);
        interceptorManager.registerInterceptor(ApplyUserDataMessage.class, applyUserDataMessageInterceptor);
        interceptorManager.registerInterceptor(UpdatedUserRequest.class, updatedUserConversationInterceptor);
        interceptorManager.registerInterceptor(QuitNotificationMessage.class, quitNotificationMessageInterceptor);

        injector.getInstance(MainLoader.class).load();

        getServer().getServicesManager().register(
                SmoothSyncAPI.class,
                injector.getInstance(DefaultSmoothSyncAPI.class),
                this,
                org.bukkit.plugin.ServicePriority.Normal
        );
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
