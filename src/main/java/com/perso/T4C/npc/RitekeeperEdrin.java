package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.behavior.RebirthBehavior;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;
import java.util.Locale;

/** Stonecrest's direct route to the Oracle's final trial and rebirth audience. */
@Spawn(type = "RitekeeperEdrin", x = 225, y = 735, z = 0, stationary = true, aggressive = false)
public final class RitekeeperEdrin extends ScriptedNpc {
  public static final String ID = "RitekeeperEdrin";
  public static final String DISPLAY_NAME = "${npc.ritekeeperedrin}";
  public static final String SPRITE_BASE = null;

  private static final String ALIGNMENT = "__QUEST_FIXED_ALIGNMENT";
  private static final String ISLAND_ACCESS = "__QUEST_ISLAND_ACCESS";
  private static final String ORACLE_CONVERSATION = "__FLAG_CONVERSATION_WITH_ORACLE";
  private static final String ASSISTANT_DEFEATED = "__FLAG_USER_HAS_DEFEATED_ASSISTANT";
  private static final String REMORTS = "__FLAG_NUMBER_OF_REMORTS";

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "PupWhiteRobe"),
              new NpcSpec.Part(BodyPart.BOOT, "PupLeatherBoots")),
          0,
          List.of(),
          "${npc.welcome.ritekeeperedrin}",
          List.of(
              topic("path", "${npc.ritekeeperedrin.path}"),
              topic("light", "${npc.ritekeeperedrin.light}"),
              topic("shadow", "${npc.ritekeeperedrin.shadow}"),
              topic("prepare", "${npc.ritekeeperedrin.prepared}"),
              topic("trial", "${npc.ritekeeperedrin.trial}"),
              topic("oracle", "${npc.ritekeeperedrin.oracle}")),
          "RitekeeperNPC",
          new NpcSpec.CombatProfile(100, 1000000, 65, 67, 63, 1000000, 250, 65535, "1d23+16"));

  private static NpcSpec.DialogueTopic topic(String keyword, String response) {
    return new NpcSpec.DialogueTopic(List.of(keyword), response, List.of());
  }

  public RitekeeperEdrin(NpcContext context) throws GameException {
    super(SPEC, context);
  }

  public static NpcSpec spec() {
    return SPEC;
  }

  @Override
  protected NpcBehavior javaBehavior() {
    return new NpcBehavior() {
      @Override
      public void onConversationStart(NpcBehaviorContext c) {
        c.sayKey("npc.welcome.ritekeeperedrin");
      }

      @Override
      public boolean onKeyword(NpcBehaviorContext c, String input) {
        String keyword = input == null ? "" : input.trim().toLowerCase(Locale.ROOT);
        return switch (keyword) {
          case "path", "rebirth" -> {
            c.sayKey("npc.ritekeeperedrin.path");
            yield true;
          }
          case "light", "good" -> {
            selectPath(c, 1);
            yield true;
          }
          case "shadow", "evil" -> {
            selectPath(c, -1);
            yield true;
          }
          case "prepare", "key" -> {
            prepare(c);
            yield true;
          }
          case "trial", "fight" -> {
            enterTrial(c);
            yield true;
          }
          case "oracle" -> {
            visitOracle(c);
            yield true;
          }
          default -> false;
        };
      }
    };
  }

  private static void selectPath(NpcBehaviorContext c, int alignment) {
    c.karma(alignment * 100);
    c.flag(ALIGNMENT, alignment);
    c.sayKey(
        alignment > 0 ? "npc.ritekeeperedrin.light" : "npc.ritekeeperedrin.shadow");
  }

  private static boolean prepare(NpcBehaviorContext c) {
    if (!RebirthBehavior.canRebirth(c.player())) {
      c.sayKey("npc.ritekeeperedrin.limit");
      return false;
    }
    if (c.karma() == 0) {
      c.sayKey("npc.ritekeeperedrin.choose");
      return false;
    }
    c.flag(ALIGNMENT, c.karma() > 0 ? 1 : -1);
    c.flag(ISLAND_ACCESS, Math.max(2, c.flag(ISLAND_ACCESS)));
    c.flag(ORACLE_CONVERSATION, 1);
    if (!c.hasItem("trial_key")) c.giveItem("trial_key");
    if (!c.hasItem("scroll_of_stonecrest")) c.giveItem("scroll_of_stonecrest");
    c.sayKey("npc.ritekeeperedrin.prepared");
    return true;
  }

  private static void enterTrial(NpcBehaviorContext c) {
    if (!prepare(c)) return;
    int requiredLevel = RebirthBehavior.requiredLevelFor(c.flag(REMORTS) + 1);
    if (c.player().getLevel() < requiredLevel) {
      c.sayKey("npc.ritekeeperedrin.level", requiredLevel);
      return;
    }
    if (c.flag(ASSISTANT_DEFEATED) == 1) {
      c.sayKey("npc.ritekeeperedrin.already_proven");
      return;
    }
    c.sayKey(c.karma() > 0 ? "npc.ritekeeperedrin.trial" : "npc.ritekeeperedrin.dark_trial");
    if (c.karma() > 0) c.teleport(2628, 2456, 2); // Gabriel's chamber.
    else c.teleport(2660, 2424, 2); // Gaenen's chamber.
  }

  private static void visitOracle(NpcBehaviorContext c) {
    if (c.flag(ASSISTANT_DEFEATED) != 1) {
      c.sayKey("npc.ritekeeperedrin.not_proven");
      return;
    }
    if (!RebirthBehavior.canRebirth(c.player())) {
      c.sayKey("npc.ritekeeperedrin.limit");
      return;
    }
    c.sayKey("npc.ritekeeperedrin.oracle");
    c.teleport(2721, 2192, 2);
  }
}
