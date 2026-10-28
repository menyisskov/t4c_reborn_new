package com.perso.T4C.server.persistence;

import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.ItemContent;
import com.perso.T4C.online.model.LootDropDraft;
import com.perso.T4C.online.model.OnlineCharacterCreation;
import com.perso.T4C.server.OnlinePickupRules;

import java.sql.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

/** Server-only PostgreSQL persistence. Call from a bounded persistence worker, never the tick thread. */
public final class OnlineStore implements AutoCloseable {
  private static final com.google.gson.Gson JSON = new com.google.gson.Gson();
  /** Matches the offline three-character account rule without importing desktop save code. */
  private static final int MAX_ACCOUNT_CHARACTERS = 3;
  @FunctionalInterface public interface Connections { Connection open() throws SQLException; }
  public record CharacterState(UUID id, UUID accountId, String name, int world, double x, double y,
      long gold, long experience, long revision, CharacterProgress progress,long locationEpoch,long portalCooldownUntil) {
    public CharacterState(UUID id,UUID accountId,String name,int world,double x,double y,long gold,long experience,long revision,CharacterProgress progress) {
      this(id,accountId,name,world,x,y,gold,experience,revision,progress,0,0);
    }
  }
  public record RewardResult(boolean applied, CharacterState character) {}
  /** Proposed ground-loot policy (LOOT_DESIGN.md section 3), in database milliseconds; pending owner approval. */
  public static final long LOOT_OWNER_WINDOW_MILLIS = 60_000L;
  public static final long LOOT_LIFETIME_MILLIS = 15L * 60_000L;
  public static final int GROUND_LOOT_LIST_LIMIT = 256;
  /** Internal view; {@code ownerCharacter} is null once the owner window has closed. Never expose it to clients. */
  public record GroundLoot(UUID id, int world, double x, double y, String key, int quantity, int charges,
      UUID ownerCharacter, long ownerRemainingMillis) {}
  /** Inclusive tile rectangle. */
  public record TileRect(double minX, double minY, double maxX, double maxY) {
    public TileRect {
      if (!Double.isFinite(minX) || !Double.isFinite(minY) || !Double.isFinite(maxX) || !Double.isFinite(maxY)
          || minX>maxX || minY>maxY) throw new IllegalArgumentException("Invalid loot rectangle");
    }
  }
  public record ExpiredLoot(UUID id, int world) {}
  public record SpawnState(String key, long generation, UUID eventId, UUID recipient, java.time.Instant claimedAt,
      Integer health, UUID targetCharacter, long nextAttackAt, Double currentX, Double currentY,
      String mode, Long targetEpoch, long stateRevision, String targetSpawnKey, UUID targetLife,
      java.time.Instant diedAt, String deathKind) {
    public SpawnState(String key,long generation,UUID eventId,UUID recipient,java.time.Instant claimedAt,
        Integer health,UUID targetCharacter,long nextAttackAt,Double currentX,Double currentY,
        String mode,Long targetEpoch,long stateRevision) {
      this(key,generation,eventId,recipient,claimedAt,health,targetCharacter,nextAttackAt,currentX,currentY,
          mode,targetEpoch,stateRevision,null,null,null,null);
    }
    public SpawnState(String key,long generation,UUID eventId,UUID recipient,java.time.Instant claimedAt,
        Integer health,UUID targetCharacter,long nextAttackAt) {
      this(key,generation,eventId,recipient,claimedAt,health,targetCharacter,nextAttackAt,null,null,
          targetCharacter==null ? "IDLE" : "RETALIATION",null,0);
    }
  }
  /** One captured authoritative pose. Coordinates are absolute tiles, never client input. */
  public record SpawnMotion(String key, UUID life, long revision, double x, double y, String mode,
      UUID targetCharacter, Long targetEpoch, String targetSpawnKey, UUID targetLife) {
    public SpawnMotion(String key,UUID life,long revision,double x,double y,String mode,
        UUID targetCharacter,Long targetEpoch) {
      this(key,life,revision,x,y,mode,targetCharacter,targetEpoch,null,null);
    }
    public SpawnMotion {
      spawnKey(key); Objects.requireNonNull(life); Objects.requireNonNull(mode);
      if ((targetSpawnKey==null)!=(targetLife==null) || targetCharacter!=null && targetSpawnKey!=null
          || targetSpawnKey!=null && targetSpawnKey.equals(key))
        throw new IllegalArgumentException("Invalid creature target identity");
      if (revision<0 || !Double.isFinite(x) || !Double.isFinite(y) || x<0 || y<0
          || !java.util.Set.of("IDLE","PATROL","NATURAL","RETALIATION","RETURN","CLAN").contains(mode)
          || ((mode.equals("NATURAL") || mode.equals("RETALIATION")) != (targetCharacter!=null))
          || mode.equals("CLAN") != (targetSpawnKey!=null)
          || (targetCharacter==null && targetEpoch!=null)
          || (targetCharacter!=null && targetEpoch==null && !mode.equals("RETALIATION"))
          || (targetEpoch!=null && targetEpoch<0)) throw new IllegalArgumentException("Invalid creature motion");
    }
  }
  public record CreatureProvocation(String key, UUID life, UUID targetCharacter, long nextAttackAt, SpawnMotion motion) {
    public CreatureProvocation(String key,UUID life,UUID targetCharacter,long nextAttackAt) {
      this(key,life,targetCharacter,nextAttackAt,null);
    }
    public CreatureProvocation { spawnKey(key); Objects.requireNonNull(life); Objects.requireNonNull(targetCharacter); if(nextAttackAt<0) throw new IllegalArgumentException("Invalid attack deadline"); }
  }
  public record MonsterAttack(String key, UUID life, UUID targetCharacter, long expectedNext, long nextNext, SpawnMotion motion) {
    public MonsterAttack(String key,UUID life,UUID targetCharacter,long expectedNext,long nextNext) {
      this(key,life,targetCharacter,expectedNext,nextNext,null);
    }
    public MonsterAttack { spawnKey(key); Objects.requireNonNull(life); Objects.requireNonNull(targetCharacter); if(expectedNext<0 || nextNext<=expectedNext) throw new IllegalArgumentException("Invalid attack deadline"); }
  }
  public record DeathTransition(long experienceLost, CharacterProgress.Respawn destination,
      CharacterProgress.Respawn corpsePosition, java.util.List<CharacterProgress.Item> droppedItems,
      long goldLost, long goldDropped) {
    public DeathTransition { Objects.requireNonNull(destination); Objects.requireNonNull(corpsePosition);
      droppedItems=java.util.List.copyOf(droppedItems);
      if(experienceLost<0 || goldLost<0 || goldDropped<0 || goldDropped>goldLost) throw new IllegalArgumentException("Invalid death penalty"); }
  }
  public record CorpseState(UUID id, UUID characterId, int world, double x, double y,
      java.util.List<CharacterProgress.Item> items, long gold, boolean recovered) {
    public CorpseState { items=java.util.List.copyOf(items); }
  }
  /** Trusted resolved damage, never a network payload. Null stored health means untouched authored health. */
  public record CreatureStrike(String key, UUID life, int maximumHealth, int before, int after, SpawnMotion motion) {
    public CreatureStrike(String key,UUID life,int maximumHealth,int before,int after) {
      this(key,life,maximumHealth,before,after,null);
    }
    public CreatureStrike {
      spawnKey(key); Objects.requireNonNull(life);
      if (maximumHealth<=0 || before<=0 || before>maximumHealth || after<0 || after>=before)
        throw new IllegalArgumentException("Invalid creature damage");
    }
  }
  /**
   * One creature hit by a spell. A lethal strike also carries the kill's rolled ground drops, which commit
   * under that creature's own life claim. The captured pose is required: every spell strike is a
   * pose-guarded update, like a melee strike.
   */
  public record SpellStrike(CreatureStrike strike, CreatureProvocation provocation, java.util.List<LootDropDraft> drops) {
    public SpellStrike {
      Objects.requireNonNull(strike); drops=java.util.List.copyOf(drops);
      if (strike.motion()==null) throw new IllegalArgumentException("A spell strike needs the captured creature pose");
      if (!drops.isEmpty() && strike.after()!=0) throw new IllegalArgumentException("Loot requires a lethal strike");
      if (provocation!=null && (!provocation.key().equals(strike.key()) || !provocation.life().equals(strike.life())))
        throw new IllegalArgumentException("Different provoked life");
      for (int i=0;i<drops.size();i++) if (drops.get(i).index()!=i) throw new IllegalArgumentException("Invalid loot index");
    }
  }
  /** One already resolved server-authored physical exchange. A miss has after==before. */
  public record ClanAttack(SpawnMotion source, SpawnMotion target, int maximumHealth,
      int before, int after, long expectedNext, long nextNext) {
    public ClanAttack {
      Objects.requireNonNull(source); Objects.requireNonNull(target);
      if (!"CLAN".equals(source.mode()) || !target.key().equals(source.targetSpawnKey())
          || !target.life().equals(source.targetLife()) || source.key().equals(target.key())
          || maximumHealth<=0 || before<=0 || before>maximumHealth || after<0 || after>before
          || expectedNext<0 || nextNext<=expectedNext)
        throw new IllegalArgumentException("Invalid clan attack draft");
    }
  }
  public record ClanAttackResult(SpawnState source, SpawnState target) {}
  /** One-time delivery to an operator; never log or serialize this record into world snapshots. */
  public record AccountKey(UUID accountId, String secret) {
    @Override public String toString() { return "AccountKey[accountId=" + accountId + ", secret=REDACTED]"; }
  }
  /** Internal server capability, not a client-supplied account/character identity. */
  public record Session(UUID accountId, UUID characterId, UUID leaseId, CharacterState character) {
    @Override public String toString() { return "Session[characterId=" + characterId + ", leaseId=REDACTED]"; }
  }
  private static final SecureRandom RANDOM = new SecureRandom();
  public static final class Conflict extends SQLException {
    public Conflict(String message) { super(message); }
  }
  public static final class CreationConflict extends SQLException {
    private final String code;
    public CreationConflict(String code) { super(code);this.code=code; }
    public String code() { return code; }
  }
  public record InitialState(CharacterProgress progress,long gold) {}
  private final Connections connections;
  private final OnlineDatabasePool ownedPool;

  /** Injected connection factories remain caller-owned; each operation closes its borrowed handle. */
  public OnlineStore(Connections connections) { this.connections=Objects.requireNonNull(connections);this.ownedPool=null; }
  private OnlineStore(OnlineDatabasePool pool) { this.connections=pool;this.ownedPool=pool; }

  /** Long-lived realm factory. Close only after its server has stopped and flushed accepted writes. */
  public static OnlineStore pooledFromEnvironment() {
    return new OnlineStore(new OnlineDatabasePool(required("T4C_DB_URL"),required("T4C_DB_USER"),required("T4C_DB_PASSWORD")));
  }

  @Override public void close() { if(ownedPool!=null) ownedPool.close(); }

  /** Short-lived operator commands and existing injected/test lifetimes need no persistent pool. */
  public static OnlineStore fromEnvironment() {
    String url = required("T4C_DB_URL"), user = required("T4C_DB_USER"), password = required("T4C_DB_PASSWORD");
    if (!url.startsWith("jdbc:postgresql://")) throw new IllegalArgumentException("PostgreSQL URL required");
    var properties = new java.util.Properties();
    properties.setProperty("user", user); properties.setProperty("password", password);
    properties.setProperty("connectTimeout", "5"); properties.setProperty("socketTimeout", "10");
    return new OnlineStore(() -> DriverManager.getConnection(url, properties));
  }

  private static String required(String key) {
    String value = System.getenv(key);
    if (value == null || value.isBlank()) throw new IllegalStateException("Missing " + key);
    return value;
  }

