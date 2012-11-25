package mobstats.entities;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import mobstats.MobStats;
import net.minecraft.server.DamageSource;
import net.minecraft.server.Entity;
import net.minecraft.server.EntityGhast;
import net.minecraft.server.EntityHuman;
import net.minecraft.server.EntityLargeFireball;
import net.minecraft.server.MathHelper;
import net.minecraft.server.Vec3D;
import net.minecraft.server.World;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTargetEvent;

/**
 *
 * @author Justin Stauch
 * @since August 24, 2012
 */
public class StatsEntityGhast extends EntityGhast implements StatsEntity {
    private int level;
    private int maxHealth;
    private Entity target = null;
    private int h = 0;
    private int i = 0;
    
    public StatsEntityGhast(World world) {
        super(world);
        level = MobStats.getPlugin().level(MobStats.getPlugin().closestOriginDistance(new Location(this.world.getWorld(), locX, locY, locZ)));
        maxHealth = MobStats.getPlugin().health(level, super.getMaxHealth());
    }
    
    public StatsEntityGhast(World world, int level, int maxHealth) {
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
    
    private boolean a(double d0, double d1, double d2, double d3) {
        try {
            Method eh = EntityGhast.class.getDeclaredMethod("a", double.class, double.class, double.class, double.class);
            eh.setAccessible(true);
            EntityGhast ghas = this;
            return (Boolean) eh.invoke(ghas, d0, d1, d2, d3);
        } catch (IllegalAccessException ex) {
            MobStats.getPlugin().getLogger().log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            MobStats.getPlugin().getLogger().log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            MobStats.getPlugin().getLogger().log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
            MobStats.getPlugin().getLogger().log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            MobStats.getPlugin().getLogger().log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    @Override
    protected void bk() {
        updateTarget();
        if ((!this.world.isStatic) && (this.world.difficulty == 0)) {
            die();
        }

        bh();
        this.f = this.g;
        double d0 = this.c - this.locX;
        double d1 = this.d - this.locY;
        double d2 = this.e - this.locZ;
        double d3 = d0 * d0 + d1 * d1 + d2 * d2;

        if ((d3 < 1.0D) || (d3 > 3600.0D)) {
            this.c = (this.locX + (this.random.nextFloat() * 2.0F - 1.0F) * 16.0F);
            this.d = (this.locY + (this.random.nextFloat() * 2.0F - 1.0F) * 16.0F);
            this.e = (this.locZ + (this.random.nextFloat() * 2.0F - 1.0F) * 16.0F);
        }

        if (this.b-- <= 0) {
            this.b += this.random.nextInt(5) + 2;
            d3 = MathHelper.sqrt(d3);
            if (a(this.c, this.d, this.e, d3)) {
                this.motX += d0 / d3 * 0.1D;
                this.motY += d1 / d3 * 0.1D;
                this.motZ += d2 / d3 * 0.1D;
            } 
            else {
                this.c = this.locX;
                this.d = this.locY;
                this.e = this.locZ;
            }
        }

        if ((this.target != null) && (this.target.dead)) {
            EntityTargetEvent event = new EntityTargetEvent(getBukkitEntity(), null, EntityTargetEvent.TargetReason.TARGET_DIED);
            this.world.getServer().getPluginManager().callEvent(event);

            if (!event.isCancelled()) {
                if (event.getTarget() == null) {
                    this.target = null;
                }
                else {
                    this.target = ((CraftEntity)event.getTarget()).getHandle();
                }
            }
        }

        if ((this.target == null) || (this.i-- <= 0)) {
            Entity target = this.world.findNearbyVulnerablePlayer(this, 100.0D);
            if (target != null) {
                EntityTargetEvent event = new EntityTargetEvent(getBukkitEntity(), target.getBukkitEntity(), EntityTargetEvent.TargetReason.CLOSEST_PLAYER);
                this.world.getServer().getPluginManager().callEvent(event);

                if (!event.isCancelled()) {
                    if (event.getTarget() == null) {
                        this.target = null;
                    }
                    else {
                        this.target = ((CraftEntity)event.getTarget()).getHandle();
                    }
                }
            }

            if (this.target != null) {
                this.i = 20;
            }
        }

        double d4 = 64.0D;

        if ((this.target != null) && (this.target.e(this) < d4 * d4)) {
            double d5 = this.target.locX - this.locX;
            double d6 = this.target.boundingBox.b + this.target.length / 2.0F - (this.locY + this.length / 2.0F);
            double d7 = this.target.locZ - this.locZ;

            this.aw = (this.yaw = -(float)Math.atan2(d5, d7) * 180.0F / 3.141593F);
            if (m(this.target)) {
                if (this.g == 10) {
                    this.world.a((EntityHuman)null, 1007, (int)this.locX, (int)this.locY, (int)this.locZ, 0);
                }

                this.g += 1;
                if (this.g == 20) {
                    this.world.a((EntityHuman)null, 1008, (int)this.locX, (int)this.locY, (int)this.locZ, 0);
                    EntityLargeFireball entitylargefireball = new StatsEntityLargeFireball(this.world, this, d5, d6, d7);
                    double d8 = 4.0D;
                    Vec3D vec3d = i(1.0F);

                    entitylargefireball.locX = (this.locX + vec3d.c * d8);
                    entitylargefireball.locY = (this.locY + this.length / 2.0F + 0.5D);
                    entitylargefireball.locZ = (this.locZ + vec3d.e * d8);
                    this.world.addEntity(entitylargefireball);
                    this.g = -40;
                }
            }
            else if (this.g > 0) {
                this.g -= 1;
            }
        } 
        else {
            this.aw = (this.yaw = -(float)Math.atan2(this.motX, this.motZ) * 180.0F / 3.141593F);
            if (this.g > 0) {
                this.g -= 1;
            }
        }

        if (!this.world.isStatic) {
            byte b0 = this.datawatcher.getByte(16);
            byte b1 = (byte)(this.g > 10 ? 1 : 0);

            if (b0 != b1) {
                this.datawatcher.watch(16, Byte.valueOf(b1));
            }
        }
        updateSuperTarget();
    }
    
    /**
     * Gets the field target and h from the superclass.
     */
    private void updateTarget() {
        try {
            Field targ = EntityGhast.class.getDeclaredField("target");
            Field ache = EntityGhast.class.getDeclaredField("h");
            Field eye = EntityGhast.class.getDeclaredField("i");
            targ.setAccessible(true);
            ache.setAccessible(true);
            eye.setAccessible(true);
            EntityGhast ghast = this;
            this.target = (Entity) targ.get(ghast);
            this.h = ache.getInt(ghast);
            this.i = eye.getInt(ghast);
        } catch (NoSuchFieldException ex) {
            System.out.println("[" + MobStats.getPlugin().getDescription().getName() + "] Error: " + ex.getMessage());
        } catch (IllegalAccessException ex) {
            System.out.println("[" + MobStats.getPlugin().getDescription().getName() + "] Error: " + ex.getMessage());
        }
    }
    
    private void updateSuperTarget() {
        try {
            Field targ = EntityGhast.class.getDeclaredField("target");
            Field ache = EntityGhast.class.getDeclaredField("h");
            Field eye = EntityGhast.class.getDeclaredField("i");
            targ.setAccessible(true);
            ache.setAccessible(true);
            eye.setAccessible(true);
            EntityGhast ghast = this;
            targ.set(ghast, this.target);
            ache.set(ghast, this.h);
            eye.set(ghast, this.i);
        } catch (NoSuchFieldException ex) {
            System.out.println("[" + MobStats.getPlugin().getDescription().getName() + "] Error: " + ex.getMessage());
        } catch (IllegalAccessException ex) {
            System.out.println("[" + MobStats.getPlugin().getDescription().getName() + "] Error: " + ex.getMessage());
        }
    }
}