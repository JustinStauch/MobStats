package mobstats.listeners;

import java.util.UUID;

import mobstats.MobStats;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;

public class Entities extends EntityListener {
    MobStats plugin;
    
    public Entities(MobStats plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void onEntityDamage(EntityDamageEvent event) {
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent damage = (EntityDamageByEntityEvent) event;
            if (damage.getDamager() instanceof Player) return;
            if (damage.getEntity() instanceof LivingEntity && damage.getDamager() instanceof LivingEntity) {
                LivingEntity hit = (LivingEntity) damage.getEntity();
                LivingEntity hitter = (LivingEntity) damage.getDamager();
                UUID hity = hitter.getUniqueId();
                event.setDamage(plugin.damage(plugin.levels.get(hity)));
            }
        }
    }
    
    @Override
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        UUID id = event.getEntity().getUniqueId();
        if (event.getEntity() instanceof Player) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        LivingEntity spawny = (LivingEntity) event.getEntity();
        if (plugin.levels.get(id) != null) plugin.levels.remove(id);
        plugin.levels.put(id, plugin.level(plugin.spawns.get(event.getLocation().getWorld()).toVector().distance(event.getLocation().toVector())));
        spawny.setHealth(plugin.health(plugin.levels.get(id)));
    }
}
