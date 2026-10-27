package com.perso.T4C.quest;

import java.util.Map;

/** Kilhiam's authored opening conversation, shared without desktop/player dependencies. */
public final class CrimsonscaleLetterRules {
  public static final String NPC = "Kilhiam";
  public static final String LETTER_FLAG = "ADDON_CRIMSONSCALE_LETTER";
  public static final String STORY_FLAG = "ADDON_STORYLINE_PROGRESS";
  public static final String ITEM = "item.letter_from_crimsonscale";
  private CrimsonscaleLetterRules() {}

  public static boolean grantsLetter(int letterFlag) { return letterFlag == 0; }

  public static String greeting(int letterFlag, int storyProgress) {
    if (grantsLetter(letterFlag)) return "npc.kilhiam.letter";
    return storyProgress == 19 ? "npc.kilhiam.highpriest"
        : storyProgress == 20 ? "npc.kilhiam.brotherkiran"
        : storyProgress < 42 ? "npc.kilhiam.temple" : "npc.kilhiam.welcome";
  }

  /** Only this proven non-combat flag is supported; unknown quest scripts remain fail-closed. */
  public static boolean hasUnsupportedCombatFlags(Map<String, Integer> flags) {
    return flags.entrySet().stream().anyMatch(e -> !LETTER_FLAG.equals(e.getKey()) || e.getValue() != 1);
  }
}
