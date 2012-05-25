package mobstats;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import mobstats.listeners.Commands;
import mobstats.listeners.Entities;
import mobstats.listeners.Players;

import net.milkbowl.vault.economy.Economy;

import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
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
    private PluginManager manager;
    private ArrayList<Drop> drops;
    private boolean useMoney;
    
    public static Economy economy = null;
    
    /**
     * This method is called when the plugin disables.
     */
    @Override
    public void onDisable() {
        
    }
    
    /**
     * This method is called when the plugin is enabled and it serves as the main method of the plugin. It goes through the process of creating files, reading files, and setting variables.
     */
    @Override
    public void onEnable() {
        info = getDescription();
        origins = new HashMap<World, ArrayList<Location>>();
        manager = getServer().getPluginManager();
        healthOfMobs = new HashMap<UUID, Integer>();
        
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            saveDefaultConfig();
        }
        config = getConfig();
        
        getMessages();
        setupOrigins();
        setupCash();
        zones = getEquation("Equations.Zone");
        damage = getEquation("Equations.Damage");
        health = getEquation("Equations.Health");
        xp = getEquation("Equations.XP");
        setupDrops();
        setupAffectedMobs();
        
        manager.registerEvents(new Entities(this), this);
        manager.registerEvents(new Players(this), this);
        
        getCommand("zone").setExecutor(new Commands(this));
        
        levels = new HashMap<UUID, Integer>();
        List<World> worlds = getServer().getWorlds();
        for (World world : worlds) {
            List<LivingEntity> entities = world.getLivingEntities();
            for (LivingEntity entity : entities) {
                levels.put(entity.getUniqueId(), level(closestOriginDistance(entity.getLocation())));
                entity.setHealth(entity.getMaxHealth());
                healthOfMobs.put(entity.getUniqueId(), health(getLevel(entity)));
            }
        }
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
        if (healthOfMobs.get(id) != null) healthOfMobs.remove(id);
        healthOfMobs.put(id, amount);
    }
    
    /**
     * Subtracts the given amount of health from the given entity.
     * 
     * @param entity The entity to damage.
     * @param damage The damage to take off of the entity's health.
     */
    public void subtractHealth(LivingEntity entity, int damage, Entity damager) {
        UUID id = entity.getUniqueId();
        int health;
        if (healthOfMobs.get(id) != null) health = healthOfMobs.get(id);
        else health = health(getLevel(entity));
        setHealth(entity, health - damage);
        if (healthOfMobs.get(id) <= 0) {
            if (damager instanceof Player && useMoney) {
                Player damagePer = (Player) damager;
                EconomyResponse depositPlayer = economy.depositPlayer(damagePer.getName(), cash(getLevel(entity)));
            }
            entity.setHealth(0);
        }
    }
    
    /**
     * Sets the Entity's level based on its Location.
     * 
     * @param entity The Entity to have its level set.
     */
    public void setLevel(Entity entity) {
        if (levels.containsKey(entity.getUniqueId())) levels.remove(entity.getUniqueId());
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
        return affectedMobs.contains(type);
    }
    
    /**
     * Returns the level of a mob at the given distance from the origin.
     * 
     * @param distance The distance of the mob from the closest origin.
     * @return The mobs level.
     */
    public int level(double distance) {
        return (int) zones.solve(distance);
    }
    
    /**
     * Gets the damage that a mob of the given level will deal.
     * 
     * @param level The mob's level.
     * @return The damage that a mob of the given level will deal.
     */
    public int damage(int level) {
        return (int) damage.solve(level);
    }
    
    /**
     * Gets the health that a mob of the given level will have.
     * 
     * @param level The mob's level.
     * @return The health that a mob of the given level will have.
     */
    public int health(int level) {
        return (int) health.solve(level);
    }
    
    /**
     * Gets the amount of xp that a mob of the given level will drop upon dying.
     * 
     * @param level The mob's level.
     * @return The xp that a mob of the given level will drop upon dying.
     */
    public int xp(int level) {
        return (int) xp.solve(level);
    }
    
    /**
     * Gets the amount of money that a mob at the given level will drop upon dying.
     * 
     * @param level
     * @return 
     */
    public double cash(int level) {
        if (!useMoney) return 0;
        return cash.solve(level);
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
}