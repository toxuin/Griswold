package com.github.toxuin.griswold;

import com.github.toxuin.griswold.util.RepairerType;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Random;

public class Repairer {

    public Entity entity;
    public String name = "Repairman";
    private Location loc;
    private RepairerType type = RepairerType.ALL;
    private double cost = 1.0d;
    private static final String DEFAULT_SOUND = "ENTITY_VILLAGER_TRADING";
    private String sound = "ENTITY_VILLAGER_TRADING";
    private Random rnd = new Random();

    final Class entityInsentient = ClassProxy.getClass("EntityInsentient");
    final Class entityHuman = ClassProxy.getClass("EntityHuman");
    final Class pathfinderGoalSelector = ClassProxy.getClass("PathfinderGoalSelector");
    final Class pathfinderGoalLookAtPlayer = ClassProxy.getClass("PathfinderGoalLookAtPlayer");
    final Class pathfinderGoalRandomLookaround = ClassProxy.getClass("PathfinderGoalRandomLookaround");
    final Class pathfinderGoal = ClassProxy.getClass("PathfinderGoal");
    final Class craftWorld = ClassProxy.getClass("CraftWorld");
    final Class craftEntity = ClassProxy.getClass("entity.CraftEntity");
    final Class craftVillager = ClassProxy.getClass("entity.CraftVillager");
    final Class entityClass = ClassProxy.getClass("Entity");

    public Repairer(final String name, final Location loc, final String sound, final String type, final double cost) {
        this.name = name;
        this.loc = loc;
        this.sound = sound;
        this.type = RepairerType.fromString(type);
        this.cost = cost;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void overwriteAI() {
        try {
            Method getHandle = craftVillager.getMethod("getHandle");
            Object villager = getHandle.invoke(craftVillager.cast(entity));
            Field goalsField = entityInsentient.getDeclaredField("goalSelector");
            goalsField.setAccessible(true);
            Object goals = pathfinderGoalSelector.cast(goalsField.get(villager));
            Field listField = pathfinderGoalSelector.getDeclaredField("b");
            listField.setAccessible(true);
            Collection list = (Collection) listField.get(goals);
            list.clear();

            Method setGoal = pathfinderGoalSelector.getMethod("a", int.class, pathfinderGoal);
            Constructor<?> lookAtPlayerConstructor = pathfinderGoalLookAtPlayer.getConstructor(entityInsentient, Class.class, float.class, float.class);
            Constructor<?> randomLookAroundConstructor = pathfinderGoalRandomLookaround.getConstructor(entityInsentient);

            setGoal.invoke(goals, 1, lookAtPlayerConstructor.newInstance(villager, entityHuman, 12.0F, 1.0F));
            setGoal.invoke(goals, 2, randomLookAroundConstructor.newInstance(villager));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public void haggle() {
        if (craftEntity == null) return;
        if (this.sound != null && !this.sound.isEmpty() && !this.sound.equals("mute") && craftEntity.isInstance(this.entity)) {
            try {
                Sound snd = Sound.valueOf(sound);
                this.entity.getWorld().playSound(entity.getLocation(), snd, 1.2f, 1.6F + (this.rnd.nextFloat() - this.rnd.nextFloat()) * 0.4F);
            } catch (IllegalArgumentException e) {
                Griswold.log.info("NPC " + name + " has invalid sound " + sound + "! Disabling sound for it...");
                sound = null;
            }
        }

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getLocation() {
        return loc;
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
}
