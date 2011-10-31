package mobstats;

import java.io.BufferedWriter;
import java.io.File;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.Character;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import mobstats.listeners.Commands;
import mobstats.listeners.Entities;
import mobstats.listeners.Players;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class MobStats extends JavaPlugin {
    private PluginDescriptionFile info;
    private PluginManager manager;
    private File directory, config;
    public HashMap<World, Location> spawns = new HashMap<World, Location>();
    public HashMap<UUID, Integer> levels = new HashMap<UUID, Integer>();
    public int size = 16;
    public boolean console = false;
    public boolean op = false;
    public String message;
    
    @Override
    public void onDisable() {
        info = getDescription();
        
        System.out.println("[" + info.getName() + "] disabled");
    }
    
    @Override
    public void onEnable() {
        info = getDescription();
        manager = getServer().getPluginManager();
        directory = getDataFolder();
        message = "You are now in a level +level zone";
        
        System.out.println("[" + info.getName() + "] ENABLED");
        
        manager.registerEvent(Type.ENTITY_DAMAGE, new Entities(this), Priority.Normal, this);
        manager.registerEvent(Type.CREATURE_SPAWN, new Entities(this), Priority.Normal, this);
        
        manager.registerEvent(Type.PLAYER_MOVE, new Players(this), Priority.Normal, this);
        manager.registerEvent(Type.PLAYER_JOIN, new Players(this), Priority.Normal, this);
        manager.registerEvent(Type.PLAYER_TELEPORT, new Players(this), Priority.Normal, this);
        manager.registerEvent(Type.PLAYER_RESPAWN, new Players(this), Priority.Normal, this);
        manager.registerEvent(Type.PLAYER_PORTAL, new Players(this), Priority.Normal, this);
        
        getCommand("zone").setExecutor(new Commands());
        
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        config = new File (directory, "config.yml");
        
        if (!config.exists()) {
            BufferedWriter out = null;
            try {
                out = new BufferedWriter(new FileWriter(config));
                out.write("#Where it says 'zone:', type the size of the zone that you want.");
                out.newLine();
                out.write("#This number is the distance from the spawn that changes the levels.");
                out.newLine();
                out.write("#eg: 'zone: 16' means that when a player moves away from the spawn, the level of the zone increases every 16 blocks (1 chunk).");
                out.newLine();
                out.write("#Where it says 'message:', write the message you want to be sent to the player when they change zones.");
                out.newLine();
                out.write("#If you type +level, it will be replaced with the level zone that the player just walked into.");
                out.newLine();
                out.write("#Where it says 'console', type 'true' or 'false' for if you want the console to be told that a player changed zones.");
                out.newLine();
                out.write("#Where it says 'op', type 'true' or 'false' for if you want ops to be told that a player has changed zones.");
                out.newLine();
                out.write("#Lines that start with '#' or that are empty are ignored.");
                out.newLine();
                out.newLine();
                out.write("zone: 16");
                out.newLine();
                out.write("message: You have just entered a +level zone");
                out.newLine();
                out.write("console: false");
                out.newLine();
                out.write("op: false");
            } catch (IOException ex) {
                System.out.println("[" + info.getName() + "] Error: " + ex.getMessage());
            } finally {
                try {
                    out.close();
                } catch (IOException ex){
                    System.out.println("[" + info.getName() + "] Error: " + ex.getMessage());
                }
            }
        }
        
        Scanner scan = null;
        try {
            scan = new Scanner(config);
        } catch (IOException ex) {
            System.out.println("[" + info.getName() + "] Error: " + ex.getMessage());
        }
        while (scan.hasNextLine()) {
            String line = scan.nextLine();
            char[] lin = line.toCharArray();
            if (!(lin.length > 0)) continue;
            Character lane = lin[0];
            if (lane.equals('#')) continue;
            String[] parts = line.split(": ");
            if (parts.length != 2) {
                parts = line.split(":");
                if (parts.length != 2) continue;
            }
            if (parts[0].equalsIgnoreCase("zone")) size = Integer.parseInt(parts[1]);
            if (parts[0].equalsIgnoreCase("message")) message = parts[1];
            if (parts[0].equalsIgnoreCase("console")) console = Boolean.parseBoolean(parts[1]);
            if (parts[0].equalsIgnoreCase("op")) op = Boolean.parseBoolean(parts[1]);
        }
        
        
        List<World> worlds = getServer().getWorlds();
        for (int x = 0; x < worlds.size(); x++) {
            List<Entity> entities = worlds.get(x).getEntities();
            spawns.put(worlds.get(x), worlds.get(x).getSpawnLocation());
            for (int y = 0; y < entities.size(); y++) {
                if ((!(entities.get(y) instanceof LivingEntity)) && (!(entities.get(y) instanceof org.bukkit.entity.Player))) continue;
                LivingEntity found = (LivingEntity) entities.get(y);
                double distance = found.getLocation().toVector().distance(spawns.get(found.getWorld()).toVector());
                int level = level(distance);
                UUID id = found.getUniqueId();
                levels.put(id, level);
                found.setHealth(health(levels.get(id)));
            }
        }
    }
    
    public int level(double distance) {
        int level = (int) distance/size;
        return level;
    }
    
    public int damage(int level) {
        int damage = (int) ((int) level * 0.25);
        return damage;
    }
    
    public int health(int level) {
        int health = (int) ((int) level * 0.75);
        return health;
    }
}