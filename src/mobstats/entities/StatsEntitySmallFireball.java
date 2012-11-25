package mobstats.entities;

import mobstats.MobStats;

import net.minecraft.server.DamageSource;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.EntitySmallFireball;
import net.minecraft.server.MovingObjectPosition;
import net.minecraft.server.World;

import org.bukkit.event.entity.EntityCombustByEntityEvent;

/**
 *
 * @author Justin Stauch
 * @since August 28, 2012
 */
public class StatsEntitySmallFireball extends EntitySmallFireball {
    
    public StatsEntitySmallFireball(World world) {
        super(world);
        this.a(0.3125F, 0.3125F);
    }

    public StatsEntitySmallFireball(World world, EntityLiving entityliving, double d0, double d1, double d2) {
        super(world, entityliving, d0, d1, d2);
        this.a(0.3125F, 0.3125F);
    }

    public StatsEntitySmallFireball(World world, double d0, double d1, double d2, double d3, double d4, double d5) {
        super(world, d0, d1, d2, d3, d4, d5);
        this.a(0.3125F, 0.3125F);
    }
    
    /**
     * Uses part of the same code from the superclass and just changes the damage, and calls the superclass method if conditions aren't met.
     * 
     * @param movingobjectposition 
     */
    @Override
    protected void a(MovingObjectPosition movingobjectposition) {
        if (!this.world.isStatic) {
            if (movingobjectposition.entity != null) {
                if (!(shooter instanceof StatsEntity)) {
                    super.a(movingobjectposition);
                    return;
                }
                if (!movingobjectposition.entity.isFireproof() && movingobjectposition.entity.damageEntity(DamageSource.fireball(this, this.shooter), MobStats.getPlugin().damage(((StatsEntity) shooter).getLevel(), 5))) {
                    // CraftBukkit start - entity damage by entity event + combust event
                    EntityCombustByEntityEvent event = new EntityCombustByEntityEvent((org.bukkit.entity.Projectile) this.getBukkitEntity(), movingobjectposition.entity.getBukkitEntity(), 5);
                    movingobjectposition.entity.world.getServer().getPluginManager().callEvent(event);

                    if (!event.isCancelled()) {
                        movingobjectposition.entity.setOnFire(event.getDuration());
                    }
                    // CraftBukkit end
                }
            }
            else {
                super.a(movingobjectposition);
            }
        }
    }
}
