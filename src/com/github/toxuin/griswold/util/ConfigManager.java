package com.github.toxuin.griswold.util;

import com.github.toxuin.griswold.Griswold;
import com.github.toxuin.griswold.npcs.GriswoldNPC;
import com.github.toxuin.griswold.professions.Blacksmith;
import com.github.toxuin.griswold.professions.Profession;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Villager;

import javax.annotation.CheckForNull;
import java.io.File;
import java.util.Set;

public class ConfigManager {
    public static File directory;
    public static FileConfiguration config = null;
    private static File configFile = null;

    public static int timeout = 5000;
    public static boolean debug = false;
    public static String lang = "en_US";

    public static void saveNpc(String name, Location loc, Profession profession) {
        //TODO: MAKE NPCS SAVE THEMSELVES

        config.set("NPC."+name+".world", loc.getWorld().getName());
        config.set("NPC."+name+".X", loc.getX());
        config.set("NPC."+name+".Y", loc.getY());
        config.set("NPC."+name+".Z", loc.getZ());
        config.set("NPC."+name+".sound", "mob.villager.haggle");
        config.set("NPC."+name+".profession", profession.getName());
        profession.saveConfig();
        saveConfig();
    }

    public static void removeNpc(String name) {
        if (!config.isConfigurationSection("NPC." + name)) Griswold.log.info(Lang.error_remove);
        config.set("NPC." + name, null);
        saveConfig();
        Griswold.plugin.reloadPlugin();

    }

    public static void setNameVisible(String npc, boolean isNameVisible) {
        config.set("NPC." + npc + ".showName", isNameVisible);
        saveConfig();
    }

    public static Location getNpcLocation(String name) {
        double x = config.getDouble("NPC."+name+".X");
        double y = config.getDouble("NPC."+name+".Y");
        double z = config.getDouble("NPC."+name+".Z");
        String world = config.getString("NPC."+name+".world");
        return new Location(Bukkit.getWorld(world), x, y, z);
    }

    public static void readConfig() {

        configFile = new File(directory, "config.yml");
        config = YamlConfiguration.loadConfiguration(configFile);

        Griswold.plugin.clearNpcChunks();

        // TODO: DEFAULTS
        Configuration defaults = config.getDefaults();

        defaults.set("interactionTimeout", 5000);
        defaults.set("language", "en_US");
        defaults.set("debug", false);
        defaults.set("version", Griswold.version);

        if (configFile.exists()) {
            debug = config.getBoolean("debug");
            timeout = config.getInt("interactionTimeout");
            lang = config.getString("language");

            if (Double.parseDouble(config.getString("Version")) < Griswold.version) {
                updateConfig(config.getString("Version"));
            } else if (Double.parseDouble(config.getString("Version")) == 0) {
                Griswold.log.info("ERROR! ERROR! ERROR! ERROR! ERROR! ERROR! ERROR!");
                Griswold.log.info("ERROR! YOUR CONFIG FILE IS CORRUPT!!! ERROR!");
                Griswold.log.info("ERROR! ERROR! ERROR! ERROR! ERROR! ERROR! ERROR!");
            }

            Lang.checkLangVersion(lang);
            Lang.init();

            Blacksmith.basicArmorPrice = config.getDouble("BasicArmorPrice");
            Blacksmith.basicToolPrice = config.getDouble("BasicToolPrice");
            Blacksmith.enchantmentPrice = config.getDouble("BasicEnchantmentPrice");
            Blacksmith.addEnchantmentPrice = config.getDouble("PriceToAddEnchantment");
            Blacksmith.clearEnchantments = config.getBoolean("ClearOldEnchantments");
            Blacksmith.maxEnchantBonus = config.getInt("EnchantmentBonus");

            if (config.isConfigurationSection("NPC")) {
                Set<String> npcs = config.getConfigurationSection("NPC").getKeys(false);
                for (String npc : npcs) {
                    Profession profession = Profession.getByName(config.getString("NPC." + npc + ".profession"));
                    GriswoldNPC squidward = new GriswoldNPC(npc, getNpcLocation(npc), Villager.class).setProfession(profession);
                    squidward.setSound(config.getString("NPC." + npc + ".sound"));
                    Griswold.plugin.registerNpc(squidward);
                }
            }

            Griswold.log.info(Lang.config_loaded);
            if (debug) Griswold.log.info(String.format(Lang.debug_loaded, Griswold.plugin.getNpcChunks().keySet().size()));
        } else {
            if (!saveConfig()) {
                Griswold.log.info(Lang.error_create_config);
            } else Griswold.log.info(Lang.default_config);
        }
    }

