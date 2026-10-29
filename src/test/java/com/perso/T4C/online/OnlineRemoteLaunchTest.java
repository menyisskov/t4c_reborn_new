package com.perso.T4C.online;

import static org.junit.jupiter.api.Assertions.*;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.OnlineCharacterView;
import com.perso.T4C.online.model.OnlineProtocol;
import com.perso.T4C.online.model.OnlineRegion;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OnlineRemoteLaunchTest {
  @Test void lobbyHandoffRequiresPersistentCharacterAndMatchingLoadedRegionSnapshot() {
    var id=UUID.randomUUID();
    var progress=new CharacterProgress(1,"warrior","male",
        new CharacterProgress.Attributes(10,10,10,10,10),new CharacterProgress.Vitals(100,100,0,0),
        new CharacterProgress.Advancement(1,0,0,0,0),List.of(),Map.of(),Map.of(),Set.of(),Map.of(),
        List.of(),0,null,List.of(),Map.of(),0,null);
    var character=new OnlineCharacterView(id,"Launch test",0,0,1,progress,3);
    var map=new OnlineConnection.MapInfo(0,0,10,10,true,OnlineProtocol.VERSION,0,0,"test",3);
    var region=map.region();
    var actor=new OnlineConnection.Actor(7,1.5,1.5,0,0,100,100);
    var snapshot=new OnlineConnection.Snapshot(1,List.of(actor),1L,List.of(),region,List.of(),List.of());
    var ready=new OnlineConnection.View(7,map,snapshot,character,List.of());

    assertTrue(OnlineClient.persistentViewReady(ready));
    assertFalse(OnlineClient.persistentViewReady(null));
    assertFalse(OnlineClient.persistentViewReady(new OnlineConnection.View(7,map,
        new OnlineConnection.Snapshot(1,List.of(),1L,List.of(),region,List.of(),List.of()),character,List.of())));
    var wrongRegion=new OnlineRegion(0,"other",0,0,10,10,3);
    assertFalse(OnlineClient.persistentViewReady(new OnlineConnection.View(7,map,
        new OnlineConnection.Snapshot(1,List.of(actor),1L,List.of(),wrongRegion,List.of(),List.of()),character,List.of())));
  }
}
