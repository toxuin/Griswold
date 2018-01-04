package com.github.toxuin.griswold;

import com.github.toxuin.griswold.util.RepairerType;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public abstract class Repairer {
    private static final String DEFAULT_SOUND = "ENTITY_VILLAGER_TRADING";

    public String name = "Repairman";
    public String sound = DEFAULT_SOUND;
    private Location loc;
    private RepairerType type = RepairerType.ALL;
    private double cost = 1.0d;

    public Repairer(final String name, final Location loc, final String sound, final String type, final double cost) {
        this.name = name;
        this.loc = loc;
        this.sound = sound;
        this.type = RepairerType.fromString(type);
        this.cost = cost;
    }

    public abstract void haggle();

    // GETTERS & SETTERS

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getLocation() {
        return this.loc;
    }

    public void setLocation(Location loc) {
        this.loc = loc;
    }

    public RepairerType getType() {
        return type;
    }

    public void setType(RepairerType type) {
        this.type = type;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public String getSound() {
        return sound;
    }

    public static String getDefaultSound() {
        return DEFAULT_SOUND;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }

    public abstract boolean isSpawned();

    public abstract void setSpawned(boolean newState);

    public abstract Entity getEntity();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Repairer repairer = (Repairer) o;

        if (Double.compare(repairer.cost, cost) != 0) return false;
        if (name != null ? !name.equals(repairer.name) : repairer.name != null) return false;
        if (loc != null ? !loc.equals(repairer.loc) : repairer.loc != null) return false;
        if (type != repairer.type) return false;
        return sound != null ? sound.equals(repairer.sound) : repairer.sound == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = name != null ? name.hashCode() : 0;
        result = 31 * result + (loc != null ? loc.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        temp = Double.doubleToLongBits(cost);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (sound != null ? sound.hashCode() : 0);
        return result;
    }
}
