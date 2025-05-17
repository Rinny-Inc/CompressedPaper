package io.noks.command;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.UnmodifiableIterator;

import net.minecraft.server.NetworkManager;

public class ProtocolCommand extends Command {
	private final Map<Integer, String> protocolNames;

	public ProtocolCommand() {
		super("protocol");
		setAliases(Arrays.asList(new String[] { "proto" }));
		this.description = "Info about protocols in use";
		this.usageMessage = "/protocol [player]";
		setPermission("op");
		this.protocolNames = new HashMap<Integer, String>();
		this.protocolNames.put(Integer.valueOf(4), "1.7.2");
		this.protocolNames.put(Integer.valueOf(5), "1.7.10");
		this.protocolNames.put(Integer.valueOf(47), "1.8");
		this.protocolNames.put(Integer.valueOf(107), "1.9");
		this.protocolNames.put(Integer.valueOf(108), "1.9.1");
		this.protocolNames.put(Integer.valueOf(109), "1.9.2");
		this.protocolNames.put(Integer.valueOf(110), "1.9.4");
		this.protocolNames.put(Integer.valueOf(210), "1.10");
	}

	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		if (!testPermission(sender))
			return true;
		if (args.length == 0) {
			Map<Integer, Integer> counts = new HashMap<Integer, Integer>();
			for (Player player : Bukkit.getOnlinePlayers()) {
				int version = player.getProtocolVersion();
				if (counts.containsKey(version)) {
					counts.put(version, counts.get(version) + 1);
					continue;
				}
				counts.put(version, 1);
			}
			for (UnmodifiableIterator<Integer> unmodifiableIterator = NetworkManager.SUPPORTED_VERSIONS.iterator(); unmodifiableIterator.hasNext();) {
				int version = unmodifiableIterator.next();
				if (counts.containsKey(version)) {
					int count = counts.get(version);
					sender.sendMessage(ChatColor.YELLOW + "Protocol " + version + " (" + this.protocolNames.get(version) + "): " + count + " " + ((count == 1) ? "player" : "players"));
				}
			}
		} else {
			Player player = Bukkit.getServer().getPlayer(args[0]);
			if (player == null) {
				sender.sendMessage(ChatColor.RED + "No such player: " + args[0]);
				return true;
			}
			int version = player.getProtocolVersion();
			sender.sendMessage(ChatColor.YELLOW + player.getName() + " is using protocol " + version + " (" + this.protocolNames.get(version) + ")");
		}
		return true;
	}
}
