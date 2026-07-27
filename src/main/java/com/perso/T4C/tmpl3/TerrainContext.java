package com.perso.T4C.tmpl3;

import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public final class TerrainContext {
    private final int x;
    private final int y;
    private final Map<Direction, TerrainSample> samples;
    private final String familyA;
    private final String familyB;
    private final String terrainA;
    private final String terrainB;

    private TerrainContext(int x, int y, Map<Direction, TerrainSample> samples, String familyA, String familyB,
            String terrainA, String terrainB) {
        this.x = x;
        this.y = y;
        this.samples = Collections.unmodifiableMap(samples);
        this.familyA = familyA;
        this.familyB = familyB;
        this.terrainA = terrainA;
        this.terrainB = terrainB;
    }

    public static TerrainContext analyze(int x, int y, int width, int height, TerrainResolver resolver) {
        Map<Direction, TerrainSample> samples = new EnumMap<>(Direction.class);
        Map<String, TerrainScore> scores = new HashMap<>();
        for (Direction direction : Direction.values()) {
            int nx = x + direction.dx;
            int ny = y + direction.dy;
            if (nx < 0 || ny < 0 || nx >= width || ny >= height) {
                continue;
            }
            String terrain = resolver.resolveTerrainName(nx, ny);
            if (terrain == null || terrain.isBlank()) {
                continue;
            }
            String family = resolver.familyName(terrain);
            if (family == null || family.isBlank()) {
                family = terrain;
            }
            TerrainSample sample = new TerrainSample(terrain, family, direction);
            samples.put(direction, sample);
            scores.computeIfAbsent(family, TerrainScore::new).add(terrain, direction.cardinal);
        }
        String familyA = pickFamily(scores, null);
        String familyB = pickFamily(scores, familyA);
        String terrainA = familyA != null ? scores.get(familyA).bestTerrainName : null;
        String terrainB = familyB != null ? scores.get(familyB).bestTerrainName : null;
        return new TerrainContext(x, y, samples, familyA, familyB, terrainA, terrainB);
    }

    public boolean isValid() {
        return familyA != null && familyB != null && !familyA.equalsIgnoreCase(familyB);
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public Map<Direction, TerrainSample> samples() {
        return samples;
    }

    public String familyA() {
        return familyA;
    }

    public String familyB() {
        return familyB;
    }

    public String terrainA() {
        return terrainA;
    }

    public String terrainB() {
        return terrainB;
    }

    public boolean isFamilyA(Direction direction) {
        TerrainSample sample = samples.get(direction);
        return sample != null && familyA != null && sample.familyName.equalsIgnoreCase(familyA);
    }

    public boolean isFamilyB(Direction direction) {
        TerrainSample sample = samples.get(direction);
        return sample != null && familyB != null && sample.familyName.equalsIgnoreCase(familyB);
    }

    private static String pickFamily(Map<String, TerrainScore> scores, String excluded) {
        return scores.values().stream()
                .filter(score -> excluded == null || !score.familyName.equalsIgnoreCase(excluded))
                .max(Comparator
                        .comparingInt((TerrainScore score) -> score.count)
                        .thenComparingInt(score -> score.cardinalCount)
                        .thenComparing(score -> score.familyName, Comparator.reverseOrder()))
                .map(score -> score.familyName)
                .orElse(null);
    }

    public record TerrainSample(String terrainName, String familyName, Direction direction) {
    }

    private static final class TerrainScore {
        final String familyName;
        int count;
        int cardinalCount;
        String bestTerrainName;

        TerrainScore(String familyName) {
            this.familyName = familyName;
        }

        void add(String terrainName, boolean cardinal) {
            count++;
            if (cardinal) {
                cardinalCount++;
            }
            if (bestTerrainName == null || terrainName.compareToIgnoreCase(bestTerrainName) < 0) {
                bestTerrainName = terrainName;
            }
        }
    }
}
