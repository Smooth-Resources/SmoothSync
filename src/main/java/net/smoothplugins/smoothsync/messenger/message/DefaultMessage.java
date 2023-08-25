package net.smoothplugins.smoothsync.messenger.message;

public class DefaultMessage {

    private final DefaultMessageType type;

    public DefaultMessage(DefaultMessageType type) {
        this.type = type;
    }

    public DefaultMessageType getType() {
        return type;
    }
}
