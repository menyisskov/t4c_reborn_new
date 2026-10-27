package com.perso.T4C.server;

import com.google.gson.Gson;
import com.perso.T4C.online.model.MonsterContent;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;

/** Loads registry-authored data without a renderer, classpath scanning or desktop saves. */
public final class MonsterCatalog {
  private MonsterCatalog() {}

  public static MonsterContent loadBundled() throws IOException {
    try (var input = MonsterCatalog.class.getResourceAsStream("/content/monsters-v1.json")) {
      if (input == null) throw new IOException("Missing bundled monster content");
      return read(input);
    }
  }

  /** Runtime authority uses the staged classified inventory only after matching every v1 spawn. */
  static MonsterContent.Classified loadClassifiedBundled(MonsterContent legacy) throws IOException {
    try(var input=MonsterCatalog.class.getResourceAsStream("/content/monsters-v2.json")) {
      if(input==null) throw new IOException("Missing classified monster content");
      byte[] bytes=input.readNBytes(16*1024*1024+1);
      if(bytes.length>16*1024*1024) throw new IOException("Classified monster content exceeds size limit");
      try {
        var raw=new Gson().fromJson(new String(bytes,StandardCharsets.UTF_8),MonsterContent.Classified.class);
        if(raw==null || raw.spawns()==null || raw.authorities()==null || raw.relations()==null)
          throw new IllegalArgumentException("Incomplete classified content");
        var classified=new MonsterContent.Classified(raw.version(),legacy.monsters(),raw.authorities(),
            raw.relations(),raw.spawns().stream().map(s->new MonsterContent.ClassifiedSpawn(
                s.type(),s.definition(),s.canonicalNpcId(),s.ownership(),s.x(),s.y(),s.world(),
                s.stationary(),s.aggressive())).toList());
        if(!new Gson().toJsonTree(raw.monsters()).equals(new Gson().toJsonTree(legacy.monsters())))
          throw new IllegalArgumentException("Classified definitions differ from hosted v1 content");
        var original=new HashMap<String,MonsterContent.Spawn>();
        for(var spawn:legacy.spawns()) original.put(spawn.persistenceKey(),spawn);
        var seen=new HashSet<String>();
        for(var spawn:classified.spawns()) {
          var unchanged=original.get(spawn.persistenceKey());
          if(unchanged==null || !seen.add(spawn.persistenceKey())
              || !java.util.Objects.equals(unchanged.definition(),spawn.definition())
              || unchanged.stationary()!=spawn.stationary() || unchanged.aggressive()!=spawn.aggressive())
            throw new IllegalArgumentException("Classified spawn changed authored monster inventory");
        }
        if(seen.size()!=original.size()) throw new IllegalArgumentException("Incomplete classified spawn inventory");
        return classified;
      } catch(RuntimeException invalid) { throw new IOException("Invalid classified monster content",invalid); }
    }
  }

  static MonsterContent read(InputStream input) throws IOException {
    int limit = 16 * 1024 * 1024;
    byte[] bytes = input.readNBytes(limit + 1);
    if (bytes.length > limit) throw new IOException("Monster content exceeds size limit");
    try {
      var content = new Gson().fromJson(new String(bytes, StandardCharsets.UTF_8), MonsterContent.class);
      if (content == null) throw new IllegalArgumentException("Empty content");
      return content;
    } catch (RuntimeException error) {
      throw new IOException("Invalid or unsupported monster content", error);
    }
  }
}
