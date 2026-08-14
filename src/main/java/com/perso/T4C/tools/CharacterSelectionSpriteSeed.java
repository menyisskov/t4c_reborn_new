package com.perso.T4C.tools;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.SpriteBinWriter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Packs the original 1.68 character-selection resources into the main sprite library. */
public final class CharacterSelectionSpriteSeed {
    private static final Path SOURCE = Path.of("assets/ui/character-selection");
    private static final List<String> NAMES = List.of(
            "Back01_1280", "Connect_Title2",
            "E_Back", "J_Back", "PS_Back", "PS_BackDown", "PS_BtnD", "PS_BtnH",
            "PS_BtnN", "PS_Over", "PS_SBtnDNH", "PS_SBtnDNN", "PS_SBtnUPH",
            "PS_SBtnUPN", "PS_SmallBtnH", "PS_SmallBtnN", "Q_Back", "Q_BackSelect");

    private CharacterSelectionSpriteSeed() {
    }

    public static void main(String[] args) throws Exception {
        List<SpriteBinWriter.Entry> entries = new ArrayList<>(NAMES.size());
        for (String name : NAMES) {
            String sourceName = name.equals("Connect_Title2") ? "Connect_Title2_Reborn" : name;
            Path file = SOURCE.resolve(sourceName + ".png");
            BufferedImage image = ImageIO.read(file.toFile());
            if (image == null) throw new IllegalArgumentException("Invalid PNG: " + file);
            entries.add(new SpriteBinWriter.Entry(name, image.getWidth(), image.getHeight(),
                    0, 0, image.getWidth(), image.getHeight(), image));
        }
        int written = SpriteBinWriter.replaceMatching(Path.of(Paths.SPRITE_BIN), entries,
                name -> name.matches("Back\\d{2}_1280"));
        System.out.println("Seeded " + written + " character-selection sprites");
    }
}
