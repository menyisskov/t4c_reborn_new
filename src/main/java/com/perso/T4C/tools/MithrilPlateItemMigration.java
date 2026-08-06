package com.perso.T4C.tools;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.player.BodyPart;

import java.util.ArrayList;
import java.util.List;

/** Adds the five equippable pieces backed by the {@code PupMithrilPlate*} sprite set. */
public final class MithrilPlateItemMigration {
    private record Piece(String key, String templateKey, BodyPart primary,
                         String primarySprite, BodyPart secondary, String secondarySprite) {
        /**
         * Item keys are already namespaced ({@code item.mithril_plate_armor}), so the
         * stored name is the matching placeholder. Passing raw prose here would make
         * ItemDefBinaryIO prefix the namespace a second time and emit the dangling
         * {@code ${item.item_mithril_plate_armor}}.
         */
        String name() {
            return "${" + key + "}";
        }
    }

    private static final List<Piece> PIECES = List.of(
            new Piece("item.mithril_plate_armor",
                    "item.ancient_platemail_armor", BodyPart.BODY, "PupMithrilPlateBody", null, null),
            new Piece("item.mithril_plate_boots",
                    "item.ancient_platemail_boots", BodyPart.FEET, "PupMithrilPlateFoot", null, null),
            new Piece("item.mithril_plate_gauntlets",
                    "item.ancient_platemail_gauntlets", BodyPart.LEFT_HAND, "PupMithrilPlateGloveL",
                    BodyPart.RIGHT_HAND, "PupMithrilPlateGloveR"),
            new Piece("item.mithril_plate_helmet",
                    "item.ancient_platemail_helmet", BodyPart.HEAD, "PupMithrilPlateHelm", null, null),
            new Piece("item.mithril_plate_leggings",
                    "item.ancient_platemail_leggings", BodyPart.LEGS, "PupMithrilPlateLegs", null, null));

    private MithrilPlateItemMigration() {
    }

    public static void main(String[] args) throws Exception {
        List<ItemDefinition> items = new ArrayList<>(ItemRegistry.load());
        for (Piece piece : PIECES) {
            ItemDefinition template = ItemRegistry.findByKey(piece.templateKey());
            if (template == null) {
                throw new IllegalStateException("Missing item template: " + piece.templateKey());
            }
            ItemDefinition mithril = fromTemplate(piece, template);
            int existing = indexOf(items, mithril.getKey());
            if (existing >= 0) items.set(existing, mithril);
            else items.add(mithril);
        }
        ItemRegistry.save(items);
        System.out.println("Created or updated " + PIECES.size() + " mithril plate items.");
    }

    private static int indexOf(List<ItemDefinition> items, String key) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) != null && key.equals(items.get(i).getKey())) return i;
        }
        return -1;
    }

    private static ItemDefinition fromTemplate(Piece piece, ItemDefinition template) {
        return new ItemDefinition(piece.key(), piece.name(), piece.primary(), piece.primarySprite(),
                piece.secondary(), piece.secondarySprite(), template.getAppearanceInventory(),
                template.getPrice(), template.getWeight(), template.getArmorClass(),
                template.getDodgeLost(), template.getMinEnd(), template.getReqAttack(),
                template.getReqStr(), template.getReqAgi(), template.getMinInt(), template.getMinWis(),
                template.getAttackSpeed(), template.isUnique(), template.isBow(), template.isUnlimitedUse(),
                0, template.getStructure(), template.getAppearanceId(), template.getDmgFormula(),
                template.getAtkDelay(), template.getRadiance(), template.getNbCharges(),
                template.isCanSummon(), template.getLockName(), template.getLockDiff(),
                template.getSignText(), template.getContainerGold(), template.getGlobalRespawn(),
                template.getLocalRespawn(), template.getSpells(), template.getBoosts(),
                template.getContainerLootGroups());
    }
}
