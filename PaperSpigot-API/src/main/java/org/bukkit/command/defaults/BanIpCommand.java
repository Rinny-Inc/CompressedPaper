package org.bukkit.command.defaults;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;

import io.noks.IPunishment;

public class BanIpCommand extends VanillaCommand implements IPunishment {
    public static final Pattern ipValidity = Pattern.compile("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    public BanIpCommand() {
        super("ban-ip");
        this.description = "Prevents the specified IP address from using this server";
        this.usageMessage = "/ban-ip (-s) <address|player> [reason ...]";
        this.setAliases(Arrays.asList(new String[] { "ipban", "banip", "blacklist" }));
        this.setPermission("bukkit.command.ban.ip");
    }

    @Override
    public boolean execute(CommandSender sender, String currentAlias, String[] args) {
        if (!testPermission(sender)) return true;
        if (args.length < 1)  {
            sender.sendMessage(ChatColor.RED + "Usage: " + usageMessage);
            return false;
        }
        boolean silent = args[0].equalsIgnoreCase("-s") || args[0].equalsIgnoreCase("-silent");
        final int playerPos = !silent ? 0 : 1;
        
        if (args.length <= playerPos) {
            sender.sendMessage(ChatColor.RED + "Please specify an IP address or player.");
            return false;
        }
        
        String target = args[playerPos];
        boolean isTemporary = args.length > playerPos + 1 && isValidDuration(args[playerPos + 1]);
        Date duration = isTemporary ? parseDuration(args[playerPos + 1]) : null;
        String reason = args.length > playerPos + (isTemporary ? 2 : 1) ? StringUtils.join(args, ' ', playerPos + (isTemporary ? 2 : 1), args.length) : "Banned by an operator.";
        
        if (ipValidity.matcher(target).matches()) {
            processIPBan(target, sender, reason, duration, silent, isTemporary);
        } else {
        	final Player player = Bukkit.getPlayer(target);

            if (player == null) {
                sender.sendMessage(ChatColor.RED + "Usage: " + usageMessage);
                return false;
            }

            processIPBan(player.getAddress().getAddress().getHostAddress(), sender, reason, duration, silent, isTemporary);
        }
        return true;
    }

    private void processIPBan(String ip, CommandSender sender, String reason, Date date, boolean silent, boolean isTemporary) {
        Bukkit.getBanList(BanList.Type.IP).addBan(ip, reason, date, sender.getName());

        String banned = null;
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd \'at\' HH:mm:ss z");
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getAddress().getAddress().getHostAddress().equals(ip)) {
            	banned = player.getName();
                player.kickPlayer("You have been blacklisted from this server!" + 
						  "\nSource: " + sender.getName() +
						  "\nReason: " + reason +
						  (isTemporary ? "\nYour ban will be removed on " + sdf.format(date) : ""));
            }
        }
        final String msg = (silent ? ChatColor.GRAY + "(SILENT) " : "") + ChatColor.GREEN + sender.getName() + " blacklisted " + (banned == null ? ip : banned);
        if (!silent) {
        	Bukkit.broadcastMessage(msg);
        	return;
        }
        Command.broadcastCommandMessage(sender, msg);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        Validate.notNull(sender, "Sender cannot be null");
        Validate.notNull(args, "Arguments cannot be null");
        Validate.notNull(alias, "Alias cannot be null");

        if (args.length == 1) {
            return super.tabComplete(sender, alias, args);
        }
        return ImmutableList.of();
    }
}
