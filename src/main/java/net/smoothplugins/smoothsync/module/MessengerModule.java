package net.smoothplugins.smoothsync.module;

import com.google.inject.AbstractModule;
import net.smoothplugins.smoothbase.common.messenger.Messenger;

public class MessengerModule extends AbstractModule {

    private final Messenger messenger;

    public MessengerModule(Messenger messenger) {
        this.messenger = messenger;
    }

    @Override
    protected void configure() {
        bind(Messenger.class).toInstance(messenger);
    }
}
