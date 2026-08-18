package com.perso.T4C.helper;

import com.perso.T4C.player.BodyPart;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class ConcealmentResolver {
  private ConcealmentResolver() {}

  public static void applyConcealment(Map<BodyPart, String> partMap) {
    applyConcealment(partMap, Set.of());
  }

  public static void applyConcealment(Map<BodyPart, String> partMap, Set<BodyPart> explicitParts) {
    if (partMap == null || partMap.isEmpty()) return;
    Map<BodyPart, String> snapshot = new EnumMap<>(BodyPart.class);
    snapshot.putAll(partMap);
    Set<BodyPart> hideAlways = EnumSet.noneOf(BodyPart.class);
    Set<BodyPart> hideDefaultsOnly = EnumSet.noneOf(BodyPart.class);
    for (Map.Entry<BodyPart, String> entry : snapshot.entrySet()) {
      BodyPart trigger = entry.getKey();
      for (BodyPart lookup : aliases(trigger)) {
        for (AppearanceDefaultsBinaryIO.ConcealmentRule rule :
            AppearanceDefaultsCatalog.rulesFor(lookup, entry.getValue())) {
          for (String hiddenName : rule.hiddenParts()) {
            BodyPart hidden = bodyPart(hiddenName);
            if (hidden == null || hidden == trigger) continue;
            (rule.hidesExplicit() ? hideAlways : hideDefaultsOnly).add(hidden);
          }
        }
      }
    }
    hideAlways.forEach(partMap::remove);
    Set<BodyPart> explicit = explicitParts == null ? Set.of() : explicitParts;
    hideDefaultsOnly.stream().filter(part -> !explicit.contains(part)).forEach(partMap::remove);
  }

  private static BodyPart[] aliases(BodyPart part) {
    if (part == BodyPart.HEAD) return new BodyPart[] {BodyPart.HEAD, BodyPart.HAT};
    if (part == BodyPart.HAT) return new BodyPart[] {BodyPart.HAT, BodyPart.HEAD};
    return new BodyPart[] {part};
  }

  private static BodyPart bodyPart(String value) {
    if (value == null || value.isBlank()) return null;
    try {
      return BodyPart.valueOf(value.trim().toUpperCase(java.util.Locale.ROOT));
    } catch (IllegalArgumentException e) {
      return null;
    }
  }
}
