package com.perso.T4C.tools;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.ItemDefBinaryIO;
import com.perso.T4C.item.ItemDefinition;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Sets {@code undroppable} on the Gem of Destiny, the one item the player can never drop.
 * Replaces the item-name check previously hardcoded in {@code MainGameScreen}.
 */
public final class GemOfDestinyUndroppableMigration {

    private static final String TARGET_KEY = "item.gem_of_destiny";

    private GemOfDestinyUndroppableMigration() {
    }

    public static void main(String[] args) throws Exception {
        boolean dryRun = args.length > 0 && "--dry-run".equals(args[args.length - 1]);
        File file = new File(Paths.ITEMS_BIN);
        List<ItemDefinition> defs = ItemDefBinaryIO.read(file);

        List<ItemDefinition> updated = new ArrayList<>(defs.size());
        boolean found = false;
        for (ItemDefinition def : defs) {
            if (!def.getKey().equalsIgnoreCase(TARGET_KEY)) {
                updated.add(def);
                continue;
            }
            found = true;
            updated.add(new ItemDefinition(def.getKey(), def.getName(), def.getBodyPart(),
                    def.getAppearanceEquippedPrimary(), def.getSecondaryBodyPart(),
                    def.getAppearanceEquippedSecondary(), def.getAppearanceInventory(),
                    def.getPrice(), def.getWeight(), def.getArmorClass(), def.getDodgeLost(), def.getMinEnd(),
                    def.getReqAttack(), def.getReqStr(), def.getReqAgi(), def.getMinInt(), def.getMinWis(),
                    def.getAttackSpeed(), def.isUnique(), def.isBow(), def.isUnlimitedUse(),
                    def.getNumId(), def.getStructure(), def.getAppearanceId(), def.getDmgFormula(), def.getAtkDelay(),
                    def.getRadiance(), def.getNbCharges(), def.isCanSummon(), def.getLockName(), def.getLockDiff(),
                    def.getSignText(), def.getContainerGold(), def.getGlobalRespawn(), def.getLocalRespawn(),
                    def.getSpells(), def.getBoosts(), def.getContainerLootGroups(), true));
        }

        if (!found) {
            System.out.println("Item '" + TARGET_KEY + "' not found in " + file + "; nothing to do.");
            return;
        }
        if (dryRun) {
            System.out.println("(dry run) would set undroppable=true on " + TARGET_KEY);
            return;
        }
        ItemDefBinaryIO.write(file, updated);
        System.out.println("Wrote " + file + " with undroppable=true on " + TARGET_KEY);
    }
}
