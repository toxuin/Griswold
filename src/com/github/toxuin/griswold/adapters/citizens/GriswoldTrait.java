package com.github.toxuin.griswold.adapters.citizens;

import com.github.toxuin.griswold.Griswold;
import com.github.toxuin.griswold.Interactor;
import com.github.toxuin.griswold.util.RepairerType;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;

public abstract class GriswoldTrait extends Trait {
    private Griswold griswold;
    private final Interactor interactor;
    
    @Persist private double cost = 1d;

    public GriswoldTrait(String name) {
        super(name);
        griswold = (Griswold) Bukkit.getServer().getPluginManager().getPlugin(Griswold.PLUGIN_NAME);
        interactor = griswold.getInteractor();
    }

    @EventHandler
    public void rightCliked(NPCRightClickEvent event) {
        NPC npc = event.getNPC();
        if (!npc.equals(this.getNPC())) return;

        CitizensFakeNPC citizensFakeNPC = new CitizensFakeNPC(npc, getType(), getCost());
        interactor.interact(event.getClicker(), citizensFakeNPC);
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public abstract RepairerType getType();
}
