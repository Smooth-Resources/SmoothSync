package net.smoothplugins.smoothsync.menu;

import com.google.inject.Key;
import com.google.inject.name.Names;
import net.kyori.adventure.text.Component;
import net.smoothplugins.smoothbase.common.file.YAMLFile;
import net.smoothplugins.smoothbase.paper.menu.Menu;
import net.smoothplugins.smoothbase.paper.menu.button.Button;
import net.smoothplugins.smoothsync.SmoothSync;
import net.smoothplugins.smoothsyncapi.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;

public class EnderSeeMenu extends Menu {

    private final YAMLFile config;
    private final User user;
    private final HashMap<String, String> placeholders;

    public EnderSeeMenu(Player player, User user, HashMap<String, String> placeholders) {
        super(player);
        this.config = SmoothSync.getInjector().getInstance(Key.get(YAMLFile.class, Names.named("config")));
        this.user = user;
        this.placeholders = placeholders;
    }

    @Override
    public void createInventory() {
        Component title = config.getComponent(placeholders, "endersee", "menu", "title");
        int size = user.getEnderChestItems().length;
        Inventory inventory = Bukkit.createInventory(null, size, title);
        setInventory(inventory);
    }

    @Override
    public void setItems() {
        for (int i = 0; i < user.getEnderChestItems().length; i++) {
            Button button = new Button(user.getEnderChestItems()[i], i);
            setItem(button);
        }
    }
}
