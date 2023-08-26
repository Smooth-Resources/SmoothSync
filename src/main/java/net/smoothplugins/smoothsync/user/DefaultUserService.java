package net.smoothplugins.smoothsync.user;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import net.smoothplugins.smoothbase.configuration.Configuration;
import net.smoothplugins.smoothbase.messenger.Messenger;
import net.smoothplugins.smoothbase.messenger.Response;
import net.smoothplugins.smoothbase.serializer.Serializer;
import net.smoothplugins.smoothbase.storage.MongoStorage;
import net.smoothplugins.smoothbase.storage.RedisStorage;
import net.smoothplugins.smoothsync.messenger.message.RequestUpdatedUserMessage;
import net.smoothplugins.smoothsync.messenger.message.SendUpdatedUserMessage;
import net.smoothplugins.smoothsyncapi.service.Destination;
import net.smoothplugins.smoothsyncapi.user.User;
import net.smoothplugins.smoothsyncapi.user.UserService;
import net.smoothplugins.smoothusersapi.SmoothUsersAPI;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DefaultUserService implements UserService {

    @Inject @Named("user")
    private MongoStorage mongoStorage;
    @Inject @Named("user")
    private RedisStorage redisStorage;
    @Inject
    private Serializer serializer;
    @Inject
    private SmoothUsersAPI smoothUsersAPI;
    @Inject
    private Messenger messenger;
    @Inject @Named("config")
    private Configuration config;

    @Override
    public void create(User user) {
        mongoStorage.create(serializer.serialize(user));
    }

    @Override
    public void update(User user, Destination... destinations) {
        for (Destination destination : destinations) {
            switch (destination) {
                case STORAGE -> {
                    mongoStorage.update( "_id", user.getUuid().toString(), serializer.serialize(user));
                }

                case CACHE -> {
                    redisStorage.update(user.getUuid().toString(), serializer.serialize(user));
                }

                case CACHE_IF_PRESENT -> {
                    if (redisStorage.contains(user.getUuid().toString())) {
                        redisStorage.update(user.getUuid().toString(), serializer.serialize(user));
                    }
                }
            }
        }
    }

    @Override
    public boolean containsByUUID(UUID uuid) {
        return redisStorage.contains(uuid.toString()) || mongoStorage.contains("_id", uuid.toString());
    }

    @Override
    public boolean containsByUsername(String username) {
        UUID uuid = getUUIDByUsername(username);
        if (uuid == null) return false;

        return containsByUUID(uuid);
    }

    @Override
    public Optional<User> getUserByUUID(UUID uuid) {
        User user = serializer.deserialize(redisStorage.get(uuid.toString()), User.class);
        if (user != null) return Optional.of(user);

        user = serializer.deserialize(mongoStorage.get("_id", uuid.toString()), User.class);

        return Optional.ofNullable(user);
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        UUID uuid = getUUIDByUsername(username);
        if (uuid == null) return Optional.empty();

        return getUserByUUID(uuid);
    }

    @Override
    public Optional<User> requestUpdatedUserByUUID(UUID uuid) throws InterruptedException {
        if (!cacheContainsByUUID(uuid)) {
            // User is disconnected and it is not in cache
            return Optional.ofNullable(serializer.deserialize(mongoStorage.get("_id", uuid.toString()), User.class));
        }

        if (redisStorage.hasTTL(uuid.toString())) {
            // User is disconnected but it is in cache
            return getUserByUUID(uuid);
        }

        // User is online, so we try to get an updated version of it
        CompletableFuture<User> completableFuture = new CompletableFuture<>();
        RequestUpdatedUserMessage requestUpdatedUserMessage = new RequestUpdatedUserMessage(uuid);
        messenger.sendRequest(serializer.serialize(requestUpdatedUserMessage), new Response() {
            @Override
            public void onSuccess(String channel, String JSON) {
                SendUpdatedUserMessage sendUpdatedUserMessage = serializer.deserialize(JSON, SendUpdatedUserMessage.class);
                completableFuture.complete(sendUpdatedUserMessage.getUser());
            }

            @Override
            public void onFail(String s) {
                completableFuture.complete(getUserByUUID(uuid).orElse(null));
            }
        }, config.getInt("updated-user-request.timeout"));

        try {
            return Optional.ofNullable(completableFuture.get());
        } catch (Exception ignored) {
            return null;
        }
    }

    @Override
    public Optional<User> requestUpdatedUserByUsername(String username) throws InterruptedException {
        UUID uuid = getUUIDByUsername(username);
        if (uuid == null) return Optional.empty();

        return requestUpdatedUserByUUID(uuid);
    }

    @Override
    public void deleteByUUID(UUID uuid, Destination... destinations) {
        for (Destination destination : destinations) {
            switch (destination) {
                case STORAGE -> {
                    mongoStorage.delete("_id", uuid.toString());
                }

                case CACHE -> {
                    redisStorage.delete(uuid.toString());
                }

                case CACHE_IF_PRESENT -> {
                    if (redisStorage.contains(uuid.toString())) {
                        redisStorage.delete(uuid.toString());
                    }
                }
            }
        }
    }

    @Override
    public void deleteByUsername(String username, Destination... destinations) {
        UUID uuid = getUUIDByUsername(username);
        if (uuid == null) return;

        deleteByUUID(uuid, destinations);
    }

    @Override
    public boolean cacheContainsByUUID(UUID uuid) {
        return redisStorage.contains(uuid.toString());
    }

    @Override
    public boolean cacheContainsByUsername(String username) {
        UUID uuid = getUUIDByUsername(username);
        if (uuid == null) return false;

        return cacheContainsByUUID(uuid);
    }

    @Override
    public void loadToCache(User user) {
        redisStorage.update(user.getUuid().toString(), serializer.serialize(user));
    }

    @Override
    public boolean removeTTLFromCacheByUUID(UUID uuid) {
        return redisStorage.removeTTL(uuid.toString());
    }

    @Override
    public boolean removeTTLFromCacheByUsername(String username) {
        UUID uuid = getUUIDByUsername(username);
        if (uuid == null) return false;

        return removeTTLFromCacheByUUID(uuid);
    }

    @Override
    public boolean setTTLOfCacheByUUID(UUID uuid, int seconds) {
        return redisStorage.setTTL(uuid.toString(), seconds);
    }

    @Override
    public boolean setTTLOfCacheByUsername(String username, int seconds) {
        UUID uuid = getUUIDByUsername(username);
        if (uuid == null) return false;

        return setTTLOfCacheByUUID(uuid, seconds);
    }

    @Override
    public boolean hasTTLOfCacheByUUID(UUID uuid) {
        return redisStorage.hasTTL(uuid.toString());
    }

    @Override
    public boolean hasTTLOfCacheByUsername(String username) {
        UUID uuid = getUUIDByUsername(username);
        if (uuid == null) return false;
        
        return hasTTLOfCacheByUUID(uuid);
    }

    @Nullable
    private UUID getUUIDByUsername(String username) {
        net.smoothplugins.smoothusersapi.user.User user = smoothUsersAPI.getUserService().getUserByUsername(username).orElse(null);
        if (user == null) return null;

        return user.getUuid();
    }
}
