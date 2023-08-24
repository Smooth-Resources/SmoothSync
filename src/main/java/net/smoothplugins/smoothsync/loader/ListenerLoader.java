package net.smoothplugins.smoothsync.loader;

import com.google.inject.Inject;
import net.smoothplugins.smoothsync.SmoothSync;
import net.smoothplugins.smoothsync.listener.*;
import org.bukkit.Bukkit;

public class ListenerLoader {

    @Inject
    private SmoothSync plugin;
    @Inject
    private PlayerJoinListener playerJoinListener;
    @Inject
    private PlayerQuitListener playerQuitListener;
    @Inject
    private PlayerDeathListener playerDeathListener;
    @Inject
    private EntityDamageListener entityDamageListener;
    @Inject
    private DataSyncListener dataSyncListener;

    public void load() {
        Bukkit.getPluginManager().registerEvents(playerJoinListener, plugin);
        Bukkit.getPluginManager().registerEvents(playerQuitListener, plugin);
        Bukkit.getPluginManager().registerEvents(playerDeathListener, plugin);
        Bukkit.getPluginManager().registerEvents(entityDamageListener, plugin);
        Bukkit.getPluginManager().registerEvents(dataSyncListener, plugin);
    }
}
