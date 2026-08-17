package com.perso.T4C.tools;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.MonsterDefBinaryIO;
import com.perso.T4C.monster.MonsterDef;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/** Generic one-shot setter for monster animation patterns persisted in monsters.bin. */
public final class MonsterAnimationPatternMigration {
    private MonsterAnimationPatternMigration() {}

    public static void main(String[] args) throws Exception {
        if (args.length < 4) throw new IllegalArgumentException(
                "Usage: <monster-regex> <walk-pattern> <attack-pattern> <death-pattern>");
        Pattern pattern = Pattern.compile(args[0]);
        List<MonsterDef> updated = new ArrayList<>();
        for (MonsterDef d : MonsterDefBinaryIO.read(new File(Paths.MONSTERS_BIN))) {
            updated.add(pattern.matcher(d.getName()).matches() ? copy(d, args[1], args[2], args[3]) : d);
        }
        MonsterDefBinaryIO.write(new File(Paths.MONSTERS_BIN), updated);
    }

    private static MonsterDef copy(MonsterDef d, String walk, String attack, String death) {
        return new MonsterDef(d.getName(), d.getDisplayName(), d.getHealth(), d.getMana(), d.getXpPerHit(),
                d.getXpOnDeath(), d.getHitDamageMin(), d.getHitDamageMax(), d.getRespawnTime(), walk, attack, death,
                d.getSoundAttack(), d.getSoundDeath(), d.getSoundHit(), d.getGoldMin(), d.getGoldMax(), d.getLoot(),
                d.isAnimateWhileStationary(), d.getStationaryAnimationPauseSeconds(), d.getStr(), d.getEnd(),
                d.getAgi(), d.getIntel(), d.getWill(), d.getWis(), d.getLuck(), d.getResists(), d.getLevel(),
                d.getDodge(), d.getAcMin(), d.getAcMax(), d.getAppearance(), d.getItemBody(), d.getItemFeet(),
                d.getItemHands(), d.getItemHead(), d.getItemLegs(), d.getItemWeapon(), d.getItemShield(),
                d.getItemBack(), d.getAggro(), d.getClan(), d.getSpeed(), d.isCanAttack(), d.getAttacks(),
                d.isTameable(), d.getTameMaxLevel(), d.getSpawnAliases(), d.getSourceEvents());
    }
}
