package com.perso.T4C.spell;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

/** SpellCastingService maps the shared rule enums to its own by name; they must not drift. */
class SpellCastRulesParityTest {

  @Test
  void ruleEnumsMirrorTheOfflineServiceByName() {
    assertEquals(
        Arrays.stream(SpellCastingService.Failure.values()).map(Enum::name).toList(),
        Arrays.stream(SpellCastRules.Verdict.values()).map(Enum::name).toList());
    assertEquals(
        Arrays.stream(SpellCastingService.TargetKind.values()).map(Enum::name).toList(),
        Arrays.stream(SpellCastRules.TargetKind.values()).map(Enum::name).toList());
  }
}
