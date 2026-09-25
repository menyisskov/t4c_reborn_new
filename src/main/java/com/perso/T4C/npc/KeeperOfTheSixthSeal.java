package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.item.definition.WyrmScales;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import java.util.List;

// T4C-0047, "The Wyrm Scales": stands in the Colosseum near the Mirror of Echoes (T4C-0042,
// MirrorwardenYsmera at 1736,1840) and ColosseumClerk (1730,1830) - a sealed archway a few steps
// from both. Each of the five existing Elder Wyrms (Rootcrown/Pyreclaw/Mistwing/Duskmaw/
// Galecrest, T4C-0029/0038) rarely drops its own named scale (see item/definition/WyrmScales.java).
// Bring one of each and the Keeper opens the sixth seal: The Convergent Wyrm
// (assets/monsters/convergent_wyrm.json), a level-750 boss using every element, summoned right
// where the Keeper stands. Repeatable - the seal isn't a one-time unlock, it closes again the
// moment the Convergent Wyrm is dealt with, same as any other world boss encounter.
@Spawn(
    type = "KeeperOfTheSixthSeal",
    x = 1724,
    y = 1836,
    z = 0,
    stationary = true,
    aggressive = false)
public final class KeeperOfTheSixthSeal extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "KeeperOfTheSixthSeal";
  public static final String DISPLAY_NAME = "${npc.keeperofthesixthseal}";
  public static final String SPRITE_BASE = null;

  private static final String CONVERGENT_WYRM_NAME = "The Convergent Wyrm";

  private static final List<String> SCALE_KEYS =
      List.of(
          WyrmScales.ROOTCROWN_KEY,
          WyrmScales.PYRECLAW_KEY,
          WyrmScales.MISTWING_KEY,
          WyrmScales.DUSKMAW_KEY,
          WyrmScales.GALECREST_KEY);

  private static final NpcSpec.DialogueTopic SEAL_TOPIC =
      new NpcSpec.DialogueTopic(
          List.of(
              "${npc.topic_keyword.keeperofthesixthseal.0.0}",
              "${npc.topic_keyword.keeperofthesixthseal.0.1}"),
          "${npc.topic.keeperofthesixthseal.0}",
          List.of());

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
          "${npc.welcome.keeperofthesixthseal}",
          List.of(SEAL_TOPIC),
          "KeeperOfTheSixthSealNPC",
          new NpcSpec.CombatProfile(100, 1000000, 65, 67, 63, 1000000, 250, 65535, "1d23+16"));

  public KeeperOfTheSixthSeal(NpcContext context) throws GameException {
    super(SPEC, context);
  }

  public static NpcSpec spec() {
    return SPEC;
  }

  @Override
  protected NpcBehavior javaBehavior() {
    return new NpcBehavior() {
      @Override
      public boolean onKeyword(NpcBehaviorContext context, String keyword) {
        if (!ScriptedNpc.matches(SEAL_TOPIC, keyword)) {
          return false;
        }
        openSeal(context);
        return true;
      }
    };
  }

  private static void openSeal(NpcBehaviorContext context) {
    for (String scaleKey : SCALE_KEYS) {
      if (!context.hasItem(scaleKey)) {
        context.say(I18n.resolve("${npc.keeperofthesixthseal.missing_scales}"));
        return;
      }
    }
    for (String scaleKey : SCALE_KEYS) {
      context.takeItem(scaleKey);
    }
    boolean summoned =
        context.summon(CONVERGENT_WYRM_NAME, context.npcTileX(), context.npcTileY(), 0);
    context.say(
        I18n.resolve(
            summoned
                ? "${npc.keeperofthesixthseal.seal_opens}"
                : "${npc.keeperofthesixthseal.seal_fails}"));
  }
}
