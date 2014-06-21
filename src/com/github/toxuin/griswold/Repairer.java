package com.github.toxuin.griswold;

import org.bukkit.entity.Entity;
import org.bukkit.Location;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;

import net.minecraft.server.v1_7_R3.EntityVillager;
import net.minecraft.server.v1_7_R3.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_7_R3.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_7_R3.PathfinderGoalSelector;
import org.bukkit.craftbukkit.v1_7_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_7_R3.entity.CraftVillager;

public class Repairer {
    public Entity entity;
    public String name = "Repairman";
    public Location loc;
    public String type = "all";
    public double cost = 1;
    public String sound = "mob.villager.haggle";
    private Random rnd = new Random();

    Class entityVillager = ClassProxy.getClass("EntityVillager");
    Class entityInsentient = ClassProxy.getClass("EntityInsentient");
    Class entityHuman = ClassProxy.getClass("EntityHuman");
    Class pathfinderGoalSelector = ClassProxy.getClass("PathfinderGoalSelector");
    Class pathfinderGoalLookAtPlayer = ClassProxy.getClass("PathfinderGoalLookAtPlayer");
    Class pathfinderGoalRandomLookaround = ClassProxy.getClass("PathfinderGoalRandomLookaround");
    Class craftWorld = ClassProxy.getClass("CraftWorld");
    Class craftEntity = ClassProxy.getClass("entity.CraftEntity");
    Class craftVillager = ClassProxy.getClass("entity.CraftVillager");

    public void overwriteAI() {
        try {
            EntityVillager villager = ((CraftVillager)entity).getHandle();
            Field goalsField = entityInsentient.getDeclaredField("goalSelector");
            goalsField.setAccessible(true);
            PathfinderGoalSelector goals = (PathfinderGoalSelector) goalsField.get(villager);
            Field listField = pathfinderGoalSelector.getDeclaredField("b");
            listField.setAccessible(true);
            @SuppressWarnings("rawtypes")
            List list = (List) listField.get(goals);
            list.clear();
            goals.a(1, new PathfinderGoalLookAtPlayer(villager, entityHuman, 12.0F, 1.0F));
            goals.a(2, new PathfinderGoalRandomLookaround(villager));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void haggle() {
        if (this.sound != null && !this.sound.isEmpty() && !this.sound.equals("mute") && this.entity instanceof CraftEntity) {
            ((CraftWorld) this.entity.getLocation().getWorld()).getHandle().makeSound(((CraftEntity) this.entity).getHandle(), this.sound, 100f, 1.6F + (this.rnd.nextFloat() - this.rnd.nextFloat()) * 0.4F);
        }
    }
}
