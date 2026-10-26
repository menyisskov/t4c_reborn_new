package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.quest.definition.ConcordCampaign;
import java.util.List;

/** Bound story materials cannot be equipped and never participate in equipment stat budgets. */
public final class ConcordEvidence {
  private ConcordEvidence() {}

  public static List<ItemDefinition> all() {
    return ConcordCampaign.all().stream()
        .filter(q -> q.getRequiredItemKey() != null)
        .map(
            q ->
                new ItemDefinition(
                    q.getRequiredItemKey(),
                    "${item." + q.getId() + "_proof}",
                    null,
                    null,
                    null,
                    null,
                    "64kInvRings 1",
                    0L,
                    1L,
                    0d,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    0L,
                    1d,
                    true,
                    false,
                    true,
                    0,
                    2,
                    0,
                    null,
                    null,
                    0,
                    0,
                    false,
                    null,
                    0,
                    null,
                    0,
                    0,
                    0,
                    List.of(),
                    List.of(),
                    List.of(),
                    true))
        .toList();
  }
}
