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
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.ArrayList;
import java.util.HashMap;

public class EditEnderChestMenu extends Menu {

    private final Configuration config;
    private final Configuration messages;
    private final User user;
    private final UserService userService;
    private final HashMap<String, String> placeholders;

    public EditEnderChestMenu(Player player, User user) {
        super(player);
        this.config = SmoothSync.getInjector().getInstance(Key.get(Configuration.class, Names.named("config")));
        this.messages = SmoothSync.getInjector().getInstance(Key.get(Configuration.class, Names.named("messages")));
        this.user = user;
        this.placeholders = new HashMap<>();
        this.userService = SmoothSync.getInjector().getInstance(UserService.class);
    }

    public void open() {
        placeholders.put("%player%", super.getPlayer().getName());

        Component title = config.getComponent("smoothsync.edit-enderchest.menu.title", placeholders);
        int size = user.getEnderChestItems().length;
        MenuType type = MenuType.CHEST;
        super.createInventory(type, size, title, new ArrayList<>());

        for (int i = 0; i < user.getEnderChestItems().length; i++) {
            MenuItem menuItem = new MenuItem(user.getEnderChestItems()[i], i);
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
                playerClickMenuItemEvent.setCancelled(true);
            }
        };
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        userService.update(user, Destination.CACHE_IF_PRESENT, Destination.STORAGE, Destination.PLAYER_IF_ONLINE);
        super.getPlayer().sendMessage(messages.getComponent("command.smoothsync.edit-enderchest.updated", placeholders));
    }
}