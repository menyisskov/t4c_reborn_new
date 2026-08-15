package com.perso.T4C.helper;

import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayerAppearanceDefaultsTest {
    @Test
    void femaleGenderRebuildsEveryNakedBodyLayer() throws Exception {
        List<Object> maleParts = new ArrayList<>();
        for (Map.Entry<BodyPart, String> entry
                : AppearanceDefaultsCatalog.nakedParts(AppearanceDefaultsCatalog.MALE).entrySet()) {
            maleParts.add(entry.getKey());
            maleParts.add(entry.getValue());
        }

        Player player = new Player(maleParts.toArray());
        player.setGender(AppearanceDefaultsCatalog.FEMALE);
        PlayerAppearanceDefaults.applyDefaults(player);

        Map<BodyPart, String> actual = player.getAnimations().getPartMap();
        Map<BodyPart, String> expected = AppearanceDefaultsCatalog.nakedParts(
                AppearanceDefaultsCatalog.FEMALE);
        expected.forEach((part, sprite) -> {
            if (part != BodyPart.HAIR) assertEquals(sprite, actual.get(part), part.name());
        });
        assertEquals("PupNormalHair", actual.get(BodyPart.HAIR),
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
}
