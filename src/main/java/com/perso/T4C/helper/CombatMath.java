package com.perso.T4C.helper;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.item.EquipmentBonusRules;
import com.perso.T4C.item.ItemDurabilityService;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;

/**
 * Shared damage formulas for player physical attacks (melee and bow).
 *
 * <p>Faithfully mirrors the T4C server logic (Character.cpp / GAME_RULES.cpp / Sword.cpp):
 * <ol>
 *   <li>Weapon damage: evaluate {@code dmgFormula} from {@link ItemDefinition} via {@link DiceFormula}.</li>
 *   <li>Natural damage bonus from STR (melee) or STR+AGI (ranged), matching GAME_RULES::GetNaturalDamage.</li>
 * </ol>
 * Result is clamped to a minimum of 1.
 */
public final class CombatMath {

    private CombatMath() {
    }

    /**
     * Melee damage: weapon formula + natural STR bonus.
     * Natural bonus = (STR - 20) / 5, only when STR >= 20.
     */
    public static int computeMeleeDamage(Player player) {
        int str = player == null ? 0 : player.getEffectiveStrength();
        int weaponDmg = rollWeaponDamage(player, false);
        int natural = str >= 20 ? (str - 20) / 5 : 0;
        return Math.max(1, weaponDmg + natural + EquipmentBonusRules.bonus(player, 10));
    }

    /**
     * Bow damage: weapon formula + natural STR+AGI bonus.
     * Natural bonus = (STR-20)/20 + (AGI-20)/10, only when stat >= 20.
     */
    public static int computeBowDamage(Player player) {
        int str = player == null ? 0 : player.getEffectiveStrength();
        int agi = player == null ? 0 : player.getEffectiveDexterity();
        int weaponDmg = rollWeaponDamage(player, true);
        int natural = (str >= 20 ? (str - 20) / 20 : 0) + (agi >= 20 ? (agi - 20) / 10 : 0);
        return Math.max(1, weaponDmg + natural + EquipmentBonusRules.bonus(player, 10));
    }

    /** Secondary weapon damage used by the original two_weapons skill hook. */
    public static int computeOffHandDamage(Player player) {
        if (player == null) return 0;
        String itemKey = player.getEquippedItems().get(BodyPart.WEAPON2);
        if (itemKey == null || itemKey.equals(player.getEquippedItems().get(BodyPart.WEAPON))) return 0;
        ItemDefinition definition = ItemRegistry.findByKey(itemKey);
        if (definition == null || definition.isBow() || ItemDurabilityService.isBroken(player, BodyPart.WEAPON2)) return 0;
        int weapon = rollDefinitionDamage(player, definition);
        int strength = player.getEffectiveStrength();
        int natural = strength >= 20 ? (strength - 20) / 5 : 0;
        return Math.max(1, weapon + natural + EquipmentBonusRules.bonus(player, 10));
    }

    // --- helpers ---

    private static int rollWeaponDamage(Player player, boolean bow) {
        if (player == null) return fallback(0, 0);

        String itemKey = player.getEquippedItems().get(BodyPart.WEAPON);
        if (bow) {
            ItemDefinition primary = ItemRegistry.findByKey(itemKey);
            if (primary == null || !primary.isBow()) itemKey = player.getEquippedItems().get(BodyPart.WEAPON2);
        }
        if (itemKey == null || ItemDurabilityService.isBroken(player,
                itemKey.equals(player.getEquippedItems().get(BodyPart.WEAPON)) ? BodyPart.WEAPON : BodyPart.WEAPON2))
            return fallback(player.getEffectiveStrength(), player.getEffectiveDexterity());

        ItemDefinition def = ItemRegistry.findByKey(itemKey);
        if (def == null) return fallback(player.getEffectiveStrength(), player.getEffectiveDexterity());

        return rollDefinitionDamage(player, def);
    }

    private static int rollDefinitionDamage(Player player, ItemDefinition def) {
        String formula = def.getDmgFormula();
        if (formula == null || formula.isEmpty() || "0".equals(formula)) {
            return fallback(player.getEffectiveStrength(), player.getEffectiveDexterity());
        }

        DiceFormula.Context ctx = new DiceFormula.Context(
                player.getEffectiveStrength(),
                player.getEffectiveEndurance(),
                player.getEffectiveDexterity(),
                player.getEffectiveIntelligence(),
                0, // will — not exposed on Player yet
                player.getEffectiveWisdom(),
                0, // luck — not exposed on Player yet
                player.getLevel()
        );
        return DiceFormula.of(formula).roll(ctx);
    }

    /** Fallback when no weapon definition is available: 1d4 base. */
    private static int fallback(int str, int agi) {
        return (int) (Math.random() * 4) + 1;
    }
}
