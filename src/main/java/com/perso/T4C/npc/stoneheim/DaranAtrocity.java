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
import java.util.List;

public final class DaranAtrocity extends ScriptedNpc {

  public static final String ID = "DaranAtrocity";

  public static final String DISPLAY_NAME = "${npc.daranatrocity}";

  public static final String SPRITE_BASE = "Atrocity";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.daranatrocity}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranatrocity.0.0}",
                      "${npc.topic_keyword.daranatrocity.0.1}"),
                  "${npc.topic.daranatrocity.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranatrocity.1.0}",
                      "${npc.topic_keyword.daranatrocity.1.1}"),
                  "${npc.topic.daranatrocity.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranatrocity.2.0}",
                      "${npc.topic_keyword.daranatrocity.2.1}"),
                  "${npc.topic.daranatrocity.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranatrocity.3.0}",
                      "${npc.topic_keyword.daranatrocity.3.1}"),
                  "${npc.topic.daranatrocity.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranatrocity.4.0}",
                      "${npc.topic_keyword.daranatrocity.4.1}"),
                  "${npc.topic.daranatrocity.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranatrocity.5.0}",
                      "${npc.topic_keyword.daranatrocity.5.1}",
                      "${npc.topic_keyword.daranatrocity.5.2}"),
                  "${npc.topic.daranatrocity.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranatrocity.6.0}",
                      "${npc.topic_keyword.daranatrocity.6.1}",
                      "${npc.topic_keyword.daranatrocity.6.2}"),
                  "${npc.topic.daranatrocity.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.daranatrocity.7.0}"),
                  "${npc.topic.daranatrocity.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranatrocity.8.0}",
                      "${npc.topic_keyword.daranatrocity.8.1}"),
                  "${npc.topic.daranatrocity.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranatrocity.9.0}",
                      "${npc.topic_keyword.daranatrocity.9.1}"),
                  "${npc.topic.daranatrocity.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranatrocity.10.0}",
                      "${npc.topic_keyword.daranatrocity.10.1}",
                      "${npc.topic_keyword.daranatrocity.10.2}"),
                  "${npc.topic.daranatrocity.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.daranatrocity.11.0}"),
                  "${npc.topic.daranatrocity.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.daranatrocity.12.0}"),
                  "${npc.topic.daranatrocity.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranatrocity.13.0}",
                      "${npc.topic_keyword.daranatrocity.13.1}"),
                  "${npc.topic.daranatrocity.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.daranatrocity.14.0}"),
                  "${npc.topic.daranatrocity.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.daranatrocity.15.0}"),
                  "${npc.topic.daranatrocity.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.daranatrocity.16.0}"),
                  "${npc.topic.daranatrocity.16}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranatrocity.17.0}",
                      "${npc.topic_keyword.daranatrocity.17.1}"),
                  "${npc.topic.daranatrocity.17}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranatrocity.18.0}",
                      "${npc.topic_keyword.daranatrocity.18.1}",
                      "${npc.topic_keyword.daranatrocity.18.2}",
                      "${npc.topic_keyword.daranatrocity.18.3}"),
                  "${npc.topic.daranatrocity.18}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranatrocity.19.0}",
                      "${npc.topic_keyword.daranatrocity.19.1}",
                      "${npc.topic_keyword.daranatrocity.19.2}",
                      "${npc.topic_keyword.daranatrocity.19.3}"),
                  "${npc.topic.daranatrocity.19}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranatrocity.20.0}",
                      "${npc.topic_keyword.daranatrocity.20.1}"),
                  "${npc.topic.daranatrocity.20}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranatrocity.21.0}",
                      "${npc.topic_keyword.daranatrocity.21.1}"),
                  "${npc.topic.daranatrocity.21}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranatrocity.22.0}",
                      "${npc.topic_keyword.daranatrocity.22.1}"),
                  "${npc.topic.daranatrocity.22}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranatrocity.23.0}",
                      "${npc.topic_keyword.daranatrocity.23.1}",
                      "${npc.topic_keyword.daranatrocity.23.2}"),
                  "${npc.topic.daranatrocity.23}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranatrocity.24.0}",
                      "${npc.topic_keyword.daranatrocity.24.1}"),
                  "${npc.topic.daranatrocity.24}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.daranatrocity.25.0}"),
                  "${npc.topic.daranatrocity.25}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.daranatrocity.26.0}",
                      "${npc.topic_keyword.daranatrocity.26.1}",
                      "${npc.topic_keyword.daranatrocity.26.2}"),
                  "${npc.topic.daranatrocity.26}",
                  List.of())),
          "DaranAtrocityNPC",
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

  public DaranAtrocity(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
