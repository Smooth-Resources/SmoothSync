package net.smoothplugins.smoothsync.command.subcommand;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import net.smoothplugins.smoothbase.common.file.YAMLFile;
import net.smoothplugins.smoothsync.command.Subcommand;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements Subcommand {

    @Inject @Named("config")
    private YAMLFile config;
    @Inject @Named("messages")
    private YAMLFile messages;

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("smoothsync.command.smoothsync.reload")) {
            sender.sendMessage(messages.getComponent("command", "smoothsync", "reload", "no-permission"));
            return;
        }

        config.reload();
        messages.reload();

        sender.sendMessage(messages.getComponent("command", "smoothsync", "reload", "success"));
    }
}
