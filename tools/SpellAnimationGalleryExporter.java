import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.google.gson.*;
import com.perso.T4C.config.NativeTimingProfile;
import com.perso.T4C.helper.SpriteLoader;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.render.SpellRenderer;
import com.perso.T4C.spell.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.nio.file.Files;
import java.util.*;

/** Exports existing spell art through the actual renderer, including masks and projectile palettes. */
public final class SpellAnimationGalleryExporter {
  private static final int CELL=256,COLUMNS=8;
  private static final Gson JSON=new GsonBuilder().setPrettyPrinting().create();
  private static final Path OUTPUT=Path.of("docs/spell-animations");
  private static Object field(Object object,String name)throws Exception {
    var f=object.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(object);
  }
  private static String base(String value){return value==null?"":value.replaceAll("-+$","");}
  private static void targets(Map<String,Object> row,SpellData spell,String category) {
    String type,count,note="";
    if(category.equals("Return scrolls") || spell.getKey().equals("spell.avalon_gateway")) {type="Self teleport";count="1 caster";}
    else if(category.equals("Divine attacks")) {type="Single target";count="1 opponent";}
    else if(spell.isAttack() && spell.getRadius()>0) {
      type="Area attack";count="Up to 8 enemies";
      note="Targets must be alive, in range and permitted by combat/line-of-sight rules.";
    } else if(spell.isAttack()) {type="Single target + splash";count="1 primary + up to 7 adjacent enemies";
      note="The primary target takes full damage; enemies on adjacent tiles (including diagonals) take 25% damage. Eight targets maximum, with line of sight and combat restrictions.";}
    else {
      type=spell.getKey().equals("spell.renew_armor")?"Self buff refresh":"Self support";count="1 caster";
      if(spell.getRadius()>0)note="Configured with an area radius, but the current game applies this support spell to the caster only; group healing is not implemented.";
    }
    row.put("targeting",type);row.put("targets",count);row.put("radius",spell.getRadius());row.put("targetNote",note);
  }
  public static void main(String[] args)throws Exception {
    var selected=new LinkedHashMap<Integer,SpellData>();
    String[] classes=JSON.fromJson(Files.readString(Path.of("target/gallery-new-spell-classes.json")),String[].class);
    for(String name:classes) {
      try {
        var authored=(SpellData)Class.forName("com.perso.T4C.spell.definition."+name).getMethod("definition").invoke(null);
        var registered=SpellRegistry.findById(authored.getSpellId());
        if(registered!=null)selected.put(registered.getSpellId(),registered);
      }catch(ClassNotFoundException ignored){}
    }
    for(var s:SpellRegistry.load()) {
      boolean ladder=HighTierSpellCurve.TIERS.contains(s.getMinLevel()) && Objects.equals(base(s.getImpactSpell()),HighTierSpellCurve.IMPACTS.getOrDefault(s.getElement(),Map.of()).get(s.getMinLevel()));
      if(ladder || s.getKey().startsWith("spell.mob_divine_") || s.getKey().startsWith("spell.item_divine_") || s.getKey().startsWith("spell.item_training_"))selected.put(s.getSpellId(),s);
    }
    Files.createDirectories(OUTPUT.resolve("assets"));
    var config=new Lwjgl3ApplicationConfiguration();config.setWindowedMode(64,64);config.setInitialVisible(false);config.disableAudio(true);
    new Lwjgl3Application(new ApplicationAdapter(){
      @Override public void create(){try { export(selected.values()); }catch(Exception e){e.printStackTrace();System.exit(1);}finally{Gdx.app.exit();}}
    },config);
  }
  private static void export(Collection<SpellData> selected)throws Exception {
    var sprites=SpriteLoader.getInstance();sprites.loadSpriteBin(com.perso.T4C.config.Paths.SPRITE_BIN);
    var renderer=new SpellRenderer(sprites);var batch=new SpriteBatch();
    var impactClass=Class.forName("com.perso.T4C.render.SpellRenderer$ImpactFrame");
    var impactFrames=SpellRenderer.class.getDeclaredMethod("getImpactFrames",String.class);impactFrames.setAccessible(true);
    var projectileFrames=SpellRenderer.class.getDeclaredMethod("getProjectileFrames",String.class);projectileFrames.setAccessible(true);
    var drawImpact=SpellRenderer.class.getDeclaredMethod("renderImpactFrame",SpriteBatch.class,impactClass,float.class,float.class);drawImpact.setAccessible(true);
    var buffer=new FrameBuffer(Pixmap.Format.RGBA8888,CELL,CELL,false);
    Map<String,Object> assets=new LinkedHashMap<>();List<Map<String,Object>> spells=new ArrayList<>();
    for(var s:selected) {
      var visuals=SpellVisualResolver.resolve(s);
      boolean ladder=Objects.equals(base(s.getImpactSpell()),HighTierSpellCurve.IMPACTS.getOrDefault(s.getElement(),Map.of()).get(s.getMinLevel()));
      String category=ladder?"Combat ladder":s.getKey().startsWith("spell.mob_divine_")?"Divine attacks":s.getKey().startsWith("spell.item_divine_")||s.getKey().startsWith("spell.item_training_")?"Return scrolls":"Other player spells";
      Map<String,Object> row=new LinkedHashMap<>();row.put("id",s.getSpellId());row.put("key",s.getKey());row.put("name",I18n.resolve(s.getName()));row.put("level",s.getMinLevel());row.put("element",s.getElement());row.put("category",category);row.put("description",I18n.resolve(s.getDescription()));
      targets(row,s,category);
      String[] families={visuals.impact(),visuals.projectile()};
      for(int mode=0;mode<2;mode++) {
        String family=families[mode];if(family==null || family.isBlank())continue;
        String key=(mode==0?"impact:":"projectile:")+family;
        row.put(mode==0?"impact":"projectile",key);
        if(assets.containsKey(key))continue;
        var frames=(List<?>) (mode==0?impactFrames:projectileFrames).invoke(renderer,family);
        Map<String,Object> asset=new LinkedHashMap<>();asset.put("family",family);asset.put("count",frames.size());asset.put("cell",CELL);asset.put("columns",COLUMNS);assets.put(key,asset);
        if(frames.isEmpty()){System.out.println("No packed frames: "+key);continue;}
        float minX=Float.MAX_VALUE,minY=Float.MAX_VALUE,maxX=-Float.MAX_VALUE,maxY=-Float.MAX_VALUE;
        for(Object frame:frames) {
          var region=(TextureRegion)field(frame,"region");float x=(int)field(frame,mode==0?"offsetX":"offsetX1"),y=(int)field(frame,mode==0?"offsetY":"offsetY1");
          minX=Math.min(minX,x);minY=Math.min(minY,y);maxX=Math.max(maxX,x+region.getRegionWidth());maxY=Math.max(maxY,y+region.getRegionHeight());
        }
        float side=Math.max(maxX-minX,maxY-minY)+16;
        var camera=new OrthographicCamera();camera.setToOrtho(true,side,side);camera.position.set((minX+maxX)/2,(minY+maxY)/2,0);camera.update();batch.setProjectionMatrix(camera.combined);
        var sheet=new Pixmap(COLUMNS*CELL,((frames.size()+COLUMNS-1)/COLUMNS)*CELL,Pixmap.Format.RGBA8888);
        for(int index=0;index<frames.size();index++) {
          Object frame=frames.get(index);buffer.begin();Gdx.gl.glClearColor(.035f,.045f,.055f,1);Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);batch.begin();
          if(mode==0)drawImpact.invoke(renderer,batch,frame,0f,0f);
          else {
            var region=(TextureRegion)field(frame,"region");int x=(int)field(frame,"offsetX1"),y=(int)field(frame,"offsetY1");
            batch.draw(region,x,y+region.getRegionHeight(),region.getRegionWidth(),-region.getRegionHeight());
          }
          batch.end();var pixels=Pixmap.createFromFrameBuffer(0,0,CELL,CELL);buffer.end();
          // Readback has a bottom-left origin; store each sprite tile top-down.
          for(int y=0;y<CELL;y++)sheet.drawPixmap(pixels,0,CELL-1-y,CELL,1,(index%COLUMNS)*CELL,(index/COLUMNS)*CELL+y,CELL,1);
          pixels.dispose();
        }
        String filename=key.replaceAll("[^A-Za-z0-9]+","_").toLowerCase(Locale.ROOT)+".png";
        PixmapIO.writePNG(Gdx.files.absolute(OUTPUT.resolve("assets").resolve(filename).toAbsolutePath().toString()),sheet,-1,false);sheet.dispose();
        asset.put("file","assets/"+filename);asset.put("nativeWidth",Math.round(maxX-minX));asset.put("nativeHeight",Math.round(maxY-minY));asset.put("nativeLeft",minX);asset.put("nativeTop",minY);asset.put("previewSpan",side);
        System.out.println(key+" -> "+frames.size()+" frames");
      }
      spells.add(row);
    }
    spells.sort(Comparator.comparing((Map<String,Object> row)->(String)row.get("category")).thenComparingInt(row->(int)row.get("element")).thenComparingInt(row->(int)row.get("level")));
    Map<String,Object> result=new LinkedHashMap<>();result.put("fps",1f/NativeTimingProfile.current().frameDurationSeconds());result.put("spells",spells);result.put("assets",assets);
    Files.writeString(OUTPUT.resolve("manifest.json"),JSON.toJson(result));buffer.dispose();renderer.dispose();batch.dispose();
    System.out.println("Exported "+spells.size()+" spells and "+assets.size()+" animation families.");
  }
}
