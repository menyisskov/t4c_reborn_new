package com.perso.T4C.server;

import com.google.gson.Gson;
import com.perso.T4C.online.model.NpcAppearanceContent;
import com.perso.T4C.online.model.NpcView;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/** Bounded authored NPC inventory; it grants no dialogue or gameplay authority. */
final class NpcAppearanceCatalog {
  private static final int MAX_BYTES=2*1024*1024;
  private static final int MAX_REGION_VIEWS=64;
  private static final String KEY_PREFIX="authored-v1:";
  private final List<NpcAppearanceContent.Spawn> spawns;

  NpcAppearanceCatalog(NpcAppearanceContent content) {
    Objects.requireNonNull(content);
    var validated=new NpcAppearanceContent(content.version(),content.spawns());
    var identities=new HashSet<String>();
    var viewIds=new HashSet<String>();
    var entries=new ArrayList<NpcAppearanceContent.Spawn>();
    for(var source:validated.spawns()) {
      var spawn=new NpcAppearanceContent.Spawn(source.spawnKey(),source.type(),
          source.canonicalNpcId(),source.world(),source.x(),source.y(),source.stationary(),
          source.aggressive(),source.deployable(),source.displayName(),source.spriteBase(),
          source.parts(),source.interactionSupported(),source.frameEvidence(),
          source.appearanceDiagnostic());
      if(!identities.add(spawn.spawnKey()) || !viewIds.add(viewId(spawn)))
        throw new IllegalArgumentException("Duplicate NPC spawn or public identity");
      // Validate public appearance shape without requiring unhosted frames to be available.
      new NpcView(viewId(spawn),spawn.displayName(),spawn.x(),spawn.y(),
          spawn.spriteBase(),spawn.parts());
      entries.add(spawn);
    }
    spawns=List.copyOf(entries);
  }

  static NpcAppearanceCatalog loadBundled() throws IOException {
    try(var in=NpcAppearanceCatalog.class.getResourceAsStream("/content/npc-appearances-v1.json")) {
      if(in==null) throw new IOException("Missing NPC appearance content");
      byte[] bytes=in.readNBytes(MAX_BYTES+1);
      if(bytes.length>MAX_BYTES) throw new IOException("NPC appearance content exceeds limit");
      try {
        return new NpcAppearanceCatalog(new Gson().fromJson(
            new String(bytes,StandardCharsets.UTF_8),NpcAppearanceContent.class));
      } catch(RuntimeException invalid) {
        throw new IOException("Invalid NPC appearance content",invalid);
      }
    }
  }

  List<NpcAppearanceContent.Spawn> inRegion(RegionMapLoader.Spec region) {
    var found=new ArrayList<NpcAppearanceContent.Spawn>();
    for(var spawn:spawns) {
      if(!region.contains(spawn.world(),spawn.x(),spawn.y())) continue;
      if(!spawn.deployable()) throw new IllegalArgumentException("Undeployable NPC in hosted region");
      if(!"@invisible".equals(spawn.spriteBase())
          && spawn.frameEvidence()!=NpcAppearanceContent.FrameEvidence.FRAME_NAME_PRESENT)
        throw new IllegalArgumentException("Unverified NPC frame in hosted region");
      found.add(spawn);
      if(found.size()>MAX_REGION_VIEWS) throw new IllegalArgumentException("Too many NPCs in hosted region");
    }
    return List.copyOf(found);
  }

  /** NpcView IDs must start with a letter and fit 64 characters; reject any truncated collision. */
  static String viewId(NpcAppearanceContent.Spawn spawn) {
    String key=spawn.spawnKey();
    if(!key.matches("authored-v1:[0-9a-f]{64}"))
      throw new IllegalArgumentException("Invalid NPC spawn identity");
    return "N"+key.substring(KEY_PREFIX.length(),KEY_PREFIX.length()+63);
  }
}
