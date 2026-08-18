package com.perso.T4C.npc.stoneheim;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.StaticDialogueBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class RoenGreenleaf extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "RoenGreenleaf";

  public static final String DISPLAY_NAME = "${npc.roengreenleaf}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupLeatherBody"),
              new NpcSpec.Part(BodyPart.BOOT, "PupBlackLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLeatherPants"),
              new NpcSpec.Part(BodyPart.HEAD, "PupElvenHat"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupBattleAxe"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.roengreenleaf}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.roengreenleaf.0.0}"),
                  "${npc.topic.roengreenleaf.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.roengreenleaf.1.0}",
                      "${npc.topic_keyword.roengreenleaf.1.1}"),
                  "${npc.topic.roengreenleaf.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.roengreenleaf.2.0}",
                      "${npc.topic_keyword.roengreenleaf.2.1}",
                      "${npc.topic_keyword.roengreenleaf.2.2}"),
                  "${npc.topic.roengreenleaf.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.roengreenleaf.3.0}",
                      "${npc.topic_keyword.roengreenleaf.3.1}",
                      "${npc.topic_keyword.roengreenleaf.3.2}"),
                  "${npc.topic.roengreenleaf.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.roengreenleaf.4.0}"),
                  "${npc.topic.roengreenleaf.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.roengreenleaf.5.0}"),
                  "${npc.topic.roengreenleaf.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.roengreenleaf.6.0}",
                      "${npc.topic_keyword.roengreenleaf.6.1}",
                      "${npc.topic_keyword.roengreenleaf.6.2}",
                      "${npc.topic_keyword.roengreenleaf.6.3}"),
                  "${npc.topic.roengreenleaf.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.roengreenleaf.7.0}",
                      "${npc.topic_keyword.roengreenleaf.7.1}",
                      "${npc.topic_keyword.roengreenleaf.7.2}",
                      "${npc.topic_keyword.roengreenleaf.7.3}"),
                  "${npc.topic.roengreenleaf.7}",
                  List.of())),
          "RoenGreenleafNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 370, 65535, "1d36+27"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if (c.flag("__QUEST_FLAG_WILL_OF_ARTHERK_QUEST") >= 14
            && c.flag("__FLAG_USER_GOT_ROEN_RING") == 0) {

          c.flag("__FLAG_USER_GOT_ROEN_RING", 1);

          c.giveItem("ring_of_the_seraph");

          c.giveXp(50000);

          c.sayKey("npc.roen.ring.give");

        } else c.sayKey("npc.roen.welcome");
      }

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        if (text != null && text.toUpperCase(java.util.Locale.ROOT).equals("RING")) {

          c.sayKey(
              c.flag("__FLAG_USER_GOT_ROEN_RING") == 1
                  ? "npc.roen.ring.known"
                  : "npc.roen.ring.none");

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }
    };
  }

  public RoenGreenleaf(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
