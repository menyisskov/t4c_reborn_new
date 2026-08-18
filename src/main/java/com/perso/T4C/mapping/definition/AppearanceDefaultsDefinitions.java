package com.perso.T4C.mapping.definition;

import java.util.List;

public final class AppearanceDefaultsDefinitions {
  private AppearanceDefaultsDefinitions() {}

  public record NakedPart(String gender, String bodyPart, String sprite) {}

  public record ConcealmentRule(
      String triggerSlot, String appearance, List<String> hiddenParts, boolean hidesExplicit) {}

  public record EquippedOverride(
      String gender,
      String sourceSlot,
      String sourceAppearance,
      String targetSlot,
      String targetAppearance) {}

  public record Defaults(
      List<NakedPart> nakedParts,
      List<ConcealmentRule> concealmentRules,
      List<EquippedOverride> equippedOverrides) {}

  public static Defaults defaults() {
    return new Defaults(
        List.of(
            new NakedPart("MALE", "BODY", "PupNakedBody"),
            new NakedPart("MALE", "HEAD", "PupNakedHead"),
            new NakedPart("MALE", "HAIR", "PupNormalHair"),
            new NakedPart("MALE", "LEGS", "PupNakedLegs"),
            new NakedPart("MALE", "FEET", "PupNakedFoot"),
            new NakedPart("MALE", "LEFT_ARM", "PupNakedArmL"),
            new NakedPart("MALE", "RIGHT_ARM", "PupNakedArmR"),
            new NakedPart("MALE", "LEFT_HAND", "PupNakedHandL"),
            new NakedPart("MALE", "RIGHT_HAND", "PupNakedHandR"),
            new NakedPart("FEMALE", "BODY", "WoNakedBody"),
            new NakedPart("FEMALE", "HEAD", "WoNakedHead"),
            new NakedPart("FEMALE", "HAIR", "WoHairPonyTail"),
            new NakedPart("FEMALE", "LEGS", "WoNakedLegs"),
            new NakedPart("FEMALE", "FEET", "WoNakedFeet"),
            new NakedPart("FEMALE", "LEFT_ARM", "WoNakedArmL"),
            new NakedPart("FEMALE", "RIGHT_ARM", "WoNakedArmR"),
            new NakedPart("FEMALE", "LEFT_HAND", "WoNakedHandL"),
            new NakedPart("FEMALE", "RIGHT_HAND", "WoNakedHandR")),
        List.of(
            new ConcealmentRule("HEAD", "PupLeatherHelm", List.of("HEAD", "HAIR"), false),
            new ConcealmentRule("HEAD", "PupPlateHelm", List.of("HEAD", "HAIR"), false),
            new ConcealmentRule("HEAD", "PupChainMailCoif", List.of("HEAD", "HAIR"), false),
            new ConcealmentRule("HEAD", "PupHornedHelmet", List.of("HEAD", "HAIR"), false),
            new ConcealmentRule("HEAD", "V2_Haume01", List.of("HAIR"), false),
            new ConcealmentRule("HEAD", "V2_Haume02", List.of("HAIR"), false),
            new ConcealmentRule("HEAD", "V2_Haume04", List.of("HAIR"), false),
            new ConcealmentRule("HEAD", "HalloweenHead", List.of("HAIR"), false),
            new ConcealmentRule("HEAD", "PupElvenHat", List.of("HAIR"), false),
            new ConcealmentRule("HEAD", "PupGoldenCrown", List.of("HAIR"), false),
            new ConcealmentRule("HEAD", "WoChainMailCoif", List.of("HAIR"), false),
            new ConcealmentRule("HEAD", "WoPlateHelm", List.of("HAIR"), false),
            new ConcealmentRule("HEAD", "64kItemGrSkeletonHelm", List.of("HAIR"), false),
            new ConcealmentRule("BODY", "PupArmoredRobe", List.of("LEFT_ARM", "RIGHT_ARM"), false),
            new ConcealmentRule(
                "BODY", "PupChainMailBody", List.of("LEFT_ARM", "RIGHT_ARM"), false),
            new ConcealmentRule("BODY", "PupLeatherArmor", List.of("LEFT_ARM", "RIGHT_ARM"), false),
            new ConcealmentRule("BODY", "PupMageRobe", List.of("LEFT_ARM", "RIGHT_ARM"), false),
            new ConcealmentRule("BODY", "PupNecromanRobe", List.of("LEFT_ARM", "RIGHT_ARM"), false),
            new ConcealmentRule("BODY", "PupPlateBody", List.of("LEFT_ARM", "RIGHT_ARM"), false),
            new ConcealmentRule(
                "BODY", "PupMithrilPlateBody", List.of("LEFT_ARM", "RIGHT_ARM"), false),
            new ConcealmentRule(
                "BODY", "PupStuddedBodyArmor", List.of("LEFT_ARM", "RIGHT_ARM"), false),
            new ConcealmentRule("BODY", "PupWhiteRobe", List.of("LEFT_ARM", "RIGHT_ARM"), false),
            new ConcealmentRule("BODY", "WoChainBody", List.of("LEFT_ARM", "RIGHT_ARM"), false),
            new ConcealmentRule("BODY", "WoClothBody", List.of("LEFT_ARM", "RIGHT_ARM"), false),
            new ConcealmentRule("BODY", "WoLeatherBody", List.of("LEFT_ARM", "RIGHT_ARM"), false),
            new ConcealmentRule("BODY", "WoMageRobe", List.of("LEFT_ARM", "RIGHT_ARM"), false),
            new ConcealmentRule("BODY", "WoNecromanRobe", List.of("LEFT_ARM", "RIGHT_ARM"), false),
            new ConcealmentRule("BODY", "WoPlateBody", List.of("LEFT_ARM", "RIGHT_ARM"), false),
            new ConcealmentRule("BODY", "WoWhiteRobe", List.of("LEFT_ARM", "RIGHT_ARM"), false),
            new ConcealmentRule(
                "BODY", "ManLichRobeBlanc", List.of("LEFT_ARM", "RIGHT_ARM"), false),
            new ConcealmentRule("BODY", "ManLichRobeGold", List.of("LEFT_ARM", "RIGHT_ARM"), false),
            new ConcealmentRule(
                "BODY", "ManLichRobeGreen", List.of("LEFT_ARM", "RIGHT_ARM"), false),
            new ConcealmentRule(
                "BODY", "ManLichRobeKimono", List.of("LEFT_ARM", "RIGHT_ARM"), false),
            new ConcealmentRule("BODY", "ManLichRobeNoir", List.of("LEFT_ARM", "RIGHT_ARM"), false),
            new ConcealmentRule("BODY", "ManLichRobeOri", List.of("LEFT_ARM", "RIGHT_ARM"), false),
            new ConcealmentRule(
                "BODY", "ManLichRobeRouge", List.of("LEFT_ARM", "RIGHT_ARM"), false),
            new ConcealmentRule("BODY", "PupRedRobe", List.of("LEFT_ARM", "RIGHT_ARM"), false),
            new ConcealmentRule(
                "BODY", "PupSpikeLeatherBody", List.of("LEFT_ARM", "RIGHT_ARM"), false),
            new ConcealmentRule("BODY", "PupLeatherBody", List.of("LEFT_ARM", "RIGHT_ARM"), false),
            new ConcealmentRule(
                "BODY", "V2_ManArmorBody01", List.of("LEFT_ARM", "RIGHT_ARM"), false),
            new ConcealmentRule("BODY", "PupArmoredRobe", List.of("LEGS", "FEET"), false),
            new ConcealmentRule("BODY", "PupArmoredRobe", List.of("BOOT"), true),
            new ConcealmentRule("BODY", "PupMageRobe", List.of("LEGS", "FEET"), false),
            new ConcealmentRule("BODY", "PupMageRobe", List.of("BOOT"), true),
            new ConcealmentRule("BODY", "PupNecromanRobe", List.of("LEGS", "FEET"), false),
            new ConcealmentRule("BODY", "PupNecromanRobe", List.of("BOOT"), true),
            new ConcealmentRule("BODY", "PupWhiteRobe", List.of("LEGS", "FEET"), false),
            new ConcealmentRule("BODY", "PupWhiteRobe", List.of("BOOT"), true),
            new ConcealmentRule("BODY", "PupRedRobe", List.of("LEGS", "FEET"), false),
            new ConcealmentRule("BODY", "PupRedRobe", List.of("BOOT"), true),
            new ConcealmentRule("BODY", "WoMageRobe", List.of("LEGS", "FEET"), false),
            new ConcealmentRule("BODY", "WoMageRobe", List.of("BOOT"), true),
            new ConcealmentRule("BODY", "WoNecromanRobe", List.of("LEGS", "FEET"), false),
            new ConcealmentRule("BODY", "WoNecromanRobe", List.of("BOOT"), true),
            new ConcealmentRule("BODY", "WoWhiteRobe", List.of("LEGS", "FEET"), false),
            new ConcealmentRule("BODY", "WoWhiteRobe", List.of("BOOT"), true),
            new ConcealmentRule("BODY", "ManLichRobeBlanc", List.of("LEGS", "FEET"), false),
            new ConcealmentRule("BODY", "ManLichRobeBlanc", List.of("BOOT"), true),
            new ConcealmentRule("BODY", "ManLichRobeGold", List.of("LEGS", "FEET"), false),
            new ConcealmentRule("BODY", "ManLichRobeGold", List.of("BOOT"), true),
            new ConcealmentRule("BODY", "ManLichRobeGreen", List.of("LEGS", "FEET"), false),
            new ConcealmentRule("BODY", "ManLichRobeGreen", List.of("BOOT"), true),
            new ConcealmentRule("BODY", "ManLichRobeKimono", List.of("LEGS", "FEET"), false),
            new ConcealmentRule("BODY", "ManLichRobeKimono", List.of("BOOT"), true),
            new ConcealmentRule("BODY", "ManLichRobeNoir", List.of("LEGS", "FEET"), false),
            new ConcealmentRule("BODY", "ManLichRobeNoir", List.of("BOOT"), true),
            new ConcealmentRule("BODY", "ManLichRobeOri", List.of("LEGS", "FEET"), false),
            new ConcealmentRule("BODY", "ManLichRobeOri", List.of("BOOT"), true),
            new ConcealmentRule("BODY", "ManLichRobeRouge", List.of("LEGS", "FEET"), false),
            new ConcealmentRule("BODY", "ManLichRobeRouge", List.of("BOOT"), true)),
        List.of(
            new EquippedOverride("FEMALE", "BODY", "PupBodyClothSet1", "BODY", "WoClothBody"),
            new EquippedOverride("FEMALE", "LEGS", "PupLegsClothSet1", "ROBELEGS", "WoClothRobe"),
            new EquippedOverride("FEMALE", "BODY", "PupPlateBody", "BODY", "WoPlateBody"),
            new EquippedOverride("FEMALE", "FEET", "PupPlateFoot", "FEET", "WoPlateBoots"),
            new EquippedOverride(
                "FEMALE", "LEFT_HAND", "PupPlateGloveL", "LEFT_HAND", "WoPlateGloveL"),
            new EquippedOverride(
                "FEMALE", "RIGHT_HAND", "PupPlateGloveR", "RIGHT_HAND", "WoPlateGloveR"),
            new EquippedOverride("FEMALE", "HAT", "PupPlateHelm", "HAT", "WoPlateHelm"),
            new EquippedOverride("FEMALE", "LEGS", "PupPlateLegs", "LEGS", "WoPlateLegs"),
            new EquippedOverride("FEMALE", "BODY", "PupLeatherBody", "BODY", "WoLeatherArms"),
            new EquippedOverride("FEMALE", "BODY", "PupSpikeLeatherBody", "BODY", "WoLeatherArms"),
            new EquippedOverride("FEMALE", "FEET", "PupLeatherBoots", "FEET", "WoLeatherBoots"),
            new EquippedOverride(
                "FEMALE", "FEET", "PupBlackLeatherBoots", "FEET", "WoBlackLeatherBoots"),
            new EquippedOverride(
                "FEMALE", "LEFT_HAND", "PupLeatherGloveL", "LEFT_HAND", "WoLeatherGloveL"),
            new EquippedOverride(
                "FEMALE", "RIGHT_HAND", "PupLeatherGloveR", "RIGHT_HAND", "WoLeatherGloveR"),
            new EquippedOverride("FEMALE", "HAT", "PupLeatherHelm", "HAT", "WoLeatherHelm"),
            new EquippedOverride("FEMALE", "LEGS", "PupLeatherPants", "LEGS", "WoLeatherLegs"),
            new EquippedOverride("FEMALE", "BODY", "PupStuddedBodyArmor", "BODY", "WoStuddedBody"),
            new EquippedOverride("FEMALE", "LEGS", "PupStuddedLegs", "LEGS", "WoStuddedLegs"),
            new EquippedOverride("FEMALE", "BODY", "PupChainMailBody", "BODY", "WoChainBody"),
            new EquippedOverride("FEMALE", "HAT", "PupChainMailCoif", "HAT", "WoChainMailCoif"),
            new EquippedOverride("FEMALE", "LEGS", "PupChainMailLegs", "LEGS", "WoChainLegs"),
            new EquippedOverride("FEMALE", "BODY", "PupArmoredRobe", "BODY", "WoArmoredRobe"),
            new EquippedOverride("FEMALE", "BODY", "PupMageRobe", "BODY", "WoMageRobe"),
            new EquippedOverride("FEMALE", "BODY", "PupNecromanRobe", "BODY", "WoNecromanRobe"),
            new EquippedOverride("FEMALE", "BODY", "PupRedRobe", "BODY", "WoRedRobe"),
            new EquippedOverride("FEMALE", "BODY", "PupWhiteRobe", "BODY", "WoWhiteRobe"),
            new EquippedOverride("FEMALE", "BODY", "ManLichRobeOri", "BODY", "WoArmoredRobe"),
            new EquippedOverride("FEMALE", "BODY", "ManLichRobeBlanc", "BODY", "WoArmoredRobe"),
            new EquippedOverride("FEMALE", "BODY", "ManLichRobeGold", "BODY", "WoArmoredRobe"),
            new EquippedOverride("FEMALE", "BODY", "ManLichRobeGreen", "BODY", "WoArmoredRobe"),
            new EquippedOverride("FEMALE", "BODY", "ManLichRobeKimono", "BODY", "WoArmoredRobe"),
            new EquippedOverride("FEMALE", "BODY", "ManLichRobeNoir", "BODY", "WoArmoredRobe"),
            new EquippedOverride("FEMALE", "BODY", "ManLichRobeRouge", "BODY", "WoArmoredRobe")));
  }
}
