package net.smoothplugins.smoothsync.listener;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import net.smoothplugins.smoothbase.configuration.Configuration;
import net.smoothplugins.smoothsync.user.UserSaver;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class EntityDamageListener implements Listener {

    @Inject
    private UserSaver userSaver;
    @Inject @Named("config")
    private Configuration config;

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!config.getBoolean("synchronization.prevent-damage")) return;

        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (userSaver.containsPlayer(player)) {
            return;
        }

        event.setCancelled(true);
    }
}
