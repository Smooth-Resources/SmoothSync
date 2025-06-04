package com.smoothresources.smoothsync.user;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.smoothresources.smoothbase.common.database.nosql.MongoDBDatabase;
import com.smoothresources.smoothbase.common.database.nosql.RedisDatabase;
import com.smoothresources.smoothbase.common.file.YAMLFile;
import com.smoothresources.smoothbase.common.messenger.Conversation;
import com.smoothresources.smoothbase.common.messenger.ConversationCallback;
import com.smoothresources.smoothbase.common.messenger.Message;
import com.smoothresources.smoothbase.common.messenger.Messenger;
import com.smoothresources.smoothbase.common.serializer.Serializer;
import com.smoothresources.smoothsync.message.user.apply.ApplyUserDataMessage;
import com.smoothresources.smoothsync.message.user.updated.UpdatedUserRequest;
import com.smoothresources.smoothsync.message.user.updated.UpdatedUserResponse;
import com.smoothresources.smoothsyncapi.service.Destination;
import com.smoothresources.smoothsyncapi.user.User;
import com.smoothresources.smoothsyncapi.user.UserService;
import com.smoothresources.smoothusersapi.SmoothUsersAPI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DefaultUserService implements UserService {

    @Inject @Named("user")
    private MongoDBDatabase mongoStorage;
    @Inject @Named("user")
    private RedisDatabase redisStorage;
    @Inject
    private Serializer serializer;
    @Inject
    private SmoothUsersAPI smoothUsersAPI;
    @Inject
    private Messenger messenger;
    @Inject @Named("config")
    private YAMLFile config;

    @Override
    public void create(User user) {
        mongoStorage.insert(user.getUuid().toString(), serializer.serialize(user));
    }

    @Override
    public void update(User user, Destination... destinations) {
        for (Destination destination : destinations) {
            switch (destination) {
                case STORAGE -> {
                    mongoStorage.update(user.getUuid().toString(), serializer.serialize(user));
                }

                case CACHE -> {
                    redisStorage.update(user.getUuid().toString(), serializer.serialize(user));
                }

                case CACHE_IF_PRESENT -> {
                    if (redisStorage.exists(user.getUuid().toString())) {
                        long ttl = redisStorage.getTTL(user.getUuid().toString());
                        redisStorage.update(user.getUuid().toString(), serializer.serialize(user));
                        redisStorage.setTTL(user.getUuid().toString(), (int) ttl);
                    }
                }

                case PLAYER_IF_ONLINE -> {
                    ApplyUserDataMessage message = new ApplyUserDataMessage(user);
                    Message messengerMessage = new Message(ApplyUserDataMessage.class, serializer.serialize(message));
                    messenger.send(messengerMessage);
                }
            }
        }
    }

    @Override
    public boolean containsByUUID(UUID uuid) {
        return redisStorage.exists(uuid.toString()) || mongoStorage.exists(uuid.toString());
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

        user = serializer.deserialize(mongoStorage.get(uuid.toString()), User.class);

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
            return Optional.ofNullable(serializer.deserialize(mongoStorage.get(uuid.toString()), User.class));
        }

        if (redisStorage.hasTTL(uuid.toString())) {
            // User is disconnected but it is in cache
            return getUserByUUID(uuid);
        }

        // User is online, so we try to get an updated version of it
        CompletableFuture<User> completableFuture = new CompletableFuture<>();
        UpdatedUserRequest request = new UpdatedUserRequest(uuid);
        ConversationCallback callback = new ConversationCallback() {
            @Override
            public void onSuccess(@NotNull Object object) {
                UpdatedUserResponse response = (UpdatedUserResponse) object;
                completableFuture.complete(response.getUser());
            }

            @Override
            public void onTimeout() {
                completableFuture.complete(getUserByUUID(uuid).orElse(null));
            }

            @Override
            public long getTimeout() {
                return config.getLong("synchronization", "timeouts", "updated-user-request");
            }
        };

        Conversation conversation = Conversation.ofRequest(UpdatedUserRequest.class, serializer.serialize(request), callback);
        messenger.send(conversation);

        try {
            return Optional.ofNullable(completableFuture.get());
        } catch (Exception ignored) {
            return Optional.empty();
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
                    mongoStorage.delete(uuid.toString());
                }

                case CACHE, CACHE_IF_PRESENT -> {
                    redisStorage.delete(uuid.toString());
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
        return redisStorage.exists(uuid.toString());
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
        com.smoothresources.smoothusersapi.user.User user = smoothUsersAPI.getUserService().getUserByUsername(username).orElse(null);
        if (user == null) return null;

        return user.getUuid();
    }
}
