package com.perso.T4C.tools;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.perso.T4C.helper.ItemDefBinaryIO;
import com.perso.T4C.helper.MonsterDefBinaryIO;
import com.perso.T4C.helper.NpcDefBinaryIO;
import com.perso.T4C.helper.ObjectMappingsBinaryIO;
import com.perso.T4C.helper.SpellBinaryIO;
import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.monster.MonsterDef;
import com.perso.T4C.npc.NpcDef;
import com.perso.T4C.spell.SpellData;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/** Merges all player-facing strings stored in content binaries into both i18n catalogues. */
public final class I18nCatalogueGenerator {
    private static final Type CATALOGUE_TYPE = new TypeToken<Map<String, String>>() { }.getType();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private I18nCatalogueGenerator() { }

    public static void main(String[] args) throws Exception {
        Map<String, String> discovered = discover();
        merge(Path.of(I18n.CATALOGUE_PATH), discovered);
        System.out.println("Merged " + discovered.size() + " binary-data placeholders into the catalogue.");
        if (java.util.Arrays.asList(args).contains("--rewrite-binaries")) {
            I18n.reload();
            rewriteBinaries();
            System.out.println("Rewrote player-facing binary strings as ${i18n.keys}.");
        }
    }

    private static void rewriteBinaries() throws Exception {
        File items = new File("assets/items/items.bin");
        ItemDefBinaryIO.write(items, ItemDefBinaryIO.read(items));
        File spells = new File("assets/spells/spells.bin");
        SpellBinaryIO.write(spells, SpellBinaryIO.read(spells));
        File monsters = new File("assets/monsters/monsters.bin");
        MonsterDefBinaryIO.write(monsters, MonsterDefBinaryIO.read(monsters));
        File npcs = new File("assets/npcs/npcs.bin");
        NpcDefBinaryIO.write(npcs, NpcDefBinaryIO.read(npcs));
        File objects = new File("assets/objects/object_mappings.bin");
        ObjectMappingsBinaryIO.write(objects, ObjectMappingsBinaryIO.read(objects));
    }

    private static Map<String, String> discover() throws Exception {
        Map<String, String> values = new TreeMap<>();
        putCodePlaceholders(values);
        for (ItemDefinition item : ItemDefBinaryIO.read(new File("assets/items/items.bin"))) {
            put(values, "item", item.getKey(), item.getName());
            put(values, "item.sign", item.getKey(), item.getSignText());
            put(values, "item.lock", item.getKey(), item.getLockName());
        }
        for (SpellData spell : SpellBinaryIO.read(new File("assets/spells/spells.bin"))) {
            put(values, "spell", spell.getName(), spell.getName());
            put(values, "spell.description", spell.getName(), spell.getDescription());
            if (spell.getBuff() != null && spell.getBuff().getEffects() != null) {
                int index = 0;
                for (SpellData.SpellEffect effect : spell.getBuff().getEffects()) {
                    if (effect != null) put(values, "spell.effect." + normalized(spell.getName()), Integer.toString(index), effect.getDescription());
                    index++;
                }
            }
        }
        for (MonsterDef monster : MonsterDefBinaryIO.read(new File("assets/monsters/monsters.bin"))) {
            put(values, "monster", monster.getName(), monster.getDisplayName());
        }
        for (NpcDef npc : NpcDefBinaryIO.read(new File("assets/npcs/npcs.bin"))) {
            put(values, "npc", npc.getName(), npc.getDisplayName());
            String identity = com.perso.T4C.i18n.I18n.normalizedKey(npc.getName());
            putRaw(values, "npc.welcome." + identity, npc.getWelcomeText());
            for (int i = 0; i < npc.getTopics().size(); i++) {
                NpcDef.DialogTopic topic = npc.getTopics().get(i);
                putRaw(values, "npc.topic." + identity + "." + i, topic.getResponse());
                for (int k = 0; k < topic.getKeywords().size(); k++) {
                    putRaw(values, "npc.topic_keyword." + identity + "." + i + "." + k,
                            topic.getKeywords().get(k));
                }
            }
        }
        for (ObjectMappingsBinaryIO.Entry entry : ObjectMappingsBinaryIO.read(new File("assets/objects/object_mappings.bin"))) {
            if (entry != null && entry.mapping != null) put(values, "object", entry.logicalName, entry.mapping.displayName);
        }
        return values;
    }

