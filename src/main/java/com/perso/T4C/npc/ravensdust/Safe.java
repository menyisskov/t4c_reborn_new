package com.perso.T4C.npc.ravensdust;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public final class Safe extends ScriptedNpc {

  public static final String ID = "Safe";

  public static final String DISPLAY_NAME = "${npc.safe}";

  public static final String SPRITE_BASE = "@static:Vault";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(),
          0,
          List.of(),
          "${npc.welcome.safe}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.safe.0.0}", "${npc.topic_keyword.safe.0.1}"),
                  "${npc.topic.safe.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.safe.1.0}",
                      "${npc.topic_keyword.safe.1.1}",
                      "${npc.topic_keyword.safe.1.2}"),
                  "${npc.topic.safe.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.safe.2.0}"), "${npc.topic.safe.2}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.safe.3.0}"), "${npc.topic.safe.3}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.safe.4.0}"), "${npc.topic.safe.4}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.safe.5.0}"), "${npc.topic.safe.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.safe.6.0}"), "${npc.topic.safe.6}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.safe.7.0}",
                      "${npc.topic_keyword.safe.7.1}",
                      "${npc.topic_keyword.safe.7.2}",
                      "${npc.topic_keyword.safe.7.3}"),
                  "${npc.topic.safe.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.safe.8.0}",
                      "${npc.topic_keyword.safe.8.1}",
                      "${npc.topic_keyword.safe.8.2}",
                      "${npc.topic_keyword.safe.8.3}"),
                  "${npc.topic.safe.8}",
                  List.of())),
          "SafeNPC",
          new NpcSpec.CombatProfile(100, 1000000, 500, 500, 500, 1000000, 1, 65535, "1d3"));

  public Safe(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }

  @Override
  protected NpcBehavior javaBehavior() {

    return BEHAVIOR;
  }

  private static final NpcBehavior BEHAVIOR =
      new NpcBehavior() {

        private static final String STEP = "__FLAG_SAFE_NUMBER";

        @Override
        public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

          String k = text == null ? "" : text.trim().toUpperCase(Locale.ROOT);

          if (k.equals("HELP")) {

            c.sayKey("npc.safe.help");

            return true;
          }

          if (!k.matches("(?:RIGHT|LEFT)\\s+[-+]?\\d+")) return false;

          int n;

          try {

            n = Integer.parseInt(k.split("\\s+")[1]);

          } catch (RuntimeException e) {

            n = 0;
          }

          if (c.globalFlag("GLOBAL_BANK_HAS_BEEN_ROBBED") > System.currentTimeMillis() / 1000L) {

            c.sayKey("npc.safe.robbed");

            return true;
          }

          if (n < 1 || n > 100) {

            c.sayKey("npc.safe.range");

            return true;
          }

          int s = c.flag(STEP);

          boolean r = k.startsWith("RIGHT");

          if (r && s == 0 && n == c.flag("__QUEST_VAULT_CODE_ONE")) {

            c.flag(STEP, 1);

            c.sayKey("npc.safe.click");

            return true;
          }

          if ((r || !r) && s == 1) {

            fail(c);

            return true;
          }

          if (s == 2 && n == c.flag("__QUEST_VAULT_CODE_THREE")) {

            reward(c);

            return true;
          }

          fail(c);

          return true;
        }

        private void fail(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

          c.flag(STEP, 0);

          switch (ThreadLocalRandom.current().nextInt(4)) {
            case 0 -> c.teleport(209, 2338, 1);

            case 1 -> {
              if (c.player().getCurrentHp() > 2)
                c.player().takeDamage(c.player().getCurrentHp() / 2);
            }

            default -> c.sayKey("npc.safe.failure");
          }
        }

        private void reward(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

          int g = 800 + ThreadLocalRandom.current().nextInt(8000);

          c.giveGold(g);

          c.giveXp(10000);

          c.flag("__QUEST_VAULT_CODE_ONE", 0);

          c.flag(STEP, 0);

          c.flag("__QUEST_VAULT_CODE_TWO", 0);

          c.flag("__QUEST_VAULT_CODE_THREE", 0);

          c.flag("__FLAG_THIEF", 1);

          c.globalFlag(
              "GLOBAL_BANK_HAS_BEEN_ROBBED", (int) (System.currentTimeMillis() / 1000L + 600));

          c.sayKey("npc.safe.success", g);
        }
      };
}
