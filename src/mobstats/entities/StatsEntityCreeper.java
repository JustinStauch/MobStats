package mobstats.entities;

import mobstats.MobStats;
import net.minecraft.server.DamageSource;
import net.minecraft.server.Entity;
import net.minecraft.server.EntityCreeper;
import net.minecraft.server.MobEffectList;
import net.minecraft.server.World;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 *
 * @since August 22, 2012
 * @author Justin Stauch
 */
public class StatsEntityCreeper extends EntityCreeper implements StatsEntity {
    private int level;
    private int maxHealth;
    
    public StatsEntityCreeper(World world) {
        super(world);
        level = MobStats.getPlugin().level(MobStats.getPlugin().closestOriginDistance(new Location(this.world.getWorld(), locX, locY, locZ)));
        maxHealth = MobStats.getPlugin().health(level, super.getMaxHealth());
    }
    
    public StatsEntityCreeper(World world, int level, int maxHealth) {
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
        plugin.dropItems((LivingEntity) getBukkitEntity());
    }
}