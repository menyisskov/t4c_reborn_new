package com.perso.T4C.tools;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.NpcDefBinaryIO;
import com.perso.T4C.npc.KeywordActionType;
import com.perso.T4C.npc.NpcDef;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * One-shot migration: switches Moonrock from the hardcoded HEAL action (silent
 * full heal, no VFX) to CAST with "Heal critical", so healing gets the same
 * projectile/impact effect as a player-cast spell via NpcCastVfxHook.
 * Idempotent. Run with --apply to write npcs.bin.
 */
public final class NpcMoonrockHealMigration {

    private NpcMoonrockHealMigration() {
    }

    private static final String NPC = "Moonrock";
    private static final String SPELL = "Heal critical";
    /**
     * Caster level/wisdom used to evaluate the spell's formulas ({@code self.wis},
     * {@code self.light}), carried in the taught-spell's unused-for-CAST price slot.
     * A high-ranking temple bishop, well above what the player's own wisdom would
     * give at low level.
     */
    private static final int CASTER_POWER = 60;

    public static void main(String[] args) throws Exception {
        boolean apply = args.length > 0 && "--apply".equals(args[0]);

        File file = new File(Paths.NPCS_BIN);
        List<NpcDef> defs = new ArrayList<>(NpcDefBinaryIO.read(file));
        boolean changed = false;
        for (int i = 0; i < defs.size(); i++) {
            NpcDef d = defs.get(i);
            if (d == null || !NPC.equals(d.getName())) continue;
            boolean ok = d.getAction() == KeywordActionType.CAST
                    && d.getTaughtSpells() != null && d.getTaughtSpells().size() == 1
                    && SPELL.equals(d.getTaughtSpells().get(0).getSpellName())
                    && d.getTaughtSpells().get(0).getPrice() == CASTER_POWER;
            if (ok) {
                System.out.println(NPC + ": already migrated, nothing to do.");
                return;
            }
            defs.set(i, new NpcDef(
                    d.getName(), d.getDisplayName(), d.getParts(), d.getSpriteBase(),
                    d.getDialogText(), d.getDialogKeyword(),
                    KeywordActionType.CAST, 0, 0,
                    d.getPatrolRadiusTiles(),
                    List.of(new NpcDef.TaughtSpell(SPELL, CASTER_POWER)),
                    d.getShopItems(), d.getTrainableStats()));
            changed = true;
            System.out.println(NPC + ": action " + d.getAction() + " -> CAST, spell " + SPELL
                    + " (caster power " + CASTER_POWER + ").");
        }
        if (!changed) {
            System.out.println(NPC + ": not found in npcs.bin, skipped.");
            return;
        }
        if (apply) {
            NpcDefBinaryIO.write(file, defs);
            System.out.println("npcs.bin updated.");
        } else {
            System.out.println("Dry run only — re-run with --apply to write npcs.bin.");
        }
    }
}
