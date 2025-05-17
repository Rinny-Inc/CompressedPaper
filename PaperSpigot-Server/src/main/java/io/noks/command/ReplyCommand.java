package io.noks.command;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.EntityPlayer;

public class ReplyCommand extends Command {
	public ReplyCommand(String name) {
        super(name);
        this.description = "Reply to the last player that messaged you";
        this.usageMessage = "/reply <message>";
        this.setPermission("bukkit.command.tell");
        this.setAliases(Arrays.asList("r"));
    }

    @Override
    public boolean execute(CommandSender sender, String currentAlias, String[] args) {
    	if (!(sender instanceof Player)) return false;
        if (!testPermission(sender)) return true;
        final EntityPlayer senderPlayer = ((CraftPlayer) sender).getHandle();
        if (senderPlayer.getMessagedUUID() == null) {
        	sender.sendMessage(ChatColor.RED + "You have no one to reply to.");
        	return false;
        }
        if (args.length == 0)  {
            sender.sendMessage(ChatColor.RED + "Usage: " + usageMessage);
            return false;
        }
        final Player player = Bukkit.getPlayer(senderPlayer.getMessagedUUID());
        
        if (player == null) {
        	senderPlayer.setMessaged(null);
            sender.sendMessage(ChatColor.RED + "You have no one to reply to.");
            return false;
        }
        final StringJoiner message = new StringJoiner(" ");
        for (int i = 0; i < args.length; i++) {
            message.add(args[i]);
        }
        final String result = ChatColor.LIGHT_PURPLE + "(From " + senderPlayer.displayName + ChatColor.LIGHT_PURPLE + ") " + message;
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "(To " + player.getDisplayName() + ChatColor.LIGHT_PURPLE + ") " + message);
        player.sendMessage(result);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        return Collections.emptyList();
    }
}
