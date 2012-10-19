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
import net.minecraft.server.PathfinderGoalArrowAttack;
import net.minecraft.server.World;
import org.bukkit.event.entity.EntityTargetEvent;

/**
 * This is the same code as the superclass with one modification made to damage but other wise just an exact copy to override the whole thing.
 * 
 * @author Justin Stauch
 * @since August 28, 2012
 */
public class StatsPathfinderGoalArrowAttack extends PathfinderGoalArrowAttack {
    World a;
    EntityLiving b;
    EntityLiving c;
    int d = 0;
    float e;
    int f = 0;
    int g;
    int h;
    
    public StatsPathfinderGoalArrowAttack(EntityLiving entityliving, float f, int i, int j) {
        super(entityliving, f, i, j);
        this.b = entityliving;
        this.a = entityliving.world;
        this.e = f;
        this.g = i;
        this.h = j;
        this.a(3);
    }
    
    @Override
    public boolean a() {
        EntityLiving entityliving = this.b.az();

        if (entityliving == null) {
            return false;
        } else {
            this.c = entityliving;
            return true;
        }
    }

    @Override
    public boolean b() {
        return this.a() || !this.b.getNavigation().f();
    }

    @Override
    public void c() {
        // CraftBukkit start
        EntityTargetEvent.TargetReason reason = this.c.isAlive() ? EntityTargetEvent.TargetReason.FORGOT_TARGET : EntityTargetEvent.TargetReason.TARGET_DIED;
        org.bukkit.craftbukkit.event.CraftEventFactory.callEntityTargetEvent(b, null, reason);
        // CraftBukkit end
        this.c = null;
    }
    
    @Override
    public void d() {
        double d0 = 100.0D;
        double d1 = this.b.e(this.c.locX, this.c.boundingBox.b, this.c.locZ);
        boolean flag = this.b.at().canSee(this.c);

        if (flag) {
            ++this.f;
        } else {
            this.f = 0;
        }

        if (d1 <= d0 && this.f >= 20) {
            this.b.getNavigation().g();
        } else {
            this.b.getNavigation().a(this.c, this.e);
        }

        this.b.getControllerLook().a(this.c, 30.0F, 30.0F);
        this.d = Math.max(this.d - 1, 0);
        if (this.d <= 0) {
            if (d1 <= d0 && flag) {
                this.f();
                this.d = this.h;
            }
        }
    }
    
    protected void f() {
        if (this.g == 1) {
            EntityArrow entityarrow;
            entityarrow = new EntityArrow(this.a, this.b, this.c, 1.6F, 12.0F);
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