package org.bukkit.command.defaults;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

import com.google.common.collect.ImmutableList;

public class KillCommand extends VanillaCommand {
    public KillCommand() {
        super("kill");
        this.description = "Commits suicide, only usable as a player";
        this.usageMessage = "/kill <player>";
        this.setPermission("bukkit.command.kill");
    }

    @Override
    public boolean execute(CommandSender sender, String currentAlias, String[] args) {
        if (!(sender instanceof Player)) {
        	sender.sendMessage("You can only perform this command as a player");
        	return false;
        }
        if (!testPermission(sender)) return true;
        if (args.length > 1) {
        	sender.sendMessage(ChatColor.RED + "Usage: " + this.usageMessage);
        	return false;
        }
        if (sender.hasPermission("bukkit.command.kill.other") && args.length == 1) {
	        final Player target = Bukkit.getPlayer(args[0]);
	        if (target == null) {
	        	return false;
	        }
	        if (target.isDead()) {
	        	return false;
	        }
	        this.killPlayer(target);
	        return true;
        }
        final Player player = (Player) sender;
        if (player.isDead()) {
        	return false;
        }
        this.killPlayer(player);
        return true;
    }
    
    private void killPlayer(Player player) {
    	final EntityDamageEvent ede = new EntityDamageEvent(player, EntityDamageEvent.DamageCause.SUICIDE, 1000);
        Bukkit.getPluginManager().callEvent(ede);
        if (ede.isCancelled()) return;

        player.setHealth(0);
        //player.sendMessage("Outch!");
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
