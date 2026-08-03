package com.perso.T4C.player;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.spell.SpellData;
import com.perso.T4C.spell.SpellRegistry;

import java.util.Locale;

/** Development/player test loadout. Safe to call repeatedly (never duplicates unique entries). */
public final class StarterLoadout {
    private StarterLoadout() {}

    public static void ensure(Player player) {
        if (player == null) return;
        add(player, "Ring of the Seraph", 1);
        add(player, "Seraph plate", 1);
        add(player, "Potion of healing", 100);
        add(player, "Potion of mana", 100);
        add(player, "Scroll of Lighthaven", 100);
        add(player, "Scroll of Windhowl", 100);
        add(player, "Scroll of Silversky", 100);
        // Representative usable weapons, resolved by the real registry keys.
        add(player, "Oak longbow", 1);
        add(player, "Fine steel long sword", 1);
        add(player, "High metal battle axe", 1);
        add(player, "item.miner_pickaxe", 1);
        // Include every registered High Elf/seraph-style armour piece available in the data set.
        for (ItemDefinition d : ItemRegistry.load()) {
            if (d == null || d.getKey() == null || d.getBodyPart() == null) continue;
            // Names are ${item.x} placeholders; the item key carries the same words.
            String n = d.getKey().toLowerCase(Locale.ROOT);
            if (d.getArmorClass() > 0 && (n.contains("elf") || n.contains("seraph") || n.contains("plate")
                    || n.contains("robe") || n.contains("armor") || n.contains("helmet")
                    || n.contains("boots") || n.contains("gloves") || n.contains("gauntlet")
                    || n.contains("greaves") || n.contains("leggings"))) add(player, d.getKey(), 1);
        }
        // Spell data has no separate GM bit; GM entries are explicitly named in the source data.
        for (SpellData spell : SpellRegistry.load()) {
            if (spell == null || spell.getName() == null || spell.getName().isBlank()) continue;
            // ${spell.x} identities keep the wording of the name in their key.
            String n = spell.getName().toLowerCase(Locale.ROOT).replace('_', ' ');
            if (!n.contains("gm") && !n.startsWith("dev_") && !player.getSpells().contains(spell.getName())) {
                player.getSpells().add(spell.getName());
            }
        }
    }

    private static void add(Player player, String key, int count) {
        if (ItemRegistry.findByKey(key) == null) return;
        int have = 0;
        for (String existing : player.getInventory()) if (key.equals(existing)) have++;
        for (int i = have; i < count; i++) player.getInventory().add(key);
    }
}
