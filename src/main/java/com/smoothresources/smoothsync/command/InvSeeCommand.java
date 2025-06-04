package com.smoothresources.smoothsync.command;

import com.smoothresources.smoothbase.common.file.YAMLFile;
import com.smoothresources.smoothsync.SmoothSync;
import com.smoothresources.smoothsync.menu.InvSeeMenu;
import com.smoothresources.smoothsyncapi.user.User;
import com.smoothresources.smoothsyncapi.user.UserService;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class InvSeeCommand extends Command {

    private YAMLFile messages;
    private UserService userService;
    private SmoothSync plugin;

    public InvSeeCommand(@NotNull String name, YAMLFile messages, UserService userService, SmoothSync plugin) {
        super(name);
        this.messages = messages;
        this.userService = userService;
        this.plugin = plugin;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.getComponent("command", "invsee", "not-a-player"));
            return true;
        }

        if (!player.hasPermission("smoothsync.command.invsee")) {
            player.sendMessage(messages.getComponent("command", "invsee", "no-permission"));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(messages.getComponent("command", "invsee", "usage"));
            return true;
        }

        HashMap<String, String> placeholders = new HashMap<>();
        placeholders.put("%player%", args[0]);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                player.sendMessage(messages.getComponent(placeholders, "command", "invsee", "loading"));
                User user = userService.requestUpdatedUserByUsername(args[0]).orElse(null);
                if (user == null) {
                    player.sendMessage(messages.getComponent("command", "invsee", "user-not-found"));
                    return;
                }

                player.sendMessage(messages.getComponent(placeholders, "command", "invsee", "success"));
                Bukkit.getScheduler().runTask(plugin, () -> {
                    InvSeeMenu invSeeMenu = new InvSeeMenu(player, user, placeholders);
                    invSeeMenu.open();
                });
            } catch (InterruptedException ignored) {
            }
        });

        return true;
    }
}
