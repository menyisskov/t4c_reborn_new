package com.perso.T4C.tools;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.MonsterDefBinaryIO;
import com.perso.T4C.monster.MonsterDef;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** Adds the addon mercenary definition which is not present in the original WDA monster table. */
public final class MercenaryMonsterMigration {
    public static final String MONSTER_NAME = "MOBMERCENARYC";

    private MercenaryMonsterMigration() {
    }

    public static void main(String[] args) throws Exception {
        File target = new File(args.length > 0 ? args[0] : Paths.MONSTERS_BIN);
        List<MonsterDef> definitions = upsert(MonsterDefBinaryIO.read(target));
        MonsterDefBinaryIO.write(target, definitions);
        System.out.println("Upserted " + MONSTER_NAME + " in " + target + ".");
    }

    static List<MonsterDef> upsert(List<MonsterDef> source) {
        List<MonsterDef> definitions = new ArrayList<>(source == null ? List.of() : source);
        definitions.removeIf(definition -> definition != null
                && MONSTER_NAME.equalsIgnoreCase(definition.getName()));
        definitions.add(createDefinition());
        return definitions;
    }

    /** Values ported from Dll Npcs Addon/MonsterStatSetup.cpp and MOBMercenaryC.cpp. */
    static MonsterDef createDefinition() {
        int[] resistsAndPowers = {90, 90, 90, 90, 60, 5000, 100, 100, 100, 100, 100, 100};
        List<MonsterDef.Attack> attacks = List.of(new MonsterDef.Attack("1d10+5", 94, 100, 0, 0, 0));
        return new MonsterDef(
                MONSTER_NAME, "Mercenary",
                116, 0, 9, 563, 6, 15, 30_000L,
                "Thief#m", "ThiefA#i", "ThiefC#l",
                null, null, null,
                12, 38, List.of(), false, 0f,
                21, 21, 21, 23, 21, 21, 21, resistsAndPowers,
                7, 43, 0, Float.floatToIntBits(2f), 10_004,
                0, 0, 0, 0, 0, 0, 0, 0,
                50, 20, 0, true, attacks);
    }
}
