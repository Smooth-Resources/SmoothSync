package com.smoothresources.smoothsync.listener;

import com.google.inject.Inject;
import com.smoothresources.smoothbase.common.messenger.Message;
import com.smoothresources.smoothbase.common.messenger.Messenger;
import com.smoothresources.smoothbase.common.serializer.Serializer;
import com.smoothresources.smoothsync.SmoothSync;
import com.smoothresources.smoothsync.message.user.quit.QuitNotificationMessage;
import com.smoothresources.smoothsync.user.UserSaver;
import com.smoothresources.smoothsyncapi.event.AsyncDataUpdateEvent;
import com.smoothresources.smoothsyncapi.service.Destination;
import com.smoothresources.smoothsyncapi.user.User;
import com.smoothresources.smoothsyncapi.user.UserService;
import com.smoothresources.smoothsyncapi.user.UserTranslator;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashSet;
import java.util.Set;

public class PlayerQuitListener implements Listener {

    @Inject
    private UserService userService;
    @Inject
    private SmoothSync plugin;
    @Inject
    private UserTranslator userTranslator;
    @Inject
    private UserSaver userSaver;
    @Inject
    private Messenger messenger;
    @Inject
    private Serializer serializer;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (!userSaver.containsPlayer(player)) return;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            User user = userService.getUserByUUID(player.getUniqueId()).orElse(null);
            if (user == null) return;

            Set<Destination> destinations = new HashSet<>();
            destinations.add(Destination.CACHE);
            destinations.add(Destination.STORAGE);
            AsyncDataUpdateEvent dataUpdateEvent = new AsyncDataUpdateEvent(player, true, user, AsyncDataUpdateEvent.Cause.LEAVE, destinations);
            Bukkit.getPluginManager().callEvent(dataUpdateEvent);

            userTranslator.translateToUser(user, player);
            userService.update(user, destinations.toArray(new Destination[0]));
            userService.setTTLOfCacheByUUID(user.getUuid(), 600); // 10 minutes

            QuitNotificationMessage message = new QuitNotificationMessage(user.getUuid());
            Message messengerMessage = new Message(QuitNotificationMessage.class, serializer.serialize(message));
            messenger.send(messengerMessage);
        });
    }
}
