package com.perso.T4C.tools;

import com.perso.T4C.helper.SpawnBinaryIO;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** Moves EXITGATE instances from monster spawns to their static-portal NPC definition. */
public final class ExitGateSpawnMigration {
    private static final String TYPE = "EXITGATE";

    private ExitGateSpawnMigration() {
    }

    public static void main(String[] args) throws Exception {
        boolean write = args.length == 1 && "--write".equals(args[0]);
        File monsterFile = new File("assets/spawns/monster_spawns.bin");
        File npcFile = new File("assets/spawns/npc_spawns.bin");
        List<SpawnBinaryIO.Entry> monsters = new ArrayList<>(SpawnBinaryIO.read(monsterFile));
        List<SpawnBinaryIO.Entry> npcs = new ArrayList<>(SpawnBinaryIO.read(npcFile));
        List<SpawnBinaryIO.Entry> gates = monsters.stream().filter(ExitGateSpawnMigration::isExitGate).toList();

        monsters.removeIf(ExitGateSpawnMigration::isExitGate);
        npcs.removeIf(ExitGateSpawnMigration::isExitGate);
        for (SpawnBinaryIO.Entry source : gates) {
            SpawnBinaryIO.Entry gate = new SpawnBinaryIO.Entry();
            gate.type = TYPE;
            gate.x = source.x;
            gate.y = source.y;
            gate.z = source.z;
            gate.stationary = true;
            gate.aggressive = false;
            npcs.add(gate);
        }

        if (!write) {
            System.out.printf("Would migrate %d EXITGATE spawns from monsters to NPCs.%n", gates.size());
            return;
        }
        if (gates.isEmpty()) {
            System.out.println("No monster EXITGATE spawns remain; nothing to migrate.");
            return;
        }
        SpawnBinaryIO.write(monsterFile, monsters);
        SpawnBinaryIO.write(npcFile, npcs);
        System.out.printf("Migrated %d EXITGATE spawns from monsters to NPCs.%n", gates.size());
    }

    private static boolean isExitGate(SpawnBinaryIO.Entry entry) {
        return TYPE.equalsIgnoreCase(entry.type);
    }
}
