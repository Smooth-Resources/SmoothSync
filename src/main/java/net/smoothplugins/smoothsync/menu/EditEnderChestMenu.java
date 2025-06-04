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
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class EditEnderChestMenu extends Menu {

    private final YAMLFile config;
    private final YAMLFile messages;
    private final User user;
    private final UserService userService;
    private final HashMap<String, String> placeholders;
    private final SmoothSync plugin;

    public EditEnderChestMenu(Player player, User user, HashMap<String, String> placeholders) {
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
        Component title = config.getComponent(placeholders, "smoothsync", "edit-enderchest", "menu", "title");
        int size = user.getEnderChestItems().length;
        Inventory inventory = Bukkit.createInventory(null, size, title);
        setInventory(inventory);
    }

    @Override
    public void setItems() {
        for (int i = 0; i < user.getEnderChestItems().length; i++) {
            ClickableButton button = new ClickableButton(user.getEnderChestItems()[i], i) {
                @Override
                public void onClick(@NotNull PlayerClickButtonEvent event) {
                    event.getOriginalBukkitEvent().setCancelled(false);
                }
            };
            setItem(button);
        }
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            user.setEnderChestItems(super.getInventory().getContents());
            userService.update(user, Destination.CACHE_IF_PRESENT, Destination.STORAGE, Destination.PLAYER_IF_ONLINE);
            super.getPlayer().sendMessage(messages.getComponent(placeholders, "command", "smoothsync", "edit-enderchest", "updated"));
        });
    }
}