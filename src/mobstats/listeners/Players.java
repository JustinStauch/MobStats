package mobstats.listeners;

import mobstats.MobStats;

import org.bukkit.Location;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class Players extends PlayerListener {
    
    MobStats plugin;
    
    public Players(MobStats plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!plugin.sendMessage) return;
        Location To = event.getTo();
        Location From = event.getFrom();
        int toL = plugin.level(plugin.closestOriginDistance(To));
        int fromL = plugin.level(plugin.closestOriginDistance(From));
        if (fromL == toL) return;
        event.getPlayer().sendMessage(plugin.message.replace("+level", Integer.toString(toL)));
        
    }
    
    @Override
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!plugin.sendJoinMessage) return;
        int level = plugin.level(plugin.closestOriginDistance(event.getPlayer().getLocation()));
        event.getPlayer().sendMessage(plugin.joinMessage.replace("+level", Integer.toString(level)));
    }
    
    @Override
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (!plugin.sendTpMessage) return;
        int level = plugin.level(plugin.closestOriginDistance(event.getTo()));
        event.getPlayer().sendMessage(plugin.tpMessage.replace("+level", Integer.toString(level)));
    }
    
    @Override
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (!plugin.sendRespawnMessage) return;
        int level = plugin.level(plugin.closestOriginDistance(event.getRespawnLocation()));
        event.getPlayer().sendMessage(plugin.respawnMessage.replace("+level", Integer.toString(level)));
    }
    
    @Override
    public void onPlayerPortal(PlayerPortalEvent event) {
        if (!plugin.sendPortalMessage) return;
        int level = plugin.level(plugin.closestOriginDistance(event.getTo()));
        event.getPlayer().sendMessage(plugin.portalMessage.replace("+level", Integer.toString(level)));
    }
}