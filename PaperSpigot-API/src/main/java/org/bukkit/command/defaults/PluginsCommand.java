package org.bukkit.command.defaults;

import java.util.Arrays;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class PluginsCommand extends BukkitCommand {
    public PluginsCommand(String name) {
        super(name);
        this.description = "Gets a list of plugins running on the server";
        this.usageMessage = "/plugins";
        this.setPermission("bukkit.command.plugins");
        this.setAliases(Arrays.asList("pl"));
    }

    @Override
    public boolean execute(CommandSender sender, String currentAlias, String[] args) {
        if (!testPermission(sender)) return true;
        sender.sendMessage("Plugins " + getPluginList());
        return true;
    }

    private String getPluginList() {
    	final Map<String, ChatColor> plugins = new TreeMap<String, ChatColor>(String.CASE_INSENSITIVE_ORDER);
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            plugins.put(plugin.getDescription().getName(), plugin.isEnabled() ? ChatColor.GREEN : ChatColor.RED);
        }
        final StringJoiner pluginList = new StringJoiner(ChatColor.RESET + ", ");
        for (Map.Entry<String, ChatColor> entry : plugins.entrySet()) {
            pluginList.add(entry.getValue() + entry.getKey());
        }

        return "(" + plugins.size() + "): " + pluginList.toString();
    }

    // Spigot Start
    @Override
    public java.util.List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        return java.util.Collections.emptyList();
    }
    // Spigot End
}
