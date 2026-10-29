package com.perso.T4C.server;
import com.perso.T4C.online.OnlineConnection;
import com.perso.T4C.server.persistence.OnlineStore;
import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.world.MovementRules;
import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;
import java.net.URI;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BooleanSupplier;
public class NativeTransferProbe {
 static final String WALK_TIMEOUT_PROPERTY="t4c.nativeTransfer.walkTimeoutSeconds";
 static final int DEFAULT_WALK_TIMEOUT_SECONDS=90,MIN_WALK_TIMEOUT_SECONDS=15,MAX_WALK_TIMEOUT_SECONDS=90;
 static void waitFor(BooleanSupplier condition,int seconds)throws Exception{long end=System.nanoTime()+TimeUnit.SECONDS.toNanos(seconds);while(!condition.getAsBoolean()&&System.nanoTime()<end)Thread.sleep(30);if(!condition.getAsBoolean())throw new AssertionError("Native state timed out");}
 static OnlineConnection.View view(OnlineConnection c){var v=c.view();if(v==null)throw new AssertionError("Unexpected native disconnect");return v;}
 static OnlineConnection.Actor own(OnlineConnection c){var v=view(c);return v.snapshot().players().stream().filter(a->a.id()==v.ownId()).findFirst().orElseThrow();}
 static void connected(OnlineConnection c)throws Exception{waitFor(()->c.view()!=null,10);c.confirmRegionReady(view(c).map().locationEpoch());}
 /** Tile a glide is arriving on (server finishes a claimed tile); mirrors HostedTransferTest.walkPortal. */
 static int arrivingTile(double c,int last){int t=(int)Math.round(c-.5);if(Math.abs(c-Math.floor(c)-.5)>1e-6)t=last>0?(int)Math.ceil(c-.5):last<0?(int)Math.floor(c-.5):t;return t;}
 static boolean atTileCenter(double c){return Math.abs(c-(Math.floor(c)+.5))<=1e-6;}
 static boolean movementLeftReservation(double oldX,double oldY,double newX,double newY,int reservedX,int reservedY){
   double dx=newX-oldX,dy=newY-oldY,epsilon=1e-6;
   if(Math.abs(dx)<=epsilon&&Math.abs(dy)<=epsilon)return false;
   if((reservedX==0&&Math.abs(dx)>epsilon)||(reservedY==0&&Math.abs(dy)>epsilon))return true;
   if((reservedX!=0&&dx*reservedX< -epsilon)||(reservedY!=0&&dy*reservedY< -epsilon))return true;
   return reservedX!=0&&reservedY!=0&&Math.abs(Math.abs(dx)-Math.abs(dy))>.05;
 }
 record RouteStep(int inputX,int inputY,int moveX,int moveY,int nextX,int nextY) {}
 static RouteStep routeStep(PreviewMap map,int startX,int startY,int goalX,int goalY){
   if(startX<0||startY<0||startX>=map.width||startY>=map.height||goalX<0||goalY<0||goalX>=map.width||goalY>=map.height)return null;
   int size=map.width*map.height,start=startY*map.width+startX,goal=goalY*map.width+goalX;
   int[] previous=new int[size],inputX=new int[size],inputY=new int[size],moveX=new int[size],moveY=new int[size];
   Arrays.fill(previous,-1);previous[start]=start;var queue=new ArrayDeque<Integer>();queue.add(start);
   var terrain=new ServerMovement.Terrain(map);
   float pixelWidth=map.width*(float)GRID_W,pixelHeight=map.height*(float)GRID_H;
   int[][] directions={{1,0},{-1,0},{0,1},{0,-1}};
   while(!queue.isEmpty()&&previous[goal]<0){
     int node=queue.remove(),x=node%map.width,y=node/map.width;
     for(int[] direction:directions){
       var reservation=MovementRules.Grid.reserve(terrain,x*GRID_W,y*GRID_H,direction[0],direction[1],true,pixelWidth,pixelHeight);
       if(reservation==null)continue;
       int nx=(int)Math.round(ServerMovement.tileX(reservation.targetX())-.5);
       int ny=(int)Math.round(ServerMovement.tileY(reservation.targetY())-.5);
       if(nx<0||ny<0||nx>=map.width||ny>=map.height)continue;
       int next=ny*map.width+nx;if(next==node||previous[next]>=0)continue;
       previous[next]=node;inputX[next]=direction[0];inputY[next]=direction[1];
       moveX[next]=reservation.moveX();moveY[next]=reservation.moveY();queue.add(next);
     }
   }
   if(previous[goal]<0||start==goal)return null;
   int first=goal;while(previous[first]!=start)first=previous[first];
   return new RouteStep(inputX[first],inputY[first],moveX[first],moveY[first],first%map.width,first/map.width);
 }
 static int parseWalkTimeoutSeconds(String value){
   if(value==null)return DEFAULT_WALK_TIMEOUT_SECONDS;
   final int seconds;
   try{seconds=Integer.parseInt(value);}
   catch(NumberFormatException invalid){throw new IllegalArgumentException(WALK_TIMEOUT_PROPERTY+" must be an integer from 15 to 90",invalid);}
   if(seconds<MIN_WALK_TIMEOUT_SECONDS||seconds>MAX_WALK_TIMEOUT_SECONDS)
     throw new IllegalArgumentException(WALK_TIMEOUT_PROPERTY+" must be from 15 to 90 seconds");
   return seconds;
 }
 static void verifyWalkTimeoutBounds(){
   if(parseWalkTimeoutSeconds(null)!=90||parseWalkTimeoutSeconds("15")!=15
       ||parseWalkTimeoutSeconds("25")!=25||parseWalkTimeoutSeconds("90")!=90)
     throw new AssertionError("Transfer walk timeout accepted an incorrect default or boundary");
   for(String invalid:new String[]{"14","91","bad"}){
     try{parseWalkTimeoutSeconds(invalid);throw new AssertionError("Invalid walk timeout accepted: "+invalid);}
     catch(IllegalArgumentException expected){ }
   }
 }
 record WalkDecision(double x,double y,int startX,int startY,int goalX,int goalY,int nextX,int nextY,
     int requestedX,int requestedY,int reservedX,int reservedY,int previousInputX,int previousInputY,
     int previousReservedX,int previousReservedY,String action) {
   String format(){return String.format(Locale.ROOT,
       "pos=(%.3f,%.3f) start=(%d,%d) goal=(%d,%d) next=(%d,%d) requested=(%d,%d) reserved=(%d,%d) previousInput=(%d,%d) previousReserved=(%d,%d) action=%s",
       x,y,startX,startY,goalX,goalY,nextX,nextY,requestedX,requestedY,reservedX,reservedY,
       previousInputX,previousInputY,previousReservedX,previousReservedY,action);}
 }
 static final class WalkDiagnostics {
   static final int CAPACITY=32;
   private final WalkDecision[] entries=new WalkDecision[CAPACITY];
   private int next,size;
   void add(WalkDecision decision){entries[next]=decision;next=(next+1)%CAPACITY;if(size<CAPACITY)size++;}
   int size(){return size;}
   String dump(){
     var lines=new ArrayList<String>(size);
     int first=(next-size+CAPACITY)%CAPACITY;
     for(int i=0;i<size;i++) lines.add(entries[(first+i)%CAPACITY].format());
     return String.join(System.lineSeparator(),lines);
   }
 }
 static void verifyDiagnosticsBuffer(){
   var diagnostics=new WalkDiagnostics();
   for(int i=0;i<=WalkDiagnostics.CAPACITY;i++)
     diagnostics.add(new WalkDecision(i+.25,i+.5,i,i+1,100,101,i+2,i+3,1,0,0,1,0,1,1,0,"send_direction"));
   String dump=diagnostics.dump();
   if(diagnostics.size()!=32||dump.contains("pos=(0.250,0.500)")
       ||!dump.contains("pos=(32.250,32.500)")||!dump.contains("requested=(1,0) reserved=(0,1) previousInput=(0,1) previousReserved=(1,0) action=send_direction"))
     throw new AssertionError("Transfer walker diagnostic ring failed its bounded-format self-check");
 }
 static void verifyPortalTarget(HostedRegionPlan plan,PreviewMap map,int portalId,int world,int sourceX,int sourceY){
   int localX=sourceX-map.originX,localY=sourceY-map.originY;
   if(localX<0||localY<0||localX>=map.width||localY>=map.height||!map.canStand(localX+.5,localY+.5))
     throw new AssertionError("Authored transfer portal target is outside the walkable hosted map");
   var transfer=plan.portals.resolve(world,sourceX+.5,sourceY+.5,plan::canStand)
       .orElseThrow(()->new AssertionError("Authored transfer portal source is not enabled"));
   if(transfer.portalId()!=portalId)throw new AssertionError("Transfer target resolves to portal "+transfer.portalId()+", expected "+portalId);
 }
 static void verifyAuthoredTargets(HostedRegionPlan plan){
   var start=plan.require(0,2944,1059).map();
   verifyPortalTarget(plan,start,16,0,2931,1056);
   var dungeon=plan.require(1,304,383).map();
   verifyPortalTarget(plan,dungeon,17,1,306,381);
 }
 static void verifyMovementRoutes(HostedRegionPlan plan){
   var start=plan.require(0,2944,1059).map();
   if(routeStep(start,(int)start.spawnX,(int)start.spawnY,2931-start.originX,1056-start.originY)==null)
     throw new AssertionError("Entrance16 is unreachable under grid-reservation movement");
   var dungeon=plan.require(1,304,383).map();
   if(routeStep(dungeon,(int)dungeon.spawnX,(int)dungeon.spawnY,306-dungeon.originX,381-dungeon.originY)==null)
     throw new AssertionError("Return17 is unreachable under grid-reservation movement");
   boolean[][] blocked=new boolean[10][10];blocked[4][5]=true;
   var slideMap=new PreviewMap(blocked,0,0,5,5);
   var slide=routeStep(slideMap,5,5,4,4);
   if(slide==null||slide.inputX()!=0||slide.inputY()!=-1||slide.moveX()!=-1||slide.moveY()!=-1
       ||slide.nextX()!=4||slide.nextY()!=4)
     throw new AssertionError("Blocked north input was not represented as its northwest grid reservation");
 }
 static void walk(OnlineConnection c,PreviewMap map,int goalX,int goalY,int destination,int timeoutSeconds)throws Exception{
   long end=System.nanoTime()+TimeUnit.SECONDS.toNanos(timeoutSeconds);int goal=goalY*map.width+goalX;
   int inputX=0,inputY=0,reservedX=0,reservedY=0,pendingX=0,pendingY=0;boolean pending=false;
   double previousActorX=Double.NaN,previousActorY=Double.NaN;
   var diagnostics=new WalkDiagnostics();
   while(System.nanoTime()<end){
     var v=view(c);if(v.map().world()==destination){c.stopInput();return;}
     c.confirmRegionReady(v.map().locationEpoch());var a=own(c);
     boolean centered=atTileCenter(a.x())&&atTileCenter(a.y());
     if(centered){if(pending){reservedX=pendingX;reservedY=pendingY;}pending=false;}
     else if(pending&&!Double.isNaN(previousActorX)
         &&movementLeftReservation(previousActorX,previousActorY,a.x(),a.y(),reservedX,reservedY)){
       reservedX=pendingX;reservedY=pendingY;pending=false;
     }
     int startX=arrivingTile(a.x(),reservedX),startY=arrivingTile(a.y(),reservedY),start=startY*map.width+startX;
     if(start==goal){
       c.stopInput();
       int previousInputX=inputX,previousInputY=inputY,previousReservedX=reservedX,previousReservedY=reservedY;
       inputX=inputY=0;pendingX=pendingY=0;pending=true;
       diagnostics.add(new WalkDecision(a.x(),a.y(),startX,startY,goalX,goalY,-1,-1,0,0,reservedX,reservedY,
           previousInputX,previousInputY,previousReservedX,previousReservedY,"stop_at_goal"));
       if(centered){reservedX=reservedY=0;pending=false;}
       previousActorX=a.x();previousActorY=a.y();
       Thread.sleep(30);continue;
     }
     RouteStep step=routeStep(map,startX,startY,goalX,goalY);
     if(step==null)throw new AssertionError("Authored portal is unreachable under grid-reservation movement");
     int previousInputX=inputX,previousInputY=inputY,previousReservedX=reservedX,previousReservedY=reservedY;
     c.direction(step.inputX(),step.inputY());inputX=step.inputX();inputY=step.inputY();
     if(centered){reservedX=step.moveX();reservedY=step.moveY();pending=false;}
     else {pendingX=step.moveX();pendingY=step.moveY();pending=true;}
     diagnostics.add(new WalkDecision(a.x(),a.y(),startX,startY,goalX,goalY,step.nextX(),step.nextY(),
         step.inputX(),step.inputY(),step.moveX(),step.moveY(),previousInputX,previousInputY,
         previousReservedX,previousReservedY,centered?"send_direction":"queue_direction"));
     previousActorX=a.x();previousActorY=a.y();
     Thread.sleep(25);
   }
   throw new AssertionError("Native portal traversal timed out at "+own(c).x()+","+own(c).y()
       +System.lineSeparator()+"Last walker decisions:"+System.lineSeparator()+diagnostics.dump());
 }
 public static void main(String[]args)throws Exception{
   boolean preflight=args.length>0&&"--preflight".equals(args[0]);
   if((preflight&&args.length>2)||(!preflight&&args.length>2))
     throw new IllegalArgumentException("Expected optional endpoint and collision file, or --preflight and optional collision file");
   int walkTimeoutSeconds=parseWalkTimeoutSeconds(System.getProperty(WALK_TIMEOUT_PROPERTY));
   verifyDiagnosticsBuffer();
   verifyWalkTimeoutBounds();
   var endpoint=preflight?null:com.perso.T4C.online.OnlineEndpoint.validate(URI.create(args.length>0?args[0]:"http://127.0.0.1:18765"));
   var collision=Path.of(args.length>1?args[1]:"assets/maps/worldmap/worldmap.colbin");
   var plan=HostedRegionPlan.startingSlice(collision);
   verifyAuthoredTargets(plan);
   verifyMovementRoutes(plan);
   if(preflight){System.out.println("PASS authored entrance16/return17 routes and grid-reservation movement preflight.");return;}
   var store=OnlineStore.fromEnvironment();store.migrate();var a=store.provisionAccount();var b=store.provisionAccount();
   var kit=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"female",false,new Random(194));
   var first=store.create(a.accountId(),"Native world transfer",0,2944.5,1059.5,kit.progress(),kit.gold());
   var second=store.create(b.accountId(),"Native world observer",0,2944.5,1059.5,kit.progress(),kit.gold());
   var credentials=new OnlineConnection.Credentials(a.secret(),first.id());
   var player=new OnlineConnection(endpoint,credentials);
   try(var observer=new OnlineConnection(endpoint,new OnlineConnection.Credentials(b.secret(),second.id()))){
     connected(player);connected(observer);var initialId=view(player).ownId();
     System.out.println("Two actual native adapters connected; walking authored entrance16 using direction intents.");
     var startingMap=plan.require(0,2944,1059).map();
     walk(player,startingMap,2931-startingMap.originX,1056-startingMap.originY,1,walkTimeoutSeconds);
     if(view(player).ownId()!=initialId)throw new AssertionError("Transfer changed player identity");
     if(view(player).map().locationEpoch()!=1||view(player).character().locationEpoch()!=1)throw new AssertionError("Mixed region state");
     waitFor(()->view(observer).snapshot().players().size()==1,5);
     if(view(observer).map().world()!=0||view(player).snapshot().players().size()!=1)throw new AssertionError("Cross-world actor leak");
     player.confirmRegionReady(1);player.close();Thread.sleep(2500);
     player=new OnlineConnection(endpoint,credentials);connected(player);
     if(view(player).map().world()!=1||view(player).character().locationEpoch()!=1)throw new AssertionError("Reconnect lost destination");
     System.out.println("Entrance, map/private epoch agreement, observer isolation and dungeon reconnect passed.");
     var dungeonMap=plan.require(1,304,383).map();
     walk(player,dungeonMap,306-dungeonMap.originX,381-dungeonMap.originY,0,walkTimeoutSeconds);player.confirmRegionReady(2);
     waitFor(()->view(observer).snapshot().players().size()==2,5);
     var saved=store.load(a.accountId(),first.id());
     if(saved.world()!=0||saved.locationEpoch()!=2||!saved.progress().equals(kit.progress())||saved.gold()!=kit.gold())throw new AssertionError("Return transfer changed progress");
     System.out.println("PASS authored return17; durable epoch2/world0; inventory/stats/gold unchanged. Rendered map switching remains unverified.");
   }finally{player.close();}
 }
}
