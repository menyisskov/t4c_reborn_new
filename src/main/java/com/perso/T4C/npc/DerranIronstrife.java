package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.StaticDialogueBehavior;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "DerranIronstrife", x = 2902, y = 255, z = 4, stationary = false, aggressive = false)
public final class DerranIronstrife extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "DerranIronstrife";

  public static final String DISPLAY_NAME = "${npc.derranironstrife}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupChainMailBody"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupChainMailLegs"),
              new NpcSpec.Part(BodyPart.HEAD, "PupChainMailCoif"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupBattleSword"),
              new NpcSpec.Part(BodyPart.SHIELD, "PupBarossaShield"),
              new NpcSpec.Part(BodyPart.BACK, "PupRedCape"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.derranironstrife}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.derranironstrife.0.0}",
                      "${npc.topic_keyword.derranironstrife.0.1}"),
                  "${npc.topic.derranironstrife.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.derranironstrife.1.0}",
                      "${npc.topic_keyword.derranironstrife.1.1}"),
                  "${npc.topic.derranironstrife.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.derranironstrife.2.0}"),
                  "${npc.topic.derranironstrife.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.derranironstrife.3.0}",
                      "${npc.topic_keyword.derranironstrife.3.1}",
                      "${npc.topic_keyword.derranironstrife.3.2}"),
                  "${npc.topic.derranironstrife.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.derranironstrife.4.0}",
                      "${npc.topic_keyword.derranironstrife.4.1}",
                      "${npc.topic_keyword.derranironstrife.4.2}"),
                  "${npc.topic.derranironstrife.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.derranironstrife.5.0}"),
                  "${npc.topic.derranironstrife.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.derranironstrife.6.0}"),
                  "${npc.topic.derranironstrife.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.derranironstrife.7.0}"),
                  "${npc.topic.derranironstrife.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.derranironstrife.8.0}",
                      "${npc.topic_keyword.derranironstrife.8.1}"),
                  "${npc.topic.derranironstrife.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.derranironstrife.9.0}"),
                  "${npc.topic.derranironstrife.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.derranironstrife.10.0}"),
                  "${npc.topic.derranironstrife.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.derranironstrife.11.0}",
                      "${npc.topic_keyword.derranironstrife.11.1}",
                      "${npc.topic_keyword.derranironstrife.11.2}",
                      "${npc.topic_keyword.derranironstrife.11.3}"),
                  "${npc.topic.derranironstrife.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.derranironstrife.12.0}",
                      "${npc.topic_keyword.derranironstrife.12.1}",
                      "${npc.topic_keyword.derranironstrife.12.2}",
                      "${npc.topic_keyword.derranironstrife.12.3}",
                      "${npc.topic_keyword.derranironstrife.12.4}"),
                  "${npc.topic.derranironstrife.12}",
                  List.of())),
          "Normal_Guard",
          new NpcSpec.CombatProfile(100, 1000000, 50, 46, 46, 1000000, 370, 65535, "1d36+27"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if (c.flag("__DERRAN_KNOWS_YOU") < 1) c.askYesNo("knowledge");

        StaticDialogueBehavior.INSTANCE.onConversationStart(c);
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String state, boolean yes) {

        if (!"knowledge".equals(state)) return false;

        if (yes) c.flag("__DERRAN_KNOWS_YOU", 1);

        return true;
      }

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }
    };
  }

  public DerranIronstrife(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
