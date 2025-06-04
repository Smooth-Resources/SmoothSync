package com.smoothresources.smoothsync.api;

import com.google.inject.Inject;
import com.smoothresources.smoothsyncapi.SmoothSyncAPI;
import com.smoothresources.smoothsyncapi.user.UserService;
import com.smoothresources.smoothsyncapi.user.UserTranslator;

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
