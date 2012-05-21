package mobstats.listeners;

import mobstats.MobStats;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * Handles all Player events which is the moving between zones.
 * 
 * @author Justin Stauch
 * @since May 20, 2011
 * 
 * copyright 2012© Justin Stauch, All Rights Reserved
 */
public class Players implements Listener{
    
    MobStats plugin;
    
    public Players (MobStats plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Handles events for when a Player moves by sending a message if it is supposed to.
     * 
     * @param event The PlayerMoveEvent that was thrown.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.isCancelled()) return;
        if (!plugin.sendMessage()) return;
        int startLevel = plugin.level(plugin.closestOriginDistance(event.getFrom()));
        int endLevel = plugin.level(plugin.closestOriginDistance(event.getTo()));
        if (endLevel == startLevel) return;
        event.getPlayer().sendMessage(plugin.getMessage().replace("+level", String.valueOf(endLevel)));
    }
    
    /**
     * Handles when a Player moves by sending a message if it is supposed to.
     * 
     * @param event The PlayerJoinEvent that was thrown.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!plugin.sendJoinMessage()) return;
        event.getPlayer().sendMessage(plugin.getJoinMessage().replace("+level", String.valueOf(plugin.level(plugin.closestOriginDistance(event.getPlayer().getLocation())))));
    }
    
    /**
     * Handles when a Player moves through a portal by sending a message if it is supposed to.
     * 
     * @param event The PlayerPortalEvent that was thrown.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerPortalEvent(PlayerPortalEvent event) {
        if (event.isCancelled()) return;
        if (!plugin.sendPortalMessage()) return;
        event.getPlayer().sendMessage(plugin.getPortalMessage().replace("+level", String.valueOf(plugin.level(plugin.closestOriginDistance(event.getPlayer().getLocation())))));
    }
    
    /**
     * Handles when a Player respawns by sending a message if it is supposed to.
     * 
     * @param event The PlayerRespawnEvent that was thrown.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (!plugin.sendRespawnMessage()) return;
        event.getPlayer().sendMessage(plugin.getRespawnMessage().replace("+level", String.valueOf(plugin.level(plugin.closestOriginDistance(event.getPlayer().getLocation())))));
    }
    
    /**
     * Handles when a Player teleports by sending a message if it is supposed to.
     * 
     * @param event THe PlayerTeleportEvent that was thrown.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.isCancelled()) return;
        if (!plugin.sendTpMessage()) return;
        event.getPlayer().sendMessage(plugin.getTpMessage().replace("+level", String.valueOf(plugin.level(plugin.closestOriginDistance(event.getPlayer().getLocation())))));
    }
}