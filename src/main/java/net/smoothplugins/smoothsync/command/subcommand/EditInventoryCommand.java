package net.smoothplugins.smoothsync.command.subcommand;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import net.smoothplugins.smoothbase.configuration.Configuration;
import net.smoothplugins.smoothsync.SmoothSync;
import net.smoothplugins.smoothsync.command.Subcommand;
import net.smoothplugins.smoothsync.menu.EditInventoryMenu;
import net.smoothplugins.smoothsyncapi.user.User;
import net.smoothplugins.smoothsyncapi.user.UserService;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class EditInventoryCommand implements Subcommand {

    @Inject @Named("messages")
    private Configuration messages;
    @Inject
    private UserService userService;
    @Inject
    private SmoothSync plugin;

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.getComponent("command.smoothsync.edit-inventory.not-a-player"));
            return;
        }

        if (!sender.hasPermission("smoothsync.command.smoothsync.edit-inventory")) {
            sender.sendMessage(messages.getComponent("command.smoothsync.edit-inventory.no-permission"));
            return;
        }

        if (args.length != 2) {
            sender.sendMessage(messages.getComponent("command.smoothsync.edit-inventory.usage"));
            return;
        }

        HashMap<String, String> placeholders = new HashMap<>();
        placeholders.put("%player%", args[1]);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                player.sendMessage(messages.getComponent("command.smoothsync.edit-inventory.loading", placeholders));
                User user = userService.requestUpdatedUserByUsername(args[1]).orElse(null);
                if (user == null) {
                    player.sendMessage(messages.getComponent("command.smoothsync.edit-inventory.user-not-found", placeholders));
                    return;
                }

                player.sendMessage(messages.getComponent("command.smoothsync.edit-inventory.success", placeholders));
                Bukkit.getScheduler().runTask(plugin, () -> {
                    EditInventoryMenu editInventoryMenu = new EditInventoryMenu(player, user, placeholders);
                    editInventoryMenu.open();
                });
            } catch (InterruptedException ignored) {
            }
        });
    }
}
