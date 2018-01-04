package com.github.toxuin.griswold.adapters.citizens;

import com.github.toxuin.griswold.util.RepairerType;
import net.citizensnpcs.api.trait.TraitName;

@TraitName(CitizensAdapter.BOTH_REPAIR_TRAIT_NAME)
public class GriswoldBothTrait extends GriswoldTrait {
    public GriswoldBothTrait() {
        super(CitizensAdapter.BOTH_REPAIR_TRAIT_NAME);
    }
    @Override
    public RepairerType getType() {
        return RepairerType.BOTH;
    }
}
