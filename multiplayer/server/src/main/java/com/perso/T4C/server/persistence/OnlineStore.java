package com.perso.T4C.server.persistence;

import java.sql.*;
import java.util.Objects;
import java.util.UUID;

/** Server-only PostgreSQL persistence. Call from a bounded persistence worker, never the tick thread. */
public final class OnlineStore {
  @FunctionalInterface public interface Connections { Connection open() throws SQLException; }
  public record CharacterState(UUID id, UUID accountId, String name, int world, double x, double y,
      long gold, long experience, long revision) {}
  public record RewardResult(boolean applied, CharacterState character) {}
  public static final class Conflict extends SQLException {
    public Conflict(String message) { super(message); }
  }
  private final Connections connections;

  public OnlineStore(Connections connections) { this.connections = Objects.requireNonNull(connections); }

  public static OnlineStore fromEnvironment() {
    String url = required("T4C_DB_URL"), user = required("T4C_DB_USER"), password = required("T4C_DB_PASSWORD");
    if (!url.startsWith("jdbc:postgresql://")) throw new IllegalArgumentException("PostgreSQL URL required");
    return new OnlineStore(() -> DriverManager.getConnection(url, user, password));
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
        try (var rows = s.executeQuery("SELECT version FROM online_schema ORDER BY version")) {
          if (rows.next()) {
            if (rows.getInt(1) != 1 || rows.next()) throw new SQLException("Unsupported online schema version");
            c.commit();
            return;
          }
        }
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
        c.commit();
      } catch (SQLException | RuntimeException error) { c.rollback(); throw error; }
    }
  }

  public CharacterState create(UUID account, String name, int world, double x, double y) throws SQLException {
    Objects.requireNonNull(account);
    if (name == null || name.isBlank() || name.length() > 32) throw new IllegalArgumentException("Invalid character name");
    position(world, x, y);
    try (var c = connections.open(); var s = c.prepareStatement("""
        INSERT INTO online_characters(id, account_id, name, world, x, y) VALUES (?, ?, ?, ?, ?, ?) RETURNING *
        """)) {
      s.setObject(1, UUID.randomUUID()); s.setObject(2, account); s.setString(3, name);
      s.setInt(4, world); s.setDouble(5, x); s.setDouble(6, y);
      try (var rows = s.executeQuery()) { rows.next(); return read(rows); }
    }
  }

  public CharacterState load(UUID account, UUID character) throws SQLException {
    try (var c = connections.open()) { return load(c, account, character, false); }
  }

  /** Compare-and-set: an old checkpoint cannot overwrite a later checkpoint or reward. */
  public CharacterState checkpoint(UUID account, UUID character, long revision, int world, double x, double y) throws SQLException {
    position(world, x, y);
    try (var c = connections.open(); var s = c.prepareStatement("""
        UPDATE online_characters SET world=?, x=?, y=?, revision=revision+1, updated_at=now()
        WHERE id=? AND account_id=? AND revision=? RETURNING *
        """)) {
      s.setInt(1, world); s.setDouble(2, x); s.setDouble(3, y);
      s.setObject(4, character); s.setObject(5, account); s.setLong(6, revision);
      try (var rows = s.executeQuery()) {
        if (!rows.next()) throw new Conflict("Character unavailable or stale checkpoint");
        return read(rows);
      }
    }
  }

  /** Event IDs and amounts must originate from authoritative gameplay, never a client request. */
  public RewardResult reward(UUID account, UUID character, UUID event, long gold, long experience) throws SQLException {
    Objects.requireNonNull(event);
    if (gold < 0 || experience < 0) throw new IllegalArgumentException("Negative reward");
    try (var c = connections.open()) {
      c.setAutoCommit(false);
      try {
        CharacterState state = load(c, account, character, true);
        try (var s = c.prepareStatement("SELECT gold, experience FROM online_rewards WHERE character_id=? AND event_id=?")) {
          s.setObject(1, character); s.setObject(2, event);
          try (var rows = s.executeQuery()) {
            if (rows.next()) {
              if (rows.getLong(1) != gold || rows.getLong(2) != experience) throw new Conflict("Reward event payload mismatch");
              c.commit(); return new RewardResult(false, state);
            }
          }
        }
        try (var s = c.prepareStatement("INSERT INTO online_rewards(character_id,event_id,gold,experience) VALUES (?,?,?,?)")) {
          s.setObject(1, character); s.setObject(2, event); s.setLong(3, gold); s.setLong(4, experience); s.executeUpdate();
        }
        try (var s = c.prepareStatement("UPDATE online_characters SET gold=gold+?, experience=experience+?, revision=revision+1, updated_at=now() WHERE id=?")) {
          s.setLong(1, gold); s.setLong(2, experience); s.setObject(3, character); s.executeUpdate();
        }
        state = load(c, account, character, false);
        c.commit(); return new RewardResult(true, state);
      } catch (SQLException | RuntimeException error) { c.rollback(); throw error; }
    }
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
    return new CharacterState(row.getObject("id", UUID.class), row.getObject("account_id", UUID.class),
        row.getString("name"), row.getInt("world"), row.getDouble("x"), row.getDouble("y"),
        row.getLong("gold"), row.getLong("experience"), row.getLong("revision"));
  }

  private static void position(int world, double x, double y) {
    if (world < 0 || !Double.isFinite(x) || !Double.isFinite(y) || x < 0 || y < 0)
      throw new IllegalArgumentException("Invalid position");
  }
}
