package com.perso.T4C.npc.ravensdust;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "Celydia", x = 1439, y = 2339, z = 0, stationary = false, aggressive = false)
public final class Celydia extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Celydia";

  public static final String DISPLAY_NAME = "${npc.celydia}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "WoClothBody"),
              new NpcSpec.Part(BodyPart.BOOT, "WoLeatherBoots"),
              new NpcSpec.Part(BodyPart.ROBELEGS, "WoClothRobe")),
          0,
          List.of(),
          "${npc.welcome.celydia}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.celydia.0.0}", "${npc.topic_keyword.celydia.0.1}"),
                  "${npc.topic.celydia.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.celydia.1.0}"), "${npc.topic.celydia.1}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.celydia.2.0}",
                      "${npc.topic_keyword.celydia.2.1}",
                      "${npc.topic_keyword.celydia.2.2}"),
                  "${npc.topic.celydia.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.celydia.3.0}"), "${npc.topic.celydia.3}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.celydia.4.0}", "${npc.topic_keyword.celydia.4.1}"),
                  "${npc.topic.celydia.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.celydia.5.0}"), "${npc.topic.celydia.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.celydia.6.0}"), "${npc.topic.celydia.6}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.celydia.7.0}"), "${npc.topic.celydia.7}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.celydia.8.0}",
                      "${npc.topic_keyword.celydia.8.1}",
                      "${npc.topic_keyword.celydia.8.2}"),
                  "${npc.topic.celydia.8}",
                  List.of())),
          "CelydiaNPC",
          new NpcSpec.CombatProfile(100, 1000000, 65, 67, 63, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(NpcBehaviorContext c) {

        int h = java.time.LocalTime.now().getHour();

        c.sayKey(h >= 22 || h < 6 ? "npc.celydia.late" : "npc.celydia.welcome");
      }

      @Override
      public boolean onKeyword(NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.equals("ROYAL KEY")) {

          c.sayKey(
              c.flag("__QUEST_DEAD_BROTHERS") >= 12
                  ? "npc.celydia.key.known"
                  : "npc.celydia.key.unknown");

          return true;
        }

        if (k.equals("REYNEN") || k.equals("ASPICDART")) {

          int q = c.flag("__QUEST_DEAD_BROTHERS");

          if (c.flag("__QUEST_ROYAL_KEY1") == 1) c.sayKey("npc.celydia.reynen.ask");
          else if (q >= 12) {

            c.sayKey("npc.celydia.reynen.found");

            c.flag("__QUEST_ROYAL_KEY1", 1);

          } else c.sayKey("npc.celydia.reynen.lore");

          return true;
        }

        return false;
      }
    };
  }

  public Celydia(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
