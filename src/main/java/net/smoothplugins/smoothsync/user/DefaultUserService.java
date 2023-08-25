package net.smoothplugins.smoothsync.user;

import com.google.inject.Inject;
import com.google.inject.name.Named;
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
        if (cacheContainsByUUID(uuid)) {
            if (redisStorage.hasTTL(uuid.toString())) {
                return getUserByUUID(uuid);
            } else {
                TestResponse testResponse = new TestResponse(); // TODO: Cambiar este objeto.

                RequestUpdatedUserMessage message = new RequestUpdatedUserMessage(uuid);
                messenger.sendRequest(serializer.serialize(message), new Response() {
                    @Override
                    public void onSuccess(String channel, String JSON) {
                        // Return user
                        SendUpdatedUserMessage sendUpdatedUserMessage = serializer.deserialize(JSON, SendUpdatedUserMessage.class);
                        testResponse.setUser(sendUpdatedUserMessage.getUser());
                    }

                    @Override
                    public void onFail(String channel) {
                        // Return user from storage or cache
                        testResponse.setUser(getUserByUUID(uuid).orElse(null));
                    }
                }, 3000L); // TODO: Hacer configurable el timeout

                while (!testResponse.isExecuted()) {
                    Thread.sleep(100L); // TODO: Mejorar esto, añadir timeout o maxtries.
                }

                return Optional.ofNullable(testResponse.getUser());
            }
        } else {
            return Optional.ofNullable(serializer.deserialize(mongoStorage.get("_id", uuid.toString()), User.class));
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

    @Nullable
    private UUID getUUIDByUsername(String username) {
        net.smoothplugins.smoothusersapi.user.User user = smoothUsersAPI.getUserService().getUserByUsername(username).orElse(null);
        if (user == null) return null;

        return user.getUuid();
    }

    private class TestResponse {
        private boolean executed;
        private User user;

        public boolean isExecuted() {
            return executed;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
            this.executed = true;
        }
    }
}
