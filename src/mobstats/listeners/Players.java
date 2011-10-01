package mobstats.listeners;

import mobstats.MobStats;

import org.bukkit.Location;
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
        double toL = plugin.level(spawn.distance(To.toVector()));
        double fromL = plugin.level(spawn.distance(From.toVector()));
        if (fromL == toL) return;
        event.getPlayer().sendMessage("You are now in a level " + toL + " zone");
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
}