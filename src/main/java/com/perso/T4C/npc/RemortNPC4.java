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

@Spawn(type = "RemortNPC4", x = 1285, y = 850, z = 1, stationary = true, aggressive = false)
public final class RemortNPC4 extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

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

        if (c.flag("__FLAG_REMORT_PROCESS") == 0) c.sayKey("npc.remort.del aan.need_alphan");
        else if (c.flag("__FLAG_REMORT_POINTS") > 0)
          c.sayKey("npc.remort.del aan.welcome", c.flag("__FLAG_REMORT_POINTS"));
        else c.sayKey("npc.remort.del aan.empty");
      }

      private static final int COST = 2;

      private void spendBulk(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String e, int budget) {

        int purchased = 0, spent = 0;

        while (spent + COST <= budget && c.flag("__FLAG_REMORT_POINTS") >= COST) {

          c.flag("__FLAG_REMORT_POINTS", c.flag("__FLAG_REMORT_POINTS") - COST);

          c.player().setBaseElementResistance(e, c.player().getElementResistance(e) + 10);

          spent += COST;

          purchased++;
        }

        if (purchased == 0) c.sayKey("npc.remort.del aan.not_enough");
        else
          c.sayKey(
              "npc.remort.del aan.bulk_done", purchased, spent, c.flag("__FLAG_REMORT_POINTS"));
      }

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String raw = text == null ? "" : text.trim();

        String k = raw.toUpperCase(java.util.Locale.ROOT);

        if (k.contains("RESISTANCE")) {

          c.sayKey("npc.remort.del aan.resistances");

          return true;
        }

        String[] parts = raw.split("\\s+", 2);

        String word = parts[0].toUpperCase(java.util.Locale.ROOT);

        Integer budget = null;

        if (parts.length > 1) {

          try {
            budget = Integer.parseInt(parts[1].trim());
          } catch (NumberFormatException ignored) {
          }
        }

        String e =
            word.equals("FIRE")
                ? "fire"
                : word.equals("WATER")
                    ? "water"
                    : word.equals("AIR")
                        ? "air"
                        : word.equals("EARTH") ? "earth" : word.equals("DARK") ? "dark" : null;

        if (e != null) {

          if (budget != null && budget > 0) {

            spendBulk(c, e, budget);

            return true;
          }

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

        if (c.flag("__FLAG_REMORT_POINTS") < COST) {

          c.sayKey("npc.remort.del aan.not_enough");

          return true;
        }

        c.flag("__FLAG_REMORT_POINTS", c.flag("__FLAG_REMORT_POINTS") - COST);

        c.player().setBaseElementResistance(e, c.player().getElementResistance(e) + 10);

        c.sayKey("npc.remort.del aan.done", c.flag("__FLAG_REMORT_POINTS"));

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
