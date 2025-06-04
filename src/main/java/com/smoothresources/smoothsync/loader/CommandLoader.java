package com.smoothresources.smoothsync.loader;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.smoothresources.smoothbase.common.file.YAMLFile;
import com.smoothresources.smoothsync.SmoothSync;
import com.smoothresources.smoothsync.command.EnderSeeCommand;
import com.smoothresources.smoothsync.command.InvSeeCommand;
import com.smoothresources.smoothsync.command.SmoothSyncCommand;
import org.bukkit.Bukkit;

public class CommandLoader {

    @Inject
    private SmoothSync plugin;
    @Inject @Named("config")
    private YAMLFile config;
    @Inject
    private SmoothSyncCommand smoothSyncCommand;
    @Inject
    private InvSeeCommand invSeeCommand;
    @Inject
    private EnderSeeCommand enderSeeCommand;

    public void load() {
        plugin.getCommand("smoothsync").setExecutor(smoothSyncCommand);

        if (config.getBoolean("invsee", "enabled")) {
            Bukkit.getCommandMap().register("invsee", "smoothsync", invSeeCommand);
        }

        if (config.getBoolean("endersee", "enabled")) {
            Bukkit.getCommandMap().register("endersee", "smoothsync", enderSeeCommand);
        }
    }
}
