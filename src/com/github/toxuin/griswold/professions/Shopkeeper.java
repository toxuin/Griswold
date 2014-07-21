package com.github.toxuin.griswold.professions;

import com.github.toxuin.griswold.Griswold;
import com.github.toxuin.griswold.events.PlayerInteractGriswoldNPCEvent;
import com.github.toxuin.griswold.npcs.GriswoldNPC;
import com.github.toxuin.griswold.util.Lang;
import com.github.toxuin.griswold.util.Pair;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class Shopkeeper implements Profession, InventoryHolder, Listener {
    private GriswoldNPC npc;
    private Map<ItemStack, Pair<Integer, Double>> storage = new HashMap<ItemStack, Pair<Integer, Double>>();
    private Inventory inventory;

    public boolean canBuy = false;
    public double buyMultiplier = 0.7;

    public Shopkeeper() {
        Griswold.plugin.getServer().getPluginManager().registerEvents(this, Griswold.plugin);
    }

    @Override
    public String use(PlayerInteractGriswoldNPCEvent event) {
        npc.makeSound();
        event.getPlayer().openInventory(inventory);
        return "Welcome to my store!";
    }

    @Override
    public void setNpc(GriswoldNPC npc) {
        this.npc = npc;
        Bukkit.createInventory(this, InventoryType.CREATIVE);
        inventory = Bukkit.createInventory(this, 54, npc.name+"'s shop");

        addItem(new ItemStack(Material.DIAMOND, 64), 900, 16);

        if (this.npc.entity instanceof Villager) {
            ((Villager) this.npc.entity).setProfession(Villager.Profession.LIBRARIAN);
        }
    }

    private void playerBuyItem(Player player, ItemStack item) {
        double price = getPrice(item);
        if (Griswold.economy.getBalance(player) <= price) {
            player.sendMessage(String.format(Lang.name_format, npc.name) + Lang.chat_poor);
            return;
        }
        EconomyResponse response = Griswold.economy.withdrawPlayer(player, price);
        if (!response.transactionSuccess()) {
            player.sendMessage(String.format(Lang.name_format, npc.name)+ChatColor.RED + Lang.chat_error);
            return;
        }
        //ItemStack resultItem = new ItemStack(item.getType(), item.getAmount());

        Pair<Integer, Double> quantityPrice = storage.get(item);
        if (quantityPrice.getLeft() == 1) {
            storage.remove(item);
            inventory.remove(item);
        } else if (quantityPrice.getLeft() != -1) {
            storage.put(item, new Pair<Integer, Double>((quantityPrice.getLeft()-1), quantityPrice.getRight()));
            if (quantityPrice.getLeft() < 64) {
                item.setAmount(quantityPrice.getLeft());
            } else item.setAmount(64);
            inventory.setItem(inventory.first(item), item);
        }
        redrawInventory();
        item.setItemMeta(null);
        giveItem(player, item);
    }

    private boolean playerSellItem(Player player, ItemStack item) {
        double price = getPrice(item) * buyMultiplier;
        if (!inventory.contains(item)) {
            player.sendMessage(String.format(Lang.name_format, npc.name) + " I do not need this.");
            return false;
        }
        Pair<Integer, Double> quantityPrice = storage.get(item);
        if (quantityPrice.getLeft() != -1) {
            storage.put(item, new Pair<Integer, Double>((quantityPrice.getLeft()+item.getAmount()), price));
        }
        EconomyResponse response = Griswold.economy.depositPlayer(player, price);
        if (!response.transactionSuccess()) {
            player.sendMessage(String.format(Lang.name_format, npc.name)+ChatColor.RED + Lang.chat_error);
            return false;
        }
        redrawInventory();
        player.getInventory().remove(item);
        return true;
    }

    private double getPrice(ItemStack item) {
        return 16d;
    }

    private void redrawInventory() {
        inventory.clear();
        for (ItemStack item : storage.keySet()) {
            int quantity = storage.get(item).getLeft();
            if (quantity > 64) quantity = 64;
            visualAddItem(item, quantity, storage.get(item).getRight(), -1); // TODO: SLOT HANDLING
        }
    }

    /**
     * Adds an item to npc stock.
     *
     * @param item Item to be added to npc stock
     * @param quantity quantity to add. set to -1 for infinite
     * @param price price per item.
     * @param slot slot in inventory to add to. Set to -1 for first empty.
     */
    private void addItem(ItemStack item, int quantity, double price, int slot) {
        if (item == null) return;
        visualAddItem(item, quantity, price, slot);
        storage.put(item, new Pair<Integer, Double>(quantity, price));
    }

    private void visualAddItem(ItemStack item, int quantity, double price, int slot) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = new ArrayList<String>();
        lore.add(ChatColor.GOLD + "Price: " + ChatColor.WHITE + price);
        lore.add(ChatColor.GOLD + "To sell: " + ChatColor.WHITE + Math.round(price * buyMultiplier));
        lore.add(ChatColor.GOLD + "In stock: " + ChatColor.WHITE + (quantity == -1 ? ChatColor.MAGIC + "900" : String.valueOf(quantity)));
        meta.setLore(lore);
        if (quantity == -1) item.setAmount(1);
        item.setAmount(quantity >= 64 ? 64 : quantity);
        item.setItemMeta(meta);
        if (slot == -1) {
            // TRY TO ADD TO EXISTING STACK IF FULL
            inventory.addItem(item);
        } else inventory.setItem(slot, item);
    }

    private void addItem(ItemStack item, int quantity, double price) {
        addItem(item, quantity, price, inventory.firstEmpty());
    }

    @EventHandler()
    public void onInventoryDragEvent(InventoryDragEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        for (int slot : event.getRawSlots()) {
            if (slot < event.getView().getTopInventory().getSize()) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (event.getAction().equals(InventoryAction.NOTHING)) return;
        if (event.getClick().isKeyboardClick()) {
            event.setCancelled(true);
            return;
        }

        if (event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
            if (event.getRawSlot() > event.getView().getTopInventory().getSize()) {
                event.setCancelled(true);
                playerSellItem((Player) event.getWhoClicked(), event.getCurrentItem());
            } else {
                event.setCancelled(true);
                playerBuyItem((Player) event.getWhoClicked(), event.getCurrentItem());
            }
            return;

        }

        switch (event.getAction()) {
            case PLACE_SOME:
            case PLACE_ONE:
            case PLACE_ALL:
                if (event.getRawSlot() < event.getView().getTopInventory().getSize()) {
                    event.setCancelled(true);
                    if (event.getWhoClicked() instanceof Player) {
                        playerSellItem((Player) event.getWhoClicked(), event.getCurrentItem());
                    }
                    break;
                } else return;
            case PICKUP_ALL:
                event.getCurrentItem().setAmount(1);
            case PICKUP_HALF:
            case PICKUP_ONE:
            case PICKUP_SOME:
                if (event.getRawSlot() < event.getView().getTopInventory().getSize()) {
                    event.setCancelled(true);
                    if (event.getWhoClicked() instanceof Player) {
                        playerBuyItem((Player) event.getWhoClicked(), event.getCurrentItem());
                    }
                    break;
                }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCloseInventory(InventoryCloseEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        npc.makeSound();
    }

    private void giveItem(Player player, ItemStack item) {
        if (player.getInventory().firstEmpty() == -1) {
            player.getWorld().dropItemNaturally(player.getLocation(), item);
        } else {
            player.getInventory().addItem(item);
        }
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }
}
