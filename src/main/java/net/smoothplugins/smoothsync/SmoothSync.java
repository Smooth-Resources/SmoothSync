package net.smoothplugins.smoothsync;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.leangen.geantyref.TypeToken;
import net.smoothplugins.smoothbase.common.connection.RedisConnection;
import net.smoothplugins.smoothbase.common.database.nosql.MongoDBDatabase;
import net.smoothplugins.smoothbase.common.database.nosql.RedisDatabase;
import net.smoothplugins.smoothbase.common.file.YAMLFile;
import net.smoothplugins.smoothbase.common.messenger.Messenger;
import net.smoothplugins.smoothbase.common.messenger.interceptor.InterceptorManager;
import net.smoothplugins.smoothbase.common.messenger.redis.RedisMessenger;
import net.smoothplugins.smoothbase.common.serializer.Serializer;
import net.smoothplugins.smoothbase.common.task.TaskManager;
import net.smoothplugins.smoothbase.paper.file.PaperYAMLFile;
import net.smoothplugins.smoothbase.paper.serializer.PaperSerializer;
import net.smoothplugins.smoothbase.paper.task.PaperTaskManager;
import net.smoothplugins.smoothsync.api.DefaultSmoothSyncAPI;
import net.smoothplugins.smoothsync.loader.MainLoader;
import net.smoothplugins.smoothsync.message.user.apply.ApplyUserDataMessage;
import net.smoothplugins.smoothsync.message.user.apply.ApplyUserDataMessageInterceptor;
import net.smoothplugins.smoothsync.message.user.quit.QuitNotificationMessage;
import net.smoothplugins.smoothsync.message.user.quit.QuitNotificationMessageInterceptor;
import net.smoothplugins.smoothsync.message.user.updated.UpdatedUserConversationInterceptor;
import net.smoothplugins.smoothsync.message.user.updated.UpdatedUserRequest;
import net.smoothplugins.smoothsync.module.*;
import net.smoothplugins.smoothsync.serializer.adapter.AdvancementAdapter;
import net.smoothplugins.smoothsync.serializer.adapter.AdvancementCollectionAdapter;
import net.smoothplugins.smoothsync.serializer.adapter.PotionEffectAdapter;
import net.smoothplugins.smoothsyncapi.SmoothSyncAPI;
import net.smoothplugins.smoothsyncapi.user.User;
import net.smoothplugins.smoothusersapi.SmoothUsersAPI;
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
