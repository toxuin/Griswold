package com.github.toxuin.griswold.adapters.citizens;

import com.github.toxuin.griswold.util.RepairerType;
import net.citizensnpcs.api.trait.TraitName;

@TraitName(CitizensAdapter.ALL_TRAIT_NAME)
public class GriswoldAllTrait extends GriswoldTrait {
    public GriswoldAllTrait() {
        super(CitizensAdapter.ALL_TRAIT_NAME);
    }
    @Override
    public RepairerType getType() {
        return RepairerType.ALL;
    }
}
