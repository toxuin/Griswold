package com.github.toxuin;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

public class Lang {
	public static String economy_not_found;
	public static String insufficient_params;
	public static String repairman_exists;
	public static String error_config;
	public static String error_remove;
	public static String repairman_list;
	public static String repairman_spawn;
	public static String debug_loaded;
	public static String default_config;
	public static String error_create_config;
	public static String lang_loaded;
	
	public static String chat_done;
	public static String chat_error;
	public static String chat_poor;
	public static String chat_norepair;
	public static String chat_cost;
	public static String chat_agreed;
	public static String chat_cannot;
	
	public static void init(String lang) {
		File langFile = new File(Griswold.directory,lang+".yml");
        YamlConfiguration language = YamlConfiguration.loadConfiguration(langFile);
        
        economy_not_found = language.getString("economy_not_found");
        insufficient_params = language.getString("insufficient_params");
        repairman_exists = language.getString("repairman_exists");
        error_config = language.getString("error_config");
        error_remove = language.getString("error_remove");
        repairman_list = language.getString("repairman_list");
        repairman_spawn = language.getString("repairman_spawn");
        debug_loaded = language.getString("debug_loaded");
        default_config = language.getString("default_config");
        error_create_config = language.getString("error_create_config");
        
        chat_done = language.getString("chat_done");
        chat_error = language.getString("chat_error");
        chat_poor = language.getString("chat_poor");
        chat_norepair = language.getString("chat_norepair");
        chat_cost = language.getString("chat_cost");
        chat_agreed = language.getString("chat_agreed");
        chat_cannot = language.getString("chat_cannot");
        
        Logger.getLogger("Minecraft").info(Griswold.prefix+lang_loaded);
	}
	
	public static void createLangFile() {
		File langFile = new File(Griswold.directory,"ru_RU.yml");
        YamlConfiguration language = YamlConfiguration.loadConfiguration(langFile);
        
        if (!langFile.exists()) {
        	language.set("economy_not_found", "Warning: economy system not found: all repairs are free!");
        	language.set("insufficient_params", "Please add more parameters! Usage: "+ChatColor.BLUE+"/repairman create "+ChatColor.GREEN+"name "+ChatColor.GRAY+"type cost!");
        	language.set("repairman_exists", "ERROR: repairman %s already exists!");
        	language.set("error_config", "ERROR when writing to config.yml");
        	language.set("error_remove", "Could not remove repairman: not found!");
        	language.set("repairman_list", "Here are all the repairmen:");
        	language.set("repairman_spawn", "SPAWNED REPAIRMAN ID:%s AT X:%s Y:%s Z:%s");
        	language.set("debug_loaded", "DEBUG: loaded total %s repairmen.");
        	language.set("default_config", "CREATED DEFAULT CONFIG");
        	language.set("error_create_config", "ERROR when creating config.yml");
        	language.set("lang_loaded", "Language file loaded!");
        	language.set("chat_done", "Отлично! Снова как новое!");
        	language.set("chat_error", "Кажется, что-то пошло не так!");
        	language.set("chat_poor", "Тебе нечем мне платить, дружище!");
        	language.set("chat_norepair", "Хм, похоже тут все в порядке - ремонт не нужен.");
        	language.set("chat_cost", "Я починю эту вещь для тебя за %s коинов.");
        	language.set("chat_agreed", "Если согласен - давай ее сюда.");
        	language.set("chat_cannot", "Я не умею чинить такие вещи.");
        	
        	try {
        		language.save(langFile); 
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
        }
	}
}
