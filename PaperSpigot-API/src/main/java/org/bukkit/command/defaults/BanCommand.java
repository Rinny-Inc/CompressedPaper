package org.bukkit.command.defaults;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;

import io.noks.IPunishment;

public class BanCommand extends VanillaCommand implements IPunishment {
    public BanCommand() {
        super("ban");
        this.description = "Prevents the specified player from using this server";
        this.usageMessage = "/ban (-s) <player> (1s,1m,1h,1d,1w,1M,1y) [reason ...]";
        this.setPermission("bukkit.command.ban.player");
    }

    @Override
    public boolean execute(CommandSender sender, String currentAlias, String[] args) {
        if (!testPermission(sender)) return true;
        if (args.length < 1)  {
            sender.sendMessage(ChatColor.RED + "Usage: " + usageMessage);
            return false;
        }
        boolean silent = args[0].equalsIgnoreCase("-s") || args[0].equalsIgnoreCase("-silent");
        int playerPos = !silent ? 0 : 1;
        
        if (args.length < playerPos) {
        	sender.sendMessage(ChatColor.RED + "Please specify a player to ban.");
            return false;
        }
        String playerName = args[playerPos];
        Player player = Bukkit.getPlayer(playerName);
        
        boolean isTemporary = args.length > playerPos + 1 && isValidDuration(args[playerPos + 1]);
        Date duration = isTemporary ? parseDuration(args[playerPos + 1]) : null;
        
        String reason = args.length > playerPos + (isTemporary ? 2 : 1)
                ? String.join(" ", Arrays.copyOfRange(args, playerPos + (isTemporary ? 2 : 1), args.length))
                : "Banned by an operator.";
        
        Bukkit.getBanList(BanList.Type.NAME).addBan(playerName, reason, duration, sender.getName());

        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd \'at\' HH:mm:ss z");
        if (player != null) {
            player.kickPlayer("You are banned from this server!" + 
							  "\nSource: " + sender.getName() +
							  "\nReason: " + reason +
							  (isTemporary ? "\nYour ban will be removed on " + sdf.format(duration) : ""));
        }

        final String msg = (silent ? ChatColor.GRAY + "(SILENT) " : "") + ChatColor.GREEN + sender.getName() + " banned " + args[playerPos] + "; Duration of " + (isTemporary ? args[playerPos + 1] : "forever");
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
