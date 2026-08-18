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
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class RemortNPC4 extends ScriptedNpc {

  public static final String ID = "RemortNPC4";

  public static final String DISPLAY_NAME = "${npc.remortnpc4}";

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
          "${npc.welcome.remortnpc4}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.remortnpc4.0.0}", "${npc.topic_keyword.remortnpc4.0.1}"),
                  "${npc.topic.remortnpc4.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.remortnpc4.1.0}",
                      "${npc.topic_keyword.remortnpc4.1.1}",
                      "${npc.topic_keyword.remortnpc4.1.2}"),
                  "${npc.topic.remortnpc4.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.remortnpc4.2.0}"),
                  "${npc.topic.remortnpc4.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.remortnpc4.3.0}"),
                  "${npc.topic.remortnpc4.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.remortnpc4.4.0}"),
                  "${npc.topic.remortnpc4.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.remortnpc4.5.0}"),
                  "${npc.topic.remortnpc4.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.remortnpc4.6.0}"),
                  "${npc.topic.remortnpc4.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.remortnpc4.7.0}"),
                  "${npc.topic.remortnpc4.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.remortnpc4.8.0}",
                      "${npc.topic_keyword.remortnpc4.8.1}",
                      "${npc.topic_keyword.remortnpc4.8.2}",
                      "${npc.topic_keyword.remortnpc4.8.3}"),
                  "${npc.topic.remortnpc4.8}",
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
                ? "npc.remort.del aan.need_alphan"
                : c.flag("__FLAG_REMORT_POINTS") > 0
                    ? "npc.remort.del aan.welcome"
                    : "npc.remort.del aan.empty");
      }

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.contains("RESISTANCE")) {

          c.sayKey("npc.remort.del aan.resistances");

          return true;
        }

        String e =
            k.equals("FIRE")
                ? "fire"
                : k.equals("WATER")
                    ? "water"
                    : k.equals("AIR")
                        ? "air"
                        : k.equals("EARTH") ? "earth" : k.equals("DARK") ? "dark" : null;

        if (e != null) {

          c.sayKey("npc.remort.del aan.cost");

          c.askYesNo("del aan_" + e);

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String s, boolean yes) {

        if (!s.startsWith("del aan_")) return false;

        if (!yes) return true;

        String e = s.substring(7);

        if (c.flag("__FLAG_REMORT_POINTS") < 2) {

          c.sayKey("npc.remort.del aan.not_enough");

          return true;
        }

        c.flag("__FLAG_REMORT_POINTS", c.flag("__FLAG_REMORT_POINTS") - 2);

        c.player().setBaseElementResistance(e, c.player().getElementResistance(e) + 10);

        c.sayKey("npc.remort.del aan.done");

        return true;
      }
    };
  }

  public RemortNPC4(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
