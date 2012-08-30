package mobstats.listeners;

import mobstats.MobStats;
import mobstats.entities.StatsEntity;
import mobstats.entities.StatsEntityBlaze;
import net.minecraft.server.World;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftBlaze;
import org.bukkit.craftbukkit.entity.CraftEntity;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
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
        event.getPlayer().sendMessage(plugin.getMessage().replace("-level", String.valueOf(endLevel)));
    }
    
    /**
     * Handles when a Player moves by sending a message if it is supposed to.
     * 
     * @param event The PlayerJoinEvent that was thrown.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (plugin.sendKillMessage()) {
            plugin.usesNotifications(event.getPlayer());
        }
        Location loco = event.getPlayer().getLocation();
        World world = ((CraftWorld) loco.getWorld()).getHandle();
        if (!plugin.sendJoinMessage()) return;
        event.getPlayer().sendMessage(plugin.getJoinMessage().replace("-level", String.valueOf(plugin.level(plugin.closestOriginDistance(event.getPlayer().getLocation())))));
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
        event.getPlayer().sendMessage(plugin.getPortalMessage().replace("-level", String.valueOf(plugin.level(plugin.closestOriginDistance(event.getPlayer().getLocation())))));
    }
    
    /**
     * Handles when a Player respawns by sending a message if it is supposed to.
     * 
     * @param event The PlayerRespawnEvent that was thrown.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (!plugin.sendRespawnMessage()) return;
        event.getPlayer().sendMessage(plugin.getRespawnMessage().replace("-level", String.valueOf(plugin.level(plugin.closestOriginDistance(event.getRespawnLocation())))));
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
        event.getPlayer().sendMessage(plugin.getTpMessage().replace("-level", String.valueOf(plugin.level(plugin.closestOriginDistance(event.getPlayer().getLocation())))));
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        EntityDamageEvent ev = event.getEntity().getLastDamageCause();
        if (!(ev instanceof EntityDamageByEntityEvent)) {
            return;
        }
        if (!plugin.sendDeathMessage()) {
            return;
        }
        Entity damager = ((EntityDamageByEntityEvent) ev).getDamager();
        if (!(((CraftEntity) damager).getHandle() instanceof StatsEntity)) {
            return;
        }
        if (damager instanceof Projectile) {
            damager = ((Projectile) damager).getShooter();
        }
        if (plugin.isAffected(damager.getType())) {
            String message = plugin.getDeathMessage();
            message = message.replaceAll("-mob", damager.getType().toString());
            message = message.replaceAll("-player", event.getEntity().getDisplayName());
            message = message.replaceAll("-level", String.valueOf(((StatsEntity) ((CraftEntity) damager).getHandle()).getLevel()));
            event.setDeathMessage(message);
        }
    }
}