  /** Transactional, serialized schema installation. Never touches desktop save files. */
  public void migrate() throws SQLException {
    try (var c = connections.open()) {
      c.setAutoCommit(false);
      try (var s = c.createStatement()) {
        s.execute("SET LOCAL lock_timeout = '5s'");
        s.execute("SELECT pg_advisory_xact_lock(7404161001)");
        s.execute("CREATE TABLE IF NOT EXISTS online_schema (version integer PRIMARY KEY, installed_at timestamptz NOT NULL DEFAULT now())");
        int version = 0;
        try (var rows = s.executeQuery("SELECT version FROM online_schema ORDER BY version")) {
          while (rows.next()) {
            if (rows.getInt(1) != ++version || version > 13) throw new SQLException("Unsupported online schema version");
          }
        }
        if (version == 0) {
        s.execute("""
            CREATE TABLE online_characters (
              id uuid PRIMARY KEY, account_id uuid NOT NULL,
              name varchar(32) NOT NULL CHECK (length(name) BETWEEN 1 AND 32),
              world integer NOT NULL CHECK (world >= 0),
              x double precision NOT NULL CHECK (x >= 0 AND x < 'Infinity'::float8),
              y double precision NOT NULL CHECK (y >= 0 AND y < 'Infinity'::float8),
              gold bigint NOT NULL DEFAULT 0 CHECK (gold >= 0),
              experience bigint NOT NULL DEFAULT 0 CHECK (experience >= 0),
              revision bigint NOT NULL DEFAULT 0 CHECK (revision >= 0),
              updated_at timestamptz NOT NULL DEFAULT now())
            """);
        s.execute("CREATE INDEX online_characters_account ON online_characters(account_id)");
        s.execute("""
            CREATE TABLE online_rewards (
              character_id uuid NOT NULL REFERENCES online_characters(id),
              event_id uuid NOT NULL,
              gold bigint NOT NULL CHECK (gold >= 0),
              experience bigint NOT NULL CHECK (experience >= 0),
              created_at timestamptz NOT NULL DEFAULT now(),
              PRIMARY KEY(character_id, event_id))
            """);
        s.execute("INSERT INTO online_schema(version) VALUES (1)");
        }
        if (version < 2) {
          s.execute("""
              CREATE TABLE online_accounts (
                id uuid PRIMARY KEY, access_hash varchar(64) UNIQUE,
                CHECK (access_hash IS NULL OR access_hash ~ '^[0-9a-f]{64}$'))
              """);
          // Preserve version-one records, but never silently grant their accounts credentials.
          s.execute("INSERT INTO online_accounts(id) SELECT DISTINCT account_id FROM online_characters");
          s.execute("ALTER TABLE online_characters ADD CONSTRAINT online_owner FOREIGN KEY(account_id) REFERENCES online_accounts(id)");
          s.execute("ALTER TABLE online_characters ADD COLUMN session_id uuid, ADD COLUMN session_until timestamptz");
          s.execute("ALTER TABLE online_characters ADD CONSTRAINT online_session_pair CHECK ((session_id IS NULL) = (session_until IS NULL))");
          s.execute("INSERT INTO online_schema(version) VALUES (2)");
        }
        if (version < 3) {
          // Existing movement-only characters remain explicitly uninitialized, never rerolled.
          s.execute("ALTER TABLE online_characters ADD COLUMN progress jsonb CHECK (progress IS NULL OR (jsonb_typeof(progress)='object' AND progress ? 'version'))");
          s.execute("INSERT INTO online_schema(version) VALUES (3)");
        }
        if (version < 4) {
          s.execute("ALTER TABLE online_rewards ADD COLUMN payload_hash varchar(64) CHECK (payload_hash IS NULL OR payload_hash ~ '^[0-9a-f]{64}$')");
          s.execute("INSERT INTO online_schema(version) VALUES (4)");
        }
        if (version < 5) {
          s.execute("""
              CREATE TABLE online_world_rewards (
                event_id uuid PRIMARY KEY, character_id uuid NOT NULL,
                FOREIGN KEY(character_id,event_id) REFERENCES online_rewards(character_id,event_id))
              """);
          s.execute("INSERT INTO online_schema(version) VALUES (5)");
        }
        if (version < 6) {
          // Legacy claims predate spawn persistence; use migration time conservatively.
          s.execute("ALTER TABLE online_world_rewards ADD COLUMN claimed_at timestamptz NOT NULL DEFAULT clock_timestamp()");
          s.execute("""
              CREATE TABLE online_spawns (
                spawn_key varchar(256) PRIMARY KEY CHECK (length(spawn_key)>0),
                generation bigint NOT NULL DEFAULT 0 CHECK (generation>=0),
                event_id uuid NOT NULL UNIQUE)
              """);
          s.execute("INSERT INTO online_schema(version) VALUES (6)");
        }
        if (version < 7) {
          s.execute("ALTER TABLE online_spawns ADD COLUMN health integer CHECK (health>=0)");
          s.execute("INSERT INTO online_schema(version) VALUES (7)");
        }
        if (version < 8) {
          s.execute("ALTER TABLE online_spawns ADD COLUMN target_character uuid REFERENCES online_characters(id), ADD COLUMN next_attack_at bigint NOT NULL DEFAULT 0 CHECK(next_attack_at>=0)");
          s.execute("CREATE INDEX online_spawns_target ON online_spawns(target_character) WHERE target_character IS NOT NULL");
          s.execute("CREATE TABLE online_combat_events (event_id uuid PRIMARY KEY, character_id uuid NOT NULL REFERENCES online_characters(id), payload_hash varchar(64) NOT NULL, created_at timestamptz NOT NULL DEFAULT now())");
          s.execute("CREATE TABLE online_corpses (id uuid PRIMARY KEY REFERENCES online_combat_events(event_id), character_id uuid NOT NULL REFERENCES online_characters(id), world integer NOT NULL CHECK(world>=0), x double precision NOT NULL, y double precision NOT NULL, items jsonb NOT NULL CHECK(jsonb_typeof(items)='array'), gold bigint NOT NULL CHECK(gold>=0), recovered boolean NOT NULL DEFAULT false, created_at timestamptz NOT NULL DEFAULT now())");
          s.execute("CREATE INDEX online_corpses_owner ON online_corpses(character_id) WHERE NOT recovered");
          s.execute("INSERT INTO online_schema(version) VALUES (8)");
        }
        if (version < 9) {
          s.execute("ALTER TABLE online_characters ADD COLUMN location_epoch bigint NOT NULL DEFAULT 0 CHECK(location_epoch>=0), ADD COLUMN portal_cooldown_until bigint NOT NULL DEFAULT 0 CHECK(portal_cooldown_until>=0)");
          s.execute("CREATE TABLE online_transfer_events(event_id uuid PRIMARY KEY,character_id uuid NOT NULL REFERENCES online_characters(id),payload_hash varchar(64) NOT NULL,created_at timestamptz NOT NULL DEFAULT now())");
          s.execute("INSERT INTO online_schema(version) VALUES (9)");
        }
        if (version < 10) {
          // Additive only: no existing row changes. Ground drops are server-owned, single-unit and
          // claimed by exactly one receipt; their lifetime is database time, never client time.
          s.execute("""
              CREATE TABLE online_ground_loot (
                id uuid PRIMARY KEY,
                source_event uuid NOT NULL REFERENCES online_world_rewards(event_id),
                drop_index integer NOT NULL CHECK (drop_index >= 0),
                world integer NOT NULL CHECK (world >= 0),
                x double precision NOT NULL CHECK (x >= 0 AND x < 'Infinity'::float8),
                y double precision NOT NULL CHECK (y >= 0 AND y < 'Infinity'::float8),
                item_key varchar(256) NOT NULL CHECK (item_key ~ '^item\\.[a-z0-9_]+$'),
                quantity integer NOT NULL CHECK (quantity > 0),
                charges integer NOT NULL CHECK (charges >= 0),
                owner_character uuid NOT NULL REFERENCES online_characters(id),
                owner_until timestamptz NOT NULL,
                expires_at timestamptz NOT NULL CHECK (expires_at >= owner_until),
                state varchar(10) NOT NULL DEFAULT 'ON_GROUND' CHECK (state IN ('ON_GROUND','CLAIMED','EXPIRED')),
                claimed_by uuid REFERENCES online_characters(id),
                claim_event uuid UNIQUE,
                created_at timestamptz NOT NULL DEFAULT clock_timestamp(),
                UNIQUE (source_event, drop_index),
                CHECK ((state = 'CLAIMED') = (claimed_by IS NOT NULL AND claim_event IS NOT NULL)))
              """);
          s.execute("CREATE INDEX online_ground_loot_live ON online_ground_loot(world, expires_at) WHERE state = 'ON_GROUND'");
          s.execute("""
              CREATE TABLE online_loot_claims (
                event_id uuid PRIMARY KEY,
                character_id uuid NOT NULL REFERENCES online_characters(id),
                loot_id uuid NOT NULL UNIQUE REFERENCES online_ground_loot(id),
                payload_hash varchar(64) NOT NULL CHECK (payload_hash ~ '^[0-9a-f]{64}$'),
                created_at timestamptz NOT NULL DEFAULT now())
              """);
          s.execute("INSERT INTO online_schema(version) VALUES (10)");
        }
        if (version < 11) {
          s.execute("""
              ALTER TABLE online_spawns
                ADD COLUMN current_x double precision,
                ADD COLUMN current_y double precision,
                ADD COLUMN mode varchar(16) NOT NULL DEFAULT 'IDLE',
                ADD COLUMN target_epoch bigint,
                ADD COLUMN state_revision bigint NOT NULL DEFAULT 0,
                ADD CONSTRAINT online_spawn_position_pair CHECK ((current_x IS NULL) = (current_y IS NULL)),
                ADD CONSTRAINT online_spawn_position_finite CHECK (current_x IS NULL OR
                  (current_x >= 0 AND current_x < 'Infinity'::float8 AND current_y >= 0 AND current_y < 'Infinity'::float8)),
                ADD CONSTRAINT online_spawn_mode CHECK (mode IN ('IDLE','PATROL','NATURAL','RETALIATION','RETURN')),
                ADD CONSTRAINT online_spawn_target_epoch CHECK (target_epoch IS NULL OR target_epoch >= 0),
                ADD CONSTRAINT online_spawn_revision CHECK (state_revision >= 0)
              """);
          // Every pre-v11 target arose from a committed player provocation. Its epoch is resolved
          // against a live lease before the creature is allowed to attack again.
          s.execute("UPDATE online_spawns SET mode='RETALIATION' WHERE target_character IS NOT NULL");
          s.execute("INSERT INTO online_schema(version) VALUES (11)");
        }
        if (version < 12) {
          s.execute("""
              ALTER TABLE online_spawns
                ADD COLUMN target_spawn_key varchar(256) REFERENCES online_spawns(spawn_key),
                ADD COLUMN target_life uuid,
                ADD COLUMN died_at timestamptz,
                ADD COLUMN death_kind varchar(8)
              """);
          s.execute("""
              UPDATE online_spawns AS spawn SET health=0,died_at=reward.claimed_at,death_kind='PLAYER'
              FROM online_world_rewards AS reward WHERE reward.event_id=spawn.event_id
              """);
          try (var invalid=s.executeQuery("""
              SELECT 1 FROM online_spawns AS spawn
              WHERE (spawn.health=0) <> (spawn.died_at IS NOT NULL)
                 OR (spawn.died_at IS NOT NULL AND spawn.health IS DISTINCT FROM 0)
              LIMIT 1
              """)) {
            if (invalid.next()) throw new SQLException("Inconsistent legacy creature death state");
          }
          // Version 11 recorded a claimed life but could retain its former target and deadline.
          // Clear only that dead life's intent; its event, damage, position and reward stay intact.
          s.execute("""
              UPDATE online_spawns SET mode='IDLE',target_character=NULL,target_epoch=NULL,next_attack_at=0
              WHERE died_at IS NOT NULL
              """);
          s.execute("ALTER TABLE online_spawns DROP CONSTRAINT online_spawn_mode");
          s.execute("""
              ALTER TABLE online_spawns
                ADD CONSTRAINT online_spawn_mode CHECK (mode IN ('IDLE','PATROL','NATURAL','RETALIATION','RETURN','CLAN')),
                ADD CONSTRAINT online_spawn_creature_target_pair
                  CHECK ((target_spawn_key IS NULL) = (target_life IS NULL)),
                ADD CONSTRAINT online_spawn_target_kind
                  CHECK (target_character IS NULL OR target_spawn_key IS NULL),
                ADD CONSTRAINT online_spawn_no_self_target
                  CHECK (target_spawn_key IS NULL OR target_spawn_key <> spawn_key),
                ADD CONSTRAINT online_spawn_mode_target CHECK (
                  (mode='CLAN' AND target_spawn_key IS NOT NULL AND target_character IS NULL)
                  OR (mode IN ('NATURAL','RETALIATION') AND target_character IS NOT NULL AND target_spawn_key IS NULL)
                  OR (mode IN ('IDLE','PATROL','RETURN') AND target_character IS NULL AND target_spawn_key IS NULL)),
                ADD CONSTRAINT online_spawn_death_pair CHECK (
                  (died_at IS NULL AND death_kind IS NULL AND (health IS NULL OR health>0))
                  OR (died_at IS NOT NULL AND death_kind IS NOT NULL
                    AND death_kind IN ('PLAYER','CLAN') AND health IS NOT NULL AND health=0)),
                ADD CONSTRAINT online_spawn_terminal_clear CHECK (
                  died_at IS NULL OR (target_character IS NULL AND target_epoch IS NULL
                    AND target_spawn_key IS NULL AND target_life IS NULL AND next_attack_at=0))
              """);
          s.execute("INSERT INTO online_schema(version) VALUES (12)");
        }
        if (version < 13) {
          s.execute("ALTER TABLE online_characters ADD CONSTRAINT online_character_owner_id UNIQUE(account_id,id)");
          s.execute("""
              CREATE TABLE online_character_creations (
                account_id uuid NOT NULL REFERENCES online_accounts(id),
                request_id uuid NOT NULL,
                payload_hash varchar(64) NOT NULL CHECK (payload_hash ~ '^[0-9a-f]{64}$'),
                character_id uuid NOT NULL,
                created_at timestamptz NOT NULL DEFAULT now(),
                PRIMARY KEY(account_id,request_id),
                FOREIGN KEY(account_id,character_id) REFERENCES online_characters(account_id,id))
              """);
          s.execute("INSERT INTO online_schema(version) VALUES (13)");
        }
        c.commit();
      } catch (SQLException | RuntimeException error) { c.rollback(); throw error; }
    }
  }

