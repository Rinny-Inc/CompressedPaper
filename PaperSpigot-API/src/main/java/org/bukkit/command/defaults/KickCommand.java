package org.bukkit.command.defaults;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;

public class KickCommand extends VanillaCommand {
    public KickCommand() {
        super("kick");
        this.description = "Removes the specified player from the server";
        this.usageMessage = "/kick (-s) <player> [reason ...]";
        this.setPermission("bukkit.command.kick");
    }

    @Override
    public boolean execute(CommandSender sender, String currentAlias, String[] args) {
        if (!testPermission(sender)) return true;
        if (args.length < 1 || args[0].length() == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: " + usageMessage);
            return false;
        }
        boolean silent = args[0].equalsIgnoreCase("-s") || args[0].equalsIgnoreCase("-silent");
        int playerPos = !silent ? 0 : 1;
        final Player player = Bukkit.getPlayerExact(args[playerPos]);

        if (player == null) {
        	sender.sendMessage( args[playerPos] + " not found.");
        	return false;
        }
        String reason = "Kicked by an operator.";

        if (args.length > playerPos + 1) {
            reason = createString(args, playerPos + 1);
        }

        player.kickPlayer(reason);
        final String msg = (silent ? ChatColor.GRAY + "(SILENT) " : "") + ChatColor.GREEN + sender.getName() + " kicked " + player.getName() + ". With reason: " + reason;
        if (!silent) {
        	Bukkit.broadcastMessage(msg);
        	return true;
        }
        Command.broadcastCommandMessage(sender, msg);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        Validate.notNull(sender, "Sender cannot be null");
        Validate.notNull(args, "Arguments cannot be null");
        Validate.notNull(alias, "Alias cannot be null");

        if (args.length >= 1) {
            return super.tabComplete(sender, alias, args);
        }
        return ImmutableList.of();
    }
}
