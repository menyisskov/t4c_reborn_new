package com.perso.T4C.tools;

import com.perso.T4C.helper.SpriteBinIO;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Adds the generated Tame progress frame to the packed sprite catalogue. */
public final class TameProgressFrameSeed {
    public static final String NAME = "64kTameProgressFrame";
    private TameProgressFrameSeed() {}

    public static void main(String[] args) throws Exception {
        Path dir = Path.of("assets/sprites");
        Path source = dir.resolve("png/" + NAME + ".png");
        BufferedImage image = ImageIO.read(source.toFile());
        if (image == null) throw new IllegalArgumentException("Invalid PNG: " + source);
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        ImageIO.write(image, "png", png);

        List<SpriteBinIO.Packed> sprites = new ArrayList<>(SpriteBinIO.readAllToList(dir, "sprites"));
        sprites.removeIf(sprite -> NAME.equalsIgnoreCase(sprite.name()));
        sprites.add(new SpriteBinIO.Packed(NAME, image.getWidth(), image.getHeight(),
                0, 0, image.getWidth(), image.getHeight(),
                SpriteBinIO.spriteType(image.getWidth(), image.getHeight()), png.toByteArray()));
        SpriteBinIO.writeSharded(dir, "sprites", sprites);
        System.out.println("Seeded " + NAME + " into sprite catalogue (" + sprites.size() + " sprites)");
    }
}
