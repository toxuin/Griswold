package com.github.toxuin.griswold.professions;

import com.github.toxuin.griswold.events.PlayerInteractGriswoldNPCEvent;
import com.github.toxuin.griswold.npcs.GriswoldNPC;
import org.bukkit.entity.LivingEntity;

public interface Profession {
    public abstract String use(PlayerInteractGriswoldNPCEvent event);
    public void setNpc(GriswoldNPC npc);
}
