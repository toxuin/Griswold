package com.github.toxuin;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

public class Lang {
	public static String economy_not_found = "ERROR: LANG NOT FOUND: economy_not_found";
	public static String permissions_not_found = "ERROR: LANG NOT FOUND: permissions_not_found";
	public static String insufficient_params = "ERROR: LANG NOT FOUND: insufficient_params";
	public static String repairman_exists = "ERROR: LANG NOT FOUND: repairman_exists";
	public static String config_loaded = "ERROR: LANG NOT FOUND: config_loaded";
	public static String error_config = "ERROR: LANG NOT FOUND: error_config";
	public static String error_remove = "ERROR: LANG NOT FOUND: error_remove";
	public static String repairman_list = "ERROR: LANG NOT FOUND: repairman_list";
	public static String repairman_spawn = "ERROR: LANG NOT FOUND: repairman_spawn";
	public static String debug_loaded = "ERROR: LANG NOT FOUND: debug_loaded";
	public static String default_config = "ERROR: LANG NOT FOUND: default_config";
	public static String error_create_config = "ERROR: LANG NOT FOUND: error_create_config";
	public static String lang_loaded = "ERROR: LANG NOT FOUND: lang_loaded";
	public static String error_accesslevel = "ERROR: LANG NOT FOUND: error_accesslevel";
	public static String new_created = "ERROR: LANG NOT FOUND: new_created";
	public static String deleted = "ERROR: LANG NOT FOUND: deleted";
	public static String despawned = "ERROR: LANG NOT FOUND: despawned";
	public static String respawned = "ERROR: LANG NOT FOUND: respawned";
	
	public static String chat_done = "ERROR: LANG NOT FOUND: chat_done";
	public static String chat_error = "ERROR: LANG NOT FOUND: chat_error";
	public static String chat_poor = "ERROR: LANG NOT FOUND: chat_poor";
	public static String chat_norepair = "ERROR: LANG NOT FOUND: chat_norepair";
	public static String chat_free = "ERROR: LANG NOT FOUND: chat_free";
	public static String chat_cost = "ERROR: LANG NOT FOUND: chat_cost";
	public static String chat_agreed = "ERROR: LANG NOT FOUND: chat_agreed";
	public static String chat_cannot = "ERROR: LANG NOT FOUND: chat_cannot";
	
	public static void init() {
		File langFile = new File(Griswold.directory,Griswold.lang+".yml");
        YamlConfiguration language = YamlConfiguration.loadConfiguration(langFile);
        
        economy_not_found = language.getString("economy_not_found");
        permissions_not_found = language.getString("permissions_not_found");
        insufficient_params = language.getString("insufficient_params");
        repairman_exists = language.getString("repairman_exists");
        config_loaded = language.getString("config_loaded");
        error_config = language.getString("error_config");
        error_remove = language.getString("error_remove");
        repairman_list = language.getString("repairman_list");
        repairman_spawn = language.getString("repairman_spawn");
        debug_loaded = language.getString("debug_loaded");
        default_config = language.getString("default_config");
        error_create_config = language.getString("error_create_config");
        lang_loaded = language.getString("lang_loaded");
        error_accesslevel = language.getString("error_accesslevel");
        new_created = language.getString("new_created");
        deleted = language.getString("deleted");
        despawned = language.getString("despawned");
        respawned = language.getString("respawned");
        
        chat_done = language.getString("chat_done");
        chat_error = language.getString("chat_error");
        chat_poor = language.getString("chat_poor");
        chat_free = language.getString("chat_free");
        chat_norepair = language.getString("chat_norepair");
        chat_cost = language.getString("chat_cost");
        chat_agreed = language.getString("chat_agreed");
        chat_cannot = language.getString("chat_cannot");
        
        Logger.getLogger("Minecraft").info(String.format(Griswold.prefix+lang_loaded, Griswold.lang+".yml"));
	}
	
	public static void createLangFile() {
		File langFile = new File(Griswold.directory, "en_US.yml");
        YamlConfiguration language = YamlConfiguration.loadConfiguration(langFile);
        
        if (!langFile.exists()) {
        	language.set("economy_not_found", "Warning: economy system not found: all repairs are free!");
        	language.set("permissions_not_found", "Warning: permission system not found: access is open to everyone!");
        	language.set("insufficient_params", "Please add more parameters! Usage: "+ChatColor.BLUE+"/repairman create "+ChatColor.GREEN+"name "+ChatColor.GRAY+"type cost!");
        	language.set("repairman_exists", "ERROR: repairman %s already exists!");
        	language.set("config_loaded", "Config loaded!");
        	language.set("error_config", "ERROR when writing to config.yml");
        	language.set("error_remove", "Could not remove repairman: not found!");
        	language.set("repairman_list", "Here are all the repairmen:");
        	language.set("repairman_spawn", "SPAWNED REPAIRMAN ID:%s AT X:%s Y:%s Z:%s");
        	language.set("debug_loaded", "DEBUG: loaded total %s repairmen.");
        	language.set("default_config", "CREATED DEFAULT CONFIG");
        	language.set("error_create_config", "ERROR when creating config.yml");
        	language.set("lang_loaded", "Language file %s loaded!");
        	language.set("error_accesslevel", "You do not have enough permissions to do that.");
        	language.set("new_created", "New blacksmith created!");
        	language.set("deleted", "Blacksmith %s deleted.");
        	language.set("despawned", "All blacksmiths despawned");
        	language.set("respawned", "All blacksmiths respawned");
        	
        	language.set("chat_done", "Looks great! Good as new!");
        	language.set("chat_error", "Whoops, something's gone wrong!");
        	language.set("chat_poor", "You have no money, that's sad!");
        	language.set("chat_free", "I will repair your item for free today!");
        	language.set("chat_norepair", "Hmm, looks like it does not need any repair.");
        	language.set("chat_cost", "Hmm! I will repair this for %s %s");
        	language.set("chat_agreed", "Agreed? Yes? Pass it to me if agreed.");
        	language.set("chat_cannot", "I can not repair this kind of things.");
        	
        	language.set("version", Griswold.version);
        	
        	try {
        		language.save(langFile); 
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
        }
	}
	
	public static void checkLangVersion(String locale) {
		File langFile = new File(Griswold.directory, locale+".yml");
        YamlConfiguration language = YamlConfiguration.loadConfiguration(langFile);
        
		if (language.getDouble("version") != 0.04) {
			language.set("lang_loaded", "Language file %s loaded!");
			
			language.set("error_accesslevel", "You do not have enough permissions to do that.");
        	language.set("new_created", "New blacksmith created!");
        	language.set("deleted", "Blacksmith %s deleted.");
        	language.set("despawned", "All blacksmiths despawned");
        	language.set("respawned", "All blacksmiths respawned");
        	
        	language.set("version", Griswold.version);
        	
    		try {
        		language.save(langFile); 
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
    	}
	}
}
