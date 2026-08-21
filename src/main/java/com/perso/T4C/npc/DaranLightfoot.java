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

@Spawn(type = "DaranLightfoot", x = 365, y = 820, z = 0, stationary = false, aggressive = false)
public final class DaranLightfoot extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "DaranLightfoot";

  public static final String DISPLAY_NAME = "${npc.daranlightfoot}";

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
              new NpcSpec.Part(BodyPart.WEAPON, "PupNormalSword"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.daranlightfoot}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranlightfoot.0.0}",
                      "${npc.topic_keyword.daranlightfoot.0.1}"),
                  "${npc.topic.daranlightfoot.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranlightfoot.1.0}",
                      "${npc.topic_keyword.daranlightfoot.1.1}"),
                  "${npc.topic.daranlightfoot.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranlightfoot.2.0}",
                      "${npc.topic_keyword.daranlightfoot.2.1}"),
                  "${npc.topic.daranlightfoot.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranlightfoot.3.0}",
                      "${npc.topic_keyword.daranlightfoot.3.1}"),
                  "${npc.topic.daranlightfoot.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranlightfoot.4.0}",
                      "${npc.topic_keyword.daranlightfoot.4.1}"),
                  "${npc.topic.daranlightfoot.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranlightfoot.5.0}",
                      "${npc.topic_keyword.daranlightfoot.5.1}",
                      "${npc.topic_keyword.daranlightfoot.5.2}"),
                  "${npc.topic.daranlightfoot.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranlightfoot.6.0}",
                      "${npc.topic_keyword.daranlightfoot.6.1}",
                      "${npc.topic_keyword.daranlightfoot.6.2}"),
                  "${npc.topic.daranlightfoot.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.daranlightfoot.7.0}"),
                  "${npc.topic.daranlightfoot.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranlightfoot.8.0}",
                      "${npc.topic_keyword.daranlightfoot.8.1}"),
                  "${npc.topic.daranlightfoot.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranlightfoot.9.0}",
                      "${npc.topic_keyword.daranlightfoot.9.1}"),
                  "${npc.topic.daranlightfoot.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranlightfoot.10.0}",
                      "${npc.topic_keyword.daranlightfoot.10.1}",
                      "${npc.topic_keyword.daranlightfoot.10.2}"),
                  "${npc.topic.daranlightfoot.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.daranlightfoot.11.0}"),
                  "${npc.topic.daranlightfoot.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.daranlightfoot.12.0}"),
                  "${npc.topic.daranlightfoot.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranlightfoot.13.0}",
                      "${npc.topic_keyword.daranlightfoot.13.1}"),
                  "${npc.topic.daranlightfoot.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.daranlightfoot.14.0}"),
                  "${npc.topic.daranlightfoot.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.daranlightfoot.15.0}"),
                  "${npc.topic.daranlightfoot.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.daranlightfoot.16.0}"),
                  "${npc.topic.daranlightfoot.16}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranlightfoot.17.0}",
                      "${npc.topic_keyword.daranlightfoot.17.1}"),
                  "${npc.topic.daranlightfoot.17}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranlightfoot.18.0}",
                      "${npc.topic_keyword.daranlightfoot.18.1}",
                      "${npc.topic_keyword.daranlightfoot.18.2}",
                      "${npc.topic_keyword.daranlightfoot.18.3}"),
                  "${npc.topic.daranlightfoot.18}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranlightfoot.19.0}",
                      "${npc.topic_keyword.daranlightfoot.19.1}"),
                  "${npc.topic.daranlightfoot.19}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranlightfoot.20.0}",
                      "${npc.topic_keyword.daranlightfoot.20.1}",
                      "${npc.topic_keyword.daranlightfoot.20.2}",
                      "${npc.topic_keyword.daranlightfoot.20.3}"),
                  "${npc.topic.daranlightfoot.20}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranlightfoot.21.0}",
                      "${npc.topic_keyword.daranlightfoot.21.1}"),
                  "${npc.topic.daranlightfoot.21}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranlightfoot.22.0}",
                      "${npc.topic_keyword.daranlightfoot.22.1}"),
                  "${npc.topic.daranlightfoot.22}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranlightfoot.23.0}",
                      "${npc.topic_keyword.daranlightfoot.23.1}"),
                  "${npc.topic.daranlightfoot.23}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranlightfoot.24.0}",
                      "${npc.topic_keyword.daranlightfoot.24.1}"),
                  "${npc.topic.daranlightfoot.24}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.daranlightfoot.25.0}"),
                  "${npc.topic.daranlightfoot.25}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.daranlightfoot.26.0}"),
                  "${npc.topic.daranlightfoot.26}",
                  List.of())),
          "DaranLightfootNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.contains("ASSASSIN") && k.contains("BLADE")) {

          if (c.itemCount("assassin_blade") >= 2) {

            c.sayKey("npc.daran.blades.ask");

            c.askYesNo("daran_blades");

          } else c.sayKey("npc.daran.blades.missing");

          return true;
        }

        if (k.equals("HELLO") || k.contains(" HI ")) {

          if (Math.random() < .2) c.systemMessageKey("message.daran.robbed");
          else if (c.player().getGold() > 10000) c.player().addGold(-1000);

          c.sayKey("npc.topic.daranlightfoot.0");

          return true;
        }

        if (k.equals("LEARN") || k.equals("TEACH")) {

          c.sayKey("npc.topic.daranlightfoot.13");

          c.openSkillLearning(
              java.util.List.of(
                  new com.perso.T4C.gui.screen.LearnScreen.TrainingOffer("rob", 1, 5000, true)));

          return true;
        }

        if (k.equals("TRAIN")) {

          int s = c.player().getSkillLevel("rob");

          if (s == 0) c.sayKey("npc.topic.daranlightfoot.14");
          else if (s < 100) {

            c.sayKey("npc.topic.daranlightfoot.13");

            c.openSkillLearning(
                java.util.List.of(
                    new com.perso.T4C.gui.screen.LearnScreen.TrainingOffer(
                        "rob", 100, 250, false)));

          } else c.sayKey("npc.daran.rob.max");

          return true;
        }

        if (k.contains("FUCK")
            || k.contains("SUCK")
            || k.contains("ASSHOLE")
            || k.contains(" ASS ")) {

          c.sayKey("npc.daran.bored");

          if (Math.random() < .2) c.systemMessageKey("message.daran.robbed");
          else if (c.player().getGold() > 10000) c.player().addGold(-5000);

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String state, boolean yes) {

        if (!"daran_blades".equals(state)) return false;

        if (yes && c.itemCount("assassin_blade") >= 2) {

          c.takeItem("assassin_blade");

          c.takeItem("assassin_blade");

          c.giveItem("broken_ethereal_key");

          c.giveXp(c.player().getLevel() * 1500);

          c.sayKey("npc.daran.blades.done");

        } else if (yes) c.sayKey("npc.daran.blades.dropped");

        return true;
      }
    };
  }

  public DaranLightfoot(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
