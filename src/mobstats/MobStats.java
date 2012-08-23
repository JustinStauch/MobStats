package mobstats;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import mobstats.equations.StatSolver;
import mobstats.equations.exponential.EMD;
import mobstats.equations.exponential.EWD;
import mobstats.equations.exponential.Exponential;
import mobstats.equations.quadratic.QMD;
import mobstats.equations.quadratic.QWD;
import mobstats.equations.quadratic.Quadratic;
import mobstats.listeners.Commands;
import mobstats.listeners.Entities;
import mobstats.listeners.Players;

import net.milkbowl.vault.economy.Economy;

import net.minecraft.server.DamageSource;
import net.minecraft.server.EntityExperienceOrb;
import net.minecraft.server.EntityLiving;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * This is the main class of the plugin.
 * 
 * This class handles the enabling and disabling of the plugin, reads files, and has various methods to get important information for the rest of the plugin.
 * 
 * @author Justin Stauch (gamerguy14)
 * @since February 14, 2012
 * @see org.bukkit.plugin.java.JavaPlugin
 * 
 * copyright 2012© Justin Stauch, All Rights Reserved
 */
public class MobStats extends JavaPlugin {
    private PluginDescriptionFile info;
    private FileConfiguration config;
    private Map<World, ArrayList<Location>> origins;
    private String message, joinMessage, portalMessage, respawnMessage, tpMessage;
    private boolean sendMessage, sendJoinMessage, sendPortalMessage, sendRespawnMessage, sendTpMessage;
    private StatSolver zones, damage, health, xp, cash;
    private ArrayList<EntityType> affectedMobs;
    private boolean useAffectedMobs;
    private HashMap<UUID, Integer> levels, healthOfMobs;
    private HashMap<UUID, Boolean> invincible; 
    private PluginManager manager;
    private ArrayList<Drop> drops;
    private boolean useMoney;
    private double delay;
    private int levelCap;
    
    public static Economy economy = null;
    
