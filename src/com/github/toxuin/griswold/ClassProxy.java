package com.github.toxuin.griswold;


import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

import static com.github.toxuin.griswold.Griswold.log;

class ClassProxy {
    // THIS WHOLE FILE IS ABSOLUTELY GAY
    // THANK FOR VERSIONED NMS PACKAGES, EvilSeph
    // THERE ARE PEOPLE WHO HATE YOU.

    // RELATIVE TO net.minecraft.server.vX_X_RX.
    static Class getClass(String className) {
        // class name changed after 1.7
        if (className.equals("EnchantmentInstance") && (Griswold.apiVersion.getMajor() >= 1 && Griswold.apiVersion.getMinor() >= 8)) {
            className = "WeightedRandomEnchant";
        }

        try {
            return Class.forName("net.minecraft.server." + Griswold.apiVersion.getNMSVersion() + "." + className);
        } catch (ClassNotFoundException e) {
            try {
                return Class.forName("org.bukkit.craftbukkit." + Griswold.apiVersion.getNMSVersion() + "." + className);
            } catch (ClassNotFoundException e1) {
                return null;
            }
        }
    }

    // DEBUGGING METHODS
    @SuppressWarnings("unused")
    static void listMethods(Class className) {
        for (Method m : className.getDeclaredMethods()) {
            log.info(m.getName() + ", ARGS: " + Arrays.toString(m.getParameterTypes()));
        }
    }

    @SuppressWarnings("unused")
    static void listConstructors(Class className) {
        for (Constructor c : className.getDeclaredConstructors()) {
            log.info("Constructor for " + className.getName() + ": " + Arrays.toString(c.getParameterTypes()));
        }
    }

    @SuppressWarnings("unused")
    static void listFields(Class className) {
        for (Field f : className.getFields()) {
            log.info(f.getName() + ": " + f.getType().getCanonicalName());
        }
    }
}
