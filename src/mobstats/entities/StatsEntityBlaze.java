package mobstats.entities;

import java.lang.reflect.Field;
import java.util.logging.Level;

import mobstats.MobStats;

import net.minecraft.server.DamageSource;
import net.minecraft.server.Entity;

import net.minecraft.server.EntityBlaze;
import net.minecraft.server.EntityHuman;
import net.minecraft.server.MathHelper;
import net.minecraft.server.World;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 *
 * @author Justin Stauch
 * @since August 24, 2012
 */
public class StatsEntityBlaze extends EntityBlaze implements StatsEntity {
    private int level;
    private int maxHealth;
    private int f;
    
    public StatsEntityBlaze(World world) {
        super(world);
        level = MobStats.getPlugin().level(MobStats.getPlugin().closestOriginDistance(new Location(this.world.getWorld(), locX, locY, locZ)));
        maxHealth = MobStats.getPlugin().health(level, super.getMaxHealth());
    }
    
    public StatsEntityBlaze(World world, int level, int maxHealth) {
        super(world);
        this.level = level;
        this.maxHealth = maxHealth;
    }
    
    @Override
    public int getMaxHealth() {
        return maxHealth;
    }
    
    @Override
    public int getLevel() {
        return level;
    }
    
    @Override
    public int c(Entity entity) {
        return MobStats.getPlugin().damage(level, super.c(entity));
    }
    
    @Override
    public int getExpReward() {
        return MobStats.getPlugin().xp(level, super.getExpReward());
    }
    
    @Override
    public void die(DamageSource damagesource) {
        super.die(damagesource);
        MobStats plugin = MobStats.getPlugin();
        double cash = plugin.cash(level);
        if (damagesource == null) {
            return;
        }
        if (damagesource.getEntity() == null) {
            return;
        }
        if (damagesource.getEntity().getBukkitEntity() instanceof Player) {
            Player player = (Player) damagesource.getEntity().getBukkitEntity();
            if (MobStats.getPlugin().useMoney()) {
                MobStats.economy.depositPlayer(player.getName(), cash);
            }
            plugin.callKillMessages(player, (LivingEntity) getBukkitEntity(), level, cash, expToDrop);
        }
    }
    
    /**
     * Taken from the superclass but switches EntitySmallFireball for StatsEntitySmallFireball and registers it.
     * 
     * @param entity
     * @param f 
     */
    @Override
    protected void a(Entity entity, float f) {
        updateFields();
        if ((this.attackTicks <= 0) && (f < 2.0F) && (entity.boundingBox.e > this.boundingBox.b) && (entity.boundingBox.b < this.boundingBox.e)) {
            this.attackTicks = 20;
            l(entity);
        } 
        else if (f < 30.0F) {
            double d0 = entity.locX - this.locX;
            double d1 = entity.boundingBox.b + entity.length / 2.0F - (this.locY + this.length / 2.0F);
            double d2 = entity.locZ - this.locZ;

            if (this.attackTicks == 0) {
                this.f += 1;
                if (this.f == 1) {
                    this.attackTicks = 60;
                    f(true);
                } 
                else if (this.f <= 4) {
                    this.attackTicks = 6;
                } 
                else {
                    this.attackTicks = 100;
                    this.f = 0;
                    f(false);
                }

                if (this.f > 1) {
                    float f1 = MathHelper.c(f) * 0.5F;

                    this.world.a((EntityHuman)null, 1009, (int)this.locX, (int)this.locY, (int)this.locZ, 0);

                    for (int i = 0; i < 1; i++) {
                        StatsEntitySmallFireball entitysmallfireball = new StatsEntitySmallFireball(this.world, this, d0 + this.random.nextGaussian() * f1, d1, d2 + this.random.nextGaussian() * f1);

                        entitysmallfireball.locY = (this.locY + this.length / 2.0F + 0.5D);
                        this.world.addEntity(entitysmallfireball);
                    }
                }
            }

            this.yaw = ((float)(Math.atan2(d2, d0) * 180.0D / 3.141592741012573D) - 90.0F);
            this.b = true;
        }
        updateSuperFields();
    }
    
    private void updateFields() {
        try {
            Field eff = EntityBlaze.class.getDeclaredField("f");
            eff.setAccessible(true);
            EntityBlaze blaze = this;
            f = eff.getInt(blaze);
        } catch (IllegalArgumentException ex) {
            MobStats.getPlugin().getLogger().log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            MobStats.getPlugin().getLogger().log(Level.SEVERE, null, ex);
        } catch (NoSuchFieldException ex) {
            MobStats.getPlugin().getLogger().log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            MobStats.getPlugin().getLogger().log(Level.SEVERE, null, ex);
        }
    }
    
    private void updateSuperFields() {
        try {
            Field eff = EntityBlaze.class.getDeclaredField("f");
            eff.setAccessible(true);
            EntityBlaze blaze = this;
            eff.setInt(blaze, f);
        } catch (IllegalAccessException ex) {
            MobStats.getPlugin().getLogger().log(Level.SEVERE, null, ex);
        } catch (NoSuchFieldException ex) {
            MobStats.getPlugin().getLogger().log(Level.SEVERE, null, ex);
        }
    }
}