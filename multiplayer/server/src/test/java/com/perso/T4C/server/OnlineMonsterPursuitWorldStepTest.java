package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.MonsterPursuitRules;
import com.perso.T4C.online.model.MonsterContent;
import com.perso.T4C.server.persistence.OnlineStore;
import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/**
 * M4 "monsters acquire / pursue / reset" through the server's whole world step ({@link HostedWorlds#step}): the
 * persisted session, the persisted creature lives, the global path-permit queue and the retaliation step together.
 * Single-player is the reference: {@code BaseMonster} chases a provoked player out to the retaliation leash
 * ({@code MONSTER_RETALIATION_LEASH_RANGE}, 60 tiles), notices an unprovoked one inside {@code MONSTER_AGGRO_RANGE}
 * (8 tiles) with line of sight, drops a natural chase beyond {@code MONSTER_AGGRO_LEASH_RANGE} (30 tiles) and walks
 * home without healing.
 */
@EnabledIfEnvironmentVariable(named="T4C_DB_URL",matches=".+")
class OnlineMonsterPursuitWorldStepTest {
  private static final int WIDTH=250,HEIGHT=80,PLAYER_ROW=40;
  private static final double HOME_X=100,HOME_Y=PLAYER_ROW;

  /** One open field, creature 1 at its home, optionally a crowd of ordinary patrollers far from the action. */
  private static final class Field implements AutoCloseable {
    final OnlineStore store;
    final HostedWorlds hosted;
    final HostedWorlds.Region region;
    final MonsterDef skeleton;
    final OnlineStore.AccountKey key;
    long sequence;
    int ticks;
    Field(boolean aggressive,int crowd) throws Exception {
      store=OnlineStore.fromEnvironment();store.migrate();
      skeleton=MonsterCatalog.loadBundled().monsters().get("Skeleton");
      var spawns=new ArrayList<MonsterContent.Spawn>();
      spawns.add(new MonsterContent.Spawn("pursuit-"+UUID.randomUUID(),skeleton.getName(),(int)HOME_X,(int)HOME_Y,0,false,aggressive));
      for(int i=0;i<crowd;i++)
        spawns.add(new MonsterContent.Spawn("crowd-"+UUID.randomUUID(),skeleton.getName(),4+7*(i%13),4+6*(i/13),0,false,false));
      var content=new MonsterContent(MonsterContent.VERSION,Map.of(skeleton.getName(),skeleton),spawns);
      var map=new PreviewMap(new boolean[HEIGHT][WIDTH],0,0,150,PLAYER_ROW);
      hosted=new HostedWorlds(HostedRegionPlan.single(map),store,ItemCatalog.loadBundled(),content);
      region=hosted.regions.getFirst();
      key=store.provisionAccount();
    }
    /** A warrior standing at the given tile; returns its session token. */
    String join(String name,double x,double y) throws Exception {
      var prepared=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(1));
      var character=store.create(key.accountId(),name,0,x,y,prepared.progress(),prepared.gold());
      return lastToken=hosted.sessions.join(store.acquire(key.secret(),character.id()),System.nanoTime()).token();
    }
    void tick() throws InterruptedException {
      hosted.step();ticks++;
      if(ticks%100==0) region.world.snapshot(lastToken); // keeps the idle-session timeout from firing
      // Real server pacing: the persistence worker needs the same breathing room it has in production.
      long due=startedNanos+ticks*(1_000_000_000L/PreviewWorld.TICK_RATE),wait=due-System.nanoTime();
      if(wait>0) Thread.sleep(wait/1_000_000L,(int)(wait%1_000_000L));
    }
    private final long startedNanos=System.nanoTime();
    String lastToken;
    boolean run(int maxTicks,BooleanSupplier until) throws InterruptedException {
      for(int i=0;i<maxTicks;i++) { tick();if(until.getAsBoolean()) return true; }
      return until.getAsBoolean();
    }
    void walk(String token,int dx,int dy,int ticks) throws InterruptedException {
      for(int i=0;i<ticks;i++) { region.world.input(token,++sequence,dx,dy);tick(); }
      region.world.input(token,++sequence,0,0);
    }
    double tilesBetween(String token) {
      var c=region.world.creaturePosition(1);var p=region.world.position(token);
      return Math.hypot((p.x()-c.x())*com.perso.T4C.config.GameConstants.GRID_W,
          (p.y()-c.y())*com.perso.T4C.config.GameConstants.GRID_H)/com.perso.T4C.config.GameConstants.GRID_W;
    }
    OnlineMonsterPursuit.Mode mode() {
      try { return OnlineMonsterPursuit.Mode.valueOf(region.creatures.capture(1).mode()); }
      catch(PreviewWorld.Rejected reloading) { return null; }
    }
    @Override public void close() throws Exception {
      try { hosted.close(); } finally { hosted.sessions.shutdown(); }
    }
  }

  /** The player's first confirmed hit: provokes creature 1 exactly as a real click does. */
  private static void provoke(Field f,String token) throws Exception {
    f.lastToken=token;
    long revision=f.hosted.sessions.character(token).revision();
    CompletionStage<?> hit=f.region.melee.attack(token,revision,1);
    var done=hit.toCompletableFuture();
    assertTrue(f.run(1000,done::isDone),"the provoking hit commits");
    done.get();
    assertTrue(f.run(1000,() -> {
      try { return f.region.creatures.life(1).targetCharacter()!=null; }
      catch(PreviewWorld.Rejected reloading) { return false; }
    }),"the provocation is persisted as the creature's target");
  }

  @Test void retaliatingSkeletonFollowsAPlayerWhoWalksAwayEvenAmongManyPatrollers() throws Exception {
    try(var f=new Field(false,120)) {
      String token=f.join("Walker",HOME_X+.5,HOME_Y+.5);
      provoke(f,token);
      var life=f.region.creatures.life(1);
      assertEquals("RETALIATION",life.mode());
      f.walk(token,1,0,160); // about 11 tiles at the player's speed, faster than the creature's
      double gap=f.tilesBetween(token);
      assertTrue(gap>8,"the player is well out of melee reach: "+gap);
      double homeToCreature=Math.hypot(f.region.world.creaturePosition(1).x()-HOME_X,f.region.world.creaturePosition(1).y()-HOME_Y);
      assertTrue(f.run(1500,() -> f.tilesBetween(token)<2.5),
          "the provoked Skeleton must close in on a player standing "+gap+" tiles away (still "+f.tilesBetween(token)+", moved "
              +homeToCreature+" from home); single-player's chase runs out to 60 tiles");
    }
  }

  @Test void unprovokedPlayerInsideAggroRangeIsNoticedAndReachedAndLeavingPastTheLeashResetsWithoutHealing() throws Exception {
    try(var f=new Field(true,0)) {
      // 12 tiles out: beyond MONSTER_AGGRO_RANGE (8 tiles), so nothing happens.
      String token=f.join("Visitor",HOME_X+12.5,HOME_Y+.5);f.lastToken=token;
      assertTrue(MonsterPursuitRules.inNaturalAcquisitionRange(true,(float)(8*32),false));
      assertFalse(MonsterPursuitRules.inNaturalAcquisitionRange(true,(float)(12*32),false));
      f.run(120,() -> false);
      assertNotEquals(OnlineMonsterPursuit.Mode.NATURAL,f.mode(),"a player outside the aggro range is not acquired");
      assertNull(f.region.creatures.life(1).targetCharacter());
      // Walk inside the 8-tile range: the Skeleton notices without being hit.
      for(int i=0;i<400 && f.tilesBetween(token)>7;i++) f.walk(token,-1,0,1);
      assertTrue(f.tilesBetween(token)<=7.2 && f.tilesBetween(token)>4,"inside the range: "+f.tilesBetween(token));
      assertTrue(f.run(300,() -> f.mode()==OnlineMonsterPursuit.Mode.NATURAL),"natural acquisition through the world step");
      var persisted=f.region.creatures;
      assertTrue(f.run(300,() -> {
        try { return persisted.life(1).targetCharacter()!=null; } catch(PreviewWorld.Rejected reloading) { return false; }
      }),"the acquisition is persisted");
      int healthBefore=Optional.ofNullable(persisted.life(1).health()).orElse(f.skeleton.getHealth());
      // Run east: the natural leash is 30 tiles (retaliation's would be 60). The creature is slower than the player.
      f.walk(token,1,0,1100);
      assertTrue(f.tilesBetween(token)>=30,"the player is beyond the natural leash: "+f.tilesBetween(token));
      assertTrue(f.run(400,() -> f.mode()==OnlineMonsterPursuit.Mode.RETURN || f.mode()==OnlineMonsterPursuit.Mode.PATROL),
          "the chase is abandoned beyond the leash");
      assertTrue(f.run(2500,() -> f.mode()==OnlineMonsterPursuit.Mode.PATROL
          && Math.hypot(f.region.world.creaturePosition(1).x()-HOME_X,f.region.world.creaturePosition(1).y()-HOME_Y)<=7),
          "the creature walks home and resumes its patrol around it");
      assertTrue(f.run(300,() -> {
        try { return persisted.life(1).targetCharacter()==null; } catch(PreviewWorld.Rejected reloading) { return false; }
      }),"the reset clears the persisted target");
      assertEquals(healthBefore,Optional.ofNullable(persisted.life(1).health()).orElse(f.skeleton.getHealth()),
          "single-player's return home does not heal");
    }
  }
}
