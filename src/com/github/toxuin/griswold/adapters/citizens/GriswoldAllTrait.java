package com.github.toxuin.griswold.adapters.citizens;

import com.github.toxuin.griswold.util.RepairerType;

public class GriswoldAllTrait extends GriswoldTrait {
    public GriswoldAllTrait() {
        super(CitizensAdapter.ALL_TRAIT_NAME);
    }
    @Override
    public RepairerType getType() {
        return RepairerType.ALL;
    }
}
