package com.perso.T4C.server;

import static com.perso.T4C.config.GameConstants.GRID_W;
import static com.perso.T4C.config.GameConstants.PLAYER_SPEED;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Owned exclusively by the simulation thread. Network clients supply directions, never positions. */
public final class PreviewWorld {
  public static final int MAX_PLAYERS = 20;
  public static final int TICK_RATE = 34;
  public static final long INPUT_TIMEOUT_TICKS = 12;
  public static final long SESSION_TIMEOUT_TICKS = TICK_RATE * 30;
  public record Joined(String token, long id) {}
  public record PlayerView(long id, double x, double y, long sequence) {}
  public record Snapshot(long tick, List<PlayerView> players) {}
  public static final class Rejected extends RuntimeException {
    public final int status;
    Rejected(int status, String code) { super(code); this.status = status; }
  }
  private static final class Player {
    long id, sequence = -1, lastSeen, lastInput;
    int dx, dy;
    double x, y;
  }
  private final PreviewMap map;
  private final Map<String, Player> players = new LinkedHashMap<>();
  private final SecureRandom random = new SecureRandom();
  private long tick, nextId = 1;

  public PreviewWorld(PreviewMap map) { this.map = map; }

  public Joined join() {
    if (players.size() >= MAX_PLAYERS) throw new Rejected(409, "full");
    byte[] secret = new byte[32];
    random.nextBytes(secret);
    String token = HexFormat.of().formatHex(secret);
    var player = new Player();
    player.id = nextId++;
    player.x = map.spawnX;
    player.y = map.spawnY;
    player.lastSeen = tick;
    players.put(token, player);
    return new Joined(token, player.id);
  }

  private Player authenticate(String token) {
    Player player = players.get(token);
    if (player == null) throw new Rejected(401, "session");
    player.lastSeen = tick;
    return player;
  }

  public void input(String token, long sequence, int dx, int dy) {
    Player player = authenticate(token);
    if (sequence < 0 || dx < -1 || dx > 1 || dy < -1 || dy > 1)
      throw new Rejected(400, "input");
    if (sequence <= player.sequence) throw new Rejected(409, "sequence");
    player.sequence = sequence;
    player.dx = dx;
    player.dy = dy;
    player.lastInput = tick;
  }

  public Snapshot snapshot(String token) {
    authenticate(token);
    List<PlayerView> result = new ArrayList<>(players.size());
    for (Player p : players.values()) result.add(new PlayerView(p.id, p.x, p.y, p.sequence));
    return new Snapshot(tick, List.copyOf(result));
  }

  public void leave(String token) { authenticate(token); players.remove(token); }

  public void step() {
    tick++;
    players.values().removeIf(p -> tick - p.lastSeen > SESSION_TIMEOUT_TICKS);
    // Fixed server-owned time and speed. Repeated input packets cannot add movement budget.
    double distance = PLAYER_SPEED / GRID_W / TICK_RATE;
    int steps = (int) Math.ceil(distance / 0.2);
    double amount = distance / steps;
    for (Player p : players.values()) {
      if (tick - p.lastInput > INPUT_TIMEOUT_TICKS) { p.dx = 0; p.dy = 0; }
      for (int i = 0; i < steps; i++) {
        double x = p.x + p.dx * amount, y = p.y + p.dy * amount;
        // Swept substeps plus orthogonal checks prevent tunneling and diagonal corner cuts.
        if (map.canStand(x, y) && map.canStand(x, p.y) && map.canStand(p.x, y)) {
          p.x = x; p.y = y;
        } else break;
      }
    }
  }
}
