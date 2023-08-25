package net.smoothplugins.smoothsync.messenger;

import net.smoothplugins.smoothbase.connection.RedisConnection;
import net.smoothplugins.smoothbase.messenger.MessageConsumer;
import net.smoothplugins.smoothbase.messenger.redis.RedisMessenger;
import net.smoothplugins.smoothbase.serializer.Serializer;
import org.bukkit.plugin.Plugin;

public class DefaultRedisMessenger extends RedisMessenger {

    public DefaultRedisMessenger(Plugin plugin, RedisConnection connection, MessageConsumer consumer, Serializer serializer) {
        super(plugin, connection, consumer, serializer);
    }
}
