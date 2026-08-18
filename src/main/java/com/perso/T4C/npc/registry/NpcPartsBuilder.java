package com.perso.T4C.npc.registry;

import com.perso.T4C.npc.core.*;
import java.util.ArrayList;
import java.util.List;

public final class NpcPartsBuilder {

  private NpcPartsBuilder() {}

  public static Object[] fromSpec(NpcSpec spec) {

    if (spec == null) return new Object[0];

    List<Object> parts = new ArrayList<>();

    for (NpcSpec.Part part : spec.parts()) {

      if (part != null && part.bodyPart() != null) {

        parts.add(part.bodyPart());

        parts.add(part.spriteBase());
      }
    }

    return parts.toArray();
  }
}
