package com.perso.T4C.tmpl3;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class Tmpl3Mask {
    public static final int WIDTH = 32;
    public static final int HEIGHT = 16;

    private final int id;
    private final String name;
    private final MaskColor[][] pixels;
    private final Map<Direction, List<MaskColor>> edgeProfiles;

    public Tmpl3Mask(int id, String name, MaskColor[][] pixels) {
        this.id = id;
        this.name = name;
        this.pixels = pixels;
        this.edgeProfiles = Collections.unmodifiableMap(extractEdgeProfiles(pixels));
    }

    public int id() {
        return id;
    }

    public String name() {
        return name;
    }

    public MaskColor[][] pixels() {
        return pixels;
    }

    public Map<Direction, List<MaskColor>> edgeProfiles() {
        return edgeProfiles;
    }

    public List<MaskColor> edgeProfile(Direction direction) {
        return edgeProfiles.getOrDefault(direction, List.of());
    }

    private static Map<Direction, List<MaskColor>> extractEdgeProfiles(MaskColor[][] pixels) {
        Map<Direction, List<MaskColor>> profiles = new EnumMap<>(Direction.class);
        profiles.put(Direction.N, horizontal(pixels, 0));
        profiles.put(Direction.S, horizontal(pixels, HEIGHT - 1));
        profiles.put(Direction.W, vertical(pixels, 0));
        profiles.put(Direction.E, vertical(pixels, WIDTH - 1));
        profiles.put(Direction.NW, diagonal(pixels, true, true));
        profiles.put(Direction.NE, diagonal(pixels, false, true));
        profiles.put(Direction.SW, diagonal(pixels, true, false));
        profiles.put(Direction.SE, diagonal(pixels, false, false));
        return profiles;
    }

    private static List<MaskColor> horizontal(MaskColor[][] pixels, int y) {
        java.util.ArrayList<MaskColor> result = new java.util.ArrayList<>(WIDTH);
        for (int x = 0; x < WIDTH; x++) {
            result.add(pixels[y][x]);
        }
        return Collections.unmodifiableList(result);
    }

    private static List<MaskColor> vertical(MaskColor[][] pixels, int x) {
        java.util.ArrayList<MaskColor> result = new java.util.ArrayList<>(HEIGHT);
        for (int y = 0; y < HEIGHT; y++) {
            result.add(pixels[y][x]);
        }
        return Collections.unmodifiableList(result);
    }

    private static List<MaskColor> diagonal(MaskColor[][] pixels, boolean left, boolean top) {
        java.util.ArrayList<MaskColor> result = new java.util.ArrayList<>(HEIGHT);
        for (int i = 0; i < HEIGHT; i++) {
            int y = top ? i : HEIGHT - 1 - i;
            int x = left ? i : WIDTH - 1 - i;
            result.add(pixels[y][x]);
        }
        return Collections.unmodifiableList(result);
    }
}
