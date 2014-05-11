package com.github.toxuin;

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
        if (command.equalsIgnoreCase("reload")) {
            // ONLY CONSOLE, OP, griswold.admin
            if (sender instanceof ConsoleCommandSender) return true;
            if (Griswold.permission == null) return sender.isOp();
            else return Griswold.permission.has(sender, "griswold.admin");
        } else if (command.equalsIgnoreCase("create")) {
             // ONLY OP, griswold.admin
            if (!(sender instanceof Player)) return false;
            if (Griswold.permission == null) return sender.isOp();
            else return Griswold.permission.has(sender, "griswold.admin");
        } else if (command.equalsIgnoreCase("remove")) {
             // ONLY OP, griswold.admin, CONSOLE
            if (sender instanceof ConsoleCommandSender) return true;
            if (Griswold.permission == null) return sender.isOp();
            else return Griswold.permission.has(sender, "griswold.admin");
        } else if (command.equalsIgnoreCase("list")) {
            // ONLY OP, griswold.admin, CONSOLE
            if (sender instanceof ConsoleCommandSender) return true;
            if (Griswold.permission == null) return sender.isOp();
            else return Griswold.permission.has(sender, "griswold.admin");
        } else if (command.equalsIgnoreCase("despawn")) {
            // ONLY OP, griswold.admin, CONSOLE
            if (sender instanceof ConsoleCommandSender) return true;
            if (Griswold.permission == null) return sender.isOp();
            else return Griswold.permission.has(sender, "griswold.admin");
        } else if (command.equalsIgnoreCase("names")) {
            // ONLY OP, griswold.admin, CONSOLE
            if (sender instanceof ConsoleCommandSender) return true;
            if (Griswold.permission == null) return sender.isOp();
            else return Griswold.permission.has(sender, "griswold.admin");
        } else if (command.equalsIgnoreCase("sound")) {
            // ONLY OP, griswold.admin, CONSOLE
            if (sender instanceof ConsoleCommandSender) return true;
            if (Griswold.permission == null) return sender.isOp();
            else return Griswold.permission.has(sender, "griswold.admin");
        }

        // UNKNOWN COMMAND. YES, THAT'S SAFE.
        return true;
    }


    public boolean onOldCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if(cmd.getName().equalsIgnoreCase("blacksmith")) {
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("reload")) {
                    if (sender.isOp() || sender instanceof ConsoleCommandSender || plugin.permission.has(sender, "griswold.admin")) {
                        plugin.reloadPlugin();
                    } else {
                        sender.sendMessage(ChatColor.RED+Lang.error_accesslevel);
                        return false;
                    }
                    return true;
                }
                if (args[0].equalsIgnoreCase("create")) {
                    if ((plugin.permission == null && sender.isOp()) || (sender instanceof Player && (plugin.permission.has(sender, "griswold.admin") || sender.isOp()))) {
                        if (args.length >= 2) {
                            Player player = (Player) sender;
                            Location location = player.getLocation().toVector().add(player.getLocation().getDirection().multiply(3)).toLocation(player.getWorld());
                            location.setY(Math.round(player.getLocation().getY()));
                            String name = args[1];
                            if (args.length < 4) {
                                plugin.createRepairman(name, location);
                                player.sendMessage(Lang.new_created);
                            } else {
                                String type = args[2];
                                String cost = args[3];
                                plugin.createRepairman(name, location, type, cost);
                                player.sendMessage(Lang.new_created);
                            }
                        } else sender.sendMessage(Lang.insufficient_params);
                    } else {
                        sender.sendMessage(ChatColor.RED+Lang.error_accesslevel);
                        return false;
                    }
                    return true;
                }
                if (args[0].equalsIgnoreCase("remove")) {
                    if (plugin.permission != null) {
                        if (args.length>1 && plugin.permission.has(sender, "griswold.admin")) {
                            plugin.removeRepairman(args[1]);
                            sender.sendMessage(String.format(Lang.deleted, args[1]));
                        } else {
                            sender.sendMessage(ChatColor.RED+Lang.error_accesslevel);
                        }
                    } else if (sender instanceof ConsoleCommandSender || sender.isOp()) {
                        plugin.removeRepairman(args[1]);
                        sender.sendMessage(String.format(Lang.deleted, args[1]));
                    }
                }
                if (args[0].equalsIgnoreCase("list")) {
                    if (plugin.permission != null) {
                        if (plugin.permission.has(sender, "griswold.admin")) plugin.listRepairmen(sender);
                    } else {
                        if (sender instanceof ConsoleCommandSender || sender.isOp())  plugin.listRepairmen(sender);
                    }
                }
                if (args[0].equalsIgnoreCase("despawn")) {
                    if (plugin.permission != null) {
                        if (plugin.permission.has(sender, "griswold.admin")) {
                            plugin.despawnAll();
                            sender.sendMessage(Lang.despawned);
                        } else {
                            sender.sendMessage(ChatColor.RED+Lang.error_accesslevel);
                        }
                    } else if (sender instanceof ConsoleCommandSender || sender.isOp()) {
                        plugin.despawnAll();
                        sender.sendMessage(Lang.despawned);
                    }
                }
                if (args[0].equalsIgnoreCase("names")) {
                    if (plugin.permission != null) {
                        if (plugin.permission.has(sender, "griswold.admin")) {
                            plugin.toggleNames();
                            sender.sendMessage(plugin.namesVisible?Lang.names_on:Lang.names_off);
                        } else {
                            sender.sendMessage(ChatColor.RED+Lang.error_accesslevel);
                        }
                    } else if (sender instanceof ConsoleCommandSender || sender.isOp()) {
                        plugin.toggleNames();
                        sender.sendMessage(plugin.namesVisible?Lang.names_on:Lang.names_off);
                    }
                }
                if (args[0].equalsIgnoreCase("sound")) {
                    if (args.length < 3) {
                        sender.sendMessage(Lang.error_few_arguments);
                        return false;
                    }
                    if (plugin.permission != null) {
                        if (plugin.permission.has(sender, "griswold.admin")) {
                            plugin.setSound(args[1], args[2]);
                            sender.sendMessage(String.format(Lang.sound_changed, args[1]));
                        } else {
                            sender.sendMessage(ChatColor.RED+Lang.error_accesslevel);
                        }
                    } else if (sender instanceof ConsoleCommandSender || sender.isOp()) {
                        plugin.setSound(args[1], args[2]);
                        sender.sendMessage(String.format(Lang.sound_changed, args[1]));
                    }
                }
            } else {
                sender.sendMessage(ChatColor.RED+Lang.error_few_arguments);
                return true;
            }
        }
        return false;
    }
}
