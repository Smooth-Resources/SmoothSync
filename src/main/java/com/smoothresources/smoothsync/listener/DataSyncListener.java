package com.smoothresources.smoothsync.listener;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import com.smoothresources.smoothbase.common.file.YAMLFile;
import com.smoothresources.smoothsyncapi.event.DataSyncEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class DataSyncListener implements Listener {

    @Inject @Named("messages")
    private YAMLFile messages;

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSync(DataSyncEvent event) {
        event.getPlayer().sendMessage(Component.text("Datos sincronizados").color(NamedTextColor.GREEN));
        /*
        Notification notification = Notification.of(messages, "event", "data-sync-event", null);
        notification.send(event.getPlayer());

         */
    }
}
