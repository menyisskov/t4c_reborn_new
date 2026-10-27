package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;
import com.perso.T4C.online.model.PortalContent;
import java.util.List;
import org.junit.jupiter.api.Test;

class PortalCatalogTest {
  @Test void authoritativeSourceAndLoadedDestinationSelectOnlyTheAuthoredFirstMatch() {
    var first=new PortalContent.Portal(7,0,10,20,1,30,40);
    var second=new PortalContent.Portal(8,0,10,20,2,50,60);
    var catalog=new PortalCatalog(new PortalContent(1,List.of(first,second)));
    var transfer=catalog.resolve(0,10.8,20.2,p->p.world()==1).orElseThrow();
    assertEquals(7,transfer.portalId());assertEquals(1,transfer.destination().world());
    assertEquals(30,transfer.destination().x());assertEquals(40,transfer.destination().y());
    assertTrue(catalog.resolve(1,10.8,20.2,p->true).isEmpty());
    assertTrue(catalog.resolve(0,11,20,p->true).isEmpty());
    assertTrue(catalog.resolve(0,10,20,p->false).isEmpty());
    assertTrue(catalog.resolve(0,Double.NaN,20,p->true).isEmpty());
  }
  @Test void collisionInspectionRejectsBlockedFootprintUnknownWorldAndOutsideDestination(@org.junit.jupiter.api.io.TempDir java.nio.file.Path temp) throws Exception {
    var path=temp.resolve("map.colbin");
    var buffer=java.nio.ByteBuffer.allocate(8+16).order(java.nio.ByteOrder.LITTLE_ENDIAN);
    buffer.putInt(4).putInt(4);buffer.put(new byte[16]);buffer.put(8+1*4+2,(byte)1);
    java.nio.file.Files.write(path,buffer.array());
    var destinations=PortalDestinations.load(java.util.Map.of(1,path));
    assertTrue(destinations.canStand(new com.perso.T4C.online.model.CharacterProgress.Respawn(1,1,2)));
    var blocked=destinations.inspect(new com.perso.T4C.online.model.CharacterProgress.Respawn(1,2,2));
    assertEquals(PortalDestinations.Status.BLOCKED,blocked.status());assertEquals(1,blocked.collisionCode());
    assertEquals(PortalDestinations.Status.UNRESOLVED_WORLD,destinations.inspect(new com.perso.T4C.online.model.CharacterProgress.Respawn(0,1,2)).status());
    assertEquals(PortalDestinations.Status.OUTSIDE_MAP,destinations.inspect(new com.perso.T4C.online.model.CharacterProgress.Respawn(1,9,2)).status());
  }
  @Test void bundledPortalDataLoadsHeadlesslyAndInvalidIdentityFailsClosed() throws Exception {
    assertNotNull(PortalCatalog.loadBundled());
    var p=new PortalContent.Portal(1,0,1,1,1,2,2);
    assertThrows(IllegalArgumentException.class,()->new PortalContent(1,List.of(p,p)));
    assertThrows(java.io.IOException.class,()->PortalCatalog.read(new java.io.ByteArrayInputStream("{\"version\":99,\"portals\":[]}".getBytes(java.nio.charset.StandardCharsets.UTF_8))));
  }
}