  /** Trusted authored spawn key, stable across content ordering and process restarts. Worker-thread only. */
  public SpawnState loadOrCreateSpawn(String key) throws SQLException {
    spawnKey(key);
    try (var c=connections.open()) {
      try (var s=c.prepareStatement("INSERT INTO online_spawns(spawn_key,event_id) VALUES (?,?) ON CONFLICT (spawn_key) DO NOTHING")) {
        s.setString(1,key); s.setObject(2,UUID.randomUUID()); s.executeUpdate();
      }
      return loadSpawn(c,key);
    }
  }

  private static SpawnState loadSpawn(Connection c,String key) throws SQLException {
      try (var s=c.prepareStatement("""
          SELECT s.generation,s.event_id,r.character_id,r.claimed_at,s.health,s.target_character,s.next_attack_at,
                 s.current_x,s.current_y,s.mode,s.target_epoch,s.state_revision,
                 s.target_spawn_key,s.target_life,s.died_at,s.death_kind
          FROM online_spawns s LEFT JOIN online_world_rewards r ON r.event_id=s.event_id
          WHERE s.spawn_key=?
          """)) {
        s.setString(1,key);
        try (var rows=s.executeQuery()) {
          if (!rows.next()) throw new Conflict("Spawn unavailable");
          var claimedAt=rows.getTimestamp(4);
          return new SpawnState(key,rows.getLong(1),rows.getObject(2,UUID.class),rows.getObject(3,UUID.class),
              claimedAt==null ? null : claimedAt.toInstant(),rows.getObject(5,Integer.class),rows.getObject(6,UUID.class),rows.getLong(7),
              rows.getObject(8,Double.class),rows.getObject(9,Double.class),rows.getString(10),rows.getObject(11,Long.class),rows.getLong(12),
              rows.getString(13),rows.getObject(14,UUID.class),rows.getTimestamp(15)==null ? null : rows.getTimestamp(15).toInstant(),rows.getString(16));
        }
      }
  }

  /** Advance only a committed terminal life after its authored delay, using database time and CAS. */
  public SpawnState respawn(String key, UUID expectedEvent, long delayMillis) throws SQLException {
    return respawn(key,expectedEvent,loadOrCreateSpawn(key).stateRevision(),delayMillis);
  }

  public SpawnState respawn(String key, UUID expectedEvent, long expectedRevision, long delayMillis) throws SQLException {
    spawnKey(key); Objects.requireNonNull(expectedEvent);
    if (delayMillis<0 || expectedRevision<0) throw new IllegalArgumentException("Invalid respawn precondition");
    try (var c=connections.open(); var s=c.prepareStatement("""
        UPDATE online_spawns s SET generation=s.generation+1,event_id=?,health=NULL,target_character=NULL,next_attack_at=0,
          current_x=NULL,current_y=NULL,mode='IDLE',target_epoch=NULL,target_spawn_key=NULL,target_life=NULL,
          died_at=NULL,death_kind=NULL,state_revision=state_revision+1
        WHERE s.spawn_key=? AND s.event_id=? AND s.state_revision=? AND s.died_at IS NOT NULL
          AND s.died_at<=clock_timestamp()-(? * interval '1 millisecond')
        RETURNING s.generation,s.event_id,s.state_revision
        """)) {
      s.setObject(1,UUID.randomUUID()); s.setString(2,key); s.setObject(3,expectedEvent);
      s.setLong(4,expectedRevision); s.setLong(5,delayMillis);
      try (var rows=s.executeQuery()) {
        if (!rows.next()) throw new Conflict("Spawn life changed or respawn not ready");
        return new SpawnState(key,rows.getLong(1),rows.getObject(2,UUID.class),null,null,null,null,0,
            null,null,"IDLE",null,rows.getLong(3));
      }
    }
  }

  /** Coalesced motion checkpoint. A mismatch is ambiguous to its owner and must trigger reload. */
  public SpawnState checkpointSpawn(SpawnMotion motion, Session targetSession) throws SQLException {
    Objects.requireNonNull(motion);
    if (targetSession!=null && !targetSession.characterId().equals(motion.targetCharacter()))
      throw new IllegalArgumentException("Wrong target session");
    try (var c=connections.open()) {
      c.setAutoCommit(false);
      try {
        if (targetSession!=null) {
          var target=lockSession(c,targetSession);
          if (motion.targetEpoch()==null || target.locationEpoch()!=motion.targetEpoch() || target.progress()==null
              || target.progress().vitals().hp()<=0) throw new Conflict("Creature target changed");
        }
        if (motion.targetSpawnKey()!=null) {
          lockSpawnKeys(c,motion.key(),motion.targetSpawnKey());
          var target=loadSpawn(c,motion.targetSpawnKey());
          if (!motion.targetLife().equals(target.eventId()) || target.diedAt()!=null)
            throw new Conflict("Creature target life changed");
        }
        try (var s=c.prepareStatement("""
            UPDATE online_spawns SET current_x=?,current_y=?,mode=?,target_character=?,target_epoch=?,
              target_spawn_key=?,target_life=?,
              state_revision=state_revision+1
            WHERE spawn_key=? AND event_id=? AND state_revision=? AND died_at IS NULL AND COALESCE(health,1)>0
              AND (? OR (target_character IS NOT DISTINCT FROM ? AND target_epoch IS NOT DISTINCT FROM ?))
            """)) {
          s.setDouble(1,motion.x());s.setDouble(2,motion.y());s.setString(3,motion.mode());
          s.setObject(4,motion.targetCharacter());s.setObject(5,motion.targetEpoch());
          s.setString(6,motion.targetSpawnKey());s.setObject(7,motion.targetLife());
          s.setString(8,motion.key());s.setObject(9,motion.life());s.setLong(10,motion.revision());
          s.setBoolean(11,motion.targetCharacter()==null || targetSession!=null);
          s.setObject(12,motion.targetCharacter());s.setObject(13,motion.targetEpoch());
          if (s.executeUpdate()!=1) throw new Conflict("Creature motion changed");
        }
        c.commit();
      } catch (SQLException | RuntimeException error) { c.rollback(); throw error; }
    }
    return loadOrCreateSpawn(motion.key());
  }

  /** Atomic, reward-free two-life exchange. The caller owns one immutable resolved draft. */
  public ClanAttackResult commitClanAttack(ClanAttack attack) throws SQLException {
    Objects.requireNonNull(attack);
    var source=attack.source();var target=attack.target();
    try(var c=connections.open()) {
      c.setAutoCommit(false);
      try {
        lockSpawnKeys(c,source.key(),target.key());
        var beforeSource=loadSpawn(c,source.key());
        var beforeTarget=loadSpawn(c,target.key());
        if (!beforeSource.eventId().equals(source.life()) || beforeSource.stateRevision()!=source.revision()
            || beforeSource.diedAt()!=null || !"CLAN".equals(beforeSource.mode())
            || !target.key().equals(beforeSource.targetSpawnKey())
            || !target.life().equals(beforeSource.targetLife())
            || beforeSource.nextAttackAt()!=attack.expectedNext()
            || !beforeTarget.eventId().equals(target.life()) || beforeTarget.stateRevision()!=target.revision()
            || beforeTarget.diedAt()!=null
            || (beforeTarget.health()==null ? attack.maximumHealth() : beforeTarget.health())!=attack.before())
          throw new Conflict("Clan attack life or target changed");
        try(var q=c.prepareStatement("""
            UPDATE online_spawns SET current_x=?,current_y=?,mode='CLAN',target_character=NULL,target_epoch=NULL,
              target_spawn_key=?,target_life=?,next_attack_at=?,state_revision=state_revision+1
            WHERE spawn_key=? AND event_id=? AND state_revision=? AND died_at IS NULL
              AND target_spawn_key=? AND target_life=? AND next_attack_at=?
              AND next_attack_at<=floor(extract(epoch FROM clock_timestamp())*1000)
            """)) {
          q.setDouble(1,source.x());q.setDouble(2,source.y());q.setString(3,target.key());
          q.setObject(4,target.life());q.setLong(5,attack.nextNext());
          q.setString(6,source.key());q.setObject(7,source.life());q.setLong(8,source.revision());
          q.setString(9,target.key());q.setObject(10,target.life());q.setLong(11,attack.expectedNext());
          if(q.executeUpdate()!=1) throw new Conflict("Clan attack cooldown changed");
        }
        boolean lethal=attack.after()==0;
        try(var q=c.prepareStatement("""
            UPDATE online_spawns SET current_x=?,current_y=?,health=?,
              mode=?,target_character=?,target_epoch=?,target_spawn_key=?,target_life=?,
              next_attack_at=CASE WHEN ? THEN 0 ELSE next_attack_at END,
              died_at=CASE WHEN ? THEN clock_timestamp() ELSE died_at END,
              death_kind=CASE WHEN ? THEN 'CLAN' ELSE death_kind END,
              state_revision=state_revision+1
            WHERE spawn_key=? AND event_id=? AND state_revision=? AND died_at IS NULL
              AND COALESCE(health,?)=?
            """)) {
          q.setDouble(1,target.x());q.setDouble(2,target.y());q.setInt(3,attack.after());
          q.setString(4,lethal ? "IDLE" : target.mode());
          q.setObject(5,lethal ? null : target.targetCharacter());
          q.setObject(6,lethal ? null : target.targetEpoch());
          q.setString(7,lethal ? null : target.targetSpawnKey());
          q.setObject(8,lethal ? null : target.targetLife());
          q.setBoolean(9,lethal);q.setBoolean(10,lethal);q.setBoolean(11,lethal);
          q.setString(12,target.key());q.setObject(13,target.life());q.setLong(14,target.revision());
          q.setInt(15,attack.maximumHealth());q.setInt(16,attack.before());
          if(q.executeUpdate()!=1) throw new Conflict("Clan target life changed");
        }
        var committed=new ClanAttackResult(loadSpawn(c,source.key()),loadSpawn(c,target.key()));
        c.commit();return committed;
      } catch(SQLException|RuntimeException failure) { c.rollback();throw failure; }
    }
  }

  private static void spawnKey(String key) {
    if (key==null || key.isBlank() || key.length()>256) throw new IllegalArgumentException("Invalid spawn key");
  }

  /** Acquire the complete creature row set in one stable key order before any mutation. */
  private static void lockSpawnKeys(Connection c,String first,String second) throws SQLException {
    var keys=java.util.stream.Stream.of(first,second).distinct().sorted().toList();
    for(var key:keys) try(var q=c.prepareStatement("SELECT 1 FROM online_spawns WHERE spawn_key=? FOR UPDATE")) {
      q.setString(1,key);try(var rows=q.executeQuery()) { if(!rows.next()) throw new Conflict("Creature life unavailable"); }
    }
  }

  /** Operator-only provisioning. The database stores a verifier, not the usable access key. */
  public AccountKey provisionAccount() throws SQLException {
    UUID account = UUID.randomUUID(); String secret = newKey();
    try (var c = connections.open(); var s = c.prepareStatement("INSERT INTO online_accounts(id,access_hash) VALUES (?,?)")) {
      s.setObject(1, account); s.setString(2, hash(secret)); s.executeUpdate();
    }
    return new AccountKey(account, secret);
  }

  public record RosterEntry(CharacterState character,boolean inUse) {}
  /** Read-only account authentication; neither a character lease nor a realm slot is acquired. */
  public java.util.List<RosterEntry> roster(String secret,UUID after) throws SQLException {
    String verifier=hash(secret);
    try(var c=connections.open()) {
      c.setAutoCommit(false);
      try {
        UUID account;
        // Same account-first lock order as rotation. Rotation cannot interleave this owner read.
        try(var q=c.prepareStatement("SELECT id FROM online_accounts WHERE access_hash=? FOR SHARE")) {
          q.setQueryTimeout(2);q.setString(1,verifier);try(var rows=q.executeQuery()) {
            if(!rows.next()) throw new Conflict("Account unavailable");account=rows.getObject(1,UUID.class);
          }
        }
        var result=new java.util.ArrayList<RosterEntry>();
        try(var q=c.prepareStatement("SELECT *,session_id IS NOT NULL AND session_until>clock_timestamp() AS in_use FROM online_characters WHERE account_id=? AND (?::uuid IS NULL OR id>?::uuid) ORDER BY id LIMIT ?")) {
          q.setQueryTimeout(2);q.setObject(1,account);q.setObject(2,after);q.setObject(3,after);
          q.setInt(4,com.perso.T4C.online.model.OnlineRoster.PAGE_SIZE+1);
          try(var rows=q.executeQuery()) { while(rows.next()) result.add(new RosterEntry(read(rows),rows.getBoolean("in_use"))); }
        }
        c.commit();return java.util.List.copyOf(result);
      } catch(SQLException|RuntimeException failure) { c.rollback();throw failure; }
    }
  }

