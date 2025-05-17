package org.bukkit.command.defaults;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import com.google.common.collect.ImmutableList;

public class WhitelistCommand extends VanillaCommand {
    private static final List<String> WHITELIST_SUBCOMMANDS = ImmutableList.of("add", "remove", "on", "off", "list", "reload", "token");

    public WhitelistCommand() {
        super("whitelist");
        this.description = "Manages the list of players allowed to use this server";
        this.usageMessage = "/whitelist (add|remove) <player>\n/whitelist (on|off|list|reload)\n/whitelist token <player> <amount>";
        this.setAliases(Arrays.asList(new String[] { "wl" }));
        this.setPermission("bukkit.command.whitelist.reload;bukkit.command.whitelist.enable;bukkit.command.whitelist.disable;bukkit.command.whitelist.list;bukkit.command.whitelist.add;bukkit.command.whitelist.remove;bukkit.command.whitelist.token");
    }

    @Override
    public boolean execute(CommandSender sender, String currentAlias, String[] args) {
        if (!testPermission(sender)) return true;

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (badPerm(sender, "reload")) return true;

                Bukkit.reloadWhitelist();
                Command.broadcastCommandMessage(sender, "Reloaded white-list from file");
                return true;
            }
            if (args[0].equalsIgnoreCase("on")) {
                if (badPerm(sender, "enable")) return true;

                Bukkit.setWhitelist(true);
                Command.broadcastCommandMessage(sender, "Turned on white-listing");
                return true;
            }
            if (args[0].equalsIgnoreCase("off")) {
                if (badPerm(sender, "disable")) return true;

                Bukkit.setWhitelist(false);
                Command.broadcastCommandMessage(sender, "Turned off white-listing");
                return true;
            }
            if (args[0].equalsIgnoreCase("list")) {
                if (badPerm(sender, "list")) return true;

                StringJoiner result = new StringJoiner(", ");
                int amount = 0;
                for (OfflinePlayer player : Bukkit.getWhitelistedPlayers()) {
                    result.add(player.getName());
                    amount++;
                }
                sender.sendMessage("White-listed players (" + (Bukkit.hasWhitelist() ? "Activated" : "Deactivated") + ", " + amount + "): " + result.toString());
                return true;
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("add")) {
            	if (sender instanceof Player) {
	            	Player player = (Player) sender;
	            	
	            	if (badPerm(sender, "add") && player.getToken() == 0) {
	            		return true;
	            	}
            	}

                Bukkit.getOfflinePlayer(args[1]).setWhitelisted(true);
                Command.broadcastCommandMessage(sender, "Added " + args[1] + " to white-list");
                return true;
            }
            if (args[0].equalsIgnoreCase("remove")) {
                if (badPerm(sender, "remove")) return true;

                Bukkit.getOfflinePlayer(args[1]).setWhitelisted(false);

                Command.broadcastCommandMessage(sender, "Removed " + args[1] + " from white-list");
                return true;
            }
        } else if (args.length == 3) {
        	if (args[0].equalsIgnoreCase("token")) {
                if (badPerm(sender, "token")) return true;
                if (!isShort(args[2])) {
                	return false;
                }
                
                OfflinePlayer op = Bukkit.getOfflinePlayer(args[1]);
                
                if (op.isOnline()) {
                	Player p = (Player) op;
                	
                	p.addToken(Short.parseShort(args[2]));
                	Command.broadcastCommandMessage(sender, "Added " + args[2] + " whitelist token to " + args[1]);
                	return true;
                }
                
                op.addToken(Short.parseShort(args[2]));
                Command.broadcastCommandMessage(sender, "Added " + args[2] + " whitelist token to " + args[1]);
                return true;
            }
        }

        sender.sendMessage(ChatColor.RED + "Correct command usage:\n" + usageMessage);
        return false;
    }

    private boolean badPerm(CommandSender sender, String perm) {
        if (!sender.hasPermission("bukkit.command.whitelist." + perm)) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to perform this action.");
            return true;
        }

        return false;
    }
    
    private static boolean isShort(String str) {
        try {
            Short.parseShort(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        Validate.notNull(sender, "Sender cannot be null");
        Validate.notNull(args, "Arguments cannot be null");
        Validate.notNull(alias, "Alias cannot be null");

        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], WHITELIST_SUBCOMMANDS, new ArrayList<String>(WHITELIST_SUBCOMMANDS.size()));
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove")) {
                final List<String> completions = new ArrayList<String>();
                for (OfflinePlayer player : Bukkit.getOnlinePlayers()) { // Spigot - well maybe sometimes you haven't turned the whitelist on just yet.
                    final String name = player.getName();
                    if (StringUtil.startsWithIgnoreCase(name, args[1]) && (args[0].equalsIgnoreCase("add") ? !player.isWhitelisted() : player.isWhitelisted())) {
                        completions.add(name);
                    }
                }
                return completions;
            }
        }
        return ImmutableList.of();
    }
}
