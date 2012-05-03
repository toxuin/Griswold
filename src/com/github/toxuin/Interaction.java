package com.github.toxuin;

import java.util.logging.Logger;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Interaction {
	Player player;
	Entity repairman;
	ItemStack item;
	long time;
	public Interaction(Player player, Entity repairman, ItemStack item, long time) {
		this.item = item;
		this.player = player;
		this.repairman = repairman;
		this.time = time;
	}
	
	public boolean equals(Interaction inter) {
		int delta = (int) (time-inter.time);
		if ((inter.item.equals(item)) &&
			(inter.player.equals(player)) &&
			(inter.repairman.equals(repairman)) &&
			(delta < RepairMan.timeout)) {
			Logger.getLogger("Minecraft").info(RepairMan.prefix+" TRUE,  "+time+" - "+inter.time+" = " + (time-inter.time) + " < " + RepairMan.timeout);
			return true;
		} else {
			Logger.getLogger("Minecraft").info(RepairMan.prefix+" FALSE,  "+time+" - "+inter.time+" = " + (time-inter.time) + " > " + RepairMan.timeout);
			return false;
		}
	}
}
