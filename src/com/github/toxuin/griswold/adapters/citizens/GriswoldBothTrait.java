package com.github.toxuin.griswold.adapters.citizens;

import com.github.toxuin.griswold.util.RepairerType;

public class GriswoldBothTrait extends GriswoldTrait {
    public GriswoldBothTrait() {
        super(CitizensAdapter.BOTH_REPAIR_TRAIT_NAME);
    }
    @Override
    public RepairerType getType() {
        return RepairerType.BOTH;
    }
}
