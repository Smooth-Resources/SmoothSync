package com.smoothresources.smoothsync.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.smoothresources.smoothbase.common.file.YAMLFile;
import com.smoothresources.smoothsync.SmoothSync;
import com.smoothresources.smoothsync.command.EnderSeeCommand;
import com.smoothresources.smoothsync.command.InvSeeCommand;
import com.smoothresources.smoothsyncapi.user.UserService;

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
