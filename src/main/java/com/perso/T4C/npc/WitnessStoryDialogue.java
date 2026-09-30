package com.perso.T4C.npc;

import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.quest.WitnessStory;
import java.util.ArrayList;
import java.util.List;

/** Additive topics keep original NPC topic indexes and legacy handlers intact. */
final class WitnessStoryDialogue {
  private WitnessStoryDialogue() {}

  private static List<String> topics(String npc) {
    return switch (npc) {
      case "Kilhiam" -> List.of("missing_names", "record", "investigation");
      case "Lantalir" -> List.of("remembrance", "testimony", "investigation");
      case "Librarian1" -> List.of("missing_names", "record", "compare", "investigation");
      case "Librarian2" -> List.of("judgment", "difference", "investigation");
      case "HarbormasterRangor" -> List.of("manifest", "compare", "investigation");
      case "Oracle" -> List.of("tremor", "witness", "investigation");
      default -> List.of();
    };
  }

  private static NpcSpec.DialogueTopic topic(String npc, String topic) {
    return new NpcSpec.DialogueTopic(
        topic.equals("investigation")
            ? List.of(
                "${npc.witness_story.keyword.investigation}", "${npc.witness_story.keyword.story}")
            : List.of("${npc.witness_story.keyword." + topic + "}"),
        "${npc.witness_story." + npc + "." + topic + "}",
        List.of());
  }

  static List<NpcSpec.DialogueTopic> append(String npc, List<NpcSpec.DialogueTopic> original) {
    var result = new ArrayList<>(original);
    for (String name : topics(npc)) result.add(topic(npc, name));
    return List.copyOf(result);
  }

  static boolean onKeyword(NpcBehaviorContext context, String keyword) {
    String npc = context.npc().specification().id();
    String matched = null;
    for (String name : topics(npc)) {
      if (ScriptedNpc.matches(topic(npc, name), keyword)) {
        matched = name;
        break;
      }
    }
    if (matched == null) return false;
    if (matched.equals("investigation")) {
      context.sayKey(WitnessStory.nextRouteKey(context.player()));
      return true;
    }
    String flag =
        switch (npc + "." + matched) {
          case "Kilhiam.record" -> WitnessStory.ARAKAS_RECORD;
          case "Lantalir.testimony" -> WitnessStory.GROVE_TESTIMONY;
          case "Librarian1.record" -> WitnessStory.WAR_ACCOUNT;
          case "Librarian2.difference" -> WitnessStory.JUDGMENT_ACCOUNT;
          case "Librarian1.compare" -> WitnessStory.LIBRARY_COMPARISON;
          case "HarbormasterRangor.compare" -> WitnessStory.MANIFEST_COMPARED;
          case "Oracle.witness" -> WitnessStory.ORACLE_TESTIMONY;
          default -> null;
        };
    if (flag != null && !WitnessStory.record(context.player(), flag)) {
      context.sayKey(WitnessStory.nextRouteKey(context.player()));
      return true;
    }
    String key = "npc.witness_story." + npc + "." + matched;
    if (npc.equals("Oracle") && !WitnessStory.hasDefeatedMakrsh(context.player())) key += ".before";
    context.sayKey(key);
    return true;
  }
}
