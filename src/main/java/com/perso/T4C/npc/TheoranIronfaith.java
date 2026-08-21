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

@Spawn(type = "TheoranIronfaith", x = 565, y = 925, z = 0, stationary = false, aggressive = false)
public final class TheoranIronfaith extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "TheoranIronfaith";

  public static final String DISPLAY_NAME = "${npc.theoranironfaith}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupWhiteRobe"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLegsClothSet1"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupMorningStar")),
          0,
          List.of(),
          "${npc.welcome.theoranironfaith}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.theoranironfaith.0.0}",
                      "${npc.topic_keyword.theoranironfaith.0.1}"),
                  "${npc.topic.theoranironfaith.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.theoranironfaith.1.0}"),
                  "${npc.topic.theoranironfaith.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.theoranironfaith.2.0}",
                      "${npc.topic_keyword.theoranironfaith.2.1}",
                      "${npc.topic_keyword.theoranironfaith.2.2}"),
                  "${npc.topic.theoranironfaith.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.theoranironfaith.3.0}",
                      "${npc.topic_keyword.theoranironfaith.3.1}",
                      "${npc.topic_keyword.theoranironfaith.3.2}"),
                  "${npc.topic.theoranironfaith.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.theoranironfaith.4.0}"),
                  "${npc.topic.theoranironfaith.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.theoranironfaith.5.0}",
                      "${npc.topic_keyword.theoranironfaith.5.1}"),
                  "${npc.topic.theoranironfaith.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.theoranironfaith.6.0}",
                      "${npc.topic_keyword.theoranironfaith.6.1}"),
                  "${npc.topic.theoranironfaith.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.theoranironfaith.7.0}",
                      "${npc.topic_keyword.theoranironfaith.7.1}"),
                  "${npc.topic.theoranironfaith.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.theoranironfaith.8.0}"),
                  "${npc.topic.theoranironfaith.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.theoranironfaith.9.0}"),
                  "${npc.topic.theoranironfaith.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.theoranironfaith.10.0}",
                      "${npc.topic_keyword.theoranironfaith.10.1}"),
                  "${npc.topic.theoranironfaith.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.theoranironfaith.11.0}"),
                  "${npc.topic.theoranironfaith.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.theoranironfaith.12.0}"),
                  "${npc.topic.theoranironfaith.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.theoranironfaith.13.0}"),
                  "${npc.topic.theoranironfaith.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.theoranironfaith.14.0}"),
                  "${npc.topic.theoranironfaith.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.theoranironfaith.15.0}",
                      "${npc.topic_keyword.theoranironfaith.15.1}"),
                  "${npc.topic.theoranironfaith.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.theoranironfaith.16.0}",
                      "${npc.topic_keyword.theoranironfaith.16.1}"),
                  "${npc.topic.theoranironfaith.16}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.theoranironfaith.17.0}",
                      "${npc.topic_keyword.theoranironfaith.17.1}"),
                  "${npc.topic.theoranironfaith.17}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.theoranironfaith.18.0}"),
                  "${npc.topic.theoranironfaith.18}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.theoranironfaith.19.0}"),
                  "${npc.topic.theoranironfaith.19}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.theoranironfaith.20.0}"),
                  "${npc.topic.theoranironfaith.20}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.theoranironfaith.21.0}",
                      "${npc.topic_keyword.theoranironfaith.21.1}"),
                  "${npc.topic.theoranironfaith.21}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.theoranironfaith.22.0}"),
                  "${npc.topic.theoranironfaith.22}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.theoranironfaith.23.0}"),
                  "${npc.topic.theoranironfaith.23}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.theoranironfaith.24.0}",
                      "${npc.topic_keyword.theoranironfaith.24.1}",
                      "${npc.topic_keyword.theoranironfaith.24.2}",
                      "${npc.topic_keyword.theoranironfaith.24.3}",
                      "${npc.topic_keyword.theoranironfaith.24.4}"),
                  "${npc.topic.theoranironfaith.24}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.theoranironfaith.25.0}",
                      "${npc.topic_keyword.theoranironfaith.25.1}",
                      "${npc.topic_keyword.theoranironfaith.25.2}",
                      "${npc.topic_keyword.theoranironfaith.25.3}"),
                  "${npc.topic.theoranironfaith.25}",
                  List.of())),
          "TheoranIronfaithNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if (c.flag("__FLAG_FOLLOWER_OF_OGRIMAR") == 1) c.sayKey("npc.theoran.ogrimar");
        else if (c.flag("__QUEST_FIXED_ALIGNMENT") == -1) c.sayKey("npc.theoran.dark");
        else c.sayKey("npc.theoran.welcome");
      }

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.contains("RING")
            && k.contains("PURE")
            && k.contains("FAITH")
            && c.itemCount("ring_of_faith") > 0
            && c.hasItem("grail_of_purity")
            && c.itemCount("flask_of_holy_water") >= 2) {

          c.askYesNo("theoran_ring");

          return true;
        }

        if (k.contains("HOLY") && k.contains("WATER") && c.hasItem("flask_of_crystal_water")) {

          c.askYesNo("theoran_water");

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String s, boolean yes) {

        if ("theoran_ring".equals(s)) {

          if (yes) {

            c.takeItem("ring_of_faith");

            c.takeItem("grail_of_purity");

            for (int i = 0; i < 2; i++) c.takeItem("flask_of_holy_water");

            c.giveItem("ring_of_pure_faith");

            c.sayKey("npc.theoran.ring.ok");
          }

          return true;
        }

        if ("theoran_water".equals(s)) {

          if (yes) {

            c.takeItem("flask_of_crystal_water");

            c.giveItem("flask_of_holy_water");

            c.sayKey("npc.theoran.water.ok");
          }

          return true;
        }

        return false;
      }
    };
  }

  public TheoranIronfaith(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
