package net.smoothplugins.smoothsync.command;

import org.bukkit.command.CommandSender;

public interface Subcommand {

    void execute(CommandSender sender, String[] args);
}
