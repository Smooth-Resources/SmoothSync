package net.smoothplugins.smoothsync.listener;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import net.smoothplugins.smoothbase.configuration.Configuration;
import net.smoothplugins.smoothsync.SmoothSync;
import net.smoothplugins.smoothsyncapi.user.User;
import net.smoothplugins.smoothsyncapi.user.UserService;
import net.smoothplugins.smoothsyncapi.user.UserTranslator;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    @Inject
    private UserService userService;
    @Inject
    private SmoothSync plugin;
    @Inject @Named("config")
    private Configuration config;
    @Inject
    private UserTranslator userTranslator;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!userService.containsByUUID(player.getUniqueId())) {
            User user = new User(player.getUniqueId());
            userTranslator.translateToUser(user, player);
            userService.create(user);
            userService.loadToCache(user);
        }

        // TODO: Hacer que al guardar cada X tiempo no se guarde justo ahora. Limpiar más cosas del user.
        player.getInventory().clear();

        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            User user = userService.getUserByUUID(player.getUniqueId()).orElseGet(() -> {
                // This code never runs, but it's here just in case.
                User newUser = new User(player.getUniqueId());
                userTranslator.translateToUser(newUser, player);
                userService.create(newUser);
                return newUser;
            });

            if (!userService.removeTTLFromCacheByUUID(user.getUuid())) {
                userService.loadToCache(user);
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                userTranslator.translateToPlayer(user, player);
            });

        },  (int) ((config.getInt("synchronization.join-delay") / 1000F) * 20));
    }
}
