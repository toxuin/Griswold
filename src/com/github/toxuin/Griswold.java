package com.github.toxuin;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import net.minecraft.server.*;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.entity.CraftVillager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Logger;

public class Griswold extends JavaPlugin implements Listener{
	public static File directory;
	public static String prefix = null;
	
	public static boolean debug = false;
	
	public static int timeout = 5000;
	
	private static FileConfiguration config = null;
	private static File configFile = null;
	static Logger log = Logger.getLogger("Minecraft");
	
	public static Set<Repairer> repairmen = new HashSet<Repairer>();
	private Set<Chunk> chunks = new HashSet<Chunk>();

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
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		if (permission != null) {
			if (!permission.has(event.getPlayer(), "griswold.tools") ||
				!permission.has(event.getPlayer(), "griswold.armor") ||
				!permission.has(event.getPlayer(), "griswold.enchant")) return;
		}
		for (Repairer rep : repairmen) {
			if (event.getRightClicked().equals(rep.entity)) {
				Interactor.interact(event.getPlayer(), rep);
				event.setCancelled(true);
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
							if (args.length < 4) {
								createRepairman(name, location);
								player.sendMessage(Lang.new_created);
							} else {
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
			} else {
				sender.sendMessage(ChatColor.RED+Lang.error_few_arguments);
				return true;
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
		
    	Repairer meGusta = new Repairer();
    	meGusta.name = name;
    	meGusta.loc = loc;
    	meGusta.type = type;
    	meGusta.cost = Double.parseDouble(cost);
		spawnRepairman(meGusta);
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
	
	private void listRepairmen(CommandSender sender) {
		String result = "";
		for (Repairer rep : repairmen) {
			result = result + rep.name + ", ";
		}
		if (!result.equals("")) {
			sender.sendMessage(ChatColor.GREEN+Lang.repairman_list);
			sender.sendMessage(result);
		}
	}
	
	private void despawnAll() {
		for (Repairer rep : repairmen) {
			rep.entity.remove();
		}
		repairmen.clear();
		chunks.clear();
	}
	
	private void spawnRepairman (Repairer squidward) {
		Location loc = squidward.loc;
		if (loc == null) {
			log.info(prefix+"ERROR: LOCATION "+loc+" IS NULL");
			return;
		}
		if (squidward.type.equals("enchant") && !Interactor.enableEnchants) {
			log.info(prefix+String.format(Lang.error_enchanter_not_spawned, loc.getX(), loc.getY(), loc.getZ()));
			return;
		}
		LivingEntity repairman = (LivingEntity) loc.getWorld().spawn(loc, EntityType.VILLAGER.getEntityClass());

		if (squidward.type.equals("enchant")) {
			((Villager) repairman).setProfession(Profession.LIBRARIAN);
		} else {
			((Villager) repairman).setProfession(Profession.BLACKSMITH);
		}

		squidward.entity = repairman;

		if (!repairmen.contains(squidward)) repairmen.add(squidward);
		
		if (!chunks.contains(loc.getChunk())) chunks.add(loc.getChunk());
		loc.getWorld().loadChunk(loc.getChunk());

		squidward.overwriteAI();

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
        	
        	if (Double.parseDouble(config.getString("Version")) < version) {
        		updateConfig(config.getString("Version"));
        	} else if (Double.parseDouble(config.getString("Version")) == 0) {
        		log.info(prefix+"ERROR! ERROR! ERROR! ERROR! ERROR! ERROR! ERROR!");
        		log.info(prefix+"ERROR! YOUR CONFIG FILE IS CORRUPT!!! ERROR!");
        		log.info(prefix+"ERROR! ERROR! ERROR! ERROR! ERROR! ERROR! ERROR!");
        	}
        	
        	
        	Lang.checkLangVersion(lang);
			Lang.init();


	        Interactor.basicArmorPrice = config.getDouble("BasicArmorPrice");
	        Interactor.basicToolsPrice = config.getDouble("BasicToolPrice");
	        Interactor.enchantmentPrice = config.getDouble("BasicEnchantmentPrice");
	        Interactor.addEnchantmentPrice = config.getDouble("PriceToAddEnchantment");
	        Interactor.clearEnchantments = config.getBoolean("ClearOldEnchantments");
	        Interactor.maxEnchantBonus = config.getInt("EnchantmentBonus");

	        Interactor.enableEnchants = config.getBoolean("UseEnchantmentSystem");

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
	        		
	        		squidward.loc.getWorld().loadChunk(squidward.loc.getChunk());
	        		
	        		spawnRepairman(squidward);
	        	}
        	}
	        log.info(prefix+Lang.config_loaded);

        	if(debug) {
        		log.info(prefix+String.format(Lang.debug_loaded, repairmen.size()));
        	}
        } else {
        	config.set("Timeout", 5000);
        	config.set("Language", "en_US");
        	config.set("BasicArmorPrice", 10.0);
        	config.set("BasicToolPrice", 10.0);
        	config.set("BasicEnchantmentPrice", 30.0);
	        config.set("UseEnchantmentSystem", true);
        	config.set("PriceToAddEnchantment", 50.0);
        	config.set("ClearOldEnchantments", true);
        	config.set("EnchantmentBonus", 5);
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
		if (Double.parseDouble(oldVersion) < 0.05d) {
			// ADDED IN 0.05
			log.info(prefix+"UPDATING CONFIG "+config.getName()+" FROM VERSION OLDER THAN 0.5");

			config.set("PriceToAddEnchantment", 50.0);
	        config.set("ClearOldEnchantments", true);
	        config.set("EnchantmentBonus", 5);

	        config.set("Version", 0.05d);
	        try {
	            config.save(configFile);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
		}

		if (Double.parseDouble(oldVersion) == 0.05d) {
			log.info(prefix+"UPDATING CONFIG "+config.getName()+" FROM VERSION 0.5");
			// ADDED IN 0.051
			config.set("UseEnchantmentSystem", true);

			config.set("Version", 0.051d);
			try {
				config.save(configFile);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private class Starter implements Runnable {
		@Override
		public void run() {
			reloadPlugin();

			if (!setupEconomy()) log.info(prefix+Lang.economy_not_found);
			if (!setupPermissions()) log.info(prefix+Lang.permissions_not_found);
			
		}
		
	}

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
}

class Repairer {
	public Entity entity;
	public String name = "Repairman";
	public Location loc;
	public String type = "all";
	public double cost = 1;

	public void overwriteAI() {
		try {
			EntityVillager villager = ((CraftVillager)entity).getHandle();
			Field goalsField = EntityLiving.class.getDeclaredField("goalSelector");
			goalsField.setAccessible(true);
			PathfinderGoalSelector goals = (PathfinderGoalSelector) goalsField.get(villager);
			Field listField = PathfinderGoalSelector.class.getDeclaredField("a");
			listField.setAccessible(true);
			@SuppressWarnings("rawtypes")
			List list = (List)listField.get(goals);
			list.clear();
			goals.a(1, new PathfinderGoalLookAtPlayer(villager, EntityHuman.class, 12.0F, 1.0F));
			goals.a(2, new PathfinderGoalRandomLookaround(villager));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}