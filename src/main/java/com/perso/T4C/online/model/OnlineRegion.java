package com.perso.T4C.online.model;

/** Immutable region/position generation; never mix actors or input from different epochs. */
public record OnlineRegion(int world,String key,int originX,int originY,int width,int height,long locationEpoch) {
  public OnlineRegion {
    if(world<0 || key==null || key.isBlank() || key.length()>128 || originX<0 || originY<0
        || width<1 || height<1 || width>256 || height>256 || locationEpoch<0)
      throw new IllegalArgumentException("Invalid online region");
  }
}
