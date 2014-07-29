package com.github.toxuin.griswold;

import com.github.toxuin.griswold.npcs.GriswoldNPC;
import com.github.toxuin.griswold.professions.Blacksmith;
import com.github.toxuin.griswold.professions.Profession;
import com.github.toxuin.griswold.professions.Shopkeeper;
import com.github.toxuin.griswold.util.ClassProxy;
import com.github.toxuin.griswold.util.Lang;
import com.github.toxuin.griswold.util.Metrics;
import com.github.toxuin.griswold.util.Pair;
import com.github.toxuin.griswold.util.ConfigManager;
import com.github.toxuin.griswold.util.Metrics.Graph;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.plugin.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class Griswold extends JavaPlugin implements Listener {
    private Map<GriswoldNPC, Pair<Integer, Integer>> npcChunks = new HashMap<GriswoldNPC, Pair<Integer, Integer>>();

    public static Griswold plugin;
    public static Logger log;
    public static Economy economy = null;
    
    public static double version;
    public static String apiVersion;

    public void onEnable() {
        plugin = this;
        log = this.getLogger();
		ConfigManager.directory = this.getDataFolder();
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
		    if (ConfigManager.debug) log.info("ERROR: failed to submit stats to MCStats");
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
        Lang.createLangFile();
		ConfigManager.readConfig();
	}
	
	public void createNpc(String name, Location loc, Profession type) {
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

    	GriswoldNPC npc = new GriswoldNPC(name, loc, Villager.class).setProfession(type);
        registerNpc(npc);
        ConfigManager.saveNpc(name, loc, type);
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
	
	public void registerNpc(GriswoldNPC npc) {
        if (!npcChunks.containsKey(npc)) npcChunks.put(npc, new Pair<Integer, Integer>(npc.loc.getChunk().getX(), npc.loc.getChunk().getZ()));
	}

    public Map<GriswoldNPC, Pair<Integer, Integer>> getNpcChunks() {
        return npcChunks;
    }

    public void clearNpcChunks() {
        npcChunks.clear();
    }

    public GriswoldNPC getNpcByName(String name) {
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
