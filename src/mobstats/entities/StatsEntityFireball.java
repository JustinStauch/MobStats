package mobstats.entities;

import mobstats.MobStats;
import net.minecraft.server.DamageSource;
import net.minecraft.server.EntityFireball;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.MovingObjectPosition;
import net.minecraft.server.World;
import org.bukkit.event.entity.ExplosionPrimeEvent;

/**
 *
 * @author Justin Stauch
 * @since August 30, 2012
 */
public class StatsEntityFireball extends EntityFireball {
    
    public StatsEntityFireball(World world, double d0, double d1, double d2, double d3, double d4, double d5) {
        super(world, d0, d1, d2, d3, d4, d5);
    }
    
    public StatsEntityFireball(World world, EntityLiving entityliving, double d0, double d1, double d2) {
        super(world, entityliving, d0, d1, d2);
    }
    
    /**
     * Copy of the code from the superclass but the damage is changed based on the level of the shooter.
     * 
     * @param movingobjectposition 
     */
    @Override
    protected void a(MovingObjectPosition movingobjectposition) {
        if (!this.world.isStatic) {
            if (movingobjectposition.entity != null) {
                if (shooter instanceof StatsEntity) {
                    movingobjectposition.entity.damageEntity(DamageSource.fireball(this, this.shooter), MobStats.getPlugin().damage(((StatsEntity) shooter).getLevel(), 6));
                }
                else {
                    movingobjectposition.entity.damageEntity(DamageSource.fireball(this, this.shooter), 6);
                }
            }

            // CraftBukkit start
            ExplosionPrimeEvent event = new ExplosionPrimeEvent((org.bukkit.entity.Explosive) org.bukkit.craftbukkit.entity.CraftEntity.getEntity(this.world.getServer(), this));
            this.world.getServer().getPluginManager().callEvent(event);

            if (!event.isCancelled()) {
                // give 'this' instead of (Entity) null so we know what causes the damage
                this.world.createExplosion(this, this.locX, this.locY, this.locZ, event.getRadius(), event.getFire());
            }
            // CraftBukkit end
            this.die();
        }
    }
}