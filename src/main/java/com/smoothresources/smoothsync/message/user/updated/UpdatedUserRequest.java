package com.smoothresources.smoothsync.message.user.updated;

import java.util.UUID;

public class UpdatedUserRequest {

    private final UUID userUUID;

    public UpdatedUserRequest(UUID userUUID) {
        this.userUUID = userUUID;
    }

    public UUID getUserUUID() {
        return userUUID;
    }
}
