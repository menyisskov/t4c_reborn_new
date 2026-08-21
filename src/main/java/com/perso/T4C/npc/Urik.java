package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "Urik", x = 2622, y = 1498, z = 0, stationary = false, aggressive = false)
public final class Urik extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Urik";

  public static final String DISPLAY_NAME = "${npc.urik}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupLeatherBody"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLeatherPants")),
          0,
          List.of(),
          "${npc.welcome.urik}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.urik.0.0}"), "${npc.topic.urik.0}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.urik.1.0}"), "${npc.topic.urik.1}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.urik.2.0}"), "${npc.topic.urik.2}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.urik.3.0}"), "${npc.topic.urik.3}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.urik.4.0}"), "${npc.topic.urik.4}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.urik.5.0}"), "${npc.topic.urik.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.urik.6.0}", "${npc.topic_keyword.urik.6.1}"),
                  "${npc.topic.urik.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.urik.7.0}",
                      "${npc.topic_keyword.urik.7.1}",
                      "${npc.topic_keyword.urik.7.2}",
                      "${npc.topic_keyword.urik.7.3}",
                      "${npc.topic_keyword.urik.7.4}"),
                  "${npc.topic.urik.7}",
                  List.of())),
          "UrikNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return BEHAVIOR;
  }

  private static final NpcBehavior BEHAVIOR =
      new NpcBehavior() {

        @Override
        public void onConversationStart(NpcBehaviorContext c) {

          int p = c.flag("ADDON_STORYLINE_PROGRESS");

          if (p < 7) c.sayKey("npc.urik.progress.before7");
          else if (p == 7) c.sayKey("npc.urik.progress.7");
          else if (p == 8) {

            c.sayKey("npc.urik.progress.8");

            c.player().addGold(1000);

            c.flag("ADDON_STORYLINE_PROGRESS", 9);

          } else c.sayKey("npc.urik.progress.done");
        }

        @Override
        public boolean onKeyword(NpcBehaviorContext c, String text) {

          String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

          int p = c.flag("ADDON_STORYLINE_PROGRESS");

          if (k.equals("SIMPLETON") || k.equals("DISCUSS")) {

            c.sayKey(p == 7 ? "npc.urik.insult" : "npc.urik.busy");

            return true;
          }

          if (k.equals("BARGAIN")) {

            c.sayKey(p == 7 ? "npc.urik.bargain" : "npc.urik.busy");

            return true;
          }

          if (k.equals("NAME") || k.equals("URIK")) {

            c.sayKey("npc.urik.name");

            return true;
          }

          if (k.equals("WORK") || k.contains("TAILOR")) {

            c.sayKey("npc.urik.work");

            return true;
          }

          return false;
        }
      };

  public Urik(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
