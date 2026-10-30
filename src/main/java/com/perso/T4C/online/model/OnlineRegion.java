package com.perso.T4C.online.model;

/** Immutable region/position generation; never mix actors or input from different epochs. */
public record OnlineRegion(int world,String key,int originX,int originY,int width,int height,long locationEpoch) {
  /**
   * Largest region edge the model accepts. The server still hosts at most 256 tiles per edge unless
   * a region is declared streamed, and the client still accepts at most 1024 for a map that is not
   * streamed (T4C-0350); this bound only has to fit the largest shipped world (6144 x 3072).
   */
  public static final int MAX_DIMENSION=8192;
  public OnlineRegion {
    if(world<0 || key==null || key.isBlank() || key.length()>128 || originX<0 || originY<0
        || width<1 || height<1 || width>MAX_DIMENSION || height>MAX_DIMENSION || locationEpoch<0)
      throw new IllegalArgumentException("Invalid online region");
  }
}
