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

public final class Leana extends ScriptedNpc {

  public static final String ID = "Leana";

  public static final String DISPLAY_NAME = "${npc.leana}";

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
          "${npc.welcome.leana}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.leana.0.0}"), "${npc.topic.leana.0}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.leana.1.0}"), "${npc.topic.leana.1}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.leana.2.0}"), "${npc.topic.leana.2}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.leana.3.0}"), "${npc.topic.leana.3}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.leana.4.0}"), "${npc.topic.leana.4}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.leana.5.0}"), "${npc.topic.leana.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.leana.6.0}"), "${npc.topic.leana.6}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.leana.7.0}"), "${npc.topic.leana.7}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.leana.8.0}",
                      "${npc.topic_keyword.leana.8.1}",
                      "${npc.topic_keyword.leana.8.2}"),
                  "${npc.topic.leana.8}",
                  List.of())),
          "LeanaNPC",
          new NpcSpec.CombatProfile(100, 1000000, 65, 67, 63, 1000000, 0, 65535, "1d3"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        c.sayKey("npc.leana.shop.ask");

        c.askYesNo("leana_shop");
      }

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        if (text != null && text.toUpperCase(java.util.Locale.ROOT).equals("SHOP")) {

          c.sayKey("npc.leana.shop.ask");

          c.askYesNo("leana_shop");

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String s, boolean yes) {

        if ("leana_shop".equals(s)) {

          c.sayKey(yes ? "npc.leana.shop.yes" : "npc.leana.shop.no");

          return true;
        }

        return false;
      }
    };
  }

  public Leana(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
