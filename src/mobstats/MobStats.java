package mobstats;

import arrowpro.arrow.ArrowType;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mobstats.entities.*;
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

import net.minecraft.server.EntityTypes;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftBlaze;
import org.bukkit.craftbukkit.entity.CraftCaveSpider;
import org.bukkit.craftbukkit.entity.CraftChicken;
import org.bukkit.craftbukkit.entity.CraftCow;
import org.bukkit.craftbukkit.entity.CraftCreeper;
import org.bukkit.craftbukkit.entity.CraftEnderDragon;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftGhast;
import org.bukkit.craftbukkit.entity.CraftIronGolem;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.entity.CraftMagmaCube;
import org.bukkit.craftbukkit.entity.CraftMushroomCow;
import org.bukkit.craftbukkit.entity.CraftOcelot;
import org.bukkit.craftbukkit.entity.CraftPig;
import org.bukkit.craftbukkit.entity.CraftPigZombie;
import org.bukkit.craftbukkit.entity.CraftSheep;
import org.bukkit.craftbukkit.entity.CraftSilverfish;
import org.bukkit.craftbukkit.entity.CraftSkeleton;
import org.bukkit.craftbukkit.entity.CraftSlime;
import org.bukkit.craftbukkit.entity.CraftSnowman;
import org.bukkit.craftbukkit.entity.CraftSpider;
import org.bukkit.craftbukkit.entity.CraftSquid;
import org.bukkit.craftbukkit.entity.CraftVillager;
import org.bukkit.craftbukkit.entity.CraftWolf;
import org.bukkit.craftbukkit.entity.CraftZombie;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
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
    private String message, joinMessage, portalMessage, respawnMessage, tpMessage, killMessage, deathMessage;
    private boolean sendMessage, sendJoinMessage, sendPortalMessage, sendRespawnMessage, sendTpMessage, sendKillMessage, sendDeathMessage;
    private StatSolver zones, damage, health, xp, cash, arrowPro;
    private ArrayList<EntityType> affectedMobs;
    private boolean useAffectedMobs;
    private PluginManager manager;
    private ArrayList<Drop> drops;
    private boolean useMoney;
    private HashMap<String, Boolean> notifications;
    
    /**
     * Instance of the main class to be passed to classes created without the ability to have this in the constructor.
     */
    private static MobStats plugin;
    
    public static Economy economy = null;
    
    /**
     * This method is called when the plugin disables.
     */
    @Override
    public void onDisable() {
        File notes = new File(getDataFolder(), "notifications.data");
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        if (!notes.exists()) {
            try {
                notes.createNewFile();
            } catch (IOException ex) {
                System.out.println("[" + info.getName() + "] Error: " + ex.getMessage());
            }
        }
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(new FileOutputStream(notes));
            out.writeObject(notifications);
        } catch (IOException ex) {
            System.out.println("[" + info.getName() + "] Error: " + ex.getMessage());
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                System.out.println("[" + info.getName() + "] Error: " + ex.getMessage());
            }
        }
        plugin = null;
        economy = null;
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
        
        registerClasses();
        getMessages();
        if (sendKillMessage()) {
            loadNotifications();
        }
        setupOrigins();
        setupCash();
        zones = getEquation("Equations.Zone");
        damage = getEquation("Equations.Damage");
        health = getEquation("Equations.Health");
        xp = getEquation("Equations.XP");
        setupDrops();
        setupAffectedMobs();
        
        if (isArrowProLoaded()) {
            StatsEntitySkeleton.arrow = ArrowType.fromName(config.getString("Skeleton Arrow"));
        }
        
        manager.registerEvents(new Entities(this), this);
        manager.registerEvents(new Players(this), this);
        
        getCommand("zone").setExecutor(new Commands(this));
        getCommand("replaceall").setExecutor(new Commands(this));
        if (sendKillMessage()) {
            getCommand("MobStats").setExecutor(new Commands(this));
        }
        
        plugin = this;
        
        replaceAllWrongEntities();
    }
    
    public boolean isArrowProLoaded() {
        Plugin[] plugins = manager.getPlugins();
        int found = 0;
        for (Plugin plug : plugins) {
            for (String dep : getDescription().getSoftDepend()) {
                if (plug.getDescription().getName().equalsIgnoreCase(dep)) {
                    found++;
                    break;
                }
            }
        }
        return found >= getDescription().getSoftDepend().size();
    }
    
    /**
     * Passes the plugin to what ever needs it.
     * 
     * @return The instance of the main class of the plugin.
     */
    public static MobStats getPlugin() {
        return plugin;
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
    
    public boolean sendKillMessage() {
        return sendKillMessage;
    }
    
    public String getKillMessage() {
        return killMessage;
    }
    
    public boolean sendDeathMessage() {
        return sendDeathMessage;
    }
    
    public String getDeathMessage() {
        return deathMessage;
    }
    
    public boolean usesNotifications(Player player) {
        if (!sendKillMessage()) {
            return false;
        }
        String name = player.getName();
        if (!notifications.containsKey(name)) {
           setUseNotifications(player, true);
        }
        return notifications.get(name);
    }
    
    public void setUseNotifications(Player player, boolean useThem) {
        notifications.put(player.getName(), useThem);
        if (useThem) {
            player.sendMessage(ChatColor.GOLD + "You will recieve a notification everytime someone kills a mob");
            player.sendMessage(ChatColor.GOLD + "Type " + ChatColor.WHITE + "/ms off" + ChatColor.GOLD + " to turn these off");
        }
        else {
            player.sendMessage(ChatColor.GOLD + "You will not recieve a notification everytime someone kills a mob");
            player.sendMessage(ChatColor.GOLD + "Type " + ChatColor.WHITE + "/ms on" + ChatColor.GOLD + " to turn notifications on");
        }
    }
    
    /**
     * Sends the notifications that someone has made a kill to everyone who sets up to hear it.
     * 
     * @param player The Player who got the kill.
     * @param entity The Entity who was killed.
     * @param level The level of the Entity that got killed.
     * @param cash The money that the Player got for the kill.
     * @param exp The experience dropped by the mob for the gill.
     */
    public void callKillMessages(Player player, LivingEntity entity, int level, double cash, int exp) {
        if (!sendKillMessage()) {
            return;
        }
        String messageTemp = getKillMessage();
        messageTemp = messageTemp.replaceAll("-mob", entity.getType().toString());
        messageTemp = messageTemp.replaceAll("-level", String.valueOf(level));
        messageTemp = messageTemp.replaceAll("-money", String.valueOf(cash));
        messageTemp = messageTemp.replaceAll("-exp", String.valueOf(exp));
        if (usesNotifications(player)) {
            String tempMessage = messageTemp;
            tempMessage = tempMessage.replaceAll("-player", "you");
            player.sendMessage(tempMessage);
        }
        messageTemp = messageTemp.replaceAll("-player", player.getDisplayName());
        for (Player play : getServer().getOnlinePlayers()) {
            if (usesNotifications(play) && !play.getName().equals(player.getName())) {
                play.sendMessage(messageTemp);
            }
        }
    }
    
    public boolean useMoney() {
        return useMoney;
    }
    
    /**
     * Replaces the minecraft entity with the equivalent MobStats entities unless it is already replaced.
     * 
     * @param bad The entity to be replaced.
     * @return The entity created.
     */
    public LivingEntity replaceEntity(LivingEntity bad, SpawnReason reason, boolean removeOtherEntity) {
        Location loco = bad.getLocation();
        World world = loco.getWorld();
        if (!isAffected(bad.getType())) {
            return null;
        }
        CraftLivingEntity good = null;
        int level = level(closestOriginDistance(loco));
        switch (bad.getType()) {
            case BLAZE:
                if (((CraftEntity) bad).getHandle() instanceof StatsEntityBlaze) {
                    return bad;
                }
                good = new CraftBlaze((CraftServer) bad.getServer(), new StatsEntityBlaze(((CraftWorld) bad.getWorld()).getHandle(), level, health(level, bad.getMaxHealth())));
                break;
            case CAVE_SPIDER:
                if (((CraftEntity) bad).getHandle() instanceof StatsEntityCaveSpider) {
                    return bad;
                }
                good = new CraftCaveSpider((CraftServer) bad.getServer(), new StatsEntityCaveSpider(((CraftWorld) bad.getWorld()).getHandle(), level, health(level, bad.getMaxHealth())));
                break;
            case CHICKEN:
                if (((CraftEntity) bad).getHandle() instanceof StatsEntityChicken) {
                    return bad;
                }
                good = new CraftChicken((CraftServer) bad.getServer(), new StatsEntityChicken(((CraftWorld) bad.getWorld()).getHandle(), level, health(level, bad.getMaxHealth())));
                break;
            case COW:
                if (((CraftEntity) bad).getHandle() instanceof StatsEntityCow) {
                    return bad;
                }
                good = new CraftCow((CraftServer) bad.getServer(), new StatsEntityCow(((CraftWorld) bad.getWorld()).getHandle(), level, health(level, bad.getMaxHealth())));
                break;
            case CREEPER:
                if (((CraftEntity) bad).getHandle() instanceof StatsEntityCreeper) {
                    return bad;
                }
                good = new CraftCreeper((CraftServer) bad.getServer(), new StatsEntityCreeper(((CraftWorld) bad.getWorld()).getHandle(), level, health(level, bad.getMaxHealth())));
                break;
            case ENDER_DRAGON:
                if (((CraftEntity) bad).getHandle() instanceof StatsEntityEnderDragon) {
                    return bad;
                }
                good = new CraftEnderDragon((CraftServer) bad.getServer(), new StatsEntityEnderDragon(((CraftWorld) bad.getWorld()).getHandle(), level, health(level, bad.getMaxHealth())));
                break;
            case ENDERMAN:
                if (((CraftEntity) bad).getHandle() instanceof StatsEntityBlaze) {
                    return bad;
                }
                good = new CraftBlaze((CraftServer) bad.getServer(), new StatsEntityBlaze(((CraftWorld) bad.getWorld()).getHandle(), level, health(level, bad.getMaxHealth())));
                break;
            case GHAST:
                if (((CraftEntity) bad).getHandle() instanceof StatsEntityGhast) {
                    return bad;
                }
                good = new CraftGhast((CraftServer) bad.getServer(), new StatsEntityGhast(((CraftWorld) bad.getWorld()).getHandle(), level, health(level, bad.getMaxHealth())));
                break;
            case IRON_GOLEM:
                if (((CraftEntity) bad).getHandle() instanceof StatsEntityIronGolem) {
                    return bad;
                }
                good = new CraftIronGolem((CraftServer) bad.getServer(), new StatsEntityIronGolem(((CraftWorld) bad.getWorld()).getHandle(), level, health(level, bad.getMaxHealth())));
                break;
            case MAGMA_CUBE:
                if (((CraftEntity) bad).getHandle() instanceof StatsEntityMagmaCube) {
                    return bad;
                }
                good = new CraftMagmaCube((CraftServer) bad.getServer(), new StatsEntityMagmaCube(((CraftWorld) bad.getWorld()).getHandle(), level, health(level, bad.getMaxHealth())));
                break;
            case MUSHROOM_COW:
                if (((CraftEntity) bad).getHandle() instanceof StatsEntityMushroomCow) {
                    return bad;
                }
                good = new CraftMushroomCow((CraftServer) bad.getServer(), new StatsEntityMushroomCow(((CraftWorld) bad.getWorld()).getHandle(), level, health(level, bad.getMaxHealth())));
                break;
            case OCELOT:
                if (((CraftEntity) bad).getHandle() instanceof StatsEntityOcelot) {
                    return bad;
                }
                good = new CraftOcelot((CraftServer) bad.getServer(), new StatsEntityOcelot(((CraftWorld) bad.getWorld()).getHandle(), level, health(level, bad.getMaxHealth())));
                break;
            case PIG:
                if (((CraftEntity) bad).getHandle() instanceof StatsEntityPig) {
                    return bad;
                }
                good = new CraftPig((CraftServer) bad.getServer(), new StatsEntityPig(((CraftWorld) bad.getWorld()).getHandle(), level, health(level, bad.getMaxHealth())));
                break;
            case PIG_ZOMBIE:
                if (((CraftEntity) bad).getHandle() instanceof StatsEntityPigZombie) {
                    return bad;
                }
                good = new CraftPigZombie((CraftServer) bad.getServer(), new StatsEntityPigZombie(((CraftWorld) bad.getWorld()).getHandle(), level, health(level, bad.getMaxHealth())));
                break;
            case SHEEP:
                if (((CraftEntity) bad).getHandle() instanceof StatsEntitySheep) {
                    return bad;
                }
                good = new CraftSheep((CraftServer) bad.getServer(), new StatsEntitySheep(((CraftWorld) bad.getWorld()).getHandle(), level, health(level, bad.getMaxHealth())));
                break;
            case SILVERFISH:
                if (((CraftEntity) bad).getHandle() instanceof StatsEntitySilverfish) {
                    return bad;
                }
                good = new CraftSilverfish((CraftServer) bad.getServer(), new StatsEntitySilverfish(((CraftWorld) bad.getWorld()).getHandle(), level, health(level, bad.getMaxHealth())));
                break;
            case SKELETON:
                if (((CraftEntity) bad).getHandle() instanceof StatsEntitySkeleton) {
                    return bad;
                }
                good = new CraftSkeleton((CraftServer) bad.getServer(), new StatsEntitySkeleton(((CraftWorld) bad.getWorld()).getHandle(), level, health(level, bad.getMaxHealth())));
                break;
            case SLIME:
                if (((CraftEntity) bad).getHandle() instanceof StatsEntitySlime) {
                    return bad;
                }
                good = new CraftSlime((CraftServer) bad.getServer(), new StatsEntitySlime(((CraftWorld) bad.getWorld()).getHandle(), level, health(level, bad.getMaxHealth())));
                break;
            case SNOWMAN:
                if (((CraftEntity) bad).getHandle() instanceof StatsEntitySnowman) {
                    return bad;
                }
                good = new CraftSnowman((CraftServer) bad.getServer(), new StatsEntitySnowman(((CraftWorld) bad.getWorld()).getHandle(), level, health(level, bad.getMaxHealth())));
                break;
            case SPIDER:
                if (((CraftEntity) bad).getHandle() instanceof StatsEntitySpider) {
                    return bad;
                }
                good = new CraftSpider((CraftServer) bad.getServer(), new StatsEntitySpider(((CraftWorld) bad.getWorld()).getHandle(), level, health(level, bad.getMaxHealth())));
                break;
            case SQUID:
                if (((CraftEntity) bad).getHandle() instanceof StatsEntitySquid) {
                    return bad;
                }
                good = new CraftSquid((CraftServer) bad.getServer(), new StatsEntitySquid(((CraftWorld) bad.getWorld()).getHandle(), level, health(level, bad.getMaxHealth())));
                break;
            case VILLAGER:
                if (((CraftEntity) bad).getHandle() instanceof StatsEntityVillager) {
                    return bad;
                }
                good = new CraftVillager((CraftServer) bad.getServer(), new StatsEntityVillager(((CraftWorld) bad.getWorld()).getHandle(), level, health(level, bad.getMaxHealth())));
                break;
            case WOLF:
                if (((CraftEntity) bad).getHandle() instanceof StatsEntityWolf) {
                    return bad;
                }
                good = new CraftWolf((CraftServer) bad.getServer(), new StatsEntityWolf(((CraftWorld) bad.getWorld()).getHandle(), level, health(level, bad.getMaxHealth())));
                break;
            case ZOMBIE:
                if (((CraftEntity) bad).getHandle() instanceof StatsEntityZombie) {
                    return bad;
                }
                good = new CraftZombie((CraftServer) bad.getServer(), new StatsEntityZombie(((CraftWorld) bad.getWorld()).getHandle(), level, health(level, bad.getMaxHealth())));
                break;
        }
        good.getHandle().setPosition(loco.getX(), loco.getY(), loco.getZ());
        good.setHealth(good.getMaxHealth());
        net.minecraft.server.World worl = ((CraftWorld) world).getHandle();
        if (removeOtherEntity) {
            worl.removeEntity(((CraftEntity) bad).getHandle());
        }
        worl.addEntity(good.getHandle(), reason);
        return good;
    }
    
    /**
     * Sets the Entity's level based on its Location.
     * 
     * @param entity The Entity to have its level set.
     */
    public void setLevel(LivingEntity entity) {
        replaceEntity(entity, SpawnReason.CUSTOM, true);
    }
    
    /**
     * Gets if a mob's stats should be modified by the plugin.
     * 
     * @param type The type of the Entity that is being checked for if it is affected.
     * @return Whether the entity is affected or not.
     */
    public boolean isAffected(EntityType type) {
        if (type.equals(EntityType.PLAYER)) return false;
        if (!useAffectedMobs) return true;
        return affectedMobs.contains(type);
    }
    
    /**
     * Returns the level of a mob at the given distance from the origin.
     * 
     * @param distance The distance of the mob from the closest origin.
     * @return The mobs level.
     */
    public int level(double distance) {
        return Double.valueOf(Math.floor(zones.solve(distance, 0))).intValue();
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
     * Gets each Drop to check for if it should drop and then drops if it is supposed to.
     * 
     * @param event The EntityDeathEvent that was thrown and holds information for the Drops.
     */
    public void dropItems(LivingEntity entity) {
        for (Drop x : drops) {
            x.drop(entity);
        }
    }
    
    /**
     * Finds the distance to the closest origin from a given Location.
     * 
     * @param loco The Location of the mob.
     * @return The distance to the closest origin.
     */
    public double closestOriginDistance(Location loco) {
        ArrayList<Location> allLoc = origins.get(loco.getWorld());
        if (allLoc == null || allLoc.isEmpty()) {
            setupOrigins();
            allLoc = origins.get(loco.getWorld());
        }
        double closest = loco.distance(allLoc.get(0));
        for (Location x : allLoc) {
            double temp = loco.distance(x);
            if (temp < closest) closest = temp;
        }
        return closest;
    }
    
    /**
     * Replaces all Entities that aren't StatsEntities but should be with StatsEntities.
     */
    public void replaceAllWrongEntities() {
        List<World> worlds = getServer().getWorlds();
        for (World world : worlds) {
            List<LivingEntity> entities = world.getLivingEntities();
            for (LivingEntity entity : entities) {
                if (!isAffected(entity.getType())) {
                    continue;
                }
                if (((CraftEntity) entity).getHandle() instanceof StatsEntity) {
                    continue;
                }
                replaceEntity(entity, SpawnReason.CUSTOM, true);
            }
        }
    }
    
    /**
     * Registers the given class to the given ids.
     * 
     * @param entity The class to register
     * @param name The name id of the entity
     * @param id The int id of the entity
     */
    public void executeEntityTypesA(Class entity, String name, int id) {
        try {
            Class[] args = new Class[3];
            args[0] = Class.class;
            args[1] = String.class;
            args[2] = int.class;
            Method a = EntityTypes.class.getDeclaredMethod("a", args);
            a.setAccessible(true);
            a.invoke(a, entity, name, id);
        } catch (NoSuchMethodException ex) {
            System.out.println("[" + info.getName() + "] Error: " + ex.getMessage());
        } catch (IllegalAccessException ex) {
            System.out.println("[" + info.getName() + "] Error: " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            System.out.println("[" + info.getName() + "] Error: " + ex.getMessage());
        } catch (InvocationTargetException ex) {
            System.out.println("[" + info.getName() + "] Error: " + ex.getMessage());
        }
    }
    
    private void loadNotifications() {
        File note = new File(getDataFolder(), "notifications.data");
        if (!note.exists()) {
            notifications = new HashMap<String, Boolean>();
            return;
        }
        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(new FileInputStream(note));
            notifications = (HashMap<String, Boolean>) in.readObject();
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
            killMessage = "";
            deathMessage = "";
            sendMessage = false;
            sendTpMessage = false;
            sendJoinMessage = false;
            sendRespawnMessage = false;
            sendPortalMessage = false;
            sendKillMessage = false;
            sendDeathMessage = false;
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
        if (!config.contains("Messages.Kill Message")) {
            killMessage = "";
            sendKillMessage = false;
        }
        else if (config.isBoolean("Messages.Kill Message")) {
            if (!config.getBoolean("Messages.Kill Message")) {
                sendKillMessage = false;
            }
            else {
                killMessage = "true";
                sendKillMessage = true;
            }
        }
        else if (config.isString("Messages.Kill Message")) {
            if (config.getString("Messages.Kill Message").equalsIgnoreCase("false")) {
                sendKillMessage = false;
            }
            else {
                killMessage = config.getString("Messages.Kill Message");
                sendKillMessage = true;
            }
        }
        if (!config.contains("Messages.Death Message")) {
            deathMessage = "";
            sendDeathMessage = false;
        }
        else if (config.isBoolean("Messages.Death Message")) {
            if (!config.getBoolean("Messages.Death Message")) {
                sendDeathMessage = false;
            }
            else {
                deathMessage = "true";
                sendDeathMessage = true;
            }
        }
        else if (config.isString("Messages.Death Message")) {
            if (config.getString("Messages.Death Message").equalsIgnoreCase("false")) {
                sendDeathMessage = false;
            }
            else {
                deathMessage = config.getString("Messages.Death Message");
                sendDeathMessage = true;
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
        if (!getConfig().contains(path)) {
            return new QMD(0, 0, 1, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
        }
        if (getConfig().getString(path + ".Type").equalsIgnoreCase("quadratic")) {
            double a, b, c, max, min;
            a = getConfig().getDouble(path + ".a");
            b = getConfig().getDouble(path + ".b");
            c = getConfig().getDouble(path + ".c");
            if (getConfig().contains(path + ".max")) {
                max = getConfig().getDouble(path + ".max");
            }
            else {
                max = Double.POSITIVE_INFINITY;
            }
            if (getConfig().contains(path + "min")) {
                min = getConfig().getDouble(path + ".min");
            }
            else {
                min = Double.NEGATIVE_INFINITY;
            }
            return new Quadratic(a, b, c, max, min);
        }
        if (getConfig().getString(path + ".Type").equalsIgnoreCase("QWD")) {
            double a, b, c, max, min;
            boolean aDef, bDef, cDef;
            String hold;
            hold = getConfig().getString(path + ".a");
            if (hold.contains("d")) {
                aDef = true;
                hold = hold.replaceAll("d", "");
                System.out.println(hold);
            }
            else aDef = false;
            a = Double.parseDouble(hold);
            hold = getConfig().getString(path + ".b");
            if (hold.contains("d")) {
                bDef = true;
                hold = hold.replaceAll("d", "");
            }
            else bDef = false;
            b = Double.parseDouble(hold);
            hold = getConfig().getString(path + ".c");
            if (hold.contains("d")) {
                cDef = true;
                hold = hold.replaceAll("d", "");
            }
            else cDef = false;
            c = Double.parseDouble(hold);
            if (getConfig().contains(path + ".max")) {
                max = getConfig().getDouble(path + ".max");
            }
            else {
                max = Double.POSITIVE_INFINITY;
            }
            if (getConfig().contains(path + "min")) {
                min = getConfig().getDouble(path + ".min");
            }
            else {
                min = Double.NEGATIVE_INFINITY;
            }
            return new QWD(a, b, c, max, min, aDef, bDef, cDef);
        }
        if (getConfig().getString(path + ".Type").equalsIgnoreCase("QMD")) {
            double a, b, c, max, min;
            a = getConfig().getDouble(path + ".a");
            b = getConfig().getDouble(path + ".b");
            c = getConfig().getDouble(path + ".c");
            if (getConfig().contains(path + ".max")) {
                max = getConfig().getDouble(path + ".max");
            }
            else {
                max = Double.POSITIVE_INFINITY;
            }
            if (getConfig().contains(path + "min")) {
                min = getConfig().getDouble(path + ".min");
            }
            else {
                min = Double.NEGATIVE_INFINITY;
            }
            return new QMD(a, b, c, max, min);
        }
        if (getConfig().getString(path + ".Type").equalsIgnoreCase("exponential")) {
            double a, b, c, d, f, max, min;
            a = getConfig().getDouble(path + ".a");
            b = getConfig().getDouble(path + ".b");
            c = getConfig().getDouble(path + ".c");
            d = getConfig().getDouble(path + ".d");
            f = getConfig().getDouble(path + ".f");
            if (getConfig().contains(path + ".max")) {
                max = getConfig().getDouble(path + ".max");
            }
            else {
                max = Double.POSITIVE_INFINITY;
            }
            if (getConfig().contains(path + "min")) {
                min = getConfig().getDouble(path + ".min");
            }
            else {
                min = Double.NEGATIVE_INFINITY;
            }
            return new Exponential(a, b, c, d, f, max, min);
        }
        if (getConfig().getString(path + ".Type").equalsIgnoreCase("EWD")) {
            double a, b, c, d, f, max, min;
            boolean aDef, bDef, cDef, dDef, fDef;
            String hold;
            hold = getConfig().getString(path + ".a");
            if (hold.contains("d")) {
                aDef = true;
                hold = hold.replaceAll("d", "");
            }
            else aDef = false;
            a = Double.parseDouble(hold);
            hold = getConfig().getString(path + ".b");
            if (hold.contains("d")) {
                bDef = true;
                hold = hold.replaceAll("d", "");
            }
            else bDef = false;
            b = Double.parseDouble(hold);
            hold = getConfig().getString(path + ".c");
            if (hold.contains("d")) {
                cDef = true;
                hold = hold.replaceAll("d", "");
            }
            else cDef = false;
            c = Double.parseDouble(hold);
            hold = getConfig().getString(path + ".d");
            if (hold.contains("d")) {
                dDef = true;
                hold = hold.replaceAll("d", "");
            }
            else dDef = false;
            d = Double.parseDouble(hold);
            hold = getConfig().getString(path + ".f");
            if (hold.contains("d")) {
                fDef = true;
                hold = hold.replaceAll("d", "");
            }
            else fDef = false;
            f = Double.parseDouble(hold);
            if (getConfig().contains(path + ".max")) {
                max = getConfig().getDouble(path + ".max");
            }
            else {
                max = Double.POSITIVE_INFINITY;
            }
            if (getConfig().contains(path + "min")) {
                min = getConfig().getDouble(path + ".min");
            }
            else {
                min = Double.NEGATIVE_INFINITY;
            }
            return new EWD(a, b, c, d, f, max, min, aDef, bDef, cDef, dDef, fDef);
        }
        if (getConfig().getString(path + ".Type").equalsIgnoreCase("EMD")) {
            double a, b, c, d, f, max, min;
            a = getConfig().getDouble(path + ".a");
            b = getConfig().getDouble(path + ".b");
            c = getConfig().getDouble(path + ".c");
            d = getConfig().getDouble(path + ".d");
            f = getConfig().getDouble(path + ".f");
            if (getConfig().contains(path + ".max")) {
                max = getConfig().getDouble(path + ".max");
            }
            else {
                max = Double.POSITIVE_INFINITY;
            }
            if (getConfig().contains(path + "min")) {
                min = getConfig().getDouble(path + ".min");
            }
            else {
                min = Double.NEGATIVE_INFINITY;
            }
            return new EMD(a, b, c, d, f, max, min);
        }
        return new QMD(0, 0, 1, 1, 1);//Just makes it so that it isn't modified, it creates this function: ⨍(x) = 1 and then returns ⨍(x)(the default value).
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
    
    /**
     * Creates a list of all the entity types that are alive except players.
     * 
     * @return List of all living non-player entities.
     */
    private ArrayList<EntityType> getListOfAllTypes() {
        ArrayList<EntityType> entities = new ArrayList<EntityType>();
        EntityType[] entity = EntityType.values();
        for (EntityType ent : entity) {
            if (ent.isAlive() && ent != EntityType.PLAYER) {
                entities.add(ent);
            }
        }
        return entities;
    }
    
    public Class getEntityClass(EntityType ent) {
        switch (ent) {
            case BLAZE:
                return StatsEntityBlaze.class;
            case CAVE_SPIDER:
                return StatsEntityCaveSpider.class;
            case CHICKEN:
                return StatsEntityChicken.class;
            case COW:
                return StatsEntityCow.class;
            case CREEPER:
                return StatsEntityCreeper.class;
            case ENDER_DRAGON:
                return StatsEntityEnderDragon.class;
            case ENDERMAN:
                return StatsEntityEnderman.class;
            case GHAST:
                return StatsEntityGhast.class;
            case GIANT:
                return StatsEntityGiant.class;
            case IRON_GOLEM:
                return StatsEntityIronGolem.class;
            case MAGMA_CUBE:
                return StatsEntityMagmaCube.class;
            case MUSHROOM_COW:
                return StatsEntityMushroomCow.class;
            case OCELOT:
                return StatsEntityOcelot.class;
            case PIG:
                return StatsEntityPig.class;
            case PIG_ZOMBIE:
                return StatsEntityPigZombie.class;
            case SHEEP:
                return StatsEntitySheep.class;
            case SILVERFISH:
                return StatsEntitySilverfish.class;
            case SKELETON:
                return StatsEntitySkeleton.class;
            case SLIME:
                return StatsEntitySlime.class;
            case SNOWMAN:
                return StatsEntitySnowman.class;
            case SPIDER:
                return StatsEntitySpider.class;
            case SQUID:
                return StatsEntitySquid.class;
            case VILLAGER:
                return StatsEntityVillager.class;
            case WOLF:
                return StatsEntityWolf.class;
            case ZOMBIE:
                return StatsEntityZombie.class;
        }
        return null;
    }
    
    private void registerClasses() {
        for (EntityType ent : getListOfAllTypes()) {
            if (isAffected(ent) && ent.isAlive()) {
                executeEntityTypesA(getEntityClass(ent), ent.toString(), ent.getTypeId());
            }
        }
    }
}