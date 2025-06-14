package net.smoothplugins.smoothsync.user;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import net.smoothplugins.smoothbase.configuration.Configuration;
import net.smoothplugins.smoothsync.SmoothSync;
import net.smoothplugins.smoothsyncapi.user.User;
import net.smoothplugins.smoothsyncapi.user.UserTranslator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;

public class DefaultUserTranslator implements UserTranslator {

    @Inject @Named("config")
    private Configuration config;
    @Inject
    private SmoothSync plugin;

    @Override
    public void translateToUser(User user, Player player) {
        user.setInventoryStorageContents(player.getInventory().getStorageContents());
        user.setInventoryArmorContents(player.getInventory().getArmorContents());
        user.setInventoryExtraContents(player.getInventory().getExtraContents());
        user.setHeldItemSlot(player.getInventory().getHeldItemSlot());
        user.setEnderChestItems(player.getEnderChest().getContents());
        user.setGameMode(player.getGameMode());
        user.setExp(player.getExp());
        user.setLevel(player.getLevel());
        user.setPotionEffects(player.getActivePotionEffects());
        user.setMaxHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        user.setHealth(player.getHealth());
        user.setFoodLevel(player.getFoodLevel());
        user.setSaturation(player.getSaturation());
        user.setExhaustion(player.getExhaustion());
        user.setMaximumAir(player.getMaximumAir());
        user.setRemainingAir(player.getRemainingAir());
        user.setLocation(player.getLocation());

        HashMap<Advancement, Collection<String>> advancements = new HashMap<>();
        Bukkit.getServer().advancementIterator().forEachRemaining(advancement -> {
            AdvancementProgress progress = player.getAdvancementProgress(advancement);
            if (progress.getRemainingCriteria().size() == advancement.getCriteria().size()) return;

            advancements.put(advancement, progress.getAwardedCriteria());
        });
        user.setAdvancements(advancements);

        HashMap<Statistic, Integer> globalStatistics = new HashMap<>();
        HashMap<Statistic, HashMap<Material, Integer>> blockStatistics = new HashMap<>();
        HashMap<Statistic, HashMap<EntityType, Integer>> entityStatistics = new HashMap<>();
        for (Statistic statistic : Statistic.values()) {
            switch (statistic) {
                case DROP, PICKUP, MINE_BLOCK, USE_ITEM, BREAK_ITEM, CRAFT_ITEM -> {
                    HashMap<Material, Integer> materialIntegerHashMap = new HashMap<>();
                    for (Material material : Material.values()) {
                        int amount = player.getStatistic(statistic, material);
                        if (amount > 0) {
                            materialIntegerHashMap.put(material, amount);
                        }
                    }

                    blockStatistics.put(statistic, materialIntegerHashMap);
                }

                case KILL_ENTITY, ENTITY_KILLED_BY -> {
                    HashMap<EntityType, Integer> entityTypeIntegerHashMap = new HashMap<>();
                    for (EntityType entityType : EntityType.values()) {
                        if (entityType == EntityType.UNKNOWN) continue;

                        int amount = player.getStatistic(statistic, entityType);
                        if (amount > 0) {
                            entityTypeIntegerHashMap.put(entityType, amount);
                        }
                    }

                    entityStatistics.put(statistic, entityTypeIntegerHashMap);
                }

                default -> {
                    int amount = player.getStatistic(statistic);
                    if (amount > 0) {
                        globalStatistics.put(statistic, amount);
                    }
                }
            }
        }

        user.setGlobalStatistics(globalStatistics);
        user.setBlockStatistics(blockStatistics);
        user.setEntityStatistics(entityStatistics);
        user.setAllowFlight(player.getAllowFlight());
        user.setFlying(player.isFlying());
    }

    @Override
    public void translateToPlayer(User user, Player player) {
        ConfigurationSection section = config.getConfigurationSection("synchronization.features");
        if (section == null) {
            plugin.getLogger().severe("Could not find section 'synchronization.features' in config.yml");
            return;
        }

        if (section.getBoolean("inventory")) {
            if (user.getInventoryStorageContents() != null) {
                player.getInventory().setStorageContents(user.getInventoryStorageContents());
            }

            if (user.getInventoryArmorContents() != null) {
                player.getInventory().setArmorContents(user.getInventoryArmorContents());
            }

            if (user.getInventoryExtraContents() != null) {
                player.getInventory().setExtraContents(user.getInventoryExtraContents());
            }

            player.getInventory().setHeldItemSlot(user.getHeldItemSlot());
        }

        if (section.getBoolean("ender-chest") && user.getEnderChestItems() != null) {
            try {
                player.getEnderChest().setContents(user.getEnderChestItems());
            } catch (IllegalArgumentException ignored) {
                for (int i = 0; i < player.getEnderChest().getSize(); i++) {
                    player.getEnderChest().setItem(i, user.getEnderChestItems()[i]);
                }
            }
        }

        if (section.getBoolean("game-mode") && user.getGameMode() != null) {
            player.setGameMode(user.getGameMode());
        }

        if (section.getBoolean("potion-effects") && user.getPotionEffects() != null) {
            player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType()));
            player.addPotionEffects(user.getPotionEffects());
        }

        if (section.getBoolean("health")) {
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);

            if (user.getHealth() > player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()) {
                player.setHealth(20);
            } else {
                player.setHealth(user.getHealth());
            }
        }

        if (section.getBoolean("food")) {
            player.setFoodLevel(user.getFoodLevel());
            player.setSaturation(user.getSaturation());
            player.setExhaustion(user.getExhaustion());
        }

        if (section.getBoolean("air")) {
            player.setMaximumAir(user.getMaximumAir());
            player.setRemainingAir(user.getRemainingAir());
        }

        if (section.getBoolean("location") && user.getLocation() != null) {
            player.teleport(user.getLocation());
        }

        if (section.getBoolean("advancements") && user.getAdvancements() != null) {
            user.getAdvancements().keySet().forEach(advancement -> {
                try {
                    AdvancementProgress progress = player.getAdvancementProgress(advancement);
                    user.getAdvancements().get(advancement).forEach(progress::awardCriteria);
                } catch (IllegalArgumentException ignored) {
                    plugin.getLogger().severe("Cannot award criteria for advancement " + advancement + ". All servers are running the same version of Minecraft?");
                }
            });
        }

        if (section.getBoolean("experience")) {
            player.setExp(user.getExp());
            player.setLevel(user.getLevel());
        }

        if (section.getBoolean("statistics") && user.getGlobalStatistics() != null && user.getBlockStatistics() != null && user.getEntityStatistics() != null) {
            user.getGlobalStatistics().keySet().forEach(statistic -> {
                player.setStatistic(statistic, user.getGlobalStatistics().get(statistic));
            });

            user.getBlockStatistics().keySet().forEach(statistic -> {
                user.getBlockStatistics().get(statistic).keySet().forEach(material -> {
                    player.setStatistic(statistic, material, user.getBlockStatistics().get(statistic).get(material));
                });
            });

            user.getEntityStatistics().keySet().forEach(statistic -> {
                user.getEntityStatistics().get(statistic).keySet().forEach(entityType -> {
                    player.setStatistic(statistic, entityType, user.getEntityStatistics().get(statistic).get(entityType));
                });
            });
        }

        if (section.getBoolean("fly")) {
            player.setAllowFlight(user.isAllowFlight());
            if (user.isAllowFlight()) {
                player.setFlying(user.isFlying());
            } else {
                player.setFlying(false);
            }
        }
    }
}
