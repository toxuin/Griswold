package com.github.toxuin.griswold;

public class ClassProxy {
    // THIS WHOLE FILE IS ABSOLUTELY GAY
    // THANK FOR VERSIONED NMS PACKAGES, EvilSeph
    // THERE ARE PEOPLE WHO HATE YOU.

    // RELATIVE TO net.minecraft.server.vX_X_RX.
    public static Class getClass(String className) {
        try {
            return Class.forName("net.minecraft.server.v1_7_R3." + className);
        } catch (ClassNotFoundException e) {
            try {
                return Class.forName("net.minecraft.server.v1_7_R2." + className);
            } catch (ClassNotFoundException e1) {
                try {
                    return Class.forName("net.minecraft.server.v1_7_R1." + className);
                } catch (ClassNotFoundException e2) {
                    return getOBCClass(className);
                    /// DO NOTHING SINCE ALL CLASS CHECKS ARE DONE ON PLUGIN LOADING
                }
            }
        }
    }

    // RELATIVE TO org.bukkit.craftbukkit.vX_X_RX.
    public static Class getOBCClass(String className) {
        try {
            return Class.forName("org.bukkit.craftbukkit.v1_7_R3." + className);
        } catch (ClassNotFoundException e) {
            try {
                return Class.forName("org.bukkit.craftbukkit.v1_7_R2." + className);
            } catch (ClassNotFoundException e1) {
                try {
                    return Class.forName("org.bukkit.craftbukkit.v1_7_R1." + className);
                } catch (ClassNotFoundException e2) {
                    e2.printStackTrace();
                    return null;
                }
            }
        }
    }
}
