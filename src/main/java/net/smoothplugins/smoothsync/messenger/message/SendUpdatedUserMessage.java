package net.smoothplugins.smoothsync.messenger.message;

import net.smoothplugins.smoothsyncapi.user.User;

public class SendUpdatedUserMessage extends DefaultMessage {

    private final User user;

    public SendUpdatedUserMessage(User user) {
        super(DefaultMessageType.SEND_UPDATED_USER);
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
