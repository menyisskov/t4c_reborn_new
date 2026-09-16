package com.perso.T4C.monster.json;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.MonsterRegistry;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads monster definitions authored as JSON files (one monster per file) from a directory and
 * merges them into {@link MonsterRegistry} alongside the Java-defined monsters. Monsters loaded
 * this way get generic combat/AI behavior via {@code DataMonster} / {@code NamedEventMonster}
 * unless a specialized Java class is separately registered for the same name.
 */
public final class MonsterJsonLoader {
  private static final Logger log = LoggerFactory.getLogger(MonsterJsonLoader.class);
  public static final String DEFAULT_DIRECTORY = "assets/monsters";

  private MonsterJsonLoader() {}

  public static void loadAndRegister() {
    loadAndRegister(DEFAULT_DIRECTORY);
  }

  public static void loadAndRegister(String directoryPath) {
    File directory = new File(directoryPath);
    File[] files = directory.listFiles((dir, name) -> name.endsWith(".json"));
    if (files == null || files.length == 0) {
      log.info("No JSON monster definitions found in {}", directory.getAbsolutePath());
      return;
    }
    Gson gson = new Gson();
    List<MonsterDef> definitions = new ArrayList<>();
    for (File file : files) {
      try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
        MonsterJsonDef json = gson.fromJson(reader, MonsterJsonDef.class);
        if (json == null) {
          log.warn("Skipping empty monster JSON file: {}", file.getName());
          continue;
        }
        definitions.add(json.toMonsterDef());
        log.info("Loaded JSON monster definition: {} ({})", json.name, file.getName());
      } catch (JsonSyntaxException | IllegalStateException e) {
        log.error("Invalid monster JSON file {}: {}", file.getName(), e.getMessage());
      } catch (IOException e) {
        log.error("Failed to read monster JSON file {}: {}", file.getName(), e.getMessage());
      }
    }
    MonsterRegistry.registerAdditionalDefinitions(definitions);
  }
}
