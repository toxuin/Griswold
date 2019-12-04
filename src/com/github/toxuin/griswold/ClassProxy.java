package com.github.toxuin.griswold;

import com.sun.istack.internal.NotNull;

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
    @NotNull
    static Class<?> getClass(String className) {
        // class name changed after 1.7
        if (className.equals("EnchantmentInstance")
            && (Griswold.apiVersion.getMajor() >= 1 && Griswold.apiVersion.getMinor() >= 8)) {
            className = "WeightedRandomEnchant";
        }

        try {
            return Class.forName("net.minecraft.server." + Griswold.apiVersion.getNMSVersion() + "." + className);
        } catch (ClassNotFoundException e) {
            try {
                return Class.forName("org.bukkit.craftbukkit." + Griswold.apiVersion.getNMSVersion() + "." + className);
            } catch (ClassNotFoundException e1) {
                Griswold.log.severe("CLASS " + className + " NOT FOUND. "
                                    + "Raise an issue on GitHub or deal with it.");
                throw new IllegalStateException();
            }
        }
    }

    static boolean classExists(String name) {
        try {
            getClass(name);
            return true;
        } catch (IllegalStateException ignored) {
            return false;
        }
    }

    static boolean checkObjectHasMethod(Object obj, String targetMethod) {
        Class<?> className = obj.getClass();
        Method[] methods = className.getDeclaredMethods();
        for (Method m : methods) {
            if (m.getName().equals(targetMethod)) return true;
        }
        return false;
    }

    // DEBUGGING METHODS
    @SuppressWarnings("unused")
    static void listMethods(Class<?> className) {
        Method[] methods = className.getDeclaredMethods();
        for (Method m : methods) {
            log.info(m.getName() + ", ARGS: " + Arrays.toString(m.getParameterTypes()));
        }
    }

    @SuppressWarnings("unused")
    static void listConstructors(Class<?> className) {
        Constructor<?>[] constructors = className.getDeclaredConstructors();
        for (Constructor<?> c : constructors) {
            log.info("Constructor for " + className.getName() + ": " + Arrays.toString(c.getParameterTypes()));
        }
    }

    @SuppressWarnings("unused")
    static void listFields(Class<?> className) {
        Field[] fields = className.getFields();
        for (Field f : fields) {
            log.info(f.getName() + ": " + f.getType().getCanonicalName());
        }
    }
}
