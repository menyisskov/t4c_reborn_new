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

@Spawn(type = "Mordenthal", x = 342, y = 248, z = 0, stationary = false, aggressive = false)
public final class Mordenthal extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Mordenthal";

  public static final String DISPLAY_NAME = "${npc.mordenthal}";

  public static final String SPRITE_BASE = "BlackWarrior";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.mordenthal}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mordenthal.0.0}", "${npc.topic_keyword.mordenthal.0.1}"),
                  "${npc.topic.mordenthal.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mordenthal.1.0}",
                      "${npc.topic_keyword.mordenthal.1.1}",
                      "${npc.topic_keyword.mordenthal.1.2}"),
                  "${npc.topic.mordenthal.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mordenthal.2.0}"),
                  "${npc.topic.mordenthal.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mordenthal.3.0}"),
                  "${npc.topic.mordenthal.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mordenthal.4.0}", "${npc.topic_keyword.mordenthal.4.1}"),
                  "${npc.topic.mordenthal.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mordenthal.5.0}", "${npc.topic_keyword.mordenthal.5.1}"),
                  "${npc.topic.mordenthal.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mordenthal.6.0}"),
                  "${npc.topic.mordenthal.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mordenthal.7.0}",
                      "${npc.topic_keyword.mordenthal.7.1}",
                      "${npc.topic_keyword.mordenthal.7.2}"),
                  "${npc.topic.mordenthal.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mordenthal.8.0}", "${npc.topic_keyword.mordenthal.8.1}"),
                  "${npc.topic.mordenthal.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mordenthal.9.0}", "${npc.topic_keyword.mordenthal.9.1}"),
                  "${npc.topic.mordenthal.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mordenthal.10.0}"),
                  "${npc.topic.mordenthal.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mordenthal.11.0}"),
                  "${npc.topic.mordenthal.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mordenthal.12.0}"),
                  "${npc.topic.mordenthal.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mordenthal.13.0}"),
                  "${npc.topic.mordenthal.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mordenthal.14.0}"),
                  "${npc.topic.mordenthal.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mordenthal.15.0}"),
                  "${npc.topic.mordenthal.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mordenthal.16.0}"),
                  "${npc.topic.mordenthal.16}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mordenthal.17.0}"),
                  "${npc.topic.mordenthal.17}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mordenthal.18.0}"),
                  "${npc.topic.mordenthal.18}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mordenthal.19.0}"),
                  "${npc.topic.mordenthal.19}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mordenthal.20.0}"),
                  "${npc.topic.mordenthal.20}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mordenthal.21.0}"),
                  "${npc.topic.mordenthal.21}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mordenthal.22.0}"),
                  "${npc.topic.mordenthal.22}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mordenthal.23.0}"),
                  "${npc.topic.mordenthal.23}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mordenthal.24.0}",
                      "${npc.topic_keyword.mordenthal.24.1}"),
                  "${npc.topic.mordenthal.24}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mordenthal.25.0}"),
                  "${npc.topic.mordenthal.25}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mordenthal.26.0}"),
                  "${npc.topic.mordenthal.26}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mordenthal.27.0}"),
                  "${npc.topic.mordenthal.27}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mordenthal.28.0}"),
                  "${npc.topic.mordenthal.28}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mordenthal.29.0}"),
                  "${npc.topic.mordenthal.29}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mordenthal.30.0}"),
                  "${npc.topic.mordenthal.30}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mordenthal.31.0}",
                      "${npc.topic_keyword.mordenthal.31.1}"),
                  "${npc.topic.mordenthal.31}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mordenthal.32.0}"),
                  "${npc.topic.mordenthal.32}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mordenthal.33.0}"),
                  "${npc.topic.mordenthal.33}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mordenthal.34.0}",
                      "${npc.topic_keyword.mordenthal.34.1}",
                      "${npc.topic_keyword.mordenthal.34.2}"),
                  "${npc.topic.mordenthal.34}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mordenthal.35.0}"),
                  "${npc.topic.mordenthal.35}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mordenthal.36.0}",
                      "${npc.topic_keyword.mordenthal.36.1}",
                      "${npc.topic_keyword.mordenthal.36.2}",
                      "${npc.topic_keyword.mordenthal.36.3}"),
                  "${npc.topic.mordenthal.36}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mordenthal.37.0}"),
                  "${npc.topic.mordenthal.37}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.mordenthal.38.0}",
                      "${npc.topic_keyword.mordenthal.38.1}"),
                  "${npc.topic.mordenthal.38}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.mordenthal.39.0}"),
                  "${npc.topic.mordenthal.39}",
                  List.of())),
          "MordenthalNPC",
          new NpcSpec.CombatProfile(100, 1000000, 50, 46, 46, 1000000, 370, 65535, "1d36+27"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if (c.flag("__FLAG_QUEST_FOR_BONES") == 2) {

          c.flag("__FLAG_QUEST_FOR_BONES", 3);

          c.sayKey("npc.mordenthal.ring");

        } else c.sayKey("npc.mordenthal.welcome");
      }

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.contains("RING") && k.contains("LION")) {

          if (c.karma() > 100) c.sayKey("npc.mordenthal.ring.good");
          else if (c.itemCount("ring_of_the_lion") >= 3) c.askYesNo("mordenthal_rings");
          else c.sayKey("npc.mordenthal.ring.need");

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String s, boolean yes) {

        if (!"mordenthal_rings".equals(s)) return false;

        if (yes) {

          for (int i = 0; i < 3; i++) c.takeItem("ring_of_the_lion");

          if (Math.random() < .5) c.giveItem("great_axe_of_the_crow");
          else c.giveItem("cloak_of_armageddon");

          c.giveXp(c.player().getLevel() * 6000);
        }

        return true;
      }
    };
  }

  public Mordenthal(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
