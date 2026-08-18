package com.perso.T4C.npc.addon;

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
import java.util.List;

public final class VincentSwiftblade extends ScriptedNpc {

  public static final String ID = "VincentSwiftblade";

  public static final String DISPLAY_NAME = "${npc.vincentswiftblade}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupStuddedBodyArmor"),
              new NpcSpec.Part(BodyPart.BOOT, "PupBlackLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupLeatherPants"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupBattleSword")),
          0,
          List.of(),
          "${npc.welcome.vincentswiftblade}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.vincentswiftblade.0.0}"),
                  "${npc.topic.vincentswiftblade.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.vincentswiftblade.1.0}"),
                  "${npc.topic.vincentswiftblade.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.vincentswiftblade.2.0}",
                      "${npc.topic_keyword.vincentswiftblade.2.1}"),
                  "${npc.topic.vincentswiftblade.2}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.vincentswiftblade.3.0}"),
                  "${npc.topic.vincentswiftblade.3}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.vincentswiftblade.4.0}",
                      "${npc.topic_keyword.vincentswiftblade.4.1}"),
                  "${npc.topic.vincentswiftblade.4}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.vincentswiftblade.5.0}"),
                  "${npc.topic.vincentswiftblade.5}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.vincentswiftblade.6.0}",
                      "${npc.topic_keyword.vincentswiftblade.6.1}"),
                  "${npc.topic.vincentswiftblade.6}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.vincentswiftblade.7.0}",
                      "${npc.topic_keyword.vincentswiftblade.7.1}",
                      "${npc.topic_keyword.vincentswiftblade.7.2}",
                      "${npc.topic_keyword.vincentswiftblade.7.3}",
                      "${npc.topic_keyword.vincentswiftblade.7.4}"),
                  "${npc.topic.vincentswiftblade.7}",
                  List.of())),
          "VincentSwiftbladeNPC",
          new NpcSpec.CombatProfile(60, 1000000, 20, 22, 24, 100000, 730, 65535, "1d90+69"));

  @Override
  protected NpcBehavior javaBehavior() {

    return BEHAVIOR;
  }

  private static final NpcBehavior BEHAVIOR =
      new NpcBehavior() {

        @Override
        public void onConversationStart(NpcBehaviorContext c) {

          if (c.flag("ADDON_MERCENARY_LEADER_DEFEAT") == 1) c.sayKey("npc.vincent.reward.ready");
          else if (c.flag("ADDON_CAMP_ASSAULT") == 1) c.sayKey("npc.vincent.progress.assault");
          else {

            c.sayKey("npc.vincent.welcome");

            c.askYesNo("mission");
          }
        }

        @Override
        public boolean onYesNo(NpcBehaviorContext c, String s, boolean yes) {

          if (!"mission".equals(s)) return false;

          c.sayKey(yes ? "npc.vincent.accept" : "npc.vincent.decline");

          if (yes) c.flag("ADDON_CAMP_ASSAULT", 1);

          return true;
        }

        @Override
        public boolean onKeyword(NpcBehaviorContext c, String text) {

          String k = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);

          if (k.equals("DETAILS")) {

            c.sayKey("npc.vincent.details");

            if (c.flag("ADDON_CAMP_ASSAULT") == 1) c.flag("ADDON_CAMP_ASSAULT", 2);

            return true;
          }

          if (k.equals("REWARD")) {

            if (c.flag("ADDON_MERCENARY_LEADER_DEFEAT") == 1) {

              c.player().addGold(500);

              c.flag("ADDON_MERCENARY_LEADER_DEFEAT", 0);

              c.sayKey("npc.vincent.reward.given");

            } else c.sayKey("npc.vincent.reward.none");

            return true;
          }

          if (k.contains("TARNIAN") || k.contains("WOLFMANE")) {

            c.sayKey("npc.vincent.tarnian");

            return true;
          }

          if (k.contains("LIGHTHAVEN")) {

            c.sayKey("npc.vincent.lighthaven");

            return true;
          }

          if (k.contains("OLIN") || k.contains("HAAD")) {

            c.sayKey("npc.vincent.olin");

            return true;
          }

          if (k.equals("NAME")) {

            c.sayKey("npc.vincent.name");

            return true;
          }

          if (k.equals("WORK") || k.equals("OCCUPATION")) {

            c.sayKey("npc.vincent.work");

            return true;
          }

          return false;
        }
      };

  public VincentSwiftblade(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
