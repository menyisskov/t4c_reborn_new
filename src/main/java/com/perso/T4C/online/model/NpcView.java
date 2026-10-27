package com.perso.T4C.online.model;

import com.perso.T4C.player.BodyPart;
import java.util.Map;

/** Public authored NPC appearance and region-local position; contains no character quest state. */
public record NpcView(String id, String displayName, double x, double y,
    String spriteBase, Map<String, String> parts) {
  public NpcView {
    if (id == null || !id.matches("[A-Za-z][A-Za-z0-9_]{0,63}")
        || displayName == null || displayName.isBlank() || displayName.length()>256
        || !Double.isFinite(x) || !Double.isFinite(y) || x<0 || y<0)
      throw new IllegalArgumentException("Invalid public NPC");
    parts=Map.copyOf(parts);
    for(var part:parts.entrySet()) {
      BodyPart.valueOf(part.getKey());
      if(part.getValue().isBlank() || part.getValue().length()>256)
        throw new IllegalArgumentException("Invalid NPC appearance part");
    }
    if((spriteBase==null || spriteBase.isBlank()) && parts.isEmpty())
      throw new IllegalArgumentException("Missing NPC appearance");
    if(spriteBase!=null && spriteBase.length()>256)
      throw new IllegalArgumentException("Invalid NPC sprite");
  }
}
