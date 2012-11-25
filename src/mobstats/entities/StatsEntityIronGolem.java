package mobstats.entities;

import java.lang.reflect.Field;
import java.util.logging.Level;
import mobstats.MobStats;
import net.minecraft.server.DamageSource;
import net.minecraft.server.Entity;
import net.minecraft.server.EntityIronGolem;
import net.minecraft.server.World;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 *
 * @author Justin Stauch
 * @since August 24, 2012
 */
public class StatsEntityIronGolem extends EntityIronGolem implements StatsEntity {
    private int level;
    private int maxHealth;
    
    public StatsEntityIronGolem(World world) {
        super(world);
        level = MobStats.getPlugin().level(MobStats.getPlugin().closestOriginDistance(new Location(this.world.getWorld(), locX, locY, locZ)));
        maxHealth = MobStats.getPlugin().health(level, super.getMaxHealth());
    }
    
    public StatsEntityIronGolem(World world, int level, int maxHealth) {
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
    public boolean m(Entity entity) {
        
        try {
            Field f = EntityIronGolem.class.getDeclaredField("f");
            f.setAccessible(true);
            EntityIronGolem gol = this;
            f.setInt(gol, 10);
        } catch (NoSuchFieldException ex) {
            MobStats.getPlugin().getLogger().log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            MobStats.getPlugin().getLogger().log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            MobStats.getPlugin().getLogger().log(Level.SEVERE, null, ex);
        }
        this.world.broadcastEntityEffect(this, (byte) 4);
        boolean flag = entity.damageEntity(DamageSource.mobAttack(this), MobStats.getPlugin().damage(level, 7 + this.random.nextInt(15)));

        if (flag) {
          entity.motY += 0.4000000059604645D;
        }

        this.world.makeSound(this, "mob.irongolem.throw", 1.0F, 1.0F);
        return flag;
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
}
