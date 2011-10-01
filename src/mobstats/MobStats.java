package mobstats;

import java.util.HashMap;
import java.util.List;
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
    public HashMap<World, Location> spawns = new HashMap<World, Location>();
    public HashMap<UUID, Integer> levels = new HashMap<UUID, Integer>();
    
    @Override
    public void onDisable() {
        info = getDescription();
        
        System.out.println("[" + info.getName() + "] disabled");
    }
    
    @Override
    public void onEnable() {
        info = getDescription();
        manager = getServer().getPluginManager();
        
        System.out.println("[" + info.getName() + "] ENABLED");
        
        manager.registerEvent(Type.ENTITY_DAMAGE, new Entities(this), Priority.Normal, this);
        manager.registerEvent(Type.CREATURE_SPAWN, new Entities(this), Priority.Normal, this);
        
        manager.registerEvent(Type.PLAYER_MOVE, new Players(this), Priority.Normal, this);
        manager.registerEvent(Type.PLAYER_JOIN, new Players(this), Priority.Normal, this);
        manager.registerEvent(Type.PLAYER_TELEPORT, new Players(this), Priority.Normal, this);
        manager.registerEvent(Type.PLAYER_RESPAWN, new Players(this), Priority.Normal, this);
        manager.registerEvent(Type.PLAYER_PORTAL, new Players(this), Priority.Normal, this);
        
        getCommand("zone").setExecutor(new Commands());
        
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
        int level = (int) distance/16;
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