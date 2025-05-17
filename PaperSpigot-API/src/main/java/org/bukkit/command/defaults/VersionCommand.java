package org.bukkit.command.defaults;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.util.StringUtil;

import com.google.common.collect.ImmutableList;

public class VersionCommand extends BukkitCommand {
    public VersionCommand(String name) {
        super(name);

        this.description = "Gets the version of this server including any plugins in use";
        this.usageMessage = "/version [plugin name]";
        this.setPermission("bukkit.command.version");
        this.setAliases(Arrays.asList("ver", "about"));
    }
    private Random rand = new Random();

    @Override
    public boolean execute(CommandSender sender, String currentAlias, String[] args) {
        if (!testPermission(sender)) return true;

        if (args.length == 0) {
        	final String[] msg = {
        		"This server is running " + ChatColor.LIGHT_PURPLE + Bukkit.getName() + ChatColor.RESET + " version " + ChatColor.LIGHT_PURPLE + Bukkit.getVersion() + ChatColor.RESET + " (Implementing API version " + Bukkit.getBukkitVersion() + ")", 
        		ChatColor.GREEN + "This Software is maintained by Noksio",
        		null
        	};
        	int i = rand.nextInt(100);
        	if (i == 10) {
        		msg[2] = ChatColor.LIGHT_PURPLE + "Rinny " + ChatColor.RESET + "is Noksio's cat!";
        	}
        	// KEEP THIS HERE: This Software is not maintained by Noksio since 27/07/2024
        	// WATCHER AS BEEN REMOVED FROM THE CODE TO NOT SKID!
            sender.sendMessage(Arrays.stream(msg).filter(Objects::nonNull).toArray(String[]::new));
            return true;
        }
        final StringBuilder name = new StringBuilder();

        for (String arg : args) {
            if (name.length() > 0) {
                name.append(' ');
            }

            name.append(arg);
        }

        String pluginName = name.toString();
        final Plugin exactPlugin = Bukkit.getPluginManager().getPlugin(pluginName);
        if (exactPlugin != null) {
            describeToSender(exactPlugin, sender);
            return true;
        }

        pluginName = pluginName.toLowerCase();
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if (plugin.getName().toLowerCase().contains(pluginName)) {
                describeToSender(plugin, sender);
                return true;
            }
        }
        
        sender.sendMessage("This server is not running any plugin by that name.");
        sender.sendMessage("Use /plugins to get a list of plugins.");
        return false;
    }

    private void describeToSender(Plugin plugin, CommandSender sender) {
        final PluginDescriptionFile desc = plugin.getDescription();
        sender.sendMessage(ChatColor.GREEN + desc.getName() + ChatColor.WHITE + " version " + ChatColor.GREEN + desc.getVersion());

        if (desc.getDescription() != null) {
            sender.sendMessage(desc.getDescription());
        }

        if (desc.getWebsite() != null) {
            sender.sendMessage("Website: " + ChatColor.GREEN + desc.getWebsite());
        }

        if (!desc.getAuthors().isEmpty()) {
        	sender.sendMessage("Author" + (desc.getAuthors().size() > 1 ? "s" : "") + ": " + getAuthors(desc));
        }
    }

    private String getAuthors(final PluginDescriptionFile desc) {
        final StringBuilder result = new StringBuilder();
        final List<String> authors = desc.getAuthors();

        for (int i = 0; i < authors.size(); i++) {
            if (result.length() > 0) {
                result.append(ChatColor.WHITE);
                result.append((i < authors.size() - 1 ? ", " : " and "));
            }

            result.append(ChatColor.GREEN);
            result.append(authors.get(i));
        }

        return result.toString();
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        Validate.notNull(sender, "Sender cannot be null");
        Validate.notNull(args, "Arguments cannot be null");
        Validate.notNull(alias, "Alias cannot be null");

        if (args.length == 1) {
            final List<String> completions = new ArrayList<String>();
            final String toComplete = args[0].toLowerCase();
            for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                if (StringUtil.startsWithIgnoreCase(plugin.getName(), toComplete)) {
                    completions.add(plugin.getName());
                }
            }
            return completions;
        }
        return ImmutableList.of();
    }
}
