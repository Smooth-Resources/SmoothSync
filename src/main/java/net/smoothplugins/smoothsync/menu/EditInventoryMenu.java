package net.smoothplugins.smoothsync.menu;

import com.google.inject.Key;
import com.google.inject.name.Names;
import es.virtualhit.virtualmenu.event.PlayerClickMenuItemEvent;
import es.virtualhit.virtualmenu.menu.Menu;
import es.virtualhit.virtualmenu.menu.item.Clickable;
import es.virtualhit.virtualmenu.menu.item.MenuItem;
import es.virtualhit.virtualmenu.menu.type.MenuType;
import net.kyori.adventure.text.Component;
import net.smoothplugins.smoothbase.configuration.Configuration;
import net.smoothplugins.smoothsync.SmoothSync;
import net.smoothplugins.smoothsyncapi.service.Destination;
import net.smoothplugins.smoothsyncapi.user.User;
import net.smoothplugins.smoothsyncapi.user.UserService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;

public class EditInventoryMenu extends Menu {

    private final Configuration config;
    private final Configuration messages;
    private final User user;
    private final HashMap<String, String> placeholders;
    private final UserService userService;
    private final SmoothSync plugin;

    public EditInventoryMenu(Player player, User user, HashMap<String, String> placeholders) {
        super(player);
        this.config = SmoothSync.getInjector().getInstance(Key.get(Configuration.class, Names.named("config")));
        this.messages = SmoothSync.getInjector().getInstance(Key.get(Configuration.class, Names.named("messages")));
        this.user = user;
        this.placeholders = placeholders;
        this.userService = SmoothSync.getInjector().getInstance(UserService.class);
        this.plugin = SmoothSync.getInjector().getInstance(SmoothSync.class);
    }

    public void open() {
        Component title = config.getComponent("smoothsync.edit-inventory.menu.title", placeholders);
        int size = 45;
        MenuType type = MenuType.CHEST;
        super.createInventory(type, size, title, new ArrayList<>());

        for (int i = 0; i < user.getInventoryStorageContents().length; i++) {
            MenuItem menuItem = new MenuItem(user.getInventoryStorageContents()[i], i);
            Clickable clickable = getClickable(ActionType.NONE);
            menuItem.setClickable(clickable);
            super.addItem(menuItem);
        }

        for (int i = 0; i < user.getInventoryArmorContents().length; i++) {
            MenuItem menuItem = new MenuItem(user.getInventoryArmorContents()[i], i + 36);
            Clickable clickable = getClickable(ActionType.NONE);
            menuItem.setClickable(clickable);
            super.addItem(menuItem);
        }

        for (int i = 0; i < user.getInventoryExtraContents().length; i++) {
            MenuItem menuItem = new MenuItem(user.getInventoryExtraContents()[i], i + 44);
            Clickable clickable = getClickable(ActionType.NONE);
            menuItem.setClickable(clickable);
            super.addItem(menuItem);
        }

        super.updateItems();
        super.open();
    }

    private enum ActionType {
        NONE
    }

    private Clickable getClickable(ActionType type) {
        return new Clickable() {
            @Override
            public void onClick(PlayerClickMenuItemEvent playerClickMenuItemEvent) {
                playerClickMenuItemEvent.setCancelled(false);
            }
        };
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
            super.getPlayer().sendMessage(messages.getComponent("command.smoothsync.edit-inventory.updated", placeholders));
        });
    }
}