package com.perso.T4C.item.json;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.ItemRegistry;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads equipment item definitions authored as JSON files (one item per file) from a directory
 * and merges them into {@link ItemRegistry} alongside the Java-defined items.
 */
public final class ItemJsonLoader {
  private static final Logger log = LoggerFactory.getLogger(ItemJsonLoader.class);
  public static final String DEFAULT_DIRECTORY = "assets/items";

  private ItemJsonLoader() {}

  public static void loadAndRegister() {
    loadAndRegister(DEFAULT_DIRECTORY);
  }

  public static void loadAndRegister(String directoryPath) {
    File directory = new File(directoryPath);
    File[] files = directory.listFiles((dir, name) -> name.endsWith(".json"));
    if (files == null || files.length == 0) {
      log.info("No JSON item definitions found in {}", directory.getAbsolutePath());
      return;
    }
    Gson gson = new Gson();
    List<ItemDefinition> definitions = new ArrayList<>();
    for (File file : files) {
      try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
        ItemJsonDef json = gson.fromJson(reader, ItemJsonDef.class);
        if (json == null) {
          log.warn("Skipping empty item JSON file: {}", file.getName());
          continue;
        }
        definitions.add(json.toItemDefinition());
      } catch (JsonSyntaxException | IllegalStateException | IllegalArgumentException e) {
        log.error("Invalid item JSON file {}: {}", file.getName(), e.getMessage());
      } catch (IOException e) {
        log.error("Failed to read item JSON file {}: {}", file.getName(), e.getMessage());
      }
    }
    ItemRegistry.registerAdditionalDefinitions(definitions);
    log.info("Loaded {} JSON item definition(s) from {}", definitions.size(), directoryPath);
  }
}
