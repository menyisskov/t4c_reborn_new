package com.perso.T4C.tools;

import com.perso.T4C.helper.SpriteBinIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/** Packs the approved mythic creature animation strips into a replacement final sprite shard. */
public final class MythicCreatureSpriteInstaller {
  private static final Path SOURCE = Path.of("assets/sprites/source/mythic");
  private static final int FRAMES = 8;
  private static final List<Creature> CREATURES =
      List.of(
          new Creature("dragon", "ObsidianDragon"),
          new Creature("griffin", "BronzeGriffin"),
          new Creature("hydra", "SwampHydra"));

  private record Creature(String sheetPrefix, String spriteBase) {}

  private MythicCreatureSpriteInstaller() {}

  public static void main(String[] args) throws IOException {
    if (args.length != 2)
      throw new IllegalArgumentException("Usage: MythicCreatureSpriteInstaller <last shard> <output shard>");
    List<SpriteBinIO.Packed> sprites = new ArrayList<>();
    SpriteBinIO.readShard(Path.of(args[0]), sprites::add);
    sprites.removeIf(
        sprite ->
            CREATURES.stream()
                .anyMatch(creature -> sprite.name().startsWith(creature.spriteBase())));
    int added = 0;
    for (Creature creature : CREATURES) {
      for (String angle : List.of("000", "045", "090")) {
        added += addStrip(sprites, creature, "walk", creature.spriteBase() + angle);
        added += addStrip(sprites, creature, "attack", creature.spriteBase() + "A" + angle);
      }
      for (String angle : List.of("135", "180")) {
        added += addStrip(sprites, creature, "back_walk", creature.spriteBase() + angle);
        added += addStrip(sprites, creature, "back_attack", creature.spriteBase() + "A" + angle);
      }
      added += addStrip(sprites, creature, "death", creature.spriteBase() + "C");
    }
    SpriteBinIO.writePayload(Path.of(args[1]), sprites);
    System.out.println("Packed " + added + " mythic creature frames into " + args[1]);
  }

  private static int addStrip(
      List<SpriteBinIO.Packed> sprites, Creature creature, String motion, String frameBase)
      throws IOException {
    Path path = SOURCE.resolve(creature.sheetPrefix() + "_" + motion + ".png");
    BufferedImage sheet = ImageIO.read(path.toFile());
    if (sheet == null || !sheet.getColorModel().hasAlpha() || sheet.getWidth() % FRAMES != 0)
      throw new IOException("Invalid animation strip: " + path);
    int width = sheet.getWidth() / FRAMES;
    int height = sheet.getHeight();
    if (width <= 0 || height <= 0) throw new IOException("Empty animation strip: " + path);
    int xOffset = -width / 2;
    int yOffset = -height + 14;
    for (int frame = 0; frame < FRAMES; frame++) {
      BufferedImage image = sheet.getSubimage(frame * width, 0, width, height);
      if (!hasVisiblePixels(image)) throw new IOException("Blank frame " + frame + " in " + path);
      ByteArrayOutputStream png = new ByteArrayOutputStream();
      ImageIO.write(image, "png", png);
      sprites.add(
          new SpriteBinIO.Packed(
              frameBase + "-" + (char) ('a' + frame),
              width,
              height,
              xOffset,
              yOffset,
              xOffset,
              yOffset,
              SpriteBinIO.spriteType(width, height),
              png.toByteArray()));
    }
    return FRAMES;
  }

  private static boolean hasVisiblePixels(BufferedImage image) {
    for (int y = 0; y < image.getHeight(); y++)
      for (int x = 0; x < image.getWidth(); x++)
        if ((image.getRGB(x, y) >>> 24) != 0) return true;
    return false;
  }
}
