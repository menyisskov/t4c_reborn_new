package com.perso.T4C.item;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.ItemDefBinaryIO;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ItemRegistry {
    private static List<ItemDefinition> cache;
    private static Map<String, ItemDefinition> byKey;

    private ItemRegistry() {
    }

    public static synchronized List<ItemDefinition> load() {
        if (cache != null) {
            return cache;
        }
        File file = new File(Paths.ITEMS_BIN);
        rebuild(loadFromFile(file));
        return cache;
    }

    public static synchronized void save(List<ItemDefinition> defs) throws IOException {
        ItemDefBinaryIO.write(new File(Paths.ITEMS_BIN), defs);
        rebuild(defs);
    }

    public static synchronized ItemDefinition findByKey(String key) {
        load();
        return key == null ? null : byKey.get(key);
    }

    public static synchronized Map<String, ItemDefinition> allByKey() {
        load();
        return byKey;
    }

    public static synchronized void invalidate() {
        cache = null;
        byKey = null;
    }

    private static void rebuild(List<ItemDefinition> defs) {
        cache = List.copyOf(defs == null ? List.of() : defs);
        Map<String, ItemDefinition> map = new LinkedHashMap<>();
        for (ItemDefinition def : cache) {
            if (def != null && def.getKey() != null) {
                map.put(def.getKey(), def);
            }
        }
        byKey = Map.copyOf(map);
    }

    static List<ItemDefinition> loadFromFile(File file) {
        if (file == null || !file.exists()) {
            return List.of();
        }
        try {
            return ItemDefBinaryIO.read(file);
        } catch (Exception ignored) {
            return List.of();
        }
    }
}
