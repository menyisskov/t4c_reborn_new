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
import java.util.List;

public final class OldHermit extends ScriptedNpc {

  public static final String ID = "OldHermit";

  public static final String DISPLAY_NAME = "${npc.oldhermit}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupBodyClothSet1"),
              new NpcSpec.Part(BodyPart.LEFT_ARM, "PupNakedArmL"),
              new NpcSpec.Part(BodyPart.RIGHT_ARM, "PupNakedArmR"),
              new NpcSpec.Part(BodyPart.BOOT, "PupBlackLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLeatherPants")),
          0,
          List.of(),
          "${npc.welcome.oldhermit}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.oldhermit.0.0}", "${npc.topic_keyword.oldhermit.0.1}"),
                  "${npc.topic.oldhermit.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.oldhermit.1.0}",
                      "${npc.topic_keyword.oldhermit.1.1}",
                      "${npc.topic_keyword.oldhermit.1.2}"),
                  "${npc.topic.oldhermit.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.oldhermit.2.0}"),
                  "${npc.topic.oldhermit.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.oldhermit.3.0}", "${npc.topic_keyword.oldhermit.3.1}"),
                  "${npc.topic.oldhermit.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.oldhermit.4.0}", "${npc.topic_keyword.oldhermit.4.1}"),
                  "${npc.topic.oldhermit.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.oldhermit.5.0}"),
                  "${npc.topic.oldhermit.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.oldhermit.6.0}"),
                  "${npc.topic.oldhermit.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.oldhermit.7.0}"),
                  "${npc.topic.oldhermit.7}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.oldhermit.8.0}", "${npc.topic_keyword.oldhermit.8.1}"),
                  "${npc.topic.oldhermit.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.oldhermit.9.0}", "${npc.topic_keyword.oldhermit.9.1}"),
                  "${npc.topic.oldhermit.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.oldhermit.10.0}"),
                  "${npc.topic.oldhermit.10}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.oldhermit.11.0}", "${npc.topic_keyword.oldhermit.11.1}"),
                  "${npc.topic.oldhermit.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.oldhermit.12.0}", "${npc.topic_keyword.oldhermit.12.1}"),
                  "${npc.topic.oldhermit.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.oldhermit.13.0}"),
                  "${npc.topic.oldhermit.13}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.oldhermit.14.0}"),
                  "${npc.topic.oldhermit.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.oldhermit.15.0}"),
                  "${npc.topic.oldhermit.15}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.oldhermit.16.0}",
                      "${npc.topic_keyword.oldhermit.16.1}",
                      "${npc.topic_keyword.oldhermit.16.2}",
                      "${npc.topic_keyword.oldhermit.16.3}"),
                  "${npc.topic.oldhermit.16}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.oldhermit.17.0}", "${npc.topic_keyword.oldhermit.17.1}"),
                  "${npc.topic.oldhermit.17}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.oldhermit.18.0}"),
                  "${npc.topic.oldhermit.18}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.oldhermit.19.0}"),
                  "${npc.topic.oldhermit.19}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.oldhermit.20.0}"),
                  "${npc.topic.oldhermit.20}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.oldhermit.21.0}"),
                  "${npc.topic.oldhermit.21}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.oldhermit.22.0}"),
                  "${npc.topic.oldhermit.22}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.oldhermit.23.0}"),
                  "${npc.topic.oldhermit.23}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.oldhermit.24.0}"),
                  "${npc.topic.oldhermit.24}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.oldhermit.25.0}"),
                  "${npc.topic.oldhermit.25}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.oldhermit.26.0}"),
                  "${npc.topic.oldhermit.26}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.oldhermit.27.0}",
                      "${npc.topic_keyword.oldhermit.27.1}",
                      "${npc.topic_keyword.oldhermit.27.2}",
                      "${npc.topic_keyword.oldhermit.27.3}",
                      "${npc.topic_keyword.oldhermit.27.4}"),
                  "${npc.topic.oldhermit.27}",
                  List.of())),
          "Old_Bald_Hermit",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if (c.flag("__FLAG_TALKED_TO_HERMIT") == 0) c.flag("__FLAG_TALKED_TO_HERMIT", 1);
      }

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

        if (!k.equals("TRUST")) return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);

        if (c.flag("__BALORK_BRAND") == 0) c.sayKey("npc.oldhermit.trust.no_brand");
        else if (c.flag("__FLAG_HERMIT_TRUST") > 0) c.sayKey("npc.oldhermit.trust.already");
        else if (c.hasItem("ring_of_trust") && c.flag("__FLAG_RING_OF_TRUST_GIVEN") == 1) {

          c.flag("__FLAG_HERMIT_TRUST", 1);

          c.takeItem("ring_of_trust");

          c.giveItem("amulet_of_precision");

          c.giveXp(4000);

          c.sayKey("npc.oldhermit.trust.complete");

        } else if (c.flag("__FLAG_RING_OF_TRUST_GIVEN") == 1) {

          c.flag("__FLAG_HERMIT_TRUST", 1);

          c.giveXp(1000);

          c.sayKey("npc.oldhermit.trust.lost");

        } else if (c.hasItem("ring_of_trust")) c.sayKey("npc.oldhermit.trust.unearned");
        else c.sayKey("npc.oldhermit.trust.missing");

        return true;
      }
    };
  }

  public OldHermit(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
