package mobstats.listeners;

import mobstats.MobStats;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor {
    MobStats plugin;
    
    public Commands(MobStats plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmnd, String commander, String[] args) {
        if (commander.equalsIgnoreCase("zone")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                int level = plugin.level(plugin.closestOriginDistance(player.getLocation()));
                player.sendMessage("You are in a level " + level + " zone");
                return true;
            }
        }
        return true;
    }
}