package mobstats.listeners;

import mobstats.MobStats;
import mobstats.entities.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

/**
 * Handles all entity related events.
 * 
 * @author Justin Stauch
 * @since April 2, 2012
 * 
 * copyright 2012© Justin Stauch, All Rights Reserved
 */
public class Entities implements Listener {
    private MobStats plugin;
    
    public Entities(MobStats plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Logs the Entity's level when it spawns.
     * 
     * @param event The CreatureSpawnEvent that was thrown.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        boolean egg = false;
        if (event.getSpawnReason().equals(SpawnReason.EGG)) {
            egg = true;
        }
        if (event.isCancelled()) {
            return;
        }
        if ((((CraftEntity) event.getEntity()).getHandle() instanceof StatsEntity) && !egg) {
            return;
        }
        plugin.replaceEntity(event.getEntity(), event.getSpawnReason(), false);
        event.setCancelled(true);
        if (event.getSpawnReason().equals(SpawnReason.BUILD_IRONGOLEM)) {
            Location loco = event.getLocation();
            loco.getBlock().setType(Material.AIR);
            loco.setY(loco.getY() + 1);
            loco.getBlock().setType(Material.AIR);
            loco.setY(loco.getY() + 1);
            loco.getBlock().setType(Material.AIR);
            loco.setY(loco.getY() - 1);
            Location side1 = new Location(loco.getWorld(), loco.getX() + 1, loco.getY(), loco.getZ());
            Location side2 = new Location(loco.getWorld(), loco.getX() - 1, loco.getY(), loco.getZ());
            if (side1.getBlock().getTypeId() == Material.IRON_BLOCK.getId() && side2.getBlock().getTypeId() == Material.IRON_BLOCK.getId()) {
                side1.getBlock().setType(Material.AIR);
                side2.getBlock().setType(Material.AIR);
            }
            else {
                side1 = new Location(loco.getWorld(), loco.getX(), loco.getY(), loco.getZ() + 1);
                side2 = new Location(loco.getWorld(), loco.getX(), loco.getY(), loco.getZ() - 1);
                side1.getBlock().setType(Material.AIR);
                side2.getBlock().setType(Material.AIR);
            }
        }
        if (event.getSpawnReason().equals(SpawnReason.BUILD_SNOWMAN)) {
            Location loco = event.getLocation();
            loco.getBlock().setType(Material.AIR);
            loco.setY(loco.getY() + 1);
            loco.getBlock().setType(Material.AIR);
            loco.setY(loco.getY() + 1);
            loco.getBlock().setType(Material.AIR);
        }
        if (event.getSpawnReason().equals(SpawnReason.BUILD_WITHER)) {
            Location loco = event.getLocation();
            loco.getBlock().setType(Material.AIR);
            loco.setY(loco.getY() + 1);
            loco.getBlock().setType(Material.AIR);
            loco.setY(loco.getY() + 1);
            loco.getBlock().setType(Material.AIR);
            loco.setY(loco.getY() - 1);
            Location side1 = new Location(loco.getWorld(), loco.getX() + 1, loco.getY(), loco.getZ());
            Location side2 = new Location(loco.getWorld(), loco.getX() - 1, loco.getY(), loco.getZ());
            if (side1.getBlock().getTypeId() == Material.SOUL_SAND.getId() && side2.getBlock().getTypeId() == Material.SOUL_SAND.getId()) {
                side1.getBlock().setType(Material.AIR);
                side2.getBlock().setType(Material.AIR);
                side1.setY(side1.getY() + 1);
                side2.setY(side2.getY() + 1);
                side1.getBlock().setType(Material.AIR);
                side2.getBlock().setType(Material.AIR);
            }
            else {
                side1 = new Location(loco.getWorld(), loco.getX(), loco.getY(), loco.getZ() + 1);
                side2 = new Location(loco.getWorld(), loco.getX(), loco.getY(), loco.getZ() - 1);
                side1.getBlock().setType(Material.AIR);
                side2.getBlock().setType(Material.AIR);
                side1.setY(side1.getY() + 1);
                side2.setY(side2.getY() + 1);
                side1.getBlock().setType(Material.AIR);
                side2.getBlock().setType(Material.AIR);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onCreatureSpawnMonitor(CreatureSpawnEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (((CraftEntity) event.getEntity()).getHandle() instanceof StatsEntity) {
            return;
        }
        net.minecraft.server.World world = ((CraftWorld) event.getEntity().getWorld()).getHandle();
        world.removeEntity(((CraftEntity) event.getEntity()).getHandle());
    }
}