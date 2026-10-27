package com.perso.T4C.tools;

import com.google.gson.GsonBuilder;
import com.perso.T4C.npc.core.NpcFactoryRegistry;
import com.perso.T4C.npc.core.NpcSpawnOwnership;
import com.perso.T4C.online.model.MonsterContent;
import com.perso.T4C.online.model.NpcAppearanceContent;
import com.perso.T4C.spawn.SpawnDefinition;
import com.perso.T4C.spawn.SpawnRegistry;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/** Build-time NPC inventory, separate from the two supported interaction scripts. */
public final class OnlineNpcAppearanceExporter {
  private OnlineNpcAppearanceExporter() {}

  public static NpcAppearanceContent snapshot(Path spriteDirectory) throws IOException {
    Set<String> spriteNames=OnlineMonsterContentExporter.spriteNames(spriteDirectory);
    var supported=OnlineNpcContentExporter.snapshot().npcs().stream().map(n->n.id()).collect(java.util.stream.Collectors.toSet());
    var source=new ArrayList<SpawnDefinition>(SpawnRegistry.npcs());
    source.addAll(SpawnRegistry.monsters().stream().filter(s->NpcSpawnOwnership.owns(s.type())).toList());
    var unique=new TreeMap<String,NpcAppearanceContent.Spawn>();
    for(var spawn:source) {
      var registration=NpcSpawnOwnership.resolveStrict(spawn.type());
      if(registration==null) throw new IllegalStateException("Unclassified NPC spawn: "+spawn);
      String id=registration.id();
      var spec=registration.specification()==null ? null : registration.specification().get();
      String display=spec==null ? registration.displayName() : spec.displayName();
      String base=spec==null ? registration.spriteBase() : spec.spriteBase();
      var parts=new TreeMap<String,String>();
      if(spec!=null) for(var part:spec.parts()) parts.put(part.bodyPart().name(),part.spriteBase());
      boolean sentinel="SUNDIAL".equalsIgnoreCase(spawn.type())
          && spawn.x()==0 && spawn.y()==0 && spawn.z()==0;
      NpcAppearanceContent.FrameEvidence evidence;
      String diagnostic;
      if(base!=null && base.startsWith("@static:")) {
        String name=base.substring("@static:".length());
        evidence=spriteNames==null ? NpcAppearanceContent.FrameEvidence.UNKNOWN
            : spriteNames.contains(com.perso.T4C.helper.SpriteBinIO.key(name))
                ? NpcAppearanceContent.FrameEvidence.FRAME_NAME_PRESENT
                : NpcAppearanceContent.FrameEvidence.NO_FRAME;
        diagnostic=evidence==NpcAppearanceContent.FrameEvidence.FRAME_NAME_PRESENT
            ? "static_frame_name_present_unrendered"
            : evidence==NpcAppearanceContent.FrameEvidence.NO_FRAME ? "static_frame_name_missing"
                : "sprite_metadata_unavailable";
      } else if("@invisible".equals(base)) {
        evidence=NpcAppearanceContent.FrameEvidence.NO_FRAME;diagnostic="intentionally_invisible";
      } else {
        evidence=NpcAppearanceContent.FrameEvidence.UNKNOWN;
        diagnostic=(base==null || base.isBlank()) && parts.isEmpty()
            ? "appearance_missing" : "frames_not_verified";
      }
      if(sentinel) diagnostic="offline_placeholder_"+diagnostic;
      String key=new MonsterContent.Spawn(spawn.type(),null,spawn.x(),spawn.y(),spawn.z(),
          spawn.stationary(),spawn.aggressive()).persistenceKey();
      var entry=new NpcAppearanceContent.Spawn(key,spawn.type(),id,spawn.z(),spawn.x(),spawn.y(),
          spawn.stationary(),spawn.aggressive(),!sentinel,display,base,parts,
          supported.contains(id),evidence,diagnostic);
      var previous=unique.putIfAbsent(key,entry);
      if(previous!=null && !previous.equals(entry))
        throw new IllegalStateException("Conflicting NPC spawn identity: "+spawn);
    }
    var ordered=unique.values().stream().sorted(Comparator.comparingInt(NpcAppearanceContent.Spawn::world)
        .thenComparingInt(NpcAppearanceContent.Spawn::y)
        .thenComparingInt(NpcAppearanceContent.Spawn::x)
        .thenComparing(NpcAppearanceContent.Spawn::type)).toList();
    return new NpcAppearanceContent(NpcAppearanceContent.VERSION,ordered);
  }

  public static String render(NpcAppearanceContent content) {
    var gson=new GsonBuilder().serializeNulls().create();
    return gson.toJson(OnlineMonsterContentExporter.canonical(gson.toJsonTree(content)))+"\n";
  }

  public static void main(String[] args) throws Exception {
    if(args.length!=1) throw new IllegalArgumentException("Expected output JSON path");
    var path=Path.of(args[0]);if(path.getParent()!=null) Files.createDirectories(path.getParent());
    Files.writeString(path,render(snapshot(Path.of("assets/sprites"))));
  }
}
