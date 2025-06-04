package net.smoothplugins.smoothsync.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import net.smoothplugins.smoothbase.common.file.YAMLFile;
import net.smoothplugins.smoothsync.SmoothSync;
import net.smoothplugins.smoothsync.command.EnderSeeCommand;
import net.smoothplugins.smoothsync.command.InvSeeCommand;
import net.smoothplugins.smoothsyncapi.user.UserService;

public class CommandModule extends AbstractModule {

    @Provides
    public InvSeeCommand provideInvSeeCommand(@Named("messages") YAMLFile messages, UserService userService, SmoothSync plugin) {
        return new InvSeeCommand("invsee", messages, userService, plugin);
    }

    @Provides
    public EnderSeeCommand provideEnderSeeCommand(@Named("messages") YAMLFile messages, UserService userService, SmoothSync plugin) {
        return new EnderSeeCommand("endersee", messages, userService, plugin);
    }
}
