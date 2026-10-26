import com.perso.T4C.helper.SpriteBinIO;
import com.perso.T4C.helper.SpriteBinWriter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/** Bake new cloth panels onto the original robe rig without changing frame geometry or alpha. */
public final class CosmeticRobeSpriteGenerator {
  private static final String[] STYLES = {
    "embercourt", "rootwarden", "stormsilk", "tidewoven", "dawnchoir", "nightveil"
  };

  public static void main(String[] args) throws Exception {
    if (args.length != 2) throw new IllegalArgumentException("Usage: <material-atlas.png> <staging-directory>");
    BufferedImage atlas = ImageIO.read(Path.of(args[0]).toFile());
    Path output = Path.of(args[1]);
    if (output.toAbsolutePath().normalize().equals(Path.of("assets/sprites").toAbsolutePath().normalize()))
      throw new IllegalArgumentException("Generate into staging for design review first");
    Files.createDirectories(output);
    List<SpriteBinWriter.Entry> entries = new ArrayList<>();
    List<String> bases = List.of("PupWhiteRobe", "WoWhiteRobe", "64kInvWhiteRobe");
    SpriteBinIO.readAll(Path.of("assets/sprites"), SpriteBinIO.DEFAULT_BASE_NAME, p -> {
      for (String base : bases) {
        if (!p.name().startsWith(base)) continue;
        String suffix = p.name().substring(base.length());
        if (!suffix.isEmpty() && !suffix.matches("[A-Z]?[0-9]{3}-[a-zA-Z0-9]+")) continue;
        try {
          BufferedImage source = ImageIO.read(new ByteArrayInputStream(p.png()));
          for (int style = 0; style < STYLES.length; style++) {
            BufferedImage result = material(source, atlas, style);
            String name = base + "__robe_" + STYLES[style] + suffix;
            entries.add(new SpriteBinWriter.Entry(name, p.width(), p.height(), p.off1X(), p.off1Y(),
                p.off2X(), p.off2Y(), p.type(), result));
          }
        } catch (Exception e) {
          throw new IllegalStateException("Unable to build robe frame " + p.name(), e);
        }
        break;
      }
    });
    int written = SpriteBinWriter.merge(output.resolve("sprites.bin"), entries);
    System.out.println("Prepared " + written + " staged robe sprite frames; live assets unchanged");
  }

  private static BufferedImage material(BufferedImage source, BufferedImage atlas, int style) {
    int cellW = atlas.getWidth() / 3, cellH = atlas.getHeight() / 2;
    int left = (style % 3) * cellW, top = (style / 3) * cellH;
    BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
    for (int y = 0; y < source.getHeight(); y++) {
      for (int x = 0; x < source.getWidth(); x++) {
        int original = source.getRGB(x, y), alpha = original >>> 24;
        if (alpha == 0) continue;
        int materialX = left + 1 + x * (cellW - 2) / source.getWidth();
        int materialY = top + 1 + y * (cellH - 2) / source.getHeight();
        int colour = atlas.getRGB(materialX, materialY);
        int r = (original >>> 16) & 255, g = (original >>> 8) & 255, b = original & 255;
        double light = (r * .299 + g * .587 + b * .114) / 255;
        double shade = light < .06 ? light : .28 + .98 * light;
        int nr = Math.min(255, (int) (((colour >>> 16) & 255) * shade));
        int ng = Math.min(255, (int) (((colour >>> 8) & 255) * shade));
        int nb = Math.min(255, (int) ((colour & 255) * shade));
        result.setRGB(x, y, (alpha << 24) | (nr << 16) | (ng << 8) | nb);
      }
    }
    return result;
  }
}
