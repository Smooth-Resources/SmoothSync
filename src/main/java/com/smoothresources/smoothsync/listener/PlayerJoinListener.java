package com.smoothresources.smoothsync.listener;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.smoothresources.smoothbase.common.file.YAMLFile;
import com.smoothresources.smoothsync.SmoothSync;
import com.smoothresources.smoothsync.user.UserSaver;
import com.smoothresources.smoothsyncapi.event.DataCleanEvent;
import com.smoothresources.smoothsyncapi.user.User;
import com.smoothresources.smoothsyncapi.user.UserService;
import com.smoothresources.smoothsyncapi.user.UserTranslator;
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
    private YAMLFile config;
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
            player.getInventory().clear();
            player.getEnderChest().clear();
            player.setExp(0);
            player.setLevel(0);
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (!userService.cacheContainsByUUID(player.getUniqueId()) || userService.hasTTLOfCacheByUUID(player.getUniqueId())) {
                User user = userService.getUserByUUID(player.getUniqueId()).orElseGet(() -> {
                    User newUser = new User(player.getUniqueId());
                    userTranslator.translateToUser(newUser, player);

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

                UserSaver.applyData(user, player);
                return;
            }

            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                if (!player.isOnline() || userSaver.containsPlayer(player)) return;

                User user = userService.getUserByUUID(player.getUniqueId()).orElse(null);
                if (user == null) return;

                if (!userService.removeTTLFromCacheByUUID(user.getUuid())) {
                    userService.loadToCache(user);
                }

                UserSaver.applyData(user, player);
            }, (int) ((config.getInt("synchronization", "timeouts", "join") / 1000F) * 20L));
        });
    }
}
