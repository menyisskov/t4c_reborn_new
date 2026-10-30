package com.perso.T4C.online;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OnlineCorpsePanelTest {
  @Test
  void menuListsDisplayFieldsAndSelectTheMatchingItemIdentity() {
    var corpseId = UUID.randomUUID();
    var first = new OnlineConnection.CorpseItem(UUID.randomUUID(), "item.dagger", 2, 3);
    var second = new OnlineConnection.CorpseItem(UUID.randomUUID(), "item.ring", 1, 0);
    var corpse = new OnlineConnection.Corpse(corpseId, 0, 12.5, 8.5, 2, 17, List.of(first, second));
    var panel = new OnlineCorpsePanel();
    panel.open(corpse);

    var lines = panel.lines(corpse, key -> key);

    assertTrue(lines.stream().anyMatch(line -> line.contains("item.dagger x2 (3)")));
    assertTrue(lines.stream().anyMatch(line -> line.contains("item.ring x1")));
    assertEquals(new OnlineCorpsePanel.Action(corpseId, second.id()), panel.choose(2, corpse).orElseThrow());
    assertTrue(panel.choose(3, corpse).isEmpty());
    var changed = new OnlineConnection.Corpse(corpseId, 0, 12.5, 8.5, 1, 17, List.of(first));
    assertTrue(panel.choose(1, changed).isEmpty(), "a stale listing cannot select a changed stack");
  }

  @Test
  void pagedRowsStayMappedToTheSelectedStack() {
    var items = java.util.stream.IntStream.range(0, 10)
        .mapToObj(i -> new OnlineConnection.CorpseItem(UUID.randomUUID(), "item.test" + i, i + 1, 0)).toList();
    var corpse = new OnlineConnection.Corpse(UUID.randomUUID(), 0, 1, 1, items.size(), 0, items);
    var panel = new OnlineCorpsePanel();
    panel.open(corpse);
    panel.turnPage(1, corpse);
    assertEquals(new OnlineCorpsePanel.Action(corpse.id(), items.get(9).id()), panel.choose(1, corpse).orElseThrow());
  }
}
