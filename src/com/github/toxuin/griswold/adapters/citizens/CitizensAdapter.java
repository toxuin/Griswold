package com.github.toxuin.griswold.adapters.citizens;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.TraitInfo;

public class CitizensAdapter {
    static final String TOOLS_REPAIR_TRAIT_NAME = "griswold_tools";
    static final String ARMOR_REPAIR_TRAIT_NAME = "griswold_armor";
    static final String BOTH_REPAIR_TRAIT_NAME = "griswold_both";
    static final String ENCHANT_TRAIT_NAME = "griswold_enchant";
    static final String ALL_TRAIT_NAME = "griswold_all";

    private TraitInfo toolsRepairerTrait; // TOOLS
    private TraitInfo armorRepairerTrait; // ARMOR
    private TraitInfo anyRepairerTrait;   // BOTH
    private TraitInfo enchanterTrait;     // ENCHANT
    private TraitInfo everythingTrait;    // ALL

    public CitizensAdapter() {
        toolsRepairerTrait = TraitInfo.create(GriswoldToolsTrait.class).withName(TOOLS_REPAIR_TRAIT_NAME);
        armorRepairerTrait = TraitInfo.create(GriswoldArmorTrait.class).withName(ARMOR_REPAIR_TRAIT_NAME);
        anyRepairerTrait = TraitInfo.create(GriswoldBothTrait.class).withName(BOTH_REPAIR_TRAIT_NAME);
        enchanterTrait = TraitInfo.create(GriswoldEnchantTrait.class).withName(ENCHANT_TRAIT_NAME);
        everythingTrait = TraitInfo.create(GriswoldAllTrait.class).withName(ALL_TRAIT_NAME);

        CitizensAPI.getTraitFactory().registerTrait(toolsRepairerTrait);
        CitizensAPI.getTraitFactory().registerTrait(armorRepairerTrait);
        CitizensAPI.getTraitFactory().registerTrait(anyRepairerTrait);
        CitizensAPI.getTraitFactory().registerTrait(enchanterTrait);
        CitizensAPI.getTraitFactory().registerTrait(everythingTrait);
    }

    public void deregisterTraits() {
        CitizensAPI.getTraitFactory().deregisterTrait(toolsRepairerTrait);
        CitizensAPI.getTraitFactory().deregisterTrait(armorRepairerTrait);
        CitizensAPI.getTraitFactory().deregisterTrait(anyRepairerTrait);
        CitizensAPI.getTraitFactory().deregisterTrait(enchanterTrait);
        CitizensAPI.getTraitFactory().deregisterTrait(everythingTrait);
    }

}
