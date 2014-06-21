package com.github.toxuin.griswold;

import org.bukkit.entity.Entity;
import org.bukkit.Location;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Repairer {
    public Entity entity;
    public String name = "Repairman";
    public Location loc;
    public String type = "all";
    public double cost = 1;
    public String sound = "mob.villager.haggle";
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

    @SuppressWarnings("unchecked")
    public void haggle() {
        if (this.sound != null && !this.sound.isEmpty() && !this.sound.equals("mute") && craftEntity.isInstance(this.entity)) {
            try {
                Method worldGetHandle = craftWorld.getMethod("getHandle");
                Object wserver = worldGetHandle.invoke(craftWorld.cast(this.entity.getLocation().getWorld()));
                Method entityGetHandle = craftEntity.getMethod("getHandle");
                Object entity = entityGetHandle.invoke(craftEntity.cast(this.entity));
                Method makeSound = wserver.getClass().getMethod("makeSound", entityClass, String.class, float.class, float.class);
                makeSound.invoke(wserver, entity, this.sound, 100f, 1.6F + (this.rnd.nextFloat() - this.rnd.nextFloat()) * 0.4F);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // DEBUGGING METHODS
    static void listMethods(Class className) {
        for (Method m : className.getDeclaredMethods()) {
            Griswold.log.info(m.getName() + ", ARGS: " + Arrays.toString(m.getParameterTypes()));
        }
    }
    static void listConstructors(Class className) {
        for (Constructor c : className.getDeclaredConstructors()) {
            Griswold.log.info("Constructor for " + className.getName() + ": " + Arrays.toString(c.getParameterTypes()));
        }
    }
    static void listFields(Class className) {
        for (Field f : className.getFields()) {
            Griswold.log.info(f.getName() + ": " + f.getType().getCanonicalName());
        }
    }
}
