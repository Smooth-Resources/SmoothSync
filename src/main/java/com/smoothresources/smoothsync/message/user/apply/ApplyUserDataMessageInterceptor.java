package com.smoothresources.smoothsync.message.user.apply;

import com.google.inject.Inject;
import com.smoothresources.smoothbase.common.messenger.interceptor.Interceptor;
import com.smoothresources.smoothsync.user.UserSaver;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ApplyUserDataMessageInterceptor implements Interceptor {

    @Inject
    private UserSaver userSaver;

    @Override
    public void intercept(@NotNull Object object) {
        ApplyUserDataMessage message = (ApplyUserDataMessage) object;

        Player player = Bukkit.getPlayer(message.getUser().getUuid());
        if (player == null || !userSaver.containsPlayer(player)) return;

        UserSaver.applyData(message.getUser(), player);
    }
}
