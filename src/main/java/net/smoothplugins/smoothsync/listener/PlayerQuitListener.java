package net.smoothplugins.smoothsync.listener;

import com.google.inject.Inject;
import net.smoothplugins.smoothbase.messenger.Messenger;
import net.smoothplugins.smoothbase.serializer.Serializer;
import net.smoothplugins.smoothsync.SmoothSync;
import net.smoothplugins.smoothsync.messenger.message.QuitNotificationMessage;
import net.smoothplugins.smoothsync.user.UserSaver;
import net.smoothplugins.smoothsyncapi.event.AsyncDataUpdateEvent;
import net.smoothplugins.smoothsyncapi.service.Destination;
import net.smoothplugins.smoothsyncapi.user.User;
import net.smoothplugins.smoothsyncapi.user.UserService;
import net.smoothplugins.smoothsyncapi.user.UserTranslator;
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
        if (!userSaver.containsPlayer(player)) return; // Player data has not been loaded yet, so we don't need to save it.

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

            // The player could have changed to another server, so we need to send a message to notify that the user data has been updated.
            // If the player has changed to another server, when the server receives the message, it will request the data and the data will be applied to the user.
            QuitNotificationMessage message = new QuitNotificationMessage(user.getUuid());
            messenger.send(serializer.serialize(message));
        });
    }
}
