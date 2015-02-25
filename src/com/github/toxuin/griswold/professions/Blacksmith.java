package com.github.toxuin.griswold.professions;

import com.github.toxuin.griswold.Griswold;
import com.github.toxuin.griswold.events.PlayerInteractGriswoldNPCEvent;
import com.github.toxuin.griswold.npcs.GriswoldNPC;
import com.github.toxuin.griswold.util.ClassProxy;
import com.github.toxuin.griswold.util.ConfigManager;
import com.github.toxuin.griswold.util.Lang;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class Blacksmith extends Profession {

    private GriswoldNPC npc;

    public boolean canRepairArmor = true;
    public boolean canRepairTools = true;
    public boolean canEnchant = true;

    public static double basicToolPrice = 10.0;
    public static double basicArmorPrice = 10.0;
    public static double enchantmentPrice = 30.0;

    public static double addEnchantmentPrice = 50.0;
    public static int maxEnchantBonus = 5;
    public static boolean clearEnchantments = false;

    private final List<Material> repairableTools = new LinkedList<Material>();
    private final List<Material> repairableArmor = new LinkedList<Material>();
    private final List<Material> notEnchantable = new LinkedList<Material>();

    final Class craftItemStack = ClassProxy.getClass("inventory.CraftItemStack");
    final Class enchantmentInstance = ClassProxy.getClass("WeightedRandomEnchant");
    final Class enchantmentManager = ClassProxy.getClass("EnchantmentManager");

    private Vector<PlayerInteractGriswoldNPCEvent> interactions = new Vector<PlayerInteractGriswoldNPCEvent>();
    private int nextIntercationSlot = 0;
    public double priceMultiplier = 1;

    public Blacksmith() {
        repairableTools.add(Material.IRON_AXE);
        repairableTools.add(Material.IRON_PICKAXE);
        repairableTools.add(Material.IRON_SWORD);
        repairableTools.add(Material.IRON_HOE);
        repairableTools.add(Material.IRON_SPADE);              // IRON TOOLS

        repairableTools.add(Material.WOOD_AXE);
        repairableTools.add(Material.WOOD_PICKAXE);
        repairableTools.add(Material.WOOD_SWORD);
        repairableTools.add(Material.WOOD_HOE);
        repairableTools.add(Material.WOOD_SPADE);              // WOODEN TOOLS

        repairableTools.add(Material.STONE_AXE);
        repairableTools.add(Material.STONE_PICKAXE);
        repairableTools.add(Material.STONE_SWORD);
        repairableTools.add(Material.STONE_HOE);
        repairableTools.add(Material.STONE_SPADE);             // STONE TOOLS

        repairableTools.add(Material.DIAMOND_AXE);
        repairableTools.add(Material.DIAMOND_PICKAXE);
        repairableTools.add(Material.DIAMOND_SWORD);
        repairableTools.add(Material.DIAMOND_HOE);
        repairableTools.add(Material.DIAMOND_SPADE);           // DIAMOND TOOLS

        repairableTools.add(Material.GOLD_AXE);
        repairableTools.add(Material.GOLD_PICKAXE);
        repairableTools.add(Material.GOLD_SWORD);
        repairableTools.add(Material.GOLD_HOE);
        repairableTools.add(Material.GOLD_SPADE);               // GOLDEN TOOLS

        repairableTools.add(Material.BOW);                      // BOW

        repairableTools.add(Material.FLINT_AND_STEEL);          // ZIPPO
        repairableTools.add(Material.SHEARS);                   // SCISSORS
        repairableTools.add(Material.FISHING_ROD);              // FISHING ROD
        repairableTools.add(Material.BOOK);                     // BOOK
        repairableTools.add(Material.ENCHANTED_BOOK);

        notEnchantable.add(Material.FLINT_AND_STEEL);
        notEnchantable.add(Material.SHEARS);
        notEnchantable.add(Material.FISHING_ROD);

        // ARMORZ!
        repairableArmor.add(Material.LEATHER_BOOTS);
        repairableArmor.add(Material.LEATHER_CHESTPLATE);
        repairableArmor.add(Material.LEATHER_HELMET);
        repairableArmor.add(Material.LEATHER_LEGGINGS);

        repairableArmor.add(Material.CHAINMAIL_BOOTS);
        repairableArmor.add(Material.CHAINMAIL_CHESTPLATE);
        repairableArmor.add(Material.CHAINMAIL_HELMET);
        repairableArmor.add(Material.CHAINMAIL_LEGGINGS);

        repairableArmor.add(Material.IRON_BOOTS);
        repairableArmor.add(Material.IRON_CHESTPLATE);
        repairableArmor.add(Material.IRON_HELMET);
        repairableArmor.add(Material.IRON_LEGGINGS);

        repairableArmor.add(Material.GOLD_BOOTS);
        repairableArmor.add(Material.GOLD_CHESTPLATE);
        repairableArmor.add(Material.GOLD_HELMET);
        repairableArmor.add(Material.GOLD_LEGGINGS);

        repairableArmor.add(Material.DIAMOND_BOOTS);
        repairableArmor.add(Material.DIAMOND_CHESTPLATE);
        repairableArmor.add(Material.DIAMOND_HELMET);
        repairableArmor.add(Material.DIAMOND_LEGGINGS);

        File configFile = new File(ConfigManager.directory, "config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        if (configFile.exists()) {
            if (config.isConfigurationSection("CustomItems.Tools")) {
                Set<String> tools = config.getConfigurationSection("CustomItems.Tools").getKeys(false);
                for (String itemId : tools) repairableTools.add(Material.getMaterial(Integer.parseInt(itemId)));
                Griswold.log.info("Added "+tools.size()+" custom tools from config file");
            }

            if (config.isConfigurationSection("CustomItems.Armor")) {
                Set<String> armor = config.getConfigurationSection("CustomItems.Armor").getKeys(false);
                for (String itemId : armor) repairableArmor.add(Material.getMaterial(Integer.parseInt(itemId)));
                Griswold.log.info("Added " + armor.size() + " custom armors from config file");
            }
        }
    }

    @Override
    public String use(PlayerInteractGriswoldNPCEvent event) {
        npc.makeSound();

        if (!canEnchant && !canRepairTools && !canRepairArmor) {
            Griswold.log.info(String.format(Lang.npc_cannot_do_anything, event.npc.name));
            return (String.format(Lang.name_format, npc.name) + Lang.chat_noitem);
        }

        // EMPTY HAND
        if (event.item.getType() == Material.AIR) {
            return (String.format(Lang.name_format, npc.name) + Lang.chat_noitem);
        }

        // REPAIRED ITEM, CANNOT ENCHANT
        if (event.item.getDurability() == 0 && !canEnchant) {
            return String.format(Lang.name_format, npc.name) + Lang.chat_norepair;
        }

        // WANTS ENCHANT, NEEDS REPAIR FIRST
        if (!canRepairTools && !canRepairArmor && canEnchant && event.item.getDurability() != 0) {
            return String.format(Lang.name_format, npc.name) + Lang.chat_needs_repair;
        }

        // ITEM NOT IN LISTS
        if ((!repairableArmor.contains(event.item.getType()) && !repairableTools.contains(event.item.getType())) || notEnchantable.contains(event.item.getType())) {
            return String.format(Lang.name_format, npc.name)+Lang.chat_cannot;
        }

        // CHECKING PERMISSIONS
        if (event.item.getDurability() != 0) {
            if (canRepairArmor && repairableArmor.contains(event.item.getType())) {
                if (!event.getPlayer().hasPermission("griswold.armor")) {
                    return ChatColor.RED + Lang.error_accesslevel;
                }
            } else if (canRepairTools && repairableTools.contains(event.item.getType())) {
                if (!event.getPlayer().hasPermission("griswold.tools")) {
                    return ChatColor.RED + Lang.error_accesslevel;
                }
            }
        } else if (canEnchant && !event.getPlayer().hasPermission("griswold.enchant")) {
            return ChatColor.RED + Lang.error_accesslevel;
        }

        // HANDLE INTERACTION QUEUEING
        for (PlayerInteractGriswoldNPCEvent e : interactions) {
            if (event.equals(e)) {
                interactions.remove(e);
                nextIntercationSlot--;
                if (event.item.getDurability() == 0 && canEnchant) {
                    return secondInteractionEnchant(event);
                } else return secondInteractionRepair(event);
            }
        }
        if (nextIntercationSlot < 0) nextIntercationSlot = 0;
        interactions.add(nextIntercationSlot++, event);
        if (nextIntercationSlot > 10) nextIntercationSlot = 0;
        return firstInteraction(event);
    }

    private String firstInteraction(PlayerInteractGriswoldNPCEvent event) {
        double price = Math.round(getPrice(event.item));
        String result;

        String notFreeMessage = Lang.chat_cost;
        String freeMessage = Lang.chat_free;

        if (event.item.getDurability() == 0) {
            price += addEnchantmentPrice;
            notFreeMessage = Lang.chat_enchant_cost;
            freeMessage = Lang.chat_enchant_free;
        }

        if (Griswold.economy != null) {
            result = String.format(Lang.name_format, npc.name) + String.format(notFreeMessage, price, Griswold.economy.currencyNamePlural());
        } else {
            result = String.format(Lang.name_format, npc.name) + freeMessage;
        }

        return result + "\n" + String.format(Lang.name_format, npc.name) + Lang.chat_agreed;
    }

    private String secondInteractionRepair(PlayerInteractGriswoldNPCEvent event) {
        double price = Math.round(getPrice(event.item));

        if (Griswold.economy != null) {
            if (Griswold.economy.getBalance(event.getPlayer()) <= price) {
                return String.format(Lang.name_format, npc.name) + Lang.chat_poor;
            } else {
                EconomyResponse response = Griswold.economy.withdrawPlayer(event.getPlayer(), price);
                if (!response.transactionSuccess()) {
                    return String.format(Lang.name_format, npc.name)+ChatColor.RED + Lang.chat_error;
                }
            }
        }

        event.item.setDurability((short) 0);
        return String.format(Lang.name_format, npc.name)+Lang.chat_done;
    }

    @SuppressWarnings("unchecked")       // TRUST ME, I'M A DOCTOR.
    private String secondInteractionEnchant(PlayerInteractGriswoldNPCEvent event) {
        double price = Math.round(getPrice(event.item)) + addEnchantmentPrice;

        if (Griswold.economy != null) {
            if (Griswold.economy.getBalance(event.getPlayer()) <= price) {
                return String.format(Lang.name_format, npc.name) + Lang.chat_poor;
            } else {
                EconomyResponse response = Griswold.economy.withdrawPlayer(event.getPlayer(), price);
                if (!response.transactionSuccess()) {
                    return String.format(Lang.name_format, npc.name)+ChatColor.RED + Lang.chat_error;
                }
            }
        }

        try { // REAL MAGIC!
            Method asNMSCopy = craftItemStack.getMethod("asNMSCopy", ItemStack.class);
            Object vanillaItem = asNMSCopy.invoke(null, (event.item.getType().equals(Material.ENCHANTED_BOOK)) ? new ItemStack(Material.BOOK) : event.item);
            int bonus = (new Random()).nextInt(maxEnchantBonus);
            Method b = enchantmentManager.getDeclaredMethod("b", Random.class, vanillaItem.getClass(), int.class);
            List<?> list = (List) b.invoke(null, new Random(), vanillaItem, bonus);

            EnchantmentStorageMeta bookmeta = null;
            ItemStack bookLeftovers = null;
            if (event.item.getType().equals(Material.BOOK)) {
                if (event.item.getAmount() > 1) bookLeftovers = new ItemStack(Material.BOOK, event.item.getAmount()-1);
                event.getPlayer().getInventory().remove(event.item);
                event.item = new ItemStack(Material.ENCHANTED_BOOK);
                bookmeta = (EnchantmentStorageMeta) event.item.getItemMeta();
            } else if (event.item.getType().equals(Material.ENCHANTED_BOOK)) {
                bookmeta = (EnchantmentStorageMeta) event.item.getItemMeta();
            }

            // REMOVE ALL ENCHANTMENTS
            if (clearEnchantments) {
                // TODO: CHECK FOR ENCHANTED BOOKS
                for (Enchantment enchantToDel : event.item.getEnchantments().keySet()) {
                    event.item.removeEnchantment(enchantToDel);
                }
            }

            if (list != null) {
                for (Object obj : list) {
                    Object instance = enchantmentInstance.cast(obj);
                    Field enchantmentField = enchantmentInstance.getField("enchantment");
                    Field levelField = enchantmentInstance.getField("level");
                    Object enchantment = enchantmentField.get(instance);
                    Field idField = enchantment.getClass().getField("id");
                    if (event.item.getType().equals(Material.ENCHANTED_BOOK) && bookmeta != null) {
                        bookmeta.addStoredEnchant(Enchantment.getById(Integer.parseInt(idField.get(enchantment).toString())), Integer.parseInt(levelField.get(instance).toString()), true);
                    } else {
                        event.item.addEnchantment(Enchantment.getById(Integer.parseInt(idField.get(enchantment).toString())), Integer.parseInt(levelField.get(instance).toString()));
                    }
                }

                if (event.item.getType().equals(Material.ENCHANTED_BOOK)) {
                    event.item.setItemMeta(bookmeta);
                    event.getPlayer().getInventory().setItemInHand(event.item);
                    if (bookLeftovers != null) {
                        // INVENTORY FULL, DROP TO PLAYER
                        if (event.getPlayer().getInventory().firstEmpty() == -1) {
                            event.getPlayer().getWorld().dropItemNaturally(event.getPlayer().getLocation(), bookLeftovers);
                        } else {
                            event.getPlayer().getInventory().addItem(bookLeftovers);
                        }
                    }
                }

                return String.format(Lang.name_format, npc.name) + Lang.chat_enchant_success;
            } else {
                return String.format(Lang.name_format, npc.name) + Lang.chat_enchant_failed;
            }

        } catch (Exception e) {
            if (ConfigManager.debug) e.printStackTrace();
            return String.format(Lang.name_format, npc.name) + Lang.chat_enchant_failed;
        }
    }

    private double getPrice(ItemStack item) {
        if (Griswold.economy == null) return 0.0;
        double price = 0;
        if (repairableTools.contains(item.getType())) price = basicToolPrice;
        else if (repairableArmor.contains(item.getType())) price = basicArmorPrice;
        price += item.getDurability();
        Map<Enchantment, Integer> enchantments = item.getEnchantments();
        if (!enchantments.isEmpty()) {
            for (Integer enchantmentLevel : enchantments.values()) {
                price += enchantmentPrice * enchantmentLevel;
            }
            /*
            for (int i = 0; i<enchantments.size(); i++) {
                Object[] enchantsLevels = enchantments.values().toArray();
                price = price + enchantmentPrice * Integer.parseInt(enchantsLevels[i].toString());
            }
            */
        }
        return price * priceMultiplier;
    }

    @Override
    public void setNpc(GriswoldNPC npc) {
        this.npc = npc;
        loadConfig();
        if (this.npc.entity instanceof Villager) {
            ((Villager) this.npc.entity).setProfession(Villager.Profession.BLACKSMITH);
        }
    }

    @Override
    public void loadConfig() {
        canRepairArmor = ConfigManager.getProfessionBoolean(npc.name, "canRepairArmor");
        canRepairTools = ConfigManager.getProfessionBoolean(npc.name, "canRepairTools");
        canEnchant = ConfigManager.getProfessionBoolean(npc.name, "canEnchant");
        basicToolPrice = ConfigManager.getProfessionDouble(npc.name, "basicToolPrice");
        basicArmorPrice = ConfigManager.getProfessionDouble(npc.name, "basicArmorPrice");
        enchantmentPrice = ConfigManager.getProfessionDouble(npc.name, "basicEnchantmentPrice");
        addEnchantmentPrice = ConfigManager.getProfessionDouble(npc.name, "pricePerEnchantment");
        maxEnchantBonus = ConfigManager.getProfessionInteger(npc.name, "enchantmentBonus");
        clearEnchantments = ConfigManager.getProfessionBoolean(npc.name, "clearOldEnchantments");
        priceMultiplier = ConfigManager.getProfessionDouble(npc.name, "costMultiplier");
    }

    @Override
    public void saveConfig() {
        ConfigManager.setProfessionParam(npc.name, "canRepairArmor", canRepairArmor);
        ConfigManager.setProfessionParam(npc.name, "canRepairTools", canRepairTools);
        ConfigManager.setProfessionParam(npc.name, "canEnchant", canEnchant);
        ConfigManager.setProfessionParam(npc.name, "basicArmorPrice", basicArmorPrice);
        ConfigManager.setProfessionParam(npc.name, "basicToolPrice", basicToolPrice);
        ConfigManager.setProfessionParam(npc.name, "basicEnchantmentPrice", enchantmentPrice);
        ConfigManager.setProfessionParam(npc.name, "pricePerEnchantment", addEnchantmentPrice);
        ConfigManager.setProfessionParam(npc.name, "enchantmentBonus", maxEnchantBonus);
        ConfigManager.setProfessionParam(npc.name, "clearEnchantments", clearEnchantments);
        ConfigManager.setProfessionParam(npc.name, "costMultiplier", priceMultiplier);
    }

    @Override
    public String getName() {
        return "Blacksmith";
    }

    @Override
    protected void finalize() throws Throwable {
        repairableTools.clear();
        repairableArmor.clear();
        notEnchantable.clear();
        super.finalize();
    }
}
