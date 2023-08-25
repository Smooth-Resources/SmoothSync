package net.smoothplugins.smoothsync.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import net.smoothplugins.smoothbase.connection.RedisConnection;
import net.smoothplugins.smoothbase.messenger.MessageConsumer;
import net.smoothplugins.smoothbase.messenger.Messenger;
import net.smoothplugins.smoothbase.serializer.Serializer;
import net.smoothplugins.smoothsync.SmoothSync;
import net.smoothplugins.smoothsync.messenger.DefaultMessageConsumer;
import net.smoothplugins.smoothsync.messenger.DefaultRedisMessenger;

public class MessengerModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(MessageConsumer.class).to(DefaultMessageConsumer.class);
    }

    @Provides @Singleton
    public Messenger provideMessenger(SmoothSync plugin, RedisConnection connection, MessageConsumer consumer, Serializer serializer) {
        return new DefaultRedisMessenger(plugin, connection, consumer, serializer);
    }
}
