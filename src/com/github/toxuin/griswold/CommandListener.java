package com.github.toxuin.griswold;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class CommandListener implements CommandExecutor {
    private Griswold plugin;

    public CommandListener(Griswold griswold) {
        this.plugin = griswold;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (args.length == 0) { sender.sendMessage(Lang.error_few_arguments); return false; }
        if (!can(sender, args[0])) { sender.sendMessage(ChatColor.RED+Lang.error_accesslevel); return true; }

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

            Player player = (Player) sender;
            Location location = player.getLocation().toVector().add(player.getLocation().getDirection().multiply(3)).toLocation(player.getWorld());
            location.setY(Math.round(player.getLocation().getY()));
            if (args.length < 4) plugin.createRepairman(args[1], location);
            else plugin.createRepairman(args[1], location, args[2], args[3]);
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
            sender.sendMessage(plugin.namesVisible?Lang.names_on:Lang.names_off);
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
        }

        return false;
    }

	private boolean can(CommandSender sender, String command) {
		if (!(command.equalsIgnoreCase("reload") || command.equalsIgnoreCase("create") ||
				command.equalsIgnoreCase("remove") || command.equalsIgnoreCase("list") ||
				command.equalsIgnoreCase("despawn") || command.equalsIgnoreCase("names") ||
				command.equalsIgnoreCase("sound"))) {
			// UNKNOWN COMMAND.
			return true;
		}

		if (!command.equalsIgnoreCase("create") && !(sender instanceof Player)) {
			return false;
		}

		if (sender instanceof ConsoleCommandSender) return true;
		//if (Griswold.permission == null) return sender.isOp();
		return sender.hasPermission("griswold.admin");
	}
}
