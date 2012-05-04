package com.github.toxuin;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

public class Lang {
	public static String economy_not_found = "Warning: economy system not found: all repairs are free!";
	public static String insufficient_params = "Please add more parameters! Usage: "+ChatColor.BLUE+"/repairman create "+ChatColor.GREEN+"name "+ChatColor.GRAY+"type cost!";
	public static String repairman_exists = "ERROR: repairman %s already exists!";
	public static String error_config = "ERROR when writing to config.yml";
	public static String error_remove = "Could not remove repairman: not found!";
	public static String repairman_list = "Here are all the repairmen:";
	public static String repairman_spawn = "SPAWNED REPAIRMAN ID:%s AT X:%s Y:%s Z:%s";
	public static String debug_loaded = "DEBUG: loaded total %s repairmen.";
	public static String default_config = "CREATED DEFAULT CONFIG";
	public static String error_create_config = "ERROR when creating config.yml";
	public static String lang_loaded = "Language file loaded!";
	
	public static String chat_done = "Отлично! Снова как новое!";
	public static String chat_error = "Кажется, что-то пошло не так!";
	public static String chat_poor = "Тебе нечем мне платить, дружище!";
	public static String chat_norepair = "Хм, похоже тут все в порядке - ремонт не нужен.";
	public static String chat_cost = "Я починю эту вещь для тебя за %s коинов.";
	public static String chat_agreed = "Если согласен - давай ее сюда.";
	public static String chat_cannot = "Я не умею чинить такие вещи.";
	
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
}