    /**
     * This method is called when the plugin disables.
     */
    @Override
    public void onDisable() {
        cleanUpHashMaps();
        File levelData = new File(getDataFolder(), "levels.data");
        File healthData = new File(getDataFolder(), "health.data");
        File invincibleData = new File(getDataFolder(), "invincible.data");
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        if (!levelData.exists()) {
            try {
                levelData.createNewFile();
            } catch (IOException ex) {
                System.out.println("[" + info.getName() + "] Error: " + ex.getMessage());
            }
        }
        if (!healthData.exists()) {
            try {
                healthData.createNewFile();
            } catch (IOException ex) {
                System.out.println("[" + info.getName() + "] Error: " + ex.getMessage());
            }
        }
        if (!invincibleData.exists()) {
            try {
                invincibleData.createNewFile();
            } catch (IOException ex) {
                System.out.println("[" + info.getName() + "] Error: " + ex.getMessage());
            }
        }
        ObjectOutputStream out = null;
        try {
            out =  new ObjectOutputStream(new FileOutputStream(levelData));
            out.writeObject(levels);
        } catch (IOException ex) {
            System.out.println("[" + info.getName() + "] Error: " + ex.getMessage());
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                System.out.println("[" + info.getName() + "] Error: " + ex.getMessage());
            }
        }
        try {
            out =  new ObjectOutputStream(new FileOutputStream(healthData));
            out.writeObject(levels);
        } catch (IOException ex) {
            System.out.println("[" + info.getName() + "] Error: " + ex.getMessage());
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                System.out.println("[" + info.getName() + "] Error: " + ex.getMessage());
            }
        }
        try {
            out =  new ObjectOutputStream(new FileOutputStream(invincibleData));
            out.writeObject(levels);
        } catch (IOException ex) {
            System.out.println("[" + info.getName() + "] Error: " + ex.getMessage());
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                System.out.println("[" + info.getName() + "] Error: " + ex.getMessage());
            }
        }
    }
    
    /**
     * This method is called when the plugin is enabled and it serves as the main method of the plugin. It goes through the process of creating files, reading files, and setting variables.
     */
    @Override
    public void onEnable() {
        info = getDescription();
        origins = new HashMap<World, ArrayList<Location>>();
        manager = getServer().getPluginManager();
        
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            saveDefaultConfig();
        }
        config = getConfig();
        
        loadHashMaps();
        getMessages();
        setupOrigins();
        setupCash();
        zones = getEquation("Equations.Zone");
        damage = getEquation("Equations.Damage");
        health = getEquation("Equations.Health");
        xp = getEquation("Equations.XP");
        setupDrops();
        setupAffectedMobs();
        delay = config.getDouble("Delay");
        if (!config.contains("Level Cap") || config.getString("Level Cap").equalsIgnoreCase("none") || config.getString("Level Cap").equalsIgnoreCase("n")) {
            levelCap = Integer.MAX_VALUE;
        }
        else {
            levelCap = config.getInt("Level Cap");
        }
        
        manager.registerEvents(new Entities(this), this);
        manager.registerEvents(new Players(this), this);
        
        getCommand("zone").setExecutor(new Commands(this));
        
        fillHashMaps();
    }
    
    public boolean sendMessage() {
        return sendMessage;
    }
    
    public String getMessage() {
        return message;
    }
    
    public boolean sendJoinMessage() {
        return sendJoinMessage;
    }
    
    public String getJoinMessage() {
        return joinMessage;
    }
    
    public boolean sendPortalMessage() {
        return sendPortalMessage;
    }
    
    public String getPortalMessage() {
        return portalMessage;
    }
    
    public boolean sendRespawnMessage() {
        return sendRespawnMessage;
    }
    
    public String getRespawnMessage() {
        return respawnMessage;
    }
    
    public boolean sendTpMessage() {
        return sendTpMessage;
    }
    
    public String getTpMessage() {
        return tpMessage;
    }
    
    /**
     * Sets the health of the given entity to the given amount.
     * 
     * @param entity The Entity to set the health for.
     * @param amount The amount to set the entity's health to.
     */
    public void setHealth(Entity entity, int amount) {
        UUID id = entity.getUniqueId();
        healthOfMobs.put(id, amount);
        invincible.put(id, false);
    }
    
    /**
     * Subtracts the given amount of health from the given entity and kills it if necessary then returns if it killed it or not.
     * 
     * @param entity The entity to damage.
     * @param damage The damage to take off of the entity's health.
     * @return If the entity died from the subtraction of health.
     */
    public boolean subtractHealth(LivingEntity entity, int damage, Entity damager) {
        UUID id = entity.getUniqueId();
        int health;
        if (healthOfMobs.get(id) != null) health = healthOfMobs.get(id);
        else health = health(getLevel(entity), entity.getMaxHealth());
        setHealth(entity, health - damage);
        if (healthOfMobs.get(id) <= 0) {
            if (damager instanceof Player && useMoney) {
                Player damagePer = (Player) damager;
                economy.depositPlayer(damagePer.getName(), cash(getLevel(entity)));
            }
            if (entity instanceof CraftLivingEntity) {
                CraftLivingEntity live = (CraftLivingEntity) entity;
                EntityLiving li = live.getHandle();
                if (damager instanceof CraftHumanEntity) {
                    li.die(DamageSource.playerAttack(((CraftHumanEntity) damager).getHandle()));
                }
                else if (damager instanceof CraftLivingEntity) {
                    li.die(DamageSource.mobAttack(((CraftLivingEntity) damager).getHandle()));
                }
                //CraftBukkit code taken from the class net.minecraft.server.EntityLiving in the protected method aI(). Since I was bypassing craftbukkit code I needed to implement some stuff that gets skipped like this which is exp dropping.
                int i = li.expToDrop;
                while (i > 0) {
                    int j = EntityExperienceOrb.getOrbValue(i);
                    i -= j;
                    li.world.addEntity(new EntityExperienceOrb(li.world, li.locX, li.locY, li.locZ, j));
                }
                //End of craftbukkit code.
            }
            entity.remove();//Makes sure the entity is gone.
            return true;
        }
        return false;
    }
    
    /**
     * Checks for if the given Entity is invincible.
     * 
     * @param entity The entity to check for invincibility with.
     * @return If the entity is invincible or not.
     */
    public boolean isInvincible(Entity entity) {
        UUID id = entity.getUniqueId();
        if (invincible.get(id) == null) {
            invincible.put(id, false);
        }
        return invincible.get(entity.getUniqueId());
    }
    
    /**
     * Makes the given Entity invincibility and then sets up a Timer to remove the invincibility.
     * 
     * @param entity The Entity to make invincible.
     */
    public void gotHit(final Entity entity) {
        invincible.put(entity.getUniqueId(), true);
        Calendar cal = new GregorianCalendar();
        Timer time = new Timer();
        cal.add(Calendar.MILLISECOND, (int) delay * 1000);
        time.schedule(new TimerTask() {
            @Override 
            public void run() {
                invincible.put(entity.getUniqueId(), false);
            }
        }, cal.getTime());
    }
    
    /**
     * Sets the Entity's level based on its Location.
     * 
     * @param entity The Entity to have its level set.
     */
    public void setLevel(Entity entity) {
        levels.put(entity.getUniqueId(), level(closestOriginDistance(entity.getLocation())));
    }
    
    /**
     * Gets if a mob's stats should be modified by the plugin.
     * 
     * @param type The type of the Entity that is being checked for if it is affected.
     * @return Whether the entity is affected or not.
     */
    public boolean isAffected(EntityType type) {
        if (!useAffectedMobs) return true;
        if (type.equals(EntityType.PLAYER)) return false;
        return affectedMobs.contains(type);
    }
    
    /**
     * Returns the level of a mob at the given distance from the origin.
     * 
     * @param distance The distance of the mob from the closest origin.
     * @return The mobs level.
     */
    public int level(double distance) {
        int level = Double.valueOf(Math.floor(zones.solve(distance, 0))).intValue();
        return level > levelCap ? levelCap : level;
    }
    
    /**
     * Gets the damage that a mob of the given level will deal.
     * 
     * @param level The mob's level.
     * @return The damage that a mob of the given level will deal.
     */
    public int damage(int level, double def) {
        return Double.valueOf(Math.floor(damage.solve(level, def))).intValue();
    }
    
    /**
     * Gets the health that a mob of the given level will have.
     * 
     * @param level The mob's level.
     * @return The health that a mob of the given level will have.
     */
    public int health(int level, double def) {
        return Double.valueOf(Math.floor(health.solve(level, def))).intValue();
    }
    
    /**
     * Gets the amount of xp that a mob of the given level will drop upon dying.
     * 
     * @param level The mob's level.
     * @return The xp that a mob of the given level will drop upon dying.
     */
    public int xp(int level, double def) {
        return Double.valueOf(Math.floor(xp.solve(level, def))).intValue();
    }
    
    /**
     * Gets the amount of money that a mob at the given level will drop upon dying.
     * 
     * @param level
     * @return The amount of money that should be rewarded.
     */
    public double cash(int level) {
        if (!useMoney) return 0;
        return Double.valueOf(Math.floor(cash.solve(level, 0))).intValue();
    }
    
    /**
     * Gets the level of the given Entity. Sets the Entity's level based on Location if none exists.
     * 
     * @param entity The Entity that the level is to be gotten for. Cannot be a Player.
     * @return The level of the Entity.
     */
    public int getLevel(Entity entity) {
        if (entity instanceof Player) return 0;
        if (!levels.containsKey(entity.getUniqueId())) levels.put(entity.getUniqueId(), level(closestOriginDistance(entity.getLocation())));
        return levels.get(entity.getUniqueId());
    }
    
    /**
     * Gets each Drop to check for if it should drop and then drops if it is supposed to.
     * 
     * @param event The EntityDeathEvent that was thrown and holds information for the Drops.
     */
    public void dropItems(EntityDeathEvent event) {
        for (Drop x : drops) x.drop(event);
    }
    
    /**
     * Finds the distance to the closest origin from a given Location.
     * 
     * @param loco The Location of the mob.
     * @return The distance to the closest origin.
     */
    public double closestOriginDistance(Location loco) {
        ArrayList<Location> allLoc = origins.get(loco.getWorld());
        double closest = loco.distance(allLoc.get(0));
        for (Location x : allLoc) {
            double temp = loco.distance(x);
            if (temp < closest) closest = temp;
        }
        return closest;
    }
    
    /**
     * Loads the HashMaps out of memory.
     */
    private void loadHashMaps() {
        ObjectInputStream in = null;
        File levelData = new File(getDataFolder(), "levels.data");
        File healthData = new File(getDataFolder(), "health.data");
        File invincibleData = new File(getDataFolder(), "invincible.data");
        if (!levelData.exists()) {
            levels = new HashMap<UUID, Integer>();
        }
        else {
            try {
                in = new ObjectInputStream(new FileInputStream(levelData));
                levels = (HashMap<UUID, Integer>) in.readObject();
            } catch (IOException ex) {
                System.out.println("[" + info.getName() + "] Error: " + ex.getMessage());
            } catch (ClassNotFoundException ex) {
                System.out.println("[" + info.getName() + "] Error: " + ex.getMessage());
            } finally {
                try {
                    in.close();
                } catch (IOException ex) {
                    System.out.println("[" + info.getName() + "] Error: " + ex.getMessage());
                }
            }
        }
        if (!healthData.exists()) {
            healthOfMobs = new HashMap<UUID, Integer>();
        }
        else {
            try {
                in = new ObjectInputStream(new FileInputStream(healthData));
                healthOfMobs = (HashMap<UUID, Integer>) in.readObject();
            } catch (IOException ex) {
                System.out.println("[" + info.getName() + "] Error: " + ex.getMessage());
            } catch (ClassNotFoundException ex) {
                System.out.println("[" + info.getName() + "] Error: " + ex.getMessage());
            } finally {
                try {
                    in.close();
                } catch (IOException ex) {
                    System.out.println("[" + info.getName() + "] Error: " + ex.getMessage());
                }
            }
        }
        if (!invincibleData.exists()) {
            invincible = new HashMap<UUID, Boolean>();
        }
        else {
            try {
                in = new ObjectInputStream(new FileInputStream(invincibleData));
                invincible = (HashMap<UUID, Boolean>) in.readObject();
            } catch (IOException ex) {
                System.out.println("[" + info.getName() + "] Error: " + ex.getMessage());
            } catch (ClassNotFoundException ex) {
                System.out.println("[" + info.getName() + "] Error: " + ex.getMessage());
            } finally {
                try {
                    in.close();
                } catch (IOException ex) {
                    System.out.println("[" + info.getName() + "] Error: " + ex.getMessage());
                }
            }
        }
        if (levels == null) {
            levels = new HashMap<UUID, Integer>();
        }
        if (healthOfMobs == null) {
            healthOfMobs = new HashMap<UUID, Integer>();
        }
        if (invincible == null) {
            invincible = new HashMap<UUID, Boolean>();
        }
    }
    
    /**
     * Looks through all the entities, calculates there stats, and adds them to HashMaps to store the stats.
     */
    private void fillHashMaps() {
        List<World> worlds = getServer().getWorlds();
        for (World world : worlds) {
            List<LivingEntity> entities = world.getLivingEntities();
            for (LivingEntity entity : entities) {
                if (!isAffected(entity.getType())) {
                    continue;
                }
                UUID id = entity.getUniqueId();
                if (!levels.containsKey(id)) {
                    levels.put(id, level(closestOriginDistance(entity.getLocation())));
                }
                if (!healthOfMobs.containsKey(id)) {
                    healthOfMobs.put(id, health(getLevel(entity), entity.getHealth()));
                }
                if(!invincible.containsKey(id)) {
                    invincible.put(id, false);
                }
                entity.setHealth(entity.getMaxHealth());
            }
        }
    }
    
    /**
     * Reads the part of the config file that contains the messages.
     * 
     * @throws Exception If there is a problem in the config reading, the exception will be thrown to be caught in the onEnable method.
     */
    private void getMessages() {
        if (!config.contains("Messages")) {
            message = "";
            tpMessage = "";
            joinMessage = "";
            respawnMessage = "";
            portalMessage = "";
            sendMessage = false;
            sendTpMessage = false;
            sendJoinMessage = false;
            sendRespawnMessage = false;
            sendPortalMessage = false;
            return;
        }
        if (!config.contains("Messages.Message")) {
            message = "";
            sendMessage = false;
        }
        else if (config.isBoolean("Messages.Message")) {
            if (!config.getBoolean("Messages.Message")) {
                sendMessage = false;
            }
            else {
                message = "true";
                sendMessage = true;
            }
        }
        else if (config.isString("Messages.Message")) {
            if (config.getString("Messages.Message").equalsIgnoreCase("false")) {
                sendMessage = false;
            }
            else {
                message = config.getString("Messages.Message");
                sendMessage = true;
            }
        }
        if (!config.contains("Messages.TP Message")) {
            tpMessage = "";
            sendTpMessage = false;
        }
        else if (config.isBoolean("Messages.TP Message")) {
            if (!config.getBoolean("Messages.TP Message")) sendTpMessage = false;
            else {
                tpMessage = "true";
                sendTpMessage = true;
            }
        }
        else if (config.isString("Messages.TP Message")) {
            if (config.getString("Messages.TP Message").equalsIgnoreCase("false")) sendTpMessage = false;
            else {
                tpMessage = config.getString("Messages.TP Message");
                sendTpMessage = true;
            }
        }
        if (!config.contains("Messages.Join Message")) {
            joinMessage = "";
            sendJoinMessage = false;
        }
        else if (config.isBoolean("Messages.Join Message")) {
            if (!config.getBoolean("Messages.Join Message")) sendJoinMessage = false;
            else {
                joinMessage = "true";
                sendJoinMessage = true;
            }
        }
        else if (config.isString("Messages.Join Message")) {
            if (config.getString("Messages.Join Message").equalsIgnoreCase("false")) sendJoinMessage = false;
            else {
                joinMessage = config.getString("Messages.Join Message");
                sendJoinMessage = true;
            }
        }
        if (!config.contains("Messages.Respawn Message")) {
            respawnMessage = "";
            sendRespawnMessage = false;
        }
        else if (config.isBoolean("Messages.Respawn Message")) {
            if (!config.getBoolean("Messages.Respawn Message")) sendRespawnMessage = false;
            else {
                respawnMessage = "true";
                sendRespawnMessage = true;
            }
        }
        else if (config.isString("Messages.Respawn Message")) {
            if (config.getString("Messages.Respawn Message").equalsIgnoreCase("false")) sendRespawnMessage = false;
            else {
                respawnMessage = config.getString("Messages.Respawn Message");
                sendRespawnMessage = true;
            }
        }
        if (!config.contains("Messages.Portal Message")) {
            portalMessage = "";
            sendPortalMessage = false;
        }
        else if (config.isBoolean("Messages.Portal Message")) {
            if (!config.getBoolean("Messages.Message")) sendPortalMessage = false;
            else {
                portalMessage = "true";
                sendPortalMessage = true;
            }
        }
        else if (config.isString("Messages.Portal Message")) {
            if (config.getString("Messages.Portal Message").equalsIgnoreCase("false")) sendMessage = false;
            else {
                portalMessage = config.getString("Messages.Portal Message");
                sendPortalMessage = true;
            }
        }
    }
    
    /**
     * Reads the config file for the origins of the levels.
     */
    private void setupOrigins() {
        if (!config.contains("Origins")) {
            List<World> worlds = getServer().getWorlds();
            for (int x = 0; x < worlds.size(); x++) {
                ArrayList<Location> temp = new ArrayList<Location>();
                temp.add(worlds.get(x).getSpawnLocation());
                origins.put(worlds.get(x), temp);
                ArrayList<String> tempS = new ArrayList<String>();
                tempS.add("spawn");
                config.addDefault("Origins." + worlds.get(x).getName(), tempS);
            }
            return;
        }
        List<World> worlds = getServer().getWorlds();
        for (int x = 0; x < worlds.size(); x++) {
            if (!config.contains("Origins." + worlds.get(x).getName())) {
                ArrayList<Location> tempLoc;
                if (origins.get(worlds.get(x)) != null) {
                    tempLoc = origins.get(worlds.get(x));
                    origins.remove(worlds.get(x));
                }
                else tempLoc = new ArrayList<Location>();
                tempLoc.add(worlds.get(x).getSpawnLocation());
                origins.put(worlds.get(x), tempLoc);
                ArrayList<String> tempS = new ArrayList<String>();
                tempS.add("spawn");
                config.addDefault("Origins." + worlds.get(x).getName(), tempS);
                continue;
            }
            else if (!config.isList(("Origins." + worlds.get(x).getName()))) {
                System.out.println("[" + info.getName() + "] Improper format in config!! Path \"Origins." + worlds.get(x).getName() + "\" must be a list.");
                ArrayList<Location> tempLoc;
                if (origins.get(worlds.get(x)) != null) {
                    tempLoc = origins.get(worlds.get(x));
                    origins.remove(worlds.get(x));
                }
                else tempLoc = new ArrayList<Location>();
                tempLoc.add(worlds.get(x).getSpawnLocation());
                origins.put(worlds.get(x), tempLoc);
                ArrayList<String> tempS = new ArrayList<String>();
                tempS.add("spawn");
                config.addDefault("Origins." + worlds.get(x).getName(), tempS);
                continue;
            }
            List<String> stringOrg = config.getStringList("Origins." + worlds.get(x).getName());
            ArrayList<Location> orgLoc = new ArrayList<Location>();
            for (String i : stringOrg) orgLoc.add(stringToLocation(worlds.get(x), i));
            if (origins.get(worlds.get(x)) == null) origins.remove(worlds.get(x));
            origins.put(worlds.get(x), orgLoc);
            config.addDefault("Origins." + worlds.get(x).getName(), stringOrg);
        }
    }
    
    /**
     * Creates an equation out of the information for a certain path in the config. So far only quadratic equations are supported.
     * 
     * @param path The path to look for values at in the config.
     * @return The equation from the given path.
     */
    private StatSolver getEquation(String path) {
        if (config.getString(path + ".Type").equalsIgnoreCase("quadratic")) {
            double a, b, c;
            a = config.getDouble(path + ".a");
            b = config.getDouble(path + ".b");
            c = config.getDouble(path + ".c");
            return new Quadratic(a, b, c);
        }
        if (config.getString(path + ".Type").equalsIgnoreCase("QWD")) {
            double a, b, c;
            boolean aDef, bDef, cDef;
            String hold;
            hold = config.getString(path + ".a");
            if (hold.contains("d")) {
                aDef = true;
                hold.replaceAll("d", "");
                System.out.println(hold);
            }
            else aDef = false;
            a = Double.parseDouble(hold);
            hold = config.getString(path + ".b");
            if (hold.contains("d")) {
                bDef = true;
                hold.replaceAll("d", "");
            }
            else bDef = false;
            b = Double.parseDouble(hold);
            hold = config.getString(path + ".c");
            if (hold.contains("d")) {
                cDef = true;
                hold.replaceAll("d", "");
            }
            else cDef = false;
            c = Double.parseDouble(hold);
            return new QWD(a, b, c, aDef, bDef, cDef);
        }
        if (config.getString(path + ".Type").equalsIgnoreCase("QMD")) {
            double a, b, c;
            a = config.getDouble(path + ".a");
            b = config.getDouble(path + ".b");
            c = config.getDouble(path + ".c");
            return new QMD(a, b, c);
        }
        if (config.getString(path + ".Type").equalsIgnoreCase("exponential")) {
            double a, b, c, d, f;
            a = config.getDouble(path + ".a");
            b = config.getDouble(path + ".b");
            c = config.getDouble(path + ".c");
            d = config.getDouble(path + ".d");
            f = config.getDouble(path + ".f");
            return new Exponential(a, b, c, d, f);
        }
        if (config.getString(path + ".Type").equalsIgnoreCase("EWD")) {
            double a, b, c, d, f;
            boolean aDef, bDef, cDef, dDef, fDef;
            String hold;
            hold = config.getString(path + ".a");
            if (hold.contains("d")) {
                aDef = true;
                hold.replaceAll("d", "");
            }
            else aDef = false;
            a = Double.parseDouble(hold);
            hold = config.getString(path + ".b");
            if (hold.contains("d")) {
                bDef = true;
                hold.replaceAll("d", "");
            }
            else bDef = false;
            b = Double.parseDouble(hold);
            hold = config.getString(path + ".c");
            if (hold.contains("d")) {
                cDef = true;
                hold.replaceAll("d", "");
            }
            else cDef = false;
            c = Double.parseDouble(hold);
            hold = config.getString(path + ".d");
            if (hold.contains("d")) {
                dDef = true;
                hold.replaceAll("d", "");
            }
            else dDef = false;
            d = Double.parseDouble(hold);
            hold = config.getString(path + ".f");
            if (hold.contains("d")) {
                fDef = true;
                hold.replaceAll("d", "");
            }
            else fDef = false;
            f = Double.parseDouble(hold);
            return new EWD(a, b, c, d, f, aDef, bDef, cDef, dDef, fDef);
        }
        if (config.getString(path + ".Type").equalsIgnoreCase("EMD")) {
            double a, b, c, d, f;
            a = config.getDouble(path + ".a");
            b = config.getDouble(path + ".b");
            c = config.getDouble(path + ".c");
            d = config.getDouble(path + ".d");
            f = config.getDouble(path + ".f");
            return new EMD(a, b, c, d, f);
        }
        return null;
    }
    
    /**
     * Sets up the part of the config related to mobs that are affected by the plugin.
     */
    private void setupAffectedMobs() {
        affectedMobs = new ArrayList<EntityType>();
        if (!config.contains("Affected Mobs")) {
            useAffectedMobs = false;
            return;
        }
        if (!config.isList("Affected Mobs")) {
            System.out.println("[" + info.getName() + "] \"Affectd Mobs\" path should be a list.");
            useAffectedMobs = false;
            return;
        }
        List<String> rawMobs = config.getStringList("Affected Mobs");
        for (String x : rawMobs) affectedMobs.add(EntityType.fromName(x));
        useAffectedMobs = true;
    }
    
    /**
     * Sets up the part of the config related to custom Drops
     */
    private void setupDrops() {
        drops = new ArrayList<Drop>();
        if (!config.contains("Drops")) return;
        List<String> dropper = config.getStringList("Drops");
        for (String x : dropper) {
            if (!config.contains(x)) {
                System.out.print("[" + info.getName() + "] Config does not conatain " + x);
                continue;
            }
            List<String> mobs;
            ArrayList<EntityType> usedMobs = new ArrayList<EntityType>();
            if (config.contains(x + ".Mobs")) {
                mobs = config.getStringList(x + ".Mobs");
                for (String y : mobs) usedMobs.add(EntityType.fromName(y));
                if (usedMobs.isEmpty()) usedMobs = getListOfAllTypes();
            }
            else usedMobs = getListOfAllTypes();
            int startZone;
            if (!config.contains(x + ".Start Zone")) startZone = 0;
            else startZone = config.getInt(x + ".Start Zone");
            config.set(x + ".Start Zone", startZone);
            int endZone;
            if (!config.contains(x + ".End Zone")) endZone = -1;
            else endZone = config.getInt(x + ".End Zone");
            config.set(x + ".End Zone", endZone);
            int numerator, denominator;
            if (!config.contains(x + ".Odds")) {
                numerator = 1;
                denominator = 1;
                config.set(x + ".Odds", 1);
            }
            else {
                String odds = config.getString(x + ".Odds");
                String[] odder = odds.split("/");
                if (odder.length == 1) {
                    numerator = 1;
                    denominator = 1;
                }
                else {
                    numerator = Integer.parseInt(odder[0]);
                    denominator = Integer.parseInt(odder[1]);
                }
                config.set(x + ".Odds", odds);
            }
            if (!config.contains(x + ".Items")) {
                System.out.println("[" + info.getName() + "] " + x + " needs items");
                continue;
            }
            List<String> items = config.getStringList(x + ".Items");
            ArrayList<ItemStack> allItems = new ArrayList<ItemStack>();
            config.set(x + ".Items", items);
            for (String item : items) {
                String[] parts = item.split(",");
                int id = Integer.parseInt(parts[0]);
                int amt;
                if (parts.length == 1) amt = 1;
                else amt = Integer.parseInt(parts[1]);
                allItems.add(new ItemStack(id, amt));
            }
            drops.add(new Drop(allItems, startZone, endZone, numerator, denominator, usedMobs, this));
        }
    }
    
    /**
     * Reads the config to setup the area for the money drops of the mobs.
     */
    private void setupCash() {
        if (!setupEconomy()) {
            System.out.println("[" + info.getName() + "] No economy found! Ignoring economy.");
            useMoney = false;
            return;
        }
        if (!config.contains("Equations.Money")) {
            useMoney = false;
            return;
        }
        useMoney = true;
        cash = getEquation("Equations.Money");
    }
    
    /**
     * Checks to see if their proper economy plugins and if there are it sets it up.
     * 
     * @return If the setup succeeded or not.
     */
    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
        if (economyProvider != null) economy = economyProvider.getProvider();
        return (economy != null);
    }
    
    /**
     * Turns a String into a Location.
     * 
     * @param world the World the Location is in.
     * @param location the String representing the Location.
     * @return the Location represented by the String.
     */
    private Location stringToLocation(World world, String location) {
        if (location.equalsIgnoreCase("spawn")) return world.getSpawnLocation();
        String[] coords = location.split(",");
        if (coords.length != 3) return world.getSpawnLocation();
        int x = Integer.parseInt(coords[0]);
        int y = Integer.parseInt(coords[1]);
        int z = Integer.parseInt(coords[2]);
        return new Location(world, x, y, z);
    }
    
    private ArrayList<EntityType> getListOfAllTypes() {
        ArrayList<EntityType> entities = new ArrayList<EntityType>();
        EntityType[] entity = EntityType.values();
        entities.addAll(Arrays.asList(entity));
        return entities;
    }
    
    /**
     * Removes all mobs that are stored in a HashMap but do not exist.
     */
    private void cleanUpHashMaps() {
        for (UUID id : levels.keySet().toArray(new UUID[levels.keySet().size()])) {
            boolean exists = false;
            for (World world : getServer().getWorlds()) {
                for (Entity ent : world.getEntities()) {
                    if (ent.getUniqueId().equals(id)) {
                        exists = true;
                        break;
                    }
                }
                if (exists) {
                    break;
                }
            }
            if (!exists) {
                levels.remove(id);
            }
        }
        for (UUID id : healthOfMobs.keySet().toArray(new UUID[healthOfMobs.keySet().size()])) {
            boolean exists = false;
            for (World world : getServer().getWorlds()) {
                for (Entity ent : world.getEntities()) {
                    if (ent.getUniqueId().equals(id)) {
                        exists = true;
                        break;
                    }
                }
                if (exists) {
                    break;
                }
            }
            if (!exists) {
                healthOfMobs.remove(id);
            }
        }
        for (UUID id : invincible.keySet().toArray(new UUID[invincible.keySet().size()])) {
            boolean exists = false;
            for (World world : getServer().getWorlds()) {
                for (Entity ent : world.getEntities()) {
                    if (ent.getUniqueId().equals(id)) {
                        exists = true;
                        break;
                    }
                }
                if (exists) {
                    break;
                }
            }
            if (!exists) {
                invincible.remove(id);
            }
        }
    }
}