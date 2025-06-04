package com.smoothresources.smoothsync.message.user.apply;

import com.smoothresources.smoothsyncapi.user.User;

public class ApplyUserDataMessage {

    private final User user;

    public ApplyUserDataMessage(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
