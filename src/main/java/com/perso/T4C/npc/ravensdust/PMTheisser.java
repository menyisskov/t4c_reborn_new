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
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public final class PMTheisser extends ScriptedNpc {

  public static final String ID = "PMTheisser";

  public static final String DISPLAY_NAME = "${npc.pmtheisser}";

  public static final String SPRITE_BASE = "GoblinBoss";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.pmtheisser}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.pmtheisser.0.0}"),
                  "${npc.topic.pmtheisser.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.pmtheisser.1.0}"),
                  "${npc.topic.pmtheisser.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.pmtheisser.2.0}", "${npc.topic_keyword.pmtheisser.2.1}"),
                  "${npc.topic.pmtheisser.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.pmtheisser.3.0}", "${npc.topic_keyword.pmtheisser.3.1}"),
                  "${npc.topic.pmtheisser.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.pmtheisser.4.0}"),
                  "${npc.topic.pmtheisser.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.pmtheisser.5.0}"),
                  "${npc.topic.pmtheisser.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.pmtheisser.6.0}", "${npc.topic_keyword.pmtheisser.6.1}"),
                  "${npc.topic.pmtheisser.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.pmtheisser.7.0}"),
                  "${npc.topic.pmtheisser.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.pmtheisser.8.0}"),
                  "${npc.topic.pmtheisser.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.pmtheisser.9.0}"),
                  "${npc.topic.pmtheisser.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.pmtheisser.10.0}"),
                  "${npc.topic.pmtheisser.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.pmtheisser.11.0}"),
                  "${npc.topic.pmtheisser.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.pmtheisser.12.0}",
                      "${npc.topic_keyword.pmtheisser.12.1}",
                      "${npc.topic_keyword.pmtheisser.12.2}",
                      "${npc.topic_keyword.pmtheisser.12.3}",
                      "${npc.topic_keyword.pmtheisser.12.4}"),
                  "${npc.topic.pmtheisser.12}",
                  List.of())),
          "GrimishNPC",
          new NpcSpec.CombatProfile(45, 996, 60, 55, 55, 22, 550, 190, "1d64+49"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.trim().toUpperCase(Locale.ROOT);

        if (k.matches("VIEW\\s+\\d+")) {

          int id;

          try {

            id = Integer.parseInt(k.split("\\s+")[1]);

          } catch (RuntimeException e) {

            id = 0;
          }

          c.sayKey("npc.pmtheisser.view", id, c.globalFlag(id));

          return true;
        }

        if (k.equals("VIEW")) {

          c.sayKey("npc.pmtheisser.view.help");

          return true;
        }

        if (k.matches("PICK\\s+[-+]?\\d+")) {

          int pick;

          try {

            pick = Integer.parseInt(k.split("\\s+")[1]);

          } catch (RuntimeException e) {

            pick = 0;
          }

          if (pick < 1 || pick > 10) {

            c.sayKey("npc.pmtheisser.pick.range");

            return true;
          }

          if (c.player().getGold() < 1000) {

            c.sayKey("npc.pmtheisser.pick.poor");

            return true;
          }

          c.player().addGold(-1000);

          int matches = 0;

          int[] rolls = new int[5];

          for (int i = 0; i < rolls.length; i++) {

            rolls[i] = 1 + ThreadLocalRandom.current().nextInt(10);

            if (rolls[i] == pick) matches++;
          }

          c.say(
              String.format(
                  Locale.ROOT,
                  com.perso.T4C.i18n.I18n.resolve("npc.pmtheisser.pick.results"),
                  rolls[0],
                  rolls[1],
                  rolls[2],
                  rolls[3],
                  rolls[4]));

          if (matches == 1) c.giveGold(1000);
          else if (matches > 1) c.giveGold(1000 * matches * matches);

          return true;
        }

        if (k.equals("PICK")) {

          c.sayKey("npc.pmtheisser.pick.help");

          return true;
        }

        if (k.equals("START GOBLIN")) {

          if (c.globalFlag("GLOBAL_FLAG_GOBLIN_QUEST") == 1)
            c.sayKey("npc.pmtheisser.goblin.running");
          else {

            c.summon("DORKENROTSMELL", 2690, 905, 0);

            c.globalFlag("GLOBAL_FLAG_GOBLIN_QUEST", 1);
          }

          return true;
        }

        if (k.equals("SUICIDE")) {

          c.sayKey("npc.pmtheisser.suicide");

          c.damageNpc(c.npcCurrentHp());

          return true;
        }

        if (k.equals("CLOCK") || k.equals("ANOTHERCLOCK")) {

          LocalTime t = LocalTime.now();

          c.say(
              String.format(
                  Locale.ROOT,
                  com.perso.T4C.i18n.I18n.resolve(
                      k.equals("CLOCK") ? "npc.pmtheisser.clock" : "npc.pmtheisser.anotherclock"),
                  t.getHour(),
                  t.getMinute()));

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      @Override
      public void onAttack(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if (c.player().getGold() > 0) c.player().addGold(-1);
      }

      @Override
      public void onAttacked(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if (c.hasItem("healing_potion")) c.takeItem("healing_potion");
      }

      @Override
      public void onDeath(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        c.giveGold(10000);
      }
    };
  }

  public PMTheisser(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
