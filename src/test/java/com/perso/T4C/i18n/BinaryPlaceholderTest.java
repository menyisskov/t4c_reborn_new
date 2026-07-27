package com.perso.T4C.i18n;

import com.perso.T4C.helper.ItemDefBinaryIO;
import com.perso.T4C.helper.MonsterDefBinaryIO;
import com.perso.T4C.helper.NpcDefBinaryIO;
import com.perso.T4C.helper.ObjectMappingsBinaryIO;
import com.perso.T4C.helper.SpellBinaryIO;
import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.monster.MonsterDef;
import com.perso.T4C.npc.NpcDef;
import com.perso.T4C.spell.SpellData;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards the rule the whole i18n design rests on: player-facing text lives in
 * lang.json, and content binaries carry only {@code ${key}} references to it.
 * <p>
 * Without this, an import or a Content Studio save can quietly reintroduce raw
 * prose into a .bin and the regression is invisible until someone reads the file.
 */
class BinaryPlaceholderTest {

    private final List<String> rawText = new ArrayList<>();
    private final List<String> danglingKeys = new ArrayList<>();

    private void check(String where, String value) {
        if (value == null || value.isBlank()) return;
        String key = I18n.keyOf(value);
        if (key == null) {
            rawText.add(where + " = \"" + value + "\"");
        } else if (!I18n.has(key)) {
            danglingKeys.add(where + " -> " + key);
        }
    }

    @Test
    void everyPlayerFacingBinaryStringIsAKnownPlaceholder() throws Exception {
        for (ItemDefinition def : ItemDefBinaryIO.read(new File("assets/items/items.bin"))) {
            check("item[" + def.getKey() + "].name", def.getName());
            check("item[" + def.getKey() + "].signText", def.getSignText());
        }
        for (SpellData spell : SpellBinaryIO.read(new File("assets/spells/spells.bin"))) {
            check("spell.name", spell.getName());
            check("spell.description", spell.getDescription());
        }
        for (MonsterDef def : MonsterDefBinaryIO.read(new File("assets/monsters/monsters.bin"))) {
            check("monster[" + def.getName() + "].displayName", def.getDisplayName());
        }
        for (NpcDef def : NpcDefBinaryIO.read(new File("assets/npcs/npcs.bin"))) {
            check("npc[" + def.getName() + "].displayName", def.getDisplayName());
            for (String shout : def.getFleeShouts()) {
                check("npc[" + def.getName() + "].fleeShout", shout);
            }
            check("npc[" + def.getName() + "].welcome", def.getWelcomeText());
            for (int i = 0; i < def.getTopics().size(); i++) {
                NpcDef.DialogTopic topic = def.getTopics().get(i);
                check("npc[" + def.getName() + "].topic[" + i + "].response", topic.getResponse());
                for (String keyword : topic.getKeywords()) {
                    check("npc[" + def.getName() + "].topic[" + i + "].keyword", keyword);
                }
            }
        }
        for (ObjectMappingsBinaryIO.Entry entry : ObjectMappingsBinaryIO.read(new File("assets/objects/object_mappings.bin"))) {
            if (entry != null && entry.mapping != null) {
                check("object[" + entry.logicalName + "].displayName", entry.mapping.displayName);
            }
        }

        assertTrue(rawText.isEmpty(),
                "Binary assets must not contain raw player-facing text; found " + rawText.size()
                        + ":\n  " + String.join("\n  ", rawText.subList(0, Math.min(20, rawText.size()))));
        assertTrue(danglingKeys.isEmpty(),
                "Placeholders must resolve against lang.json; found " + danglingKeys.size()
                        + " dangling:\n  " + String.join("\n  ", danglingKeys.subList(0, Math.min(20, danglingKeys.size()))));
    }
}
