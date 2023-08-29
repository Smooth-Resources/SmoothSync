package net.smoothplugins.smoothsync.command.subcommand;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import net.smoothplugins.smoothbase.configuration.Configuration;
import net.smoothplugins.smoothsync.SmoothSync;
import net.smoothplugins.smoothsync.command.Subcommand;
import net.smoothplugins.smoothsync.menu.EditEnderChestMenu;
import net.smoothplugins.smoothsyncapi.user.User;
import net.smoothplugins.smoothsyncapi.user.UserService;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class EditEnderChestCommand implements Subcommand {

    @Inject
    @Named("messages")
    private Configuration messages;
    @Inject
    private UserService userService;
    @Inject
    private SmoothSync plugin;

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.getComponent("command.smoothsync.edit-enderchest.not-a-player"));
            return;
        }

        if (!sender.hasPermission("smoothsync.command.smoothsync.edit-enderchest")) {
            sender.sendMessage(messages.getComponent("command.smoothsync.edit-enderchest.no-permission"));
            return;
        }

        if (args.length != 2) {
            sender.sendMessage(messages.getComponent("command.smoothsync.edit-enderchest.usage"));
            return;
        }

        HashMap<String, String> placeholders = new HashMap<>();
        placeholders.put("%player%", args[1]);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                player.sendMessage(messages.getComponent("command.smoothsync.edit-enderchest.loading", placeholders));
                User user = userService.requestUpdatedUserByUsername(args[0]).orElse(null);
                if (user == null) {
                    player.sendMessage(messages.getComponent("command.smoothsync.edit-enderchest.user-not-found"));
                    return;
                }

                player.sendMessage(messages.getComponent("command.smoothsync.edit-enderchest.success", placeholders));
                Bukkit.getScheduler().runTask(plugin, () -> {
                    EditEnderChestMenu editEnderChestMenu = new EditEnderChestMenu(player, user);
                    editEnderChestMenu.open();
                });
            } catch (InterruptedException ignored) {
            }
        });
    }
}