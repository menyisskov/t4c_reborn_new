package com.perso.T4C.harvest;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.HerbDefinitionBinaryIO;

import java.io.File;
import java.io.IOException;
import java.util.List;

/** Cached access to the data-driven herb catalogue. */
public final class HerbRegistry {
    private static List<HerbDefinition> cache;

    private HerbRegistry() {}

    public static synchronized List<HerbDefinition> load() {
        if (cache != null) return cache;
        File file = new File(Paths.HERBS_BIN);
        if (!file.exists()) return cache = List.of();
        try {
            return cache = List.copyOf(HerbDefinitionBinaryIO.read(file));
        } catch (Exception ignored) {
            return cache = List.of();
        }
    }

    public static synchronized void save(List<HerbDefinition> definitions) throws IOException {
        HerbDefinitionBinaryIO.write(new File(Paths.HERBS_BIN), definitions);
        cache = List.copyOf(definitions == null ? List.of() : definitions);
    }

    public static synchronized void invalidate() { cache = null; }
}
