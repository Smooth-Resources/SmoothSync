package net.smoothplugins.smoothsync.command;

import net.smoothplugins.smoothbase.configuration.Configuration;
import net.smoothplugins.smoothsync.SmoothSync;
import net.smoothplugins.smoothsync.menu.EnderSeeMenu;
import net.smoothplugins.smoothsyncapi.user.User;
import net.smoothplugins.smoothsyncapi.user.UserService;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class EnderSeeCommand extends Command {

    private Configuration messages;
    private UserService userService;
    private SmoothSync plugin;

    public EnderSeeCommand(@NotNull String name, Configuration messages, UserService userService, SmoothSync plugin) {
        super(name);
        this.messages = messages;
        this.userService = userService;
        this.plugin = plugin;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.getComponent("command.endersee.not-a-player"));
            return true;
        }

        if (!player.hasPermission("smoothsync.command.endersee")) {
            player.sendMessage(messages.getComponent("command.endersee.no-permission"));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(messages.getComponent("command.endersee.usage"));
            return true;
        }

        HashMap<String, String> placeholders = new HashMap<>();
        placeholders.put("%player%", args[0]);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                player.sendMessage(messages.getComponent("command.endersee.loading", placeholders));
                User user = userService.requestUpdatedUserByUsername(args[0]).orElse(null);
                if (user == null) {
                    player.sendMessage(messages.getComponent("command.endersee.user-not-found", placeholders));
                    return;
                }

                player.sendMessage(messages.getComponent("command.endersee.success", placeholders));
                Bukkit.getScheduler().runTask(plugin, () -> {
                    EnderSeeMenu enderSeeMenu = new EnderSeeMenu(player, user, placeholders);
                    enderSeeMenu.open();
                });
            } catch (InterruptedException ignored) {
            }
        });

        return true;
    }
}
