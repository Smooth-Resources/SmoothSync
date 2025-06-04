package com.smoothresources.smoothsync.module;

import com.google.inject.AbstractModule;
import com.smoothresources.smoothbase.common.serializer.Serializer;

public class SerializerModule extends AbstractModule {

    private final Serializer serializer;

    public SerializerModule(Serializer serializer) {
        this.serializer = serializer;
    }

    @Override
    protected void configure() {
        bind(Serializer.class).toInstance(serializer);
    }
}
