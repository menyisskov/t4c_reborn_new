package com.perso.T4C.screens;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.online.OnlineCorpsePanel;
import com.perso.T4C.session.RemoteIntents;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class MainGameScreenRemoteCorpseTest {
  @Test
  void selectedCorpseItemRoutesThroughRemoteIntentWithoutLocalPlayerOrSaveAccess() {
    var corpse = UUID.randomUUID();
    var item = UUID.randomUUID();
    var sent = new AtomicReference<String>();
    var remote = (RemoteIntents) java.lang.reflect.Proxy.newProxyInstance(
        RemoteIntents.class.getClassLoader(), new Class<?>[] {RemoteIntents.class},
        (proxy, method, args) -> {
          if (method.getName().equals("recover") && args != null && args.length == 2) {
            sent.set(args[0] + " " + args[1]);
            return true;
          }
          if (method.getReturnType() == boolean.class) return false;
          return null;
        });

    assertTrue(MainGameScreen.submitRemoteCorpseChoice(remote,
        new OnlineCorpsePanel.Action(corpse, item)));
    assertEquals(corpse + " " + item, sent.get());
  }
}
