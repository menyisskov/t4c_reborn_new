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
import java.util.Locale;

@Spawn(type = "Yrian", x = 2877, y = 169, z = 0, stationary = false, aggressive = false)
public final class Yrian extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "Yrian";

  public static final String DISPLAY_NAME = "${npc.yrian}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupMageRobe"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupGemStaff")),
          0,
          List.of(),
          "${npc.welcome.yrian}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.yrian.0.0}", "${npc.topic_keyword.yrian.0.1}"),
                  "${npc.topic.yrian.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.yrian.1.0}"), "${npc.topic.yrian.1}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.yrian.2.0}"), "${npc.topic.yrian.2}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.yrian.3.0}"), "${npc.topic.yrian.3}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.yrian.4.0}"), "${npc.topic.yrian.4}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.yrian.5.0}"), "${npc.topic.yrian.5}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.yrian.6.0}"), "${npc.topic.yrian.6}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.yrian.7.0}"), "${npc.topic.yrian.7}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.yrian.8.0}", "${npc.topic_keyword.yrian.8.1}"),
                  "${npc.topic.yrian.8}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.yrian.9.0}",
                      "${npc.topic_keyword.yrian.9.1}",
                      "${npc.topic_keyword.yrian.9.2}"),
                  "${npc.topic.yrian.9}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.yrian.10.0}"), "${npc.topic.yrian.10}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.yrian.11.0}",
                      "${npc.topic_keyword.yrian.11.1}",
                      "${npc.topic_keyword.yrian.11.2}"),
                  "${npc.topic.yrian.11}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.yrian.12.0}",
                      "${npc.topic_keyword.yrian.12.1}",
                      "${npc.topic_keyword.yrian.12.2}"),
                  "${npc.topic.yrian.12}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.yrian.13.0}"), "${npc.topic.yrian.13}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.yrian.14.0}", "${npc.topic_keyword.yrian.14.1}"),
                  "${npc.topic.yrian.14}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.yrian.15.0}"), "${npc.topic.yrian.15}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.yrian.16.0}"), "${npc.topic.yrian.16}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.yrian.17.0}"), "${npc.topic.yrian.17}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.yrian.18.0}", "${npc.topic_keyword.yrian.18.1}"),
                  "${npc.topic.yrian.18}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.yrian.19.0}", "${npc.topic_keyword.yrian.19.1}"),
                  "${npc.topic.yrian.19}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.yrian.20.0}"), "${npc.topic.yrian.20}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.yrian.21.0}", "${npc.topic_keyword.yrian.21.1}"),
                  "${npc.topic.yrian.21}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.yrian.22.0}"), "${npc.topic.yrian.22}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.yrian.23.0}"), "${npc.topic.yrian.23}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.yrian.24.0}"), "${npc.topic.yrian.24}", List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.yrian.25.0}",
                      "${npc.topic_keyword.yrian.25.1}",
                      "${npc.topic_keyword.yrian.25.2}",
                      "${npc.topic_keyword.yrian.25.3}"),
                  "${npc.topic.yrian.25}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.yrian.26.0}",
                      "${npc.topic_keyword.yrian.26.1}",
                      "${npc.topic_keyword.yrian.26.2}"),
                  "${npc.topic.yrian.26}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of("${npc.topic_keyword.yrian.27.0}", "${npc.topic_keyword.yrian.27.1}"),
                  "${npc.topic.yrian.27}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.yrian.28.0}",
                      "${npc.topic_keyword.yrian.28.1}",
                      "${npc.topic_keyword.yrian.28.2}",
                      "${npc.topic_keyword.yrian.28.3}",
                      "${npc.topic_keyword.yrian.28.4}"),
                  "${npc.topic.yrian.28}",
                  List.of())),
          "YrianNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return BEHAVIOR;
  }

  private static final NpcBehavior BEHAVIOR =
      new NpcBehavior() {

        @Override
        public void onConversationStart(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

          int p = c.flag("__FLAG_ADDON_STORYLINE_PROGRESS");

          if (p < 13) c.sayKey("npc.yrian.start.welcome");
          else if (p == 13) {

            c.sayKey("npc.yrian.start.odour");

            c.askYesNo("yrian_odour");

          } else if (p == 14) c.sayKey("npc.yrian.start.staff");
          else if (p == 15 && c.hasItem("solinae_staff")) {

            c.takeItem("solinae_staff");

            c.giveXp(75000);

            c.sayKey("npc.yrian.start.staff.returned");

          } else if (p == 15) c.sayKey("npc.yrian.start.staff.missing");
          else if (p == 16) c.sayKey("npc.yrian.start.nomad");
          else if (p < 42) c.sayKey("npc.yrian.start.travel");
          else c.sayKey("npc.yrian.start.healed");
        }

        @Override
        public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

          String keyword = text == null ? "" : text.toUpperCase(Locale.ROOT);

          if (keyword.equals("COMMANDER")) {

            if (c.flag("__FLAG_ADDON_STORYLINE_PROGRESS") == 13)
              c.flag("__FLAG_ADDON_STORYLINE_PROGRESS", 14);

            return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
          }

          if (keyword.equals("CRYPT")) {

            if (c.flag("__FLAG_ADDON_STORYLINE_PROGRESS") == 15) {

              c.giveItem("guardian_ring_of_vitality");

              c.giveItem("gem_of_vitality");

              c.flag("__FLAG_ADDON_STORYLINE_PROGRESS", 16);
            }

            return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
          }

          if (keyword.equals("STONE OF LIFE")) {

            long now = System.currentTimeMillis() / 1000L;

            int quest = c.globalFlag("__QUEST_STONE_OF_LIFE");

            if (quest > now && c.hasItem("stone_of_life")) {

              c.takeItem("stone_of_life");

              c.giveItem("twisted_dagger");

              c.globalFlag("__QUEST_STONE_OF_LIFE", 0);

              c.sayKey("npc.yrian.stone.retrieved");

            } else if (quest > now) c.sayKey("npc.yrian.stone.stolen");
            else {

              c.globalFlag("__QUEST_STONE_OF_LIFE", (int) (now + 7200));

              c.sayKey("npc.yrian.stone.request");
            }

            return true;
          }

          if (!keyword.equals("HEAL") && !keyword.contains(" MEND ")) return false;

          if (c.player().getCurrentHp() >= c.player().getMaxHp()) {

            c.sayKey("message.no_healing_needed");

          } else if (c.player().getLevel() < 6) {

            c.player().applyHeal(c.player().getMaxHp(), c.player().getMaxHp());

            c.castTargetSpell("spell.npc_cantrip_serious_heal");

            c.sayKey("message.healed_dialog");

          } else {

            c.sayKey("npc.topic.yrian.27");

            c.askYesNo("yrian_heal");
          }

          return true;
        }

        @Override
        public boolean onYesNo(
            com.perso.T4C.npc.behavior.NpcBehaviorContext c, String state, boolean answer) {

          if ("yrian_odour".equals(state)) {

            c.sayKey(answer ? "npc.yrian.odour.yes" : "npc.yrian.odour.no");

            return true;
          }

          if (!"yrian_heal".equals(state)) return false;

          if (!answer) return true;

          int cost = Math.max(0, (c.player().getMaxHp() - c.player().getCurrentHp()) / 2);

          if (c.player().getGold() < cost) {

            c.sayKey("npc.kiran.heal.poor");

            return true;
          }

          c.player().addGold(-cost);

          c.player().applyHeal(c.player().getMaxHp(), c.player().getMaxHp());

          c.castTargetSpell("spell.npc_cantrip_serious_heal");

          c.sayKey("message.healed_dialog");

          return true;
        }
      };

  public Yrian(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
