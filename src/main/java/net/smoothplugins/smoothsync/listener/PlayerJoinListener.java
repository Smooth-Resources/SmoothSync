package net.smoothplugins.smoothsync.listener;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import net.smoothplugins.smoothbase.configuration.Configuration;
import net.smoothplugins.smoothsync.SmoothSync;
import net.smoothplugins.smoothsync.user.UserSaver;
import net.smoothplugins.smoothsyncapi.event.DataCleanEvent;
import net.smoothplugins.smoothsyncapi.event.DataSyncEvent;
import net.smoothplugins.smoothsyncapi.user.User;
import net.smoothplugins.smoothsyncapi.user.UserService;
import net.smoothplugins.smoothsyncapi.user.UserTranslator;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerJoinListener implements Listener {

    @Inject
    private UserService userService;
    @Inject
    private SmoothSync plugin;
    @Inject @Named("config")
    private Configuration config;
    @Inject
    private UserTranslator userTranslator;
    @Inject
    private UserSaver userSaver;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        ItemStack[] storageContentsBackup = player.getInventory().getStorageContents();
        ItemStack[] armorContentsBackup = player.getInventory().getArmorContents();
        ItemStack[] extraContentsBackup = player.getInventory().getExtraContents();
        ItemStack[] enderChestBackup = player.getEnderChest().getContents();
        float expBackup = player.getExp();
        int levelBackup = player.getLevel();

        DataCleanEvent dataCleanEvent = new DataCleanEvent(player);
        Bukkit.getPluginManager().callEvent(dataCleanEvent);

        if (!dataCleanEvent.isCancelled()) {
            // We clear sensible data to prevent exploits.
            player.getInventory().clear();
            player.getEnderChest().clear();
            player.setExp(0);
            player.setLevel(0);
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            // If the user is not in the cache or has TTL, means that the user doesn't come from another server with SmoothSync.
            // So, the currently stored data is valid, so we don't need to wait for an QuitNotificationMessage to apply the data.
            if (!userService.cacheContainsByUUID(player.getUniqueId()) || userService.hasTTLOfCacheByUUID(player.getUniqueId())) {
                User user = userService.getUserByUUID(player.getUniqueId()).orElseGet(() -> {
                    // This is a new player, so we need to create a new user.
                    User newUser = new User(player.getUniqueId());
                    userTranslator.translateToUser(newUser, player);

                    // We restore the data that we cleared before (maybe SmoothSync has been installed on a not new server).
                    newUser.setInventoryStorageContents(storageContentsBackup);
                    newUser.setInventoryArmorContents(armorContentsBackup);
                    newUser.setInventoryExtraContents(extraContentsBackup);
                    newUser.setEnderChestItems(enderChestBackup);
                    newUser.setExp(expBackup);
                    newUser.setLevel(levelBackup);

                    userService.create(newUser);
                    return newUser;
                });

                if (!userService.removeTTLFromCacheByUUID(user.getUuid())) {
                    userService.loadToCache(user);
                }

                applyData(user, player);
                return;
            }

            // The player is in the cache and doesn't have TTL, so we need to wait for the data to be updated.
            // This is because PlayerQuitListener is executed after PlayerJoinListener.
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                // If everything works fine, the message should have been received and the player should be in userSaver.
                if (!player.isOnline() || userSaver.containsPlayer(player)) return;

                // If the message has not been received, we need to load the data from the storage.
                // This should not happen, but it is a security measure (maybe the server has crashed).
                User user = userService.getUserByUUID(player.getUniqueId()).orElse(null);
                if (user == null) return;

                if (!userService.removeTTLFromCacheByUUID(user.getUuid())) {
                    userService.loadToCache(user);
                }

                applyData(user, player);
            }, (int) ((config.getInt("synchronization.timeouts.join") / 1000F) * 20L)); // By default, 2 seconds.
        });
    }

    private void applyData(User user, Player player) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            DataSyncEvent dataSyncEvent = new DataSyncEvent(player, user);
            Bukkit.getPluginManager().callEvent(dataSyncEvent);
            if (!dataSyncEvent.isCancelled()) {
                userTranslator.translateToPlayer(user, player);
                userSaver.addPlayer(player);
            }
        });
    }
}
