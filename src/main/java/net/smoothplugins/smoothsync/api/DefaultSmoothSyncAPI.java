package net.smoothplugins.smoothsync.api;

import com.google.inject.Inject;
import net.smoothplugins.smoothsyncapi.SmoothSyncAPI;
import net.smoothplugins.smoothsyncapi.user.UserService;
import net.smoothplugins.smoothsyncapi.user.UserTranslator;

public class DefaultSmoothSyncAPI implements SmoothSyncAPI {

    @Inject
    private UserService userService;
    @Inject
    private UserTranslator userTranslator;

    @Override
    public UserService getUserService() {
        return userService;
    }

    @Override
    public UserTranslator getUserTranslator() {
        return userTranslator;
    }
}
