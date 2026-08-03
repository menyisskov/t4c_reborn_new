package com.perso.T4C.tools;

import com.perso.T4C.config.MapDefinition;
import com.perso.T4C.harvest.HerbManager;
import com.perso.T4C.helper.MapReader;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

/** CLI diagnostic for the ground names eligible for herb spawning around one tile. */
public final class HerbSpawnAudit {
    private HerbSpawnAudit() {}

    public static void main(String[] args) throws Exception {
        if (args.length < 3) throw new IllegalArgumentException("Usage: HerbSpawnAudit <tileX> <tileY> <z> [radius]");
        int centerX = Integer.parseInt(args[0]);
        int centerY = Integer.parseInt(args[1]);
        int z = Integer.parseInt(args[2]);
        int radius = args.length > 3 ? Math.max(1, Integer.parseInt(args[3])) : 30;
        MapDefinition map = MapDefinition.fromZ(z);
        Map<String, Integer> counts = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        int eligible = 0;
        try (MapReader reader = new MapReader(new File(map.getMapPath()), true)) {
            for (int y = Math.max(0, centerY - radius); y <= Math.min(reader.getHeight() - 1, centerY + radius); y++) {
                for (int x = Math.max(0, centerX - radius); x <= Math.min(reader.getWidth() - 1, centerX + radius); x++) {
                    String name = reader.getGroundSpriteName(x, y);
                    counts.merge(name == null ? "<null>" : name, 1, Integer::sum);
                    if (HerbManager.isAllowedGround(name)) eligible++;
                }
            }
        }
        System.out.println("Eligible tiles: " + eligible);
        counts.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(30).forEach(entry -> System.out.println(entry.getValue() + " x " + entry.getKey()));
    }
}
