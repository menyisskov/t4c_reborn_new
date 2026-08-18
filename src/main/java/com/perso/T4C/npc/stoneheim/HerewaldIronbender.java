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

public final class HerewaldIronbender extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "HerewaldIronbender";

  public static final String DISPLAY_NAME = "${npc.herewaldironbender}";

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
          "${npc.welcome.herewaldironbender}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.herewaldironbender.0.0}",
                      "${npc.topic_keyword.herewaldironbender.0.1}"),
                  "${npc.topic.herewaldironbender.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.herewaldironbender.1.0}",
                      "${npc.topic_keyword.herewaldironbender.1.1}"),
                  "${npc.topic.herewaldironbender.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.herewaldironbender.2.0}",
                      "${npc.topic_keyword.herewaldironbender.2.1}"),
                  "${npc.topic.herewaldironbender.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.herewaldironbender.3.0}",
                      "${npc.topic_keyword.herewaldironbender.3.1}",
                      "${npc.topic_keyword.herewaldironbender.3.2}"),
                  "${npc.topic.herewaldironbender.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.herewaldironbender.4.0}",
                      "${npc.topic_keyword.herewaldironbender.4.1}",
                      "${npc.topic_keyword.herewaldironbender.4.2}"),
                  "${npc.topic.herewaldironbender.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.herewaldironbender.5.0}"),
                  "${npc.topic.herewaldironbender.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.herewaldironbender.6.0}"),
                  "${npc.topic.herewaldironbender.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.herewaldironbender.7.0}",
                      "${npc.topic_keyword.herewaldironbender.7.1}",
                      "${npc.topic_keyword.herewaldironbender.7.2}",
                      "${npc.topic_keyword.herewaldironbender.7.3}"),
                  "${npc.topic.herewaldironbender.7}",
                  List.of())),
          "HerewaldIronbenderNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        c.sayKey("npc.herewald.shop.ask");

        c.askYesNo("herewald_shop");
      }

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (k.equals("BUY") || k.equals("SHOP") || k.equals("ARMOR")) {

          c.sayKey("npc.herewald.shop.ask");

          c.askYesNo("herewald_shop");

          return true;
        }

        if (k.equals("ORACLE")) {

          c.sayKey(
              c.flag("__QUEST_FIXED_ALIGNMENT") >= 1
                  ? "npc.herewald.oracle.good"
                  : "npc.herewald.oracle.bad");

          return true;
        }

        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String s, boolean yes) {

        if ("herewald_shop".equals(s)) {

          if (yes)
            c.openShop(
                java.util.List.of(
                    "chainmail",
                    "chainmail_gloves",
                    "chainmail_coif",
                    "chainmail_girdle",
                    "chainmail_leggings",
                    "chainmail_boots",
                    "scale_mail",
                    "scalemail_gauntlets",
                    "scalemail_helmet",
                    "scalemail_protector",
                    "scalemail_leggings",
                    "scalemail_boots",
                    "large_shield",
                    "tower_shield"));

          return true;
        }

        return false;
      }
    };
  }

  public HerewaldIronbender(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
