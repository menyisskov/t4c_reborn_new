package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "SebanForgelot", x = 2172, y = 500, z = 0, stationary = false, aggressive = false)
public final class SebanForgelot extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "SebanForgelot";

  public static final String DISPLAY_NAME = "${npc.sebanforgelot}";

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
          "${npc.welcome.sebanforgelot}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.sebanforgelot.0.0}"),
                  "${npc.topic.sebanforgelot.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.sebanforgelot.1.0}"),
                  "${npc.topic.sebanforgelot.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.sebanforgelot.2.0}"),
                  "${npc.topic.sebanforgelot.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.sebanforgelot.3.0}"),
                  "${npc.topic.sebanforgelot.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.sebanforgelot.4.0}"),
                  "${npc.topic.sebanforgelot.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.sebanforgelot.5.0}"),
                  "${npc.topic.sebanforgelot.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.sebanforgelot.6.0}"),
                  "${npc.topic.sebanforgelot.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.sebanforgelot.7.0}",
                      "${npc.topic_keyword.sebanforgelot.7.1}",
                      "${npc.topic_keyword.sebanforgelot.7.2}"),
                  "${npc.topic.sebanforgelot.7}",
                  List.of())),
          "ShopKeeper",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(NpcBehaviorContext c) {

        c.sayKey(
            java.time.LocalTime.now().getHour() >= 6 && java.time.LocalTime.now().getHour() < 18
                ? "npc.seban.day"
                : "npc.seban.night");
      }
    };
  }

  public SebanForgelot(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
