package com.perso.T4C.tools;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.NpcDefBinaryIO;
import com.perso.T4C.npc.NpcDef;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/** Generic one-shot setter for a persisted NPC event annotation. */
public final class NpcEventMetadataMigration {
    private NpcEventMetadataMigration() {}

    public static void main(String[] args) throws Exception {
        if (args.length < 3) throw new IllegalArgumentException("Usage: <npc-regex> <event-key> <script>");
        List<NpcDef> updated = new ArrayList<>();
        for (NpcDef def : NpcDefBinaryIO.read(new File(Paths.NPCS_BIN))) {
            if (!def.getName().matches(args[0])) { updated.add(def); continue; }
            var events = new LinkedHashMap<>(def.getSourceEvents());
            events.put(args[1], args[2].replace("\\n", "\n"));
            updated.add(new NpcDef(def.getName(), def.getDisplayName(), def.getParts(), def.getSpriteBase(),
                    def.getPatrolRadiusTiles(), def.getFleeShouts(), def.getWelcomeText(), def.getTopics(),
                    def.getSourceTemplate(), def.getSourceScript(), events));
        }
        NpcDefBinaryIO.write(new File(Paths.NPCS_BIN), updated);
    }
}
