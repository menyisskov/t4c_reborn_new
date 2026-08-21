package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "Edgar", x = 0, y = 0, z = 0, stationary = false, aggressive = false)
public final class Edgar extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Edgar";

  public static final String DISPLAY_NAME = "${npc.edgar}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupBodyClothSet1"),
              new NpcSpec.Part(BodyPart.LEFT_ARM, "PupNakedArmL"),
              new NpcSpec.Part(BodyPart.RIGHT_ARM, "PupNakedArmR"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLegsClothSet1")),
          0,
          List.of(),
          "${npc.welcome.edgar}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.edgar.0.0}"), "${npc.topic.edgar.0}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.edgar.1.0}", "${npc.topic_keyword.edgar.1.1}"),
                  "${npc.topic.edgar.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.edgar.2.0}",
                      "${npc.topic_keyword.edgar.2.1}",
                      "${npc.topic_keyword.edgar.2.2}"),
                  "${npc.topic.edgar.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.edgar.3.0}"), "${npc.topic.edgar.3}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.edgar.4.0}",
                      "${npc.topic_keyword.edgar.4.1}",
                      "${npc.topic_keyword.edgar.4.2}"),
                  "${npc.topic.edgar.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.edgar.5.0}"), "${npc.topic.edgar.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.edgar.6.0}", "${npc.topic_keyword.edgar.6.1}"),
                  "${npc.topic.edgar.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.edgar.7.0}"), "${npc.topic.edgar.7}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.edgar.8.0}"), "${npc.topic.edgar.8}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.edgar.9.0}"), "${npc.topic.edgar.9}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.edgar.10.0}"), "${npc.topic.edgar.10}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.edgar.11.0}"), "${npc.topic.edgar.11}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.edgar.12.0}"), "${npc.topic.edgar.12}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.edgar.13.0}",
                      "${npc.topic_keyword.edgar.13.1}",
                      "${npc.topic_keyword.edgar.13.2}"),
                  "${npc.topic.edgar.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.edgar.14.0}", "${npc.topic_keyword.edgar.14.1}"),
                  "${npc.topic.edgar.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.edgar.15.0}",
                      "${npc.topic_keyword.edgar.15.1}",
                      "${npc.topic_keyword.edgar.15.2}",
                      "${npc.topic_keyword.edgar.15.3}"),
                  "${npc.topic.edgar.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.edgar.16.0}",
                      "${npc.topic_keyword.edgar.16.1}",
                      "${npc.topic_keyword.edgar.16.2}",
                      "${npc.topic_keyword.edgar.16.3}",
                      "${npc.topic_keyword.edgar.16.4}"),
                  "${npc.topic.edgar.16}",
                  List.of())),
          "EdgarNPC",
          new NpcSpec.CombatProfile(5, 84, 20, 19, 19, 2, 70, 30, "1d8+4"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if (c.flag("__SHAKE_EDGAR") > 1) c.sayKey("npc.edgar.refuse");
        else if (c.flag("__FLAG_ADDON_STORYLINE_PROGRESS") < 42) c.sayKey("npc.edgar.busy");
        else c.sayKey("npc.edgar.thanks");
      }

      @Override
      public void onDeath(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        int f = c.flag("__SHAKE_EDGAR");

        if (f == 2) {

          c.flag("__SHAKE_EDGAR", 3);

          c.giveGold(1000);

          c.shoutKey("npc.edgar.death.paid");

        } else c.shoutKey(f > 2 ? "npc.edgar.death.again" : "npc.edgar.death.first");

        if (c.karma() >= -100 * (c.flag("__QUEST_ISLAND_ACCESS") + 1))
          c.karma(c.karma() - 5 * (500 + c.karma()) / 500);
      }
    };
  }

  public Edgar(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
