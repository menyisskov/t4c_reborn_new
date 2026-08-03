package com.perso.T4C.tools;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.player.BodyPart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Adds or refreshes the custom miner pickaxe definition in items.bin. */
public final class MinerPickaxeItemSeed {
    public static final String ITEM_KEY = "item.miner_pickaxe";

    private MinerPickaxeItemSeed() {}

    public static void main(String[] args) throws Exception {
        List<ItemDefinition> definitions = new ArrayList<>(ItemRegistry.load());
        ItemDefinition pickaxe = new ItemDefinition(
                ITEM_KEY, "${item.miner_pickaxe}",
                BodyPart.WEAPON, "Pickaxe",
                null, null, "Inv_Pickaxe",
                25, 7, 0,
                0, 0, 0, 0, 0, 0, 0,
                1.0d, false, false, false,
                0, 1, 0,
                "1d6", "1200+1d600",
                0, 0, false,
                null, 0, null,
                0, 0, 0,
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

        int existing = -1;
        for (int i = 0; i < definitions.size(); i++) {
            if (ITEM_KEY.equals(definitions.get(i).getKey())) {
                existing = i;
                break;
            }
        }
        if (existing >= 0) definitions.set(existing, pickaxe);
        else definitions.add(pickaxe);

        ItemRegistry.save(definitions);
        System.out.println((existing >= 0 ? "Updated " : "Added ") + ITEM_KEY);
    }
}
