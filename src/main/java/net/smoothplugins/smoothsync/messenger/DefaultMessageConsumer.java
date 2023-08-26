package net.smoothplugins.smoothsync.messenger;

import com.google.inject.Inject;
import net.smoothplugins.smoothbase.messenger.MessageConsumer;
import net.smoothplugins.smoothbase.messenger.Messenger;
import net.smoothplugins.smoothbase.serializer.Serializer;
import net.smoothplugins.smoothsync.SmoothSync;
import net.smoothplugins.smoothsync.messenger.message.DefaultMessage;
import net.smoothplugins.smoothsync.messenger.message.QuitNotificationMessage;
import net.smoothplugins.smoothsync.messenger.message.RequestUpdatedUserMessage;
import net.smoothplugins.smoothsync.messenger.message.SendUpdatedUserMessage;
import net.smoothplugins.smoothsync.user.UserSaver;
import net.smoothplugins.smoothsyncapi.event.DataSyncEvent;
import net.smoothplugins.smoothsyncapi.user.User;
import net.smoothplugins.smoothsyncapi.user.UserService;
import net.smoothplugins.smoothsyncapi.user.UserTranslator;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class DefaultMessageConsumer implements MessageConsumer {

    @Inject
    private Serializer serializer;
    @Inject
    private Messenger messenger;
    @Inject
    private UserService userService;
    @Inject
    private UserTranslator userTranslator;
    @Inject
    private SmoothSync plugin;
    @Inject
    private UserSaver userSaver;

    @Override
    public void consume(String JSON, @Nullable UUID identifier) {
        DefaultMessage tempMessage = serializer.deserialize(JSON, DefaultMessage.class);
        switch (tempMessage.getType()) {
            case REQUEST_UPDATED_USER -> {
                RequestUpdatedUserMessage message = serializer.deserialize(JSON, RequestUpdatedUserMessage.class);
                consumeRequestUpdatedUserMessage(message, identifier);
            }

            case QUIT_NOTIFICATION -> {
                QuitNotificationMessage message = serializer.deserialize(JSON, QuitNotificationMessage.class);
                consumeQuitNotificationMessage(message);
            }
        }
    }

    private void consumeRequestUpdatedUserMessage(RequestUpdatedUserMessage message, UUID identifier) {
        Player player = Bukkit.getPlayer(message.getUserUUID());
        if (player == null) return;

        User user = userService.getUserByUUID(message.getUserUUID()).orElse(null);
        userTranslator.translateToUser(user, player);

        SendUpdatedUserMessage sendUpdatedUserMessage = new SendUpdatedUserMessage(user);
        messenger.sendResponse(serializer.serialize(sendUpdatedUserMessage), identifier);
    }

    private void consumeQuitNotificationMessage(QuitNotificationMessage message) {
        Player player = Bukkit.getPlayer(message.getUserUUID());
        if (player == null || userSaver.containsPlayer(player)) return;

        User user = userService.getUserByUUID(player.getUniqueId()).orElse(null);
        if (user == null) return;

        if (!userService.removeTTLFromCacheByUUID(user.getUuid())) {
            userService.loadToCache(user);
        }

        applyData(user, player);
    }

    private void applyData(User user, Player player) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            DataSyncEvent dataSyncEvent = new DataSyncEvent(player, user);
            Bukkit.getPluginManager().callEvent(dataSyncEvent);
            if (!dataSyncEvent.isCancelled()) {
                userTranslator.translateToPlayer(user, player);
                userSaver.addPlayer(player);
            }
        });
    }
}
