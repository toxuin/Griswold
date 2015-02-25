package com.github.toxuin.griswold.util;

import com.github.toxuin.griswold.Griswold;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

public class ClassProxy {
    // THIS WHOLE FILE IS ABSOLUTELY GAY
    // THANK FOR VERSIONED NMS PACKAGES, EvilSeph
    // THERE ARE PEOPLE WHO HATE YOU.

    // RELATIVE TO net.minecraft.server.vX_X_RX.
    public static Class getClass(String className) {
        if (className.equals("WeightedRandomEnchant") && Griswold.apiVersion.contains("v1_7")) {
            className = "EnchantmentInstance";
        }

        try {
            return Class.forName("net.minecraft.server." + Griswold.apiVersion + "." + className);
        } catch (ClassNotFoundException e) {
            try {
                return Class.forName("org.bukkit.craftbukkit." + Griswold.apiVersion + "." + className);
            } catch (ClassNotFoundException e1) {
                return null;
            }
        }
    }


    // DEBUGGING METHODS
    public static void listDeclaredMethods(Class className) {
        for (Method m : className.getDeclaredMethods()) {
            Griswold.log.info(m.getName() + ", ARGS: " + Arrays.toString(m.getParameterTypes()));
        }
    }

    public static void listMethods(Class className) {
        for (Method m : className.getMethods()) {
            Griswold.log.info(m.getName() + ", ARGS: " + Arrays.toString(m.getParameterTypes()));
        }
    }

    public static void listDeclaredConstructors(Class className) {
        for (Constructor c : className.getDeclaredConstructors()) {
            Griswold.log.info("Constructor for " + className.getName() + ": " + Arrays.toString(c.getParameterTypes()));
        }
    }

    public static void listConstructors(Class className) {
        for (Constructor c : className.getConstructors()) {
            Griswold.log.info("Constructor for " + className.getName() + ": " + Arrays.toString(c.getParameterTypes()));
        }
    }

    public static void listDeclaredFields(Class className) {
        for (Field f : className.getDeclaredFields()) {
            Griswold.log.info(f.getName() + ": " + f.getType().getCanonicalName());
        }
    }

    public static void listFields(Class className) {
        for (Field f : className.getFields()) {
            Griswold.log.info(f.getName() + ": " + f.getType().getCanonicalName());
        }
    }
}
