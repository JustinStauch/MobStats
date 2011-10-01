package mobstats.listeners;

import mobstats.MobStats;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Commands implements CommandExecutor {
    MobStats plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command cmnd, String commander, String[] args) {
        if (commander.equalsIgnoreCase("zone")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                Vector spawn = plugin.spawns.get(player.getWorld()).toVector();
                double dis = spawn.distance(player.getLocation().toVector());
                int level = plugin.level(dis);
                player.sendMessage("You are in a level " + level + " zone");
                return true;
            }
        }
        return true;
    }
    
}
