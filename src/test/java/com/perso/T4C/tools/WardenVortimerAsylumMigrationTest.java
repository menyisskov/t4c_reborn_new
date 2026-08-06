package com.perso.T4C.tools;

import com.perso.T4C.npc.NpcDef;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WardenVortimerAsylumMigrationTest {
    @Test
    void addsTheOriginalGonAsylumDestinationToTheEntranceCommand() {
        String script = """
                Command3(INTL(10664, "DOOR"), INTL(8191, "KEY"), INTL(7296, "ENTER"))
                    INTL(10666, "Come in.")
                """;
        NpcDef original = new NpcDef("WardenVortimer", "Warden Vortimer", List.of(), null,
                0, List.of(), "Greetings.", List.of(), "WardenVortimer", script, Map.of());

        NpcDef migrated = WardenVortimerAsylumMigration.update(original);

        assertTrue(migrated.getSourceScript().contains("TELEPORT(2704, 2226, 0)"));
        assertTrue(migrated.getSourceScript().contains("\"ENTRER\""));
        assertEquals(migrated.getSourceScript(), WardenVortimerAsylumMigration.update(migrated).getSourceScript());
    }
}
