package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.StaticDialogueBehavior;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import com.perso.T4C.spawn.SpawnKind;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Spawn(type = "Jarko", x = 0, y = 0, z = 0, stationary = false, aggressive = false)
@Spawn(
    type = "JARKO",
    x = 1088,
    y = 103,
    z = 1,
    stationary = false,
    aggressive = false,
    kind = SpawnKind.MONSTER)
public final class Jarko extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Jarko";

  public static final String DISPLAY_NAME = "${npc.jarko}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupNecromanRobe"),
              new NpcSpec.Part(BodyPart.BOOT, "PupBlackLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLeatherPants"),
              new NpcSpec.Part(BodyPart.HEAD, "PupHornedHelmet"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupWoodenStaff"),
              new NpcSpec.Part(BodyPart.BACK, "PupRedCape")),
          0,
          List.of(),
          "${npc.welcome.jarko}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.jarko.0.0}", "${npc.topic_keyword.jarko.0.1}"),
                  "${npc.topic.jarko.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.jarko.1.0}",
                      "${npc.topic_keyword.jarko.1.1}",
                      "${npc.topic_keyword.jarko.1.2}"),
                  "${npc.topic.jarko.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.jarko.2.0}"), "${npc.topic.jarko.2}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.jarko.3.0}"), "${npc.topic.jarko.3}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.jarko.4.0}",
                      "${npc.topic_keyword.jarko.4.1}",
                      "${npc.topic_keyword.jarko.4.2}"),
                  "${npc.topic.jarko.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.jarko.5.0}",
                      "${npc.topic_keyword.jarko.5.1}",
                      "${npc.topic_keyword.jarko.5.2}"),
                  "${npc.topic.jarko.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.jarko.6.0}", "${npc.topic_keyword.jarko.6.1}"),
                  "${npc.topic.jarko.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.jarko.7.0}"), "${npc.topic.jarko.7}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.jarko.8.0}"), "${npc.topic.jarko.8}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.jarko.9.0}"), "${npc.topic.jarko.9}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.jarko.10.0}",
                      "${npc.topic_keyword.jarko.10.1}",
                      "${npc.topic_keyword.jarko.10.2}",
                      "${npc.topic_keyword.jarko.10.3}"),
                  "${npc.topic.jarko.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.jarko.11.0}", "${npc.topic_keyword.jarko.11.1}"),
                  "${npc.topic.jarko.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.jarko.12.0}",
                      "${npc.topic_keyword.jarko.12.1}",
                      "${npc.topic_keyword.jarko.12.2}",
                      "${npc.topic_keyword.jarko.12.3}"),
                  "${npc.topic.jarko.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.jarko.13.0}"), "${npc.topic.jarko.13}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.jarko.14.0}"), "${npc.topic.jarko.14}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.jarko.15.0}"), "${npc.topic.jarko.15}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.jarko.16.0}"), "${npc.topic.jarko.16}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.jarko.17.0}",
                      "${npc.topic_keyword.jarko.17.1}",
                      "${npc.topic_keyword.jarko.17.2}",
                      "${npc.topic_keyword.jarko.17.3}",
                      "${npc.topic_keyword.jarko.17.4}"),
                  "${npc.topic.jarko.17}",
                  List.of())),
          "_Jarko",
          new NpcSpec.CombatProfile(32, 1262, 47, 43, 43, 16, 394, 138, "1d40+29"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        boolean handled = StaticDialogueBehavior.INSTANCE.onKeyword(c, text);

        String key = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if ("SPELLBOOK".equals(key)) c.askYesNo("spellbook");
        else if ("STONE OF LIFE".equals(key)) c.askYesNo("stone");

        return handled || "SPELLBOOK".equals(key) || "STONE OF LIFE".equals(key);
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String state, boolean yes) {

        if ("spellbook".equals(state)) {

          if (yes) c.npc().provoke();

          return true;
        }

        if ("stone".equals(state)) {

          c.npc().provoke();

          return true;
        }

        return false;
      }

      @Override
      public void onAttacked(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if (ThreadLocalRandom.current().nextInt(20) != 0) return;

        int count = 1 + ThreadLocalRandom.current().nextInt(2);

        for (int i = 0; i < count; i++) {

          c.summon(
              "Skeleton Warrior",
              c.npcTileX() - 4 + ThreadLocalRandom.current().nextInt(8),
              c.npcTileY() - 4 + ThreadLocalRandom.current().nextInt(8),
              0);
        }
      }
    };
  }

  public Jarko(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
