package net.smoothplugins.smoothsync.messenger.message;

import net.smoothplugins.smoothsyncapi.user.User;

public class UpdatedUserMessage extends DefaultMessage {

    private final User user;

    public UpdatedUserMessage(User user) {
        super(DefaultMessageType.REQUEST_UPDATED_USER);
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
