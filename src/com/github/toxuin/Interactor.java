package com.github.toxuin;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Interactor {
	
	public static double basicToolsPrice = 10.0;
	public static double basicArmorPrice = 10.0;
	public static double enchantmentPrice = 30.0;
	
	private final static List<Integer> repairableTools;
	private final static List<Integer> repairableArmor;
	static {
		repairableTools = new LinkedList<Integer>();
		repairableArmor = new LinkedList<Integer>();
		
		for (int i = 256; i <= 259; i++) repairableTools.add(i); // IRON TOOLS AND ZIPPO
		for (int i = 267; i <= 279; i++) repairableTools.add(i); // WOODEN, STONE, DIAMOND TOOLS
		for (int i = 283; i <= 286; i++) repairableTools.add(i); // GOLDEN TOOLS
		for (int i = 290; i <= 294; i++) repairableTools.add(i); // HOES
		repairableTools.add(359); // SCISSORS
		repairableTools.add(Material.BOW.getId()); // BOW
		repairableTools.add(Material.FISHING_ROD.getId()); // FISHING ROD
		
		for (int i = 298; i <= 317; i++) repairableArmor.add(i); // ALL ARMOR
	}
	
	private static Set<Interaction> interactions = new HashSet<Interaction>();
	
	public static void interact(Player player, Repairer repairman) {
		final ItemStack item = player.getItemInHand();
		
		double price = getPrice(repairman, item);
		
		boolean canRepair = false;
		if (repairman.type.equalsIgnoreCase("all")) {
			canRepair = (repairableTools.contains((item.getTypeId()))) || repairableArmor.contains((item.getTypeId())) ? true : false;
		} else if (repairman.type.equalsIgnoreCase("tools")) {
			canRepair = (repairableTools.contains((item.getTypeId()))) ? true : false;
		} else if (repairman.type.equalsIgnoreCase("armor")) {
			canRepair = repairableArmor.contains((item.getTypeId())) ? true : false;
		}
		
		if (item != null && canRepair) {
			Interaction interaction = new Interaction(player, repairman.entity, item, System.currentTimeMillis());

			for (Interaction inter : interactions) {
				if (interaction.equals(inter) && item.getDurability() != 0) {
					EconomyResponse r = Griswold.economy.depositPlayer(player.getName(), 1.05);
		            if(r.transactionSuccess()) {
						item.setDurability((short) 0);
						player.sendMessage(ChatColor.GOLD+"<"+repairman.name+">"+ChatColor.WHITE+" Отлично! Снова как новое!");
		            } else {
		            	
		            }
					return;
				}
			}
			
			if (interactions.size() > 10) interactions.clear();
			if (item.getDurability() == 0) {
				player.sendMessage(ChatColor.GOLD+"<"+repairman.name+">"+ChatColor.WHITE+" Хм, похоже тут все в порядке - ремонт не нужен.");
			} else {
				interactions.add(interaction);
				player.sendMessage(ChatColor.GOLD+"<"+repairman.name+">"+ChatColor.WHITE+" Я починю эту вещь для тебя за "+price+" коинов.");
				player.sendMessage(ChatColor.GOLD+"<"+repairman.name+">"+ChatColor.WHITE+" Если согласен - давай ее сюда.");
			}
		} else {
			player.sendMessage(ChatColor.GOLD+"<"+repairman.name+">"+ChatColor.WHITE+" Я не умею чинить такие вещи.");
		}
		
	}

	private static double getPrice(Repairer repairman, ItemStack item) {
		double price = 0;
		
		if (repairableTools.contains(item.getTypeId())) price = basicToolsPrice;
		else if (repairableTools.contains(item.getTypeId())) price = basicArmorPrice;
		
		Map<Enchantment, Integer> enchantments = item.getEnchantments();
		
		if (!enchantments.isEmpty()) {
			for (int i = 0; i<enchantments.size(); i++) {
				Object[] enchantsLevels = enchantments.values().toArray();
				price = price + enchantmentPrice * Integer.parseInt(enchantsLevels[i].toString());
			}
		}
		
		return price * repairman.cost;
	}
}
