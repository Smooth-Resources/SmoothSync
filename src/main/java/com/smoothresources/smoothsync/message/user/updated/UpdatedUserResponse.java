package com.smoothresources.smoothsync.message.user.updated;

import com.smoothresources.smoothsyncapi.user.User;

public class UpdatedUserResponse {

    private final User user;

    public UpdatedUserResponse(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
