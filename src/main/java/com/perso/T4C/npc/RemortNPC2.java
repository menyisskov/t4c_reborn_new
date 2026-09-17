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

@Spawn(type = "RemortNPC2", x = 1245, y = 930, z = 1, stationary = true, aggressive = false)
public final class RemortNPC2 extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "RemortNPC2";

  public static final String DISPLAY_NAME = "${npc.remortnpc2}";

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
          "${npc.welcome.remortnpc2}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.remortnpc2.0.0}", "${npc.topic_keyword.remortnpc2.0.1}"),
                  "${npc.topic.remortnpc2.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.remortnpc2.1.0}",
                      "${npc.topic_keyword.remortnpc2.1.1}",
                      "${npc.topic_keyword.remortnpc2.1.2}"),
                  "${npc.topic.remortnpc2.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.remortnpc2.2.0}", "${npc.topic_keyword.remortnpc2.2.1}"),
                  "${npc.topic.remortnpc2.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.remortnpc2.3.0}"),
                  "${npc.topic.remortnpc2.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.remortnpc2.4.0}"),
                  "${npc.topic.remortnpc2.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.remortnpc2.5.0}"),
                  "${npc.topic.remortnpc2.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.remortnpc2.6.0}"),
                  "${npc.topic.remortnpc2.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.remortnpc2.7.0}"),
                  "${npc.topic.remortnpc2.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.remortnpc2.8.0}",
                      "${npc.topic_keyword.remortnpc2.8.1}",
                      "${npc.topic_keyword.remortnpc2.8.2}",
                      "${npc.topic_keyword.remortnpc2.8.3}"),
                  "${npc.topic.remortnpc2.8}",
                  List.of())),
          "RemortNPC",
          new NpcSpec.CombatProfile(100, 1000000, 500, 500, 500, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if (c.flag("__FLAG_REMORT_PROCESS") == 0) c.sayKey("npc.remort.betran.need_alphan");
        else if (c.flag("__FLAG_REMORT_POINTS") > 0)
          c.sayKey("npc.remort.betran.welcome", c.flag("__FLAG_REMORT_POINTS"));
        else c.sayKey("npc.remort.betran.empty");
      }

      private int currentValue(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String a) {

        return switch (a) {
          case "strength" -> c.player().getStrength();

          case "agility" -> c.player().getDexterity();

          case "endurance" -> c.player().getEndurance();

          case "intelligence" -> c.player().getIntelligence();

          default -> c.player().getWisdom();
        };
      }

      private void setValue(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String a, int v) {

        if (a.equals("strength")) c.player().setStrength(v);
        else if (a.equals("agility")) c.player().setDexterity(v);
        else if (a.equals("endurance")) c.player().setEndurance(v);
        else if (a.equals("intelligence")) c.player().setIntelligence(v);
        else c.player().setWisdom(v);
      }

      private int costFor(int delta) {

        return delta == 0 ? 1 : delta <= 3 ? 2 : delta <= 6 ? 3 : delta == 7 ? 4 : 5;
      }

      private void spendBulk(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String a, int budget) {

        int purchased = 0, spent = 0;

        while (spent < budget) {

          int current = currentValue(c, a);

          int delta = current - (20 + c.flag("__FLAG_NUMBER_OF_REMORTS") * 5);

          if (delta > 9) break;

          int cost = costFor(delta);

          if (spent + cost > budget || c.flag("__FLAG_REMORT_POINTS") < cost) break;

          c.flag("__FLAG_REMORT_POINTS", c.flag("__FLAG_REMORT_POINTS") - cost);

          setValue(c, a, current + 1);

          spent += cost;

          purchased++;
        }

        if (purchased == 0) c.sayKey("npc.remort.betran.not_enough");
        else
          c.sayKey(
              "npc.remort.betran.bulk_done", purchased, spent, c.flag("__FLAG_REMORT_POINTS"));
      }

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String raw = text == null ? "" : text.trim();

        String k = raw.toUpperCase(java.util.Locale.ROOT);

        if (k.contains("ATTRIBUTE") || k.equals("STAT")) {

          c.sayKey(
              c.flag("__FLAG_REMORT_POINTS") > 0
                  ? "npc.remort.betran.attributes"
                  : "npc.remort.betran.empty");

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

        String a =
            word.equals("STRENGTH")
                ? "strength"
                : word.equals("AGILITY")
                    ? "agility"
                    : word.equals("ENDURANCE")
                        ? "endurance"
                        : word.equals("INTELLIGENCE")
                            ? "intelligence"
                            : word.equals("WISDOM") ? "wisdom" : null;

        if (a != null) {

          if (budget != null && budget > 0) {

            spendBulk(c, a, budget);

            return true;
          }

          int current = currentValue(c, a);

          int delta = current - (20 + c.flag("__FLAG_NUMBER_OF_REMORTS") * 5);

          if (delta > 9) c.sayKey("npc.remort.betran.cap");
          else {

            int cost = costFor(delta);

            c.sayKey("npc.remort.betran.cost", cost);

            c.askYesNo("remort_" + a);
          }

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String s, boolean yes) {

        if (!s.startsWith("remort_")) return false;

        if (!yes) return true;

        String a = s.substring(7);

        int current = currentValue(c, a);

        int delta = current - (20 + c.flag("__FLAG_NUMBER_OF_REMORTS") * 5),
            cost = costFor(delta);

        if (delta > 9 || c.flag("__FLAG_REMORT_POINTS") < cost) {

          c.sayKey("npc.remort.betran.not_enough");

          return true;
        }

        c.flag("__FLAG_REMORT_POINTS", c.flag("__FLAG_REMORT_POINTS") - cost);

        setValue(c, a, current + 1);

        c.sayKey("npc.remort.betran.done", c.flag("__FLAG_REMORT_POINTS"));

        return true;
      }
    };
  }

  public RemortNPC2(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
