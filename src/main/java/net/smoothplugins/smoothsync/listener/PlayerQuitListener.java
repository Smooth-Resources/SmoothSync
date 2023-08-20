package net.smoothplugins.smoothsync.listener;

import com.google.inject.Inject;
import net.smoothplugins.smoothsync.SmoothSync;
import net.smoothplugins.smoothsyncapi.service.Destination;
import net.smoothplugins.smoothsyncapi.user.User;
import net.smoothplugins.smoothsyncapi.user.UserService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.time.LocalDateTime;

public class PlayerQuitListener implements Listener {

    @Inject
    private UserService userService;
    @Inject
    private SmoothSync plugin;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent event) {
        System.out.println("quit: " + LocalDateTime.now());
        Player player = event.getPlayer();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            User user = userService.getUserByUUID(player.getUniqueId()).orElse(null);
            if (user == null) return;

            user.setItems(player.getInventory().getContents());
            userService.update(user, Destination.CACHE, Destination.STORAGE);
            userService.setTTLOfCacheByUUID(user.getUuid(), 600); // 10 minutes
        });
    }
}
