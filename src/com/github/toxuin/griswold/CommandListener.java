package com.github.toxuin.griswold;

import com.github.toxuin.griswold.util.RepairerType;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class CommandListener implements CommandExecutor, TabCompleter {
    private Griswold plugin;

    CommandListener(Griswold griswold) {
        this.plugin = griswold;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Lang.error_few_arguments);
            return false;
        }
        if (!can(sender, args[0])) {
            sender.sendMessage(ChatColor.RED + Lang.error_accesslevel);
            return true;
        }

        // RELOAD COMMAND
        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadPlugin();
            return true;
        }

        // CREATE COMMAND
        else if (args[0].equalsIgnoreCase("create")) {
            if (args.length < 2) {
                sender.sendMessage(Lang.insufficient_params);
                return true;
            }

            Player player = (Player) sender; // check performed in can() method
            Location location = player.getLocation().toVector().add(player.getLocation().getDirection().multiply(3)).toLocation(player.getWorld());
            location.setY(Math.round(player.getLocation().getY()));
            if (args.length < 3) {
                plugin.createRepairman(args[1], location);
            } else if (args.length < 4) {
                if (!RepairerType.present(args[2])) {
                    player.sendMessage(Lang.chat_type_error);
                    return true;
                }
                plugin.createRepairman(args[1], location, args[2]);
            } else {
                if (!RepairerType.present(args[2])) {
                    player.sendMessage(Lang.chat_type_error);
                    return true;
                }
                plugin.createRepairman(args[1], location, args[2], args[3]);
            }
            player.sendMessage(Lang.new_created);
            return true;
        }

        // REMOVE COMMAND
        else if (args[0].equalsIgnoreCase("remove")) {
            if (args.length < 2) {
                sender.sendMessage(Lang.error_few_arguments);
                return true;
            }

            plugin.removeRepairman(args[1]);
            sender.sendMessage(String.format(Lang.deleted, args[1]));
            return true;
        }

        // LIST COMMAND
        else if (args[0].equalsIgnoreCase("list")) {
            plugin.listRepairmen(sender);
            return true;
        }

        // DESPAWN
        else if (args[0].equalsIgnoreCase("despawn")) {
            plugin.despawnAll();
            sender.sendMessage(Lang.despawned);
            return true;
        }

        // NAMES COMMAND
        else if (args[0].equalsIgnoreCase("names")) {
            plugin.toggleNames();
            sender.sendMessage(Griswold.namesVisible ? Lang.names_on : Lang.names_off);
            return true;
        }

        // SOUND COMMAND
        else if (args[0].equalsIgnoreCase("sound")) {
            if (args.length < 3) {
                sender.sendMessage(Lang.error_few_arguments);
                return true;
            }

            plugin.setSound(args[1], args[2]);
            sender.sendMessage(String.format(Lang.sound_changed, args[1]));
            return true;
        }

        /*
        // DAMAGE COMMAND, ONLY FOR TESTING!
        else if (args[0].equalsIgnoreCase("dmg")) {
            if (!(sender instanceof Player)) return false;
            Player player = (Player) sender;
            // ItemStack item = player.getInventory().getItemInMainHand();
            ItemStack item = player.getItemInHand();
            item.setDurability((short) 17);
        }
        */

        // HIDE COMMAND
        else if (args[0].equalsIgnoreCase("hide")) {
            if (args.length < 2) {
                sender.sendMessage(Lang.error_few_arguments);
                return true;
            }
            try {
                Repairer.getByName(args[1]).despawn();
                sender.sendMessage(Lang.chat_hidden);
            } catch (IllegalArgumentException ignored) {
                sender.sendMessage(Lang.error_remove);
            }
            return true;
        }

        // UNHIDE COMMAND
        else if (args[0].equalsIgnoreCase("unhide")) {
            if (args.length < 2) {
                sender.sendMessage(Lang.error_few_arguments);
                return true;
            }
            try {
                Repairer.getByName(args[1]).spawn();
                sender.sendMessage(Lang.chat_unhidden);
            } catch (IllegalArgumentException ignored) {
                sender.sendMessage(Lang.chat_error);
            }
            return true;
        }

        return false;
    }

    private boolean can(CommandSender sender, String command) {
        if (!(command.equalsIgnoreCase("reload") || command.equalsIgnoreCase("create") ||
                command.equalsIgnoreCase("remove") || command.equalsIgnoreCase("list") ||
                command.equalsIgnoreCase("despawn") || command.equalsIgnoreCase("names") ||
                command.equalsIgnoreCase("sound") || command.equalsIgnoreCase("hide") ||
                command.equalsIgnoreCase("unhide"))) {
            // UNKNOWN COMMAND.
            return true;
        }

        if (command.equalsIgnoreCase("create") && !(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You have to be a player to do that...");
            return false;
        }

        return sender instanceof ConsoleCommandSender || sender.hasPermission("griswold.admin");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        List<String> completions = new ArrayList<>();

        // NO ARGS
        if (args.length == 1) {
            List<String> commands = new ArrayList<>();
            commands.add("reload");
            commands.add("create");
            commands.add("remove");
            commands.add("list");
            commands.add("despawn");
            commands.add("names");
            commands.add("sound");
            commands.add("hide");
            commands.add("unhide");
            StringUtil.copyPartialMatches(args[0], commands, completions);
            return completions;
        }

        // NO PERMS
        if (args.length == 2 && !args[1].isEmpty() && !can(sender, args[1])) {
            return completions;
        }

        // RELOAD COMMAND
        if (args[0].startsWith("reload")) {
            return completions;
        }

        // CREATE COMMAND
        else if (args[0].startsWith("create")) {
            if (args.length < 3) {
                completions.add("<name>");
                return completions;
            } else if (args.length == 3) {
                completions.add("tools");
                completions.add("armor");
                completions.add("both");
                completions.add("enchant");
                completions.add("all");
                return completions;
            }
        }

        // REMOVE COMMAND
        else if (args[0].startsWith("remove")) {
            return completions;
        }

        // LIST COMMAND
        else if (args[0].startsWith("list")) {
            return completions;
        }

        // DESPAWN
        else if (args[0].startsWith("despawn")) {
            return completions;
        }

        // NAMES COMMAND
        else if (args[0].startsWith("names")) {
            return completions;
        }

        // SOUND COMMAND
        else if (args[0].startsWith("sound")) {
            return completions;
        }

        // HIDE COMMAND
        else if (args[0].startsWith("hide")) {
            return completions;
        }

        // UNHIDE COMMAND
        else if (args[0].startsWith("unhide")) {
            return completions;
        }

        return completions;

    }
}
