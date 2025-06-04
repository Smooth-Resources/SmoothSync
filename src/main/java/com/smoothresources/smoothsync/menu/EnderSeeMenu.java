package com.smoothresources.smoothsync.menu;

import com.google.inject.Key;
import com.google.inject.name.Names;
import net.kyori.adventure.text.Component;
import com.smoothresources.smoothbase.common.file.YAMLFile;
import com.smoothresources.smoothbase.paper.menu.Menu;
import com.smoothresources.smoothbase.paper.menu.button.Button;
import com.smoothresources.smoothsync.SmoothSync;
import com.smoothresources.smoothsyncapi.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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
