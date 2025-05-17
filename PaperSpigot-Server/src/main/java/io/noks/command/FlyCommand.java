package io.noks.command;

import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FlyCommand extends Command {
	public FlyCommand(String name) {
        super(name);
        this.description = "Allow flight for a precise player";
        this.usageMessage = "/fly (player)";
        this.setPermission("bukkit.command.fly");
    }

    @Override
    public boolean execute(CommandSender sender, String currentAlias, String[] args) {
    	if (!(sender instanceof Player)) return false;
        if (!testPermission(sender)) return true;
        if (args.length > 1) {
        	sender.sendMessage(ChatColor.RED + "Usage: " + this.usageMessage);
        	return false;
        }
        if (args.length == 0)  {
            Player player = (Player) sender;
            
            player.setAllowFlight(!player.getAllowFlight());
            player.setFlying(player.getAllowFlight());
            
            player.sendMessage(ChatColor.YELLOW + "Fly " + (player.isFlying() ? "" : "un") + "toggled!");
            return true;
        }
        if (!sender.hasPermission(this.getPermission() + ".other")) {
        	sender.sendMessage(ChatColor.RED + "No permission!");
        	return false;
        }
        final Player target = Bukkit.getPlayer(args[0]);
        
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Target is null.");
            return false;
        }
        
        target.setAllowFlight(!target.getAllowFlight());
        target.setFlying(target.getAllowFlight());

        target.sendMessage(ChatColor.YELLOW + "Flight toggled by " + sender.getName());
        sender.sendMessage(ChatColor.YELLOW + "Flight toggled for " + target.getName());
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
    	if (args.length == 1) {
    		return super.tabComplete( sender, alias, args );
    	}
        return Collections.emptyList();
    }
}
