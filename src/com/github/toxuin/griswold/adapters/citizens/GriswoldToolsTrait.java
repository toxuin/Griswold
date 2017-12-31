package com.github.toxuin.griswold.adapters.citizens;

import com.github.toxuin.griswold.util.RepairerType;

public class GriswoldToolsTrait extends GriswoldTrait {
    public GriswoldToolsTrait() {
        super(CitizensAdapter.TOOLS_REPAIR_TRAIT_NAME);
    }

    @Override
    public RepairerType getType() {
        return RepairerType.TOOLS;
    }
}
