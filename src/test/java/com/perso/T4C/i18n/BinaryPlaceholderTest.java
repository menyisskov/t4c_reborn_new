package com.perso.T4C.i18n;

import static org.junit.jupiter.api.Assertions.assertTrue;
import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.mapping.definition.ObjectMappingDefinitions;
import com.perso.T4C.npc.LighthavenSamaritan;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcFactoryRegistry;
import com.perso.T4C.quest.QuestDef;
import com.perso.T4C.quest.QuestRegistry;
import com.perso.T4C.spell.SpellData;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

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
  void everyPlayerFacingStringIsAKnownPlaceholder() throws Exception {
    for (ItemDefinition def : ItemRegistry.load()) {
      check("item[" + def.getKey() + "].name", def.getName());
      check("item[" + def.getKey() + "].signText", def.getSignText());
    }
    for (SpellData spell : com.perso.T4C.spell.SpellRegistry.load()) {
      check("spell.name", spell.getName());
      check("spell.description", spell.getDescription());
    }
    for (NpcFactoryRegistry.Registration registration : NpcFactoryRegistry.registrations()) {
      check("npc[" + registration.id() + "].displayName", registration.displayName());
      if (registration.factory().create(new NpcContext(null)) instanceof LighthavenSamaritan npc) {
        for (LighthavenSamaritan.DialogueTopic topic : npc.getTopics()) {
          check(
              "npc[" + registration.id() + "].topic[" + topic.id() + "].response",
              topic.response());
          for (String keyword : topic.keywords()) {
            check("npc[" + registration.id() + "].topic[" + topic.id() + "].keyword", keyword);
          }
        }
      }
    }
    for (QuestDef quest : QuestRegistry.load()) {
      check("quest[" + quest.getId() + "].title", quest.getTitle());
      check("quest[" + quest.getId() + "].offerText", quest.getOfferText());
      check("quest[" + quest.getId() + "].completionText", quest.getCompletionText());
      check("quest[" + quest.getId() + "].completedText", quest.getCompletedText());
    }
    for (var entry : ObjectMappingDefinitions.all().entrySet()) {
      if (entry != null && entry.getValue() != null) {
        check("object[" + entry.getKey() + "].displayName", entry.getValue().displayName);
      }
    }
    assertTrue(
        rawText.isEmpty(),
        "Binary assets must not contain raw player-facing text; found "
            + rawText.size()
            + ":\n  "
            + String.join("\n  ", rawText.subList(0, Math.min(20, rawText.size()))));
    assertTrue(
        danglingKeys.isEmpty(),
        "Placeholders must resolve against lang.json; found "
            + danglingKeys.size()
            + " dangling:\n  "
            + String.join("\n  ", danglingKeys.subList(0, Math.min(20, danglingKeys.size()))));
  }
}
