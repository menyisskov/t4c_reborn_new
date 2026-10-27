package com.perso.T4C.tools;

import com.google.gson.Gson;
import com.perso.T4C.online.model.PortalContent;
import com.perso.T4C.teleport.TeleportRegistry;
import java.nio.file.*;

/** Build-time bridge: the headless server receives authored data, never desktop registries. */
public final class OnlinePortalContentExporter {
  private OnlinePortalContentExporter() {}
  public static PortalContent snapshot() {
    return new PortalContent(PortalContent.VERSION,TeleportRegistry.load().stream().map(p ->
        new PortalContent.Portal(p.id(),p.sourceZ(),p.sourceX(),p.sourceY(),p.targetZ(),p.targetX(),p.targetY())).toList());
  }
  public static String render(PortalContent content) { return new Gson().toJson(content)+"\n"; }
  public static void main(String[] args) throws Exception {
    if(args.length!=1) throw new IllegalArgumentException("Expected output JSON path");
    var path=Path.of(args[0]);if(path.getParent()!=null) Files.createDirectories(path.getParent());
    Files.writeString(path,render(snapshot()));
  }
}
