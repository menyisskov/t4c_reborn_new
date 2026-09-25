package com.perso.T4C.npc;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;

import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.helper.CollisionManager;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.mirror.MirrorTrials;
import com.perso.T4C.monster.EchoOfSelf;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.companion.CompanionManager;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcScriptRuntime;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;
import com.perso.T4C.spawn.Spawn;
import java.util.List;
import java.util.function.Supplier;

// T4C-0042: the Mirror of Echoes. Ysmera keeps the mirror that holds the echoes a character leaves
// behind each time they are reborn. She stands in the Colosseum (fast travel: "Colosseum") so the
// Echo can be fought on the arena floor. "Echo" summons the player's own reflection at their next
// trial (see mirror/MirrorTrials and monster/EchoOfSelf); after all ten trials, "call" brings the
// bound Echo along as a companion. Her greeting reads the player's own life back to them.
@Spawn(type = "MirrorwardenYsmera", x = 1736, y = 1840, z = 0, stationary = true, aggressive = false)
public final class MirrorwardenYsmera extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "MirrorwardenYsmera";

  public static final String DISPLAY_NAME = "${npc.mirrorwardenysmera}";

  public static final String SPRITE_BASE = null;

  /** Tiles between the player and where their Echo steps out of the glass. */
  private static final int SUMMON_DISTANCE_TILES = 4;

  // The declarative topics below double as the compendium's record of her keywords; the stateful
  // ones (ECHO, TRIALS, CALL) are intercepted by javaBehavior() first.
  private static final NpcSpec.DialogueTopic MIRROR_TOPIC = topic(0, 2);
  private static final NpcSpec.DialogueTopic ECHO_TOPIC = topic(1, 2);
  private static final NpcSpec.DialogueTopic TRIALS_TOPIC = topic(2, 2);
  private static final NpcSpec.DialogueTopic CALL_TOPIC = topic(3, 2);
  private static final NpcSpec.DialogueTopic YSMERA_TOPIC = topic(4, 2);

  private static final NpcSpec SPEC =
      new NpcSpec(
          ID,
          DISPLAY_NAME,
          SPRITE_BASE,
          List.of(
              new NpcSpec.Part(BodyPart.BODY, "WoWhiteRobe"),
              new NpcSpec.Part(BodyPart.ROBELEGS, "WoClothRobe"),
              new NpcSpec.Part(BodyPart.BOOT, "WoBlackLeatherBoots"),
              new NpcSpec.Part(BodyPart.BACK, "PupSeraphWhiteWings"),
              new NpcSpec.Part(BodyPart.WEAPON, "PupGemStaff")),
          0,
          List.of(),
          "${npc.welcome.mirrorwardenysmera}",
          List.of(MIRROR_TOPIC, ECHO_TOPIC, TRIALS_TOPIC, CALL_TOPIC, YSMERA_TOPIC),
          "MirrorwardenYsmeraNPC",
          new NpcSpec.CombatProfile(100, 1000000, 65, 67, 63, 1000000, 250, 65535, "1d23+16"));

  private final Supplier<CompanionManager> companions;

  public MirrorwardenYsmera(NpcContext context) throws GameException {
    super(SPEC, context);
    this.companions = context == null ? () -> null : context.companionManagerSupplier();
  }

  public static NpcSpec spec() {
    return SPEC;
  }

  private static NpcSpec.DialogueTopic topic(int index, int keywordCount) {
    List<String> keywords =
        java.util.stream.IntStream.range(0, keywordCount)
            .mapToObj(k -> "${npc.topic_keyword.mirrorwardenysmera." + index + "." + k + "}")
            .toList();
    return new NpcSpec.DialogueTopic(
        keywords, "${npc.topic.mirrorwardenysmera." + index + "}", List.of());
  }

  @Override
  protected NpcBehavior javaBehavior() {
    return new NpcBehavior() {
      @Override
      public void onConversationStart(NpcBehaviorContext context) {
        Player player = context.player();
        int cleared = MirrorTrials.clearedTier(player);
        String key =
            cleared == 0
                ? "npc.mirrorwardenysmera.greet.new"
                : cleared >= MirrorTrials.MAX_TIER
                    ? "npc.mirrorwardenysmera.greet.bound"
                    : "npc.mirrorwardenysmera.greet.returning";
        context.say(MirrorTrials.line(key, player, cleared));
      }

      @Override
      public boolean onKeyword(NpcBehaviorContext context, String keyword) {
        if (ScriptedNpc.matches(ECHO_TOPIC, keyword)) {
          offerEcho(context);
          return true;
        }
        if (ScriptedNpc.matches(TRIALS_TOPIC, keyword)) {
          describeTrials(context);
          return true;
        }
        if (ScriptedNpc.matches(CALL_TOPIC, keyword)) {
          callBoundEcho(context);
          return true;
        }
        return false;
      }
    };
  }

  private void offerEcho(NpcBehaviorContext context) {
    Player player = context.player();
    if (EchoOfSelf.isActive()) {
      context.say(MirrorTrials.line("npc.mirrorwardenysmera.echo.busy", player, 0));
      return;
    }
    int tier = MirrorTrials.nextTier(player);
    Vector2 spot = findEchoSpot(player);
    player.setQuestFlag(MirrorTrials.FLAG_CHALLENGE, tier);
    boolean summoned =
        NpcScriptRuntime.summon(
            MirrorTrials.ECHO_MONSTER_NAME, spot.x, spot.y, player.getCoordinates().getZ());
    if (!summoned) {
      context.say(MirrorTrials.line("npc.mirrorwardenysmera.echo.failed", player, tier));
      return;
    }
    String key =
        MirrorTrials.hasBoundEcho(player)
            ? "npc.mirrorwardenysmera.echo.rematch"
            : "npc.mirrorwardenysmera.echo.summoned";
    context.say(MirrorTrials.line(key, player, tier));
    context.endConversation();
  }

  private void describeTrials(NpcBehaviorContext context) {
    Player player = context.player();
    int cleared = MirrorTrials.clearedTier(player);
    if (cleared >= MirrorTrials.MAX_TIER) {
      context.say(MirrorTrials.line("npc.mirrorwardenysmera.trials.done", player, cleared));
      return;
    }
    int next = MirrorTrials.nextTier(player);
    context.say(
        I18n.message(
            "npc.mirrorwardenysmera.trials.progress",
            String.valueOf(player.getName()),
            cleared,
            next,
            MirrorTrials.tierStrengthPercent(next),
            player.getQuestFlag(MirrorTrials.FLAG_VICTORIES),
            player.getQuestFlag(MirrorTrials.FLAG_FALLS)));
  }

  private void callBoundEcho(NpcBehaviorContext context) {
    Player player = context.player();
    if (!MirrorTrials.hasBoundEcho(player)) {
      context.say(MirrorTrials.line("npc.mirrorwardenysmera.call.locked", player, 0));
      return;
    }
    CompanionManager manager = companions.get();
    if (manager == null) {
      context.say(MirrorTrials.line("npc.mirrorwardenysmera.echo.failed", player, 0));
      return;
    }
    if (manager.hasCompanion()) {
      context.say(MirrorTrials.line("npc.mirrorwardenysmera.call.occupied", player, 0));
      return;
    }
    if (manager.spawnCompanionFromDef(
        player, MirrorTrials.echoCompanion(player), player.getPositionVector(), 0)) {
      context.say(MirrorTrials.line("npc.mirrorwardenysmera.call.done", player, 0));
    }
  }

  /** An open tile a few steps from the player, so the Echo doesn't appear inside a wall. */
  private static Vector2 findEchoSpot(Player player) {
    Vector2 origin = player.getPositionVector();
    CollisionManager collisions = CollisionManager.getInstance();
    if (!collisions.isInitialized()) return new Vector2(origin);
    for (int ring = SUMMON_DISTANCE_TILES; ring >= 2; ring--) {
      for (int step = 0; step < 8; step++) {
        double angle = step * Math.PI / 4d;
        float x = origin.x + (float) Math.cos(angle) * ring * GRID_W;
        float y = origin.y + (float) Math.sin(angle) * ring * GRID_H;
        if (!collisions.hasCollisionNear(x, y, 1)
            && collisions.canMove(origin.x, origin.y, x, y)) {
          return new Vector2(x, y);
        }
      }
    }
    return new Vector2(origin);
  }
}
