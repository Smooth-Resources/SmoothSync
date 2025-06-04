package net.smoothplugins.smoothsync.message.user.apply;

import net.smoothplugins.smoothsyncapi.user.User;

public class ApplyUserDataMessage {

    private final User user;

    public ApplyUserDataMessage(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
