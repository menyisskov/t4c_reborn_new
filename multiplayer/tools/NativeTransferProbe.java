package com.perso.T4C.server;
import com.perso.T4C.online.OnlineConnection;
import com.perso.T4C.server.persistence.OnlineStore;
import com.perso.T4C.helper.CharacterClass;
import java.net.URI;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BooleanSupplier;
public class NativeTransferProbe {
 static void waitFor(BooleanSupplier condition,int seconds)throws Exception{long end=System.nanoTime()+TimeUnit.SECONDS.toNanos(seconds);while(!condition.getAsBoolean()&&System.nanoTime()<end)Thread.sleep(30);if(!condition.getAsBoolean())throw new AssertionError("Native state timed out");}
 static OnlineConnection.View view(OnlineConnection c){var v=c.view();if(v==null)throw new AssertionError("Unexpected native disconnect");return v;}
 static OnlineConnection.Actor own(OnlineConnection c){var v=view(c);return v.snapshot().players().stream().filter(a->a.id()==v.ownId()).findFirst().orElseThrow();}
 static void connected(OnlineConnection c)throws Exception{waitFor(()->c.view()!=null,10);c.confirmRegionReady(view(c).map().locationEpoch());}
 static void walk(OnlineConnection c,PreviewMap map,int goalX,int goalY,int destination)throws Exception{
   long end=System.nanoTime()+TimeUnit.SECONDS.toNanos(90);int goal=goalY*map.width+goalX;
   while(System.nanoTime()<end){
     var v=view(c);if(v.map().world()==destination){c.stopInput();return;}
     c.confirmRegionReady(v.map().locationEpoch());var a=own(c);int start=(int)a.y()*map.width+(int)a.x();
     if(start==goal){c.stopInput();Thread.sleep(30);continue;}
     int[] prev=new int[map.width*map.height];Arrays.fill(prev,-1);prev[start]=start;var q=new ArrayDeque<Integer>();q.add(start);
     while(!q.isEmpty()&&prev[goal]<0){int node=q.remove(),x=node%map.width,y=node/map.width;for(var d:new int[][]{{1,0},{-1,0},{0,1},{0,-1}}){int nx=x+d[0],ny=y+d[1];if(!map.canStand(nx+.5,ny+.5))continue;int next=ny*map.width+nx;if(prev[next]>=0)continue;prev[next]=node;q.add(next);}}
     if(prev[goal]<0)throw new AssertionError("Authored portal is unreachable");
     int next=goal;while(prev[next]!=start)next=prev[next];
     c.direction(Integer.compare(next%map.width,start%map.width),Integer.compare(next/map.width,start/map.width));Thread.sleep(25);
   }
   throw new AssertionError("Native portal traversal timed out at "+own(c).x()+","+own(c).y());
 }
 public static void main(String[]args)throws Exception{
   if(args.length>2)throw new IllegalArgumentException("Expected optional endpoint and collision file");
   var endpoint=com.perso.T4C.online.OnlineEndpoint.validate(URI.create(args.length>0?args[0]:"http://127.0.0.1:18765"));
   var collision=Path.of(args.length>1?args[1]:"assets/maps/worldmap/worldmap.colbin");
   var store=OnlineStore.fromEnvironment();store.migrate();var a=store.provisionAccount();var b=store.provisionAccount();
   var kit=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"female",false,new Random(194));
   var first=store.create(a.accountId(),"Native world transfer",0,2944.5,1059.5,kit.progress(),kit.gold());
   var second=store.create(b.accountId(),"Native world observer",0,2944.5,1059.5,kit.progress(),kit.gold());
   var credentials=new OnlineConnection.Credentials(a.secret(),first.id());
   var player=new OnlineConnection(endpoint,credentials);
   try(var observer=new OnlineConnection(endpoint,new OnlineConnection.Credentials(b.secret(),second.id()))){
     connected(player);connected(observer);var initialId=view(player).ownId();
     var plan=HostedRegionPlan.startingSlice(collision);
     System.out.println("Two actual native adapters connected; walking authored entrance16 using direction intents.");
     walk(player,plan.require(0,2944,1059).map(),27,27,1);
     if(view(player).ownId()!=initialId)throw new AssertionError("Transfer changed player identity");
     if(view(player).map().locationEpoch()!=1||view(player).character().locationEpoch()!=1)throw new AssertionError("Mixed region state");
     waitFor(()->view(observer).snapshot().players().size()==1,5);
     if(view(observer).map().world()!=0||view(player).snapshot().players().size()!=1)throw new AssertionError("Cross-world actor leak");
     player.confirmRegionReady(1);player.close();Thread.sleep(2500);
     player=new OnlineConnection(endpoint,credentials);connected(player);
     if(view(player).map().world()!=1||view(player).character().locationEpoch()!=1)throw new AssertionError("Reconnect lost destination");
     System.out.println("Entrance, map/private epoch agreement, observer isolation and dungeon reconnect passed.");
     walk(player,plan.require(1,304,383).map(),42,28,0);player.confirmRegionReady(2);
     waitFor(()->view(observer).snapshot().players().size()==2,5);
     var saved=store.load(a.accountId(),first.id());
     if(saved.world()!=0||saved.locationEpoch()!=2||!saved.progress().equals(kit.progress())||saved.gold()!=kit.gold())throw new AssertionError("Return transfer changed progress");
     System.out.println("PASS authored return17; durable epoch2/world0; inventory/stats/gold unchanged. Rendered map switching remains unverified.");
   }finally{player.close();}
 }
}
