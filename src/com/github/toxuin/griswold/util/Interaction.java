package com.github.toxuin.griswold.util;

import com.github.toxuin.griswold.Griswold;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class Interaction {

    private final UUID player;
    private final Entity repairman;
    private final ItemStack item;
    private final int damage;
    private final long time;
    public boolean valid;

    public Interaction(UUID playerId, Entity repairman, ItemStack item, int dmg, long time) {
        this.item = item;
        this.damage = dmg;
        this.player = playerId;
        this.repairman = repairman;
        this.time = time;
        this.valid = true;
    }

    public boolean equals(Interaction inter) {
        int delta = (int) (time - inter.time);
        return ((inter.item.equals(item))
                && (inter.valid)
                && (inter.damage == damage)
                && (inter.player.equals(player))
                && (inter.repairman.equals(repairman))
                && (delta < Griswold.timeout));
    }

    @Override
    public int hashCode() {
        int result = player != null ? player.hashCode() : 0;
        result = 31 * result + (repairman != null ? repairman.hashCode() : 0);
        result = 31 * result + (item != null ? item.hashCode() : 0);
        result = 31 * result + damage;
        result = 31 * result + (int) (time ^ (time >>> 32));
        result = 31 * result + (valid ? 1 : 0);
        return result;
    }
}