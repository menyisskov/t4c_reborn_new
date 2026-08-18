package com.perso.T4C.npc.ravensdust;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class BishopCrowbanner extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "BishopCrowbanner";

  public static final String DISPLAY_NAME = "${npc.bishopcrowbanner}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupNecromanRobe"),
              new NpcSpec.Part(BodyPart.BOOT, "PupBlackLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLeatherPants")),
          0,
          List.of(),
          "${npc.welcome.bishopcrowbanner}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.bishopcrowbanner.0.0}",
                      "${npc.topic_keyword.bishopcrowbanner.0.1}"),
                  "${npc.topic.bishopcrowbanner.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.bishopcrowbanner.1.0}",
                      "${npc.topic_keyword.bishopcrowbanner.1.1}"),
                  "${npc.topic.bishopcrowbanner.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.bishopcrowbanner.2.0}"),
                  "${npc.topic.bishopcrowbanner.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.bishopcrowbanner.3.0}"),
                  "${npc.topic.bishopcrowbanner.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.bishopcrowbanner.4.0}",
                      "${npc.topic_keyword.bishopcrowbanner.4.1}"),
                  "${npc.topic.bishopcrowbanner.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.bishopcrowbanner.5.0}",
                      "${npc.topic_keyword.bishopcrowbanner.5.1}",
                      "${npc.topic_keyword.bishopcrowbanner.5.2}",
                      "${npc.topic_keyword.bishopcrowbanner.5.3}"),
                  "${npc.topic.bishopcrowbanner.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.bishopcrowbanner.6.0}",
                      "${npc.topic_keyword.bishopcrowbanner.6.1}"),
                  "${npc.topic.bishopcrowbanner.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.bishopcrowbanner.7.0}",
                      "${npc.topic_keyword.bishopcrowbanner.7.1}",
                      "${npc.topic_keyword.bishopcrowbanner.7.2}"),
                  "${npc.topic.bishopcrowbanner.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.bishopcrowbanner.8.0}",
                      "${npc.topic_keyword.bishopcrowbanner.8.1}",
                      "${npc.topic_keyword.bishopcrowbanner.8.2}"),
                  "${npc.topic.bishopcrowbanner.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.bishopcrowbanner.9.0}",
                      "${npc.topic_keyword.bishopcrowbanner.9.1}"),
                  "${npc.topic.bishopcrowbanner.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.bishopcrowbanner.10.0}",
                      "${npc.topic_keyword.bishopcrowbanner.10.1}",
                      "${npc.topic_keyword.bishopcrowbanner.10.2}",
                      "${npc.topic_keyword.bishopcrowbanner.10.3}"),
                  "${npc.topic.bishopcrowbanner.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.bishopcrowbanner.11.0}",
                      "${npc.topic_keyword.bishopcrowbanner.11.1}",
                      "${npc.topic_keyword.bishopcrowbanner.11.2}",
                      "${npc.topic_keyword.bishopcrowbanner.11.3}",
                      "${npc.topic_keyword.bishopcrowbanner.11.4}"),
                  "${npc.topic.bishopcrowbanner.11}",
                  List.of())),
          "ChamberlainThomarNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 370, 65535, "1d36+27"));

  @Override
  protected NpcBehavior javaBehavior() {

    return BEHAVIOR;
  }

  private static final NpcBehavior BEHAVIOR =
      new NpcBehavior() {

        @Override
        public void onInitialise(NpcBehaviorContext c) {

          c.npc().setStationary(true);
        }

        @Override
        public void onConversationStart(NpcBehaviorContext c) {

          c.sayKey(
              c.globalFlag("__QUEST_BISHOP_ILLNESS") == 1
                  ? "npc.bishop.remission"
                  : "npc.bishop.ill");
        }

        @Override
        public boolean onKeyword(NpcBehaviorContext c, String text) {

          String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

          if (k.contains("GIVE") && k.contains("APPLE")) {

            if (c.hasItem("poison_apple")) {

              c.takeItem("poison_apple");

              c.globalFlag("__QUEST_BISHOP_ILLNESS", 0);

              c.globalFlag(
                  "__GLOBAL_QUEST_CROWBANNER_TIMER",
                  (int) (System.currentTimeMillis() / 1000L + 14400));

              if (c.flag("__QUEST_SEEK_SHADEEN") <= 1) c.flag("__QUEST_SEEK_SHADEEN", 2);

              c.sayKey("npc.bishop.poison.apple");

            } else if (c.hasItem("apple")) {

              c.takeItem("apple");

              c.sayKey("npc.bishop.apple");

            } else c.sayKey("npc.bishop.noapple");

            return true;
          }

          return false;
        }
      };

  public BishopCrowbanner(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
