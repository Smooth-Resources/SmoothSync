package net.smoothplugins.smoothsync.loader;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import net.smoothplugins.smoothbase.configuration.Configuration;
import net.smoothplugins.smoothsync.SmoothSync;
import net.smoothplugins.smoothsync.command.EnderSeeCommand;
import net.smoothplugins.smoothsync.command.InvSeeCommand;
import net.smoothplugins.smoothsync.command.SmoothSyncCommand;
import org.bukkit.Bukkit;

public class CommandLoader {

    @Inject
    private SmoothSync plugin;
    @Inject @Named("config")
    private Configuration config;
    @Inject
    private SmoothSyncCommand smoothSyncCommand;
    @Inject
    private InvSeeCommand invSeeCommand;
    @Inject
    private EnderSeeCommand enderSeeCommand;

    public void load() {
        plugin.getCommand("smoothsync").setExecutor(smoothSyncCommand);

        if (config.getBoolean("invsee.enabled")) {
            Bukkit.getCommandMap().register("invsee", "smoothsync", invSeeCommand);
        }

        if (config.getBoolean("endersee.enabled")) {
            Bukkit.getCommandMap().register("endersee", "smoothsync", enderSeeCommand);
        }
    }
}
