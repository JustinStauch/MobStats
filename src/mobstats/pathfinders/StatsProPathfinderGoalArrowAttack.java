package mobstats.pathfinders;

import arrowpro.arrow.ArrowType;
import arrowpro.arrow.ProArrow;
import mobstats.MobStats;
import mobstats.entities.StatsEntity;
import mobstats.entities.StatsEntitySkeleton;
import net.minecraft.server.EntityArrow;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.EntitySnowball;
import net.minecraft.server.MathHelper;

/**
 * Does the same as the super type but shoots ProArrows instead.
 * 
 * @author Justin Stauch
 * @since October 14, 2012
 */
public class StatsProPathfinderGoalArrowAttack extends StatsPathfinderGoalArrowAttack {
    
    public StatsProPathfinderGoalArrowAttack(EntityLiving entityliving, float f, int i, int j) {
        super(entityliving, f, i, j);
    }
    
    @Override
    protected void f() {
        if (this.g == 1) {
            EntityArrow entityarrow;
            if (MobStats.getPlugin().isArrowProLoaded()) {
                if (b instanceof StatsEntitySkeleton) {
                    ArrowType type = StatsEntitySkeleton.arrow;
                    entityarrow = new ProArrow(this.a, type.newAction(), this.b, this.c, 1.6F, 12.0F);
                }
                else {
                    entityarrow = new EntityArrow(this.a, this.b, this.c, 1.6F, 12.0F);
                }
            }
            else {
                entityarrow = new EntityArrow(this.a, this.b, this.c, 1.6F, 12.0F);
            }
            if (b instanceof StatsEntity) {
                entityarrow.b(MobStats.getPlugin().damage(((StatsEntity) b).getLevel(), entityarrow.d()));
            }
            
            this.a.makeSound(this.b, "random.bow", 1.0F, 1.0F / (this.b.au().nextFloat() * 0.4F + 0.8F));
            this.a.addEntity(entityarrow);
        } else if (this.g == 2) {
            EntitySnowball entitysnowball = new EntitySnowball(this.a, this.b);
            double d0 = this.c.locX - this.b.locX;
            double d1 = this.c.locY + (double) this.c.getHeadHeight() - 1.100000023841858D - entitysnowball.locY;
            double d2 = this.c.locZ - this.b.locZ;
            float f = MathHelper.sqrt(d0 * d0 + d2 * d2) * 0.2F;

            entitysnowball.c(d0, d1 + (double) f, d2, 1.6F, 12.0F);
            this.a.makeSound(this.b, "random.bow", 1.0F, 1.0F / (this.b.au().nextFloat() * 0.4F + 0.8F));
            this.a.addEntity(entitysnowball);
        }
    }
}