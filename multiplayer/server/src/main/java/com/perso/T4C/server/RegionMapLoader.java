package com.perso.T4C.server;

import com.perso.T4C.helper.BinaryIOUtils;
import com.perso.T4C.online.model.PortalContent;
import com.perso.T4C.world.CollisionRules;
import static com.perso.T4C.config.GameConstants.PLAYER_COLLISION_FOOTPRINT_ABOVE_TILES;
import java.io.*;
import java.nio.file.Path;
import java.util.*;

/** Startup-only bounded crop loader. Enabled portal sources are trusted hosted-graph data. */
public final class RegionMapLoader {
  /** Hosting policy: a region that is not declared streamed may not exceed this edge (no AOI to bound its cost). */
  static final int MAX_PLAIN_EDGE=256;
  /**
   * {@code streamed} regions are whole-world regions served through windows and a shared grid
   * (WORLD_STREAMING_DESIGN.md); only they may exceed {@link #MAX_PLAIN_EDGE}, up to the model bound.
   */
  public record Spec(int world,String key,int originX,int originY,int width,int height,int spawnX,int spawnY,boolean streamed) {
    public Spec(int world,String key,int originX,int originY,int width,int height,int spawnX,int spawnY) {
      this(world,key,originX,originY,width,height,spawnX,spawnY,false);
    }
    public Spec {
      new com.perso.T4C.online.model.OnlineRegion(world,key,originX,originY,width,height,0);
      if(!streamed && (width>MAX_PLAIN_EDGE || height>MAX_PLAIN_EDGE))
        throw new IllegalArgumentException("Region exceeds "+MAX_PLAIN_EDGE+" tiles and is not streamed");
      if(spawnX<originX || spawnY<originY || spawnX>=(long)originX+width || spawnY>=(long)originY+height)
        throw new IllegalArgumentException("Spawn outside region");
    }
    boolean contains(int world,double x,double y) {
      return world==this.world && Double.isFinite(x) && Double.isFinite(y) && x>=originX && y>=originY
          && x<(long)originX+width && y<(long)originY+height;
    }
  }
  /** A streamed region is a window over the world's shared grid; no per-region copy is made. */
  static PreviewMap loadStreamed(SharedCollision shared,Spec region) throws IOException {
    if(!region.streamed() || region.world()!=shared.world()) throw new IOException("Not a streamed region of this world");
    if((long)region.originX()+region.width()>shared.width() || (long)region.originY()+region.height()>shared.height())
      throw new IOException("Region outside collision map");
    return new PreviewMap(shared,region.originX(),region.originY(),region.width(),region.height(),
        region.spawnX()-region.originX(),region.spawnY()-region.originY());
  }
  public static PreviewMap load(Path path,Spec region,List<PortalContent.Portal> enabledPortals) throws IOException {
    if(region.streamed()) throw new IOException("A streamed region loads through SharedCollision");
    try(var in=new DataInputStream(BinaryIOUtils.openInputStream(path.toFile(),1<<20))) {
      int width=BinaryIOUtils.readIntLE(in),height=BinaryIOUtils.readIntLE(in);long size=(long)width*height;
      if(width<=0 || height<=0 || width>32768 || height>32768 || size>64L*1024*1024)
        throw new IOException("Collision map exceeds supported dimensions");
      if((long)region.originX()+region.width()>width || (long)region.originY()+region.height()>height)
        throw new IOException("Region outside collision map");
      var data=in.readNBytes((int)size);if(data.length!=size) throw new IOException("Truncated collision map");
      var blocked=new boolean[region.height()][region.width()];var terrain=new byte[region.height()][region.width()];
      var foot=new boolean[region.height()][region.width()];var passable=new boolean[region.height()][region.width()];
      for(int y=0;y<region.height();y++) for(int x=0;x<region.width();x++) {
        int absoluteX=region.originX()+x,absoluteY=region.originY()+y;
        terrain[y][x]=data[absoluteY*width+absoluteX];
        foot[y][x]=CollisionRules.blocksMovement(terrain[y][x]&255);
        for(int above=0;above<=PLAYER_COLLISION_FOOTPRINT_ABOVE_TILES;above++)
          blocked[y][x]|=absoluteY-above<0 || CollisionRules.blocksMovement(data[(absoluteY-above)*width+absoluteX]&255);
      }
      // Offline permits players on authored portal sources, while sight keeps original terrain.
      for(var portal:enabledPortals) if(region.contains(portal.sourceWorld(),portal.sourceX(),portal.sourceY()))
        { blocked[portal.sourceY()-region.originY()][portal.sourceX()-region.originX()]=false;
          passable[portal.sourceY()-region.originY()][portal.sourceX()-region.originX()]=true; }
      return new PreviewMap(blocked,terrain,foot,passable,region.originX(),region.originY(),region.spawnX()-region.originX(),region.spawnY()-region.originY());
    }
  }
}
