package com.github.toxuin.griswold.adapters.citizens;

import com.github.toxuin.griswold.Repairer;
import com.github.toxuin.griswold.util.RepairerType;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Entity;

public class CitizensFakeNPC extends Repairer {
    private NPC citizensNPC;

    public CitizensFakeNPC(NPC npc, RepairerType type, double citizensCost) {
        super(npc.getName(), npc.getStoredLocation(), Repairer.getDefaultSound(), type.toString(), citizensCost);
        this.citizensNPC = npc;
    }

    public NPC getCitizensNpc() {
        return citizensNPC;
    }

    @Override
    public double getCost() {
        this.citizensNPC.getTrait(GriswoldTrait.class);
        GriswoldTrait trait = this.citizensNPC.getTrait(GriswoldTrait.class);
        if (trait != null) return trait.getCost();
        return 1;
    }

    @Override
    public void setCost(double cost) {
        this.citizensNPC.getTrait(GriswoldTrait.class);
        GriswoldTrait trait = this.citizensNPC.getTrait(GriswoldTrait.class);
        if (trait != null) trait.setCost(cost);
    }

    void setCitizensNPC(NPC npc) {
        this.citizensNPC = npc;
    }

    @Override
    public void haggle() {
        // BEEP BEEP MOTHERFUCKER
    }

    @Override
    public boolean isSpawned() {
        return citizensNPC.isSpawned();
    }

    @Override
    public void setSpawned(boolean newState) {
        // YOU HAVE NO POWER HERE
    }

    @Override
    public Entity getEntity() {
        return citizensNPC.getEntity();
    }
}
