package net.smoothplugins.smoothsync.loader;

import com.google.inject.Inject;

public class MainLoader {

    @Inject
    private ListenerLoader listenerLoader;
    @Inject
    private CommandLoader commandLoader;

    public void load() {
        listenerLoader.load();
        commandLoader.load();
    }

    public void unload() {

    }
}
