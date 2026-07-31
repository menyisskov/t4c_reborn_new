package com.perso.T4C.objects;

public record ObjectPos(String name, long x, long y, long z, boolean mirror) {
    public ObjectPos(String name, long x, long y, long z) {
        this(name, x, y, z, false);
    }
}
