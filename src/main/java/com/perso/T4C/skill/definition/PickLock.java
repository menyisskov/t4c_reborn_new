package com.perso.T4C.skill.definition;

import com.perso.T4C.skill.SkillDefinition;
import java.util.Map;

public final class PickLock {
  private PickLock() {}

  public static SkillDefinition definition() {
    return new SkillDefinition("pick_lock", 12, 0, 0, 40, 0, 0, 1, Map.of(), 1000L);
  }
}
