package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(
    type = "NafuhrBloodhammer",
    x = 1446,
    y = 2454,
    z = 0,
    stationary = false,
    aggressive = false)
public final class NafuhrBloodhammer extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "NafuhrBloodhammer";

  public static final String DISPLAY_NAME = "${npc.nafuhrbloodhammer}";

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
              new NpcSpec.Part(BodyPart.LEGS, "PupLegsClothSet1"),
              new NpcSpec.Part(BodyPart.HEAD, "PupChainMailCoif"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.nafuhrbloodhammer}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.nafuhrbloodhammer.0.0}",
                      "${npc.topic_keyword.nafuhrbloodhammer.0.1}"),
                  "${npc.topic.nafuhrbloodhammer.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.nafuhrbloodhammer.1.0}",
                      "${npc.topic_keyword.nafuhrbloodhammer.1.1}",
                      "${npc.topic_keyword.nafuhrbloodhammer.1.2}"),
                  "${npc.topic.nafuhrbloodhammer.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.nafuhrbloodhammer.2.0}"),
                  "${npc.topic.nafuhrbloodhammer.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.nafuhrbloodhammer.3.0}",
                      "${npc.topic_keyword.nafuhrbloodhammer.3.1}"),
                  "${npc.topic.nafuhrbloodhammer.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.nafuhrbloodhammer.4.0}"),
                  "${npc.topic.nafuhrbloodhammer.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.nafuhrbloodhammer.5.0}",
                      "${npc.topic_keyword.nafuhrbloodhammer.5.1}"),
                  "${npc.topic.nafuhrbloodhammer.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.nafuhrbloodhammer.6.0}",
                      "${npc.topic_keyword.nafuhrbloodhammer.6.1}"),
                  "${npc.topic.nafuhrbloodhammer.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.nafuhrbloodhammer.7.0}",
                      "${npc.topic_keyword.nafuhrbloodhammer.7.1}"),
                  "${npc.topic.nafuhrbloodhammer.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.nafuhrbloodhammer.8.0}"),
                  "${npc.topic.nafuhrbloodhammer.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.nafuhrbloodhammer.9.0}"),
                  "${npc.topic.nafuhrbloodhammer.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.nafuhrbloodhammer.10.0}",
                      "${npc.topic_keyword.nafuhrbloodhammer.10.1}",
                      "${npc.topic_keyword.nafuhrbloodhammer.10.2}"),
                  "${npc.topic.nafuhrbloodhammer.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.nafuhrbloodhammer.11.0}"),
                  "${npc.topic.nafuhrbloodhammer.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.nafuhrbloodhammer.12.0}"),
                  "${npc.topic.nafuhrbloodhammer.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.nafuhrbloodhammer.13.0}",
                      "${npc.topic_keyword.nafuhrbloodhammer.13.1}"),
                  "${npc.topic.nafuhrbloodhammer.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.nafuhrbloodhammer.14.0}"),
                  "${npc.topic.nafuhrbloodhammer.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.nafuhrbloodhammer.15.0}"),
                  "${npc.topic.nafuhrbloodhammer.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.nafuhrbloodhammer.16.0}",
                      "${npc.topic_keyword.nafuhrbloodhammer.16.1}",
                      "${npc.topic_keyword.nafuhrbloodhammer.16.2}"),
                  "${npc.topic.nafuhrbloodhammer.16}",
                  List.of())),
          "ShopKeeper_set_two",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 370, 65535, "1d36+27"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if (c.player().getGold() < 2000) c.sayKey("npc.nafuhr.poor");
      }

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String keyword) {

        String k = keyword == null ? "" : keyword.toUpperCase(java.util.Locale.ROOT);

        if (k.contains("DRAGON") && k.contains("TOOTH") || k.equals("TEETH")) {

          if (c.hasItem("tiger_tooth") && c.flag("__FLAG_SIGFRIED_SCAM") == 2)
            c.sayKey("npc.nafuhr.tooth.tiger");
          else if (c.hasItem("dragon_tooth") && c.flag("__FLAG_SIGFRIED_SCAM") >= 1)
            c.sayKey("npc.nafuhr.tooth.fake");
          else if (c.hasItem("dragon_tooth")) {

            c.flag("__FLAG_SIGFRIED_SCAM", 1);

            c.sayKey("npc.nafuhr.tooth.small");

          } else c.sayKey("npc.nafuhr.tooth.none");

          return true;
        }

        return false;
      }
    };
  }

  public NafuhrBloodhammer(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
