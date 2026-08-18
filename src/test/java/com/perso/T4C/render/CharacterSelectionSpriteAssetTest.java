package com.perso.T4C.render;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.helper.SpriteBinIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class CharacterSelectionSpriteAssetTest {
  @Test
  void originalCharacterSelectionSpritesArePacked() throws Exception {
    Map<String, int[]> expected = new HashMap<>();
    expected.put("Back01_1280", size(1280, 768));
    expected.put("Connect_Title2", size(360, 114));
    expected.put("E_Back", size(520, 197));
    expected.put("J_Back", size(456, 251));
    expected.put("PS_Back", size(456, 239));
    expected.put("PS_BackDown", size(456, 64));
    for (String name : new String[] {"PS_BtnD", "PS_BtnH", "PS_BtnN"}) {
      expected.put(name, size(116, 27));
    }
    expected.put("PS_Over", size(256, 23));
    for (String name : new String[] {"PS_SBtnDNH", "PS_SBtnDNN", "PS_SBtnUPH", "PS_SBtnUPN"}) {
      expected.put(name, size(20, 21));
    }
    expected.put("PS_SmallBtnH", size(72, 27));
    expected.put("PS_SmallBtnN", size(72, 27));
    expected.put("Q_Back", size(628, 384));
    expected.put("Q_BackSelect", size(500, 40));
    Map<String, SpriteBinIO.Packed> packed = new HashMap<>();
    Set<String> backgrounds = new java.util.HashSet<>();
    SpriteBinIO.readAll(
        Path.of("assets/sprites"),
        SpriteBinIO.DEFAULT_BASE_NAME,
        sprite -> {
          if (expected.containsKey(sprite.name())) packed.put(sprite.name(), sprite);
          if (sprite.name().matches("Back\\d{2}_1280")) backgrounds.add(sprite.name());
        });
    assertEquals(expected.keySet(), packed.keySet());
    assertEquals(Set.of("Back01_1280"), backgrounds);
    expected.forEach(
        (name, dimensions) -> {
          SpriteBinIO.Packed sprite = packed.get(name);
          assertArrayEquals(dimensions, size(sprite.width(), sprite.height()), name);
          assertTrue(sprite.png().length > 0, name + " must contain PNG payload data");
        });
    BufferedImage title =
        ImageIO.read(new ByteArrayInputStream(packed.get("Connect_Title2").png()));
    assertEquals(
        0,
        title.getRGB(0, 0) >>> 24,
        "Connect_Title2 must retain a genuinely transparent background");
  }

  private static int[] size(int width, int height) {
    return new int[] {width, height};
  }
}
