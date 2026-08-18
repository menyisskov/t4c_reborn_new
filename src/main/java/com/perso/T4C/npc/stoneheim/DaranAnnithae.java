package com.perso.T4C.npc.stoneheim;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.gui.screen.LearnScreen;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.behavior.StaticDialogueBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class DaranAnnithae extends ScriptedNpc {

  public static final String ID = "DaranAnnithae";

  public static final String DISPLAY_NAME = "${npc.daranannithae}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "WoClothBody"),
              new NpcSpec.Part(BodyPart.BOOT, "WoLeatherBoots"),
              new NpcSpec.Part(BodyPart.ROBELEGS, "WoClothRobe")),
          0,
          List.of(),
          "${npc.welcome.daranannithae}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranannithae.0.0}",
                      "${npc.topic_keyword.daranannithae.0.1}"),
                  "${npc.topic.daranannithae.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranannithae.1.0}",
                      "${npc.topic_keyword.daranannithae.1.1}"),
                  "${npc.topic.daranannithae.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranannithae.2.0}",
                      "${npc.topic_keyword.daranannithae.2.1}"),
                  "${npc.topic.daranannithae.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranannithae.3.0}",
                      "${npc.topic_keyword.daranannithae.3.1}"),
                  "${npc.topic.daranannithae.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranannithae.4.0}",
                      "${npc.topic_keyword.daranannithae.4.1}"),
                  "${npc.topic.daranannithae.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranannithae.5.0}",
                      "${npc.topic_keyword.daranannithae.5.1}",
                      "${npc.topic_keyword.daranannithae.5.2}"),
                  "${npc.topic.daranannithae.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranannithae.6.0}",
                      "${npc.topic_keyword.daranannithae.6.1}",
                      "${npc.topic_keyword.daranannithae.6.2}"),
                  "${npc.topic.daranannithae.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.daranannithae.7.0}"),
                  "${npc.topic.daranannithae.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranannithae.8.0}",
                      "${npc.topic_keyword.daranannithae.8.1}"),
                  "${npc.topic.daranannithae.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranannithae.9.0}",
                      "${npc.topic_keyword.daranannithae.9.1}"),
                  "${npc.topic.daranannithae.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranannithae.10.0}",
                      "${npc.topic_keyword.daranannithae.10.1}",
                      "${npc.topic_keyword.daranannithae.10.2}"),
                  "${npc.topic.daranannithae.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.daranannithae.11.0}"),
                  "${npc.topic.daranannithae.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.daranannithae.12.0}"),
                  "${npc.topic.daranannithae.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranannithae.13.0}",
                      "${npc.topic_keyword.daranannithae.13.1}"),
                  "${npc.topic.daranannithae.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.daranannithae.14.0}"),
                  "${npc.topic.daranannithae.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.daranannithae.15.0}"),
                  "${npc.topic.daranannithae.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.daranannithae.16.0}"),
                  "${npc.topic.daranannithae.16}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranannithae.17.0}",
                      "${npc.topic_keyword.daranannithae.17.1}"),
                  "${npc.topic.daranannithae.17}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranannithae.18.0}",
                      "${npc.topic_keyword.daranannithae.18.1}",
                      "${npc.topic_keyword.daranannithae.18.2}",
                      "${npc.topic_keyword.daranannithae.18.3}"),
                  "${npc.topic.daranannithae.18}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranannithae.19.0}",
                      "${npc.topic_keyword.daranannithae.19.1}",
                      "${npc.topic_keyword.daranannithae.19.2}",
                      "${npc.topic_keyword.daranannithae.19.3}"),
                  "${npc.topic.daranannithae.19}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranannithae.20.0}",
                      "${npc.topic_keyword.daranannithae.20.1}"),
                  "${npc.topic.daranannithae.20}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranannithae.21.0}",
                      "${npc.topic_keyword.daranannithae.21.1}"),
                  "${npc.topic.daranannithae.21}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranannithae.22.0}",
                      "${npc.topic_keyword.daranannithae.22.1}"),
                  "${npc.topic.daranannithae.22}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranannithae.23.0}",
                      "${npc.topic_keyword.daranannithae.23.1}"),
                  "${npc.topic.daranannithae.23}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.daranannithae.24.0}"),
                  "${npc.topic.daranannithae.24}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranannithae.25.0}",
                      "${npc.topic_keyword.daranannithae.25.1}"),
                  "${npc.topic.daranannithae.25}",
                  List.of())),
          "DaranAnnithaeNPC",
          new NpcSpec.CombatProfile(100, 1000000, 65, 67, 63, 1000000, 0, 65535, "1d3"));

  private static final class DaranBehavior implements NpcBehavior {

    @Override
    public boolean onKeyword(NpcBehaviorContext c, String text) {

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

        c.sayKey("npc.welcome.daranlightfoot");

        return true;
      }

      if (k.equals("LEARN") || k.equals("TEACH")) {

        c.sayKey("npc.topic.daranlightfoot.13");

        c.openSkillLearning(List.of(new LearnScreen.TrainingOffer("rob", 1, 5000, true)));

        return true;
      }

      if (k.equals("TRAIN")) {

        int s = c.player().getSkillLevel("rob");

        if (s == 0) c.sayKey("npc.topic.daranlightfoot.14");
        else if (s < 100) {

          c.sayKey("npc.topic.daranlightfoot.13");

          c.openSkillLearning(List.of(new LearnScreen.TrainingOffer("rob", 100, 250, false)));

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
    public boolean onYesNo(NpcBehaviorContext c, String state, boolean yes) {

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
  }

  @Override
  protected NpcBehavior javaBehavior() {

    return new DaranBehavior();
  }

  public DaranAnnithae(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
