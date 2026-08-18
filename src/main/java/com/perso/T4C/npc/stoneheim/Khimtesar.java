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

public final class Khimtesar extends ScriptedNpc {

  public static final String ID = "Khimtesar";

  public static final String DISPLAY_NAME = "${npc.khimtesar}";

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
              new NpcSpec.Part(BodyPart.WEAPON, "PupLichStaff")),
          0,
          List.of(),
          "${npc.welcome.khimtesar}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.khimtesar.0.0}"),
                  "${npc.topic.khimtesar.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.khimtesar.1.0}", "${npc.topic_keyword.khimtesar.1.1}"),
                  "${npc.topic.khimtesar.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.khimtesar.2.0}"),
                  "${npc.topic.khimtesar.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.khimtesar.3.0}"),
                  "${npc.topic.khimtesar.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.khimtesar.4.0}",
                      "${npc.topic_keyword.khimtesar.4.1}",
                      "${npc.topic_keyword.khimtesar.4.2}"),
                  "${npc.topic.khimtesar.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.khimtesar.5.0}",
                      "${npc.topic_keyword.khimtesar.5.1}",
                      "${npc.topic_keyword.khimtesar.5.2}",
                      "${npc.topic_keyword.khimtesar.5.3}"),
                  "${npc.topic.khimtesar.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.khimtesar.6.0}",
                      "${npc.topic_keyword.khimtesar.6.1}",
                      "${npc.topic_keyword.khimtesar.6.2}",
                      "${npc.topic_keyword.khimtesar.6.3}",
                      "${npc.topic_keyword.khimtesar.6.4}"),
                  "${npc.topic.khimtesar.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.khimtesar.7.0}", "${npc.topic_keyword.khimtesar.7.1}"),
                  "${npc.topic.khimtesar.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.khimtesar.8.0}",
                      "${npc.topic_keyword.khimtesar.8.1}",
                      "${npc.topic_keyword.khimtesar.8.2}"),
                  "${npc.topic.khimtesar.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.khimtesar.9.0}", "${npc.topic_keyword.khimtesar.9.1}"),
                  "${npc.topic.khimtesar.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.khimtesar.10.0}", "${npc.topic_keyword.khimtesar.10.1}"),
                  "${npc.topic.khimtesar.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.khimtesar.11.0}", "${npc.topic_keyword.khimtesar.11.1}"),
                  "${npc.topic.khimtesar.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.khimtesar.12.0}", "${npc.topic_keyword.khimtesar.12.1}"),
                  "${npc.topic.khimtesar.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.khimtesar.13.0}",
                      "${npc.topic_keyword.khimtesar.13.1}",
                      "${npc.topic_keyword.khimtesar.13.2}"),
                  "${npc.topic.khimtesar.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.khimtesar.14.0}",
                      "${npc.topic_keyword.khimtesar.14.1}",
                      "${npc.topic_keyword.khimtesar.14.2}"),
                  "${npc.topic.khimtesar.14}",
                  List.of())),
          "KhimtesarNPC",
          new NpcSpec.CombatProfile(100, 1000000, 65, 67, 63, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.contains("FUCK")
            || k.contains("SUCK")
            || k.contains("ASSHOLE")
            || k.contains(" ASS ")) {

          c.sayKey("npc.khimtesar.abuse");

          c.player().addGold(-(c.player().getGold() / 2));

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }
    };
  }

  public Khimtesar(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
