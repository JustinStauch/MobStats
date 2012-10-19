package mobstats.entities;

import arrowpro.arrow.ArrowType;

import java.lang.reflect.Field;
import java.util.List;

import mobstats.MobStats;
import mobstats.pathfinders.StatsPathfinderGoalArrowAttack;
import mobstats.pathfinders.StatsProPathfinderGoalArrowAttack;

import net.minecraft.server.DamageSource;
import net.minecraft.server.Entity;
import net.minecraft.server.EntityHuman;
import net.minecraft.server.EntitySkeleton;
import net.minecraft.server.MobEffectList;
import net.minecraft.server.PathfinderGoal;
import net.minecraft.server.PathfinderGoalFleeSun;
import net.minecraft.server.PathfinderGoalFloat;
import net.minecraft.server.PathfinderGoalHurtByTarget;
import net.minecraft.server.PathfinderGoalLookAtPlayer;
import net.minecraft.server.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.PathfinderGoalRandomLookaround;
import net.minecraft.server.PathfinderGoalRandomStroll;
import net.minecraft.server.PathfinderGoalRestrictSun;
import net.minecraft.server.World;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 *
 * @author Justin Stauch
 * @since August 24, 2012
 */
public class StatsEntitySkeleton extends EntitySkeleton implements StatsEntity {
    private int level;
    private int maxHealth;
    public static ArrowType arrow;
    
    public StatsEntitySkeleton(World world) {
        super(world);
        level = MobStats.getPlugin().level(MobStats.getPlugin().closestOriginDistance(new Location(this.world.getWorld(), locX, locY, locZ)));
        maxHealth = MobStats.getPlugin().health(level, super.getMaxHealth());
        
        try {
            Field goala = this.goalSelector.getClass().getDeclaredField("a");
            goala.setAccessible(true);
            ((List<PathfinderGoal>) goala.get(this.goalSelector)).clear();

            Field targeta = this.targetSelector.getClass().getDeclaredField("a");
            targeta.setAccessible(true);
            ((List<PathfinderGoal>) targeta.get(this.targetSelector)).clear();
            
            this.goalSelector.a(1, new PathfinderGoalFloat(this));
            this.goalSelector.a(2, new PathfinderGoalRestrictSun(this));
            this.goalSelector.a(3, new PathfinderGoalFleeSun(this, this.bw));
            if (MobStats.getPlugin().isArrowProLoaded()) {
                this.goalSelector.a(4, new StatsProPathfinderGoalArrowAttack(this, this.bw, 1, 60));
            }
            else {
                this.goalSelector.a(4, new StatsPathfinderGoalArrowAttack(this, this.bw, 1, 60));
            }
            this.goalSelector.a(5, new PathfinderGoalRandomStroll(this, this.bw));
            this.goalSelector.a(6, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
            this.goalSelector.a(6, new PathfinderGoalRandomLookaround(this));
            this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, false));
            this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, 16.0F, 0, true));
        } catch (SecurityException ex) {
            System.out.println("[" + MobStats.getPlugin().getDescription().getName() + "] Error: " + ex.getMessage());
        } catch (NoSuchFieldException ex) {
            System.out.println("[" + MobStats.getPlugin().getDescription().getName() + "] Error: " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            System.out.println("[" + MobStats.getPlugin().getDescription().getName() + "] Error: " + ex.getMessage());
        } catch (IllegalAccessException ex) {
            System.out.println("[" + MobStats.getPlugin().getDescription().getName() + "] Error: " + ex.getMessage());
        }
    }
    
    public StatsEntitySkeleton(World world, int level, int maxHealth) {
        super(world);
        this.level = level;
        this.maxHealth = maxHealth;
        
        try {
            Field goala = this.goalSelector.getClass().getDeclaredField("a");
            goala.setAccessible(true);
            ((List<PathfinderGoal>) goala.get(this.goalSelector)).clear();

            Field targeta = this.targetSelector.getClass().getDeclaredField("a");
            targeta.setAccessible(true);
            ((List<PathfinderGoal>) targeta.get(this.targetSelector)).clear();
            
            this.goalSelector.a(1, new PathfinderGoalFloat(this));
            this.goalSelector.a(2, new PathfinderGoalRestrictSun(this));
            this.goalSelector.a(3, new PathfinderGoalFleeSun(this, this.bw));
            if (MobStats.getPlugin().isArrowProLoaded()) {
                this.goalSelector.a(4, new StatsProPathfinderGoalArrowAttack(this, this.bw, 1, 60));
            }
            else {
                this.goalSelector.a(4, new StatsPathfinderGoalArrowAttack(this, this.bw, 1, 60));
            }
            this.goalSelector.a(5, new PathfinderGoalRandomStroll(this, this.bw));
            this.goalSelector.a(6, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
            this.goalSelector.a(6, new PathfinderGoalRandomLookaround(this));
            this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, false));
            this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, 16.0F, 0, true));
        } catch (SecurityException ex) {
            System.out.println("[" + MobStats.getPlugin().getDescription().getName() + "] Error: " + ex.getMessage());
        } catch (NoSuchFieldException ex) {
            System.out.println("[" + MobStats.getPlugin().getDescription().getName() + "] Error: " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            System.out.println("[" + MobStats.getPlugin().getDescription().getName() + "] Error: " + ex.getMessage());
        } catch (IllegalAccessException ex) {
            System.out.println("[" + MobStats.getPlugin().getDescription().getName() + "] Error: " + ex.getMessage());
        }
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
        plugin.dropItems((LivingEntity) getBukkitEntity());
    }
}
