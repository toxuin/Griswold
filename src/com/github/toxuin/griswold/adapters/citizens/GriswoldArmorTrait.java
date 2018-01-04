package com.github.toxuin.griswold.adapters.citizens;

import com.github.toxuin.griswold.util.RepairerType;
import net.citizensnpcs.api.trait.TraitName;

@TraitName(CitizensAdapter.ARMOR_REPAIR_TRAIT_NAME)
public class GriswoldArmorTrait extends GriswoldTrait {
    public GriswoldArmorTrait() {
        super(CitizensAdapter.ARMOR_REPAIR_TRAIT_NAME);
    }
    @Override
    public RepairerType getType() {
        return RepairerType.ARMOR;
    }
}