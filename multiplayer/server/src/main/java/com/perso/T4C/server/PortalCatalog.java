package com.perso.T4C.server;

import com.google.gson.Gson;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.PortalContent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;

/** Lookup uses simulation-owned absolute coordinates. A client never chooses a destination. */
public final class PortalCatalog {
  private record Tile(int world,int x,int y) {}
  public record Transfer(int portalId,CharacterProgress.Respawn source,CharacterProgress.Respawn destination) {}
  public record Diagnostic(int portalId,boolean shadowed,CharacterProgress.Respawn destination,PortalDestinations.Inspection inspection) {}
  private final PortalContent content;
  private final Map<Tile,PortalContent.Portal> sources;
  public PortalCatalog(PortalContent content) {
    this.content=content;
    var indexed=new HashMap<Tile,PortalContent.Portal>();
    for(var p:content.portals()) indexed.putIfAbsent(new Tile(p.sourceWorld(),p.sourceX(),p.sourceY()),p);
    sources=Map.copyOf(indexed);
  }
  public static PortalCatalog loadBundled() throws IOException {
    try(var in=PortalCatalog.class.getResourceAsStream("/content/portals-v1.json")) {
      if(in==null) throw new IOException("Missing bundled portal content");return read(in);
    }
  }
  static PortalCatalog read(InputStream in) throws IOException {
    int limit=4*1024*1024;byte[] bytes=in.readNBytes(limit+1);
    if(bytes.length>limit) throw new IOException("Portal content exceeds size limit");
    try { return new PortalCatalog(new Gson().fromJson(new String(bytes,StandardCharsets.UTF_8),PortalContent.class)); }
    catch(RuntimeException invalid) { throw new IOException("Invalid portal content",invalid); }
  }
  public List<Diagnostic> audit(PortalDestinations destinations) {
    return content.portals().stream().map(p -> {
      var destination=new CharacterProgress.Respawn(p.targetWorld(),p.targetX(),p.targetY());
      boolean shadowed=sources.get(new Tile(p.sourceWorld(),p.sourceX(),p.sourceY())).id()!=p.id();
      return new Diagnostic(p.id(),shadowed,destination,destinations.inspect(destination));
    }).toList();
  }
  /** Missing/unloaded/blocked targets fail closed; loading regions belongs to the world manager. */
  public Optional<Transfer> resolve(int world,double x,double y,Predicate<CharacterProgress.Respawn> destinationAvailable) {
    Objects.requireNonNull(destinationAvailable);
    if(world<0 || !Double.isFinite(x) || !Double.isFinite(y) || x<0 || y<0 || x>=Integer.MAX_VALUE || y>=Integer.MAX_VALUE)
      return Optional.empty();
    var p=sources.get(new Tile(world,(int)x,(int)y));if(p==null) return Optional.empty();
    var destination=new CharacterProgress.Respawn(p.targetWorld(),p.targetX(),p.targetY());
    if(!destinationAvailable.test(destination)) return Optional.empty();
    return Optional.of(new Transfer(p.id(),new CharacterProgress.Respawn(world,x,y),destination));
  }
}
