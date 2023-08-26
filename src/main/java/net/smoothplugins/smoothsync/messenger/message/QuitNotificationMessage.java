package net.smoothplugins.smoothsync.messenger.message;

import java.util.UUID;

public class QuitNotificationMessage extends DefaultMessage {

    private final UUID userUUID;

    public QuitNotificationMessage(UUID userUUID) {
        super(DefaultMessageType.QUIT_NOTIFICATION);
        this.userUUID = userUUID;
    }

    public UUID getUserUUID() {
        return userUUID;
    }
}
