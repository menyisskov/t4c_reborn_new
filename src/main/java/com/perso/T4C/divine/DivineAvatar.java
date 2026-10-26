package com.perso.T4C.divine;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.MonsterLifecycle;
import com.perso.T4C.npc.core.NpcScriptRuntime;
import com.perso.T4C.player.Player;
import java.util.concurrent.ThreadLocalRandom;

/** Divine trials use their authored tier damage for ranged attacks as well as melee. */
@com.perso.T4C.monster.core.NoRecoveryLoot
public class DivineAvatar extends DataMonster implements MonsterLifecycle {
  private long nextAfflictionAt;

  public DivineAvatar(MonsterDef definition, float x, float y) throws GameException {
    super(definition, x, y);
    DivinePantheon.all().stream()
        .filter(g -> definition.getName().equals("Divine " + g.name()))
        .filter(g -> !DivinePantheon.seraphQuestKeys(g).isEmpty())
        .findFirst()
        .ifPresent(g -> lootTable = DivinePantheon.seraphLoot(g, definition));
  }

  @Override
  protected boolean canCastMonsterSpell(int spellId) {
    return !DivineAfflictions.isAffliction(spellId)
        || (!isFightingCompanion() && System.currentTimeMillis() >= nextAfflictionAt);
  }

  @Override
  protected int rollMonsterSpellDamage(int spellId) {
    if (DivineAfflictions.isAffliction(spellId)) {
      nextAfflictionAt = System.currentTimeMillis() + DivineAfflictions.CAST_INTERVAL_MILLIS;
      return 0;
    }
    var god = DivinePantheon.findJudgment(spellId);
    if (god != null)
      return ThreadLocalRandom.current()
          .nextInt(god.judgmentDamageMin(), god.judgmentDamageMax() + 1);
    MonsterDef d = getDefinition();
    return ThreadLocalRandom.current().nextInt(d.getHitDamageMin(), d.getHitDamageMax() + 1);
  }

  @Override
  public void setRespawnDelayMillis(long delayMillis) {
    // The manager normally randomizes the next cycle to 18-30 seconds after a respawn.
    // Divine trials retain their authored shrine interval for every cycle.
    super.setRespawnDelayMillis(getDefinition().getRespawnTime());
  }

  @Override
  public NpcScriptRuntime.Effects onDeath(Player player) {
    if (player != null) {
      long readyAt = deathTime + getDefinition().getRespawnTime();
      player.setQuestFlag(cooldownFlag() + ".high", (int) (readyAt >>> 32));
      player.setQuestFlag(cooldownFlag() + ".low", (int) readyAt);
    }
    return NpcScriptRuntime.Effects.empty();
  }

  @Override
  public NpcScriptRuntime.Effects onSpawn(Player player) {
    if (player != null) {
      long readyAt =
          ((long) player.getQuestFlag(cooldownFlag() + ".high") << 32)
              | (player.getQuestFlag(cooldownFlag() + ".low") & 0xffffffffL);
      if (readyAt > System.currentTimeMillis()) {
        // Changing maps rebuilds the monster manager. Restore the waiting shrine without
        // firing another death event, granting loot, or restarting its clock.
        isDead = true;
        health = 0;
        deathTime = readyAt - getDefinition().getRespawnTime();
        corpseHiddenAtMs = 0L;
        animations.startDeath();
        animations.update(1f, false);
      }
    }
    return NpcScriptRuntime.Effects.empty();
  }

  private String cooldownFlag() {
    return "divine.respawn."
        + getDefinition().getName().toLowerCase(java.util.Locale.ROOT).replace(' ', '_');
  }
}
