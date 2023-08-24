package net.smoothplugins.smoothsync.module;

import com.google.inject.AbstractModule;
import net.smoothplugins.smoothsync.user.DefaultUserService;
import net.smoothplugins.smoothsync.user.DefaultUserTranslator;
import net.smoothplugins.smoothsync.user.UserSaver;
import net.smoothplugins.smoothsyncapi.user.UserService;
import net.smoothplugins.smoothsyncapi.user.UserTranslator;

public class UserModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(UserService.class).to(DefaultUserService.class);
        bind(UserTranslator.class).to(DefaultUserTranslator.class);
        bind(UserSaver.class).asEagerSingleton();
    }
}
