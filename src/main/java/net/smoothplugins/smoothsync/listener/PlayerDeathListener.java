package net.smoothplugins.smoothsync.listener;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import net.smoothplugins.smoothbase.configuration.Configuration;
import net.smoothplugins.smoothsync.SmoothSync;
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
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PlayerDeathListener implements Listener {

    @Inject
    private SmoothSync plugin;
    @Inject
    private UserTranslator userTranslator;
    @Inject
    private UserService userService;
    @Inject @Named("config")
    private Configuration config;

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Player player = event.getEntity();
            User user = userService.getUserByUUID(player.getUniqueId()).orElse(null);
            if (user == null) return;

            userTranslator.translateToUser(user, player);

            Set<Destination> destinations = new HashSet<>();
            destinations.add(Destination.CACHE);
            if (!config.getBoolean("data-update.performance-mode")) {
                destinations.add(Destination.STORAGE);
            }

            DataUpdateEvent dataUpdateEvent = new DataUpdateEvent(player, user, DataUpdateEvent.Cause.DEATH, destinations);
            Bukkit.getPluginManager().callEvent(dataUpdateEvent);

            if (dataUpdateEvent.isCancelled()) return;

            if (config.getBoolean("data-update.performance-mode")) {
                userService.update(user, destinations.toArray(new Destination[0]));
            } else {
                userService.update(user, destinations.toArray(new Destination[0]));
            }
        });
    }
}
