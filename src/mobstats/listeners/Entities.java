package mobstats.listeners;

import mobstats.MobStats;

import org.bukkit.EntityEffect;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
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
        Entity damager;
        if (event.getDamager() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getDamager();
            damager = arrow.getShooter();
        }
        else damager = event.getDamager();
        if (!(damager instanceof Player)) {
            if (!plugin.isAffected(damager.getType())) return;
            int damage = event.getDamage();
            event.setDamage(plugin.damage(plugin.getLevel(damager), damage));
        }
        if (!(event.getEntity() instanceof Player)) {
            if (!plugin.isAffected(event.getEntity().getType())) return;
            if (plugin.isInvincible(event.getEntity())) {
                return;
            }
            if (event.getEntity() instanceof LivingEntity) {
                if (plugin.isInvincible(event.getEntity())) {
                    return;
                }
                plugin.gotHit(event.getEntity());
                LivingEntity ent = (LivingEntity) event.getEntity();
                boolean died = plugin.subtractHealth(ent, event.getDamage(), event.getDamager());
                event.setDamage(-1);
                event.getEntity().setLastDamageCause(event);
                if ((ent.isDead() || ent.getHealth() == 0) && !died) {
                    ent.setHealth(ent.getMaxHealth());
                }
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
        int xp = event.getDroppedExp();
        event.setDroppedExp(plugin.xp(plugin.getLevel(event.getEntity()), xp));
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
        plugin.setHealth(event.getEntity(), plugin.health(plugin.getLevel(event.getEntity()), event.getEntity().getMaxHealth()));
    }
}