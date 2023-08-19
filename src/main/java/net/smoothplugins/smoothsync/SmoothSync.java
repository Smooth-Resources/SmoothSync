package net.smoothplugins.smoothsync;

import com.google.inject.Guice;
import com.google.inject.Injector;
import net.smoothplugins.smoothbase.configuration.Configuration;
import net.smoothplugins.smoothsync.module.ConfigurationModule;
import net.smoothplugins.smoothsync.module.SmoothSyncModule;
import org.bukkit.plugin.java.JavaPlugin;

public final class SmoothSync extends JavaPlugin {

    private static Injector injector;

    @Override
    public void onEnable() {
        // Plugin startup logic
        Configuration config = new Configuration(this, "config");
        Configuration messages = new Configuration(this, "messages");

        injector = Guice.createInjector(
                new SmoothSyncModule(this),
                new ConfigurationModule(config, messages)
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
