package net.smoothplugins.smoothsync.menu;

import com.google.inject.Key;
import com.google.inject.name.Names;
import net.kyori.adventure.text.Component;
import net.smoothplugins.smoothbase.common.file.YAMLFile;
import net.smoothplugins.smoothbase.paper.menu.Menu;
import net.smoothplugins.smoothbase.paper.menu.button.ClickableButton;
import net.smoothplugins.smoothbase.paper.menu.event.PlayerClickButtonEvent;
import net.smoothplugins.smoothsync.SmoothSync;
import net.smoothplugins.smoothsyncapi.service.Destination;
import net.smoothplugins.smoothsyncapi.user.User;
import net.smoothplugins.smoothsyncapi.user.UserService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class EditInventoryMenu extends Menu {

    private final YAMLFile config;
    private final YAMLFile messages;
    private final User user;
    private final HashMap<String, String> placeholders;
    private final UserService userService;
    private final SmoothSync plugin;

    public EditInventoryMenu(Player player, User user, HashMap<String, String> placeholders) {
        super(player);
        this.config = SmoothSync.getInjector().getInstance(Key.get(YAMLFile.class, Names.named("config")));
        this.messages = SmoothSync.getInjector().getInstance(Key.get(YAMLFile.class, Names.named("messages")));
        this.user = user;
        this.placeholders = placeholders;
        this.userService = SmoothSync.getInjector().getInstance(UserService.class);
        this.plugin = SmoothSync.getInjector().getInstance(SmoothSync.class);
    }

    @Override
    public void createInventory() {
        Component title = config.getComponent(placeholders, "smoothsync", "edit-inventory", "menu", "title");
        int size = 45;
        Inventory inventory = Bukkit.createInventory(null, size, title);
        setInventory(inventory);
    }

    @Override
    public void setItems() {
        for (int i = 0; i < user.getInventoryStorageContents().length; i++) {
            ClickableButton button = new ClickableButton(user.getInventoryStorageContents()[i], i) {
                @Override
                public void onClick(@NotNull PlayerClickButtonEvent event) {
                    event.getOriginalBukkitEvent().setCancelled(false);
                }
            };
            setItem(button);
        }

        for (int i = 0; i < user.getInventoryArmorContents().length; i++) {
            ClickableButton button = new ClickableButton(user.getInventoryArmorContents()[i], i + 36) {
                @Override
                public void onClick(@NotNull PlayerClickButtonEvent event) {
                    event.getOriginalBukkitEvent().setCancelled(false);
                }
            };
            setItem(button);
        }

        for (int i = 0; i < user.getInventoryExtraContents().length; i++) {
            ClickableButton button = new ClickableButton(user.getInventoryExtraContents()[i], i + 44) {
                @Override
                public void onClick(@NotNull PlayerClickButtonEvent event) {
                    event.getOriginalBukkitEvent().setCancelled(false);
                }
            };
            setItem(button);
        }
    }

    private ItemStack[] getStorageContents() {
        Inventory inventory = super.getInventory();
        ItemStack[] storageContents = new ItemStack[36];
        for (int i = 0; i < 36; i++) {
            storageContents[i] = inventory.getItem(i);
        }

        return storageContents;
    }

    private ItemStack[] getArmorContents() {
        Inventory inventory = super.getInventory();
        ItemStack[] armorContents = new ItemStack[4];
        for (int i = 0; i < 4; i++) {
            armorContents[i] = inventory.getItem(i + 36);
        }

        return armorContents;
    }

    private ItemStack[] getExtraContents() {
        Inventory inventory = super.getInventory();
        ItemStack[] extraContents = new ItemStack[1];
        for (int i = 0; i < 1; i++) {
            extraContents[i] = inventory.getItem(i + 44);
        }

        return extraContents;
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            user.setInventoryStorageContents(getStorageContents());
            user.setInventoryArmorContents(getArmorContents());
            user.setInventoryExtraContents(getExtraContents());

            userService.update(user, Destination.CACHE_IF_PRESENT, Destination.STORAGE, Destination.PLAYER_IF_ONLINE);
            super.getPlayer().sendMessage(messages.getComponent(placeholders, "command", "smoothsync", "edit-inventory", "updated"));
        });
    }
}