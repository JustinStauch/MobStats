package mobstats.entities;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import mobstats.MobStats;
import net.minecraft.server.DamageSource;
import net.minecraft.server.Entity;
import net.minecraft.server.EntityFireball;
import net.minecraft.server.EntityGhast;
import net.minecraft.server.EntityHuman;
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
    
    /**
     * Copied the code from the super class then replaced an EntityFireball instance with a StatsEntityFireball instance.
     */
    @Override
    protected void be() {
        try {
            updateTarget();
            if (!this.world.isStatic && this.world.difficulty == 0) {
                this.die();
            }

            this.bb();
            this.e = this.f;
            double d0 = this.b - this.locX;
            double d1 = this.c - this.locY;
            double d2 = this.d - this.locZ;
            double d3 = d0 * d0 + d1 * d1 + d2 * d2;

            if (d3 < 1.0D || d3 > 3600.0D) {
                this.b = this.locX + (double) ((this.random.nextFloat() * 2.0F - 1.0F) * 16.0F);
                this.c = this.locY + (double) ((this.random.nextFloat() * 2.0F - 1.0F) * 16.0F);
                this.d = this.locZ + (double) ((this.random.nextFloat() * 2.0F - 1.0F) * 16.0F);
            }

            if (this.a-- <= 0) {
                this.a += this.random.nextInt(5) + 2;
                d3 = (double) MathHelper.sqrt(d3);
                Method a = EntityGhast.class.getMethod("a", double.class, double.class, double.class, double.class);
                a.setAccessible(true);
                EntityGhast ghast = this;
                if ((Boolean) a.invoke(ghast, this.b, this.c, this.d, d3)) {
                    this.motX += d0 / d3 * 0.1D;
                    this.motY += d1 / d3 * 0.1D;
                    this.motZ += d2 / d3 * 0.1D;
                } else {
                    this.b = this.locX;
                    this.c = this.locY;
                    this.d = this.locZ;
                }
            }

            if (this.target != null && this.target.dead) {
                // CraftBukkit start
                EntityTargetEvent event = new EntityTargetEvent(this.getBukkitEntity(), null, EntityTargetEvent.TargetReason.TARGET_DIED);
                this.world.getServer().getPluginManager().callEvent(event);

                if (!event.isCancelled()) {
                    if (event.getTarget() == null) {
                        this.target = null;
                    } else {
                        this.target = ((CraftEntity) event.getTarget()).getHandle();
                    }
                }
                // CraftBukkit end
            }

            if (this.target == null || this.h-- <= 0) {
                // CraftBukkit start
                Entity target = this.world.findNearbyVulnerablePlayer(this, 100.0D);
                if (target != null) {
                    EntityTargetEvent event = new EntityTargetEvent(this.getBukkitEntity(), target.getBukkitEntity(), EntityTargetEvent.TargetReason.CLOSEST_PLAYER);
                    this.world.getServer().getPluginManager().callEvent(event);

                    if (!event.isCancelled()) {
                        if (event.getTarget() == null) {
                            this.target = null;
                        } else {
                            this.target = ((CraftEntity) event.getTarget()).getHandle();
                        }
                    }
                }
                // CraftBukkit end

                if (this.target != null) {
                    this.h = 20;
                }
            }

            double d4 = 64.0D;

            if (this.target != null && this.target.e((Entity) this) < d4 * d4) {
                double d5 = this.target.locX - this.locX;
                double d6 = this.target.boundingBox.b + (double) (this.target.length / 2.0F) - (this.locY + (double) (this.length / 2.0F));
                double d7 = this.target.locZ - this.locZ;

                this.aq = this.yaw = -((float) Math.atan2(d5, d7)) * 180.0F / 3.1415927F;
                if (this.l(this.target)) {
                    if (this.f == 10) {
                        this.world.a((EntityHuman) null, 1007, (int) this.locX, (int) this.locY, (int) this.locZ, 0);
                    }

                    ++this.f;
                    if (this.f == 20) {
                        this.world.a((EntityHuman) null, 1008, (int) this.locX, (int) this.locY, (int) this.locZ, 0);
                        StatsEntityFireball entityfireball = new StatsEntityFireball(this.world, this, d5, d6, d7);
                        double d8 = 4.0D;
                        Vec3D vec3d = this.i(1.0F);

                        entityfireball.locX = this.locX + vec3d.a * d8;
                        entityfireball.locY = this.locY + (double) (this.length / 2.0F) + 0.5D;
                        entityfireball.locZ = this.locZ + vec3d.c * d8;
                        this.world.addEntity(entityfireball);
                        this.f = -40;
                    }
                } else if (this.f > 0) {
                    --this.f;
                }
            } else {
                this.aq = this.yaw = -((float) Math.atan2(this.motX, this.motZ)) * 180.0F / 3.1415927F;
                if (this.f > 0) {
                    --this.f;
                }
            }

            if (!this.world.isStatic) {
                byte b0 = this.datawatcher.getByte(16);
                byte b1 = (byte) (this.f > 10 ? 1 : 0);

                if (b0 != b1) {
                    this.datawatcher.watch(16, Byte.valueOf(b1));
                }
            }
            updateSuperTarget();
        } catch (NoSuchMethodException ex) {
            System.out.println("[" + MobStats.getPlugin().getDescription().getName() + "] Error: " + ex.getMessage());
        } catch (IllegalAccessException ex) {
            System.out.println("[" + MobStats.getPlugin().getDescription().getName() + "] Error: " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            System.out.println("[" + MobStats.getPlugin().getDescription().getName() + "] Error: " + ex.getMessage());
        } catch (InvocationTargetException ex) {
            System.out.println("[" + MobStats.getPlugin().getDescription().getName() + "] Error: " + ex.getMessage());
        }
    }
    
    /**
     * Gets the field target and h from the superclass.
     */
    private void updateTarget() {
        try {
            Field targ = EntityGhast.class.getDeclaredField("target");
            Field ache = EntityGhast.class.getDeclaredField("h");
            targ.setAccessible(true);
            ache.setAccessible(true);
            EntityGhast ghast = this;
            this.target = (Entity) targ.get(ghast);
            this.h = ache.getInt(ghast);
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
            targ.setAccessible(true);
            ache.setAccessible(true);
            EntityGhast ghast = this;
            targ.set(ghast, this.target);
            ache.set(ghast, this.h);
        } catch (NoSuchFieldException ex) {
            System.out.println("[" + MobStats.getPlugin().getDescription().getName() + "] Error: " + ex.getMessage());
        } catch (IllegalAccessException ex) {
            System.out.println("[" + MobStats.getPlugin().getDescription().getName() + "] Error: " + ex.getMessage());
        }
    }
}