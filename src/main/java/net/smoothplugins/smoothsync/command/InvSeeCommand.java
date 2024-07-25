package net.smoothplugins.smoothsync.command;

import net.smoothplugins.smoothbase.configuration.Configuration;
import net.smoothplugins.smoothsync.SmoothSync;
import net.smoothplugins.smoothsync.menu.InvSeeMenu;
import net.smoothplugins.smoothsyncapi.user.User;
import net.smoothplugins.smoothsyncapi.user.UserService;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class InvSeeCommand extends Command {

    private Configuration messages;
    private UserService userService;
    private SmoothSync plugin;

    public InvSeeCommand(@NotNull String name, Configuration messages, UserService userService, SmoothSync plugin) {
        super(name);
        this.messages = messages;
        this.userService = userService;
        this.plugin = plugin;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.getComponent("command.invsee.not-a-player"));
            return true;
        }

        if (!player.hasPermission("smoothsync.command.invsee")) {
            player.sendMessage(messages.getComponent("command.invsee.no-permission"));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(messages.getComponent("command.invsee.usage"));
            return true;
        }

        HashMap<String, String> placeholders = new HashMap<>();
        placeholders.put("%player%", args[0]);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                player.sendMessage(messages.getComponent("command.invsee.loading", placeholders));
                User user = userService.requestUpdatedUserByUsername(args[0]).orElse(null);
                if (user == null) {
                    player.sendMessage(messages.getComponent("command.invsee.user-not-found", placeholders));
                    return;
                }

                player.sendMessage(messages.getComponent("command.invsee.success", placeholders));
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
