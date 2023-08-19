package net.smoothplugins.smoothsync;

import com.google.inject.Guice;
import com.google.inject.Injector;
import net.smoothplugins.smoothsync.module.SmoothSyncModule;
import org.bukkit.plugin.java.JavaPlugin;

public final class SmoothSync extends JavaPlugin {

    private static Injector injector;

    @Override
    public void onEnable() {
        // Plugin startup logic
        injector = Guice.createInjector(
                new SmoothSyncModule(this)
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
