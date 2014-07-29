package com.github.toxuin.griswold.professions;

import com.github.toxuin.griswold.events.PlayerInteractGriswoldNPCEvent;
import com.github.toxuin.griswold.npcs.GriswoldNPC;
import com.github.toxuin.griswold.util.ConfigManager;
import org.bukkit.entity.LivingEntity;

import javax.annotation.CheckForNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class Profession {
    public abstract String use(PlayerInteractGriswoldNPCEvent event);
    public abstract void setNpc(GriswoldNPC npc);
    public abstract String getName();
    public abstract void loadConfig();
    public abstract void saveConfig();

    public static Map<String, Class> knownProfessions = new HashMap<String, Class>();
    static {
        knownProfessions.put("Blacksmith", Blacksmith.class);
        knownProfessions.put("Shopkeeper", Shopkeeper.class);
    }

    @CheckForNull
    public static Profession getByName(String name) {
        try {
            return (Profession) knownProfessions.get(name).newInstance();
        } catch (Exception exception) {
            if (ConfigManager.debug) exception.printStackTrace();
            return null;
        }
    }
}
