package com.smoothresources.smoothsync.loader;

import com.google.inject.Inject;
import com.smoothresources.smoothsync.listener.*;
import com.smoothresources.smoothbase.paper.menu.manager.MenuManager;
import com.smoothresources.smoothsync.SmoothSync;
import com.smoothresources.smoothsync.listener.*;
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
        new MenuManager(plugin).registerListeners();
    }
}
