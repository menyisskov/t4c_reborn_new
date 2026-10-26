package com.perso.T4C.tools;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.MonsterRegistry;
import com.perso.T4C.monster.json.MonsterJsonLoader;
import com.perso.T4C.online.model.MonsterContent;
import com.perso.T4C.spawn.SpawnRegistry;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.TreeMap;

/** Build-time bridge only: the headless distribution never scans desktop monster classes. */
public final class OnlineMonsterContentExporter {
  private OnlineMonsterContentExporter() {}

  public static MonsterContent snapshot() {
    // Restore the registry before loading JSON so repeated exports cannot accumulate definitions.
    MonsterRegistry.resetToGeneratedDefinitions();
    MonsterJsonLoader.loadAndRegister();
    var definitions = new TreeMap<String, MonsterDef>();
    for (var definition : MonsterRegistry.load()) {
      // JSON can intentionally override a generated definition. Use the runtime lookup winner.
      var resolved = MonsterRegistry.findByName(definition.getName());
      definitions.put(resolved.getName(), resolved);
    }
    var spawns = SpawnRegistry.monsters().stream().map(spawn -> {
      var definition = MonsterRegistry.findByName(spawn.type());
      if (definition != null) definitions.putIfAbsent(definition.getName(), definition);
      return new MonsterContent.Spawn(spawn.type(), definition == null ? null : definition.getName(),
          spawn.x(), spawn.y(), spawn.z(), spawn.stationary(), spawn.aggressive());
    }).sorted(Comparator.comparingInt(MonsterContent.Spawn::world)
        .thenComparingInt(MonsterContent.Spawn::y).thenComparingInt(MonsterContent.Spawn::x)
        .thenComparing(MonsterContent.Spawn::type).thenComparing(MonsterContent.Spawn::stationary)
        .thenComparing(MonsterContent.Spawn::aggressive)).toList();
    return new MonsterContent(MonsterContent.VERSION, definitions, spawns);
  }

  public static String render(MonsterContent content) {
    var gson = new GsonBuilder().serializeNulls().create();
    return gson.toJson(canonical(gson.toJsonTree(content))) + "\n";
  }

  private static JsonElement canonical(JsonElement value) {
    if (value.isJsonObject()) {
      var result = new JsonObject();
      new TreeMap<>(value.getAsJsonObject().asMap()).forEach((key, child) -> result.add(key, canonical(child)));
      return result;
    }
    if (value.isJsonArray()) {
      var result = new JsonArray();
      value.getAsJsonArray().forEach(child -> result.add(canonical(child)));
      return result;
    }
    return value;
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 1) throw new IllegalArgumentException("Expected output JSON path");
    var content = snapshot();
    var path = Path.of(args[0]);
    if (path.getParent() != null) Files.createDirectories(path.getParent());
    Files.writeString(path, render(content));
    System.out.println("Exported monster definitions and authored spawns; custom behavior is not executable data.");
  }
}
