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
import java.util.List;

public final class HeadhunterWajpoothin extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "HeadhunterWajpoothin";

  public static final String DISPLAY_NAME = "${npc.headhunterwajpoothin}";

  public static final String SPRITE_BASE = "64kSkavenWarrior";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.headhunterwajpoothin}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.headhunterwajpoothin.0.0}",
                      "${npc.topic_keyword.headhunterwajpoothin.0.1}"),
                  "${npc.topic.headhunterwajpoothin.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.headhunterwajpoothin.1.0}",
                      "${npc.topic_keyword.headhunterwajpoothin.1.1}",
                      "${npc.topic_keyword.headhunterwajpoothin.1.2}"),
                  "${npc.topic.headhunterwajpoothin.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.headhunterwajpoothin.2.0}"),
                  "${npc.topic.headhunterwajpoothin.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.headhunterwajpoothin.3.0}",
                      "${npc.topic_keyword.headhunterwajpoothin.3.1}"),
                  "${npc.topic.headhunterwajpoothin.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.headhunterwajpoothin.4.0}",
                      "${npc.topic_keyword.headhunterwajpoothin.4.1}",
                      "${npc.topic_keyword.headhunterwajpoothin.4.2}",
                      "${npc.topic_keyword.headhunterwajpoothin.4.3}"),
                  "${npc.topic.headhunterwajpoothin.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.headhunterwajpoothin.5.0}",
                      "${npc.topic_keyword.headhunterwajpoothin.5.1}",
                      "${npc.topic_keyword.headhunterwajpoothin.5.2}",
                      "${npc.topic_keyword.headhunterwajpoothin.5.3}",
                      "${npc.topic_keyword.headhunterwajpoothin.5.4}"),
                  "${npc.topic.headhunterwajpoothin.5}",
                  List.of())),
          "HeadhunterWajpoothinNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.contains("SKRAUGBASHOR") && k.contains("MACE")) {

          if (c.itemCount("moon_tug_scalp") >= 5) {

            c.sayKey("npc.wajpoothin.scalp.ask");

            c.askYesNo("wajpoothin_scalps");

          } else c.sayKey("npc.wajpoothin.scalp.info");

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String s, boolean yes) {

        if (!"wajpoothin_scalps".equals(s)) return false;

        if (yes && c.itemCount("moon_tug_scalp") >= 5) {

          for (int i = 0; i < 5; i++) c.takeItem("moon_tug_scalp");

          c.giveItem("skraugbashor_mace");

          c.giveXp(c.player().getLevel() * 1000);

          c.sayKey("npc.wajpoothin.scalp.done");
        }

        return true;
      }
    };
  }

  public HeadhunterWajpoothin(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
