package com.smoothresources.smoothsync.module;

import com.google.inject.AbstractModule;
import com.smoothresources.smoothsync.SmoothSync;
import com.smoothresources.smoothusersapi.SmoothUsersAPI;

public class SmoothSyncModule extends AbstractModule {

    private final SmoothSync plugin;
    private final SmoothUsersAPI smoothUsersAPI;

    public SmoothSyncModule(SmoothSync plugin, SmoothUsersAPI smoothUsersAPI) {
        this.plugin = plugin;
        this.smoothUsersAPI = smoothUsersAPI;
    }

    @Override
    protected void configure() {
        bind(SmoothSync.class).toInstance(plugin);
        bind(SmoothUsersAPI.class).toInstance(smoothUsersAPI);
    }
}
