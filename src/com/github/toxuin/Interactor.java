package com.github.toxuin;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Interactor {
	private final static List<Integer> repairableIDs;
	static {
		repairableIDs = new LinkedList<Integer>();
		for (int i = 256; i <= 259; i++) repairableIDs.add(i); // IRON TOOLS AND ZIPPO
		for (int i = 267; i <= 279; i++) repairableIDs.add(i); // WOODEN, STONE, DIAMOND TOOLS
		for (int i = 283; i <= 286; i++) repairableIDs.add(i); // GOLDEN TOOLS
		for (int i = 290; i <= 294; i++) repairableIDs.add(i); // HOES
		for (int i = 298; i <= 317; i++) repairableIDs.add(i); // ALL ARMOR
		repairableIDs.add(359); // SCISSORS
		repairableIDs.add(Material.BOW.getId()); // BOW
		repairableIDs.add(Material.FISHING_ROD.getId()); // FISHING ROD
	}
	
	private static Set<Interaction> interactions = new HashSet<Interaction>();
	
	public static void interact(Player player, Repairer repairman) {
		final ItemStack item = player.getItemInHand();
		
		double price = getPrice(item);
		
		if (item != null && repairableIDs.contains((item.getTypeId()))) {
			Interaction interaction = new Interaction(player, repairman.entity, item, System.currentTimeMillis());

			for (Interaction inter : interactions) {
				if (interaction.equals(inter) && item.getDurability() != 0) {
					item.setDurability((short) 0);
					player.sendMessage(ChatColor.GOLD+"<"+repairman.name+">"+ChatColor.WHITE+" Отлично! Снова как новое!");
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
			player.sendMessage(ChatColor.GOLD+"<"+repairman.name+">"+ChatColor.WHITE+" Я не могу чинить такие вещи.");
		}
		
	}

	private static double getPrice(ItemStack item) {
		// TODO Auto-generated method stub
		return 100500;
	}
}
