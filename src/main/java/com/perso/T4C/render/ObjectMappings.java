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

    public static Map<String, ObjectMapping> load() {
        File file = new File(Paths.OBJECT_MAPPINGS_BIN);
        if (!file.exists()) {
            return new HashMap<>();
        }
        try {
            return fromEntries(ObjectMappingsBinaryIO.read(file));
        } catch (Exception ignored) {
            return new HashMap<>();
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
