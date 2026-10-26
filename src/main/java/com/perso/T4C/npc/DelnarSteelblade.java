package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.StaticDialogueBehavior;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

@Spawn(type = "DelnarSteelblade", x = 315, y = 700, z = 0, stationary = false, aggressive = false)
public final class DelnarSteelblade extends ScriptedNpc {
  private static final String SKULL_TOTAL_FLAG = "__QUEST_DELNAR_SKULLS_TURNED_IN";
  private static final List<String> CHAINMAIL_REWARDS =
      List.of(
          "chainmail_armor",
          "chainmail_boots",
          "chainmail_gauntlets",
          "chainmail_helmet",
          "chainmail_leggings");
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "DelnarSteelblade";

  public static final String DISPLAY_NAME = "${npc.delnarsteelblade}";

  public static final String SPRITE_BASE = null;

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupChainMailBody"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots"),
              new NpcSpec.Part(BodyPart.LEGS, "PupChainMailLegs"),
              new NpcSpec.Part(BodyPart.HEAD, "PupChainMailCoif"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupNormalSword"),
              new NpcSpec.Part(BodyPart.BACK, "PupRedCape"),
              new NpcSpec.Part(BodyPart.RIGHT_HAND, "PupLeatherGloveR"),
              new NpcSpec.Part(BodyPart.LEFT_HAND, "PupLeatherGloveL")),
          0,
          List.of(),
          "${npc.welcome.delnarsteelblade}",
          List.of(
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.delnarsteelblade.0.0}",
                      "${npc.topic_keyword.delnarsteelblade.0.1}",
                      "${npc.topic_keyword.delnarsteelblade.0.2}"),
                  "${npc.topic.delnarsteelblade.0}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.delnarsteelblade.1.0}",
                      "${npc.topic_keyword.delnarsteelblade.1.1}"),
                  "${npc.topic.delnarsteelblade.1}",
                  List.of()),
              new NpcSpec.DialogueTopic(
                  List.of(
                      "${npc.topic_keyword.delnarsteelblade.2.0}",
                      "${npc.topic_keyword.delnarsteelblade.2.1}",
                      "${npc.topic_keyword.delnarsteelblade.2.2}",
                      "${npc.topic_keyword.delnarsteelblade.2.3}",
                      "${npc.topic_keyword.delnarsteelblade.2.4}"),
                  "${npc.topic.delnarsteelblade.2}",
                  List.of())),
          "DelnarSteelbladeNPC",
          new NpcSpec.CombatProfile(100, 1000000, 20, 22, 24, 1000000, 250, 65535, "1d23+16"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onAttack(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        int roll = (int) (Math.random() * 21);

        if (roll <= 2) c.shoutKey("npc.delnarsteelblade.attack." + roll);
      }

      @Override
      public boolean onKeyword(com.perso.T4C.npc.behavior.NpcBehaviorContext c, String text) {

        String keyword = text == null ? "" : text.toUpperCase(java.util.Locale.ROOT);
        if (keyword.contains("VILLAIN") && keyword.contains("SKULL")) {
          if (c.itemCount("villain_skull") > 0) {
            c.sayKey("npc.delnar.skulls.ask");
            c.askYesNo("delnar_skulls");
          } else c.sayKey("npc.topic.delnarsteelblade.1");
          return true;
        }
        return StaticDialogueBehavior.INSTANCE.onKeyword(c, text);
      }

      @Override
      public boolean onYesNo(
          com.perso.T4C.npc.behavior.NpcBehaviorContext c, String subject, boolean yes) {
        if (!"delnar_skulls".equals(subject)) return false;
        if (!yes) return true;

        int count = c.itemCount("villain_skull");
        int total = c.flag(SKULL_TOTAL_FLAG);
        int xp = 0;
        for (int i = 0; i < count; i++) {
          c.takeItem("villain_skull");
          total++;
          xp += total == 100 ? 500_000 : total == 25 ? 100_000 : total == 10 ? 25_000 : 10_000;
        }
        c.flag(SKULL_TOTAL_FLAG, total);
        if (xp > 0) {
          c.giveXp(xp);
          c.giveItem(
              CHAINMAIL_REWARDS.get(
                  java.util.concurrent.ThreadLocalRandom.current().nextInt(CHAINMAIL_REWARDS.size())));
        }
        c.sayKey(count > 0 ? "npc.delnar.skulls.done" : "npc.delnar.skulls.none");
        return true;
      }

      @Override
      public void onAttacked(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        int roll = (int) (Math.random() * 21);

        if (roll <= 2) c.shoutKey("npc.delnarsteelblade.attacked." + roll);
      }

      @Override
      public void onDeath(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        if ((int) (Math.random() * 3) == 0) c.shoutKey("npc.delnarsteelblade.death");
      }
    };
  }

  public DelnarSteelblade(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
