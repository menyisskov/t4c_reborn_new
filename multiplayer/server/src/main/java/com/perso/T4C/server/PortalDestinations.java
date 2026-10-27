package com.perso.T4C.server;

import com.perso.T4C.config.GameConstants;
import com.perso.T4C.helper.BinaryIOUtils;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.world.CollisionRules;
import java.io.*;
import java.nio.file.Path;
import java.util.*;

/** Collision-only destination validation; the caller separately ensures the region is hosted. */
public final class PortalDestinations {
  public enum Status { AVAILABLE, UNRESOLVED_WORLD, OUTSIDE_MAP, BLOCKED }
  public record Inspection(Status status,int collisionCode) {}
  private record Grid(int width,int height,byte[] cells) {}
  private final Map<Integer,Grid> worlds;
  private PortalDestinations(Map<Integer,Grid> worlds) { this.worlds=Map.copyOf(worlds); }
  public static PortalDestinations load(Map<Integer,Path> paths) throws IOException {
    var grids=new HashMap<Integer,Grid>();
    for(var entry:paths.entrySet()) {
      if(entry.getKey()<0) throw new IllegalArgumentException("Invalid world");
      try(var in=new DataInputStream(BinaryIOUtils.openInputStream(entry.getValue().toFile(),1<<20))) {
        int width=BinaryIOUtils.readIntLE(in),height=BinaryIOUtils.readIntLE(in);long size=(long)width*height;
        if(width<=0 || height<=0 || width>32768 || height>32768 || size>64L*1024*1024)
          throw new IOException("Collision map exceeds supported dimensions");
        var data=in.readNBytes((int)size);if(data.length!=size) throw new IOException("Truncated collision map");
        grids.put(entry.getKey(),new Grid(width,height,data));
      }
    }
    return new PortalDestinations(grids);
  }
  public Inspection inspect(CharacterProgress.Respawn destination) {
    var grid=worlds.get(destination.world());if(grid==null) return new Inspection(Status.UNRESOLVED_WORLD,-1);
    if(destination.x()>=grid.width() || destination.y()>=grid.height()) return new Inspection(Status.OUTSIDE_MAP,-1);
    int x=(int)destination.x(),y=(int)destination.y();
    for(int above=0;above<=GameConstants.PLAYER_COLLISION_FOOTPRINT_ABOVE_TILES;above++) {
      if(y-above<0) return new Inspection(Status.OUTSIDE_MAP,-1);
      int code=grid.cells()[(y-above)*grid.width()+x]&255;
      if(CollisionRules.blocksMovement(code)) return new Inspection(Status.BLOCKED,code);
    }
    return new Inspection(Status.AVAILABLE,grid.cells()[y*grid.width()+x]&255);
  }
  public boolean canStand(CharacterProgress.Respawn destination) { return inspect(destination).status()==Status.AVAILABLE; }
}
