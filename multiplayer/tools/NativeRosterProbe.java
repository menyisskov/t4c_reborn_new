package com.perso.T4C.server;

import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.online.OnlineAccountClient;
import com.perso.T4C.online.OnlineConnection;
import com.perso.T4C.online.OnlineEndpoint;
import com.perso.T4C.server.persistence.OnlineStore;
import java.net.URI;
import java.util.*;
import java.util.concurrent.TimeUnit;

/** Two-account discovery and selected-character joins through actual native network adapters. */
public final class NativeRosterProbe {
  private static void connected(OnlineConnection connection,UUID expected) throws Exception {
    long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(10);
    while(connection.view()==null && System.nanoTime()<deadline) Thread.sleep(25);
    var view=connection.view();
    if(view==null || !expected.equals(view.character().id())) throw new AssertionError("Selected character did not connect");
    connection.confirmRegionReady(view.map().locationEpoch());
  }
  public static void main(String[] args) throws Exception {
    if(args.length>1) throw new IllegalArgumentException("Expected optional endpoint");
    var endpoint=OnlineEndpoint.validate(URI.create(args.length==0 ? "http://127.0.0.1:18765" : args[0]));
    var store=OnlineStore.fromEnvironment();store.migrate();
    var owner=store.provisionAccount();var other=store.provisionAccount();
    var kit=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(196));
    var a=store.create(owner.accountId(),"Roster first",0,2944.5,1059.5,kit.progress(),kit.gold());
    var b=store.create(owner.accountId(),"Roster second",0,2944.5,1059.5,kit.progress(),kit.gold());
    var foreign=store.create(other.accountId(),"Roster observer",0,2944.5,1059.5,kit.progress(),kit.gold());
    try(var account=new OnlineAccountClient(endpoint,owner.secret());var witness=new OnlineAccountClient(endpoint,other.secret())) {
      var page=account.page(null);var otherPage=witness.page(null);
      if(!new HashSet<>(page.characters().stream().map(c->c.id()).toList()).equals(Set.of(a.id(),b.id()))
          || page.nextAfter()!=null || otherPage.characters().size()!=1 || !otherPage.characters().getFirst().id().equals(foreign.id()))
        throw new AssertionError("Account discovery leaked or omitted an owned character");
      try(var selected=new OnlineConnection(endpoint,new OnlineConnection.Credentials(owner.secret(),a.id()));
          var observer=new OnlineConnection(endpoint,new OnlineConnection.Credentials(other.secret(),foreign.id()))) {
        connected(selected,a.id());connected(observer,foreign.id());
        var active=account.page(null);
        if(active.characters().stream().noneMatch(c->c.id().equals(a.id()) && c.availability().equals("in_use")))
          throw new AssertionError("Roster did not reflect the active selected character lease");
      }
      Thread.sleep(2500); // bounded asynchronous logout; never force lease takeover
      try(var selected=new OnlineConnection(endpoint,new OnlineConnection.Credentials(owner.secret(),b.id()))) {
        connected(selected,b.id());
        if(!store.load(owner.accountId(),a.id()).progress().equals(kit.progress())
            || !store.load(owner.accountId(),b.id()).progress().equals(kit.progress()))
          throw new AssertionError("Roster selection changed character progress");
      }
    }
    System.out.println("PASS actual native account adapters: owner-only roster, active lease summary, two selected character joins and unchanged progress. No rendered selector claim.");
  }
}
