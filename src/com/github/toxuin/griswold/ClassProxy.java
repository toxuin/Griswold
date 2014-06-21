package com.github.toxuin.griswold;

public class ClassProxy {
    // THIS WHOLE FILE IS ABSOLUTELY GAY
    // THANK FOR VERSIONED NMS PACKAGES, EvilSeph
    // THERE ARE PEOPLE WHO HATE YOU.

    private static final String[] SUPPORTED_API = {"v1_7_R3", "v1_7_R2", "v1_7_R1"};

    // RELATIVE TO net.minecraft.server.vX_X_RX.
    public static Class getClass(String className) {
        Class result = null;
        for (String api : SUPPORTED_API) {
            try {
                result = Class.forName("net.minecraft.server." + api + "." + className);
            } catch (ClassNotFoundException e) {
                try {
                    result = Class.forName("org.bukkit.craftbukkit." + api + "." + className);
                } catch (ClassNotFoundException e1) {
                    // DO NOTHING SINCE RESULT IS ALREADY NULL
                }
            }
        }
        return result;
    }
}
