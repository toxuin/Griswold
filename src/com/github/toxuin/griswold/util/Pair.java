package com.github.toxuin.griswold.util;

public class Pair {
    public int x = 0;
    public int z = 0;
    public Pair (int x, int z) {
        this.x = x;
        this.z = z;
    }

    public boolean equals(Pair pair) {
        return this.x == pair.x && this.z == pair.z;
    }
    public String toString() {
        return "Pair{x="+this.x+"z="+this.z+"}";
    }
}