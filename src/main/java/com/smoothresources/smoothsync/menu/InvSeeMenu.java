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

public class InvSeeMenu extends Menu {

    private final YAMLFile config;
    private final User user;
    private final HashMap<String, String> placeholders;

    public InvSeeMenu(Player player, User user, HashMap<String, String> placeholders) {
        super(player);
        this.config = SmoothSync.getInjector().getInstance(Key.get(YAMLFile.class, Names.named("config")));
        this.user = user;
        this.placeholders = placeholders;
    }

    @Override
    public void createInventory() {
        Component title = config.getComponent(placeholders, "invsee", "menu", "title");
        int size = 45;
        Inventory inventory = Bukkit.createInventory(null, size, title);
        setInventory(inventory);
    }

    @Override
    public void setItems() {
        for (int i = 0; i < user.getInventoryStorageContents().length; i++) {
            Button button = new Button(user.getInventoryStorageContents()[i], i);
            setItem(button);
        }

        for (int i = 0; i < user.getInventoryArmorContents().length; i++) {
            Button button = new Button(user.getInventoryArmorContents()[i], i + 36);
            setItem(button);
        }

        for (int i = 0; i < user.getInventoryExtraContents().length; i++) {
            Button button = new Button(user.getInventoryExtraContents()[i], i + 44);
            setItem(button);
        }
    }
}
