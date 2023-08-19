package net.smoothplugins.smoothsync.module;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import net.smoothplugins.smoothbase.configuration.Configuration;

public class ConfigurationModule extends AbstractModule {

    private final Configuration config;
    private final Configuration messages;

    public ConfigurationModule(Configuration config, Configuration messages) {
        this.config = config;
        this.messages = messages;
    }

    @Override
    protected void configure() {
        bind(Configuration.class).annotatedWith(Names.named("config")).toInstance(config);
        bind(Configuration.class).annotatedWith(Names.named("messages")).toInstance(messages);
    }
}
