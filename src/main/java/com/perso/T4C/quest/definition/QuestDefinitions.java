package com.perso.T4C.quest.definition;

import com.perso.T4C.quest.QuestDef;
import java.util.List;

public final class QuestDefinitions {
  private QuestDefinitions() {}

  public static List<QuestDef> all() {
    return List.of(
        LighthavenSamaritanRats.definition(),
        OrtanalasBridgeGoblins.definition(),
        SilverskyTideWarden.definition(),
        EmberfangHillsBounty.definition(),
        WindhowlMarchesCentaurs.definition(),
        HollowMarchWights.definition(),
        AerieWyrmlingCull.definition(),
        BastionWardenSiege.definition(),
        DeepOnesCavePurge.definition(),
        DrakesLairVigil.definition(),
        PassageToAvalon.definition(),
        AvalonWildsVigil.definition(),
        FadingVeilReckoning.definition());
  }
}
