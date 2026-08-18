package com.perso.T4C.npc.windhowl;

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

public final class LordSunrock extends ScriptedNpc {

  public static final String ID = "LordSunrock";

  public static final String DISPLAY_NAME = "${npc.lordsunrock}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupChainMailBody"),
              new NpcSpec.Part(BodyPart.BOOT, "PupPlateFoot"),
              new NpcSpec.Part(BodyPart.LEGS, "PupChainMailLegs"),
              new NpcSpec.Part(BodyPart.HEAD, "PupChainMailCoif"),
              new NpcSpec.Part(BodyPart.BACK, "PupRedCape"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.lordsunrock}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lordsunrock.0.0}"),
                  "${npc.topic.lordsunrock.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lordsunrock.1.0}"),
                  "${npc.topic.lordsunrock.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.lordsunrock.2.0}",
                      "${npc.topic_keyword.lordsunrock.2.1}"),
                  "${npc.topic.lordsunrock.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.lordsunrock.3.0}",
                      "${npc.topic_keyword.lordsunrock.3.1}"),
                  "${npc.topic.lordsunrock.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.lordsunrock.4.0}",
                      "${npc.topic_keyword.lordsunrock.4.1}",
                      "${npc.topic_keyword.lordsunrock.4.2}"),
                  "${npc.topic.lordsunrock.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lordsunrock.5.0}"),
                  "${npc.topic.lordsunrock.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.lordsunrock.6.0}",
                      "${npc.topic_keyword.lordsunrock.6.1}",
                      "${npc.topic_keyword.lordsunrock.6.2}"),
                  "${npc.topic.lordsunrock.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lordsunrock.7.0}"),
                  "${npc.topic.lordsunrock.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lordsunrock.8.0}"),
                  "${npc.topic.lordsunrock.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lordsunrock.9.0}"),
                  "${npc.topic.lordsunrock.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lordsunrock.10.0}"),
                  "${npc.topic.lordsunrock.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lordsunrock.11.0}"),
                  "${npc.topic.lordsunrock.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lordsunrock.12.0}"),
                  "${npc.topic.lordsunrock.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lordsunrock.13.0}"),
                  "${npc.topic.lordsunrock.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lordsunrock.14.0}"),
                  "${npc.topic.lordsunrock.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.lordsunrock.15.0}",
                      "${npc.topic_keyword.lordsunrock.15.1}"),
                  "${npc.topic.lordsunrock.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.lordsunrock.16.0}",
                      "${npc.topic_keyword.lordsunrock.16.1}",
                      "${npc.topic_keyword.lordsunrock.16.2}",
                      "${npc.topic_keyword.lordsunrock.16.3}",
                      "${npc.topic_keyword.lordsunrock.16.4}"),
                  "${npc.topic.lordsunrock.16}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lordsunrock.17.0}"),
                  "${npc.topic.lordsunrock.17}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.lordsunrock.18.0}"),
                  "${npc.topic.lordsunrock.18}",
                  List.of())),
          "Lord_SunRock",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        int t = c.flag("__QUEST_USER_IS_A_TRAITOR");

        if (t == 3) c.sayKey("npc.sunrock.traitor");
        else if (t == 1) c.sayKey("npc.sunrock.merchandise");
        else if (c.flag("__QUEST_BRIGAND_GOT_THE_REPORT") == 0) c.sayKey("npc.sunrock.welcome");
        else c.sayKey("npc.sunrock.welcome");
      }

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.equals("SPELLBOOK")) {

          if (c.hasItem("jarko_spellbook")) {

            c.takeItem("jarko_spellbook");

            c.giveGold(800);

            c.giveItem("golden_ring");

            c.sayKey("npc.sunrock.spellbook.ok");

          } else c.sayKey("npc.sunrock.spellbook.need");

          return true;
        }

        if (k.equals("DIAMOND")) {

          c.askYesNo("sunrock_diamond");

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String s, boolean yes) {

        if (!"sunrock_diamond".equals(s)) return false;

        if (yes && c.hasItem("diamond")) {

          c.takeItem("diamond");

          c.giveGold(400);

          c.sayKey("npc.sunrock.diamond.ok");
        }

        return true;
      }
    };
  }

  public LordSunrock(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
