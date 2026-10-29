package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.online.model.CharacterProgress;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * Authored portal targets and respawn points are whole tile numbers; an arrival on the number itself is a tile corner,
 * from which movement keeps a half-tile offset along a row boundary. Arrivals use the centre of the authored tile.
 */
class ArrivalAtTileCentreTest {
  @Test
  void wholeTileNumbersBecomeTileCentres() {
    var centred=PersistentSessions.arrivalAtTileCentre(new CharacterProgress.Respawn(1,2772,1096));
    assertEquals(1,centred.world());
    assertEquals(2772.5,centred.x());
    assertEquals(1096.5,centred.y());
  }

  @Test
  void anAlreadyCentredOrFractionalPositionStaysInItsOwnTile() {
    var centred=PersistentSessions.arrivalAtTileCentre(new CharacterProgress.Respawn(0,2944.5,1059.5));
    assertEquals(2944.5,centred.x());assertEquals(1059.5,centred.y());
    var fractional=PersistentSessions.arrivalAtTileCentre(new CharacterProgress.Respawn(0,10.9,20.1));
    assertEquals(10.5,fractional.x());assertEquals(20.5,fractional.y());
  }

  @Test
  void everyEnabledPortalTargetIsStandableAtItsCentre() throws Exception {
    var plan=HostedRegionPlan.startingSlice(Path.of("../../assets/maps/worldmap/worldmap.colbin"));
    var portals=PortalCatalog.loadBundled();
    int checked=0;
    for(var diagnostic:portals.audit(com.perso.T4C.server.PortalDestinations.load(java.util.Map.of(
        0,Path.of("../../assets/maps/worldmap/worldmap.colbin"),1,Path.of("../../assets/maps/dungeon/DungeonMap.colbin"))))) {
      var target=diagnostic.destination();
      boolean hosted=plan.canStand(target);
      if(!hosted) continue;
      assertTrue(plan.canStand(PersistentSessions.arrivalAtTileCentre(target)),"portal "+diagnostic.portalId()+" centre must stay standable");
      checked++;
    }
    assertTrue(checked>=12,"all twelve hosted routes are checked");
  }
}
