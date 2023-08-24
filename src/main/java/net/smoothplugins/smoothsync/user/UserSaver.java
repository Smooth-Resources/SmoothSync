package net.smoothplugins.smoothsync.user;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import net.smoothplugins.smoothbase.configuration.Configuration;
import net.smoothplugins.smoothsync.SmoothSync;
import net.smoothplugins.smoothsyncapi.event.AsyncDataUpdateEvent;
import net.smoothplugins.smoothsyncapi.event.DataUpdateEvent;
import net.smoothplugins.smoothsyncapi.service.Destination;
import net.smoothplugins.smoothsyncapi.user.User;
import net.smoothplugins.smoothsyncapi.user.UserService;
import net.smoothplugins.smoothsyncapi.user.UserTranslator;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserSaver {

    @Inject
    private UserService userService;
    @Inject
    private UserTranslator userTranslator;
    @Inject
    private SmoothSync plugin;
    @Inject @Named("config")
    private Configuration config;
    private BukkitTask task;

    private List<Player> players = new ArrayList<>();

    public void init() {
        int seconds = config.getInt("data-update.save-interval");
        int ticks = seconds * 20;
        task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::updatePlayers, ticks, ticks);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
        }
    }

    private void updatePlayers() {
        List<Player> offlinePlayers = new ArrayList<>();
        players.forEach(player -> {
            if (!player.isOnline()) {
                offlinePlayers.add(player);
                return;
            }

            User user = userService.getUserByUUID(player.getUniqueId()).orElse(null);
            if (user == null) return;

            userTranslator.translateToUser(user, player);

            Set<Destination> destinations = new HashSet<>();
            destinations.add(Destination.CACHE);
            if (!config.getBoolean("data-update.performance-mode")) {
                destinations.add(Destination.STORAGE);
            }

            AsyncDataUpdateEvent dataUpdateEvent = new AsyncDataUpdateEvent(player, true, user, AsyncDataUpdateEvent.Cause.INTERVAL, destinations);
            Bukkit.getPluginManager().callEvent(dataUpdateEvent);

            if (dataUpdateEvent.isCancelled()) return;

            if (config.getBoolean("data-update.performance-mode")) {
                userService.update(user, destinations.toArray(new Destination[0]));
            } else {
                userService.update(user, destinations.toArray(new Destination[0]));
            }
        });

        offlinePlayers.forEach(players::remove);
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public boolean containsPlayer(Player player) {
        return players.contains(player);
    }
}
