package com.perso.T4C.npc.companion;

import com.perso.T4C.player.BodyPart;
import java.util.List;
import lombok.Getter;

@Getter
public final class CompanionDef {

  private final String id;

  private final String displayName;

  private final List<Part> parts;

  private final String spriteBase;

  private final int baseHp;

  private final float hpPerLevel;

  private final int damageMin;

  private final int damageMax;

  private final float damagePerLevel;

  private final float attackCooldown;

  private final List<SpellEntry> spells;

  public CompanionDef(
      String id,
      String displayName,
      List<Part> parts,
      String spriteBase,
      int baseHp,
      float hpPerLevel,
      int damageMin,
      int damageMax,
      float damagePerLevel,
      float attackCooldown,
      List<SpellEntry> spells) {

    this.id = id;

    this.displayName = displayName;

    this.parts = List.copyOf(parts == null ? List.of() : parts);

    this.spriteBase = spriteBase;

    this.baseHp = baseHp;

    this.hpPerLevel = hpPerLevel;

    this.damageMin = damageMin;

    this.damageMax = damageMax;

    this.damagePerLevel = damagePerLevel;

    this.attackCooldown = attackCooldown;

    this.spells = List.copyOf(spells == null ? List.of() : spells);
  }

  public int resolveMaxHp(int level) {

    return Math.max(1, baseHp + Math.round(hpPerLevel * (Math.max(1, level) - 1)));
  }

  @Getter
  public static final class Part {

    private final BodyPart bodyPart;

    private final String spriteBase;

    public Part(BodyPart bodyPart, String spriteBase) {

      this.bodyPart = bodyPart;

      this.spriteBase = spriteBase;
    }
  }

  @Getter
  public static final class SpellEntry {

    private final String spellKey;

    private final CompanionSpellTrigger trigger;

    private final int priority;

    private final float cooldownSeconds;

    private final float healthThreshold;

    private final int minDamage;

    private final int maxDamage;

    private final float damagePerLevel;

    private final float rangeTiles;

    public SpellEntry(
        String spellKey,
        CompanionSpellTrigger trigger,
        int priority,
        float cooldownSeconds,
        float healthThreshold,
        int minDamage,
        int maxDamage,
        float damagePerLevel,
        float rangeTiles) {

      this.spellKey = spellKey;

      this.trigger = trigger;

      this.priority = priority;

      this.cooldownSeconds = cooldownSeconds;

      this.healthThreshold = healthThreshold;

      this.minDamage = minDamage;

      this.maxDamage = maxDamage;

      this.damagePerLevel = damagePerLevel;

      this.rangeTiles = rangeTiles;
    }

    public int rollAmount(java.util.random.RandomGenerator random, int level) {

      int low = Math.min(minDamage, maxDamage);

      int high = Math.max(minDamage, maxDamage);

      int base = low >= high ? low : low + random.nextInt(high - low + 1);

      return Math.max(0, base + Math.round(damagePerLevel * (Math.max(1, level) - 1)));
    }
  }
}
