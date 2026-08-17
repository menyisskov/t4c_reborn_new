package com.perso.T4C.tools;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.MonsterDefBinaryIO;
import com.perso.T4C.monster.MonsterDef;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Generic one-shot setter for persisted monster metadata, with regex capture expansion. */
public final class MonsterEventMetadataMigration {
    private MonsterEventMetadataMigration() {}

    public static void main(String[] args) throws Exception {
        if (args.length < 3) throw new IllegalArgumentException(
                "Usage: <monster-regex> <event-key> <value-with-$captures>");
        Pattern pattern = Pattern.compile(args[0]);
        List<MonsterDef> updated = new ArrayList<>();
        for (MonsterDef def : MonsterDefBinaryIO.read(new File(Paths.MONSTERS_BIN))) {
            Matcher matcher = pattern.matcher(def.getName());
            if (!matcher.matches()) {
                updated.add(def);
                continue;
            }
            var events = new LinkedHashMap<>(def.getSourceEvents());
            events.put(args[1], matcher.replaceFirst(args[2]).replace("\\n", "\n"));
            updated.add(copy(def, events));
        }
        MonsterDefBinaryIO.write(new File(Paths.MONSTERS_BIN), updated);
    }

    private static MonsterDef copy(MonsterDef d, java.util.Map<String, String> events) {
        return new MonsterDef(d.getName(), d.getDisplayName(), d.getHealth(), d.getMana(), d.getXpPerHit(),
                d.getXpOnDeath(), d.getHitDamageMin(), d.getHitDamageMax(), d.getRespawnTime(), d.getWalkPattern(),
                d.getAttackPattern(), d.getDeathPattern(), d.getSoundAttack(), d.getSoundDeath(), d.getSoundHit(),
                d.getGoldMin(), d.getGoldMax(), d.getLoot(), d.isAnimateWhileStationary(),
                d.getStationaryAnimationPauseSeconds(), d.getStr(), d.getEnd(), d.getAgi(), d.getIntel(),
                d.getWill(), d.getWis(), d.getLuck(), d.getResists(), d.getLevel(), d.getDodge(), d.getAcMin(),
                d.getAcMax(), d.getAppearance(), d.getItemBody(), d.getItemFeet(), d.getItemHands(), d.getItemHead(),
                d.getItemLegs(), d.getItemWeapon(), d.getItemShield(), d.getItemBack(), d.getAggro(), d.getClan(),
                d.getSpeed(), d.isCanAttack(), d.getAttacks(), d.isTameable(), d.getTameMaxLevel(),
                d.getSpawnAliases(), events);
    }
}
