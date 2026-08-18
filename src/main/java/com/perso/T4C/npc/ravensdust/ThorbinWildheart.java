package com.perso.T4C.npc.ravensdust;

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
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "ThorbinWildheart", x = 1507, y = 2544, z = 0, stationary = false, aggressive = false)
public final class ThorbinWildheart extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "ThorbinWildheart";

  public static final String DISPLAY_NAME = "${npc.thorbinwildheart}";

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
              new NpcSpec.Part(BodyPart.BOOT, "PupBlackLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLeatherPants"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.thorbinwildheart}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.thorbinwildheart.0.0}",
                      "${npc.topic_keyword.thorbinwildheart.0.1}"),
                  "${npc.topic.thorbinwildheart.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.thorbinwildheart.1.0}",
                      "${npc.topic_keyword.thorbinwildheart.1.1}"),
                  "${npc.topic.thorbinwildheart.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.thorbinwildheart.2.0}",
                      "${npc.topic_keyword.thorbinwildheart.2.1}",
                      "${npc.topic_keyword.thorbinwildheart.2.2}"),
                  "${npc.topic.thorbinwildheart.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.thorbinwildheart.3.0}",
                      "${npc.topic_keyword.thorbinwildheart.3.1}"),
                  "${npc.topic.thorbinwildheart.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.thorbinwildheart.4.0}",
                      "${npc.topic_keyword.thorbinwildheart.4.1}"),
                  "${npc.topic.thorbinwildheart.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.thorbinwildheart.5.0}",
                      "${npc.topic_keyword.thorbinwildheart.5.1}"),
                  "${npc.topic.thorbinwildheart.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.thorbinwildheart.6.0}"),
                  "${npc.topic.thorbinwildheart.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.thorbinwildheart.7.0}"),
                  "${npc.topic.thorbinwildheart.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.thorbinwildheart.8.0}",
                      "${npc.topic_keyword.thorbinwildheart.8.1}",
                      "${npc.topic_keyword.thorbinwildheart.8.2}",
                      "${npc.topic_keyword.thorbinwildheart.8.3}"),
                  "${npc.topic.thorbinwildheart.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.thorbinwildheart.9.0}"),
                  "${npc.topic.thorbinwildheart.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.thorbinwildheart.10.0}"),
                  "${npc.topic.thorbinwildheart.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.thorbinwildheart.11.0}"),
                  "${npc.topic.thorbinwildheart.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.thorbinwildheart.12.0}",
                      "${npc.topic_keyword.thorbinwildheart.12.1}"),
                  "${npc.topic.thorbinwildheart.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.thorbinwildheart.13.0}"),
                  "${npc.topic.thorbinwildheart.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.thorbinwildheart.14.0}",
                      "${npc.topic_keyword.thorbinwildheart.14.1}",
                      "${npc.topic_keyword.thorbinwildheart.14.2}",
                      "${npc.topic_keyword.thorbinwildheart.14.3}"),
                  "${npc.topic.thorbinwildheart.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.thorbinwildheart.15.0}",
                      "${npc.topic_keyword.thorbinwildheart.15.1}",
                      "${npc.topic_keyword.thorbinwildheart.15.2}",
                      "${npc.topic_keyword.thorbinwildheart.15.3}",
                      "${npc.topic_keyword.thorbinwildheart.15.4}"),
                  "${npc.topic.thorbinwildheart.15}",
                  List.of())),
          "ThorbinWildheartNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 370, 65535, "1d36+27"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.contains("BALDRIC") || k.contains("SILVERKNIFE")) {

          if (c.hasItem("baldric_drum")) {

            c.sayKey("npc.thorbin.drum.ask");

            c.askYesNo("thorbin_drum");

          } else c.sayKey("npc.thorbin.drum.need");

          return true;
        }

        if (k.contains("DRINK") || k.equals("BUY") || k.contains("BEER") || k.contains("ALE")) {

          c.sayKey("npc.thorbin.beer.ask");

          c.askYesNo("thorbin_beer");

          return true;
        }

        if (k.equals("KARMA")) {

          c.sayKey("npc.thorbin.karma.ask");

          c.askYesNo("thorbin_karma");

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String s, boolean yes) {

        if ("thorbin_drum".equals(s)) {

          if (yes && c.hasItem("baldric_drum")) {

            c.takeItem("baldric_drum");

            if (Math.random() < .333) c.giveItem("cape_of_fire_resistance");
            else c.giveItem("empty_beer_mug");

            c.giveXp(3000);

            c.sayKey("npc.thorbin.drum.done");
          }

          return true;
        }

        if ("thorbin_beer".equals(s)) {

          if (yes && c.player().getGold() >= 10) {

            c.player().addGold(-10);

            c.giveItem("empty_beer_mug");

            c.sayKey("npc.thorbin.beer.done");
          }

          return true;
        }

        if ("thorbin_karma".equals(s)) {

          if (yes && c.player().getGold() >= 250) {

            c.player().addGold(-250);

            int k = c.karma();

            c.sayKey(
                k >= 400
                    ? "npc.thorbin.karma.pure"
                    : k >= 300
                        ? "npc.thorbin.karma.blessed"
                        : k >= 200
                            ? "npc.thorbin.karma.redeemed"
                            : k >= 100
                                ? "npc.thorbin.karma.good"
                                : k > -100
                                    ? "npc.thorbin.karma.neutral"
                                    : k <= -400
                                        ? "npc.thorbin.karma.bane"
                                        : k <= -300
                                            ? "npc.thorbin.karma.cursed"
                                            : k <= -200
                                                ? "npc.thorbin.karma.malignant"
                                                : k <= -100
                                                    ? "npc.thorbin.karma.evil"
                                                    : "npc.thorbin.karma.unknown");
          }

          return true;
        }

        return false;
      }
    };
  }

  public ThorbinWildheart(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
