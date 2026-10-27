package com.perso.T4C.tools;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.MonsterAnimations;
import com.perso.T4C.monster.core.MonsterClanRelations;
import com.perso.T4C.monster.core.MonsterRegistry;
import com.perso.T4C.monster.json.MonsterJsonLoader;
import com.perso.T4C.npc.core.NpcSpawnOwnership;
import com.perso.T4C.helper.SpriteBinIO;
import com.perso.T4C.online.model.MonsterContent;
import com.perso.T4C.spawn.SpawnRegistry;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

  /** Staged authority data only; the version-one runtime loader remains unchanged. */
  public static MonsterContent.Classified snapshotClassified(Path spriteDirectory) throws IOException {
    var legacy=snapshot();
    Set<String> spriteNames=spriteNames(spriteDirectory);
    var authorities=new TreeMap<String,MonsterContent.Authority>();
    legacy.monsters().forEach((name,definition) -> {
      var clan=MonsterClanRelations.resolveClan(MonsterRegistry.sourceClassName(name),name);
      var pattern=definition.getAttackPattern();
      var evidence=pattern==null || pattern.isBlank()
          ? MonsterContent.AttackFrameEvidence.NO_FRAME
          : spriteNames==null ? MonsterContent.AttackFrameEvidence.UNKNOWN
          : MonsterAnimations.hasFirstAttackFrameName(pattern,spriteNames)
              ? MonsterContent.AttackFrameEvidence.FRAME_NAME_PRESENT
              : MonsterContent.AttackFrameEvidence.NO_FRAME;
      authorities.put(name,new MonsterContent.Authority(MonsterContent.Clan.valueOf(clan.name()),evidence));
    });
    var relations=MonsterClanRelations.getRelations().stream()
        .map(relation -> new MonsterContent.ClanRelation(
            MonsterContent.Clan.valueOf(relation.source.name()),
            MonsterContent.Clan.valueOf(relation.target.name())))
        .sorted(Comparator.comparing((MonsterContent.ClanRelation r)->r.source().name())
            .thenComparing(r->r.target().name())).toList();
    var spawns=legacy.spawns().stream().map(spawn -> {
      boolean npcRegistered=NpcSpawnOwnership.owns(spawn.type());
      boolean npcOnly=spawn.definition()==null && npcRegistered;
      var registration=npcRegistered ? NpcSpawnOwnership.resolveStrict(spawn.type()) : null;
      if(npcRegistered && registration==null)
        throw new IllegalStateException("NPC-owned spawn has no canonical registration: "+spawn.type());
      return new MonsterContent.ClassifiedSpawn(spawn.type(),spawn.definition(),
          registration==null ? null : registration.id(),
          npcOnly ? MonsterContent.Ownership.NPC : MonsterContent.Ownership.MONSTER,
          spawn.x(),spawn.y(),spawn.world(),spawn.stationary(),spawn.aggressive());
    }).toList();
    return new MonsterContent.Classified(MonsterContent.Classified.VERSION,legacy.monsters(),authorities,
        relations,spawns);
  }

  /** Null means the build-time sprite name source was unavailable, never an inferred absence. */
  static Set<String> spriteNames(Path directory) throws IOException {
    if(SpriteBinIO.resolveShards(directory,SpriteBinIO.DEFAULT_BASE_NAME).isEmpty()) return null;
    var names=new HashSet<String>();
    SpriteBinIO.readAll(directory,SpriteBinIO.DEFAULT_BASE_NAME,packed -> names.add(SpriteBinIO.key(packed.name())));
    return Set.copyOf(names);
  }

  public static String renderClassified(MonsterContent.Classified content) {
    var gson=new GsonBuilder().serializeNulls().create();
    return gson.toJson(canonical(gson.toJsonTree(content)))+"\n";
  }

  static JsonElement canonical(JsonElement value) {
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
    boolean classified=args.length==2 && "--classified".equals(args[0]);
    if (!classified && args.length!=1) throw new IllegalArgumentException("Expected output JSON path");
    var path = Path.of(classified ? args[1] : args[0]);
    if (path.getParent() != null) Files.createDirectories(path.getParent());
    Files.writeString(path,classified
        ? renderClassified(snapshotClassified(Path.of("assets/sprites"))) : render(snapshot()));
    System.out.println("Exported monster definitions and authored spawns; custom behavior is not executable data.");
  }
}
