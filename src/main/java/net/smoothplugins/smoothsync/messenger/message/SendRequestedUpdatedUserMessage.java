package net.smoothplugins.smoothsync.messenger.message;

import net.smoothplugins.smoothsyncapi.user.User;

public class SendRequestedUpdatedUserMessage extends DefaultMessage {

    private final User user;

    public SendRequestedUpdatedUserMessage(User user) {
        super(DefaultMessageType.REQUESTED_UPDATED_USER);
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
