package net.smoothplugins.smoothsync.message.user.quit;

import java.util.UUID;

public class QuitNotificationMessage {

    private final UUID userUUID;

    public QuitNotificationMessage(UUID userUUID) {
        this.userUUID = userUUID;
    }

    public UUID getUserUUID() {
        return userUUID;
    }
}

