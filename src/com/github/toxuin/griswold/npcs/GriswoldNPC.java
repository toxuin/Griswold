package com.github.toxuin.griswold.npcs;

import com.github.toxuin.griswold.Griswold;
import com.github.toxuin.griswold.util.ConfigManager;
import com.github.toxuin.griswold.util.Lang;
import com.github.toxuin.griswold.events.PlayerInteractGriswoldNPCEvent;
import com.github.toxuin.griswold.professions.Profession;
import com.github.toxuin.griswold.util.ClassProxy;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.lang.reflect.*;
import java.util.*;

public class GriswoldNPC {
    public Class<LivingEntity> blueprintEntity;
    public LivingEntity entity;
    public String name = "Repairman";
    public Location loc;
    public Profession profession = null;
    Sound sound = Sound.VILLAGER_HAGGLE;
    private Random rnd = new Random();

    final Class entityInsentientClass = ClassProxy.getClass("EntityInsentient");
    final Class entityHumanClass = ClassProxy.getClass("EntityHuman");
    final Class pathfinderGoalSelectorClass = ClassProxy.getClass("PathfinderGoalSelector");
    final Class pathfinderGoalLookAtPlayerClass = ClassProxy.getClass("PathfinderGoalLookAtPlayer");
    final Class pathfinderGoalRandomLookaroundClass = ClassProxy.getClass("PathfinderGoalRandomLookaround");
    final Class pathfinderGoalClass = ClassProxy.getClass("PathfinderGoal");
    final Class craftLivingEntityClass = ClassProxy.getClass("entity.CraftLivingEntity");

    public GriswoldNPC(String name, Location location, Class blueprintEntity) {
        this.name = name;
        this.loc = location;
        this.blueprintEntity = blueprintEntity;

        this.loc.getWorld().loadChunk(this.loc.getChunk());

        LivingEntity npcEntity = loc.getWorld().spawn(loc, this.blueprintEntity);

        npcEntity.setCustomName(this.name);
        entity = npcEntity;
        setNameVisible(ConfigManager.isNameVisible(name));
        this.overwriteAI();

        if (ConfigManager.debug) {
            Griswold.log.info(String.format(Lang.repairman_spawn, this.entity.getUniqueId(), this.loc.getX(), this.loc.getY(), this.loc.getZ()));
        }
    }

    public GriswoldNPC setProfession(Profession profession) {
        this.profession = profession;
        this.profession.setNpc(this);
        return this;
    }

    public void interact(PlayerInteractGriswoldNPCEvent event) {
        if (profession == null) return;
        String[] messages = this.profession.use(event).split("\n");
        for (String message : messages) {
            if (!message.equals("")) event.getPlayer().sendMessage(message);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void overwriteAI() {
        try {
            Method getHandle = craftLivingEntityClass.getMethod("getHandle");
            Object livingEntity = getHandle.invoke(craftLivingEntityClass.cast(entity));
            Field goalsField = entityInsentientClass.getDeclaredField("goalSelector");
            goalsField.setAccessible(true);
            Object goals = pathfinderGoalSelectorClass.cast(goalsField.get(livingEntity));
            Field listField = pathfinderGoalSelectorClass.getDeclaredField("b");
            listField.setAccessible(true);
            List list = (List) listField.get(goals);
            list.clear();

            Method setGoal = pathfinderGoalSelectorClass.getMethod("a", new Class[]{int.class, pathfinderGoalClass});
            Constructor<?> lookAtPlayerConstructor = pathfinderGoalLookAtPlayerClass.getConstructor(entityInsentientClass, Class.class, float.class, float.class);
            Constructor<?> randomLookAroundConstructor = pathfinderGoalRandomLookaroundClass.getConstructor(entityInsentientClass);

            setGoal.invoke(goals, 1, lookAtPlayerConstructor.newInstance(livingEntity, entityHumanClass, 12.0F, 1.0F));
            setGoal.invoke(goals, 2, randomLookAroundConstructor.newInstance(livingEntity));

            if (this.entity instanceof Skeleton) {
                Skeleton s = (Skeleton) this.entity;
                s.setSkeletonType(Skeleton.SkeletonType.WITHER);
                s.getEquipment().setHelmet(new ItemStack(Material.COBBLESTONE));
            } else if (this.entity instanceof Spider) {
                // DO NOT RUN AWAY IN DAY
                // NO ATTACK IN NIGHT
            } else if (this.entity instanceof Ocelot) {
                Ocelot ocelot = (Ocelot) this.entity;
                ocelot.setCatType(Ocelot.Type.SIAMESE_CAT);
            } else if (this.entity instanceof Zombie) {
                Zombie z = (Zombie) this.entity;
                z.setVillager(true);
            } else if (this.entity instanceof Horse) {
                Horse h = (Horse) this.entity;
                h.setCarryingChest(true);
                h.setColor(Horse.Color.CREAMY);
                h.setStyle(Horse.Style.WHITEFIELD);
                h.setVariant(Horse.Variant.UNDEAD_HORSE);
            } else if (this.entity instanceof Enderman) {
                Enderman e = (Enderman) this.entity;
                e.setCarriedMaterial(new MaterialData(Material.COBBLESTONE));
                e.setCanPickupItems(false);
            } else if (this.entity instanceof Wolf) {
                Wolf w = (Wolf) this.entity;
                w.setAngry(false);
                w.setCollarColor(DyeColor.CYAN);
                w.setSitting(true);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        /*
        for (BukkitTask taks : runningTasks) {
            taks.cancel();
        }
        */
        super.finalize();
    }

    public void remove() {
        this.entity.remove();
    }

    public void setNameVisible(boolean nameVisible) {
        entity.setCustomNameVisible(nameVisible);
        ConfigManager.setNameVisible(this.name, nameVisible);
    }

    public boolean isNameVisible() {
        return entity.isCustomNameVisible();
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
