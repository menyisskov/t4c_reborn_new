package com.perso.T4C.online.model;

/**
 * The region-local tile rectangle a streamed snapshot's lists are complete for. Coordinates stay
 * region-local like every other snapshot coordinate; a window is metadata, not a new origin.
 */
public record OnlineWindow(int x,int y,int width,int height) {
  public OnlineWindow {
    if(x<0 || y<0 || width<1 || height<1 || (long)x+width>OnlineRegion.MAX_DIMENSION
        || (long)y+height>OnlineRegion.MAX_DIMENSION) throw new IllegalArgumentException("Invalid online window");
  }
  public boolean contains(double px,double py) {
    return px>=x && py>=y && px<(long)x+width && py<(long)y+height;
  }
  public boolean fitsIn(OnlineRegion region) {
    return (long)x+width<=region.width() && (long)y+height<=region.height();
  }
}
