package mobstats.entities;

import mobstats.MobStats;
import net.minecraft.server.DamageSource;
import net.minecraft.server.EntityPig;
import net.minecraft.server.World;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 *
 * @author Justin Stauch
 * @since August 24, 2012
 */
public class StatsEntityPig extends EntityPig implements StatsEntity {
    private int level;
    private int maxHealth;
    
    public StatsEntityPig(World world) {
        super(world);
        level = MobStats.getPlugin().level(MobStats.getPlugin().closestOriginDistance(new Location(this.world.getWorld(), locX, locY, locZ)));
        maxHealth = MobStats.getPlugin().health(level, super.getMaxHealth());
    }
    
    public StatsEntityPig(World world, int level, int maxHealth) {
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
