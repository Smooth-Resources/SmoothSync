package net.smoothplugins.smoothsync.listener;

import com.google.inject.Inject;
import net.smoothplugins.smoothsync.SmoothSync;
import net.smoothplugins.smoothsync.user.UserSaver;
import net.smoothplugins.smoothsyncapi.event.DataUpdateEvent;
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

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (!userSaver.containsPlayer(player)) return;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            User user = userService.getUserByUUID(player.getUniqueId()).orElse(null);
            if (user == null) return;

            Set<Destination> destinations = new HashSet<>();
            destinations.add(Destination.CACHE);
            destinations.add(Destination.STORAGE);
            DataUpdateEvent dataUpdateEvent = new DataUpdateEvent(player, user, DataUpdateEvent.Cause.LEAVE, destinations);
            Bukkit.getPluginManager().callEvent(dataUpdateEvent);

            userTranslator.translateToUser(user, player);
            userService.update(user, destinations.toArray(new Destination[0]));
            userService.setTTLOfCacheByUUID(user.getUuid(), 600); // 10 minutes
        });
    }
}
