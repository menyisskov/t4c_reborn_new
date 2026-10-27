package com.perso.T4C.online.model;

import java.util.*;

/** Authored portal order is significant: the offline engine selects the first source match. */
public record PortalContent(int version,List<Portal> portals) {
  public static final int VERSION=1;
  public record Portal(int id,int sourceWorld,int sourceX,int sourceY,int targetWorld,int targetX,int targetY) {
    public Portal {
      if(id<0 || sourceWorld<0 || sourceX<0 || sourceY<0 || targetWorld<0 || targetX<0 || targetY<0)
        throw new IllegalArgumentException("Invalid portal");
    }
  }
  public PortalContent {
    if(version!=VERSION) throw new IllegalArgumentException("Unsupported portal content version");
    portals=List.copyOf(portals);var ids=new HashSet<Integer>();
    for(var portal:portals) if(!ids.add(portal.id())) throw new IllegalArgumentException("Duplicate portal identity");
  }
}