  /** Cheap owner check before a potentially slow HTTP body read; creation rechecks under lock. */
  public void authenticateAccount(String secret) throws SQLException {
    String verifier=hash(secret);
    try(var c=connections.open();var q=c.prepareStatement("SELECT 1 FROM online_accounts WHERE access_hash=?")) {
      q.setQueryTimeout(2);q.setString(1,verifier);
      try(var rows=q.executeQuery()) { if(!rows.next()) throw new Conflict("Account unavailable"); }
    }
  }

  /** Account-serialized, idempotent creation; the starter factory runs only after receipt and capacity checks. */
  public RosterEntry createAuthenticated(String secret,OnlineCharacterCreation.Request request,
      int world,double x,double y,Supplier<InitialState> starter) throws SQLException {
    Objects.requireNonNull(request);Objects.requireNonNull(starter);position(world,x,y);
    String verifier=hash(secret);
    String payload=hashCreationPayload(request);
    try(var c=connections.open()) {
      c.setAutoCommit(false);
      try {
        try(var timeout=c.createStatement()) { timeout.execute("SET LOCAL lock_timeout = '2s'"); }
        UUID account;
        try(var q=c.prepareStatement("SELECT id FROM online_accounts WHERE access_hash=? FOR UPDATE")) {
          q.setQueryTimeout(3);q.setString(1,verifier);
          try(var rows=q.executeQuery()) {
            if(!rows.next()) throw new Conflict("Account unavailable");
            account=rows.getObject(1,UUID.class);
          }
        }
        try(var q=c.prepareStatement("""
            SELECT c.payload_hash,c.character_id FROM online_character_creations c
            WHERE c.account_id=? AND c.request_id=?
            """)) {
          q.setQueryTimeout(2);q.setObject(1,account);q.setObject(2,request.requestId());
          try(var rows=q.executeQuery()) {
            if(rows.next()) {
              if(!payload.equals(rows.getString(1))) throw new CreationConflict("creation_conflict");
              RosterEntry existing;
              try(var owned=c.prepareStatement("""
                  SELECT *,session_id IS NOT NULL AND session_until>clock_timestamp() AS in_use
                  FROM online_characters WHERE id=? AND account_id=?
                  """)) {
                owned.setQueryTimeout(2);owned.setObject(1,rows.getObject(2,UUID.class));owned.setObject(2,account);
                try(var state=owned.executeQuery()) {
                  if(!state.next()) throw new SQLException("Creation receipt has no owned character");
                  existing=new RosterEntry(read(state),state.getBoolean("in_use"));
                }
              }
              c.commit();return existing;
            }
          }
        }
        var names=new java.util.ArrayList<String>();
        try(var q=c.prepareStatement("SELECT name FROM online_characters WHERE account_id=? LIMIT ?")) {
          q.setQueryTimeout(2);q.setObject(1,account);
          q.setInt(2,MAX_ACCOUNT_CHARACTERS+1);
          try(var rows=q.executeQuery()) { while(rows.next()) names.add(rows.getString(1)); }
        }
        if(names.size()>=MAX_ACCOUNT_CHARACTERS)
          throw new CreationConflict("roster_full");
        if(names.stream().anyMatch(name->name.equalsIgnoreCase(request.name())))
          throw new CreationConflict("name_taken");
        InitialState initial=Objects.requireNonNull(starter.get());
        if(initial.progress()==null || initial.gold()<0) throw new IllegalArgumentException("Invalid initial state");
        CharacterState created;
        try(var q=c.prepareStatement("""
            INSERT INTO online_characters(id,account_id,name,world,x,y,progress,gold)
            VALUES (?,?,?,?,?,?,?::jsonb,?) RETURNING *
            """)) {
          q.setQueryTimeout(2);q.setObject(1,UUID.randomUUID());q.setObject(2,account);
          q.setString(3,request.name());q.setInt(4,world);q.setDouble(5,x);q.setDouble(6,y);
          q.setString(7,encodeProgress(initial.progress()));q.setLong(8,initial.gold());
          try(var rows=q.executeQuery()) { if(!rows.next()) throw new SQLException("Creation insert failed");created=read(rows); }
        }
        try(var q=c.prepareStatement("""
            INSERT INTO online_character_creations(account_id,request_id,payload_hash,character_id)
            VALUES (?,?,?,?)
            """)) {
          q.setQueryTimeout(2);q.setObject(1,account);q.setObject(2,request.requestId());
          q.setString(3,payload);q.setObject(4,created.id());q.executeUpdate();
        }
        c.commit();return new RosterEntry(created,false);
      } catch(SQLException|RuntimeException failure) { c.rollback();throw failure; }
    }
  }

