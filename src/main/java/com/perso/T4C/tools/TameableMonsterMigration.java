package com.perso.T4C.tools;

import com.perso.T4C.monster.MonsterDef;
import com.perso.T4C.monster.MonsterRegistry;
import java.util.List;
import java.util.Map;

/** One-shot, explicitly curated tameability migration. */
public final class TameableMonsterMigration {
    private static final Map<String, Integer> SPECIES = Map.ofEntries(
            Map.entry("Brown Rat", 10), Map.entry("Wild Horse", 30),
            Map.entry("Cow", 20), Map.entry("Pig", 20), Map.entry("Unicorn", 50),
            Map.entry("Pegasus", 60), Map.entry("Dromadary", 30));
    private TameableMonsterMigration() {}

    public static List<MonsterDef> migrate(List<MonsterDef> defs) {
        return defs.stream().map(def -> copy(def, SPECIES.get(def.getName()))).toList();
    }

    private static MonsterDef copy(MonsterDef d, Integer cap) {
        return new MonsterDef(d.getName(), d.getDisplayName(), d.getHealth(), d.getMana(), d.getXpPerHit(), d.getXpOnDeath(),
                d.getHitDamageMin(), d.getHitDamageMax(), d.getRespawnTime(), d.getWalkPattern(), d.getAttackPattern(),
                d.getDeathPattern(), d.getSoundAttack(), d.getSoundDeath(), d.getSoundHit(), d.getGoldMin(), d.getGoldMax(),
                d.getLoot(), d.isAnimateWhileStationary(), d.getStationaryAnimationPauseSeconds(), d.getStr(), d.getEnd(),
                d.getAgi(), d.getIntel(), d.getWill(), d.getWis(), d.getLuck(), d.getResists(), d.getLevel(), d.getDodge(),
                d.getAcMin(), d.getAcMax(), d.getAppearance(), d.getItemBody(), d.getItemFeet(), d.getItemHands(), d.getItemHead(),
                d.getItemLegs(), d.getItemWeapon(), d.getItemShield(), d.getItemBack(), d.getAggro(), d.getClan(), d.getSpeed(),
                d.isCanAttack(), d.getAttacks(), cap != null, cap == null ? 0 : cap);
    }

    public static void main(String[] args) throws Exception {
        List<MonsterDef> migrated = migrate(MonsterRegistry.load());
        MonsterRegistry.save(migrated);
        System.out.println("Migrated " + migrated.stream().filter(MonsterDef::isTameable).count() + " tameable species");
    }
}
