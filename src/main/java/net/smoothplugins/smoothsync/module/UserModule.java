package net.smoothplugins.smoothsync.module;

import com.google.inject.AbstractModule;
import net.smoothplugins.smoothsync.user.DefaultUserService;
import net.smoothplugins.smoothsyncapi.user.UserService;

public class UserModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(UserService.class).to(DefaultUserService.class);
    }
}
