package com.github.toxuin.griswold.professions;

import com.github.toxuin.griswold.EventListener;
import com.github.toxuin.griswold.Griswold;
import com.github.toxuin.griswold.events.PlayerInteractGriswoldNPCEvent;
import com.github.toxuin.griswold.npcs.GriswoldNPC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class Shopkeeper implements Profession, InventoryHolder, Listener {
    private GriswoldNPC npc;

    private Inventory inventory = Bukkit.createInventory(this, InventoryType.CHEST, "SHOP");

    public Shopkeeper() {
        ItemStack item = new ItemStack(Material.DIAMOND, 64);
        ItemMeta meta = item.getItemMeta();
        List<String> lore = new ArrayList<String>();
        lore.add(ChatColor.GOLD + "Price: " + ChatColor.WHITE + "16");
        lore.add(ChatColor.GOLD + "In stock: " + ChatColor.MAGIC +"900");
        meta.setLore(lore);
        item.setItemMeta(meta);
        inventory.addItem(item);
        Griswold.plugin.getServer().getPluginManager().registerEvents(this, Griswold.plugin);
    }

    @Override
    public String use(PlayerInteractGriswoldNPCEvent event) {
        npc.makeSound();
        event.getPlayer().openInventory(inventory);
        return "Welcome to my store!";
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onOpenInventory(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (event.isLeftClick()) {
            // BUY 1
        } else if (event.isRightClick()) {
            // BUY 64
        }
        Griswold.log.info("YOU NO TAKE ME DIAMOND");
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCloseInventory(InventoryCloseEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        Player player = (Player) event.getPlayer();
        player.sendMessage("Thank you for your patronage!");
    }

    @Override
    public void setNpc(GriswoldNPC npc) {
        this.npc = npc;

        if (this.npc.entity instanceof Villager) {
            ((Villager) this.npc.entity).setProfession(Villager.Profession.PRIEST);
        }
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }
}
