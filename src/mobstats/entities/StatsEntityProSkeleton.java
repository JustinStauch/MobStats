package mobstats.entities;

import arrowpro.arrow.ArrowType;
import arrowpro.arrow.ProArrow;

import mobstats.MobStats;

import net.minecraft.server.Enchantment;
import net.minecraft.server.EnchantmentManager;
import net.minecraft.server.EntityArrow;

import net.minecraft.server.EntityLiving;
import net.minecraft.server.World;

/**
 * Same as the StatsEntitySkeleton except it can shoot special arrows instead of just normal arrows.
 * 
 * @author Justin Stauch
 * @since November 10, 2012
 * @see StatsEntitySkeleton
 */
public class StatsEntityProSkeleton extends StatsEntitySkeleton {
    private ArrowType arrow;
    
    public StatsEntityProSkeleton(World world) {
        super(world);
    }
    
    public StatsEntityProSkeleton(World world, int level, int maxHealth) {
        super(world, level, maxHealth);
    }
    
    @Override
    public void d(EntityLiving entityliving) {
        if (!MobStats.getPlugin().isArrowProLoaded()) {
            super.d(entityliving);
            return;
        }
        EntityArrow entityarrow;
        int i = EnchantmentManager.getEnchantmentLevel(Enchantment.ARROW_DAMAGE.id, bD());
        int j = EnchantmentManager.getEnchantmentLevel(Enchantment.ARROW_KNOCKBACK.id, bD());

        if (MobStats.getPlugin().isArrowProLoaded() && arrow != null) {
            entityarrow = new ProArrow(this.world, arrow.newAction(), this, entityliving, 1.6F, 12.0F);
        }
        else {
            entityarrow = new EntityArrow(this.world, this, entityliving, 1.6F, 12.0F);
        }
        
        if (i > 0) {
          entityarrow.b(entityarrow.c() + i * 0.5D + 0.5D);
        }

        if (j > 0) {
          entityarrow.a(j);
        }

        if ((EnchantmentManager.getEnchantmentLevel(Enchantment.ARROW_FIRE.id, bD()) > 0) || (getSkeletonType() == 1)) {
          entityarrow.setOnFire(100);
        }

        entityarrow.b(MobStats.getPlugin().damage(getLevel(), entityarrow.c()));
        this.world.makeSound(this, "random.bow", 1.0F, 1.0F / (aB().nextFloat() * 0.4F + 0.8F));
        this.world.addEntity(entityarrow);
    }
    
    /**
     * Sets the arrow that this skeleton should use.
     * 
     * @param arrow The arrow for the skeleton to use.
     */
    public void setArrow(ArrowType arrow) {
        this.arrow = arrow;
    }
}