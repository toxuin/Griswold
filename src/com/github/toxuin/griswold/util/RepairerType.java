package com.github.toxuin.griswold.util;

import com.github.toxuin.griswold.Griswold;

import java.util.Arrays;

public enum RepairerType {

    TOOLS, ARMOR, BOTH, ENCHANT, ALL;

    public static RepairerType fromString(String type) {
        for (RepairerType t : RepairerType.values()) {
            if (t.toString().equalsIgnoreCase(type)) return t; // IGNORE CASE
        }
        Griswold.log.severe("ERROR ERROR ERROR! Type " + type + " not found. WTF?");
        return ALL;
    }

    public static boolean present(String type) {
        return Arrays.stream(RepairerType.values()).anyMatch(t -> t.toString().equalsIgnoreCase(type));
    }
}
