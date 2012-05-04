package com.github.toxuin;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Entity;

public class Griswold extends JavaPlugin implements Listener{
	public static File directory;
	public static String prefix = null;
	
	public static boolean debug = false;
	
	public static int timeout = 5000;
	
	private static FileConfiguration config = null;
	private static File configFile = null;
	private Logger log = Logger.getLogger("Minecraft");
	
	public static Set<Repairer> repairmen = new HashSet<Repairer>();
	
    private Map<Entity, Integer> FrozenTicks = new HashMap<Entity, Integer>();
    private Map<Entity, Location> FrozenPos = new HashMap<Entity, Location>();
    
	public static Permission permission = null;
    public static Economy economy = null;
    
    private String lang = "ru_RU";
 
	public void onEnable(){
		directory = this.getDataFolder();
		PluginDescriptionFile pdfFile = this.getDescription();
		prefix = "[" + pdfFile.getName()+ "]: ";

		
		this.getServer().getPluginManager().registerEvents(this, this);
		
		readConfig();
		Lang.init(lang);
		
		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new  Frosttouch_freezeController(), 0, 1);
		
		if (!setupEconomy()) log.info(prefix+Lang.economy_not_found);
        setupPermissions();
		
		log.info( prefix + "Enabled! Version: " + pdfFile.getVersion());
	}

	public void onDisable(){
        despawnAll();
		log.info( prefix + "Disabled.");
	}
	
	// MAKE THEM INVINCIBLE
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onEntityDamage(EntityDamageEvent event) {
		for (Repairer rep : repairmen) {
			if (event.getEntity().equals(rep.entity)) {
				event.setDamage(0);
				event.setCancelled(true);
			}
		}
	}

	// MAKE INTERACTION
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		if (!event.getPlayer().hasPermission("griswold.tools") || !event.getPlayer().hasPermission("griswold.armor")) return;
		for (Repairer rep : repairmen) {
			if (event.getRightClicked().equals(rep.entity)) {
				Interactor.interact(event.getPlayer(), rep);
			}
		}
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		
		if(cmd.getName().equalsIgnoreCase("blacksmith")) {
			if (args.length > 0) {
				if (args[0].equalsIgnoreCase("reload")) {
					if (sender instanceof ConsoleCommandSender || permission.has(sender, "griswold.admin")) {
						despawnAll();
						readConfig();
						Lang.init(lang);
					}
					return true;
				}
				if (args[0].equalsIgnoreCase("create")) {
					if (sender instanceof Player && permission.has(sender, "griswold.admin")) {
						if (args.length >= 2) {
							Player player = (Player) sender;
							Location location = player.getLocation().toVector().add(player.getLocation().getDirection().multiply(3)).toLocation(player.getWorld());
							location.setY(Math.round(player.getLocation().getY()));
							String name = args[1];
							if (args.length < 4) createRepairman(name, location); else {
								String type = args[2];
								String cost = args[3];
								createRepairman(name, location, type, cost);
							}
						} else sender.sendMessage(Lang.insufficient_params);
					} else return false;
					return true;
				}
				if (args[0].equalsIgnoreCase("remove")) {
					if (args.length>1 && permission.has(sender, "griswold.admin")) removeRepairman(args[2]);
				}
				if (args[0].equalsIgnoreCase("list")) {
					if (permission.has(sender, "griswold.admin")) listRepairmen(sender);
				}
				if (args[0].equalsIgnoreCase("despawn")) {
					if (permission.has(sender, "griswold.admin")) despawnAll();
				}
				if (args[0].equalsIgnoreCase("respawn")) {
					if (permission.has(sender, "griswold.admin")) respawnAll();
				}
			}
		}
		return false; 
	}
	
	private void createRepairman(String name, Location loc) {
		createRepairman(name, loc, "all", "1");
	}
	
	private void createRepairman(String name, Location loc, String type, String cost) {
		boolean found = false;
		for (Repairer rep : repairmen) {
			if (rep.name == name) found = true;
		}
		if (found) {
			log.info(prefix+"ERROR: repairman " + name +" already exists!");
			return;
		}
			
		config.set("repairmen."+name+".world", loc.getWorld().getName());
		config.set("repairmen."+name+".X", loc.getX());
		config.set("repairmen."+name+".Y", loc.getY());
		config.set("repairmen."+name+".Z", loc.getZ());
		config.set("repairmen."+name+".type", type);
		config.set("repairmen."+name+".cost", Double.parseDouble(cost));
    	
    	try {
    		config.save(configFile);
    	} catch (Exception e) {
    		log.info(prefix+Lang.error_config);
    		e.printStackTrace();
    	}
		
    	Repairer squidward = new Repairer();
    	squidward.name = name;
    	squidward.loc = loc;
    	squidward.type = type;
    	squidward.cost = Double.parseDouble(cost);
		spawnRepairman(squidward);
	}
	
	private void removeRepairman(String name) {
		config.set("repairmen."+name, null);
		
		Repairer squidward = new Repairer();
		
		for (Repairer rep : repairmen) {
			if (rep.name == name) {
				squidward = rep;
			}
		}
		
		if (squidward.entity != null) repairmen.remove(squidward); else log.info(prefix+Lang.error_remove);
	}
	
	private void listRepairmen(CommandSender sender){
		String result = "";
		for (Repairer rep : repairmen) {
			result = result + rep.name + ", ";
		}
		if (result != "") {
			sender.sendMessage(ChatColor.GREEN+Lang.repairman_list);
			sender.sendMessage(result);
		}
	}
	
	private void despawnAll() {
		for (Repairer rep : repairmen) {
			rep.entity.remove();
		}
		FrozenTicks.clear();
		FrozenPos.clear();
		repairmen.clear();
	}
	
	private void respawnAll() {
		despawnAll();
		for (Repairer rep : repairmen) {
			spawnRepairman(rep);
		}
	}
	
	
	private void spawnRepairman (Repairer squidward) {
		Location loc = squidward.loc;
		if (loc == null) return;
		LivingEntity repairman = loc.getWorld().spawnCreature(loc, EntityType.VILLAGER);
		FrozenTicks.put(repairman, 5);
		FrozenPos.put(repairman, loc);
		
		squidward.entity = repairman;
		
		repairmen.add(squidward);

		if (debug) {
			log.info(prefix+"SPAWNED REPAIRMAN ID:" + squidward.entity.getEntityId() + " AT X:"+ loc.getX() + " Y:" + loc.getY() + " Z:" + loc.getZ());
		}
	}
	
	private void readConfig() {
		configFile = new File(directory,"config.yml");
        config = YamlConfiguration.loadConfiguration(configFile);
        
        repairmen.clear();
        
        if (configFile.exists()) {
        	if (config.isConfigurationSection("repairmen")) {
        		Set<String> repairmen = config.getConfigurationSection("repairmen").getKeys(false);
	        	for (String repairman : repairmen) {
	        		Repairer squidward = new Repairer();
	        		squidward.name = repairman;
	        		squidward.loc = new Location(this.getServer().getWorld(config.getString("repairmen."+repairman+".world")),
	        									config.getDouble("repairmen."+repairman+".X"),
	        									config.getDouble("repairmen."+repairman+".Y"),
	        									config.getDouble("repairmen."+repairman+".Z"));
	        		squidward.type = config.getString("repairmen."+repairman+".type");
	        		squidward.cost = config.getDouble("repairmen."+repairman+".type");
	        		
	        		spawnRepairman(squidward);
	        	}
        	}
        	debug = config.getBoolean("Debug");
        	timeout = config.getInt("Timeout");
        	lang = config.getString("Language");

        	Interactor.basicArmorPrice = config.getDouble("BasicArmorPrice");
        	Interactor.basicToolsPrice = config.getDouble("BasicToolPrice");
        	Interactor.enchantmentPrice = config.getDouble("BasicEnchantmentPrice");
        	
        	if(debug) {
        		log.info(prefix+"DEBUG: loaded total "+repairmen.size() + " repairmen.");
        	}
        	
        	log.info(prefix+"Config loaded!");
        } else {
        	config.set("Timeout", 5000);
        	config.set("Language", "ru_RU");
        	config.set("BasicArmorPrice", 10.0);
        	config.set("BasicToolPrice", 10.0);
        	config.set("BasicEnchantmentPrice", 30.0);
        	config.set("Debug", false);
        	config.set("Version", this.getDescription().getVersion());
        	try {
        		config.save(configFile);
        		log.info(prefix+Lang.default_config);
        	} catch (Exception e) {
        		log.info(prefix+Lang.error_create_config);
        		e.printStackTrace();
        	}
        }

    	Lang.createLangFile();
		
	}
	
	// NOT MY CODE BELOW THIS LINE
	// ---------------------------


    private boolean setupPermissions()
    {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        return (permission != null);
    }

    private boolean setupEconomy()
    {
    	if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }

	private class Frosttouch_freezeController extends TimerTask
    {
        //Make sure all frozen mobs are stuck in place
        public void run()
        {
            Set<Entity> keys = FrozenTicks.keySet();
            for(Entity ent : keys)
            {
                //If this has an entry, freeze
                if(FrozenTicks.containsKey(ent) && FrozenTicks.get(ent) > 0)
                {
                    //If moved, move back
                    if(!FrozenPos.get(ent).equals(ent.getLocation()))
                    {
                        ent.teleport(FrozenPos.get(ent));
                    }
                }
            }
        }
    }
}
