package com.github.toxuin.griswold;

import com.github.toxuin.griswold.Metrics.Graph;
import com.github.toxuin.griswold.adapters.citizens.CitizensAdapter;
import com.github.toxuin.griswold.util.Pair;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class Griswold extends JavaPlugin implements Listener {

    public static final String PLUGIN_NAME = "Griswold";
    static File directory;
    static boolean debug = false;
    public static int timeout = 5000;
    public static Logger log;

    static FileConfiguration config = null;
    private static File configFile = null;
    static Map<GriswoldNPC, Pair> npcChunks = new HashMap<>();
    Interactor interactor;

    static Economy economy = null;

    static double version;

    static ApiVersion apiVersion;
    static String lang = "en_US";
    static boolean namesVisible = true;
    static boolean findDuplicates = false;
    static int duplicateFinderRadius = 5;

    CitizensAdapter citizensAdapter;

    public void onEnable() {
        log = this.getLogger();
        directory = this.getDataFolder();
        PluginDescriptionFile pdfFile = this.getDescription();
        version = Double.parseDouble(pdfFile.getVersion());
        apiVersion = new ApiVersion(this.getServer().getBukkitVersion(),
                Bukkit.getServer().getClass().getPackage().getName());

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
        if (ClassProxy.getClass("entity.CraftVillager") == null
            || ClassProxy.getClass("EnchantmentInstance") == null) {
            log.severe("PLUGIN NOT LOADED!!!");
            log.severe("ERROR: YOU ARE USING THE WRONG VERSION OF THIS PLUGIN.");
            log.severe("GO TO http://dev.bukkit.org/bukkit-plugins/griswold/");
            log.severe("YOUR SERVER VERSION IS " + this.getServer().getBukkitVersion());
            log.severe("PLUGIN NOT LOADED!!!");
            this.getPluginLoader().disablePlugin(this);
            return;
        }

        this.getServer().getPluginManager().registerEvents(new EventListener(this), this);
        final CommandListener cmdListener = new CommandListener(this);
        getCommand("blacksmith").setExecutor(cmdListener);
        getCommand("blacksmith").setTabCompleter(cmdListener);

        interactor = new Interactor();

        this.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
            reloadPlugin();
            if (!setupEconomy()) log.info(Lang.economy_not_found);
            if (Lang.chat_agreed.startsWith("ERROR:")) reloadPlugin(); // this is fucking gold
        }, 20);

        Plugin citizens = getServer().getPluginManager().getPlugin("Citizens");
        if (citizens != null && citizens.isEnabled()) {
            citizensAdapter = new CitizensAdapter();
            log.info("Registered Griswold traits with Citizens");
        }

        try {
            Metrics metrics = new Metrics(this);
            Graph citizensGraph = metrics.createGraph("Using Citizens2");
            citizensGraph.addPlotter(new Metrics.Plotter() {
                @Override
                public int getValue() {
                    return citizensAdapter == null ? 0 : 1;
                }
            });

            Graph numberOfNpcGraph = metrics.createGraph("Number of NPCs");
            numberOfNpcGraph.addPlotter(new Metrics.Plotter("Total") {
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
        if (citizensAdapter != null) citizensAdapter.deregisterTraits();
        log.info("Disabled.");
    }

    void reloadPlugin() {
        despawnAll();
        readConfig();
    }

    void createRepairman(String name, Location loc) {
        createRepairman(name, loc, "all", "1");
    }

    void createRepairman(String name, Location loc, String type) {
        createRepairman(name, loc, type, "1");
    }

    void createRepairman(String name, Location loc, String type, String cost) {
        boolean found = false;
        Set<GriswoldNPC> npcs = npcChunks.keySet();
        for (Repairer rep : npcs) {
            if (rep.getName().equalsIgnoreCase(name)) found = true;
        }
        if (found) {
            log.info(String.format(Lang.repairman_exists, name)); // TODO: report to user???
            return;
        }

        config.set("repairmen." + name + ".world", loc.getWorld().getName());
        config.set("repairmen." + name + ".X", loc.getX());
        config.set("repairmen." + name + ".Y", loc.getY());
        config.set("repairmen." + name + ".Z", loc.getZ());
        config.set("repairmen." + name + ".sound", Repairer.getDefaultSound());
        config.set("repairmen." + name + ".type", type);
        config.set("repairmen." + name + ".cost", Double.parseDouble(cost));

        try {
            config.save(configFile);
        } catch (Exception e) {
            log.info(Lang.error_config);
            e.printStackTrace();
        }

        GriswoldNPC meGusta = new GriswoldNPC(name, loc, Repairer.getDefaultSound(), type, Double.parseDouble(cost));
        meGusta.spawn();
    }

    void removeRepairman(String name) {
        if (!config.isConfigurationSection("repairmen." + name)) {
            log.info(Lang.error_remove);
            return;
        }
        config.set("repairmen." + name, null);
        try {
            config.save(configFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        reloadPlugin();
    }

    void listRepairmen(CommandSender sender) {
        StringBuilder result = new StringBuilder();
        npcChunks.keySet().forEach(rep -> result.append(rep.getName()).append(", "));
        if (!result.toString().equals("")) {
            sender.sendMessage(ChatColor.GREEN + Lang.repairman_list);
            sender.sendMessage(result.toString());
        }
    }

    void despawnAll() {
        Griswold.npcChunks.keySet().forEach(GriswoldNPC::despawn);
        npcChunks.clear();
    }

    void toggleNames() {
        namesVisible = !namesVisible;

        npcChunks.keySet().forEach((rep) -> rep.toggleName(namesVisible));

        config.set("ShowNames", namesVisible);
        try {
            config.save(configFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void setSound(String name, String sound) {
        npcChunks.keySet().stream()
                .filter(rep -> rep.getName().equals(name))
                .forEach(rep -> rep.setSound(sound));
    }

    Map<GriswoldNPC, Pair> getNpcChunks() {
        return npcChunks;
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

            if (interactor != null) interactor.loadConfigItems();

            if (config.isConfigurationSection("repairmen")) {
                Set<String> repairmen = config.getConfigurationSection("repairmen").getKeys(false);
                for (String repairman : repairmen) {
                    Location loc = new Location(
                            this.getServer().getWorld(config.getString("repairmen." + repairman + ".world")),
                            config.getDouble("repairmen." + repairman + ".X"),
                            config.getDouble("repairmen." + repairman + ".Y"),
                            config.getDouble("repairmen." + repairman + ".Z"));
                    String sound = config.getString("repairmen." + repairman + ".sound");
                    String type = config.getString("repairmen." + repairman + ".type");
                    double cost = config.getDouble("repairmen." + repairman + ".cost");

                    GriswoldNPC squidward = new GriswoldNPC(repairman, loc, sound, type, cost);

                    squidward.loadChunk();
                    squidward.spawn();
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
            log.info("UPDATING CONFIG " + config.getName() + " FROM VERSION 0.7");
            if (config.isConfigurationSection("repairmen")) {
                Set<String> repairmen = config.getConfigurationSection("repairmen").getKeys(false);
                for (String repairman : repairmen) {
                    if (config.getString("repairmen." + repairman + ".sound").equals("mob.villager.haggle")) {
                        config.set("repairmen." + repairman + ".sound", "ENTITY_VILLAGER_TRADING");
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

        if (Double.parseDouble(oldVersion) == 0.073d) {
            log.info("UPDATING CONFIG " + config.getName() + " FROM VERSION 0.73");
            config.set("DuplicateFinder", true);
            config.set("DuplicateFinderRadius", 5);

            config.set("Version", 0.076d);

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
        RegisteredServiceProvider<Economy> economyProvider =
                getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
        return (economy != null);
    }

    public Interactor getInteractor() {
        return interactor;
    }
}
