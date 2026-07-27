package com.perso.T4C.tmpl3;

import java.util.EnumMap;
import java.util.Map;

public final class ExpectedMaskBuilder {
    public MaskColor[][] build(TerrainContext context) {
        MaskColor[][] expected = new MaskColor[Tmpl3Mask.HEIGHT][Tmpl3Mask.WIDTH];
        for (int y = 0; y < Tmpl3Mask.HEIGHT; y++) {
            for (int x = 0; x < Tmpl3Mask.WIDTH; x++) {
                expected[y][x] = colorForPixel(context, x, y);
            }
        }
        return expected;
    }

    private MaskColor colorForPixel(TerrainContext context, int px, int py) {
        double nx = (px - 15.5d) / 15.5d;
        double ny = (py - 7.5d) / 7.5d;
        Map<Direction, Double> weights = new EnumMap<>(Direction.class);
        weights.put(Direction.N, Math.max(0d, -ny));
        weights.put(Direction.S, Math.max(0d, ny));
        weights.put(Direction.W, Math.max(0d, -nx));
        weights.put(Direction.E, Math.max(0d, nx));
        weights.put(Direction.NW, Math.max(0d, -ny) * Math.max(0d, -nx) * 1.45d);
        weights.put(Direction.NE, Math.max(0d, -ny) * Math.max(0d, nx) * 1.45d);
        weights.put(Direction.SW, Math.max(0d, ny) * Math.max(0d, -nx) * 1.45d);
        weights.put(Direction.SE, Math.max(0d, ny) * Math.max(0d, nx) * 1.45d);

        double green = 0d;
        double blue = 0d;
        for (Direction direction : Direction.values()) {
            double weight = weights.getOrDefault(direction, 0d);
            if (weight <= 0d) {
                continue;
            }
            if (context.isFamilyA(direction)) {
                green += weight;
            } else if (context.isFamilyB(direction)) {
                blue += weight;
            }
        }
        if (green <= 0d && blue <= 0d) {
            return MaskColor.UNKNOWN;
        }
        double total = green + blue;
        if (total > 0d && Math.abs(green - blue) / total < 0.06d) {
            return MaskColor.UNKNOWN;
        }
        return green >= blue ? MaskColor.GREEN : MaskColor.BLUE;
    }
}
