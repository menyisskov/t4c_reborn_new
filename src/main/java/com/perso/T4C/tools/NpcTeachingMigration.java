package com.perso.T4C.tools;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.NpcDefBinaryIO;
import com.perso.T4C.npc.KeywordActionType;
import com.perso.T4C.npc.NpcDef;
import com.perso.T4C.spell.SpellData;
import com.perso.T4C.spell.SpellRegistry;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One-shot migration: bakes the spell-teaching data from the original C++
 * scripts (Iraltok.CPP, Kilhiam.CPP) into npcs.bin so the game code no longer
 * hardcodes any teaching list. Prices come from the scripts' AddTeachSkill
 * calls; when the script had none, the spell's catalogue price is used.
 * Idempotent: only missing spells are appended. Run with --apply to write.
 */
public final class NpcTeachingMigration {

    private NpcTeachingMigration() {
    }

    public static void main(String[] args) throws Exception {
        boolean apply = args.length > 0 && "--apply".equals(args[0]);

        File file = new File(Paths.NPCS_BIN);
        List<NpcDef> defs = NpcDefBinaryIO.read(file);
        Map<String, NpcDef> byName = new LinkedHashMap<>();
        for (NpcDef d : defs) {
            if (d != null && d.getName() != null) byName.put(d.getName(), d);
        }

        boolean changed = false;
        changed |= ensureTeaching(byName, "Iraltok",
                List.of(taught("Fire dart", 532), taught("Flaming arrow", 5300)));
        changed |= ensureTeaching(byName, "Kilhiam",
                List.of(taught("Heal light", 897), taught("Light", 100)));

        if (!changed) {
            System.out.println("Nothing to do: npcs.bin already contains the teaching data.");
            return;
        }
        if (apply) {
            NpcDefBinaryIO.write(file, new ArrayList<>(byName.values()));
            System.out.println("npcs.bin updated.");
        } else {
            System.out.println("Dry run only — re-run with --apply to write npcs.bin.");
        }
    }

    /** Price 0 in the script → use the spell's catalogue price so npcs.bin is self-contained. */
    private static NpcDef.TaughtSpell taught(String spellName, int scriptPrice) {
        int price = scriptPrice;
        if (price <= 0) {
            SpellData spell = SpellRegistry.findByName(spellName);
            if (spell != null) price = spell.getPrice();
        }
        return new NpcDef.TaughtSpell(spellName, Math.max(price, 0));
    }

    private static boolean ensureTeaching(Map<String, NpcDef> byName, String npcName,
                                          List<NpcDef.TaughtSpell> wanted) {
        NpcDef existing = byName.get(npcName);
        if (existing == null) {
            System.out.println(npcName + ": not found in npcs.bin, skipped.");
            return false;
        }
        List<NpcDef.TaughtSpell> taught = existing.getTaughtSpells() == null
                ? new ArrayList<>() : new ArrayList<>(existing.getTaughtSpells());
        boolean changed = false;
        for (NpcDef.TaughtSpell w : wanted) {
            NpcDef.TaughtSpell current = taught.stream()
                    .filter(s -> w.getSpellName().equals(s.getSpellName()))
                    .findFirst().orElse(null);
            if (current == null) {
                taught.add(w);
                changed = true;
                System.out.println(npcName + ": + " + w.getSpellName() + " (" + w.getPrice() + " gold)");
            } else if (current.getPrice() != w.getPrice()) {
                taught.set(taught.indexOf(current), w);
                changed = true;
                System.out.println(npcName + ": " + w.getSpellName() + " price "
                        + current.getPrice() + " -> " + w.getPrice());
            }
        }
        KeywordActionType action = existing.getAction() != null && existing.getAction() != KeywordActionType.NONE
                ? existing.getAction() : KeywordActionType.TEACH;
        if (!changed && action == existing.getAction()) {
            return false;
        }
        byName.put(npcName, new NpcDef(
                existing.getName(), existing.getDisplayName(), existing.getParts(), existing.getSpriteBase(),
                existing.getDialogText(), existing.getDialogKeyword(),
                action, existing.getActionParam1(), existing.getActionParam2(),
                existing.getPatrolRadiusTiles(), taught,
                existing.getShopItems(), existing.getTrainableStats()));
        return true;
    }
}