  private static String hashCreationPayload(OnlineCharacterCreation.Request request) {
    String canonical=request.name()+"\u0000"+request.characterClass()+"\u0000"
        +request.gender()+"\u0000"+request.evil();
    try {
      return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
          .digest(canonical.getBytes(StandardCharsets.UTF_8)));
    } catch(java.security.NoSuchAlgorithmException impossible) { throw new AssertionError(impossible); }
  }

  /** Operator-only rotation also fences all existing sessions before returning the new key. */
  public AccountKey rotateAccountKey(UUID account) throws SQLException {
    String secret = newKey();
    try (var c = connections.open()) {
      c.setAutoCommit(false);
      try {
        try (var s = c.prepareStatement("UPDATE online_accounts SET access_hash=? WHERE id=?")) {
          s.setString(1, hash(secret)); s.setObject(2, account);
          if (s.executeUpdate() != 1) throw new Conflict("Account unavailable");
        }
        try (var s = c.prepareStatement("UPDATE online_characters SET session_id=NULL, session_until=NULL WHERE account_id=?")) {
          s.setObject(1, account); s.executeUpdate();
        }
        c.commit();
      } catch (SQLException | RuntimeException error) { c.rollback(); throw error; }
    }
    return new AccountKey(account, secret);
  }

  /** Authenticate a provisioned key, then atomically acquire one character's expiring write lease. */
  public Session acquire(String secret, UUID character) throws SQLException {
    String verifier = hash(secret);
    try (var c = connections.open()) {
      c.setAutoCommit(false);
      try {
        UUID account;
        // Lock order is account then character, also used by key rotation.
        try (var s = c.prepareStatement("SELECT id FROM online_accounts WHERE access_hash=? FOR SHARE")) {
          s.setString(1, verifier);
          try (var rows = s.executeQuery()) {
            if (!rows.next()) throw new Conflict("Account or character unavailable");
            account = rows.getObject(1, UUID.class);
          }
        }
        UUID lease = UUID.randomUUID();
        try (var s = c.prepareStatement("""
            UPDATE online_characters SET session_id=?, session_until=clock_timestamp()+interval '30 seconds'
            WHERE id=? AND account_id=? AND (session_id IS NULL OR session_until<=clock_timestamp()) RETURNING *
            """)) {
          s.setObject(1, lease); s.setObject(2, character); s.setObject(3, account);
          try (var rows = s.executeQuery()) {
            if (!rows.next()) throw new Conflict("Account or character unavailable");
            var session = new Session(account, character, lease, read(rows));
            c.commit(); return session;
          }
        }
      } catch (SQLException | RuntimeException error) { c.rollback(); throw error; }
    }
  }

  public void renew(Session session) throws SQLException {
    try (var c = connections.open(); var s = c.prepareStatement("""
        UPDATE online_characters SET session_until=clock_timestamp()+interval '30 seconds'
        WHERE id=? AND account_id=? AND session_id=? AND session_until>clock_timestamp()
        """)) {
      bindSession(s, session, 1);
      if (s.executeUpdate()!=1) throw new Conflict("Session expired or replaced");
    }
  }

  public void release(Session session) throws SQLException {
    try (var c = connections.open(); var s = c.prepareStatement("""
        UPDATE online_characters SET session_id=NULL, session_until=NULL
        WHERE id=? AND account_id=? AND session_id=?
        """)) {
      bindSession(s, session, 1); s.executeUpdate();
    }
  }

  public CharacterState create(UUID account, String name, int world, double x, double y) throws SQLException {
    return create(account,name,world,x,y,null,0);
  }

  /** Trusted creation only; initial kit and gold are inserted together, never granted on login. */
  public CharacterState create(UUID account, String name, int world, double x, double y,
      CharacterProgress progress, long startingGold) throws SQLException {
    Objects.requireNonNull(account);
    if (name == null || name.isBlank() || name.length() > 32) throw new IllegalArgumentException("Invalid character name");
    position(world, x, y);
    if (startingGold<0 || (progress==null && startingGold!=0)) throw new IllegalArgumentException("Invalid starting balance");
    try (var c = connections.open(); var s = c.prepareStatement("""
        INSERT INTO online_characters(id, account_id, name, world, x, y, progress, gold) VALUES (?, ?, ?, ?, ?, ?, ?::jsonb, ?) RETURNING *
        """)) {
      s.setObject(1, UUID.randomUUID()); s.setObject(2, account); s.setString(3, name);
      s.setInt(4, world); s.setDouble(5, x); s.setDouble(6, y);
      s.setString(7,progress==null ? null : encodeProgress(progress)); s.setLong(8,startingGold);
      try (var rows = s.executeQuery()) { rows.next(); return read(rows); }
    }
  }

  public CharacterState load(UUID account, UUID character) throws SQLException {
    try (var c = connections.open()) { return load(c, account, character, false); }
  }

  /** Compare-and-set: an old checkpoint cannot overwrite a later checkpoint or reward. */
  public CharacterState checkpoint(Session session, long revision, int world, double x, double y) throws SQLException {
    position(world, x, y);
    try (var c = connections.open(); var s = c.prepareStatement("""
        UPDATE online_characters SET world=?, x=?, y=?, revision=revision+1, updated_at=now()
        WHERE id=? AND account_id=? AND session_id=? AND session_until>clock_timestamp() AND revision=? RETURNING *
        """)) {
      s.setInt(1, world); s.setDouble(2, x); s.setDouble(3, y);
      bindSession(s, session, 4); s.setLong(7, revision);
      try (var rows = s.executeQuery()) {
        if (!rows.next()) throw new Conflict("Character unavailable or stale checkpoint");
        return read(rows);
      }
    }
  }

  /** Atomic gameplay/location checkpoint. Does not overwrite the character's gold or experience. */
  public CharacterState checkpointCharacter(Session session, long revision, int world, double x, double y,
      CharacterProgress progress) throws SQLException {
    position(world,x,y);
    String encoded=encodeProgress(progress);
    try (var c=connections.open(); var s=c.prepareStatement("""
        UPDATE online_characters SET world=?, x=?, y=?, progress=?::jsonb, revision=revision+1, updated_at=now()
        WHERE id=? AND account_id=? AND session_id=? AND session_until>clock_timestamp() AND revision=? RETURNING *
        """)) {
      s.setInt(1,world); s.setDouble(2,x); s.setDouble(3,y); s.setString(4,encoded);
      bindSession(s,session,5); s.setLong(8,revision);
      try (var rows=s.executeQuery()) {
        if (!rows.next()) throw new Conflict("Character unavailable or stale checkpoint");
        return read(rows);
      }
    }
  }

  /** Event IDs and amounts must originate from authoritative gameplay, never a client request. */
  public RewardResult reward(Session session, UUID event, long gold, long experience) throws SQLException {
    return applyReward(session,event,gold,experience,null,null,false,null);
  }

  /** Server-owned quest/loot/progression result; receipt, balances and gameplay state commit once. */
  public RewardResult commitGameplayEvent(Session session, long revision, UUID event, long gold,
      long experience, CharacterProgress progress) throws SQLException {
    if (revision<0) throw new IllegalArgumentException("Invalid revision");
    return applyReward(session,event,gold,experience,revision,Objects.requireNonNull(progress),false,null);
  }

  /** One recipient across the realm for a durable server-generated world event identity. */
  public RewardResult commitWorldReward(Session session, long revision, UUID event, long gold,
      long experience, CharacterProgress progress) throws SQLException {
    if (revision < 0) throw new IllegalArgumentException("Invalid revision");
    return applyReward(session,event,gold,experience,revision,Objects.requireNonNull(progress),true,null);
  }

  /** Damage, XP/items/cooldowns and (for lethal hits) the life claim share one transaction. */
  public RewardResult commitCreatureStrike(Session session, long revision, UUID event, long gold,
      long experience, CharacterProgress progress, CreatureStrike strike) throws SQLException {
    if (revision<0) throw new IllegalArgumentException("Invalid revision");
    Objects.requireNonNull(strike); Objects.requireNonNull(event);
    boolean lethal=strike.after()==0;
    if (lethal!=event.equals(strike.life())) throw new IllegalArgumentException("Invalid strike event identity");
    return applyReward(session,event,gold,experience,revision,Objects.requireNonNull(progress),lethal,strike);
  }

  private RewardResult applyReward(Session session, UUID event, long gold, long experience,
      Long revision, CharacterProgress progress, boolean worldReward, CreatureStrike strike) throws SQLException {
    return applyReward(session,event,gold,experience,revision,progress,worldReward,strike,null);
  }

  public RewardResult commitGameplayEvent(Session session, long revision, UUID event, long gold,
      long experience, CharacterProgress progress, CreatureProvocation provocation) throws SQLException {
    if(revision<0) throw new IllegalArgumentException("Invalid revision");
    return applyReward(session,event,gold,experience,revision,Objects.requireNonNull(progress),false,null,provocation);
  }

  public RewardResult commitCreatureStrike(Session session, long revision, UUID event, long gold,
      long experience, CharacterProgress progress, CreatureStrike strike, CreatureProvocation provocation) throws SQLException {
    return commitCreatureStrike(session,revision,event,gold,experience,progress,strike,provocation,java.util.List.of());
  }

  /**
   * A lethal strike also commits its rolled ground drops in the same transaction as the reward. Drops
   * are never accepted for a hit that does not kill: they would have no source life to remain attached to.
   */
  public RewardResult commitCreatureStrike(Session session, long revision, UUID event, long gold,
      long experience, CharacterProgress progress, CreatureStrike strike, CreatureProvocation provocation,
      java.util.List<LootDropDraft> drops) throws SQLException {
    if(revision<0) throw new IllegalArgumentException("Invalid revision");
    Objects.requireNonNull(strike); Objects.requireNonNull(event); Objects.requireNonNull(drops);
    boolean lethal=strike.after()==0;
    if(lethal!=event.equals(strike.life())) throw new IllegalArgumentException("Invalid strike event identity");
    if(provocation!=null && (!provocation.key().equals(strike.key()) || !provocation.life().equals(strike.life()))) throw new IllegalArgumentException("Different provoked life");
    if(!drops.isEmpty() && !lethal) throw new IllegalArgumentException("Loot requires a lethal strike");
    return applyReward(session,event,gold,experience,revision,Objects.requireNonNull(progress),lethal,strike,provocation,drops);
  }

  private RewardResult applyReward(Session session, UUID event, long gold, long experience,
      Long revision, CharacterProgress progress, boolean worldReward, CreatureStrike strike,
      CreatureProvocation provocation) throws SQLException {
    return applyReward(session,event,gold,experience,revision,progress,worldReward,strike,provocation,java.util.List.of());
  }

  private RewardResult applyReward(Session session, UUID event, long gold, long experience,
      Long revision, CharacterProgress progress, boolean worldReward, CreatureStrike strike,
      CreatureProvocation provocation, java.util.List<LootDropDraft> drops) throws SQLException {
    Objects.requireNonNull(event);
    if(provocation!=null && !provocation.targetCharacter().equals(session.characterId())) throw new IllegalArgumentException("Invalid provocation owner");
    if (gold < 0 || experience < 0) throw new IllegalArgumentException("Negative reward");
    drops=java.util.List.copyOf(drops);
    for (int i=0;i<drops.size();i++) if (drops.get(i).index()!=i) throw new IllegalArgumentException("Invalid loot index");
    if (!drops.isEmpty() && (!worldReward || strike==null || strike.after()!=0)) throw new IllegalArgumentException("Loot requires a lethal strike");
    String encoded=progress==null ? null : encodeProgress(progress);
    String base=encoded==null ? null : strike==null ? encoded
        : encoded+"\ncreature-strike-v1\n"+JSON.toJson(canonical(JSON.toJsonTree(strike)));
    // Only a kill with drops adds this segment, so every gold-only receipt keeps its existing hash.
    if (base!=null && !drops.isEmpty()) base=base+"\nloot-drops-v1\n"+JSON.toJson(canonical(JSON.toJsonTree(drops)));
    String fingerprint=base==null ? null : fingerprint(base);
    if(provocation!=null) fingerprint=fingerprint(fingerprint+"\nprovocation-v1\n"+JSON.toJson(provocation));
    SpawnMotion motion=strike!=null && strike.motion()!=null ? strike.motion()
        : provocation==null ? null : provocation.motion();
    if (strike!=null && strike.motion()!=null && provocation!=null && provocation.motion()!=null
        && !strike.motion().equals(provocation.motion())) throw new IllegalArgumentException("Different creature poses");
    if (motion!=null && (strike!=null && (!motion.key().equals(strike.key()) || !motion.life().equals(strike.life()))
        || provocation!=null && (!motion.key().equals(provocation.key()) || !motion.life().equals(provocation.life()))))
      throw new IllegalArgumentException("Different creature motion life");
    if (motion!=null && motion.targetCharacter()!=null && !motion.targetCharacter().equals(session.characterId()))
      throw new IllegalArgumentException("Invalid creature target epoch");
    if (motion!=null && strike!=null && strike.after()==0)
      for (var drop:drops) if (Math.abs(drop.x()-motion.x())>1e-7 || Math.abs(drop.y()-motion.y())>1e-7)
        throw new IllegalArgumentException("Drop is not at creature death position");
    UUID account = session.accountId(), character = session.characterId();
    try (var c = connections.open()) {
      c.setAutoCommit(false);
      try {
        CharacterState state;
        try (var s = c.prepareStatement("""
            SELECT * FROM online_characters WHERE id=? AND account_id=? AND session_id=?
            AND session_until>clock_timestamp() FOR UPDATE
            """)) {
          bindSession(s, session, 1);
          try (var rows = s.executeQuery()) {
            if (!rows.next()) throw new Conflict("Session expired or replaced");
            state = read(rows);
          }
        }
        boolean claimed = false;
        if (worldReward) {
          try (var s = c.prepareStatement("SELECT character_id FROM online_world_rewards WHERE event_id=?")) {
            s.setObject(1,event);
            try (var rows = s.executeQuery()) {
              if (rows.next()) {
                if (!character.equals(rows.getObject(1,UUID.class))) throw new Conflict("World event already awarded");
                claimed = true;
              }
            }
          }
        }
        try (var s = c.prepareStatement("SELECT gold, experience, payload_hash FROM online_rewards WHERE character_id=? AND event_id=?")) {
          s.setObject(1, character); s.setObject(2, event);
          try (var rows = s.executeQuery()) {
            if (rows.next()) {
              if (worldReward && !claimed) throw new Conflict("Event was not a world reward");
              if (rows.getLong(1) != gold || rows.getLong(2) != experience
                  || !Objects.equals(fingerprint,rows.getString(3))) throw new Conflict("Reward event payload mismatch");
              c.commit(); return new RewardResult(false, state);
            }
          }
        }
        if (revision!=null && state.revision()!=revision) throw new Conflict("Stale gameplay event");
        boolean matchedSpawn=false;
        if(worldReward && strike==null) {
          // Character lock precedes a possible spawn lock on every PLAYER claim path.
          // Take it before either reward insert, matching the physical lethal path.
          try(var q=c.prepareStatement("SELECT died_at FROM online_spawns WHERE event_id=? FOR UPDATE")) {
            q.setObject(1,event);
            try(var rows=q.executeQuery()) {
              if(rows.next()) {
                if(rows.getTimestamp(1)!=null) throw new Conflict("World spawn life already terminal");
                matchedSpawn=true;
              }
            }
          }
        }
        if (motion!=null) {
          if (motion.targetCharacter()!=null && (motion.targetEpoch()==null
              || state.locationEpoch()!=motion.targetEpoch())) throw new Conflict("Creature target epoch changed");
          try(var s=c.prepareStatement("""
              UPDATE online_spawns SET current_x=?,current_y=?,mode=?,target_character=?,target_epoch=?,
                target_spawn_key=?,target_life=?,
                next_attack_at=CASE WHEN ? AND target_character IS NULL THEN ? ELSE next_attack_at END,
                state_revision=state_revision+1
              WHERE spawn_key=? AND event_id=? AND state_revision=? AND died_at IS NULL AND COALESCE(health,1)>0
              """)) {
            s.setDouble(1,motion.x());s.setDouble(2,motion.y());
            s.setString(3,strike!=null && strike.after()==0 ? "IDLE" : motion.mode());
            s.setObject(4,strike!=null && strike.after()==0 ? null : motion.targetCharacter());
            s.setObject(5,strike!=null && strike.after()==0 ? null : motion.targetEpoch());
            s.setString(6,strike!=null && strike.after()==0 ? null : motion.targetSpawnKey());
            s.setObject(7,strike!=null && strike.after()==0 ? null : motion.targetLife());
            s.setBoolean(8,provocation!=null);s.setLong(9,provocation==null ? 0 : provocation.nextAttackAt());
            s.setString(10,motion.key());s.setObject(11,motion.life());s.setLong(12,motion.revision());
            if(s.executeUpdate()!=1) throw new Conflict("Creature life unavailable");
          }
        } else if(provocation!=null) {
          try(var s=c.prepareStatement("UPDATE online_spawns SET target_character=?,target_epoch=?,target_spawn_key=NULL,target_life=NULL,mode='RETALIATION',state_revision=state_revision+1,next_attack_at=CASE WHEN target_character IS NULL THEN ? ELSE next_attack_at END WHERE spawn_key=? AND event_id=? AND current_x IS NULL AND died_at IS NULL AND COALESCE(health,1)>0")) {
            s.setObject(1,provocation.targetCharacter()); s.setLong(2,state.locationEpoch()); s.setLong(3,provocation.nextAttackAt()); s.setString(4,provocation.key()); s.setObject(5,provocation.life());
            if(s.executeUpdate()!=1) throw new Conflict("Creature life unavailable");
          }
        }
        if (strike!=null) {
          // Character lock precedes spawn lock. CAS prevents two attackers rewarding the same damage.
          try (var s=c.prepareStatement("""
              UPDATE online_spawns SET health=?,state_revision=state_revision+CASE WHEN ? THEN 0 ELSE 1 END,
                died_at=CASE WHEN ? THEN clock_timestamp() ELSE died_at END,
                death_kind=CASE WHEN ? THEN 'PLAYER' ELSE death_kind END,
                target_character=CASE WHEN ? THEN NULL ELSE target_character END,
                target_epoch=CASE WHEN ? THEN NULL ELSE target_epoch END,
                target_spawn_key=CASE WHEN ? THEN NULL ELSE target_spawn_key END,
                target_life=CASE WHEN ? THEN NULL ELSE target_life END,
                next_attack_at=CASE WHEN ? THEN 0 ELSE next_attack_at END,
                mode=CASE WHEN ? THEN 'IDLE' ELSE mode END
              WHERE spawn_key=? AND event_id=? AND died_at IS NULL AND COALESCE(health,?)=? AND (? OR current_x IS NULL)
              """)) {
            s.setInt(1,strike.after());s.setBoolean(2,motion!=null);
            for(int p=3;p<=10;p++) s.setBoolean(p,strike.after()==0);
            s.setString(11,strike.key()); s.setObject(12,strike.life());
            s.setInt(13,strike.maximumHealth()); s.setInt(14,strike.before());s.setBoolean(15,motion!=null);
            if (s.executeUpdate()!=1) throw new Conflict("Creature life or health changed");
          }
        }
        try (var s = c.prepareStatement("INSERT INTO online_rewards(character_id,event_id,gold,experience,payload_hash) VALUES (?,?,?,?,?)")) {
          s.setObject(1, character); s.setObject(2, event); s.setLong(3, gold); s.setLong(4, experience);
          s.setString(5,fingerprint); s.executeUpdate();
        }
        if (worldReward) {
          try (var s = c.prepareStatement("INSERT INTO online_world_rewards(event_id,character_id) VALUES (?,?) ON CONFLICT (event_id) DO NOTHING")) {
            s.setObject(1,event); s.setObject(2,character);
            if (s.executeUpdate()!=1) throw new Conflict("World event already awarded");
          }
          if (matchedSpawn) {
            // Older trusted world-event callers may claim a spawn life without a CreatureStrike.
            // Keep that claim terminal, and let a competing clan kill reject the whole reward.
            try(var q=c.prepareStatement("""
                UPDATE online_spawns SET health=0,died_at=clock_timestamp(),death_kind='PLAYER',
                  target_character=NULL,target_epoch=NULL,target_spawn_key=NULL,target_life=NULL,
                  next_attack_at=0,mode='IDLE',state_revision=state_revision+1
                WHERE event_id=? AND died_at IS NULL
                """)) {
              q.setObject(1,event);
              if(q.executeUpdate()!=1) throw new Conflict("World spawn life already terminal");
            }
          }
        }
        if (!drops.isEmpty()) {
          // Database time starts both windows, so a rolled-back kill never leaves loot behind.
          try (var s = c.prepareStatement("""
              INSERT INTO online_ground_loot(id,source_event,drop_index,world,x,y,item_key,quantity,charges,owner_character,owner_until,expires_at)
              VALUES (?,?,?,?,?,?,?,?,?,?,clock_timestamp()+(? * interval '1 millisecond'),clock_timestamp()+(? * interval '1 millisecond'))
              """)) {
            for (var drop : drops) {
              s.setObject(1,drop.id()); s.setObject(2,event); s.setInt(3,drop.index()); s.setInt(4,drop.world());
              s.setDouble(5,drop.x()); s.setDouble(6,drop.y()); s.setString(7,drop.key()); s.setInt(8,drop.quantity());
              s.setInt(9,drop.charges()); s.setObject(10,character); s.setLong(11,LOOT_OWNER_WINDOW_MILLIS);
              s.setLong(12,LOOT_LIFETIME_MILLIS); s.addBatch();
            }
            for (int changed : s.executeBatch()) if (changed!=1) throw new Conflict("Loot drop not created");
          }
        }
        try (var s = c.prepareStatement("UPDATE online_characters SET gold=gold+?, experience=experience+?, progress=COALESCE(?::jsonb,progress), revision=revision+1, updated_at=now() WHERE id=?")) {
          s.setLong(1, gold); s.setLong(2, experience); s.setString(3,encoded); s.setObject(4, character); s.executeUpdate();
        }
        state = load(c, account, character, false);
        c.commit(); return new RewardResult(true, state);
      } catch (SQLException | RuntimeException error) { c.rollback(); throw error; }
    }
  }

  /**
   * One spell cast that damaged one or more creatures: caster progress, every creature's pose-guarded
   * health change, every kill's life claim with its drops, and the receipt commit in one transaction.
   * {@code event} is a fresh server id for the cast (the receipt); each lethal strike additionally
   * claims its own life id through {@code online_world_rewards}, which needs a receipt row of the same
   * id, so a kill another attacker already claimed rolls the whole cast back. A repeat of the same event
   * and payload is a harmless replay; the same event with a different payload is a conflict.
   */
  public RewardResult commitSpellStrikes(Session session, long revision, UUID event, long gold, long experience,
      CharacterProgress progress, java.util.List<SpellStrike> strikes) throws SQLException {
    if (revision<0) throw new IllegalArgumentException("Invalid revision");
    Objects.requireNonNull(event); Objects.requireNonNull(progress);
    if (gold<0 || experience<0) throw new IllegalArgumentException("Negative reward");
    strikes=java.util.List.copyOf(strikes);
    if (strikes.isEmpty()) throw new IllegalArgumentException("A spell commit needs at least one strike");
    var keys=new java.util.HashSet<String>();
    for (var hit:strikes) {
      if (!keys.add(hit.strike().key())) throw new IllegalArgumentException("Creature struck twice");
      var motion=hit.strike().motion();
      if (!motion.key().equals(hit.strike().key()) || !motion.life().equals(hit.strike().life()))
        throw new IllegalArgumentException("Different creature motion life");
      if (motion.targetCharacter()!=null && !motion.targetCharacter().equals(session.characterId()))
        throw new IllegalArgumentException("Invalid creature target epoch");
      if (hit.provocation()!=null && !hit.provocation().targetCharacter().equals(session.characterId()))
        throw new IllegalArgumentException("Invalid provocation owner");
      if (hit.provocation()!=null && hit.provocation().motion()!=null && !hit.provocation().motion().equals(motion))
        throw new IllegalArgumentException("Different creature poses");
      for (var drop:hit.drops())
        if (Math.abs(drop.x()-motion.x())>1e-7 || Math.abs(drop.y()-motion.y())>1e-7)
          throw new IllegalArgumentException("Drop is not at creature death position");
    }
    String encoded=encodeProgress(progress);
    String fingerprint=fingerprint(encoded+"\nspell-strikes-v1\n"+JSON.toJson(canonical(JSON.toJsonTree(strikes))));
    UUID account=session.accountId(), character=session.characterId();
    try (var c=connections.open()) {
      c.setAutoCommit(false);
      try {
        var state=lockSession(c,session);
        try (var s=c.prepareStatement("SELECT gold, experience, payload_hash FROM online_rewards WHERE character_id=? AND event_id=?")) {
          s.setObject(1,character); s.setObject(2,event);
          try (var rows=s.executeQuery()) {
            if (rows.next()) {
              if (rows.getLong(1)!=gold || rows.getLong(2)!=experience || !Objects.equals(fingerprint,rows.getString(3)))
                throw new Conflict("Reward event payload mismatch");
              c.commit(); return new RewardResult(false,state);
            }
          }
        }
        if (state.revision()!=revision) throw new Conflict("Stale gameplay event");
        // The cast receipt precedes the per-kill receipts: nothing else references it.
        try (var s=c.prepareStatement("INSERT INTO online_rewards(character_id,event_id,gold,experience,payload_hash) VALUES (?,?,?,?,?)")) {
          s.setObject(1,character); s.setObject(2,event); s.setLong(3,gold); s.setLong(4,experience);
          s.setString(5,fingerprint); s.executeUpdate();
        }
        for (var hit:strikes) {
          var strike=hit.strike(); var motion=strike.motion(); boolean lethal=strike.after()==0;
          if (motion.targetCharacter()!=null && (motion.targetEpoch()==null || state.locationEpoch()!=motion.targetEpoch()))
            throw new Conflict("Creature target epoch changed");
          try (var s=c.prepareStatement("""
              UPDATE online_spawns SET current_x=?,current_y=?,mode=?,target_character=?,target_epoch=?,
                target_spawn_key=?,target_life=?,
                next_attack_at=CASE WHEN ? AND target_character IS NULL THEN ? ELSE next_attack_at END,
                state_revision=state_revision+1
              WHERE spawn_key=? AND event_id=? AND state_revision=? AND died_at IS NULL AND COALESCE(health,1)>0
              """)) {
            s.setDouble(1,motion.x());s.setDouble(2,motion.y());
            s.setString(3,lethal ? "IDLE" : motion.mode());
            s.setObject(4,lethal ? null : motion.targetCharacter());
            s.setObject(5,lethal ? null : motion.targetEpoch());
            s.setString(6,lethal ? null : motion.targetSpawnKey());
            s.setObject(7,lethal ? null : motion.targetLife());
            s.setBoolean(8,hit.provocation()!=null);s.setLong(9,hit.provocation()==null ? 0 : hit.provocation().nextAttackAt());
            s.setString(10,motion.key());s.setObject(11,motion.life());s.setLong(12,motion.revision());
            if (s.executeUpdate()!=1) throw new Conflict("Creature life unavailable");
          }
          try (var s=c.prepareStatement("""
              UPDATE online_spawns SET health=?,state_revision=state_revision+CASE WHEN ? THEN 0 ELSE 1 END,
                died_at=CASE WHEN ? THEN clock_timestamp() ELSE died_at END,
                death_kind=CASE WHEN ? THEN 'PLAYER' ELSE death_kind END,
                target_character=CASE WHEN ? THEN NULL ELSE target_character END,
                target_epoch=CASE WHEN ? THEN NULL ELSE target_epoch END,
                target_spawn_key=CASE WHEN ? THEN NULL ELSE target_spawn_key END,
                target_life=CASE WHEN ? THEN NULL ELSE target_life END,
                next_attack_at=CASE WHEN ? THEN 0 ELSE next_attack_at END,
                mode=CASE WHEN ? THEN 'IDLE' ELSE mode END
              WHERE spawn_key=? AND event_id=? AND died_at IS NULL AND COALESCE(health,?)=? AND (? OR current_x IS NULL)
              """)) {
            s.setInt(1,strike.after());s.setBoolean(2,true);
            for (int p=3;p<=10;p++) s.setBoolean(p,lethal);
            s.setString(11,strike.key()); s.setObject(12,strike.life());
            s.setInt(13,strike.maximumHealth()); s.setInt(14,strike.before());s.setBoolean(15,true);
            if (s.executeUpdate()!=1) throw new Conflict("Creature life or health changed");
          }
          if (!lethal) continue;
          try (var s=c.prepareStatement("INSERT INTO online_rewards(character_id,event_id,gold,experience,payload_hash) VALUES (?,?,0,0,?)")) {
            s.setObject(1,character); s.setObject(2,strike.life()); s.setString(3,fingerprint); s.executeUpdate();
          }
          try (var s=c.prepareStatement("INSERT INTO online_world_rewards(event_id,character_id) VALUES (?,?) ON CONFLICT (event_id) DO NOTHING")) {
            s.setObject(1,strike.life()); s.setObject(2,character);
            if (s.executeUpdate()!=1) throw new Conflict("World event already awarded");
          }
          if (!hit.drops().isEmpty()) {
            try (var s=c.prepareStatement("""
                INSERT INTO online_ground_loot(id,source_event,drop_index,world,x,y,item_key,quantity,charges,owner_character,owner_until,expires_at)
                VALUES (?,?,?,?,?,?,?,?,?,?,clock_timestamp()+(? * interval '1 millisecond'),clock_timestamp()+(? * interval '1 millisecond'))
                """)) {
              for (var drop:hit.drops()) {
                s.setObject(1,drop.id()); s.setObject(2,strike.life()); s.setInt(3,drop.index()); s.setInt(4,drop.world());
                s.setDouble(5,drop.x()); s.setDouble(6,drop.y()); s.setString(7,drop.key()); s.setInt(8,drop.quantity());
                s.setInt(9,drop.charges()); s.setObject(10,character); s.setLong(11,LOOT_OWNER_WINDOW_MILLIS);
                s.setLong(12,LOOT_LIFETIME_MILLIS); s.addBatch();
              }
              for (int changed:s.executeBatch()) if (changed!=1) throw new Conflict("Loot drop not created");
            }
          }
        }
        try (var s=c.prepareStatement("UPDATE online_characters SET gold=gold+?, experience=experience+?, progress=?::jsonb, revision=revision+1, updated_at=now() WHERE id=?")) {
          s.setLong(1,gold); s.setLong(2,experience); s.setString(3,encoded); s.setObject(4,character); s.executeUpdate();
        }
        state=load(c,account,character,false);
        c.commit(); return new RewardResult(true,state);
      } catch (SQLException | RuntimeException error) { c.rollback(); throw error; }
    }
  }

  /** Persist one server-resolved retaliation, including a fatal move/penalty/corpse, exactly once. */
  public RewardResult commitMonsterAttack(Session session, long revision, UUID event,
      CharacterProgress progress, MonsterAttack attack, DeathTransition death) throws SQLException {
    Objects.requireNonNull(event); Objects.requireNonNull(attack);
    if(revision<0 || !attack.targetCharacter().equals(session.characterId())) throw new IllegalArgumentException("Invalid attack owner");
    String encoded=encodeProgress(progress);
    String digest=fingerprint(encoded+"\nmonster-attack-v1\n"+JSON.toJson(attack)+"\n"+JSON.toJson(death));
    try(var c=connections.open()) {
      c.setAutoCommit(false);
      try {
        var state=lockSession(c,session);
        try(var q=c.prepareStatement("SELECT character_id,payload_hash FROM online_combat_events WHERE event_id=?")) {
          q.setObject(1,event);
          try(var rows=q.executeQuery()) { if(rows.next()) {
            if(!session.characterId().equals(rows.getObject(1,UUID.class)) || !digest.equals(rows.getString(2))) throw new Conflict("Combat event payload mismatch");
            c.commit(); return new RewardResult(false,state);
          } }
        }
        if(state.revision()!=revision) throw new Conflict("Stale combat event");
        if(death!=null) lockTargetSpawns(c,session.characterId(),attack.key());
        if (attack.motion()!=null) {
          var motion=attack.motion();
          if (!motion.key().equals(attack.key()) || !motion.life().equals(attack.life())
              || !attack.targetCharacter().equals(motion.targetCharacter())
              || !Objects.equals(motion.targetEpoch(),state.locationEpoch()))
            throw new Conflict("Creature attack target epoch changed");
          try(var q=c.prepareStatement("""
              UPDATE online_spawns SET current_x=?,current_y=?,mode=?,target_epoch=?,next_attack_at=?,
                state_revision=state_revision+1
              WHERE spawn_key=? AND event_id=? AND state_revision=? AND target_character=?
                AND next_attack_at=? AND next_attack_at<=floor(extract(epoch FROM clock_timestamp())*1000)
                AND died_at IS NULL AND COALESCE(health,1)>0
              """)) {
            q.setDouble(1,motion.x());q.setDouble(2,motion.y());q.setString(3,motion.mode());
            q.setLong(4,motion.targetEpoch());q.setLong(5,attack.nextNext());
            q.setString(6,attack.key());q.setObject(7,attack.life());q.setLong(8,motion.revision());
            q.setObject(9,attack.targetCharacter());q.setLong(10,attack.expectedNext());
            if(q.executeUpdate()!=1) throw new Conflict("Creature attack changed or not due");
          }
        } else {
          try(var q=c.prepareStatement("UPDATE online_spawns SET next_attack_at=?,state_revision=state_revision+1 WHERE spawn_key=? AND event_id=? AND current_x IS NULL AND target_character=? AND next_attack_at=? AND next_attack_at<=floor(extract(epoch FROM clock_timestamp())*1000) AND died_at IS NULL AND COALESCE(health,1)>0")) {
            q.setLong(1,attack.nextNext());q.setString(2,attack.key());q.setObject(3,attack.life());
            q.setObject(4,attack.targetCharacter());q.setLong(5,attack.expectedNext());
            if(q.executeUpdate()!=1) throw new Conflict("Creature attack changed or not due");
          }
        }
        long goldLost=death==null ? 0 : death.goldLost(),xpLost=death==null ? 0 : death.experienceLost();
        if(goldLost>state.gold() || xpLost>state.experience()) throw new Conflict("Invalid death balances");
        if(death!=null) validateDeathInventory(state.progress(),progress,death.droppedItems());
        try(var q=c.prepareStatement("INSERT INTO online_combat_events(event_id,character_id,payload_hash) VALUES(?,?,?)")) {
          q.setObject(1,event);q.setObject(2,session.characterId());q.setString(3,digest);q.executeUpdate();
        }
        var destination=death==null ? new CharacterProgress.Respawn(state.world(),state.x(),state.y()) : death.destination();
        try(var q=c.prepareStatement("UPDATE online_characters SET world=?,x=?,y=?,gold=gold-?,experience=experience-?,progress=?::jsonb,revision=revision+1,location_epoch=location_epoch+"+(death==null ? 0 : 1)+",updated_at=now() WHERE id=?")) {
          q.setInt(1,destination.world());q.setDouble(2,destination.x());q.setDouble(3,destination.y());q.setLong(4,goldLost);q.setLong(5,xpLost);q.setString(6,encoded);q.setObject(7,session.characterId());q.executeUpdate();
        }
        if(death!=null) {
          if(!death.droppedItems().isEmpty() || death.goldDropped()>0) {
            try(var q=c.prepareStatement("INSERT INTO online_corpses(id,character_id,world,x,y,items,gold) VALUES(?,?,?,?,?,?::jsonb,?)")) {
              q.setObject(1,event);q.setObject(2,session.characterId());q.setInt(3,death.corpsePosition().world());q.setDouble(4,death.corpsePosition().x());q.setDouble(5,death.corpsePosition().y());q.setString(6,JSON.toJson(death.droppedItems()));q.setLong(7,death.goldDropped());q.executeUpdate();
            }
          }
          try(var q=c.prepareStatement("UPDATE online_spawns SET target_character=NULL,target_epoch=NULL,mode='RETURN',next_attack_at=0,state_revision=state_revision+1 WHERE target_character=?")) { q.setObject(1,session.characterId());q.executeUpdate(); }
        }
        state=load(c,session.accountId(),session.characterId(),false);c.commit();return new RewardResult(true,state);
      } catch(SQLException|RuntimeException failure) { c.rollback();throw failure; }
    }
  }

  /** Atomic server-authored portal movement, with durable epoch fencing and receipt replay. */
  public RewardResult commitTransfer(Session session,long revision,UUID event,PortalTransfer transfer) throws SQLException {
    Objects.requireNonNull(event);Objects.requireNonNull(transfer);
    if(revision<0) throw new IllegalArgumentException("Invalid revision");
    String digest=fingerprint(JSON.toJson(transfer));
    try(var c=connections.open()) {
      c.setAutoCommit(false);
      try {
        var state=lockSession(c,session);
        try(var q=c.prepareStatement("SELECT character_id,payload_hash FROM online_transfer_events WHERE event_id=?")) {
          q.setObject(1,event);try(var rows=q.executeQuery()) { if(rows.next()) {
            if(!session.characterId().equals(rows.getObject(1,UUID.class)) || !digest.equals(rows.getString(2))) throw new Conflict("Transfer event payload mismatch");
            c.commit();return new RewardResult(false,state);
          } }
        }
        if(state.revision()!=revision || state.locationEpoch()!=transfer.expectedEpoch() || state.world()!=transfer.source().world())
          throw new Conflict("Stale transfer");
        try(var q=c.prepareStatement("SELECT portal_cooldown_until<=floor(extract(epoch FROM clock_timestamp())*1000) FROM online_characters WHERE id=?")) {
          q.setObject(1,state.id());try(var rows=q.executeQuery()) { rows.next();if(!rows.getBoolean(1)) throw new Conflict("Portal cooldown"); }
        }
        Math.addExact(state.locationEpoch(),1);
        try(var q=c.prepareStatement("INSERT INTO online_transfer_events(event_id,character_id,payload_hash) VALUES(?,?,?)")) {
          q.setObject(1,event);q.setObject(2,state.id());q.setString(3,digest);q.executeUpdate();
        }
        var destination=transfer.destination();
        try(var q=c.prepareStatement("UPDATE online_characters SET world=?,x=?,y=?,location_epoch=location_epoch+1,portal_cooldown_until=?,revision=revision+1,updated_at=now() WHERE id=?")) {
          q.setInt(1,destination.world());q.setDouble(2,destination.x());q.setDouble(3,destination.y());q.setLong(4,transfer.cooldownUntil());q.setObject(5,state.id());q.executeUpdate();
        }
        lockTargetSpawns(c,state.id(),null);
        try(var q=c.prepareStatement("UPDATE online_spawns SET target_character=NULL,target_epoch=NULL,mode='RETURN',next_attack_at=0,state_revision=state_revision+1 WHERE target_character=?")) { q.setObject(1,state.id());q.executeUpdate(); }
        state=load(c,session.accountId(),session.characterId(),false);c.commit();return new RewardResult(true,state);
      } catch(SQLException|RuntimeException failure) { c.rollback();throw failure; }
    }
  }

  private static CharacterState lockSession(Connection c,Session session) throws SQLException {
    try(var q=c.prepareStatement("SELECT * FROM online_characters WHERE id=? AND account_id=? AND session_id=? AND session_until>clock_timestamp() FOR UPDATE")) {
      bindSession(q,session,1);try(var rows=q.executeQuery()) { if(!rows.next()) throw new Conflict("Session expired or replaced");return read(rows); }
    }
  }

  /** Character row is already locked; acquire all affected spawn rows in stable key order. */
  private static void lockTargetSpawns(Connection c,UUID character,String extraKey) throws SQLException {
    try(var q=c.prepareStatement("SELECT spawn_key FROM online_spawns WHERE target_character=? OR spawn_key=? ORDER BY spawn_key FOR UPDATE")) {
      q.setObject(1,character);q.setString(2,extraKey);
      try(var rows=q.executeQuery()) { while(rows.next()) rows.getString(1); }
    }
  }

  private static void validateDeathInventory(CharacterProgress before,CharacterProgress after,java.util.List<CharacterProgress.Item> drops) throws Conflict {
    if(before==null || !before.equipment().equals(after.equipment()) || !before.storage().equals(after.storage())) throw new Conflict("Invalid death inventory");
    var actual=new java.util.HashMap<String,Long>();var expected=new java.util.HashMap<String,Long>();
    for(var item:before.inventory()) expected.merge(item.key()+"\n"+item.charges(),(long)item.quantity(),Math::addExact);
    var ids=new java.util.HashSet<UUID>();
    for(var item:after.inventory()) { ids.add(item.id()); actual.merge(item.key()+"\n"+item.charges(),(long)item.quantity(),Math::addExact); }
    for(var item:after.storage()) ids.add(item.id());
    for(var item:drops) { if(!ids.add(item.id())) throw new Conflict("Duplicate dropped item");actual.merge(item.key()+"\n"+item.charges(),(long)item.quantity(),Math::addExact); }
    if(!actual.equals(expected)) throw new Conflict("Death must preserve owned item quantities and charges");
  }

  /** Bounded owner-only retrieval. No account keys, sessions or other players' inventories leave storage. */
  public java.util.List<CorpseState> listCorpses(Session session) throws SQLException {
    try(var c=connections.open()) {
      c.setAutoCommit(false);
      try {
        lockSession(c,session);
        var result=new java.util.ArrayList<CorpseState>();
        try(var q=c.prepareStatement("SELECT * FROM online_corpses WHERE character_id=? AND NOT recovered ORDER BY created_at,id LIMIT 256")) {
          q.setObject(1,session.characterId());try(var rows=q.executeQuery()) { while(rows.next()) result.add(readCorpse(rows)); }
        }
        c.commit();return java.util.List.copyOf(result);
      } catch(SQLException|RuntimeException failure) { c.rollback();throw failure; }
    }
  }

  /** Server-owned carry limits for corpse recovery. Weight is per single unit; unknown keys weigh nothing. */
  public interface CarryRules {
    long maximumWeight(CharacterProgress progress);
    long unitWeight(String key);
  }
  public static final String CORPSE_TOO_HEAVY="too_heavy";
  public static final String CORPSE_ITEM_UNAVAILABLE="item_unavailable";

  /**
   * Trusted simulation position is frozen while this command runs; never accept client coordinates.
   * Items are taken under the shared carry-weight rule. With {@code itemId} the one named stack is taken
   * or the call is refused with the corpse unchanged. Without it, every stack that fits is taken in
   * order and the rest stays on the corpse; only a call that moves nothing is refused. The first
   * successful call also takes the corpse gold, which weighs nothing. The corpse is marked recovered
   * once it is empty. A repeat of an already-applied request fails the revision fence instead of
   * taking more.
   */
  public RewardResult recoverCorpse(Session session,long revision,UUID corpseId,UUID itemId,int world,double x,double y,CarryRules rules) throws SQLException {
    Objects.requireNonNull(corpseId);Objects.requireNonNull(rules);position(world,x,y);
    if(revision<0) throw new IllegalArgumentException("Invalid revision");
    try(var c=connections.open()) {
      c.setAutoCommit(false);
      try {
        var state=lockSession(c,session);CorpseState corpse;
        try(var q=c.prepareStatement("SELECT * FROM online_corpses WHERE id=? AND character_id=? FOR UPDATE")) {
          q.setObject(1,corpseId);q.setObject(2,session.characterId());try(var rows=q.executeQuery()) { if(!rows.next()) throw new Conflict("Corpse unavailable");corpse=readCorpse(rows); }
        }
        if(corpse.recovered()) { c.commit();return new RewardResult(false,state); }
        if(state.revision()!=revision) throw new Conflict("Stale corpse recovery");
        if(corpse.world()!=world || Math.hypot((x-corpse.x())*com.perso.T4C.config.GameConstants.GRID_W,(y-corpse.y())*com.perso.T4C.config.GameConstants.GRID_H)>com.perso.T4C.config.GameConstants.PLAYER_MELEE_ATTACK_RANGE) throw new Conflict("Corpse out of reach");
        var p=Objects.requireNonNull(state.progress());var inventory=new java.util.ArrayList<>(p.inventory());
        var candidates=new java.util.ArrayList<CharacterProgress.Item>();
        if(itemId==null) candidates.addAll(corpse.items());
        else corpse.items().stream().filter(item -> item.id().equals(itemId)).findFirst().ifPresent(candidates::add);
        if(itemId!=null && candidates.isEmpty()) throw new Conflict(CORPSE_ITEM_UNAVAILABLE);
        long carried=0L;
        for(var item:inventory) carried=Math.addExact(carried,Math.multiplyExact(Math.max(0L,rules.unitWeight(item.key())),(long)item.quantity()));
        long maximum=rules.maximumWeight(p);
        var remaining=new java.util.ArrayList<>(corpse.items());int taken=0;
        for(var item:candidates) {
          long weight=Math.multiplyExact(Math.max(0L,rules.unitWeight(item.key())),(long)item.quantity());
          if(!com.perso.T4C.item.CarryCapacityRules.canAdd(carried,weight,maximum)) continue;
          carried+=weight;inventory.add(item);remaining.remove(item);taken++;
        }
        if(taken==0 && !candidates.isEmpty() && (itemId!=null || corpse.gold()==0)) throw new Conflict(CORPSE_TOO_HEAVY);
        var progress=new CharacterProgress(p.version(),p.characterClass(),p.gender(),p.attributes(),p.vitals(),p.advancement(),inventory,p.equipment(),p.skills(),p.spells(),p.questFlags(),p.storage(),p.storageGold(),p.respawn(),p.buffs(),p.cooldowns(),p.hiddenUntil(),p.companion());
        long gold=Math.addExact(state.gold(),corpse.gold());
        try(var q=c.prepareStatement("UPDATE online_characters SET gold=?,progress=?::jsonb,revision=revision+1,updated_at=now() WHERE id=?")) {
          q.setLong(1,gold);q.setString(2,encodeProgress(progress));q.setObject(3,session.characterId());q.executeUpdate();
        }
        try(var q=c.prepareStatement("UPDATE online_corpses SET items=?::jsonb,gold=0,recovered=? WHERE id=?")) { q.setString(1,JSON.toJson(remaining));q.setBoolean(2,remaining.isEmpty());q.setObject(3,corpseId);q.executeUpdate(); }
        state=load(c,session.accountId(),session.characterId(),false);c.commit();return new RewardResult(true,state);
      } catch(SQLException|RuntimeException failure) { c.rollback();throw failure; }
    }
  }

  /**
   * Atomic pickup of one ground drop. The receipt is bound to this character, this loot and this event.
   * A replay returns the committed result before the revision check. Every refusal raises before any
   * write, so the loot and the inventory stay exactly as they were.
   */
  public RewardResult claimGroundLoot(Session session, long revision, UUID event, UUID lootId,
      int world, double x, double y, ItemContent content) throws SQLException {
    Objects.requireNonNull(session); Objects.requireNonNull(event); Objects.requireNonNull(lootId); Objects.requireNonNull(content);
    position(world,x,y);
    if (revision<0) throw new IllegalArgumentException("Invalid revision");
    String digest=fingerprint("ground-loot-claim-v1\n"+lootId);
    try (var c=connections.open()) {
      c.setAutoCommit(false);
      try {
        var state=lockSession(c,session);
        try (var q=c.prepareStatement("SELECT character_id,loot_id,payload_hash FROM online_loot_claims WHERE event_id=?")) {
          q.setObject(1,event);
          try (var rows=q.executeQuery()) { if (rows.next()) {
            if (!session.characterId().equals(rows.getObject(1,UUID.class)) || !lootId.equals(rows.getObject(2,UUID.class))
                || !digest.equals(rows.getString(3))) throw new Conflict("Loot claim payload mismatch");
            c.commit(); return new RewardResult(false,state);
          } }
        }
        if (state.revision()!=revision) throw new Conflict("revision");
        var progress=state.progress();
        if (progress==null) throw new Conflict(OnlinePickupRules.Refusal.LOOT_UNAVAILABLE.code());
        // Character lock precedes loot lock. Times are read in the same statement as the locked row.
        String key; int quantity, charges; UUID owner; long ownerUntil, expiresAt, now;
        try (var q=c.prepareStatement("""
            SELECT item_key,quantity,charges,world,x,y,owner_character,state,
              floor(extract(epoch FROM owner_until)*1000)::bigint AS owner_until_ms,
              floor(extract(epoch FROM expires_at)*1000)::bigint AS expires_ms,
              floor(extract(epoch FROM clock_timestamp())*1000)::bigint AS now_ms
            FROM online_ground_loot WHERE id=? FOR UPDATE
            """)) {
          q.setObject(1,lootId);
          try (var rows=q.executeQuery()) {
            if (!rows.next() || !"ON_GROUND".equals(rows.getString("state"))) throw new Conflict(OnlinePickupRules.Refusal.LOOT_UNAVAILABLE.code());
            key=rows.getString("item_key"); quantity=rows.getInt("quantity"); charges=rows.getInt("charges");
            owner=rows.getObject("owner_character",UUID.class); ownerUntil=rows.getLong("owner_until_ms");
            expiresAt=rows.getLong("expires_ms"); now=rows.getLong("now_ms");
            var loot=new OnlinePickupRules.Loot(lootId,key,rows.getInt("world"),rows.getDouble("x"),rows.getDouble("y"),owner,ownerUntil,expiresAt);
            var pickup=new OnlinePickupRules.Pickup(session.characterId(),world,x,y,now);
            java.util.Optional<OnlinePickupRules.Refusal> refusal;
            try { refusal=OnlinePickupRules.refusal(loot,pickup,progress,content); }
            catch (IllegalArgumentException unsupported) { throw new Conflict("unsupported_pickup"); }
            if (refusal.isPresent()) throw new Conflict(refusal.get().code());
          }
        }
        boolean alreadyOwned=progress.inventory().stream().anyMatch(item -> item.id().equals(lootId))
            || progress.storage().stream().anyMatch(item -> item.id().equals(lootId))
            || progress.equipment().containsValue(lootId);
        if (alreadyOwned) throw new Conflict("Loot already owned");
        var inventory=new java.util.ArrayList<>(progress.inventory());
        inventory.add(new CharacterProgress.Item(lootId,key,quantity,charges));
        var p=progress;
        var next=new CharacterProgress(p.version(),p.characterClass(),p.gender(),p.attributes(),p.vitals(),p.advancement(),inventory,p.equipment(),p.skills(),p.spells(),p.questFlags(),p.storage(),p.storageGold(),p.respawn(),p.buffs(),p.cooldowns(),p.hiddenUntil(),p.companion());
        try (var q=c.prepareStatement("UPDATE online_ground_loot SET state='CLAIMED',claimed_by=?,claim_event=? WHERE id=? AND state='ON_GROUND'")) {
          q.setObject(1,session.characterId()); q.setObject(2,event); q.setObject(3,lootId);
          if (q.executeUpdate()!=1) throw new Conflict(OnlinePickupRules.Refusal.LOOT_UNAVAILABLE.code());
        }
        try (var q=c.prepareStatement("INSERT INTO online_loot_claims(event_id,character_id,loot_id,payload_hash) VALUES (?,?,?,?)")) {
          q.setObject(1,event); q.setObject(2,session.characterId()); q.setObject(3,lootId); q.setString(4,digest); q.executeUpdate();
        }
        try (var q=c.prepareStatement("UPDATE online_characters SET progress=?::jsonb, revision=revision+1, updated_at=now() WHERE id=?")) {
          q.setString(1,encodeProgress(next)); q.setObject(2,session.characterId()); q.executeUpdate();
        }
        state=load(c,session.accountId(),session.characterId(),false);
        c.commit(); return new RewardResult(true,state);
      } catch (SQLException|RuntimeException failure) { c.rollback(); throw failure; }
    }
  }

  /** Live drops in a rectangle of one region, oldest first. Expired or claimed rows never appear. */
  public java.util.List<GroundLoot> listGroundLoot(int world, TileRect area) throws SQLException {
    if (world<0) throw new IllegalArgumentException("Invalid loot world");
    Objects.requireNonNull(area);
    try (var c=connections.open(); var s=c.prepareStatement("""
        SELECT id,world,x,y,item_key,quantity,charges,owner_character,
          owner_until>clock_timestamp() AS owner_open,
          GREATEST(0::bigint,floor(extract(epoch FROM (owner_until-clock_timestamp()))*1000)::bigint) AS owner_remaining_ms
        FROM online_ground_loot
        WHERE state='ON_GROUND' AND world=? AND expires_at>clock_timestamp()
          AND x BETWEEN ? AND ? AND y BETWEEN ? AND ?
        ORDER BY created_at,id LIMIT ?
        """)) {
      s.setInt(1,world); s.setDouble(2,area.minX()); s.setDouble(3,area.maxX());
      s.setDouble(4,area.minY()); s.setDouble(5,area.maxY()); s.setInt(6,GROUND_LOOT_LIST_LIMIT);
      try (var rows=s.executeQuery()) {
        var result=new java.util.ArrayList<GroundLoot>();
        while (rows.next()) {
          boolean open=rows.getBoolean("owner_open");
          result.add(new GroundLoot(rows.getObject("id",UUID.class),rows.getInt("world"),rows.getDouble("x"),rows.getDouble("y"),
              rows.getString("item_key"),rows.getInt("quantity"),rows.getInt("charges"),
              open ? rows.getObject("owner_character",UUID.class) : null,open ? rows.getLong("owner_remaining_ms") : 0L));
        }
        return java.util.List.copyOf(result);
      }
    }
  }

  /** Bounded sweep: marks due drops expired and returns them so the simulation can publish removals. */
  public java.util.List<ExpiredLoot> expireGroundLoot(int limit) throws SQLException {
    if (limit<1 || limit>GROUND_LOOT_LIST_LIMIT) throw new IllegalArgumentException("Invalid expiry batch");
    try (var c=connections.open(); var s=c.prepareStatement("""
        UPDATE online_ground_loot SET state='EXPIRED'
        WHERE id IN (SELECT id FROM online_ground_loot WHERE state='ON_GROUND' AND expires_at<=clock_timestamp()
                     ORDER BY expires_at,id LIMIT ? FOR UPDATE SKIP LOCKED)
          AND state='ON_GROUND'
        RETURNING id,world
        """)) {
      s.setInt(1,limit);
      try (var rows=s.executeQuery()) {
        var result=new java.util.ArrayList<ExpiredLoot>();
        while (rows.next()) result.add(new ExpiredLoot(rows.getObject(1,UUID.class),rows.getInt(2)));
        return java.util.List.copyOf(result);
      }
    }
  }

  private static CorpseState readCorpse(ResultSet row) throws SQLException {
    try {
      var items=JSON.fromJson(row.getString("items"),CharacterProgress.Item[].class);
      return new CorpseState(row.getObject("id",UUID.class),row.getObject("character_id",UUID.class),row.getInt("world"),row.getDouble("x"),row.getDouble("y"),java.util.List.of(items),row.getLong("gold"),row.getBoolean("recovered"));
    } catch(RuntimeException invalid) { throw new SQLException("Invalid corpse state"); }
  }

  private static String encodeProgress(CharacterProgress progress) {
    var tree=JSON.toJsonTree(Objects.requireNonNull(progress)).getAsJsonObject();
    // Sets and maps must hash identically after a JVM restart or a PostgreSQL JSONB round trip.
    var spells=new com.google.gson.JsonArray();
    progress.spells().stream().sorted().forEach(spells::add);
    tree.add("spells",spells);
    String encoded=JSON.toJson(canonical(tree));
    if (encoded.getBytes(StandardCharsets.UTF_8).length>1024*1024) throw new IllegalArgumentException("Character state too large");
    return encoded;
  }

  private static com.google.gson.JsonElement canonical(com.google.gson.JsonElement value) {
    if (value.isJsonObject()) {
      var sorted=new com.google.gson.JsonObject();
      value.getAsJsonObject().keySet().stream().sorted()
          .forEach(key -> sorted.add(key,canonical(value.getAsJsonObject().get(key))));
      return sorted;
    }
    if (value.isJsonArray()) {
      var array=new com.google.gson.JsonArray();
      value.getAsJsonArray().forEach(element -> array.add(canonical(element)));
      return array;
    }
    return value;
  }

  private static String fingerprint(String encoded) {
    try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(encoded.getBytes(StandardCharsets.UTF_8))); }
    catch (java.security.NoSuchAlgorithmException error) { throw new AssertionError(error); }
  }

  private static void bindSession(PreparedStatement s, Session session, int first) throws SQLException {
    s.setObject(first, session.characterId()); s.setObject(first+1, session.accountId()); s.setObject(first+2, session.leaseId());
  }

  private static String newKey() {
    byte[] bytes = new byte[32]; RANDOM.nextBytes(bytes); return HexFormat.of().formatHex(bytes);
  }

  private static String hash(String key) throws Conflict {
    if (key == null || !key.matches("[0-9a-f]{64}")) throw new Conflict("Account or character unavailable");
    try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.US_ASCII))); }
    catch (java.security.NoSuchAlgorithmException error) { throw new AssertionError(error); }
  }

  private CharacterState load(Connection c, UUID account, UUID character, boolean lock) throws SQLException {
    try (var s = c.prepareStatement("SELECT * FROM online_characters WHERE id=? AND account_id=?" + (lock ? " FOR UPDATE" : ""))) {
      s.setObject(1, character); s.setObject(2, account);
      try (var rows = s.executeQuery()) {
        if (!rows.next()) throw new Conflict("Character unavailable");
        return read(rows);
      }
    }
  }

  private static CharacterState read(ResultSet row) throws SQLException {
    CharacterProgress progress=null;
    String encoded=row.getString("progress");
    if (encoded!=null) {
      try {
        progress=JSON.fromJson(encoded,CharacterProgress.class);
        if (progress==null) throw new IllegalArgumentException();
      } catch (RuntimeException invalid) {
        // Fail closed without logging the player's inventory, credentials or raw database row.
        throw new SQLException("Unsupported or invalid online character state");
      }
    }
    return new CharacterState(row.getObject("id", UUID.class), row.getObject("account_id", UUID.class),
        row.getString("name"), row.getInt("world"), row.getDouble("x"), row.getDouble("y"),
        row.getLong("gold"), row.getLong("experience"), row.getLong("revision"),progress,row.getLong("location_epoch"),row.getLong("portal_cooldown_until"));
  }

  private static void position(int world, double x, double y) {
    if (world < 0 || !Double.isFinite(x) || !Double.isFinite(y) || x < 0 || y < 0)
      throw new IllegalArgumentException("Invalid position");
  }
}
