package com.github.toxuin.griswold.util;

public class Pair<L, R> {
    private final L left;
    private final R right;

    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public L getLeft() { return left; }
    public R getRight() { return right; }

    @Override
    public int hashCode() { return left.hashCode() ^ right.hashCode(); }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (!(other instanceof Pair)) return false;
        Pair otherPair = (Pair) other;
        return this.left.equals(otherPair.getLeft()) &&
                this.right.equals(otherPair.getRight());
    }

    @Override
    public String toString() {
        return "Pair{left<"+this.getLeft().getClass().getName()+">="+this.getLeft()+", " +
               "right<"+this.getRight().getClass().getName()+">="+this.getRight()+"}";
    }
}