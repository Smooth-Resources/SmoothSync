package com.smoothresources.smoothsync.message.user.quit;

import com.google.inject.Inject;
import com.smoothresources.smoothbase.common.messenger.interceptor.Interceptor;
import com.smoothresources.smoothsync.user.UserSaver;
import com.smoothresources.smoothsyncapi.user.User;
import com.smoothresources.smoothsyncapi.user.UserService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class QuitNotificationMessageInterceptor implements Interceptor {

    @Inject
    private UserSaver userSaver;
    @Inject
    private UserService userService;

    @Override
    public void intercept(@NotNull Object object) {
        QuitNotificationMessage message = (QuitNotificationMessage) object;

        Player player = Bukkit.getPlayer(message.getUserUUID());
        if (player == null || userSaver.containsPlayer(player)) return;

        User user = userService.getUserByUUID(player.getUniqueId()).orElse(null);
        if (user == null) return;

        if (!userService.removeTTLFromCacheByUUID(user.getUuid())) {
            userService.loadToCache(user);
        }

        UserSaver.applyData(user, player);
    }
}
