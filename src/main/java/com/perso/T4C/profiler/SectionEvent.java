package com.perso.T4C.profiler;

import jdk.jfr.Category;
import jdk.jfr.Description;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.StackTrace;

@Name("T4C.Section")
@Label("Frame Section")
@Description("One named section within a render frame (ground, entities, hud…)")
@Category("T4C")
@StackTrace(false)
public class SectionEvent extends Event {

    @Label("Section Name")
    public String section;

    @Label("Frame Index")
    public long frameIndex;
}
