package mobstats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import mobstats.listeners.Commands;
import mobstats.listeners.Entities;
import mobstats.listeners.Players;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * This is the main class of the plugin.
 * 
 * This class handles the enabling and disabling of the plugin, reads files, and has various methods to get important information for the rest of the plugin.
 * 
 * @author Justin Stauch (gamerguy14)
 * @since February 14, 2012
 * @see org.bukkit.plugin.java.JavaPlugin
 */
public class MobStats extends JavaPlugin {
    private PluginDescriptionFile info;
    private FileConfiguration config;
    private Map<World, ArrayList<Location>> origins;
    private String message, joinMessage, portalMessage, respawnMessage, tpMessage;
    private boolean sendMessage, sendJoinMessage, sendPortalMessage, sendRespawnMessage, sendTpMessage;
    private StatSolver zones, damage, health, xp;
    private ArrayList<EntityType> affectedMobs;
    private boolean useAffectedMobs;
    private HashMap<UUID, Integer> levels;
    private PluginManager manager;
    
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
        
        getConfig().options().copyDefaults(true);
        saveConfig();
        config = getConfig();
        
        getMessages();
        setupOrigins();
        zones = getEquation("Equations.Zone");
        damage = getEquation("Equations.Damage");
        health = getEquation("Equations.Health");
        xp = getEquation("Equations.XP");
        saveConfig();
        
        manager.registerEvents(new Entities(this), this);
        manager.registerEvents(new Players(this), this);
        
        getCommand("zone").setExecutor(new Commands(this));
        
        levels = new HashMap<UUID, Integer>();
        List<World> worlds = getServer().getWorlds();
        for (World world : worlds) {
            List<LivingEntity> entities = world.getLivingEntities();
            for (LivingEntity entity : entities) {
                levels.put(entity.getUniqueId(), level(closestOriginDistance(entity.getLocation())));
                entity.setHealth(health(levels.get(entity.getUniqueId())));
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
                continue;
            }
            List<String> stringOrg = config.getStringList("Origins." + worlds.get(x).getName());
            ArrayList<Location> orgLoc = new ArrayList<Location>();
            for (String i : stringOrg) orgLoc.add(stringToLocation(worlds.get(x), i));
            if (origins.get(worlds.get(x)) == null) origins.remove(worlds.get(x));
            origins.put(worlds.get(x), orgLoc);
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
    public void setupAffectedMobs() {
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
}