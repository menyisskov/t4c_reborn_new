package com.perso.T4C.render;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.DecorLayerRuleBinaryIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;
/**
 * Class representing DecorFlags.
 */

public final class DecorFlags {
    private static final Logger log = LoggerFactory.getLogger(DecorFlags.class);

    private DecorFlags() {}

    public static Set<String> loadDefaults() {
        return Set.of(
                "TapisRouge 1",
                "TapisRouge 1M",
                "TapisRouge 2",
                "TapisRouge 2M",
                "TapisRouge 3",
                "TapisRouge 3M",
                "Ble",
                // Bridges are walked upon, so they must stay behind entities instead of fading them.
                "Bridge1",
                "Bridge1M",
                "Bridge2",
                "Bridge2M",
                "Bridge3",
                "Bridge3M"
        );
    }

    /**
     * Decors an entity can be talked to through, while still blocking movement and attacks.
     *
     * <p>Cemetery gates are railings: their tiles are {@code ABSOLUTE} so nobody walks or shoots
     * through them, but they are open enough to hold a conversation across.
     */
    public static Set<String> loadSeeThroughForTalkRules() {
        return Set.of(
                "Cemetery Gates -",
                "Cemetery Gates -M",
                "Cemetery Gates >",
                "Cemetery Gates >M",
                "Cemetery Gates X",
                "Cemetery Gates XM",
                "Cemetery Gates \\v",
                "Cemetery Gates \\vM",
                "Cemetery Gates ^",
                "Cemetery Gates v"
        );
    }

    public static Set<String> loadPlayerAlwaysAboveRules() {
        Set<String> names = new LinkedHashSet<>(loadDefaults());
        File file = new File(Paths.DECOR_LAYER_RULES_BIN);
        if (!file.exists()) {
            return names;
        }
        try {
            names.addAll(DecorLayerRuleBinaryIO.read(file));
        } catch (Exception e) {
            log.warn("Could not load decor layer rules from {}: {}", file.getPath(), e.getMessage());
        }
        return names;
    }
}
