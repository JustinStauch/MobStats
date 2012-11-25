package mobstats;

import arrowpro.arrow.ArrowType;

import java.util.ArrayList;
import java.util.Random;

import mobstats.entities.StatsEntity;

import net.minecraft.server.EntityLiving;
import net.minecraft.server.ItemStack;

import org.bukkit.entity.EntityType;

/**
 * A MobEqipment that can also hold an ArrowPro pro arrow.
 * 
 * @author Justin Stauch
 * @since November 16, 2012
 */
public class ProMobEquipment extends MobEquipment {
    private final ArrowType arrow;
    
    public ProMobEquipment(ItemStack[] items, int startZone, int endZone, int numerator, int denominator, ArrayList<EntityType> mobs, ArrowType arrow) {
        super(items, startZone, endZone, numerator, denominator, mobs);
        this.arrow = arrow;
    }
    
    /**
     * Gets the arrow in use.
     * 
     * @return The arrow that this holds.
     */
    public ArrowType getArrow(EntityLiving entity) {
        Random random = new Random();
        if (!(entity instanceof StatsEntity)) {
            return null;
        }
        int level = ((StatsEntity) entity).getLevel();
        if (random.nextInt(getDenominator()) < getNumerator() && getStartZone() <= level && getEndZone() >= level) {
            return arrow;
        }
        return null;
    }
}