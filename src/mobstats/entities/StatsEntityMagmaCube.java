package mobstats.entities;

import mobstats.MobStats;
import net.minecraft.server.DamageSource;
import net.minecraft.server.EntityHuman;
import net.minecraft.server.EntityMagmaCube;
import net.minecraft.server.EntitySlime;
import net.minecraft.server.World;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 *
 * @author Justin Stauch
 * @since August 24, 2012
 */
public class StatsEntityMagmaCube extends EntityMagmaCube implements StatsEntity {
    private int level;
    private int maxHealth;
    
    private StatsEntityMagmaCube(World world, int level) {
        super(world);
        this.level = level;
        maxHealth = MobStats.getPlugin().health(level, super.getMaxHealth());
    }
    
    public StatsEntityMagmaCube(World world) {
        super(world);
        level = MobStats.getPlugin().level(MobStats.getPlugin().closestOriginDistance(new Location(this.world.getWorld(), locX, locY, locZ)));
        maxHealth = MobStats.getPlugin().health(level, super.getMaxHealth());
    }
    
    public StatsEntityMagmaCube(World world, int level, int maxHealth) {
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
     * Taking from the super class but with the addition of a damage edit.
     * 
     * @param entityhuman 
     */
    @Override
    public void c_(EntityHuman entityhuman) {
        if (l()) {
            int i = getSize();

            if ((m(entityhuman)) && (e(entityhuman) < 0.6D * i * 0.6D * i) && (entityhuman.damageEntity(DamageSource.mobAttack(this), MobStats.getPlugin().damage(level, m())))) {
                this.world.makeSound(this, "mob.attack", 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
            }
        }
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
    
    @Override
    protected EntitySlime i() {
        return new StatsEntityMagmaCube(world, level);
    }
}