package net.smoothplugins.smoothsync.messenger.message;

import java.util.UUID;

public class RequestUpdatedUserMessage extends DefaultMessage {

    private final UUID userUUID;

    public RequestUpdatedUserMessage(UUID userUUID) {
        super(DefaultMessageType.REQUEST_UPDATED_USER);
        this.userUUID = userUUID;
    }

    public UUID getUserUUID() {
        return userUUID;
    }
}
