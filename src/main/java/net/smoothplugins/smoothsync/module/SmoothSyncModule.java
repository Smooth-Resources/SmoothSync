package net.smoothplugins.smoothsync.module;

import com.google.inject.AbstractModule;
import net.smoothplugins.smoothsync.SmoothSync;

public class SmoothSyncModule extends AbstractModule {

    private final SmoothSync plugin;

    public SmoothSyncModule(SmoothSync plugin) {
        this.plugin = plugin;
    }

    @Override
    protected void configure() {
        bind(SmoothSync.class).toInstance(plugin);
    }
}
