import com.google.gson.JsonParser;
import com.perso.T4C.server.HostedRegionPlan;
import com.perso.T4C.server.MonsterCatalog;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Operator tool for the streamed-world soak (T4C-0360): moves the twenty QA characters of a soak profile directory
 * from the temple to twenty distinct, well separated monster fields of world 0, so a run exercises activation,
 * chases and deactivation across the map instead of the temple alone. Disposable database only: it runs one
 * UPDATE per character through T4C_DB_URL / T4C_DB_USER / T4C_DB_PASSWORD and refuses a database whose name
 * does not start with t4c_acc_ or t4c_loot_.
 *
 * Usage: {@code SoakSpread PROFILES_DIR COLLISION_FILE}.
 */
public final class SoakSpread {
  private SoakSpread() {}

  public static void main(String[] args) throws Exception {
    if (args.length != 2) throw new IllegalArgumentException("Expected profiles-directory collision-file");
    String url = System.getenv("T4C_DB_URL");
    if (url == null || !url.matches("jdbc:postgresql://127\\.0\\.0\\.1:\\d+/t4c_(acc|loot)_[0-9a-f]{12}.*"))
      throw new IllegalArgumentException("Refusing: T4C_DB_URL must name a disposable t4c_acc_/t4c_loot_ database on loopback");
    var ids = new ArrayList<UUID>();
    try (var files = Files.list(Path.of(args[0]))) {
      for (var file : files.filter(p -> p.getFileName().toString().matches("player-[0-9]{2}\\.json")).sorted().toList())
        ids.add(UUID.fromString(JsonParser.parseString(Files.readString(file)).getAsJsonObject().get("characterId").getAsString()));
    }
    if (ids.size() != 20) throw new IllegalArgumentException("Exactly twenty profiles are required");
    var plan = HostedRegionPlan.startingSlice(Path.of(args[1]), true);
    var map = plan.regions().getFirst().map();
    var content = MonsterCatalog.loadBundled();
    // Density on 32-tile cells; greedily take the busiest cells at least 300 tiles apart that hold a standable tile.
    int cell = 32, w = 6144 / cell, h = 3072 / cell;
    var count = new int[h][w];
    for (var s : content.region(0, 0, 0, 6144, 3072)) count[s.y() / cell][s.x() / cell]++;
    var order = new ArrayList<int[]>();
    for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) if (count[y][x] > 0) order.add(new int[] {x, y, count[y][x]});
    order.sort((a, b) -> b[2] - a[2]);
    var spots = new ArrayList<int[]>();
    for (var c : order) {
      if (spots.size() == 20) break;
      int cx = c[0] * cell + cell / 2, cy = c[1] * cell + cell / 2;
      if (spots.stream().anyMatch(s -> Math.max(Math.abs(s[0] - cx), Math.abs(s[1] - cy)) < 300)) continue;
      int[] tile = null;
      for (int r = 0; r < cell && tile == null; r++)
        for (int dy = -r; dy <= r && tile == null; dy++) for (int dx = -r; dx <= r && tile == null; dx++)
          if (Math.max(Math.abs(dx), Math.abs(dy)) == r && map.canStand(cx + dx + .5, cy + dy + .5)
              && map.canRestOnGrid(cx + dx + .5, cy + dy + .5)) tile = new int[] {cx + dx, cy + dy};
      if (tile != null) spots.add(new int[] {tile[0], tile[1], c[2]});
    }
    if (spots.size() != 20) throw new IllegalStateException("Only " + spots.size() + " separated fields found");
    var properties = new java.util.Properties();
    properties.setProperty("user", System.getenv("T4C_DB_USER"));
    properties.setProperty("password", System.getenv("T4C_DB_PASSWORD"));
    try (var c = java.sql.DriverManager.getConnection(url, properties);
         var s = c.prepareStatement("UPDATE online_characters SET world=0,x=?,y=?,revision=revision+1 WHERE id=?")) {
      for (int i = 0; i < 20; i++) {
        s.setDouble(1, spots.get(i)[0] + .5);
        s.setDouble(2, spots.get(i)[1] + .5);
        s.setObject(3, ids.get(i));
        if (s.executeUpdate() != 1) throw new IllegalStateException("Character " + (i + 1) + " not found");
        System.out.printf("player-%02d -> %d,%d (%d creatures in its 32-tile cell)%n", i + 1, spots.get(i)[0], spots.get(i)[1], spots.get(i)[2]);
      }
    }
  }
}
