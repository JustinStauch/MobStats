package mobstats.listeners;

import mobstats.MobStats;
import org.bukkit.ChatColor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handles the plugin's command.
 * 
 * @author Justin Stauch
 * @since May 20, 2012
 * 
 * copyright 2012© Justin Stauch, All Rights Reserved
 */
public class Commands implements CommandExecutor {

    MobStats plugin;
    
    public Commands(MobStats plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commander, String[] args) {
        if (commander.equalsIgnoreCase("zone")) {
            if (!(sender instanceof Player)) return true;
            sender.sendMessage("Your current zone is " + plugin.level(plugin.closestOriginDistance(((Player) sender).getLocation())));
            return true;
        }
        return true;
    }
}
