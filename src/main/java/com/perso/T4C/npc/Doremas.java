package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "Doremas", x = 1448, y = 2503, z = 0, stationary = false, aggressive = false)
public final class Doremas extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Doremas";

  public static final String DISPLAY_NAME = "${npc.doremas}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupStuddedBodyArmor"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupStuddedLegs"),
              new NpcSpec.Part(BodyPart.HEAD, "PupChainMailCoif"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupBattleDagger")),
          0,
          List.of(),
          "${npc.welcome.doremas}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.doremas.0.0}", "${npc.topic_keyword.doremas.0.1}"),
                  "${npc.topic.doremas.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.doremas.1.0}",
                      "${npc.topic_keyword.doremas.1.1}",
                      "${npc.topic_keyword.doremas.1.2}"),
                  "${npc.topic.doremas.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.doremas.2.0}"), "${npc.topic.doremas.2}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.doremas.3.0}"), "${npc.topic.doremas.3}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.doremas.4.0}"), "${npc.topic.doremas.4}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.doremas.5.0}"), "${npc.topic.doremas.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.doremas.6.0}"), "${npc.topic.doremas.6}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.doremas.7.0}"), "${npc.topic.doremas.7}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.doremas.8.0}"), "${npc.topic.doremas.8}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.doremas.9.0}"), "${npc.topic.doremas.9}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.doremas.10.0}", "${npc.topic_keyword.doremas.10.1}"),
                  "${npc.topic.doremas.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.doremas.11.0}"),
                  "${npc.topic.doremas.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.doremas.12.0}"),
                  "${npc.topic.doremas.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.doremas.13.0}"),
                  "${npc.topic.doremas.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.doremas.14.0}",
                      "${npc.topic_keyword.doremas.14.1}",
                      "${npc.topic_keyword.doremas.14.2}",
                      "${npc.topic_keyword.doremas.14.3}"),
                  "${npc.topic.doremas.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.doremas.15.0}",
                      "${npc.topic_keyword.doremas.15.1}",
                      "${npc.topic_keyword.doremas.15.2}",
                      "${npc.topic_keyword.doremas.15.3}"),
                  "${npc.topic.doremas.15}",
                  List.of())),
          "DoremasNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 370, 65535, "1d36+27"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        int scrolls = c.itemCount("scroll_of_evil_deed");

        if (scrolls > 1) c.sayKey("npc.doremas.scroll.many");
        else if (scrolls == 1
            && c.flag("__FLAG_SHADEEN_PLAYER_B") != 0
            && c.flag("__FLAG_USER_HAS_ROBBED_THE_SAFE_BEFORE") == 0)
          c.sayKey("npc.doremas.scroll.assigned");
        else if (scrolls == 1) c.sayKey("npc.doremas.scroll.instructions");
        else c.sayKey("npc.doremas.empty");
      }

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String keyword) {

        String k = keyword == null ? "" : keyword.toUpperCase(java.util.Locale.ROOT);

        if (k.contains("ABORT") && c.flag("__FLAG_SHADEEN_PLAYER_B") != 0) {

          c.flag("__FLAG_SHADEEN_PLAYER_B", 0);

          c.sayKey("npc.doremas.abort");

          return true;
        }

        return false;
      }
    };
  }

  public Doremas(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
