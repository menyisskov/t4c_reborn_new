package com.perso.T4C.npc;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.SpawnBinaryIO;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards the scenery NPCs that the original server pins in place with {@code SetCanMove(FALSE)}.
 *
 * <p>The spawn binary long predated that detection, so portals, doors and furniture wandered off
 * their spot. These assertions keep the flag from silently rotting again.
 */
class StaticNpcSpawnTest {

    private static List<SpawnBinaryIO.Entry> spawns() throws Exception {
        return SpawnBinaryIO.read(new File(Paths.NPC_SPAWNS_BIN));
    }

    /**
     * Every one of the hundred portal NPCs in the original calls {@code SetCanMove(FALSE)} in
     * {@code OnInitialise}, so a portal that drifts is always a port defect.
     */
    @Test
    void everyPortalIsPinnedInPlace() throws Exception {
        List<SpawnBinaryIO.Entry> portals = spawns().stream()
                .filter(spawn -> spawn.type != null
                        && spawn.type.toLowerCase(Locale.ROOT).contains("portal"))
                .toList();

        assertTrue(portals.size() >= 60, "the original ships dozens of portals, found " + portals.size());
        List<String> wandering = portals.stream()
                .filter(spawn -> !spawn.stationary)
                .map(spawn -> spawn.type)
                .toList();
        assertEquals(List.of(), wandering, "portals must never wander");
    }

    /**
     * The portal the doppelganger quest hangs on, at the coordinates it occupies in the original
     * server. Named explicitly because it is the one that surfaced the defect.
     */
    @Test
    void theDoppelgangerPortalStandsStillAtItsOriginalSpot() throws Exception {
        SpawnBinaryIO.Entry portal = spawns().stream()
                .filter(spawn -> "DoppelgangerPortal1a".equals(spawn.type))
                .findFirst()
                .orElseThrow(() -> new AssertionError("DoppelgangerPortal1a must be spawned"));

        assertTrue(portal.stationary, "DoppelgangerPortal1a is static in the original server");
        assertEquals(0, portal.z);
    }

    /** Scenery imported alongside the portals: doors, chests and nexus stones do not walk either. */
    @Test
    void sceneryNpcsDoNotWander() throws Exception {
        List<SpawnBinaryIO.Entry> all = spawns();
        for (String type : List.of("DoorGunthar1", "NexusStone1", "WoodenChestNomad1",
                "BookshelfNomad1", "KhimtesarWell")) {
            SpawnBinaryIO.Entry entry = all.stream()
                    .filter(spawn -> type.equals(spawn.type))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Missing spawn " + type));
            assertTrue(entry.stationary, type + " is scenery and must stay put");
        }
    }

    /** The fix must not have frozen the world: ordinary NPCs keep their wandering behaviour. */
    @Test
    void ordinaryNpcsStillWander() throws Exception {
        List<SpawnBinaryIO.Entry> all = spawns();
        assertTrue(all.stream().anyMatch(spawn -> !spawn.stationary),
                "ordinary NPCs must retain their default wandering behaviour");
        for (String type : List.of("Fali", "Rolph")) {
            SpawnBinaryIO.Entry entry = all.stream()
                    .filter(spawn -> type.equals(spawn.type))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Missing spawn " + type));
            assertFalse(entry.stationary, type + " walks around in the original server");
        }
    }
}
