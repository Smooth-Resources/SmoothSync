package com.smoothresources.smoothsync.module;

import com.google.inject.AbstractModule;
import com.smoothresources.smoothsync.user.DefaultUserService;
import com.smoothresources.smoothsync.user.DefaultUserTranslator;
import com.smoothresources.smoothsync.user.UserSaver;
import com.smoothresources.smoothsyncapi.user.UserService;
import com.smoothresources.smoothsyncapi.user.UserTranslator;

public class UserModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(UserService.class).to(DefaultUserService.class);
        bind(UserTranslator.class).to(DefaultUserTranslator.class);
        bind(UserSaver.class).asEagerSingleton();
    }
}
