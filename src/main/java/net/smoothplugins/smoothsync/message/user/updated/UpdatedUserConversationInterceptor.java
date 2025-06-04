package net.smoothplugins.smoothsync.message.user.updated;

import com.google.inject.Inject;
import net.smoothplugins.smoothbase.common.messenger.Conversation;
import net.smoothplugins.smoothbase.common.messenger.Messenger;
import net.smoothplugins.smoothbase.common.messenger.interceptor.ConversationInterceptor;
import net.smoothplugins.smoothbase.common.serializer.Serializer;
import net.smoothplugins.smoothsyncapi.user.User;
import net.smoothplugins.smoothsyncapi.user.UserService;
import net.smoothplugins.smoothsyncapi.user.UserTranslator;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class UpdatedUserConversationInterceptor implements ConversationInterceptor {

    @Inject
    private Messenger messenger;
    @Inject
    private Serializer serializer;
    @Inject
    private UserService userService;
    @Inject
    private UserTranslator userTranslator;

    @Override
    public void intercept(@NotNull Object object, @NotNull UUID conversationId) {
        System.out.println("Received updated user data request");
        UpdatedUserRequest request = (UpdatedUserRequest) object;

        Player player = Bukkit.getPlayer(request.getUserUUID());
        if (player == null) return;

        System.out.println("1");
        User user = userService.getUserByUUID(request.getUserUUID()).orElse(null);
        userTranslator.translateToUser(user, player);

        System.out.println("2");
        UpdatedUserResponse response = new UpdatedUserResponse(user);
        Conversation conversation = Conversation.ofResponse(UpdatedUserResponse.class, serializer.serialize(response), conversationId);
        System.out.println("3");
        messenger.send(conversation);
        System.out.println("4");
    }

    @Override
    public void intercept(@NotNull Object object) {
        // Not needed
    }
}
