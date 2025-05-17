package org.spigotmc;

import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import com.sun.management.OperatingSystemMXBean;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class TicksPerSecondCommand extends Command {
	private long startTime = System.currentTimeMillis();
	private final DecimalFormat df = new DecimalFormat("#.##");
	private final OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
	private final Runtime runtime = Runtime.getRuntime();

    public TicksPerSecondCommand(String name) {
        super( name );
        this.description = "Gets the current ticks per second for the server";
        this.usageMessage = "/tps";
        this.setPermission( "bukkit.command.tps" );
    }

    @Override
    public boolean execute(CommandSender sender, String currentAlias, String[] args) {
        if (!testPermission(sender)) {
            return true;
        }
        
        final double usedMemory = (runtime.totalMemory() - runtime.freeMemory());
        final double freeMemory = (runtime.maxMemory() - usedMemory);
        final double processCpuLoad = osBean.getProcessCpuLoad() * 100;
        
        sender.sendMessage(ChatColor.GOLD + "Java Version: " + ChatColor.YELLOW + Runtime.version());
        sender.sendMessage(ChatColor.GOLD + "Memory: " + ChatColor.YELLOW + formatMem(usedMemory) + ChatColor.GRAY + "/" + ChatColor.YELLOW + formatMem(runtime.maxMemory()) + ChatColor.GRAY + " (" + ChatColor.RED + formatMem(freeMemory) + " free" + ChatColor.GRAY + ")");
        sender.sendMessage(ChatColor.GOLD + "Process CPU Load: " + ChatColor.YELLOW + df.format(processCpuLoad) + "%");
        sender.sendMessage(ChatColor.GOLD + "Uptime: " + ChatColor.YELLOW + formatFullMilis(System.currentTimeMillis() - this.startTime));
        return true;
    }
    
    private String formatMem(double mem) { 
    	return ChatColor.RED.toString() + Math.round(mem / 1024.0D / 1024.0D) + "MB"; 
    }
    
    // Rinny start
    private String formatFullMilis(Long millis) {
        final double seconds = Math.max(0L, millis) / 1000.0;
        final double[] timeUnits = {31536000.0, 2592000.0, 604800.0, 86400.0, 3600.0, 60.0, 1.0};
        final String[] unitNames = {"year", "month", "week", "day", "hour", "minute", "second"};
        
        for (int i = 0; i < timeUnits.length; i++) {
            double unitValue = seconds / timeUnits[i];
            if (unitValue >= 1.0) {
                return df.format(unitValue) + " " + unitNames[i] + (unitValue != 1.0 ? "s" : "");
            }
        }
        return "0 seconds";
    }
    // Rinny end
}
