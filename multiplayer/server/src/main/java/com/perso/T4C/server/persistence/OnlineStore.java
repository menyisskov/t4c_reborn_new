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
      long gold, long experience, long revision, CharacterProgress progress) {}
  public record RewardResult(boolean applied, CharacterState character) {}
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
            if (rows.getInt(1) != ++version || version > 4) throw new SQLException("Unsupported online schema version");
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
        c.commit();
      } catch (SQLException | RuntimeException error) { c.rollback(); throw error; }
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
    return applyReward(session,event,gold,experience,null,null);
  }

  /** Server-owned quest/loot/progression result; receipt, balances and gameplay state commit once. */
  public RewardResult commitGameplayEvent(Session session, long revision, UUID event, long gold,
      long experience, CharacterProgress progress) throws SQLException {
    if (revision<0) throw new IllegalArgumentException("Invalid revision");
    return applyReward(session,event,gold,experience,revision,Objects.requireNonNull(progress));
  }

  private RewardResult applyReward(Session session, UUID event, long gold, long experience,
      Long revision, CharacterProgress progress) throws SQLException {
    Objects.requireNonNull(event);
    if (gold < 0 || experience < 0) throw new IllegalArgumentException("Negative reward");
    String encoded=progress==null ? null : encodeProgress(progress);
    String fingerprint=encoded==null ? null : fingerprint(encoded);
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
        try (var s = c.prepareStatement("SELECT gold, experience, payload_hash FROM online_rewards WHERE character_id=? AND event_id=?")) {
          s.setObject(1, character); s.setObject(2, event);
          try (var rows = s.executeQuery()) {
            if (rows.next()) {
              if (rows.getLong(1) != gold || rows.getLong(2) != experience
                  || !Objects.equals(fingerprint,rows.getString(3))) throw new Conflict("Reward event payload mismatch");
              c.commit(); return new RewardResult(false, state);
            }
          }
        }
        if (revision!=null && state.revision()!=revision) throw new Conflict("Stale gameplay event");
        try (var s = c.prepareStatement("INSERT INTO online_rewards(character_id,event_id,gold,experience,payload_hash) VALUES (?,?,?,?,?)")) {
          s.setObject(1, character); s.setObject(2, event); s.setLong(3, gold); s.setLong(4, experience);
          s.setString(5,fingerprint); s.executeUpdate();
        }
        try (var s = c.prepareStatement("UPDATE online_characters SET gold=gold+?, experience=experience+?, progress=COALESCE(?::jsonb,progress), revision=revision+1, updated_at=now() WHERE id=?")) {
          s.setLong(1, gold); s.setLong(2, experience); s.setString(3,encoded); s.setObject(4, character); s.executeUpdate();
        }
        state = load(c, account, character, false);
        c.commit(); return new RewardResult(true, state);
      } catch (SQLException | RuntimeException error) { c.rollback(); throw error; }
    }
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
        row.getLong("gold"), row.getLong("experience"), row.getLong("revision"),progress);
  }

  private static void position(int world, double x, double y) {
    if (world < 0 || !Double.isFinite(x) || !Double.isFinite(y) || x < 0 || y < 0)
      throw new IllegalArgumentException("Invalid position");
  }
}
