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

@Spawn(type = "Idas", x = 1378, y = 2387, z = 0, stationary = false, aggressive = false)
public final class Idas extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Idas";

  public static final String DISPLAY_NAME = "${npc.idas}";

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
          "${npc.welcome.idas}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.idas.0.0}", "${npc.topic_keyword.idas.0.1}"),
                  "${npc.topic.idas.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.idas.1.0}"), "${npc.topic.idas.1}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.idas.2.0}",
                      "${npc.topic_keyword.idas.2.1}",
                      "${npc.topic_keyword.idas.2.2}"),
                  "${npc.topic.idas.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.idas.3.0}"), "${npc.topic.idas.3}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.idas.4.0}"), "${npc.topic.idas.4}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.idas.5.0}"), "${npc.topic.idas.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.idas.6.0}", "${npc.topic_keyword.idas.6.1}"),
                  "${npc.topic.idas.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.idas.7.0}", "${npc.topic_keyword.idas.7.1}"),
                  "${npc.topic.idas.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.idas.8.0}"), "${npc.topic.idas.8}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.idas.9.0}", "${npc.topic_keyword.idas.9.1}"),
                  "${npc.topic.idas.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.idas.10.0}"), "${npc.topic.idas.10}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.idas.11.0}",
                      "${npc.topic_keyword.idas.11.1}",
                      "${npc.topic_keyword.idas.11.2}",
                      "${npc.topic_keyword.idas.11.3}",
                      "${npc.topic_keyword.idas.11.4}"),
                  "${npc.topic.idas.11}",
                  List.of())),
          "Normal_Guard",
          new NpcSpec.CombatProfile(100, 1000000, 50, 46, 46, 1000000, 370, 65535, "1d36+27"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(NpcBehaviorContext c) {

        long now = System.currentTimeMillis() / 1000L;

        if (c.globalFlag("GLOBAL_BANK_HAS_BEEN_ROBBED") > now) {

          if (c.globalFlag("GLOBAL_BANK_HAS_BEEN_ROBBED_BY") == c.flag("__FLAG_SHADEEN_PLAYER_B")) {

            while (c.hasItem("light_healing_potion")) c.takeItem("light_healing_potion");

            while (c.hasItem("healing_potion")) c.takeItem("healing_potion");

            c.sayKey("npc.idas.thief");

            c.teleport(209, 2338, 1);

          } else c.sayKey("npc.idas.bank.robbed");

        } else {

          c.flag("__FLAG_THIEF", 0);

          c.sayKey("npc.idas.welcome");
        }
      }
    };
  }

  public Idas(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
