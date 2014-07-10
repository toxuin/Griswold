package com.github.toxuin.griswold.npcs;

import com.github.toxuin.griswold.Griswold;
import com.github.toxuin.griswold.Lang;
import com.github.toxuin.griswold.professions.Profession;
import com.github.toxuin.griswold.util.ClassProxy;
import com.github.toxuin.griswold.util.Interaction;
import org.bukkit.Sound;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class GriswoldNPC {
    public Class<LivingEntity> blueprintEntity;
    public LivingEntity entity;
    public String name = "Repairman";
    public Location loc;
    Profession profession;
    Sound sound = Sound.VILLAGER_HAGGLE;
    private Random rnd = new Random();

    final Set<Interaction> interactions = new HashSet<Interaction>();

    final Class craftEntity = ClassProxy.getClass("entity.CraftEntity");
    final Class entityInsentient = ClassProxy.getClass("EntityInsentient");
    final Class entityHuman = ClassProxy.getClass("EntityHuman");
    final Class pathfinderGoalSelector = ClassProxy.getClass("PathfinderGoalSelector");
    final Class pathfinderGoalLookAtPlayer = ClassProxy.getClass("PathfinderGoalLookAtPlayer");
    final Class pathfinderGoalRandomLookaround = ClassProxy.getClass("PathfinderGoalRandomLookaround");
    final Class pathfinderGoal = ClassProxy.getClass("PathfinderGoal");
    final Class craftWorld = ClassProxy.getClass("CraftWorld");
    final Class craftLivingEntity = ClassProxy.getClass("entity.CraftLivingEntity");
    final Class entityClass = ClassProxy.getClass("Entity");

    public GriswoldNPC(String name, Location location, Profession profession, Class blueprintEntity) {
        this.name = name;
        this.loc = location;
        this.profession = profession;
        this.blueprintEntity = blueprintEntity;

        this.loc.getWorld().loadChunk(this.loc.getChunk());
        LivingEntity repairman = loc.getWorld().spawn(loc, this.blueprintEntity);

        repairman.setCustomNameVisible(Griswold.namesVisible);
        repairman.setCustomName(this.name);
        entity = repairman;

        this.overwriteAI();


        if (Griswold.debug) {
            Griswold.log.info(String.format(Lang.repairman_spawn, this.entity.getUniqueId(), this.loc.getX(), this.loc.getY(), this.loc.getZ()));
        }

        //if (!blueprintEntity.equals(Villager.class)) return;

        /*if (this.type.equals(NPCType.ENCHANT) || this.type.equals(NPCType.DISENCHANT)) {
            ((Villager) entity).setProfession(Villager.Profession.LIBRARIAN);
        } else {
            ((Villager) entity).setProfession(Villager.Profession.BLACKSMITH);
        } */
    }

    public void interact(Interaction interaction) {

    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void overwriteAI() {
        try {
            Method getHandle = craftLivingEntity.getMethod("getHandle");
            Object villager = getHandle.invoke(craftLivingEntity.cast(entity));
            Field goalsField = entityInsentient.getDeclaredField("goalSelector"); // TODO: CAN BE ANY LivingEntity
            goalsField.setAccessible(true);
            Object goals = pathfinderGoalSelector.cast(goalsField.get(villager));
            Field listField = pathfinderGoalSelector.getDeclaredField("b");
            listField.setAccessible(true);
            List list = (List) listField.get(goals);
            list.clear();

            Method setGoal = pathfinderGoalSelector.getMethod("a", new Class[] { int.class, pathfinderGoal });
            Constructor<?> lookAtPlayerConstructor = pathfinderGoalLookAtPlayer.getConstructor(entityInsentient, Class.class, float.class, float.class);
            Constructor<?> randomLookAroundConstructor = pathfinderGoalRandomLookaround.getConstructor(entityInsentient);

            setGoal.invoke(goals, 1, lookAtPlayerConstructor.newInstance(villager, entityHuman, 12.0F, 1.0F));
            setGoal.invoke(goals, 2, randomLookAroundConstructor.newInstance(villager));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void remove() {
        this.entity.remove();
    }

    public void setNameVisible(boolean nameVisible) {
        entity.setCustomNameVisible(nameVisible);
    }

    public void makeSound() {
        if (this.sound != null) {
            this.entity.getWorld().playSound(entity.getLocation(), sound, 1.2f, 1.6F + (this.rnd.nextFloat() - this.rnd.nextFloat()) * 0.4F);
        }
    }

    public void setSound(String newSound) {
        if (newSound.equals("mute")) {
            sound = null;
            return;
        }
        try {
            sound = Sound.valueOf(newSound);
        } catch (IllegalArgumentException e) {
            Griswold.log.info("NPC " + name + " has invalid sound! Disabling sound for it...");
            sound = null;
        }
    }
}
