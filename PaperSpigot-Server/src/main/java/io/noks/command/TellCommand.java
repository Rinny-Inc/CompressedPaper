package io.noks.command;

import java.util.Arrays;
import java.util.StringJoiner;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.VanillaCommand;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.EntityPlayer;

public class TellCommand extends VanillaCommand {
    public TellCommand() {
        super("tell");
        this.description = "Sends a private message to the given player";
        this.usageMessage = "/tell <player> <message>";
        this.setAliases(Arrays.asList(new String[] { "w", "msg" }));
        this.setPermission("bukkit.command.tell");
    }

    @Override
    public boolean execute(CommandSender sender, String currentAlias, String[] args) {
    	if (!(sender instanceof Player)) return false;
        if (!testPermission(sender)) return true;
        if (args.length < 2)  {
            sender.sendMessage(ChatColor.RED + "Usage: " + usageMessage);
            return false;
        }
        final Player targetPlayer = Bukkit.getPlayerExact(args[0]);
        if (targetPlayer == null) {
            sender.sendMessage(ChatColor.RED + "There's no player by that name online.");
            return false;
        }
        final EntityPlayer player = ((CraftPlayer)targetPlayer).getHandle();
        final EntityPlayer senderPlayer = ((CraftPlayer) Bukkit.getPlayer(sender.getName())).getHandle();
        if (player.getUniqueID() == senderPlayer.getUniqueID()) {
        	sender.sendMessage(ChatColor.RED + "You can't send yourself a message.");
        	return false;
        }
        final StringJoiner message = new StringJoiner(" ");
        for (int i = 1; i < args.length; i++) {
            message.add(args[i]);
        }
        senderPlayer.setMessaged(player.getUniqueID());
        player.setMessaged(senderPlayer.getUniqueID());
        final String result = ChatColor.LIGHT_PURPLE + "(From " + senderPlayer.displayName + ChatColor.LIGHT_PURPLE + ") " + message;
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "(To " + player.displayName + ChatColor.LIGHT_PURPLE + ") " + message);
        player.getBukkitEntity().sendMessage(result);
        return true;
    }

    // Spigot Start
    @Override
    public java.util.List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        if ( args.length == 1 ) {
            return super.tabComplete( sender, alias, args );
        }
        return java.util.Collections.emptyList();
    }
    // Spigot End
}
