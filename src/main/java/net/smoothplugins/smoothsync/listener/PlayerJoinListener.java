package net.smoothplugins.smoothsync.listener;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import net.smoothplugins.smoothbase.configuration.Configuration;
import net.smoothplugins.smoothsync.SmoothSync;
import net.smoothplugins.smoothsyncapi.user.User;
import net.smoothplugins.smoothsyncapi.user.UserService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.time.LocalDateTime;

public class PlayerJoinListener implements Listener {

    @Inject
    private UserService userService;
    @Inject
    private SmoothSync plugin;
    @Inject @Named("config")
    private Configuration config;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        System.out.println("join: " + LocalDateTime.now());
        Player player = event.getPlayer();

        // TODO: Hacer que al guardar cada X tiempo no se guarde justo ahora.
        player.getInventory().clear();

        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            System.out.println("join-sync: " + LocalDateTime.now());
            User user = userService.getUserByUUID(player.getUniqueId()).orElseGet(() -> {
                User newUser = new User(player.getUniqueId(), player.getInventory().getContents());
                userService.create(newUser);
                return newUser;
            });

            if (!userService.removeTTLFromCacheByUUID(user.getUuid())) {
                userService.loadToCache(user);
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                player.getInventory().setContents(user.getItems());
                player.sendMessage("Synced your inventory with the database.");
            });
        },  (int) ((config.getInt("synchronization.join-delay") / 1000F) * 20));
    }
}
