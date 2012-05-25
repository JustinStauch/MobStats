package mobstats.listeners;

import mobstats.MobStats;

import org.bukkit.EntityEffect;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

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
     * Handles when an Entity damages an Entity.
     * 
     * @param event The EntityDamageByEntityEvent that was thrown.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            if (!plugin.isAffected(event.getDamager().getType())) return;
            event.setDamage(plugin.damage(plugin.getLevel(event.getDamager())));
        }
        if (!(event.getEntity() instanceof Player)) {
            if (!plugin.isAffected(event.getEntity().getType())) return;
            if (event.getEntity() instanceof LivingEntity) {
                plugin.subtractHealth((LivingEntity) event.getEntity(), event.getDamage(), event.getDamager());
                event.setDamage(0);
                event.getEntity().playEffect(EntityEffect.HURT);
                event.getEntity().setLastDamageCause(event);
            }
        }
    }
    
    /**
     * Handles when an Entity dies.
     * 
     * @param event The EntityDeathEvent that was thrown.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeathEvent(EntityDeathEvent event) {
        if (event.getEntity() instanceof Player) return;
        plugin.dropItems(event);
        if (!plugin.isAffected(event.getEntity().getType())) return;
        event.setDroppedExp(plugin.xp(plugin.getLevel(event.getEntity())));
    }
    
    /**
     * Logs the Entity's level when it spawns.
     * 
     * @param event The CreatureSpawnEvent that was thrown.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCreatureSpawnEvent(CreatureSpawnEvent event) {
        if (event.getEntity() instanceof Player) return;
        plugin.setLevel(event.getEntity());
        event.getEntity().setHealth(event.getEntity().getMaxHealth());
        plugin.setHealth(event.getEntity(), plugin.health(plugin.getLevel(event.getEntity())));
    }
}