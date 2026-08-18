package com.perso.T4C.npc.ravensdust;

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

public final class Etheanan extends ScriptedNpc {

  public static final String ID = "Etheanan";

  public static final String DISPLAY_NAME = "${npc.etheanan}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupNecromanRobe"),
              new NpcSpec.Part(BodyPart.BOOT, "PupBlackLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLeatherPants")),
          0,
          List.of(),
          "${npc.welcome.etheanan}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.etheanan.0.0}"),
                  "${npc.topic.etheanan.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.etheanan.1.0}"),
                  "${npc.topic.etheanan.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.etheanan.2.0}"),
                  "${npc.topic.etheanan.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.etheanan.3.0}"),
                  "${npc.topic.etheanan.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.etheanan.4.0}"),
                  "${npc.topic.etheanan.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.etheanan.5.0}"),
                  "${npc.topic.etheanan.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.etheanan.6.0}"),
                  "${npc.topic.etheanan.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.etheanan.7.0}"),
                  "${npc.topic.etheanan.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.etheanan.8.0}"),
                  "${npc.topic.etheanan.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.etheanan.9.0}"),
                  "${npc.topic.etheanan.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.etheanan.10.0}"),
                  "${npc.topic.etheanan.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.etheanan.11.0}"),
                  "${npc.topic.etheanan.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.etheanan.12.0}"),
                  "${npc.topic.etheanan.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.etheanan.13.0}"),
                  "${npc.topic.etheanan.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.etheanan.14.0}"),
                  "${npc.topic.etheanan.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.etheanan.15.0}"),
                  "${npc.topic.etheanan.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.etheanan.16.0}"),
                  "${npc.topic.etheanan.16}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.etheanan.17.0}"),
                  "${npc.topic.etheanan.17}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.etheanan.18.0}"),
                  "${npc.topic.etheanan.18}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.etheanan.19.0}",
                      "${npc.topic_keyword.etheanan.19.1}",
                      "${npc.topic_keyword.etheanan.19.2}"),
                  "${npc.topic.etheanan.19}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.etheanan.20.0}",
                      "${npc.topic_keyword.etheanan.20.1}",
                      "${npc.topic_keyword.etheanan.20.2}"),
                  "${npc.topic.etheanan.20}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.etheanan.21.0}"),
                  "${npc.topic.etheanan.21}",
                  List.of())),
          "Mage",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 370, 65535, "1d36+27"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        if (text != null && text.toUpperCase(java.util.Locale.ROOT).equals("FISHING")) {

          c.sayKey("npc.etheanan.fishing.ask");

          c.askYesNo("etheanan_fishing");

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String s, boolean yes) {

        if (!"etheanan_fishing".equals(s)) return false;

        c.sayKey(yes ? "npc.etheanan.fishing.yes" : "npc.etheanan.fishing.no");

        return true;
      }
    };
  }

  public Etheanan(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
