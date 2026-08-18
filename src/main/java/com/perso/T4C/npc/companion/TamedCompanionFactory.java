package com.perso.T4C.npc.companion;

import com.perso.T4C.monster.core.*;

import com.perso.T4C.helper.DiceFormula;
import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.monster.MonsterDef;
import com.perso.T4C.monster.core.MonsterRegistry;
import java.util.ArrayList;
import java.util.List;

public final class TamedCompanionFactory {

  public static final String ID_PREFIX = "tamed:";

  private TamedCompanionFactory() {}

  public static CompanionDef fromSpeciesName(String name) {

    MonsterDef def = MonsterRegistry.findByName(name);

    return def == null || !def.isTameable() ? null : fromMonster(def);
  }

  public static CompanionDef fromMonster(MonsterDef def) {

    if (def == null) return null;

    String sprite = companionSpriteBase(def.getWalkPattern());

    List<CompanionDef.Part> parts = sprite == null || sprite.isBlank() ? paperdoll(def) : List.of();

    int min = def.getHitDamageMin(), max = def.getHitDamageMax();

    if ((min <= 0 || max <= 0) && def.getAttacks() != null && !def.getAttacks().isEmpty()) {

      DiceFormula dice = DiceFormula.of(def.getAttacks().get(0).getName());

      min = dice.min();

      max = dice.max();
    }

    return new CompanionDef(
        ID_PREFIX + def.getName(),
        def.getDisplayName(),
        parts,
        sprite,
        Math.max(1, def.getHealth()),
        Math.max(1f, def.getHealth() * .10f),
        Math.max(0, min),
        Math.max(min, max),
        0f,
        1.5f,
        List.of());
  }

  public static String companionSpriteBase(String walkPattern) {

    if (walkPattern == null || walkPattern.isBlank()) return walkPattern;

    String value = walkPattern.trim();

    int marker = value.indexOf('#');

    return marker > 0 ? value.substring(0, marker) : value;
  }

  private static List<CompanionDef.Part> paperdoll(MonsterDef def) {

    int[] ids = {
      def.getItemBody(),
      def.getItemFeet(),
      def.getItemHands(),
      def.getItemHead(),
      def.getItemLegs(),
      def.getItemWeapon(),
      def.getItemShield(),
      def.getItemBack()
    };

    List<CompanionDef.Part> result = new ArrayList<>();

    for (int id : ids)
      for (ItemDefinition item : ItemRegistry.load())
        if (item.getNumId() == id) {

          add(result, item.getBodyPart(), item.getAppearanceEquippedPrimary());

          add(result, item.getSecondaryBodyPart(), item.getAppearanceEquippedSecondary());

          break;
        }

    return result;
  }

  private static void add(
      List<CompanionDef.Part> parts, com.perso.T4C.player.BodyPart part, String sprite) {

    if (part != null && sprite != null && !sprite.isBlank())
      parts.add(new CompanionDef.Part(part, sprite));
  }
}
