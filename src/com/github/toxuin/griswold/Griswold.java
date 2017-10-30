package com.github.toxuin.griswold;

import com.github.toxuin.griswold.Metrics.Graph;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftVillager;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class Griswold extends JavaPlugin implements Listener {
    public static File directory;
    public static boolean debug = false;
    public static int timeout = 5000;
    static Logger log;

    private static FileConfiguration config = null;
    private static File configFile = null;
    private Map<Repairer, Pair> npcChunks = new HashMap<>();
    Interactor interactor;

    public static Economy economy = null;

    public static double version;

    public static ApiVersion apiVersion;
    static String lang = "en_US";
    public boolean namesVisible = true;
    private boolean findDuplicates = false;
    private int duplicateFinderRadius = 5;

    public void onEnable() {
        log = this.getLogger();
        directory = this.getDataFolder();
        PluginDescriptionFile pdfFile = this.getDescription();
        version = Double.parseDouble(pdfFile.getVersion());
        apiVersion = new ApiVersion(this.getServer().getBukkitVersion(), Bukkit.getServer().getClass().getPackage().getName());

        if (!apiVersion.isValid()) {
            log.severe("UNKNOWN SERVER API VERSION: " + this.getServer().getBukkitVersion());
            log.severe("PLUGIN WORK WILL BE UNSTABLE");
            log.severe("PLEASE REPORT THIS TO THE DEVELOPERS AT http://dev.bukkit.org/bukkit-plugins/griswold/");
            log.severe("TELL HIM YOU SAW THIS:"
                    + " MAJOR: " + apiVersion.getMajor()
                    + " MINOR: " + apiVersion.getMinor()
                    + " RELEASE: " + apiVersion.getRelease()
                    + " BUILD: " + apiVersion.getBuild());
        }

        // CHECK IF USING THE WRONG PLUGIN VERSION
        if (ClassProxy.getClass("entity.CraftVillager") == null || ClassProxy.getClass("EnchantmentInstance") == null) {
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

        this.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
            reloadPlugin();
            if (!setupEconomy()) log.info(Lang.economy_not_found);
            if (Lang.chat_agreed.startsWith("ERROR:")) reloadPlugin();
        }, 20);

        interactor = new Interactor();

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

        log.info("Enabled! Version: " + version + " on api version " + apiVersion);
    }

    public void onDisable() {
        interactor = null;
        getCommand("blacksmith").setExecutor(null);
        despawnAll();
        log.info("Disabled.");
    }

    public void reloadPlugin() {
        despawnAll();
        readConfig();
    }

    public void createRepairman(String name, Location loc) {
        createRepairman(name, loc, "all", "1");
    }

    public void createRepairman(String name, Location loc, String type, String cost) {
        boolean found = false;
        Set<Repairer> npcs = npcChunks.keySet();
        for (Repairer rep : npcs) {
            if (rep.name.equalsIgnoreCase(name)) found = true;
        }
        if (found) {
            log.info(String.format(Lang.repairman_exists, name));
            return;
        }

        config.set("repairmen." + name + ".world", loc.getWorld().getName());
        config.set("repairmen." + name + ".X", loc.getX());
        config.set("repairmen." + name + ".Y", loc.getY());
        config.set("repairmen." + name + ".Z", loc.getZ());
        config.set("repairmen." + name + ".sound", "mob.villager.haggle");
        config.set("repairmen." + name + ".type", type);
        config.set("repairmen." + name + ".cost", Double.parseDouble(cost));

        try {
            config.save(configFile);
        } catch (Exception e) {
            log.info(Lang.error_config);
            e.printStackTrace();
        }

        Repairer meGusta = new Repairer();
        meGusta.name = name;
        meGusta.loc = loc;
        meGusta.type = type;
        meGusta.cost = Double.parseDouble(cost);
        spawnRepairman(meGusta);
    }

    public void removeRepairman(String name) {
        if (config.isConfigurationSection("repairmen." + name)) {
            config.set("repairmen." + name, null);
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

    public void listRepairmen(CommandSender sender) {
        String result = "";
        Set<Repairer> npcs = npcChunks.keySet();
        for (Repairer rep : npcs) {
            result = result + rep.name + ", ";
        }
        if (!result.equals("")) {
            sender.sendMessage(ChatColor.GREEN + Lang.repairman_list);
            sender.sendMessage(result);
        }
    }

    public void despawnAll() {
        Set<Repairer> npcs = npcChunks.keySet();
        for (Repairer rep : npcs) {
            rep.entity.remove();
        }
        npcChunks.clear();
    }

    public void despawn(final String squidward) {
        npcChunks.keySet().forEach((rep) -> {
            if (rep.name.equalsIgnoreCase(squidward)) rep.entity.remove();
        });
    }

    public void toggleNames() {
        namesVisible = !namesVisible;
        Set<Repairer> npcs = npcChunks.keySet();
        for (Repairer rep : npcs) {
            LivingEntity entity = (LivingEntity) rep.entity;
            entity.setCustomNameVisible(namesVisible);
        }

        config.set("ShowNames", namesVisible);
        try {
            config.save(configFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setSound(String name, String sound) {
        Set<Repairer> npcs = npcChunks.keySet();
        for (Repairer rep : npcs) {
            if (rep.name.equals(name)) {
                rep.sound = sound;
                return;
            }
        }
    }

    public void spawnRepairman(Repairer squidward) {
        Location loc = squidward.loc;
        if (loc == null) {
            log.info("ERROR: LOCATION IS NULL");
            return;
        }
        if (squidward.type.equals("enchant") && !Interactor.enableEnchants) {
            log.info(String.format(Lang.error_enchanter_not_spawned, loc.getX(), loc.getY(), loc.getZ()));
            return;
        }
        LivingEntity repairman = (LivingEntity) loc.getWorld().spawn(loc, EntityType.VILLAGER.getEntityClass());
        repairman.setCustomNameVisible(namesVisible);
        repairman.setCustomName(squidward.name);
        if (squidward.type.equals("enchant")) {
            ((Villager) repairman).setProfession(Profession.LIBRARIAN);
        } else {
            ((Villager) repairman).setProfession(Profession.BLACKSMITH);
        }

        squidward.entity = repairman;

        if (!npcChunks.containsKey(squidward))
            npcChunks.put(squidward, new Pair(loc.getChunk().getX(), loc.getChunk().getZ()));

        //loc.getWorld().loadChunk(loc.getChunk());

        squidward.overwriteAI();

        // FILTER DUPLICATES
        if (findDuplicates)
            Arrays.asList(squidward.loc.getChunk().getEntities()).forEach((doppelganger) -> {
                if (squidward.entityClass == null) return; // YOU'RE WEIRD
                if (!doppelganger.getClass().equals(CraftVillager.class)) return; // are you even villager?
                if (squidward.entity.equals(doppelganger)) return; // prevent suiciding
                if (!(doppelganger.getLocation().distance(squidward.loc) <= duplicateFinderRadius)) return;
                if (doppelganger.getName().equals(squidward.name)) doppelganger.remove(); // 100% DUPLICATE
            });

        if (debug) {
            log.info(String.format(Lang.repairman_spawn, squidward.entity.getEntityId(), loc.getX(), loc.getY(), loc.getZ()));
        }
    }

    /**
     * Spawn a repairman by name from config
     *
     * @param name MeGusta's name
     */
    public void spawnRepairman(final String name) {
        Set<String> repairmen = config.getConfigurationSection("repairmen").getKeys(false);
        if (!repairmen.contains(name)) throw new IllegalArgumentException("Repairman with name " + name + " not found");

        Repairer squidward = new Repairer();
        squidward.name = name;
        squidward.loc = new Location(this.getServer().getWorld(config.getString("repairmen." + name + ".world")),
                config.getDouble("repairmen." + name + ".X"),
                config.getDouble("repairmen." + name + ".Y"),
                config.getDouble("repairmen." + name + ".Z"));
        squidward.sound = config.getString("repairmen." + name + ".sound");
        squidward.type = config.getString("repairmen." + name + ".type");
        squidward.cost = config.getDouble("repairmen." + name + ".cost");

        squidward.loc.getWorld().loadChunk(squidward.loc.getChunk()); // TODO: do it one place, less code?

        spawnRepairman(squidward);


    }

    public Map<Repairer, Pair> getNpcChunks() {
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
            findDuplicates = config.getBoolean("DuplicateFinder");
            duplicateFinderRadius = config.getInt("DuplicateFinderRadius");

            if (Double.parseDouble(config.getString("Version")) < version) {
                updateConfig(config.getString("Version"));
            } else if (Double.parseDouble(config.getString("Version")) == 0) {
                log.info("ERROR! ERROR! ERROR! ERROR! ERROR! ERROR! ERROR!");
                log.info("ERROR! YOUR CONFIG FILE IS CORRUPT!!! ERROR!");
                log.info("ERROR! ERROR! ERROR! ERROR! ERROR! ERROR! ERROR!");
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
                    squidward.loc = new Location(this.getServer().getWorld(config.getString("repairmen." + repairman + ".world")),
                            config.getDouble("repairmen." + repairman + ".X"),
                            config.getDouble("repairmen." + repairman + ".Y"),
                            config.getDouble("repairmen." + repairman + ".Z"));
                    squidward.sound = config.getString("repairmen." + repairman + ".sound");
                    squidward.type = config.getString("repairmen." + repairman + ".type");
                    squidward.cost = config.getDouble("repairmen." + repairman + ".cost");

                    squidward.loc.getWorld().loadChunk(squidward.loc.getChunk());

                    spawnRepairman(squidward);
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
            config.set("DuplicateFinder", true);
            config.set("DuplicateFinderRadius", 5);
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
            log.info("UPDATING CONFIG " + config.getName() + " FROM VERSION OLDER THAN 0.5");

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
            log.info("UPDATING CONFIG " + config.getName() + " FROM VERSION 0.5");
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
            log.info("UPDATING CONFIG " + config.getName() + " FROM VERSION 0.51/0.6");
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
            log.info("UPDATING CONFIG " + config.getName() + " FROM VERSION 0.7*");
            if (config.isConfigurationSection("repairmen")) {
                Set<String> repairmen = config.getConfigurationSection("repairmen").getKeys(false);
                for (String repairman : repairmen) {
                    if (config.getString("repairmen." + repairman + ".sound").equals("mob.villager.haggle")) {
                        config.set("repairmen." + repairman + ".sound", "VILLAGER_HAGGLE");
                    }
                }
            }
            config.set("Version", 0.073d);

            try {
                config.save(configFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (Double.parseDouble(oldVersion) == 0.08d || Double.parseDouble(oldVersion) == 0.073d) { // ver 0.08 never existed! ^_^
            log.info("UPDATING CONFIG " + config.getName() + " FROM VERSION 0.73");
            config.set("DuplicateFinder", true);
            config.set("DuplicateFinderRadius", 5);

            config.set("Version", 0.075d);

            try {
                config.save(configFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
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
}

class Pair {
    public int x = 0;
    public int z = 0;

    public Pair(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public boolean equals(Pair pair) {
        return this.x == pair.x && this.z == pair.z;
    }

    public String toString() {
        return "Pair{x=" + this.x + "z=" + this.z + "}";
    }
}
