package net.smoothplugins.smoothsync.loader;

import com.google.inject.Inject;
import net.smoothplugins.smoothsync.user.UserSaver;
import net.smoothplugins.smoothsyncapi.event.AsyncDataUpdateEvent;
import net.smoothplugins.smoothsyncapi.service.Destination;
import net.smoothplugins.smoothsyncapi.user.User;
import net.smoothplugins.smoothsyncapi.user.UserService;
import net.smoothplugins.smoothsyncapi.user.UserTranslator;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class MainLoader {

    @Inject
    private ListenerLoader listenerLoader;
    @Inject
    private CommandLoader commandLoader;
    @Inject
    private UserService userService;
    @Inject
    private UserTranslator userTranslator;
    @Inject
    private UserSaver userSaver;

    public void load() {
        listenerLoader.load();
        commandLoader.load();
        userSaver.init();
    }

    public void unload() {
        userSaver.stop();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!userSaver.containsPlayer(player)) return;

            User user = userService.getUserByUUID(player.getUniqueId()).orElse(null);
            if (user == null) return;

            Set<Destination> destinations = new HashSet<>();
            destinations.add(Destination.CACHE);
            destinations.add(Destination.STORAGE);
            AsyncDataUpdateEvent dataUpdateEvent = new AsyncDataUpdateEvent(player, false, user, AsyncDataUpdateEvent.Cause.STOP, destinations);
            Bukkit.getPluginManager().callEvent(dataUpdateEvent);

            userTranslator.translateToUser(user, player);
            userService.update(user, destinations.toArray(new Destination[0]));
            userService.setTTLOfCacheByUUID(user.getUuid(), 600); // 10 minutes
        }
    }
}
