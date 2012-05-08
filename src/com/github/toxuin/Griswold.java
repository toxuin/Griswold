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
import org.bukkit.Chunk;
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
import org.bukkit.event.world.ChunkUnloadEvent;
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
	private Set<Chunk> chunks = new HashSet<Chunk>();
	
    private Map<Entity, Integer> FrozenTicks = new HashMap<Entity, Integer>();
    private Map<Entity, Location> FrozenPos = new HashMap<Entity, Location>();
    
	public static Permission permission = null;
    public static Economy economy = null;
    
    public static double version; 
    static String lang = "en_US";
 
	public void onEnable(){
		directory = this.getDataFolder();
		PluginDescriptionFile pdfFile = this.getDescription();
		version = Double.parseDouble(pdfFile.getVersion());
		prefix = "[" + pdfFile.getName()+ "]: ";

		this.getServer().getPluginManager().registerEvents(this, this);
		
		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Frosttouch_freezeController(), 0, 5);

		this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Starter(), 20);
		
		log.info( prefix + "Enabled! Version: " + version);
	}

	public void onDisable(){
        despawnAll();
		log.info( prefix + "Disabled.");
	}
	
	// MAKE THEM INVINCIBLE
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onEntityDamage(EntityDamageEvent event) {
		if (repairmen.isEmpty()) return;
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
		if (permission != null) {
			if (!permission.has(event.getPlayer(), "griswold.tools") || !permission.has(event.getPlayer(), "griswold.armor")) return;
		}
		for (Repairer rep : repairmen) {
			if (event.getRightClicked().equals(rep.entity)) {
				Interactor.interact(event.getPlayer(), rep);
			}
		}
	}
	
	// PREVENT DESPAWN
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onChunkUnload(ChunkUnloadEvent event) {
		if (chunks.isEmpty()) return;
		for (Chunk chunk : chunks){
			if (event.getChunk().equals(chunk)) {
				chunk.getWorld().loadChunk(chunk);
				event.setCancelled(true);
			}
		}
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		
		if(cmd.getName().equalsIgnoreCase("blacksmith")) {
			if (args.length > 0) {
				if (args[0].equalsIgnoreCase("reload")) {
					if (sender.isOp() || sender instanceof ConsoleCommandSender || permission.has(sender, "griswold.admin")) { 
						reloadPlugin();
					} else {
						sender.sendMessage(ChatColor.RED+Lang.error_accesslevel);
						return false;
					}
					return true;
				}
				if (args[0].equalsIgnoreCase("create")) {
					if ((permission == null && sender.isOp()) || (sender instanceof Player && (permission.has(sender, "griswold.admin") || sender.isOp()))) {
						if (args.length >= 2) {
							Player player = (Player) sender;
							Location location = player.getLocation().toVector().add(player.getLocation().getDirection().multiply(3)).toLocation(player.getWorld());
							location.setY(Math.round(player.getLocation().getY()));
							String name = args[1];
							if (args.length < 4) createRepairman(name, location); else {
								String type = args[2];
								String cost = args[3];
								createRepairman(name, location, type, cost);
								player.sendMessage(Lang.new_created);
							}
						} else sender.sendMessage(Lang.insufficient_params);
					} else {
						sender.sendMessage(ChatColor.RED+Lang.error_accesslevel);
						return false;
					}
					return true;
				}
				if (args[0].equalsIgnoreCase("remove")) {
					if (permission != null) {
						if (args.length>1 && permission.has(sender, "griswold.admin")) {
							removeRepairman(args[1]);
							sender.sendMessage(String.format(Lang.deleted, args[1]));
						} else {
							sender.sendMessage(ChatColor.RED+Lang.error_accesslevel);
						}
					} else {
						if (sender instanceof ConsoleCommandSender || sender.isOp()) {
							removeRepairman(args[1]);
							sender.sendMessage(String.format(Lang.deleted, args[1]));
						}
					}
				}
				if (args[0].equalsIgnoreCase("list")) {
					if (permission != null) {
						if (permission.has(sender, "griswold.admin")) listRepairmen(sender);
					} else {
						if (sender instanceof ConsoleCommandSender || sender.isOp())  listRepairmen(sender);
					}
				}
				if (args[0].equalsIgnoreCase("despawn")) {
					if (permission != null) {
						if (permission.has(sender, "griswold.admin")) {
							despawnAll();
							sender.sendMessage(Lang.despawned);
						} else {
							sender.sendMessage(ChatColor.RED+Lang.error_accesslevel);
						}
					} else {
						if (sender instanceof ConsoleCommandSender || sender.isOp()) {
							despawnAll();
							sender.sendMessage(Lang.despawned);
						}
					}
				}
				if (args[0].equalsIgnoreCase("respawn")) {
					if (permission != null) {
						if (permission.has(sender, "griswold.admin")) {
							respawnAll();
							sender.sendMessage(Lang.respawned);
						} else {
							sender.sendMessage(ChatColor.RED+Lang.error_accesslevel);
						}
					} else {
						if (sender instanceof ConsoleCommandSender || sender.isOp()) {
							respawnAll();
							sender.sendMessage(Lang.respawned);
						}
					}
				}
			}
		}
		return false; 
	}
	
	private void reloadPlugin() {
		despawnAll();
		readConfig();
	}
	
	private void createRepairman(String name, Location loc) {
		createRepairman(name, loc, "all", "1");
	}
	
	private void createRepairman(String name, Location loc, String type, String cost) {
		boolean found = false;
		for (Repairer rep : repairmen) {
			if (rep.name.equalsIgnoreCase(name)) found = true;
		}
		if (found) {
			log.info(prefix+String.format(Lang.repairman_exists, name));
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
		if (config.isConfigurationSection("repairmen."+name)){
			config.set("repairmen."+name, null);
			try {
				config.save(configFile); 
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
		} else {
			log.info(prefix+Lang.error_remove);
			return;
		}
		reloadPlugin();
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
		
		chunks.add(loc.getChunk());

		loc.getWorld().loadChunk(loc.getChunk());

		if (debug) {
			log.info(prefix+String.format(Lang.repairman_spawn, squidward.entity.getEntityId(), loc.getX(), loc.getY(), loc.getZ()));
		}
	}
	
	private void readConfig() {

    	Lang.createLangFile();
		
		configFile = new File(directory,"config.yml");
        config = YamlConfiguration.loadConfiguration(configFile);
        
        repairmen.clear();
        
        if (configFile.exists()) {
        	debug = config.getBoolean("Debug");
        	timeout = config.getInt("Timeout");
        	lang = config.getString("Language");
        	
        	if (Double.parseDouble(config.getString("Version")) != version) {
        		updateConfig(config.getString("Version"));
        	}
        	
        	
        	Lang.checkLangVersion(lang);
			Lang.init();
			
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
	        		squidward.cost = config.getDouble("repairmen."+repairman+".cost");
	        		
	        		spawnRepairman(squidward);
	        	}
        	}
        	
        	Interactor.basicArmorPrice = config.getDouble("BasicArmorPrice");
        	Interactor.basicToolsPrice = config.getDouble("BasicToolPrice");
        	Interactor.enchantmentPrice = config.getDouble("BasicEnchantmentPrice");
        	
        	if(debug) {
        		log.info(prefix+String.format(Lang.debug_loaded, repairmen.size()));
        	}
        	
        	log.info(prefix+Lang.config_loaded);
        } else {
        	config.set("Timeout", 5000);
        	config.set("Language", "en_US");
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
	}
	
	private void updateConfig(String oldVersion) {
		// íìí éóÖçú ÇÄÜçõÖ òíìäà, äéíéêõÖ ÑéãÜçõ éÅçéÇãüíú äéçîàÉ.
        	config.set("Version", version); // éÉÄ.
        	try {
        		config.save(configFile); 
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
	}

	private class Starter implements Runnable {
		@Override
		public void run() {
			despawnAll();
			readConfig();

			if (!setupEconomy()) log.info(prefix+Lang.economy_not_found);
			if (!setupPermissions()) log.info(prefix+Lang.permissions_not_found);
			
		}
		
	}
	
	// NONE OF MY CODE BELOW THIS LINE
	// -------------------------------


    private boolean setupPermissions()
    {
    	if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
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
