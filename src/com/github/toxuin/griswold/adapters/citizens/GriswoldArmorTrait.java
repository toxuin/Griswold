package com.github.toxuin.griswold.adapters.citizens;

import com.github.toxuin.griswold.util.RepairerType;

public class GriswoldArmorTrait extends GriswoldTrait {
    public GriswoldArmorTrait() {
        super(CitizensAdapter.ARMOR_REPAIR_TRAIT_NAME);
    }
    @Override
    public RepairerType getType() {
        return RepairerType.ARMOR;
    }
}