package com.perso.T4C.server.persistence;

import com.perso.T4C.online.model.CharacterProgress;

import java.sql.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Objects;
import java.util.UUID;

/** Server-only PostgreSQL persistence. Call from a bounded persistence worker, never the tick thread. */
public final class OnlineStore {
  private static final com.google.gson.Gson JSON = new com.google.gson.Gson();
  @FunctionalInterface public interface Connections { Connection open() throws SQLException; }
  public record CharacterState(UUID id, UUID accountId, String name, int world, double x, double y,
      long gold, long experience, long revision, CharacterProgress progress,long locationEpoch,long portalCooldownUntil) {
    public CharacterState(UUID id,UUID accountId,String name,int world,double x,double y,long gold,long experience,long revision,CharacterProgress progress) {
      this(id,accountId,name,world,x,y,gold,experience,revision,progress,0,0);
    }
  }
  public record RewardResult(boolean applied, CharacterState character) {}
  public record SpawnState(String key, long generation, UUID eventId, UUID recipient, java.time.Instant claimedAt,
      Integer health, UUID targetCharacter, long nextAttackAt) {}
  public record CreatureProvocation(String key, UUID life, UUID targetCharacter, long nextAttackAt) {
    public CreatureProvocation { spawnKey(key); Objects.requireNonNull(life); Objects.requireNonNull(targetCharacter); if(nextAttackAt<0) throw new IllegalArgumentException("Invalid attack deadline"); }
  }
  public record MonsterAttack(String key, UUID life, UUID targetCharacter, long expectedNext, long nextNext) {
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
  public record CreatureStrike(String key, UUID life, int maximumHealth, int before, int after) {
    public CreatureStrike {
      spawnKey(key); Objects.requireNonNull(life);
      if (maximumHealth<=0 || before<=0 || before>maximumHealth || after<0 || after>=before)
        throw new IllegalArgumentException("Invalid creature damage");
    }
  }
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
  private final Connections connections;

