package net.smoothplugins.smoothsync.user;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import net.smoothplugins.smoothbase.configuration.Configuration;
import net.smoothplugins.smoothsync.SmoothSync;
import net.smoothplugins.smoothsyncapi.user.User;
import net.smoothplugins.smoothsyncapi.user.UserTranslator;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class DefaultUserTranslator implements UserTranslator {

    @Inject @Named("config")
    private Configuration config;
    @Inject
    private SmoothSync plugin;

    @Override
    public void translateToUser(User user, Player player) {
        user.setInventoryItems(player.getInventory().getContents());
        user.setEnderChestItems(player.getEnderChest().getContents());
        user.setGameMode(player.getGameMode());
        user.setExp(player.getExp());
        user.setPotionEffects(player.getActivePotionEffects());
        user.setHealthScale(player.getHealthScale());
        user.setHealth(player.getHealth());
        user.setFoodLevel(player.getFoodLevel());
        user.setSaturation(player.getSaturation());
        user.setExhaustion(player.getExhaustion());
        user.setMaximumAir(player.getMaximumAir());
        user.setRemainingAir(player.getRemainingAir());
        user.setLocation(player.getLocation());

        HashMap<Advancement, AdvancementProgress> advancements = new HashMap<>();
        Bukkit.getServer().advancementIterator().forEachRemaining(advancement -> {
            advancements.put(advancement, player.getAdvancementProgress(advancement));
        });
        user.setAdvancements(advancements);

        HashMap<Statistic, Integer> statistics = new HashMap<>();
        for (Statistic statistic : Statistic.values()) {
            statistics.put(statistic, player.getStatistic(statistic));
        }
        user.setStatistics(statistics);
    }

    @Override
    public void translateToPlayer(User user, Player player) {
        ConfigurationSection section = config.getConfigurationSection("synchronization.features");
        if (section == null) {
            plugin.getLogger().severe("Could not find section 'synchronization.features' in config.yml");
            return;
        }

        if (section.getBoolean("inventory")) {
            try {
                player.getInventory().setContents(user.getInventoryItems());
            } catch (IllegalArgumentException ignored) {
                for (int i = 0; i < player.getInventory().getSize(); i++) {
                    player.getInventory().setItem(i, user.getInventoryItems()[i]);
                }
            }
        }

        if (section.getBoolean("ender-chest")) {
            try {
                player.getEnderChest().setContents(user.getEnderChestItems());
            } catch (IllegalArgumentException ignored) {
                for (int i = 0; i < player.getEnderChest().getSize(); i++) {
                    player.getEnderChest().setItem(i, user.getEnderChestItems()[i]);
                }
            }
        }

        if (section.getBoolean("game-mode")) {
            player.setGameMode(user.getGameMode());
        }

        if (section.getBoolean("experience")) {
            player.setExp(user.getExp());
        }

        if (section.getBoolean("potion-effects")) {
            player.addPotionEffects(user.getPotionEffects());
        }

        if (section.getBoolean("health")) {
            player.setHealthScale(user.getHealthScale());
            player.setHealth(user.getHealth());
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

        if (section.getBoolean("location")) {
            player.teleport(user.getLocation());
        }

        if (section.getBoolean("advancements")) {
            user.getAdvancements().keySet().forEach(advancement -> {
                AdvancementProgress progress = player.getAdvancementProgress(advancement);
                progress = user.getAdvancements().get(advancement);
            });
        }

        if (section.getBoolean("statistics")) {
            user.getStatistics().keySet().forEach(statistic -> {
                player.setStatistic(statistic, user.getStatistics().get(statistic));
            });
        }
    }
}
