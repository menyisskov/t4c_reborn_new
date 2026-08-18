package com.perso.T4C.spell;

import com.perso.T4C.i18n.I18n;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
public class SpellData {
  public static final int ATTACK_PHYSICAL = 1;
  public static final int ATTACK_MENTAL = 2;
  private final String name;
  private final String description;
  private final String manaCost;
  private final int radius;
  private final int minInt;
  private final int minWis;
  private final int minLevel;
  private final boolean isAttack;
  private final boolean lineOfSight;
  private final String iconId;
  private final String projectileSpell;
  private final String impactSpell;
  private final int minDamage;
  private final int maxDamage;
  private final String sound;
  private final String soundImpact;
  private final int cooldownSeconds;
  private final String duration;
  private final String frequency;
  private final int price;
  private final SpellBuff buff;
  private final int spellId;
  private final int element;
  private final int targetType;
  private final int attackType;
  private final String successRate;
  private final String mentalExhaustion;
  private final String physicalExhaustion;
  private final String attackExhaustion;
  private final int visualEffect;
  private final int visualEffectTarget;
  private final boolean pvp;
  private final List<T4cEffect> t4cEffects;

  public String getKey() {
    String key = I18n.keyOf(name);
    if (key == null) return name;
    while (key.startsWith("spell.spell_")) {
      key = "spell." + key.substring("spell.spell_".length());
    }
    return key;
  }

  public SpellData withPrice(int newPrice) {
    return new SpellData(
        name,
        description,
        manaCost,
        radius,
        minInt,
        minWis,
        minLevel,
        isAttack,
        lineOfSight,
        iconId,
        projectileSpell,
        impactSpell,
        minDamage,
        maxDamage,
        sound,
        soundImpact,
        cooldownSeconds,
        duration,
        frequency,
        newPrice,
        buff,
        spellId,
        element,
        targetType,
        attackType,
        successRate,
        mentalExhaustion,
        physicalExhaustion,
        attackExhaustion,
        visualEffect,
        visualEffectTarget,
        pvp,
        t4cEffects);
  }

  public SpellData withT4cEffects(List<T4cEffect> effects) {
    return new SpellData(
        name,
        description,
        manaCost,
        radius,
        minInt,
        minWis,
        minLevel,
        isAttack,
        lineOfSight,
        iconId,
        projectileSpell,
        impactSpell,
        minDamage,
        maxDamage,
        sound,
        soundImpact,
        cooldownSeconds,
        duration,
        frequency,
        price,
        buff,
        spellId,
        element,
        targetType,
        attackType,
        successRate,
        mentalExhaustion,
        physicalExhaustion,
        attackExhaustion,
        visualEffect,
        visualEffectTarget,
        pvp,
        effects);
  }

  public SpellData(
      String name,
      String description,
      String manaCost,
      int radius,
      int minInt,
      int minWis,
      int minLevel,
      boolean isAttack,
      boolean lineOfSight,
      String iconId,
      String projectileSpell,
      String impactSpell,
      int minDamage,
      int maxDamage,
      String sound,
      String soundImpact,
      int cooldownSeconds,
      String duration,
      String frequency,
      int price,
      SpellBuff buff,
      int spellId,
      int element,
      int targetType,
      int attackType,
      String successRate,
      String mentalExhaustion,
      String physicalExhaustion,
      String attackExhaustion,
      int visualEffect,
      int visualEffectTarget,
      boolean pvp,
      List<T4cEffect> t4cEffects) {
    this.name = name;
    this.description = description;
    this.manaCost = manaCost;
    this.radius = radius;
    this.minInt = minInt;
    this.minWis = minWis;
    this.minLevel = minLevel;
    this.isAttack = isAttack;
    this.lineOfSight = lineOfSight;
    this.iconId = iconId;
    this.projectileSpell = projectileSpell;
    this.impactSpell = impactSpell;
    this.minDamage = minDamage;
    this.maxDamage = maxDamage;
    this.sound = sound;
    this.soundImpact = soundImpact;
    this.cooldownSeconds = cooldownSeconds;
    this.duration = duration;
    this.frequency = frequency;
    this.price = price;
    this.buff = buff;
    this.spellId = spellId;
    this.element = element;
    this.targetType = targetType;
    this.attackType = attackType;
    this.successRate = successRate;
    this.mentalExhaustion = mentalExhaustion;
    this.physicalExhaustion = physicalExhaustion;
    this.attackExhaustion = attackExhaustion;
    this.visualEffect = visualEffect;
    this.visualEffectTarget = visualEffectTarget;
    this.pvp = pvp;
    this.t4cEffects = t4cEffects != null ? t4cEffects : Collections.emptyList();
  }

