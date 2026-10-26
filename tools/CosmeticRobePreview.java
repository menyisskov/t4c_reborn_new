import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.helper.*;
import com.perso.T4C.player.*;
public class CosmeticRobePreview {
 static final String[] STYLES={"embercourt","rootwarden","stormsilk","tidewoven","dawnchoir","nightveil"};
 public static void main(String[] args){var config=new Lwjgl3ApplicationConfiguration();config.setWindowedMode(720,300);config.setInitialVisible(false);config.disableAudio(true);
 new Lwjgl3Application(new ApplicationAdapter(){
  public void create(){try{
   SpriteLoader.getInstance().loadSpriteBin("target/robe-preview-sprites/sprites.bin");
   Player[] models=new Player[12];PlayerMovement[] movement=new PlayerMovement[12];
   for(int i=0;i<12;i++){
    var p=new Player();p.setGender(i<6?"MALE":"FEMALE");PlayerAppearanceDefaults.applyDefaults(p);
    p.getAnimations().getPartMap().put(BodyPart.BODY,(i<6?"PupWhiteRobe":"WoWhiteRobe")+"__robe_"+STYLES[i%6]);
    ConcealmentResolver.applyConcealment(p.getAnimations().getPartMap());p.getAnimations().refresh();models[i]=p;movement[i]=new PlayerMovement();
   }
   var batch=new SpriteBatch();var cam=new OrthographicCamera();cam.setToOrtho(true,720,300);batch.setProjectionMatrix(cam.combined);
   var buffer=new FrameBuffer(Pixmap.Format.RGBA8888,1440,600,false);float[][] dirs={{0,1},{-1,1},{-1,0},{-1,-1},{0,-1},{1,-1},{1,0},{1,1}};
   for(int frame=0;frame<48;frame++){
    buffer.begin();Gdx.gl.glClearColor(.065f,.075f,.085f,1);Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);batch.begin();
    float[] dir=dirs[frame/6];
    for(int i=0;i<12;i++){movement[i].faceToward(0,0,dir[0],dir[1]);models[i].getAnimations().update(1f/17f,true);models[i].getAnimations().render(batch,new Vector2(60+(i%6)*120,110+(i/6)*150),movement[i].getCurrentAngle(),movement[i].isFlipX(),true);}
    batch.end();var pixels=Pixmap.createFromFrameBuffer(0,0,1440,600);PixmapIO.writePNG(Gdx.files.local(String.format("target/robe-preview-frames/frame_%02d.png",frame)),pixels,-1,true);pixels.dispose();buffer.end();
   }
   buffer.dispose();batch.dispose();System.out.println("Rendered twelve staged robe appearances in eight directions");
  }catch(Exception e){e.printStackTrace();System.exit(1);}finally{Gdx.app.exit();}}
 },config);}
}
