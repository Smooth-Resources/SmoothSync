package com.smoothresources.smoothsync.command.subcommand;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.smoothresources.smoothbase.common.file.YAMLFile;
import com.smoothresources.smoothsync.SmoothSync;
import com.smoothresources.smoothsync.command.Subcommand;
import com.smoothresources.smoothsync.menu.EditInventoryMenu;
import com.smoothresources.smoothsyncapi.user.User;
import com.smoothresources.smoothsyncapi.user.UserService;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class EditInventoryCommand implements Subcommand {

    @Inject @Named("messages")
    private YAMLFile messages;
    @Inject
    private UserService userService;
    @Inject
    private SmoothSync plugin;

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.getComponent("command", "smoothsync", "edit-inventory", "not-a-player"));
            return;
        }

        if (!sender.hasPermission("smoothsync.command.smoothsync.edit-inventory")) {
            sender.sendMessage(messages.getComponent("command", "smoothsync", "edit-inventory", "no-permission"));
            return;
        }

        if (args.length != 2) {
            sender.sendMessage(messages.getComponent("command", "smoothsync", "edit-inventory", "usage"));
            return;
        }

        HashMap<String, String> placeholders = new HashMap<>();
        placeholders.put("%player%", args[1]);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                player.sendMessage(messages.getComponent(placeholders, "command", "smoothsync", "edit-inventory", "loading"));
                User user = userService.requestUpdatedUserByUsername(args[1]).orElse(null);
                if (user == null) {
                    player.sendMessage(messages.getComponent("command", "smoothsync", "edit-inventory", "user-not-found"));
                    return;
                }

                player.sendMessage(messages.getComponent(placeholders, "command", "smoothsync", "edit-inventory", "success"));
                Bukkit.getScheduler().runTask(plugin, () -> {
                    EditInventoryMenu editInventoryMenu = new EditInventoryMenu(player, user, placeholders);
                    editInventoryMenu.open();
                });
            } catch (InterruptedException ignored) {
            }
        });
    }
}
