package net.smoothplugins.smoothsync.loader;

import com.google.inject.Inject;
import net.smoothplugins.smoothsync.SmoothSync;
import net.smoothplugins.smoothsync.command.SmoothSyncCommand;

public class CommandLoader {

    @Inject
    private SmoothSync plugin;
    @Inject
    private SmoothSyncCommand smoothSyncCommand;

    public void load() {
        plugin.getCommand("smoothsync").setExecutor(smoothSyncCommand);
    }
}
