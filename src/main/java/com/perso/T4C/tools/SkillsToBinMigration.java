package com.perso.T4C.tools;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.SkillDefBinaryIO;
import com.perso.T4C.skill.SkillDefinition;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Writes {@code assets/skills/skills.bin} from the values that used to be hardcoded in
 * {@code SkillService.createDefinitions()} and its learn/use-cooldown switches.
 *
 * <p>Run once from the repository root; afterwards the asset is edited from Content Studio.
 *
 * <p>Usage: {@code SkillsToBinMigration [--dry-run]}
 */
public final class SkillsToBinMigration {

    private SkillsToBinMigration() {
    }

    /** {@code {id, minLevel, minStr, minEnd, minAgi, minInt, minWis, learningCost, useCooldownMillis, prerequisites}}. */
    private static final Object[][] SKILLS = {
            {"attack", 1, 0, 0, 0, 0, 0, 1, 0L, Map.of()},
            {"dodge", 1, 0, 0, 0, 0, 0, 1, 0L, Map.of()},
            {"archery", 1, 0, 0, 0, 0, 0, 1, 0L, Map.of()},
            {"powerful_blow", 15, 50, 0, 30, 0, 0, 1, 0L, Map.of()},
            {"stun_blow", 3, 25, 0, 20, 0, 0, 1, 0L, Map.of()},
            {"parry", 10, 0, 0, 30, 20, 0, 1, 0L, Map.of()},
            {"armor_penetration", 25, 75, 0, 40, 30, 0, 1, 0L, Map.of()},
            {"two_weapons", 25, 75, 0, 40, 30, 0, 1, 0L, Map.of()},
            {"rapid_healing", 30, 0, 80, 0, 0, 0, 1, 5_000L, Map.of()},
            {"pick_lock", 12, 0, 0, 40, 0, 0, 1, 1_000L, Map.of()},
            {"peek", 1, 0, 0, 0, 0, 0, 1, 0L, Map.of()},
            {"rob", 17, 0, 0, 50, 0, 0, 1, 1_000L, Map.of("peek", 25)},
            {"sneak", 24, 0, 0, 75, 0, 0, 1, 1_000L, Map.of()},
    };

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        boolean dryRun = args.length > 0 && "--dry-run".equals(args[0]);

        List<SkillDefinition> defs = new ArrayList<>();
        for (Object[] row : SKILLS) {
            defs.add(new SkillDefinition(
                    (String) row[0],
                    (Integer) row[1],
                    (Integer) row[2],
                    (Integer) row[3],
                    (Integer) row[4],
                    (Integer) row[5],
                    (Integer) row[6],
                    (Integer) row[7],
                    (Map<String, Integer>) row[9],
                    (Long) row[8]));
        }

        System.out.printf("skills=%d%n", defs.size());
        if (dryRun) {
            System.out.println("dry run - rien ecrit");
            return;
        }

        SkillDefBinaryIO.write(new File(Paths.SKILLS_BIN), defs);
        System.out.println("written: " + Paths.SKILLS_BIN);
    }
}
