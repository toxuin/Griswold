package com.github.toxuin.griswold.adapters.citizens;

import com.github.toxuin.griswold.util.RepairerType;
import net.citizensnpcs.api.trait.TraitName;

@TraitName(CitizensAdapter.TOOLS_REPAIR_TRAIT_NAME)
public class GriswoldToolsTrait extends GriswoldTrait {
    public GriswoldToolsTrait() {
        super(CitizensAdapter.TOOLS_REPAIR_TRAIT_NAME);
    }

    @Override
    public RepairerType getType() {
        return RepairerType.TOOLS;
    }
}
