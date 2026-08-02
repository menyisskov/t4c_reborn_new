package com.perso.T4C.tools;

import com.perso.T4C.helper.SpriteBinIO;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Adds the generated Tame Beast icon to the packed sprite catalogue. */
public final class TameBeastIconSeed {
    private static final String NAME = "64kSpellIconTameBeast";
    private TameBeastIconSeed() {}

    public static void main(String[] args) throws Exception {
        Path dir = Path.of("assets/sprites");
        List<SpriteBinIO.Packed> sprites = new ArrayList<>(SpriteBinIO.readAllToList(dir, "sprites"));
        sprites.removeIf(s -> NAME.equalsIgnoreCase(s.name()));
        byte[] png = Files.readAllBytes(dir.resolve("png/" + NAME + ".png"));
        sprites.add(new SpriteBinIO.Packed(NAME, 30, 30, 0, 0, 30, 30,
                SpriteBinIO.spriteType(30, 30), png));
        SpriteBinIO.writeSharded(dir, "sprites", sprites);
        System.out.println("Seeded " + NAME + " into sprite catalogue (" + sprites.size() + " sprites)");
    }
}