    private static void putCodePlaceholders(Map<String, String> values) {
        for (String word : new String[] {"armor", "axe", "belt", "boots", "bow", "cloth", "gloves",
                "healing", "helmet", "leather", "mana", "pants", "potion", "ring", "scroll", "sword"}) {
            values.put("item.word." + word, word);
        }
        for (String word : new String[] {"arrow", "avalanche", "bless", "blizzard", "bolt", "burst", "clear",
                "combat", "critical", "cure", "curse", "darkness", "detect", "dispel", "drain", "earth",
                "entangle", "fire", "firestorm", "flaming", "freeze", "glacier", "heal", "healing", "hidden",
                "ice", "inferno", "invisibility", "life", "light", "lightning", "mana", "mass", "meteor", "of",
                "poison", "protection", "rain", "resist", "sanctuary", "sense", "serious", "shard", "shield",
                "sight", "skin", "soul", "steal", "stone", "storm", "strength", "surge", "thought", "true",
                "tsunami", "turn", "undead"}) {
            values.put("spell.word." + word, word);
        }
        values.put("object.closed_wooden_door", "Wooden door");
        values.put("object.flipped_wooden_door", "Flipped wooden door");
        values.put("object.opened_wooden_door", "Opened wooden door");
        values.put("ui.world_map", "WORLD MAP");
        values.put("ui.training", "TRAINING");
        values.put("ui.character_sheet", "CHARACTER SHEET");
        values.put("ui.spellbook", "SPELLBOOK");
        values.put("ui.inventory", "INVENTORY");
        values.put("ui.gold", "GOLD");
        values.put("ui.item_name", "Item Name");
        values.put("ui.price", "Price");
        values.put("ui.quantity_short", "Qty.");
        values.put("ui.on_hand", "On Hand");
        values.put("ui.cost", "Cost");
        values.put("ui.total", "Total");
        values.put("message.not_enough_gold", "Not enough gold.");
        values.put("message.action_prevented", "A mystical force prevents you from performing this action.");
        values.put("message.item_picked_up", "You picked up %s");
        values.put("message.pickup_too_far", "You are too far away to pick that up");
        values.put("message.item_too_heavy", "This item is too heavy for you");
        values.put("message.unique_item_owned", "You already possess this unique item");
        values.put("message.spell_not_learned", "You have not learned this spell.");
        values.put("message.spell_wrong_target", "This spell cannot affect that target.");
        values.put("message.spell_pvp_forbidden", "This spell cannot be used in this PvP situation.");
        values.put("message.target_too_far", "Target too far away.");
        values.put("message.target_no_line_of_sight", "Target not in line of sight.");
        values.put("message.spell_cooldown", "This spell is not ready yet.");
        values.put("message.spell_exhausted", "You are too exhausted to cast a spell.");
        values.put("message.not_enough_mana", "Not enough mana.");
        values.put("message.spell_failed", "The spell failed.");
        values.put("message.attack_npc", "You attack %s.");
        values.put("message.teleport", "Teleport");
        values.put("message.map_load_failed", "Unable to load map Z=%d.");
        values.put("message.player_died", "You died: %d XP lost, %d item(s) left on your corpse.");
        values.put("message.equip_wrong_slot", "You cannot equip this item in that slot.");
        values.put("message.item_not_owned", "You no longer possess this item.");
        values.put("message.item_not_equippable", "This item cannot be equipped.");
        values.put("message.item_cannot_equip", "You cannot equip this item.");
        values.put("message.equip_need_endurance", "You need %d endurance to equip %s.");
        values.put("message.equip_need_strength", "You need %d strength to equip %s.");
        values.put("message.equip_need_agility", "You need %d agility to equip %s.");
        values.put("message.equip_need_intelligence", "You need %d intelligence to equip %s.");
        values.put("message.equip_need_wisdom", "You need %d wisdom to equip %s.");
        values.put("message.equip_need_attack", "You need %d attack to equip %s.");
        values.put("message.equip_requirements", "You do not meet the requirements to equip %s.");
        values.put("message.spell_already_known", "You already know %s.");
        values.put("message.spell_cannot_learn", "You cannot learn %s: %s.");
        values.put("message.spell_learned", "You have learned %s!");
        values.put("message.stat_at_maximum", "%s is already at maximum.");
        values.put("message.stat_increased", "%s increased to %d!");
    }

    private static void merge(Path path, Map<String, String> discovered) throws Exception {
        Map<String, String> existing;
        try (FileReader reader = new FileReader(path.toFile(), StandardCharsets.UTF_8)) {
            existing = GSON.fromJson(reader, CATALOGUE_TYPE);
        }
        Map<String, String> merged = new LinkedHashMap<>();
        if (existing != null) merged.putAll(existing);
        discovered.forEach(merged::putIfAbsent);
        Files.createDirectories(path.getParent());
        try (FileWriter writer = new FileWriter(path.toFile(), StandardCharsets.UTF_8)) {
            GSON.toJson(merged, writer);
            writer.write(System.lineSeparator());
        }
    }

    /**
     * Records the catalogue default for a binary field. A field already stored as a
     * placeholder carries no text to seed with, so it is skipped rather than writing
     * a literal "${...}" into the catalogue as if it were a translation.
     */
    private static void put(Map<String, String> values, String namespace, String identity, String text) {
        if (identity == null || identity.isBlank() || text == null || text.isBlank()) return;
        if (I18n.keyOf(text) != null) return;
        values.putIfAbsent(namespace + "." + normalized(identity), text);
    }

    /** Same guard as {@link #put} for entries whose key is already fully built. */
    private static void putRaw(Map<String, String> values, String key, String text) {
        if (text == null || text.isBlank() || I18n.keyOf(text) != null) return;
        values.putIfAbsent(key, text);
    }

    private static String normalized(String value) {
        return value.trim().toLowerCase(java.util.Locale.ROOT)
                .replaceFirst("^\\s*\\[[^]]+]\\s*", "")
                .replaceFirst("^a\\s+", "")
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_|_$", "");
    }
}
