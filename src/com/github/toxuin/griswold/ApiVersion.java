package com.github.toxuin.griswold;

public class ApiVersion {
    private int major;
    private int minor;
    private int release;
    private String build;
    private String nmsVersion;
    private String raw;

    public ApiVersion(String versionString, String nmsVersion) {
        this.raw = versionString;
        // DIGITS.DIGITS.DIGITS-ALPHANUMERIC
        // MAJOR.MINOR.BUILD-RELEASE
        // 1.12.1-R0.1-SNAPSHOT
        String version = versionString.substring(0, versionString.indexOf('-')); // FROM START TO FIRST "-"
        this.build = versionString.substring(versionString.indexOf('-') + 1); // WHATEVER GOES AFTER FIRST "-"
        String versionParts[] = version.split("\\.");
        try {
            major = Integer.parseInt(versionParts[0]);
        } catch (NumberFormatException e) {
            major = -1;
        }
        if (versionParts.length > 1) {
            try {
                minor = Integer.parseInt(versionParts[1]);
            } catch (NumberFormatException e) {
                minor = -1;
            }
        }
        if (versionParts.length > 2) {
            try {
                release = Integer.parseInt(versionParts[2]);
            } catch (NumberFormatException e) {
                release = -1;
            }
        }
        this.nmsVersion = nmsVersion.substring(nmsVersion.lastIndexOf(".") + 1);
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getRelease() {
        return release;
    }

    public String getBuild() {
        return build;
    }

    public String getNMSVersion() {
        return nmsVersion;
    }

    public boolean isValid() {
        return major != -1 && minor != -1;
    }

    @Override
    public String toString() {
        return this.raw;
    }
}
