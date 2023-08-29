package net.smoothplugins.smoothsync.command;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import net.smoothplugins.smoothbase.configuration.Configuration;
import net.smoothplugins.smoothsync.command.subcommand.EditInventoryCommand;
import net.smoothplugins.smoothsync.command.subcommand.ReloadCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public class SmoothSyncCommand implements CommandExecutor, TabCompleter {

    @Inject @Named("messages")
    private Configuration messages;
    @Inject
    private ReloadCommand reloadCommand;
    @Inject
    private EditInventoryCommand editInventoryCommand;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("smoothsync.command.smoothsync")) {
            sender.sendMessage(messages.getComponent("command.smoothsync.no-permission"));
            return true;
        }

        if (args.length == 0) {
            messages.getComponentList("command.smoothsync.help").forEach(sender::sendMessage);
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "reload" -> {
                reloadCommand.execute(sender, args);
            }

            case "edit-inventory" -> {
                editInventoryCommand.execute(sender, args);
            }

            case "edit-enderchest" -> {

            }

            default -> {
                messages.getComponentList("command.smoothsync.help").forEach(sender::sendMessage);
                return true;
            }
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return null;
    }
}
