import com.perso.T4C.helper.SpriteBinIO;
import com.perso.T4C.helper.SpriteBinWriter;
import com.perso.T4C.tools.MythicVfxPacker;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import javax.imageio.ImageIO;

/** Resize packed effects and their masks together, preserving their shared tile anchor. */
public final class ScalePackedSpellVfx {
  public static void main(String[] args) throws Exception {
    if (args.length < 2) throw new IllegalArgumentException("Usage: <scale> <family>...");
    double scale = Double.parseDouble(args[0]);
    if (!Double.isFinite(scale) || scale <= 0 || scale > 1)
      throw new IllegalArgumentException("Scale must be in (0, 1]");
    var families = Arrays.asList(Arrays.copyOfRange(args, 1, args.length));
    java.util.function.Predicate<String> selected = name -> families.stream().anyMatch(
        base -> MythicVfxPacker.isFrameOf(name, base) || MythicVfxPacker.isFrameOf(name, base + "A"));
    var originals = new ArrayList<SpriteBinIO.Packed>();
    SpriteBinIO.readAll(Path.of("assets/sprites"), SpriteBinIO.DEFAULT_BASE_NAME,
        p -> { if (selected.test(p.name())) originals.add(p); });
    for (String base : families)
      if (originals.stream().noneMatch(p -> MythicVfxPacker.isFrameOf(p.name(), base)))
        throw new IllegalArgumentException("Missing effect family: " + base);
    var backup = Path.of("target/vfx-size-before");
    Files.createDirectories(backup);
    var entries = new ArrayList<SpriteBinWriter.Entry>();
    for (var p : originals) {
      Files.write(backup.resolve(p.name() + ".png"), p.png());
      int w = Math.max(1, (int) Math.round(p.width() * scale));
      int h = Math.max(1, (int) Math.round(p.height() * scale));
      var image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
      var g = image.createGraphics();
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
      g.drawImage(ImageIO.read(new ByteArrayInputStream(p.png())), 0, 0, w, h, null);
      g.dispose();
      int x = 16 + (int) Math.round((p.off1X() - 16) * scale);
      int y = (int) Math.round(p.off1Y() * scale);
      entries.add(new SpriteBinWriter.Entry(p.name(), w, h, x, y, 32 - w - x, y, image));
    }
    int written = SpriteBinWriter.replaceMatching(Path.of("assets/sprites/sprites.bin"), entries, selected);
    System.out.println("Scaled " + written + " frames to " + scale + ": " + families);
  }
}
