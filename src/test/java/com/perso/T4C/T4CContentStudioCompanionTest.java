package com.perso.T4C;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.perso.T4C.npc.companion.*;
import com.perso.T4C.npc.companion.CompanionDef;
import com.perso.T4C.npc.companion.CompanionSpellTrigger;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class T4CContentStudioCompanionTest {
  @Test
  void mapsEditableCompanionFieldsWithoutLosingNestedContent() {
    Map<String, Object> part = new LinkedHashMap<>();
    part.put("bodyPart", "BODY");
    part.put("spriteBase", "PupNecromanRobe");
    Map<String, Object> spell = new LinkedHashMap<>();
    spell.put("spellKey", "spell.fire_dart");
    spell.put("trigger", "ATTACK");
    spell.put("priority", 10);
    spell.put("cooldownSeconds", 3f);
    spell.put("healthThreshold", 0f);
    spell.put("minDamage", 6);
    spell.put("maxDamage", 12);
    spell.put("damagePerLevel", 1.5f);
    spell.put("rangeTiles", 8f);
    Map<String, Object> item = new LinkedHashMap<>();
    item.put("id", "test_companion");
    item.put("displayName", "Compagnon test");
    item.put("spriteBase", "");
    item.put("baseHp", 35);
    item.put("hpPerLevel", 6f);
    item.put("damageMin", 2);
    item.put("damageMax", 5);
    item.put("damagePerLevel", .4f);
    item.put("attackCooldown", 1.5f);
    item.put("speed", 51.75f);
    item.put("parts", List.of(part));
    item.put("spells", List.of(spell));
    CompanionDef result = new T4CContentStudio().companionFromMap(item, new LinkedHashMap<>());
    assertEquals("test_companion", result.getId());
    assertEquals("PupNecromanRobe", result.getParts().get(0).getSpriteBase());
    assertEquals(CompanionSpellTrigger.ATTACK, result.getSpells().get(0).getTrigger());
    assertEquals("spell.fire_dart", result.getSpells().get(0).getSpellKey());
  }
}
