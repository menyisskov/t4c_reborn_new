package com.perso.T4C.helper;

import java.util.List;

public final class AppearanceDefaultsData {
  private AppearanceDefaultsData() {}

  public record NakedPart(String gender, String bodyPart, String sprite) {}

  public record ConcealmentRule(
      String triggerSlot, String appearance, List<String> hiddenParts, boolean hidesExplicit) {
    public ConcealmentRule {
      hiddenParts = hiddenParts == null ? List.of() : List.copyOf(hiddenParts);
    }
  }

  public record EquippedOverride(
      String gender,
      String sourceSlot,
      String sourceAppearance,
      String targetSlot,
      String targetAppearance) {}

  public record Defaults(
      List<NakedPart> nakedParts,
      List<ConcealmentRule> concealmentRules,
      List<EquippedOverride> equippedOverrides) {
    public Defaults(List<NakedPart> nakedParts, List<ConcealmentRule> concealmentRules) {
      this(nakedParts, concealmentRules, List.of());
    }

    public Defaults {
      nakedParts = nakedParts == null ? List.of() : List.copyOf(nakedParts);
      concealmentRules = concealmentRules == null ? List.of() : List.copyOf(concealmentRules);
      equippedOverrides = equippedOverrides == null ? List.of() : List.copyOf(equippedOverrides);
    }
  }
}
