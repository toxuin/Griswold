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

public class GriswoldNPC extends Repairer {

    private Entity entity;
    private Random rnd = new Random();
    private boolean spawned;

    private final Class entityInsentient = ClassProxy.getClass("EntityInsentient");
    private final Class entityHuman = ClassProxy.getClass("EntityHuman");
    private final Class pathfinderGoalSelector = ClassProxy.getClass("PathfinderGoalSelector");
    private final Class pathfinderGoalLookAtPlayer = ClassProxy.getClass("PathfinderGoalLookAtPlayer");
    private final Class pathfinderGoalRandomLookaround = ClassProxy.getClass("PathfinderGoalRandomLookaround");
    private final Class pathfinderGoal = ClassProxy.getClass("PathfinderGoal");
    private final Class craftWorld = ClassProxy.getClass("CraftWorld");
    private final Class craftEntity = ClassProxy.getClass("entity.CraftEntity");
    private final Class craftVillager = ClassProxy.getClass("entity.CraftVillager");
    private final Class entityClass = ClassProxy.getClass("Entity");

    GriswoldNPC(String name, Location loc, String sound, String type, double cost) {
        super(name, loc, sound, type, cost);
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
            Constructor<?> lookAtPlayerConstructor =
                    pathfinderGoalLookAtPlayer.getConstructor(entityInsentient, Class.class, float.class, float.class);
            Constructor<?> randomLookAroundConstructor =
                    pathfinderGoalRandomLookaround.getConstructor(entityInsentient);

            setGoal.invoke(goals, 1, lookAtPlayerConstructor.newInstance(villager, entityHuman, 12.0F, 1.0F));
            setGoal.invoke(goals, 2, randomLookAroundConstructor.newInstance(villager));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void haggle() {
        if (craftEntity == null) return;
        if (this.sound != null && !this.sound.isEmpty() && !this.sound.equals("mute") &&
            craftEntity.isInstance(this.entity)) {
            try {
                Sound snd = Sound.valueOf(sound);
                this.entity.getWorld().playSound(entity.getLocation(), snd, 1.2f,
                        1.6F + (this.rnd.nextFloat() - this.rnd.nextFloat()) * 0.4F);
            } catch (IllegalArgumentException e) {
                Griswold.log.info("NPC " + this.getName() + " has invalid sound "
                                  + sound + "! Disabling sound for it...");
                sound = null;
            }
        }
    }

    void loadChunk() {
        getLocation().getWorld().loadChunk(getLocation().getChunk());
    }

    void spawn() {
        if (isSpawned()) return;
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
        if (Griswold.findDuplicates) {
            if (this.entityClass == null) return; // YOU'RE WEIRD
            Class craftVillagerClass = ClassProxy.getClass("entity.CraftVillager");
            if (craftVillagerClass == null) {
                Griswold.log.severe("ERROR: CANNOT FIND CLASS CraftVillager");
                return;
            }

            Arrays.stream(getLocation().getChunk().getEntities())
                    .filter(doppelganger ->
                            doppelganger.getLocation().distance(getLocation()) <= Griswold.duplicateFinderRadius)
                    .filter(craftVillagerClass::isInstance) // are you even villager?
                    .filter(doppelganger -> !this.entity.equals(doppelganger)) // prevent suiciding
                    .filter(doppelganger -> doppelganger.getName().equals(this.name)) // 100% DUPLICATE
                    .forEach(Entity::remove);
        }

        if (Griswold.debug) {
            Griswold.log.info(String
                    .format(Lang.repairman_spawn, this.entity.getEntityId(), loc.getX(), loc.getY(), loc.getZ()));
        }
        setSpawned(true);
    }

    void despawn() {
        if (!this.isSpawned()) return;
        Griswold.npcChunks.keySet().stream().filter(rep -> rep.equals(this)).forEach(rep -> rep.getEntity().remove());
        setSpawned(false);
    }

    @Override
    public boolean isSpawned() {
        return spawned;
    }

    @Override
    public void setSpawned(boolean newState) {
        this.spawned = newState;
    }

    public void toggleName(boolean toggle) {
        LivingEntity entity = (LivingEntity) this.entity;
        entity.setCustomNameVisible(toggle);
    }

    @Override
    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public static GriswoldNPC getByName(final String name) throws IllegalArgumentException {
        for (GriswoldNPC npc : Griswold.npcChunks.keySet()) if (npc.getName().equals(name)) return npc;

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

        return new GriswoldNPC(name, loc, sound, type, cost);
    }
}
