package mobstats.entities;

import mobstats.MobStats;

import net.minecraft.server.DamageSource;
import net.minecraft.server.Enchantment;
import net.minecraft.server.EnchantmentManager;
import net.minecraft.server.Entity;
import net.minecraft.server.EntityArrow;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.EntitySkeleton;
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
    
    public StatsEntitySkeleton(World world) {
        super(world);
        level = MobStats.getPlugin().level(MobStats.getPlugin().closestOriginDistance(new Location(this.world.getWorld(), locX, locY, locZ)));
        maxHealth = MobStats.getPlugin().health(level, super.getMaxHealth());
    }
    
    public StatsEntitySkeleton(World world, int level, int maxHealth) {
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
    public void d(EntityLiving entityliving) {
        EntityArrow entityarrow;
        int i = EnchantmentManager.getEnchantmentLevel(Enchantment.ARROW_DAMAGE.id, bD());
        int j = EnchantmentManager.getEnchantmentLevel(Enchantment.ARROW_KNOCKBACK.id, bD());
        entityarrow = new EntityArrow(this.world, this, entityliving, 1.6F, 12.0F);
        
        if (i > 0) {
          entityarrow.b(entityarrow.c() + i * 0.5D + 0.5D);
        }

        if (j > 0) {
          entityarrow.a(j);
        }

        if ((EnchantmentManager.getEnchantmentLevel(Enchantment.ARROW_FIRE.id, bD()) > 0) || (getSkeletonType() == 1)) {
          entityarrow.setOnFire(100);
        }

        entityarrow.b(MobStats.getPlugin().damage(level, entityarrow.c()));
        this.world.makeSound(this, "random.bow", 1.0F, 1.0F / (aB().nextFloat() * 0.4F + 0.8F));
        this.world.addEntity(entityarrow);
    }
    
    @Override
    public int getExpReward() {
        return MobStats.getPlugin().xp(level, super.getExpReward());
    }
    
    @Override
    public int c(Entity entity) {
        return MobStats.getPlugin().damage(level, super.c(entity));
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
