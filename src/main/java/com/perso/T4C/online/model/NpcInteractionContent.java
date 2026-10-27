package com.perso.T4C.online.model;

import java.util.List;
import java.util.Map;

/** Authored, non-hostile NPC appearances and spawn locations supported by online interactions. */
public record NpcInteractionContent(int version, List<Npc> npcs) {
  public static final int VERSION = 1;
  public NpcInteractionContent {
    if (version != VERSION) throw new IllegalArgumentException("Unsupported NPC content version");
    npcs = List.copyOf(npcs);
    if (npcs.isEmpty() || npcs.stream().map(Npc::id).distinct().count() != npcs.size())
      throw new IllegalArgumentException("Missing or duplicate NPC identity");
  }

  public record Npc(String id, String displayName, int world, int x, int y,
      String spriteBase, Map<String, String> parts) {
    public Npc {
      if (id == null || id.isBlank() || displayName == null || displayName.isBlank()
          || world < 0 || x < 0 || y < 0) throw new IllegalArgumentException("Invalid NPC");
      parts = Map.copyOf(parts);
      if ((spriteBase == null || spriteBase.isBlank()) && parts.isEmpty())
        throw new IllegalArgumentException("Missing NPC appearance");
    }
  }
}
