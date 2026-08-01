package com.perso.T4C.npc;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds the flat (BodyPart, spriteBase, ...) array expected by
 * {@link BaseNPC}'s constructor from a definition's part list. Shared by every
 * composite NPC so the layered-sprite wiring lives in a single place.
 */
public final class NpcPartsBuilder {
    private NpcPartsBuilder() {
    }

    public static Object[] fromDef(NpcDef def) {
        if (def == null) {
            return new Object[0];
        }
        List<Object> parts = new ArrayList<>();
        for (NpcDef.Part part : def.getParts()) {
            if (part != null && part.getBodyPart() != null) {
                parts.add(part.getBodyPart());
                parts.add(part.getSpriteBase());
            }
        }
        return parts.toArray();
    }
}
