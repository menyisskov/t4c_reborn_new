package com.perso.T4C.profiler;

import jdk.jfr.Category;
import jdk.jfr.Description;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.StackTrace;

@Name("T4C.Frame")
@Label("Game Frame")
@Description("One full render frame")
@Category("T4C")
@StackTrace(false)
public class FrameEvent extends Event {

    @Label("Frame Index")
    public long frameIndex;

    @Label("FPS (libGDX)")
    public int fps;

    @Label("Tile X")
    public int tileX;

    @Label("Tile Y")
    public int tileY;

    @Label("Z Level")
    public int z;

    @Label("composedCache size")
    public int composedCacheSize;
}
