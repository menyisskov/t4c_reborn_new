package com.perso.T4C.combat;

import java.util.random.RandomGenerator;

/**
 * Stateless physical combat rules ported from TFC_MAIN.cpp, GAME_RULES.cpp and
 * the original skill callbacks.
 */
public final class CombatResolver {
    private CombatResolver() {
    }

    public static CombatResult resolve(PhysicalAttackRequest request, RandomGenerator random) {
        if (request == null || random == null) {
            throw new IllegalArgumentException("Request and random generator are required");
        }
        CombatProfile attacker = request.attacker();
        CombatProfile target = request.target();
        int attackSkill = request.ranged() ? attacker.archery() : attacker.attack();
        int precision = roll(random, attackSkill) + roll(random, attacker.agility() / 3)
                - roll(random, target.dodge()) - roll(random, target.agility() / 3);
        int rawDamage = request.baseDamage();
        if (precision <= 0 && !target.stunned()) {
            return CombatResult.miss(precision, rawDamage);
        }

        double strike = rawDamage;
        if (attacker.hidden()) {
            strike *= (149 + roll(random, 50)) / 100d;
        }
        if (target.stunned()) {
            strike *= 1.5d;
        }

        boolean powerful = triggersHalfSkill(random, attacker.skill("powerful_blow"));
        if (powerful) {
            strike *= 1.33d;
        }

        double trueStrike = strike;
        boolean dualWeapon = request.offHandDamage() > 0 && attacker.skill("two_weapons") > 0;
        if (dualWeapon) {
            strike += dualWeaponBonus(request.offHandDamage(), attacker.skill("two_weapons"), random);
        }

        boolean penetration = false;
        double effectiveArmor = target.armorClass();
        int penetrationSkill = attacker.skill("armor_penetration");
        if (target.armorClass() < 5000d && triggersHalfSkill(random, penetrationSkill)) {
            penetration = true;
            double ignoredArmor = penetrationSkill > 125
                    ? penetrationSkill / (penetrationSkill + 5d) * target.armorClass()
                    : penetrationSkill / (penetrationSkill + 13.2d) * target.armorClass();
            effectiveArmor = Math.max(0d, target.armorClass() - ignoredArmor);
            strike += Math.min(penetrationSkill / 200d, 1d) * (trueStrike / 3.05d);
        }
        strike = Math.max(0d, strike - effectiveArmor);

        boolean parried = false;
        int parry = target.skill("parry");
        if (parry > 0 && target.weaponEquipped()) {
            double chance = (parry / (parry + 50d)) * 10d + (parry * target.strength() / 3000d);
            chance = Math.max(0d, Math.min(25d, chance));
            parried = roll(random, 100) < chance;
            if (parried) {
                strike = 0d;
            }
        }

        long stunMillis = resolveStun(attacker, target, random);
        return new CombatResult(true, precision, rawDamage, Math.max(0, (int) strike),
                parried, powerful, penetration, dualWeapon, stunMillis);
    }

    private static long resolveStun(CombatProfile attacker, CombatProfile target, RandomGenerator random) {
        int skill = attacker.skill("stun_blow");
        if (skill <= 0) {
            return 0L;
        }
        int chance = attacker.level() >= target.level() * 3 / 4 ? skill / 2 : skill / 4;
        if (roll(random, 100) >= chance) {
            return 0L;
        }
        if (roll(random, attacker.strength()) <= roll(random, target.endurance() * 3)) {
            return 0L;
        }
        return 1000L + roll(random, 1000);
    }

    private static boolean triggersHalfSkill(RandomGenerator random, int skill) {
        return skill > 0 && roll(random, 100) < skill / 2;
    }

    private static int dualWeaponBonus(int offHandDamage, int skill, RandomGenerator random) {
        int maximum = skill * (offHandDamage / 4) / 500;
        if (maximum <= 0) {
            return 0;
        }
        if (maximum <= 20) {
            return maximum;
        }
        int spread = Math.max(1, maximum / 5);
        return Math.max(0, maximum + random.nextInt(spread * 2) - spread);
    }

    private static int roll(RandomGenerator random, int faces) {
        return faces <= 1 ? 1 : random.nextInt(faces) + 1;
    }
}
