package com.github.toxuin.griswold;

import com.github.toxuin.griswold.npcs.GriswoldNPC;
import com.github.toxuin.griswold.professions.Blacksmith;
import com.github.toxuin.griswold.professions.Profession;
import com.github.toxuin.griswold.professions.Shopkeeper;
import com.github.toxuin.griswold.util.ClassProxy;
import com.github.toxuin.griswold.util.Lang;
import com.github.toxuin.griswold.util.Metrics;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.plugin.*;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.toxuin.griswold.util.Metrics.Graph;
import com.github.toxuin.griswold.util.Pair;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class Griswold extends JavaPlugin implements Listener {
	public static File directory;
	public static boolean debug = false;
	public static int timeout = 5000;
    public static Logger log;
	
	private static FileConfiguration config = null;
	private static File configFile = null;
    private Map<GriswoldNPC, Pair<Integer, Integer>> npcChunks = new HashMap<GriswoldNPC, Pair<Integer, Integer>>();

    public static Griswold plugin;

    public static Economy economy = null;
    
    public static double version;
    public static String apiVersion;
    public static String lang = "en_US";
    public static boolean namesVisible = true;

    public void onEnable() {
        plugin = this;
        log = this.getLogger();
		directory = this.getDataFolder();
		PluginDescriptionFile pdfFile = this.getDescription();
		version = Double.parseDouble(pdfFile.getVersion());
        apiVersion = this.getServer().getClass().getPackage().getName().substring(
                     this.getServer().getClass().getPackage().getName().lastIndexOf('.') + 1);

        // CHECK IF USING THE WRONG PLUGIN VERSION
        if (ClassProxy.getClass("entity.CraftVillager") == null) {
            log.severe("PLUGIN NOT LOADED!!!");
            log.severe("ERROR: YOU ARE USING THE WRONG VERSION OF THIS PLUGIN.");
            log.severe("GO TO http://dev.bukkit.org/bukkit-plugins/griswold/");
            log.severe("YOUR SERVER VERSION IS " + this.getServer().getBukkitVersion());
            log.severe("PLUGIN NOT LOADED!!!");
            this.getPluginLoader().disablePlugin(this);
            return;
        }

        this.getServer().getPluginManager().registerEvents(new EventListener(this), this);
        getCommand("blacksmith").setExecutor(new CommandListener(this));

		this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Starter(), 20);

        try {
		    Metrics metrics = new Metrics(this);
		    Graph graph = metrics.createGraph("Number of NPCs");
		    graph.addPlotter(new Metrics.Plotter("Total") {
		        @Override
		        public int getValue() {
		            return npcChunks.keySet().size();
		        }
		    });
		    metrics.start();
		} catch (IOException e) {
		    if (debug) log.info("ERROR: failed to submit stats to MCStats");
		}
		
		log.info("Enabled! Version: " + version);
	}

	public void onDisable() {
        plugin = null;
        despawnAll();
		log.info("Disabled.");
	}

	public void reloadPlugin() {
		despawnAll();
		readConfig();
	}
	
	public void createNpc(String name, Location loc) {
        createNpc(name, loc, "all", "1");
	}
	
	public void createNpc(String name, Location loc, String type, String cost) {
		boolean found = false;
        Set<GriswoldNPC> npcs = npcChunks.keySet();
		for (GriswoldNPC rep : npcs) {
			if (rep.name.equalsIgnoreCase(name)) found = true;
		}
		if (found) {
			log.info(String.format(Lang.repairman_exists, name));
			return;
		}
			
		config.set("repairmen."+name+".world", loc.getWorld().getName());
		config.set("repairmen."+name+".X", loc.getX());
		config.set("repairmen."+name+".Y", loc.getY());
		config.set("repairmen."+name+".Z", loc.getZ());
        config.set("repairmen."+name+".sound", "mob.villager.haggle");
		config.set("repairmen."+name+".type", type);
		config.set("repairmen."+name+".cost", Double.parseDouble(cost));
    	
    	try {
    		config.save(configFile);
    	} catch (Exception e) {
    		log.info(Lang.error_config);
    		e.printStackTrace();
    	}

        // SAFE TO USE SO FAR:
        //      IronGolem, Snowman, Villager, Whitch
        //      Ocelot, Zombie, Skeleton, Horse, Chicken
        //      Creeper, Cow, Pig, Wolf, Sheep, Squid
        // NOT WORKING:
        //      Spider(runs away)
        //      Blaze (Shoots, flies away)
        //      Wither (kills other npc, flies)
        //      Human (cannot spawn)
        //      Bat (flies away)
        //      Enderman (teleports, walks)
        //      Slime (jumps away)
        //      Herobrine (steals diamonds and kicks dogs)
        Profession profession = (Griswold.economy != null) ? new Shopkeeper() : new Blacksmith();
    	GriswoldNPC npc = new GriswoldNPC(name, loc, profession, Villager.class);
        registerNpc(npc);
	}
	
	public void removeNpc(String name) {
		if (config.isConfigurationSection("repairmen."+name)){
			config.set("repairmen."+name, null);
			try {
				config.save(configFile); 
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
		} else {
			log.info(Lang.error_remove);
			return;
		}
		reloadPlugin();
	}
	
	public void listNpc(CommandSender sender) {
		String result = "";
        Set<GriswoldNPC> npcs = npcChunks.keySet();
		for (GriswoldNPC rep : npcs) {
			result = result + rep.name + ", ";
		}
		if (!result.equals("")) {
			sender.sendMessage(ChatColor.GREEN+Lang.repairman_list);
			sender.sendMessage(result);
		}
	}
	
	public void despawnAll() {
        Set<GriswoldNPC> npcs = npcChunks.keySet();
		for (GriswoldNPC rep : npcs) {
			rep.remove();
		}
        npcChunks.clear();
	}

    public void toggleNames() {
        namesVisible = !namesVisible;
        Set<GriswoldNPC> npcs = npcChunks.keySet();
        for (GriswoldNPC rep : npcs) {
            rep.setNameVisible(namesVisible);
        }

        config.set("ShowNames", namesVisible);
        try {
            config.save(configFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	public void registerNpc(GriswoldNPC npc) {
        if (!npcChunks.containsKey(npc)) npcChunks.put(npc, new Pair<Integer, Integer>(npc.loc.getChunk().getX(), npc.loc.getChunk().getZ()));
	}

    public Map<GriswoldNPC, Pair<Integer, Integer>> getNpcChunks() {
        return this.npcChunks;
    }
	
	private void readConfig() {

    	Lang.createLangFile();
		
		configFile = new File(directory, "config.yml");
        config = YamlConfiguration.loadConfiguration(configFile);
        
        npcChunks.clear();
        
        if (configFile.exists()) {
        	debug = config.getBoolean("Debug");
        	timeout = config.getInt("Timeout");
        	lang = config.getString("Language");
            namesVisible = config.getBoolean("ShowNames");
        	
        	if (Double.parseDouble(config.getString("Version")) < version) {
        		updateConfig(config.getString("Version"));
        	} else if (Double.parseDouble(config.getString("Version")) == 0) {
        		log.info("ERROR! ERROR! ERROR! ERROR! ERROR! ERROR! ERROR!");
        		log.info("ERROR! YOUR CONFIG FILE IS CORRUPT!!! ERROR!");
        		log.info("ERROR! ERROR! ERROR! ERROR! ERROR! ERROR! ERROR!");
        	}

        	Lang.checkLangVersion(lang);
			Lang.init();

	        Blacksmith.basicArmorPrice = config.getDouble("BasicArmorPrice");
	        Blacksmith.basicToolsPrice = config.getDouble("BasicToolPrice");
	        Blacksmith.enchantmentPrice = config.getDouble("BasicEnchantmentPrice");
	        Blacksmith.addEnchantmentPrice = config.getDouble("PriceToAddEnchantment");
	        Blacksmith.clearEnchantments = config.getBoolean("ClearOldEnchantments");
	        Blacksmith.maxEnchantBonus = config.getInt("EnchantmentBonus");

	        if (config.isConfigurationSection("repairmen")) {
        		Set<String> repairmen = config.getConfigurationSection("repairmen").getKeys(false);
	        	for (String repairman : repairmen) {

	        		Location loc = new Location(this.getServer().getWorld(config.getString("repairmen."+repairman+".world")),
	        									config.getDouble("repairmen."+repairman+".X"),
	        									config.getDouble("repairmen."+repairman+".Y"),
	        									config.getDouble("repairmen."+repairman+".Z"));
                    String sound = config.getString("repairmen." + repairman + ".sound");

                    Profession profession = new Blacksmith();

                    GriswoldNPC squidward = new GriswoldNPC(repairman, loc, profession, Villager.class);
                    squidward.setSound(sound);
                    if (squidward.profession instanceof Blacksmith) {
                        String type = config.getString("repairmen."+repairman+".type");
                        Blacksmith blacksmithy = (Blacksmith) squidward.profession;
                        blacksmithy.priceMultiplier = config.getDouble("repairmen."+repairman+".cost") ;
                        blacksmithy.canEnchant = (config.getBoolean("UseEnchantmentSystem") && (type.equals("all") || type.equals("enchant")));
                        // TODO: WILL BE REMOVED
                        if ((type.equals("all") || type.equals("both"))) {
                            blacksmithy.canRepairTools = true;
                            blacksmithy.canRepairArmor = true;
                        } else if (type.equals("tools")) {
                            blacksmithy.canRepairTools = true;
                            blacksmithy.canRepairArmor = false;
                        } else if (type.equals("armor")) {
                            blacksmithy.canRepairTools = false;
                            blacksmithy.canRepairArmor = true;
                        }
                    }

	        		registerNpc(squidward);
	        	}
        	}
	        log.info(Lang.config_loaded);

        	if (debug) {
        		log.info(String.format(Lang.debug_loaded, npcChunks.keySet().size()));
        	}
        } else {
        	config.set("Timeout", 5000);
        	config.set("Language", "en_US");
            config.set("ShowNames", true);
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
        		log.info(Lang.default_config);
        	} catch (Exception e) {
        		log.info(Lang.error_create_config);
        		e.printStackTrace();
        	}
        }
	}
	
	private void updateConfig(String oldVersion) {
		if (Double.parseDouble(oldVersion) < 0.05d) {
			// ADDED IN 0.05
			log.info("UPDATING CONFIG "+config.getName()+" FROM VERSION OLDER THAN 0.5");

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
			log.info("UPDATING CONFIG "+config.getName()+" FROM VERSION 0.5");
			// ADDED IN 0.051
			config.set("UseEnchantmentSystem", true);

			config.set("Version", 0.051d);
			try {
				config.save(configFile);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

        if (Double.parseDouble(oldVersion) == 0.06d || Double.parseDouble(oldVersion) == 0.051d) {
            log.info("UPDATING CONFIG "+config.getName()+" FROM VERSION 0.51/0.6");
            // ADDED IN 0.07
            config.set("ShowNames", true);

            config.set("Version", 0.07d);
            try {
                config.save(configFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (Double.parseDouble(oldVersion) == 0.07d) {
            log.info("UPDATING CONFIG "+config.getName()+" FROM VERSION 0.7*");
            if (config.isConfigurationSection("repairmen")) {
                Set<String> repairmen = config.getConfigurationSection("repairmen").getKeys(false);
                for (String repairman : repairmen) {
                    if (config.getString("repairmen." + repairman + ".sound").equals("mob.villager.haggle")) {
                        config.set("repairmen." + repairman + ".sound", "VILLAGER_HAGGLE");
                    }
                }
            }
            config.set("Version", 0.08d);

            try {
                config.save(configFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
	}

    public GriswoldNPC getNPCByName(String name) {
        for (GriswoldNPC npc : npcChunks.keySet()) {
            if (npc.name.equals(name)) return npc;
        }
        return null;
    }

    private class Starter implements Runnable {
		@Override
		public void run() {
			reloadPlugin();
			if (!setupEconomy()) log.info(Lang.economy_not_found);
		}
		
	}

    private boolean setupEconomy() {
    	if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
        return (economy != null);
    }

    public static boolean isNpc(Entity entity) {
        if (Griswold.plugin.getNpcChunks().isEmpty()) return false;
        Set<GriswoldNPC> npcs = Griswold.plugin.getNpcChunks().keySet();
        for (GriswoldNPC npc : npcs) {
            if (npc.entity.equals(entity)) {
                return true;
            }
        }
        return false;
    }
}
