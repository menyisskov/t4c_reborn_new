package com.perso.T4C.npc.arakas;

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
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "Lantalir", x = 2801, y = 220, z = 0, stationary = false, aggressive = false)
public final class Lantalir extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Lantalir";

  public static final String DISPLAY_NAME = "${npc.lantalir}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupMageRobe"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots"),
              new NpcSpec.Part(BodyPart.HEAD, "PupElvenHat"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupLichStaff")),
          0,
          List.of(),
          "${npc.welcome.lantalir}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lantalir.0.0}", "${npc.topic_keyword.lantalir.0.1}"),
                  "${npc.topic.lantalir.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.lantalir.1.0}",
                      "${npc.topic_keyword.lantalir.1.1}",
                      "${npc.topic_keyword.lantalir.1.2}"),
                  "${npc.topic.lantalir.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lantalir.2.0}"),
                  "${npc.topic.lantalir.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lantalir.3.0}", "${npc.topic_keyword.lantalir.3.1}"),
                  "${npc.topic.lantalir.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lantalir.4.0}"),
                  "${npc.topic.lantalir.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.lantalir.5.0}",
                      "${npc.topic_keyword.lantalir.5.1}",
                      "${npc.topic_keyword.lantalir.5.2}"),
                  "${npc.topic.lantalir.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lantalir.6.0}"),
                  "${npc.topic.lantalir.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lantalir.7.0}"),
                  "${npc.topic.lantalir.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lantalir.8.0}"),
                  "${npc.topic.lantalir.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lantalir.9.0}"),
                  "${npc.topic.lantalir.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lantalir.10.0}"),
                  "${npc.topic.lantalir.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lantalir.11.0}"),
                  "${npc.topic.lantalir.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lantalir.12.0}"),
                  "${npc.topic.lantalir.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lantalir.13.0}"),
                  "${npc.topic.lantalir.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lantalir.14.0}"),
                  "${npc.topic.lantalir.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lantalir.15.0}"),
                  "${npc.topic.lantalir.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.lantalir.16.0}", "${npc.topic_keyword.lantalir.16.1}"),
                  "${npc.topic.lantalir.16}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lantalir.17.0}"),
                  "${npc.topic.lantalir.17}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.lantalir.18.0}", "${npc.topic_keyword.lantalir.18.1}"),
                  "${npc.topic.lantalir.18}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.lantalir.19.0}", "${npc.topic_keyword.lantalir.19.1}"),
                  "${npc.topic.lantalir.19}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lantalir.20.0}"),
                  "${npc.topic.lantalir.20}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.lantalir.21.0}",
                      "${npc.topic_keyword.lantalir.21.1}",
                      "${npc.topic_keyword.lantalir.21.2}",
                      "${npc.topic_keyword.lantalir.21.3}",
                      "${npc.topic_keyword.lantalir.21.4}"),
                  "${npc.topic.lantalir.21}",
                  List.of())),
          "LantalirNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.contains("FEYLOR") || k.contains("ERRAND")) {

          long now = System.currentTimeMillis() / 1000L;

          if (c.npcFlag("BOOK_OF_FEYLOR_QUEST") > now) {

            c.sayKey("npc.lantalir.feylor.cooldown");

          } else if (c.hasItem("book_of_feylor")) {

            c.npcFlag(
                "BOOK_OF_FEYLOR_QUEST", (int) (now + (3600L * (1 + (int) (Math.random() * 2)))));

            c.takeItem("book_of_feylor");

            c.giveItem("sapphire_bracelet");

            c.giveXp(2500);

            c.sayKey("npc.lantalir.feylor.complete");

          } else c.sayKey("npc.lantalir.feylor.request");

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }
    };
  }

  public Lantalir(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
