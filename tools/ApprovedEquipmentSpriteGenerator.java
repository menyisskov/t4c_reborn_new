import com.perso.T4C.helper.SpriteBinIO;
import com.perso.T4C.helper.SpriteBinWriter;
import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/** Approved Moonsteel designs on T4C's complete original equipment animation rigs. */
public final class ApprovedEquipmentSpriteGenerator {
  private static final List<String> BASES = List.of(
      "PupPlateBody", "PupPlateFoot", "PupPlateGloveL", "PupPlateGloveR", "PupPlateHelm",
      "PupPlateLegs", "PupPlateArmL", "PupPlateArmR", "WoPlateBody", "WoPlateBoots",
      "WoPlateGloveL", "WoPlateGloveR", "WoPlateHelm", "WoPlateLegs",
      "PupLongBow", "PupHalberd", "PupBattleSword",
      "64kInvPlateArmorSleeves", "64kInvPlateArmorFeet", "64kInvPlateGlove",
      "64kInvPlateArmorHelm", "64kInvPlateArmorLegs");

  public static void main(String[] args) throws Exception {
    if (args.length == 2 && args[0].equals("--install")) {
      var entries = new ArrayList<SpriteBinWriter.Entry>();
      SpriteBinIO.readAll(Path.of(args[1]), SpriteBinIO.DEFAULT_BASE_NAME, p -> {
        try {
          entries.add(new SpriteBinWriter.Entry(p.name(), p.width(), p.height(), p.off1X(),
              p.off1Y(), p.off2X(), p.off2Y(), p.type(), ImageIO.read(new ByteArrayInputStream(p.png()))));
        } catch (Exception e) { throw new IllegalStateException(e); }
      });
      System.out.println("Installed " + SpriteBinWriter.merge(Path.of("assets/sprites/sprites.bin"), entries)
          + " approved sprite frames");
      return;
    }
    if (args.length != 2) throw new IllegalArgumentException("Usage: <approved-concepts.png> <staging-directory>");
    Path output = Path.of(args[1]);
    Files.createDirectories(output);
    var entries = new ArrayList<SpriteBinWriter.Entry>();
    SpriteBinIO.readAll(Path.of("assets/sprites"), SpriteBinIO.DEFAULT_BASE_NAME, p -> {
      for (String base : BASES) {
        if (!p.name().startsWith(base)) continue;
        String suffix = p.name().substring(base.length());
        if (!suffix.isEmpty() && !suffix.matches("[A-Z]?[0-9]{3}-[a-zA-Z0-9]+")) continue;
        try {
          var source = ImageIO.read(new ByteArrayInputStream(p.png()));
          var result = new BufferedImage(p.width(), p.height(), BufferedImage.TYPE_INT_ARGB);
          boolean bow = base.equals("PupLongBow"), weapon = bow || base.equals("PupHalberd") || base.equals("PupBattleSword");
          for (int y = 0; y < p.height(); y++) for (int x = 0; x < p.width(); x++) {
            int pixel = source.getRGB(x,y), a = pixel >>> 24;
            if (a == 0) continue;
            int r = (pixel >>> 16) & 255, g = (pixel >>> 8) & 255, b = pixel & 255;
            double light = r*.299 + g*.587 + b*.114;
            boolean wood = weapon && r > b*1.2 && r > g*1.05;
            int colour;
            if (wood || bow && y % 13 > 3) {
              int value = (int)(light*.4);
              colour = (a<<24) | ((value+8)<<16) | ((value+5)<<8) | value;
            } else {
              double value = light < 12 ? light : Math.min(255, 20+light*1.05);
              if (!weapon && (x+2*y)%13 == 0) value *= .72;
              colour = (a<<24) | ((int)(value*.94)<<16) | ((int)(value*.98)<<8) | (int)value;
            }
            result.setRGB(x,y,colour);
          }
          entries.add(new SpriteBinWriter.Entry(base+"__silver300"+suffix,p.width(),p.height(),
              p.off1X(),p.off1Y(),p.off2X(),p.off2Y(),p.type(),result));
        } catch (Exception e) { throw new IllegalStateException(p.name(),e); }
        break;
      }
    });
    BufferedImage sheet = ImageIO.read(Path.of(args[0]).toFile());
    addIcon(entries,sheet,1,0,"64kInvLongBow__silver300");
    addIcon(entries,sheet,0,1,"64kInvHalberd__silver300");
    addIcon(entries,sheet,1,1,"64kInvGlinting Sword__silver300");
    System.out.println("Prepared "+SpriteBinWriter.merge(output.resolve("sprites.bin"),entries)+" Moonsteel frames");
  }

  private static void addIcon(List<SpriteBinWriter.Entry> entries, BufferedImage sheet, int col, int row, String name) {
    int w=sheet.getWidth()/2,h=sheet.getHeight()/2;
    BufferedImage icon=new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
    var g=icon.createGraphics();g.drawImage(sheet,-col*w,-row*h,null);g.dispose();
    Color background=new Color(icon.getRGB(0,0));
    var queue=new ArrayDeque<Integer>();boolean[] visited=new boolean[w*h];
    for(int x=0;x<w;x++){queue.add(x);queue.add((h-1)*w+x);}
    for(int y=0;y<h;y++){queue.add(y*w);queue.add(y*w+w-1);}
    while(!queue.isEmpty()){
      int i=queue.remove();if(visited[i])continue;visited[i]=true;
      int x=i%w,y=i/w;Color c=new Color(icon.getRGB(x,y));
      if(Math.abs(c.getRed()-background.getRed())>25 || Math.abs(c.getGreen()-background.getGreen())>25 || Math.abs(c.getBlue()-background.getBlue())>25)continue;
      icon.setRGB(x,y,0);if(x>0)queue.add(i-1);if(x+1<w)queue.add(i+1);if(y>0)queue.add(i-w);if(y+1<h)queue.add(i+w);
    }
    var result=new BufferedImage(32,32,BufferedImage.TYPE_INT_ARGB);g=result.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
    g.drawImage(icon,0,0,32,32,null);g.dispose();
    entries.add(new SpriteBinWriter.Entry(name,32,32,0,0,0,0,result));
  }
}
