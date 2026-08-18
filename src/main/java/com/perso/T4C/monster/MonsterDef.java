package com.perso.T4C.monster;

import com.perso.T4C.monster.core.*;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MonsterDef {
  private final String name;
  private final String displayName;
  private final int health;
  private final int mana;
  private final int xpPerHit;
  private final int xpOnDeath;
  private final int hitDamageMin;
  private final int hitDamageMax;
  private final long respawnTime;
  private final String walkPattern;
  private final String attackPattern;
  private final String deathPattern;
  private final String soundAttack;
  private final String soundDeath;
  private final String soundHit;
  private final int goldMin;
  private final int goldMax;
  private final List<LootDrop> loot;
  private final boolean animateWhileStationary;
  private final float stationaryAnimationPauseSeconds;
  private final int str;
  private final int end;
  private final int agi;
  private final int intel;
  private final int will;
  private final int wis;
  private final int luck;
  private final int[] resists;
  private final int level;
  private final int dodge;
  private final int acMin;
  private final int acMax;
  private final int appearance;
  private final int itemBody;
  private final int itemFeet;
  private final int itemHands;
  private final int itemHead;
  private final int itemLegs;
  private final int itemWeapon;
  private final int itemShield;
  private final int itemBack;
  private final int aggro;
  private final int clan;
  private final int speed;
  private final boolean canAttack;
  private final List<Attack> attacks;
  private final boolean tameable;
  private final int tameMaxLevel;
  private final List<String> spawnAliases;
  private final Map<String, String> sourceEvents;

  public boolean canBeTamedBy(int casterLevel) {
    return tameable && level <= tameMaxLevel && level <= casterLevel;
  }

  public boolean isDefaultAggressive() {
    return aggro > 0;
  }

  public MonsterDef withLoot(List<LootDrop> newLoot) {
    return new MonsterDef(
        name,
        displayName,
        health,
        mana,
        xpPerHit,
        xpOnDeath,
        hitDamageMin,
        hitDamageMax,
        respawnTime,
        walkPattern,
        attackPattern,
        deathPattern,
        soundAttack,
        soundDeath,
        soundHit,
        goldMin,
        goldMax,
        newLoot == null ? List.of() : newLoot,
        animateWhileStationary,
        stationaryAnimationPauseSeconds,
        str,
        end,
        agi,
        intel,
        will,
        wis,
        luck,
        resists,
        level,
        dodge,
        acMin,
        acMax,
        appearance,
        itemBody,
        itemFeet,
        itemHands,
        itemHead,
        itemLegs,
        itemWeapon,
        itemShield,
        itemBack,
        aggro,
        clan,
        speed,
        canAttack,
        attacks,
        tameable,
        tameMaxLevel,
        spawnAliases,
        sourceEvents);
  }

  @Getter
  @AllArgsConstructor
  public static final class LootDrop {
    private final String item;
    private final float chance;
  }

  @Getter
  @AllArgsConstructor
  public static final class Attack {
    private final String name;
    private final int value1;
    private final int value2;
    private final int value3;
    private final int value4;
    private final int value5;
  }
}