  public SpellData(
      String name,
      String description,
      String manaCost,
      int radius,
      int minInt,
      int minWis,
      int minLevel,
      boolean isAttack,
      boolean lineOfSight,
      String iconId,
      String projectileSpell,
      String impactSpell,
      int minDamage,
      int maxDamage,
      String sound,
      String soundImpact,
      int cooldownSeconds,
      String duration,
      int price,
      SpellBuff buff) {
    this(
        name,
        description,
        manaCost,
        radius,
        minInt,
        minWis,
        minLevel,
        isAttack,
        lineOfSight,
        iconId,
        projectileSpell,
        impactSpell,
        minDamage,
        maxDamage,
        sound,
        soundImpact,
        cooldownSeconds,
        duration,
        null,
        price,
        buff);
  }

  public SpellData(
      String name,
      String description,
      String manaCost,
      int radius,
      int minInt,
      int minWis,
      int minLevel,
      boolean isAttack,
      boolean lineOfSight,
      String iconId,
      String projectileSpell,
      String impactSpell,
      int minDamage,
      int maxDamage,
      String sound,
      String soundImpact,
      int cooldownSeconds,
      String duration,
      String frequency,
      int price,
      SpellBuff buff) {
    this(
        name,
        description,
        manaCost,
        radius,
        minInt,
        minWis,
        minLevel,
        isAttack,
        lineOfSight,
        iconId,
        projectileSpell,
        impactSpell,
        minDamage,
        maxDamage,
        sound,
        soundImpact,
        cooldownSeconds,
        duration,
        frequency,
        price,
        buff,
        0,
        0,
        0,
        0,
        null,
        null,
        null,
        null,
        0,
        0,
        false,
        Collections.emptyList());
  }

  public SpellData(
      String name,
      String description,
      String manaCost,
      int radius,
      int minInt,
      int minWis,
      int minLevel,
      boolean isAttack,
      boolean lineOfSight,
      String iconId,
      String projectileSpell,
      String impactSpell,
      int minDamage,
      int maxDamage,
      String sound,
      String soundImpact,
      int cooldownSeconds,
      String duration,
      SpellBuff buff) {
    this(
        name,
        description,
        manaCost,
        radius,
        minInt,
        minWis,
        minLevel,
        isAttack,
        lineOfSight,
        iconId,
        projectileSpell,
        impactSpell,
        minDamage,
        maxDamage,
        sound,
        soundImpact,
        cooldownSeconds,
        duration,
        0,
        buff);
  }

  @Getter
  @AllArgsConstructor
  @ToString
  public static final class T4cEffect {
    private final int effectType;
    private final List<EffectParam> parameters;

    @Getter
    @AllArgsConstructor
    @ToString
    public static final class EffectParam {
      private final int paramId;
      private final String expression;
    }
  }

  @Getter
  @AllArgsConstructor
  @ToString
  public static class SpellBuff {
    private final Integer durationSeconds;
    private final Boolean unlimited;
    private final List<SpellEffect> effects;
  }

  @Getter
  @AllArgsConstructor
  public static class SpellEffect {
    private final String type;
    private final String attribute;
    private final String amount;
    private final String description;
  }
}
