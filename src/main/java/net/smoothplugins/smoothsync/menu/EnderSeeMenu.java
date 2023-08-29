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
import net.smoothplugins.smoothsyncapi.user.User;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;

public class EnderSeeMenu extends Menu {

    private final Configuration config;
    private final User user;
    private final HashMap<String, String> placeholders;

    public EnderSeeMenu(Player player, User user, HashMap<String, String> placeholders) {
        super(player);
        this.config = SmoothSync.getInjector().getInstance(Key.get(Configuration.class, Names.named("config")));
        this.user = user;
        this.placeholders = placeholders;
    }

    public void open() {
        Component title = config.getComponent("endersee.menu.title", placeholders);
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
}