    private static void updateConfig(String oldVer) {
        double oldVersion = Double.parseDouble(oldVer);

        if (oldVersion < 0.05d) {
            Griswold.log.info("UPDATING CONFIG " + config.getName() + " FROM VERSION OLDER THAN 0.05");

            config.set("PriceToAddEnchantment", 50.0);
            config.set("ClearOldEnchantments", true);
            config.set("EnchantmentBonus", 5);

            config.set("Version", 0.05d);
            saveConfig();
            oldVersion = 0.05d;
        }

        if (oldVersion == 0.05d) {
            Griswold.log.info("UPDATING CONFIG " + config.getName() + " FROM VERSION 0.05");
            config.set("UseEnchantmentSystem", true);

            config.set("Version", 0.051d);
            saveConfig();
            oldVersion = 0.051d;
        }

        if (oldVersion == 0.06d || oldVersion == 0.051d) {
            Griswold.log.info("UPDATING CONFIG " + config.getName() + " FROM VERSION 0.051/0.06");
            config.set("ShowNames", true);

            config.set("Version", 0.07d);
            saveConfig();
            oldVersion = 0.07d;
        }

        if (oldVersion == 0.07d) {
            Griswold.log.info("UPDATING CONFIG " + config.getName() + " FROM VERSION 0.07*");
            if (config.isConfigurationSection("repairmen")) {
                Set<String> repairmen = config.getConfigurationSection("repairmen").getKeys(false);
                for (String repairman : repairmen) {
                    if (config.getString("repairmen." + repairman + ".sound").equals("mob.villager.haggle")) {
                        config.set("repairmen." + repairman + ".sound", "VILLAGER_HAGGLE");
                    }
                }
            }
            config.set("Version", 0.08d);
            saveConfig();
            oldVersion = 0.08d;
        }

        if (oldVersion == 0.08d) {
            Griswold.log.info("UPDATING CONFIG " + config.getName() + " FROM VERSION 0.08");
            if (config.isInt("Timeout")) {
                config.set("interactionTimeout", config.getInt("Timeout"));
                config.set("Timeout", null);
            }

            if (config.isString("Language")) {
                config.set("language", config.getString("Language"));
                config.set("Language", null);
            }

            if (config.isBoolean("Debug")) {
                config.set("debug", config.getBoolean("Debug"));
                config.set("Debug", null);
            }

            if (config.isDouble("Version")) {
                config.set("Version", null);

            }

            // TRANSFORMATION OF ALL NPC
            if (config.isConfigurationSection("repairmen")) {
                Set<String> npcs = config.getConfigurationSection("repairmen").getKeys(false);
                for (String npc : npcs) {
                    config.set("NPC." + npc + ".world", config.getString("repairmen." + npc + ".world"));
                    config.set("NPC." + npc + ".X", config.getDouble("repairmen." + npc + ".X"));
                    config.set("NPC." + npc + ".Y", config.getDouble("repairmen." + npc + ".Y"));
                    config.set("NPC." + npc + ".Z", config.getDouble("repairmen." + npc + ".Z"));
                    config.set("NPC." + npc + ".sound", config.getString("repairmen." + npc + ".sound"));
                    config.set("NPC." + npc + ".showName", config.getBoolean("ShowNames"));
                    config.set("NPC." + npc + ".profession", "Blacksmith");   // THERE WERE NO OTHER PROFESSIONS BEFORE 0.08
                    config.set("NPC." + npc + ".Blacksmith.costMultiplier", config.getDouble("repairmen." + npc + ".cost"));
                    config.set("NPC." + npc + ".Blacksmith.basicArmorPrice", config.getDouble("BasicArmorPrice"));
                    config.set("NPC." + npc + ".Blacksmith.basicToolPrice", config.getDouble("BasicToolPrice"));
                    config.set("NPC." + npc + ".Blacksmith.basicEnchantmentPrice", config.getDouble("BasicEnchantmentPrice"));
                    config.set("NPC." + npc + ".Blacksmith.pricePerEnchantment", config.getDouble("PriceToAddEnchantment"));
                    config.set("NPC." + npc + ".Blacksmith.clearOldEnchantments", config.getBoolean("ClearOldEnchantments"));
                    config.set("NPC." + npc + ".Blacksmith.enchantmentBonus", config.getInt("EnchantmentBonus"));

                    String type = config.getString("repairmen." + npc + ".type");
                    boolean canEnchant = (type.equalsIgnoreCase("all") || type.equalsIgnoreCase("enchant"));
                    boolean canRepairArmor = (type.equalsIgnoreCase("all") || type.equalsIgnoreCase("both") || type.equalsIgnoreCase("armor"));
                    boolean canRepairTools = (type.equalsIgnoreCase("all") || type.equalsIgnoreCase("both") || type.equalsIgnoreCase("tools"));
                    config.set("NPC." + npc + ".Blacksmith.canEnchant", canEnchant);
                    config.set("NPC." + npc + ".Blacksmith.canRepairTools", canRepairTools);
                    config.set("NPC." + npc + ".Blacksmith.canRepairArmor", canRepairArmor);
                }
                config.set("repairmen", null);
            }

            config.set("ShowNames", null);
            config.set("BasicArmorPrice", null);
            config.set("BasicToolPrice", null);
            config.set("BasicEnchantmentPrice", null);
            config.set("UseEnchantmentSystem", null);
            config.set("PriceToAddEnchantment", null);
            config.set("ClearOldEnchantments", null);
            config.set("EnchantmentBonus", null);

            config.set("version", 0.081d);
            saveConfig();
            oldVersion = 0.081d;
        }
    }

