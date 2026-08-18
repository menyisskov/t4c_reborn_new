package com.perso.T4C.npc.stoneheim;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.StaticDialogueBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "KhanWulgworth", x = 2165, y = 140, z = 2, stationary = false, aggressive = false)
public final class KhanWulgworth extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "KhanWulgworth";

  public static final String DISPLAY_NAME = "${npc.khanwulgworth}";

  public static final String SPRITE_BASE = "64kSkavenPeon";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.khanwulgworth}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.khanwulgworth.0.0}",
                      "${npc.topic_keyword.khanwulgworth.0.1}"),
                  "${npc.topic.khanwulgworth.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.khanwulgworth.1.0}",
                      "${npc.topic_keyword.khanwulgworth.1.1}",
                      "${npc.topic_keyword.khanwulgworth.1.2}"),
                  "${npc.topic.khanwulgworth.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.khanwulgworth.2.0}",
                      "${npc.topic_keyword.khanwulgworth.2.1}"),
                  "${npc.topic.khanwulgworth.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.khanwulgworth.3.0}",
                      "${npc.topic_keyword.khanwulgworth.3.1}"),
                  "${npc.topic.khanwulgworth.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.khanwulgworth.4.0}",
                      "${npc.topic_keyword.khanwulgworth.4.1}"),
                  "${npc.topic.khanwulgworth.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.khanwulgworth.5.0}",
                      "${npc.topic_keyword.khanwulgworth.5.1}",
                      "${npc.topic_keyword.khanwulgworth.5.2}"),
                  "${npc.topic.khanwulgworth.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.khanwulgworth.6.0}"), null, List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.khanwulgworth.7.0}",
                      "${npc.topic_keyword.khanwulgworth.7.1}",
                      "${npc.topic_keyword.khanwulgworth.7.2}",
                      "${npc.topic_keyword.khanwulgworth.7.3}",
                      "${npc.topic_keyword.khanwulgworth.7.4}"),
                  "${npc.topic.khanwulgworth.7}",
                  List.of())),
          "KhanWulgworthNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.contains("SKRAUGBASHOR") && k.contains("MACE")) {

          if (c.itemCount("skraugbashor_mace") >= 5) {

            c.sayKey("npc.khan.mace.ask");

            c.askYesNo("khan_mace");

          } else c.sayKey("npc.khan.mace.info");

          return true;
        }

        if (k.contains("VILLAIN") && k.contains("SKULL")) {

          if (c.flag("__FLAG_FOLLOWER_OF_OGRIMAR") == 1) c.sayKey("npc.khan.skull");
          else {

            c.sayKey("npc.khan.skull.denied");

            c.npc().provoke();
          }

          return true;
        }

        if (k.contains("BELT") && k.contains("STRENGTH")) {

          c.sayKey("npc.khan.belt");

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String s, boolean yes) {

        if (!"khan_mace".equals(s)) return false;

        if (yes && c.itemCount("skraugbashor_mace") >= 5) {

          for (int i = 0; i < 5; i++) c.takeItem("skraugbashor_mace");

          c.giveItem("belt_of_skraug_strength");

          c.giveXp(c.player().getLevel() * 1000);

          c.sayKey("npc.khan.mace.done");
        }

        return true;
      }
    };
  }

  public KhanWulgworth(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
