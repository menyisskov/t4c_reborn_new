package com.perso.T4C.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PlayerAppearanceDefaultsTest {
  @Test
  void femaleGenderRebuildsEveryNakedBodyLayer() throws Exception {
    List<Object> maleParts = new ArrayList<>();
    for (Map.Entry<BodyPart, String> entry :
        AppearanceDefaultsCatalog.nakedParts(AppearanceDefaultsCatalog.MALE).entrySet()) {
      maleParts.add(entry.getKey());
      maleParts.add(entry.getValue());
    }
    Player player = new Player(maleParts.toArray());
    player.setGender(AppearanceDefaultsCatalog.FEMALE);
    PlayerAppearanceDefaults.applyDefaults(player);
    Map<BodyPart, String> actual = player.getAnimations().getPartMap();
    Map<BodyPart, String> expected =
        AppearanceDefaultsCatalog.nakedParts(AppearanceDefaultsCatalog.FEMALE);
    expected.forEach(
        (part, sprite) -> {
          if (part != BodyPart.HAIR) assertEquals(sprite, actual.get(part), part.name());
        });
    assertEquals(
        "PupNormalHair",
        actual.get(BodyPart.HAIR),
        "Existing hair customization must remain untouched");
  }

  @Test
  void femaleClothUsesTheSameAppearancesAsAmelia() throws Exception {
    Player player = new Player();
    player.setGender(AppearanceDefaultsCatalog.FEMALE);
    player.getEquippedItems().put(BodyPart.BODY, "item.cloth_vest");
    player.getEquippedItems().put(BodyPart.LEGS, "item.cloth_pants");
    PlayerAppearanceDefaults.applyDefaults(player);
    Map<BodyPart, String> actual = player.getAnimations().getPartMap();
    assertEquals("WoClothBody", actual.get(BodyPart.BODY));
    assertEquals("WoClothRobe", actual.get(BodyPart.ROBELEGS));
  }

  @Test
  void femalePlateUsesWomanPuppetAppearances() throws Exception {
    Player player = new Player();
    player.setGender(AppearanceDefaultsCatalog.FEMALE);
    player.getEquippedItems().put(BodyPart.BODY, "item.ancient_platemail_armor");
    player.getEquippedItems().put(BodyPart.FEET, "item.ancient_platemail_boots");
    player.getEquippedItems().put(BodyPart.LEFT_HAND, "item.ancient_platemail_gauntlets");
    player.getEquippedItems().put(BodyPart.HEAD, "item.ancient_platemail_helmet");
    player.getEquippedItems().put(BodyPart.LEGS, "item.ancient_platemail_leggings");
    PlayerAppearanceDefaults.applyDefaults(player);
    Map<BodyPart, String> actual = player.getAnimations().getPartMap();
    assertEquals("WoPlateBody", actual.get(BodyPart.BODY));
    assertEquals("WoPlateBoots", actual.get(BodyPart.FEET));
    assertEquals("WoPlateGloveL", actual.get(BodyPart.LEFT_HAND));
    assertEquals("WoPlateGloveR", actual.get(BodyPart.RIGHT_HAND));
    assertEquals("WoPlateHelm", actual.get(BodyPart.HAT));
    assertEquals("WoPlateLegs", actual.get(BodyPart.LEGS));
  }

  @Test
  void femaleLeatherAndChainUseWomanPuppetAppearances() throws Exception {
    Player player = new Player();
    player.setGender(AppearanceDefaultsCatalog.FEMALE);
    player.getEquippedItems().put(BodyPart.BODY, "item.elven_chainmail_armor");
    player.getEquippedItems().put(BodyPart.HEAD, "item.elven_chainmail_helmet");
    player.getEquippedItems().put(BodyPart.LEGS, "item.elven_chainmail_leggings");
    player.getEquippedItems().put(BodyPart.FEET, "item.elven_leather_boots");
    player.getEquippedItems().put(BodyPart.LEFT_HAND, "item.elven_leather_gloves");
    PlayerAppearanceDefaults.applyDefaults(player);
    Map<BodyPart, String> actual = player.getAnimations().getPartMap();
    assertEquals("WoChainBody", actual.get(BodyPart.BODY));
    assertEquals("WoChainMailCoif", actual.get(BodyPart.HAT));
    assertEquals("WoChainLegs", actual.get(BodyPart.LEGS));
    assertEquals("WoLeatherBoots", actual.get(BodyPart.FEET));
    assertEquals("WoLeatherGloveL", actual.get(BodyPart.LEFT_HAND));
    assertEquals("WoLeatherGloveR", actual.get(BodyPart.RIGHT_HAND));
  }

  @Test
  void femaleColoredRobePreservesItsPalette() {
    AppearanceDefaultsCatalog.EquippedAppearance actual =
        AppearanceDefaultsCatalog.equippedAppearance(
            AppearanceDefaultsCatalog.FEMALE, BodyPart.BODY, "PupWhiteRobe__pal7");
    assertEquals(BodyPart.BODY, actual.bodyPart());
    assertEquals("WoWhiteRobe__pal7", actual.sprite());
  }

  @Test
  void removedMithrilFramesFallBackToVisiblePlateForBothGenders() throws Exception {
    Player male = new Player();
    male.getEquippedItems().put(BodyPart.BODY, "item.mithril_plate_armor");
    PlayerAppearanceDefaults.applyDefaults(male);
    Player female = new Player();
    female.setGender(AppearanceDefaultsCatalog.FEMALE);
    female.getEquippedItems().put(BodyPart.BODY, "item.mithril_plate_armor");
    PlayerAppearanceDefaults.applyDefaults(female);
    assertEquals("PupPlateBody", male.getAnimations().getPartMap().get(BodyPart.BODY));
    assertEquals("WoPlateBody", female.getAnimations().getPartMap().get(BodyPart.BODY));
  }
}
