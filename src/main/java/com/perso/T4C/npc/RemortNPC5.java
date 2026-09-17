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

@Spawn(type = "RemortNPC5", x = 1325, y = 850, z = 1, stationary = true, aggressive = false)
public final class RemortNPC5 extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "RemortNPC5";

  public static final String DISPLAY_NAME = "${npc.remortnpc5}";

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
          "${npc.welcome.remortnpc5}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.remortnpc5.0.0}", "${npc.topic_keyword.remortnpc5.0.1}"),
                  "${npc.topic.remortnpc5.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.remortnpc5.1.0}",
                      "${npc.topic_keyword.remortnpc5.1.1}",
                      "${npc.topic_keyword.remortnpc5.1.2}"),
                  "${npc.topic.remortnpc5.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.remortnpc5.2.0}"),
                  "${npc.topic.remortnpc5.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.remortnpc5.3.0}"),
                  "${npc.topic.remortnpc5.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.remortnpc5.4.0}",
                      "${npc.topic_keyword.remortnpc5.4.1}",
                      "${npc.topic_keyword.remortnpc5.4.2}",
                      "${npc.topic_keyword.remortnpc5.4.3}"),
                  "${npc.topic.remortnpc5.4}",
                  List.of())),
          "RemortNPC",
          new NpcSpec.CombatProfile(100, 1000000, 500, 500, 500, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if (c.flag("__FLAG_REMORT_PROCESS") == 0) c.sayKey("npc.remort.epilan.need_alphan");
        else if (c.flag("__FLAG_REMORT_POINTS") > 0)
          c.sayKey("npc.remort.epilan.welcome", c.flag("__FLAG_REMORT_POINTS"));
        else c.sayKey("npc.remort.epilan.empty");
      }

      private void spendBulk(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String stat, int budget) {

        int purchased = 0;

        while (purchased < budget && c.flag("__FLAG_REMORT_POINTS") >= 1) {

          c.flag("__FLAG_REMORT_POINTS", c.flag("__FLAG_REMORT_POINTS") - 1);

          if (stat.equals("health")) c.player().setMaxHp(c.player().getMaxHp() + 10);
          else c.player().setMaxMana(c.player().getMaxMana() + 5);

          purchased++;
        }

        if (purchased == 0) c.sayKey("npc.remort.epilan.not_enough");
        else
          c.sayKey(
              "npc.remort.epilan.bulk_done", purchased, purchased, c.flag("__FLAG_REMORT_POINTS"));
      }

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String raw = text == null ? "" : text.trim();

        String[] parts = raw.split("\\s+", 2);

        String k = parts[0].toUpperCase(java.util.Locale.ROOT);

        Integer budget = null;

        if (parts.length > 1) {

          try {
            budget = Integer.parseInt(parts[1].trim());
          } catch (NumberFormatException ignored) {
          }
        }

        if (k.equals("HEALTH")) {

          if (budget != null && budget > 0) {

            spendBulk(c, "health", budget);

            return true;
          }

          c.sayKey("npc.remort.epilan.health");

          c.askYesNo("epilan_health");

          return true;
        }

        if (k.equals("MANA")) {

          if (budget != null && budget > 0) {

            spendBulk(c, "mana", budget);

            return true;
          }

          c.sayKey("npc.remort.epilan.mana");

          c.askYesNo("epilan_mana");

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String s, boolean yes) {

        if (!s.startsWith("epilan_")) return false;

        if (!yes) return true;

        if (c.flag("__FLAG_REMORT_POINTS") < 1) {

          c.sayKey("npc.remort.epilan.not_enough");

          return true;
        }

        c.flag("__FLAG_REMORT_POINTS", c.flag("__FLAG_REMORT_POINTS") - 1);

        if (s.endsWith("health")) c.player().setMaxHp(c.player().getMaxHp() + 10);
        else {

          c.player().setMaxMana(c.player().getMaxMana() + 5);
        }

        c.sayKey("npc.remort.epilan.done", c.flag("__FLAG_REMORT_POINTS"));

        return true;
      }
    };
  }

  public RemortNPC5(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
