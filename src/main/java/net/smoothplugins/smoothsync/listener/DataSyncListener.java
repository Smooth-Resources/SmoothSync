package net.smoothplugins.smoothsync.listener;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import net.smoothplugins.smoothbase.configuration.Configuration;
import net.smoothplugins.smoothbase.notification.Notification;
import net.smoothplugins.smoothsyncapi.event.DataSyncEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class DataSyncListener implements Listener {

    @Inject @Named("messages")
    private Configuration messages;

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSync(DataSyncEvent event) {
        Notification notification = Notification.of(messages, "event.data-sync-event", null);
        notification.send(event.getPlayer());
    }
}
