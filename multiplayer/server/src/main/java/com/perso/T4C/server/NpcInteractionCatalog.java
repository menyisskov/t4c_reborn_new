package com.perso.T4C.server;

import com.google.gson.Gson;
import com.perso.T4C.online.model.NpcInteractionContent;
import com.perso.T4C.quest.CrimsonscaleLetterRules;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Strict catalog for implemented interactions, not a claim that arbitrary NPC scripts are supported. */
public final class NpcInteractionCatalog {
  /**
   * Kilhiam's letter conversation, Warden Aelric's renewed_wards quest giver, and Sigfried the
   * weaponsmith, whose interaction is a greeting plus the vendor trade routes; nothing else.
   */
  static final Set<String> SUPPORTED = Set.of(CrimsonscaleLetterRules.NPC, "WardenAelric", "Sigfried");
  private final NpcInteractionContent content;
  public NpcInteractionCatalog(NpcInteractionContent content) {
    var ids = content.npcs().stream().map(NpcInteractionContent.Npc::id).collect(Collectors.toSet());
    if (!ids.equals(SUPPORTED)) throw new IllegalArgumentException("Unsupported NPC interaction");
    this.content=content;
  }
  public List<NpcInteractionContent.Npc> npcs() { return content.npcs(); }
  public NpcInteractionContent.Npc require(String id) {
    return content.npcs().stream().filter(n->n.id().equals(id)).findFirst()
        .orElseThrow(()->new IllegalArgumentException("Unsupported NPC interaction"));
  }
  public static NpcInteractionCatalog loadBundled() throws IOException {
    try(var in=NpcInteractionCatalog.class.getResourceAsStream("/content/npc-interactions-v1.json")) {
      if(in==null) throw new IOException("Missing NPC interaction content");
      int limit=64*1024; byte[] bytes=in.readNBytes(limit+1);
      if(bytes.length>limit) throw new IOException("NPC interaction content exceeds limit");
      try { return new NpcInteractionCatalog(new Gson().fromJson(new String(bytes,StandardCharsets.UTF_8),NpcInteractionContent.class)); }
      catch(RuntimeException invalid) { throw new IOException("Invalid NPC interaction content",invalid); }
    }
  }
}
