package com.smoothresources.smoothsync.module;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.smoothresources.smoothbase.common.file.YAMLFile;

public class ConfigurationModule extends AbstractModule {

    private final YAMLFile config;
    private final YAMLFile messages;

    public ConfigurationModule(YAMLFile config, YAMLFile messages) {
        this.config = config;
        this.messages = messages;
    }

    @Override
    protected void configure() {
        bind(YAMLFile.class).annotatedWith(Names.named("config")).toInstance(config);
        bind(YAMLFile.class).annotatedWith(Names.named("messages")).toInstance(messages);
    }
}
