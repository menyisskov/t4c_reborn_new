package com.perso.T4C.helper;

import com.perso.T4C.mapping.definition.AppearanceDefaultsDefinitions;
import com.perso.T4C.mapping.definition.DecorLayerRuleDefinitions;
import com.perso.T4C.mapping.definition.GroundMosaicDefinitions;
import com.perso.T4C.mapping.definition.ItemIconDefinitions;
import com.perso.T4C.mapping.definition.ObjectMappingDefinitions;
import com.perso.T4C.mapping.definition.ObjectPositionDefinitions;
import com.perso.T4C.mapping.definition.XpCurveDefinitions;
import com.perso.T4C.objects.ObjectPos;
import com.perso.T4C.render.ObjectMapping;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class JavaDataCatalog {
  private JavaDataCatalog() {}

  public static List<XpCurve.Entry> xpCurve() {
    return XpCurveDefinitions.all();
  }

  public static Map<Integer, String> itemIcons() {
    return ItemIconDefinitions.all();
  }

  public static List<GroundMosaicData.Definition> mosaics() {
    return GroundMosaicDefinitions.all().stream()
        .map(d -> new GroundMosaicData.Definition(d.id(), d.width(), d.height(), d.frames()))
        .toList();
  }

  public static Set<String> decorRules() {
    return new LinkedHashSet<>(DecorLayerRuleDefinitions.all());
  }

  public static List<ObjectPos> objectPositions() {
    return ObjectPositionDefinitions.all();
  }

  public static Map<String, ObjectMapping> objectMappings() {
    return new LinkedHashMap<>(ObjectMappingDefinitions.all());
  }

  public static AppearanceDefaultsData.Defaults appearanceDefaults() {
    AppearanceDefaultsDefinitions.Defaults source = AppearanceDefaultsDefinitions.defaults();
    List<AppearanceDefaultsData.NakedPart> naked = new ArrayList<>();
    for (var part : source.nakedParts())
      naked.add(
          new AppearanceDefaultsData.NakedPart(part.gender(), part.bodyPart(), part.sprite()));
    List<AppearanceDefaultsData.ConcealmentRule> rules = new ArrayList<>();
    for (var rule : source.concealmentRules())
      rules.add(
          new AppearanceDefaultsData.ConcealmentRule(
              rule.triggerSlot(), rule.appearance(), rule.hiddenParts(), rule.hidesExplicit()));
    List<AppearanceDefaultsData.EquippedOverride> overrides = new ArrayList<>();
    for (var override : source.equippedOverrides())
      overrides.add(
          new AppearanceDefaultsData.EquippedOverride(
              override.gender(),
              override.sourceSlot(),
              override.sourceAppearance(),
              override.targetSlot(),
              override.targetAppearance()));
    return new AppearanceDefaultsData.Defaults(naked, rules, overrides);
  }
}
