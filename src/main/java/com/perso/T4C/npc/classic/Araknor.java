package com.perso.T4C.npc.classic;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcSpec;
import com.perso.T4C.npc.script.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "Araknor", x = 2981, y = 1035, z = 0, stationary = false, aggressive = false)
public final class Araknor extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Araknor";

  public static final String DISPLAY_NAME = "${npc.araknor}";

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
              new NpcSpec.Part(BodyPart.HEAD, "PupHornedHelmet")),
          0,
          List.of(),
          "${npc.welcome.araknor}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.araknor.0.0}"), "${npc.topic.araknor.0}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.araknor.1.0}", "${npc.topic_keyword.araknor.1.1}"),
                  "${npc.topic.araknor.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.araknor.2.0}",
                      "${npc.topic_keyword.araknor.2.1}",
                      "${npc.topic_keyword.araknor.2.2}"),
                  "${npc.topic.araknor.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.araknor.3.0}",
                      "${npc.topic_keyword.araknor.3.1}",
                      "${npc.topic_keyword.araknor.3.2}"),
                  "${npc.topic.araknor.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.araknor.4.0}"), "${npc.topic.araknor.4}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.araknor.5.0}"), "${npc.topic.araknor.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.araknor.6.0}", "${npc.topic_keyword.araknor.6.1}"),
                  "${npc.topic.araknor.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.araknor.7.0}"), "${npc.topic.araknor.7}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.araknor.8.0}"), "${npc.topic.araknor.8}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.araknor.9.0}"), "${npc.topic.araknor.9}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.araknor.10.0}"),
                  "${npc.topic.araknor.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.araknor.11.0}"),
                  "${npc.topic.araknor.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.araknor.12.0}"),
                  "${npc.topic.araknor.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.araknor.13.0}"),
                  "${npc.topic.araknor.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.araknor.14.0}", "${npc.topic_keyword.araknor.14.1}"),
                  "${npc.topic.araknor.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.araknor.15.0}"),
                  "${npc.topic.araknor.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.araknor.16.0}"),
                  "${npc.topic.araknor.16}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.araknor.17.0}"),
                  "${npc.topic.araknor.17}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.araknor.18.0}"),
                  "${npc.topic.araknor.18}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.araknor.19.0}",
                      "${npc.topic_keyword.araknor.19.1}",
                      "${npc.topic_keyword.araknor.19.2}",
                      "${npc.topic_keyword.araknor.19.3}"),
                  "${npc.topic.araknor.19}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.araknor.20.0}",
                      "${npc.topic_keyword.araknor.20.1}",
                      "${npc.topic_keyword.araknor.20.2}",
                      "${npc.topic_keyword.araknor.20.3}",
                      "${npc.topic_keyword.araknor.20.4}"),
                  "${npc.topic.araknor.20}",
                  List.of())),
          "AraknorNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        if (text != null && text.toUpperCase(java.util.Locale.ROOT).contains("TEACH")) {

          c.openSpellLearning(java.util.List.of("lesser_drain"));

          return true;
        }

        return false;
      }
    };
  }

  public Araknor(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
