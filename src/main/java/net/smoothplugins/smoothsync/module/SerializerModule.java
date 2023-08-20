package net.smoothplugins.smoothsync.module;

import com.google.inject.AbstractModule;
import net.smoothplugins.smoothbase.serializer.Serializer;

public class SerializerModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Serializer.class).toInstance(new Serializer(null));
    }
}
