package net.smoothplugins.smoothsync.module;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import net.smoothplugins.smoothbase.configuration.Configuration;

public class ConfigurationModule extends AbstractModule {

    private final Configuration config;
    private final Configuration messages;
    private final Configuration invSeeMenu;

    public ConfigurationModule(Configuration config, Configuration messages, Configuration invSeeMenu) {
        this.config = config;
        this.messages = messages;
        this.invSeeMenu = invSeeMenu;
    }

    @Override
    protected void configure() {
        bind(Configuration.class).annotatedWith(Names.named("config")).toInstance(config);
        bind(Configuration.class).annotatedWith(Names.named("messages")).toInstance(messages);
        bind(Configuration.class).annotatedWith(Names.named("invsee-menu")).toInstance(invSeeMenu);
    }
}
