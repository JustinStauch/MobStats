package mobstats.listeners;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import mobstats.MobStats;
import mobstats.entities.StatsEntity;
import net.minecraft.server.Entity;
import net.minecraft.server.World;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;

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
        if (commander.equalsIgnoreCase("MobStats") || commander.equalsIgnoreCase("ms")) {
            if (!(sender instanceof Player)) {
                return true;
            }
            Player player = (Player) sender;
            if (args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("true")) {
                plugin.setUseNotifications(player, true);
            }
            else if (args[0].equalsIgnoreCase("off") || args[0].equalsIgnoreCase("false")) {
                plugin.setUseNotifications(player, false);
            }
            else {
                return false;
            }
            return true;
        }
        if (commander.equalsIgnoreCase("replaceall") || commander.equalsIgnoreCase("ra")) {
            if (sender instanceof Player) {
                if (!((Player) sender).isOp()) {
                    sender.sendMessage(ChatColor.RED + "You can't use this command.");
                    return true;
                }
            }
            sender.sendMessage(ChatColor.GRAY + "Replacing all...");
            plugin.replaceAllWrongEntities();
            sender.sendMessage(ChatColor.GRAY + "Finished replacing");
        }
        return true;
    }
}
