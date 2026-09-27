package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.spawn.Spawn;
import com.perso.T4C.spawn.SpawnKind;
import java.util.List;

// T4C-0066: was unplaced (x=0,y=0,z=0) despite a fully-written "final test" fight (see
// javaBehavior() below). A first attempt at this fix placed this at an invented coordinate near
// the Oracle Invulnerable Guardian cluster - wrong: a Codex review on that PR caught that the
// canonical spawn already exists as a plain, non-scripted monster/GABRIELARCHONIS.java
// (DataMonster, no onHit fight logic) at (2628,2448,2), wired through SpawnGroup0075 next to the
// escape-door groups. That duplicate class + its SpawnGroup file are deleted; this class's own
// MONSTER-kind @Spawn below now owns that same canonical spot directly, the same two-@Spawn
// pattern BaneBlackblood.java already uses (one NPC-kind for dialogue, one MONSTER-kind, real
// coordinates, for combat/respawn).
@Spawn(type = "GabrielArchonis", x = 0, y = 0, z = 0, stationary = false, aggressive = false)
@Spawn(
    type = "GABRIELARCHONIS",
    x = 2628,
    y = 2448,
    z = 2,
    stationary = false,
    aggressive = false,
    kind = SpawnKind.MONSTER)
public final class GabrielArchonis extends ScriptedNpc {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String ID = "GabrielArchonis";

  public static final String DISPLAY_NAME = "${npc.gabrielarchonis}";

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
          "${npc.welcome.gabrielarchonis}",
          List.of(),
          "OracleAssistantNPC",
          new NpcSpec.CombatProfile(
              150,
              10000000,
              165,
              149,
              149,
              9999,
              1810,
              0,
              "if(target.hp>10?if(target.hp<200?target.hp-10:190-1d5):0)"));

  @Override
  protected NpcBehavior javaBehavior() {

    return new NpcBehavior() {

      @Override
      public void onHit(com.perso.T4C.npc.behavior.NpcBehaviorContext c) {

        long now = System.currentTimeMillis() / 1000L;

        int stamp = c.npcFlag("GABRIEL_CAN_BE_HIT");

        if (stamp == 0 || now - stamp >= 300) {

          stamp = (int) now;

          c.npcFlag("GABRIEL_CAN_BE_HIT", stamp);

          c.flag("__FLAG_CHARACTER_CAN_HIT_ASSISTANT", stamp);
        }

        if (c.flag("__FLAG_CHARACTER_CAN_HIT_ASSISTANT") != stamp) {

          c.systemMessageKey("npc.gabrielarchonis.cannot_hit");

          return;
        }

        c.npcFlag("GABRIEL_CAN_BE_HIT", stamp);

        c.flag("__FLAG_CHARACTER_CAN_HIT_ASSISTANT", stamp);

        if (c.player().getCurrentHp() < 50) {

          if (c.npcCurrentHp() > 750000) c.castSelfSpell(10717);
          else {

            c.flag("__FLAG_USER_HAS_DEFEATED_ASSISTANT", 1);

            c.sayKey("npc.gabrielarchonis.defeated");

            c.castTargetSpell(10471);

            c.selfDestructNpc();
          }

        } else if (c.npcCurrentHp() <= 9900000) c.castSelfSpell(10718);
      }
    };
  }

  public GabrielArchonis(NpcContext context) throws GameException {

    super(SPEC, context);
  }

  public static NpcSpec spec() {

    return SPEC;
  }
}
