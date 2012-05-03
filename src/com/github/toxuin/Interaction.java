package com.github.toxuin;

import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Interaction {
	Player player;
	int repairman;
	ItemStack item;
	long time;
	public Interaction(Player player, int repairman, ItemStack item, long time) {
		this.item = item;
		this.player = player;
		this.repairman = repairman;
		this.time = time;
	}
	
	public boolean equals(Interaction inter) {
		if ((inter.item == item) &&
			(inter.player == player) &&
			(inter.repairman == repairman) &&
			(time < inter.time+RepairMan.timeout)) { // „“€Š ’› ‹…€’œ … ’Ž ‘€‚ˆ‚€…˜œ
			Logger.getLogger("Minecraft").info(RepairMan.prefix+" "+time+" < "+inter.time+"!!!");
			return true;
		} else {
			Logger.getLogger("Minecraft").info(RepairMan.prefix+" "+time+" > "+inter.time+"!!!");
			return false;
		}
	}
}
