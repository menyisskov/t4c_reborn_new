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

@Spawn(type = "RemortNPC3", x = 1245, y = 890, z = 1, stationary = true, aggressive = false)
public final class RemortNPC3 extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "RemortNPC3";

  public static final String DISPLAY_NAME = "${npc.remortnpc3}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupWhiteRobe"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots")),
          0,
          List.of(),
          "${npc.welcome.remortnpc3}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.remortnpc3.0.0}", "${npc.topic_keyword.remortnpc3.0.1}"),
                  "${npc.topic.remortnpc3.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.remortnpc3.1.0}",
                      "${npc.topic_keyword.remortnpc3.1.1}",
                      "${npc.topic_keyword.remortnpc3.1.2}"),
                  "${npc.topic.remortnpc3.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.remortnpc3.2.0}"),
                  "${npc.topic.remortnpc3.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.remortnpc3.3.0}"),
                  "${npc.topic.remortnpc3.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.remortnpc3.4.0}"),
                  "${npc.topic.remortnpc3.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.remortnpc3.5.0}"),
                  "${npc.topic.remortnpc3.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.remortnpc3.6.0}"),
                  "${npc.topic.remortnpc3.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.remortnpc3.7.0}"),
                  "${npc.topic.remortnpc3.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.remortnpc3.8.0}"),
                  "${npc.topic.remortnpc3.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.remortnpc3.9.0}",
                      "${npc.topic_keyword.remortnpc3.9.1}",
                      "${npc.topic_keyword.remortnpc3.9.2}",
                      "${npc.topic_keyword.remortnpc3.9.3}"),
                  "${npc.topic.remortnpc3.9}",
                  List.of())),
          "RemortNPC",
          new NpcSpec.CombatProfile(100, 1000000, 500, 500, 500, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        c.sayKey(
            c.flag("__FLAG_REMORT_PROCESS") == 0
                ? "npc.remort.caplan.need_alphan"
                : c.flag("__FLAG_REMORT_POINTS") > 0
                    ? "npc.remort.caplan.welcome"
                    : "npc.remort.caplan.empty");
      }

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.equals("POWER")) {

          c.sayKey("npc.remort.caplan.powers");

          return true;
        }

        String e =
            k.equals("FIRE")
                ? "fire"
                : k.equals("WATER")
                    ? "water"
                    : k.equals("AIR")
                        ? "air"
                        : k.equals("EARTH")
                            ? "earth"
                            : k.equals("LIGHT") ? "light" : k.equals("DARK") ? "dark" : null;

        if (e != null) {

          int cost = Math.max(1, (c.player().getElementPower(e) - 95) / 5);

          c.sayKey("npc.remort.caplan.cost", cost);

          c.askYesNo("caplan_" + e);

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String s, boolean yes) {

        if (!s.startsWith("caplan_")) return false;

        if (!yes) return true;

        String e = s.substring(7);

        int cost = Math.max(1, (c.player().getElementPower(e) - 95) / 5);

        if (c.flag("__FLAG_REMORT_POINTS") < cost) {

          c.sayKey("npc.remort.caplan.not_enough");

          return true;
        }

        c.flag("__FLAG_REMORT_POINTS", c.flag("__FLAG_REMORT_POINTS") - cost);

        c.player().setBaseElementPower(e, c.player().getElementPower(e) + 5);

        c.sayKey("npc.remort.caplan.done");

        return true;
      }
    };
  }

  public RemortNPC3(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
