package mobstats.entities;

import java.lang.reflect.Field;

import mobstats.MobStats;

import net.minecraft.server.DamageSource;
import net.minecraft.server.Entity;

import net.minecraft.server.EntityBlaze;
import net.minecraft.server.EntityHuman;
import net.minecraft.server.MathHelper;
import net.minecraft.server.MobEffectList;
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
    
    /**
     * This method is the same as the one from net.minecraft.server.EntityMonster with the difference of modifying the damage with the plugin's damage equation.
     * 
     * @see net.minecraft.server.EntityMonster
     * @param entity
     * @return 
     */
    @Override
    public boolean k(Entity entity) {
        int i = damage;
        if (hasEffect(MobEffectList.INCREASE_DAMAGE)) {
            i += 3 << getEffect(MobEffectList.INCREASE_DAMAGE).getAmplifier();
        }
        if (hasEffect(MobEffectList.WEAKNESS)) {
            i -= 2 << getEffect(MobEffectList.WEAKNESS).getAmplifier();
        }
        //MobStats - Change damage based on the level of the mob.
        i = MobStats.getPlugin().damage(level, i);
        //MobStats end
        return entity.damageEntity(DamageSource.mobAttack(this), i);
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
        if (this.attackTicks <= 0 && f < 2.0F && entity.boundingBox.e > this.boundingBox.b && entity.boundingBox.b < this.boundingBox.e) {
            this.attackTicks = 20;
            this.k(entity);
        } else if (f < 30.0F) {
            double d0 = entity.locX - this.locX;
            double d1 = entity.boundingBox.b + (double) (entity.length / 2.0F) - (this.locY + (double) (this.length / 2.0F));
            double d2 = entity.locZ - this.locZ;

            if (this.attackTicks == 0) {
                int g = 0;
                try {
                    Field ge = EntityBlaze.class.getDeclaredField("g");
                    ge.setAccessible(true);
                    EntityBlaze blaze = this;
                    g = ge.getInt(blaze);
                } catch (NoSuchFieldException ex) {
                    System.out.println("[" + MobStats.getPlugin().getDescription().getName() + "] Error: " + ex.getMessage());
                } catch (IllegalAccessException ex) {
                    System.out.println("[" + MobStats.getPlugin().getDescription().getName() + "] Error: " + ex.getMessage());
                }
                ++g;
                if (g == 1) {
                    this.attackTicks = 60;
                    this.e(true);
                } else if (g <= 4) {
                    this.attackTicks = 6;
                } else {
                    this.attackTicks = 100;
                    g = 0;
                    this.e(false);
                }

                if (g > 1) {
                    float f1 = MathHelper.c(f) * 0.5F;

                    this.world.a((EntityHuman) null, 1009, (int) this.locX, (int) this.locY, (int) this.locZ, 0);

                    for (int i = 0; i < 1; ++i) {
                        StatsEntitySmallFireball entitysmallfireball = new StatsEntitySmallFireball(this.world, this, d0 + this.random.nextGaussian() * (double) f1, d1, d2 + this.random.nextGaussian() * (double) f1);

                        entitysmallfireball.locY = this.locY + (double) (this.length / 2.0F) + 0.5D;
                        this.world.addEntity(entitysmallfireball);
                    }
                }
            }

            this.yaw = (float) (Math.atan2(d2, d0) * 180.0D / 3.1415927410125732D) - 90.0F;
            this.b = true;
        }
    }
}