  public OnlineStore(Connections connections) { this.connections = Objects.requireNonNull(connections); }

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
            if (rows.getInt(1) != ++version || version > 9) throw new SQLException("Unsupported online schema version");
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
      try (var s=c.prepareStatement("""
          SELECT s.generation,s.event_id,r.character_id,r.claimed_at,s.health,s.target_character,s.next_attack_at
          FROM online_spawns s LEFT JOIN online_world_rewards r ON r.event_id=s.event_id
          WHERE s.spawn_key=?
          """)) {
        s.setString(1,key);
        try (var rows=s.executeQuery()) {
          if (!rows.next()) throw new Conflict("Spawn unavailable");
          var claimedAt=rows.getTimestamp(4);
          return new SpawnState(key,rows.getLong(1),rows.getObject(2,UUID.class),rows.getObject(3,UUID.class),
              claimedAt==null ? null : claimedAt.toInstant(),rows.getObject(5,Integer.class),rows.getObject(6,UUID.class),rows.getLong(7));
        }
      }
    }
  }

  /** Advance only a committed rewarded life after its authored delay, using database time and CAS. */
  public SpawnState respawn(String key, UUID expectedEvent, long delayMillis) throws SQLException {
    spawnKey(key); Objects.requireNonNull(expectedEvent);
    if (delayMillis<0) throw new IllegalArgumentException("Negative respawn delay");
    try (var c=connections.open(); var s=c.prepareStatement("""
        UPDATE online_spawns s SET generation=s.generation+1,event_id=?,health=NULL,target_character=NULL,next_attack_at=0
        FROM online_world_rewards r
        WHERE s.spawn_key=? AND s.event_id=? AND r.event_id=s.event_id
          AND r.claimed_at<=clock_timestamp()-(? * interval '1 millisecond')
        RETURNING s.generation,s.event_id
        """)) {
      s.setObject(1,UUID.randomUUID()); s.setString(2,key); s.setObject(3,expectedEvent); s.setLong(4,delayMillis);
      try (var rows=s.executeQuery()) {
        if (!rows.next()) throw new Conflict("Spawn life changed or respawn not ready");
        return new SpawnState(key,rows.getLong(1),rows.getObject(2,UUID.class),null,null,null,null,0);
      }
    }
  }

  private static void spawnKey(String key) {
    if (key==null || key.isBlank() || key.length()>256) throw new IllegalArgumentException("Invalid spawn key");
  }

  /** Operator-only provisioning. The database stores a verifier, not the usable access key. */
  public AccountKey provisionAccount() throws SQLException {
    UUID account = UUID.randomUUID(); String secret = newKey();
    try (var c = connections.open(); var s = c.prepareStatement("INSERT INTO online_accounts(id,access_hash) VALUES (?,?)")) {
      s.setObject(1, account); s.setString(2, hash(secret)); s.executeUpdate();
    }
    return new AccountKey(account, secret);
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
    if(revision<0) throw new IllegalArgumentException("Invalid revision");
    Objects.requireNonNull(strike); Objects.requireNonNull(event);
    boolean lethal=strike.after()==0;
    if(lethal!=event.equals(strike.life())) throw new IllegalArgumentException("Invalid strike event identity");
    if(provocation!=null && (!provocation.key().equals(strike.key()) || !provocation.life().equals(strike.life()))) throw new IllegalArgumentException("Different provoked life");
    return applyReward(session,event,gold,experience,revision,Objects.requireNonNull(progress),lethal,strike,provocation);
  }

  private RewardResult applyReward(Session session, UUID event, long gold, long experience,
      Long revision, CharacterProgress progress, boolean worldReward, CreatureStrike strike,
      CreatureProvocation provocation) throws SQLException {
    Objects.requireNonNull(event);
    if(provocation!=null && !provocation.targetCharacter().equals(session.characterId())) throw new IllegalArgumentException("Invalid provocation owner");
    if (gold < 0 || experience < 0) throw new IllegalArgumentException("Negative reward");
    String encoded=progress==null ? null : encodeProgress(progress);
    String fingerprint=encoded==null ? null : fingerprint(strike==null ? encoded
        : encoded+"\ncreature-strike-v1\n"+JSON.toJson(canonical(JSON.toJsonTree(strike))));
    if(provocation!=null) fingerprint=fingerprint(fingerprint+"\nprovocation-v1\n"+JSON.toJson(provocation));
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
        if(provocation!=null) {
          try(var s=c.prepareStatement("UPDATE online_spawns SET target_character=?,next_attack_at=CASE WHEN target_character IS NULL THEN ? ELSE next_attack_at END WHERE spawn_key=? AND event_id=? AND COALESCE(health,1)>0 AND NOT EXISTS (SELECT 1 FROM online_world_rewards WHERE event_id=online_spawns.event_id)")) {
            s.setObject(1,provocation.targetCharacter()); s.setLong(2,provocation.nextAttackAt()); s.setString(3,provocation.key()); s.setObject(4,provocation.life());
            if(s.executeUpdate()!=1) throw new Conflict("Creature life unavailable");
          }
        }
        if (strike!=null) {
          // Character lock precedes spawn lock. CAS prevents two attackers rewarding the same damage.
          try (var s=c.prepareStatement("""
              UPDATE online_spawns SET health=?
              WHERE spawn_key=? AND event_id=? AND COALESCE(health,?)=?
                AND NOT EXISTS (SELECT 1 FROM online_world_rewards WHERE event_id=online_spawns.event_id)
              """)) {
            s.setInt(1,strike.after()); s.setString(2,strike.key()); s.setObject(3,strike.life());
            s.setInt(4,strike.maximumHealth()); s.setInt(5,strike.before());
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
        }
        try (var s = c.prepareStatement("UPDATE online_characters SET gold=gold+?, experience=experience+?, progress=COALESCE(?::jsonb,progress), revision=revision+1, updated_at=now() WHERE id=?")) {
          s.setLong(1, gold); s.setLong(2, experience); s.setString(3,encoded); s.setObject(4, character); s.executeUpdate();
        }
        state = load(c, account, character, false);
        c.commit(); return new RewardResult(true, state);
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
        try(var q=c.prepareStatement("UPDATE online_spawns SET next_attack_at=? WHERE spawn_key=? AND event_id=? AND target_character=? AND next_attack_at=? AND next_attack_at<=floor(extract(epoch FROM clock_timestamp())*1000) AND COALESCE(health,1)>0 AND NOT EXISTS(SELECT 1 FROM online_world_rewards WHERE event_id=online_spawns.event_id)")) {
          q.setLong(1,attack.nextNext());q.setString(2,attack.key());q.setObject(3,attack.life());q.setObject(4,attack.targetCharacter());q.setLong(5,attack.expectedNext());
          if(q.executeUpdate()!=1) throw new Conflict("Creature attack changed or not due");
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
          try(var q=c.prepareStatement("UPDATE online_spawns SET target_character=NULL WHERE target_character=?")) { q.setObject(1,session.characterId());q.executeUpdate(); }
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
        try(var q=c.prepareStatement("UPDATE online_spawns SET target_character=NULL WHERE target_character=?")) { q.setObject(1,state.id());q.executeUpdate(); }
        state=load(c,session.accountId(),session.characterId(),false);c.commit();return new RewardResult(true,state);
      } catch(SQLException|RuntimeException failure) { c.rollback();throw failure; }
    }
  }

  private static CharacterState lockSession(Connection c,Session session) throws SQLException {
    try(var q=c.prepareStatement("SELECT * FROM online_characters WHERE id=? AND account_id=? AND session_id=? AND session_until>clock_timestamp() FOR UPDATE")) {
      bindSession(q,session,1);try(var rows=q.executeQuery()) { if(!rows.next()) throw new Conflict("Session expired or replaced");return read(rows); }
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

  /** Trusted simulation position is frozen while this command runs; never accept client coordinates. */
  public RewardResult recoverCorpse(Session session,long revision,UUID corpseId,int world,double x,double y) throws SQLException {
    Objects.requireNonNull(corpseId);position(world,x,y);
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
        var p=Objects.requireNonNull(state.progress());var inventory=new java.util.ArrayList<>(p.inventory());inventory.addAll(corpse.items());
        var progress=new CharacterProgress(p.version(),p.characterClass(),p.gender(),p.attributes(),p.vitals(),p.advancement(),inventory,p.equipment(),p.skills(),p.spells(),p.questFlags(),p.storage(),p.storageGold(),p.respawn(),p.buffs(),p.cooldowns(),p.hiddenUntil(),p.companion());
        long gold=Math.addExact(state.gold(),corpse.gold());
        try(var q=c.prepareStatement("UPDATE online_characters SET gold=?,progress=?::jsonb,revision=revision+1,updated_at=now() WHERE id=?")) {
          q.setLong(1,gold);q.setString(2,encodeProgress(progress));q.setObject(3,session.characterId());q.executeUpdate();
        }
        try(var q=c.prepareStatement("UPDATE online_corpses SET recovered=true WHERE id=?")) { q.setObject(1,corpseId);q.executeUpdate(); }
        state=load(c,session.accountId(),session.characterId(),false);c.commit();return new RewardResult(true,state);
      } catch(SQLException|RuntimeException failure) { c.rollback();throw failure; }
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
