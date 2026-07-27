package com.perso.T4C.render;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.ObjectMappingsBinaryIO;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Editable render mappings for interactive map objects.
 */
public final class ObjectMappings {
    private ObjectMappings() {
    }

    public static Map<String, ObjectMapping> loadDefaults() {
        Map<String, ObjectMapping> mappings = new HashMap<>();
        mappings.put("CLOSED_WOODEN_DOOR", new ObjectMapping(40842, "RockDoor%d$11", true, false,
                "Open Wooden Door.wav", "Close Wooden Door.wav", false, ""));
        mappings.put("CLOSED_WOODEN_DOOR_FLIP", new ObjectMapping(40842, "RockDoor%d$11", true, true,
                "Open Wooden Door.wav", "Close Wooden Door.wav", false, ""));
        mappings.put("ORACLE_DOOR_F", new ObjectMapping(130, "RockDoor%d$11", true, false,
                "Open Wooden Door.wav", "Close Wooden Door.wav", false, ""));
        mappings.put("MAP CAULDRON", new ObjectMapping(130, "RockDoor%d$11", false, false, "", "", false, ""));
        return mappings;
    }

    public static Map<String, ObjectMapping> load() {
        File file = new File(Paths.OBJECT_MAPPINGS_BIN);
        if (!file.exists()) {
            Map<String, ObjectMapping> defaults = loadDefaults();
            try {
                save(defaults);
            } catch (IOException ignored) {
            }
            return defaults;
        }
        try {
            Map<String, ObjectMapping> loaded = fromEntries(ObjectMappingsBinaryIO.read(file));
            for (Map.Entry<String, ObjectMapping> entry : loadDefaults().entrySet()) {
                loaded.putIfAbsent(entry.getKey(), entry.getValue());
            }
            return loaded;
        } catch (Exception ignored) {
            return loadDefaults();
        }
    }

    public static void save(Map<String, ObjectMapping> mappings) throws IOException {
        ObjectMappingsBinaryIO.write(new File(Paths.OBJECT_MAPPINGS_BIN), toEntries(mappings));
    }

    public static List<ObjectMappingsBinaryIO.Entry> toEntries(Map<String, ObjectMapping> mappings) {
        List<ObjectMappingsBinaryIO.Entry> entries = new ArrayList<>();
        if (mappings == null) {
            return entries;
        }
        List<String> keys = new ArrayList<>(mappings.keySet());
        keys.sort(String::compareToIgnoreCase);
        for (String key : keys) {
            ObjectMapping mapping = mappings.get(key);
            if (key != null && !key.isBlank() && mapping != null) {
                entries.add(new ObjectMappingsBinaryIO.Entry(key.toUpperCase(Locale.ROOT), mapping));
            }
        }
        return entries;
    }

    public static Map<String, ObjectMapping> fromEntries(List<ObjectMappingsBinaryIO.Entry> entries) {
        Map<String, ObjectMapping> mappings = new HashMap<>();
        if (entries == null) {
            return mappings;
        }
        for (ObjectMappingsBinaryIO.Entry entry : entries) {
            if (entry == null || entry.logicalName == null || entry.logicalName.isBlank() || entry.mapping == null) {
                continue;
            }
            mappings.put(entry.logicalName.toUpperCase(Locale.ROOT), entry.mapping);
        }
        return mappings;
    }
}
