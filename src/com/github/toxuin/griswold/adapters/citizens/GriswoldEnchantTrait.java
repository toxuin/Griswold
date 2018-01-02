package com.github.toxuin.griswold.adapters.citizens;

import com.github.toxuin.griswold.util.RepairerType;
import net.citizensnpcs.api.trait.TraitName;

@TraitName(CitizensAdapter.ENCHANT_TRAIT_NAME)
public class GriswoldEnchantTrait extends GriswoldTrait {
    public GriswoldEnchantTrait() {
        super(CitizensAdapter.ENCHANT_TRAIT_NAME);
    }
    @Override
    public RepairerType getType() {
        return RepairerType.ENCHANT;
    }
}