    public static boolean saveConfig() {
        try {
            config.save(configFile);
            return true;
        } catch (Exception e) {
            Griswold.log.info(Lang.error_config);
            if (debug) e.printStackTrace();
            return false;
        }
    }

    public static boolean isNameVisible(String npcName) {
        return config.getBoolean("NPC." + npcName + ".showName");
    }

    @CheckForNull
    public static Object getProfessionObject(String npc, String paramName) {
        String profession = config.getString("NPC." + npc + ".profession");
        if (profession == null) return null;
        return config.getDouble("NPC." + npc + "." + profession + "." + paramName);
    }

    @CheckForNull
    public static Double getProfessionDouble(String npc, String paramName) {
        try {
            return (Double) getProfessionObject(npc, paramName);
        } catch (Exception e) {
            return null;
        }
    }

    @CheckForNull
    public static String getProfessionString(String npc, String paramName) {
        try {
            return (String) getProfessionObject(npc, paramName);
        } catch (Exception e) {
            return null;
        }
    }

    @CheckForNull
    public static Boolean getProfessionBoolean(String npc, String paramName) {
        try {
            return (Boolean) getProfessionObject(npc, paramName);
        } catch (Exception e) {
            return null;
        }
    }

    @CheckForNull
    public static Integer getProfessionInteger(String npc, String paramName) {
        try {
            return (Integer) getProfessionObject(npc, paramName);
        } catch (Exception e) {
            return null;
        }
    }

    public static void setProfessionParam(String npcName, String param, Object thingy) {
        String profession = config.getString("NPC." + npcName + ".profession");
        config.set("NPC." + npcName + "." + profession + "." + param, thingy);
    }
}
