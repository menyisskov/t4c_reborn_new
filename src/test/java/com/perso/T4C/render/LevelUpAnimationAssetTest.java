package com.perso.T4C.render;

import com.perso.T4C.helper.SpriteBinIO;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LevelUpAnimationAssetTest {

    @Test
    void levelUpEffectHasACompleteSpellAnimation() throws Exception {
        String prefix = SpellRenderer.LEVEL_UP_EFFECT.toLowerCase(Locale.ROOT);
        List<SpriteBinIO.Packed> frames = new ArrayList<>();
        List<SpriteBinIO.Packed> legacyFrames = new ArrayList<>();

        SpriteBinIO.readAll(Path.of("assets/sprites"), SpriteBinIO.DEFAULT_BASE_NAME, sprite -> {
            String name = sprite.name().toLowerCase(Locale.ROOT);
            if (name.startsWith(prefix)) {
                frames.add(sprite);
            }
            if (name.startsWith("levelupcodex-") || name.startsWith("spelllevelup-")) {
                legacyFrames.add(sprite);
            }
        });

        assertEquals(18, frames.size(), "The generated level-up sequence must remain complete");
        assertTrue(frames.stream().allMatch(frame -> frame.width() == 96 && frame.height() == 128),
                "Every level-up frame must retain its render dimensions");
        assertTrue(frames.stream().allMatch(frame -> frame.png().length > 0),
                "Every level-up frame must contain image data");
        assertFalse(legacyFrames.iterator().hasNext(), "Renamed level-up frames must be removed");
    }
}
