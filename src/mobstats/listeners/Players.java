package mobstats.listeners;

import java.util.ArrayList;
import java.util.List;
import mobstats.MobStats;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;

public class Players extends PlayerListener {
    
    MobStats plugin;
    
    public Players(MobStats plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void onPlayerMove(PlayerMoveEvent event) {
        Location To = event.getTo();
        Location From = event.getFrom();
        Vector spawn = plugin.spawns.get(event.getPlayer().getWorld()).toVector();
        int toL = plugin.level(spawn.distance(To.toVector()));
        int fromL = plugin.level(spawn.distance(From.toVector()));
        if (fromL == toL) return;
        event.getPlayer().sendMessage(plugin.message.replace("+level", Integer.toString(toL)));
        
    }
    
    @Override
    public void onPlayerJoin(PlayerJoinEvent event) {
        Vector spawn = plugin.spawns.get(event.getPlayer().getWorld()).toVector();
        double dis = spawn.distance(event.getPlayer().getLocation().toVector());
        int level = plugin.level(dis);
        event.getPlayer().sendMessage("You have just joined into a level " + level + " zone");
    }
    
    @Override
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Vector spawn = plugin.spawns.get(event.getPlayer().getWorld()).toVector();
        double dis = spawn.distance(event.getPlayer().getLocation().toVector());
        int level = plugin.level(dis);
        event.getPlayer().sendMessage("You have just teleported into a level " + level + " zone");
    }
    
    @Override
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Vector spawn = plugin.spawns.get(event.getPlayer().getWorld()).toVector();
        double dis = spawn.distance(event.getPlayer().getLocation().toVector());
        int level = plugin.level(dis);
        event.getPlayer().sendMessage("You have just respawned into a level " + level + " zone");
    }
    
    @Override
    public void onPlayerPortal(PlayerPortalEvent event) {
        Vector spawn = plugin.spawns.get(event.getPlayer().getWorld()).toVector();
        double dis = spawn.distance(event.getTo().toVector());
        int level = plugin.level(dis);
        event.getPlayer().sendMessage("You have just moved into a level " + level + " zone");
    }
    
    public void send(String name, int level) {
        if (plugin.console) System.out.println(name + " just moved into a level " + level + " zone");
        if (plugin.op) {
            List<World> worlds = plugin.getServer().getWorlds();
            List<Player> players = new ArrayList<Player>();
            for (int x = 0; x < worlds.size(); x++) {
                List<Player> player = worlds.get(x).getPlayers();
                for (int y = 0; y < player.size(); y++) {
                    if (player.get(y).isOp()) players.add(player.get(y));
                }
            }
            for (int z = 0; z < players.size(); z++) {
                players.get(z).sendMessage(name + " just moved into a level " + level + " zone");
            }
        }
    }
}