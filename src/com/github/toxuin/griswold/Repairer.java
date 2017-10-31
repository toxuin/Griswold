package com.github.toxuin.griswold;

import com.github.toxuin.griswold.util.Pair;
import com.github.toxuin.griswold.util.RepairerType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Villager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;
import java.util.Set;

public class Repairer {

    private Entity entity;
    public String name = "Repairman";
    private Location loc;
    private RepairerType type = RepairerType.ALL;
    private double cost = 1.0d;
    private static final String DEFAULT_SOUND = "ENTITY_VILLAGER_TRADING";
    private String sound = "ENTITY_VILLAGER_TRADING";
    private boolean spawned;

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
        this.spawned = false;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void overwriteAI() {
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
    void haggle() {
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

    void loadChunk() {
        getLocation().getWorld().loadChunk(getLocation().getChunk());
    }

    void spawn() {
        if(spawned) return;
        Location loc = getLocation();
        if (loc == null) {
            Griswold.log.info("ERROR: LOCATION IS NULL");
            return;
        }
        if (getType().equals(RepairerType.ENCHANT) && !Interactor.enableEnchants) {
            Griswold.log.info(String.format(Lang.error_enchanter_not_spawned, loc.getX(), loc.getY(), loc.getZ()));
            return;
        }
        LivingEntity repairman = (LivingEntity) loc.getWorld().spawn(loc, EntityType.VILLAGER.getEntityClass());
        repairman.setCustomNameVisible(Griswold.namesVisible);
        repairman.setCustomName(name);
        if (getType().equals(RepairerType.ENCHANT)) {
            ((Villager) repairman).setProfession(Villager.Profession.LIBRARIAN);
        } else {
            ((Villager) repairman).setProfession(Villager.Profession.BLACKSMITH);
        }

        this.entity = repairman;

        if (!Griswold.npcChunks.containsKey(this))
            Griswold.npcChunks.put(this, new Pair(loc.getChunk().getX(), loc.getChunk().getZ()));

        this.overwriteAI();

        // FILTER DUPLICATES
        if (Griswold.findDuplicates)
            Arrays.asList(getLocation().getChunk().getEntities()).forEach((doppelganger) -> {
                if (this.entityClass == null) return; // YOU'RE WEIRD
                if (!(doppelganger.getLocation().distance(getLocation()) <= Griswold.duplicateFinderRadius)) return;
                Class craftVillagerClass = ClassProxy.getClass("entity.CraftVillager");
                if (craftVillagerClass == null) {
                    Griswold.log.severe("ERROR: CANNOT FIND CLASS CraftVillager");
                    return;
                }
                if (!craftVillagerClass.isInstance(doppelganger)) return; // are you even villager?
                if (this.entity.equals(doppelganger)) return; // prevent suiciding
                if (doppelganger.getName().equals(this.name)) doppelganger.remove(); // 100% DUPLICATE
            });

        if (Griswold.debug) {
            Griswold.log.info(String.format(Lang.repairman_spawn, this.entity.getEntityId(), loc.getX(), loc.getY(), loc.getZ()));
        }
        spawned = true;
    }

    public void despawn() {
        if(!spawned) return;
        Griswold.npcChunks.keySet().forEach((rep) -> {
            if (rep.equals(this)) rep.getEntity().remove();
        });
        spawned = false;
    }

    public void toggleName(boolean toggle) {
        LivingEntity entity = (LivingEntity) this.entity;
        entity.setCustomNameVisible(toggle);
    }

    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
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

    public static Repairer getByName(final String name) throws IllegalArgumentException {
        for(Repairer r : Griswold.npcChunks.keySet()) if(r.getName().equals(name)) return r;
        FileConfiguration config = Griswold.config;
        Set<String> repairmen = config.getConfigurationSection("repairmen").getKeys(false);
        if (!repairmen.contains(name)) throw new IllegalArgumentException("Repairman with name " + name + " not found");


        Location loc = new Location(Bukkit.getWorld(config.getString("repairmen." + name + ".world")),
                config.getDouble("repairmen." + name + ".X"),
                config.getDouble("repairmen." + name + ".Y"),
                config.getDouble("repairmen." + name + ".Z"));
        String sound = config.getString("repairmen." + name + ".sound");
        String type = config.getString("repairmen." + name + ".type");
        double cost = config.getDouble("repairmen." + name + ".cost");

        return new Repairer(name, loc, sound, type, cost);
    }

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
