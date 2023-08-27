package net.smoothplugins.smoothsync.command;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import net.smoothplugins.smoothbase.configuration.Configuration;
import net.smoothplugins.smoothsync.SmoothSync;
import net.smoothplugins.smoothsync.menu.EnderSeeMenu;
import net.smoothplugins.smoothsync.menu.InvSeeMenu;
import net.smoothplugins.smoothsyncapi.user.User;
import net.smoothplugins.smoothsyncapi.user.UserService;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class EnderSeeCommand implements CommandExecutor {

    @Inject @Named("messages")
    private Configuration messages;
    @Inject
    private UserService userService;
    @Inject
    private SmoothSync plugin;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
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
                    player.sendMessage(messages.getComponent("command.endersee.user-not-found"));
                    return;
                }

                player.sendMessage(messages.getComponent("command.endersee.success", placeholders));
                Bukkit.getScheduler().runTask(plugin, () -> {
                    EnderSeeMenu enderSeeMenu = new EnderSeeMenu(player, user);
                    enderSeeMenu.open();
                });
            } catch (InterruptedException ignored) {
            }
        });

        return true;
    }
}
