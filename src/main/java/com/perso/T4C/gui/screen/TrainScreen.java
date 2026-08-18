package com.perso.T4C.gui.screen;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.gui.core.GuiClickZone;
import com.perso.T4C.gui.core.GuiManager;
import com.perso.T4C.gui.core.GuiScreenBase;
import com.perso.T4C.gui.widget.GuiText;
import com.perso.T4C.helper.PlayerStateStore;
import com.perso.T4C.helper.SpriteLoader;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.registry.NpcDef;
import com.perso.T4C.player.Player;
import com.perso.T4C.ui.FontManager;
import com.perso.T4C.ui.SystemMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.ToIntFunction;

public class TrainScreen extends GuiScreenBase {
  private static final float ROW_HEIGHT = 36f;
  private static final float ROW_START_Y = 40f;
  private static final float STAT_X = 10f;
  private static final float VALUE_X = 220f;
  private static final float COST_X = 290f;
  private static final float PLUS_X = 420f;
  private static final float GOLD_LABEL_Y = 340f;
  private static final Color ELIGIBLE_COLOR = Color.valueOf("F2B705");
  private static final Color BLOCKED_COLOR = Color.valueOf("B03030");
  private static final Map<String, StatAccessor> STAT_ACCESSORS = new HashMap<>();

  static {
    STAT_ACCESSORS.put(
        "strength", new StatAccessor(Player::getStrength, (p, v) -> p.setStrength(v)));
    STAT_ACCESSORS.put(
        "dexterity", new StatAccessor(Player::getDexterity, (p, v) -> p.setDexterity(v)));
    STAT_ACCESSORS.put(
        "endurance", new StatAccessor(Player::getEndurance, (p, v) -> p.setEndurance(v)));
    STAT_ACCESSORS.put(
        "intelligence", new StatAccessor(Player::getIntelligence, (p, v) -> p.setIntelligence(v)));
    STAT_ACCESSORS.put("wisdom", new StatAccessor(Player::getWisdom, (p, v) -> p.setWisdom(v)));
  }

  private final Player player;
  private final List<TrainEntry> entries = new ArrayList<>();
  private final List<GuiText> rowLabels = new ArrayList<>();
  private final List<GuiClickZone> plusZones = new ArrayList<>();

  public TrainScreen(Player player, List<NpcDef.TrainableStat> trainableStats) {
    this.player = player;
    try {
      background = SpriteLoader.getInstance().getRegionFromSpriteName("GUI_BackSpell");
    } catch (GameException ignored) {
      background = null;
    }
    centerOnScreen();
    addCloseButton();
    addHeader();
    loadEntries(trainableStats);
    rebuildList();
  }

  private void addCloseButton() {
    addCloseButton(550f, 1f);
  }

  private void addHeader() {
    if (background == null) return;
    labels.add(GuiText.translatedHeader("ui.training", "TRAINING", x + 250f, y + 5f));
  }

  private void loadEntries(List<NpcDef.TrainableStat> trainableStats) {
    entries.clear();
    if (trainableStats == null) return;
    for (NpcDef.TrainableStat ts : trainableStats) {
      if (ts == null || ts.getStatId() == null || ts.getStatId().isEmpty()) continue;
      entries.add(new TrainEntry(ts, resolveDisplayName(ts.getStatId())));
    }
  }

  private static String resolveDisplayName(String statId) {
    if (I18n.has("skill." + statId)) return I18n.key("skill." + statId);
    if (STAT_ACCESSORS.containsKey(statId)) {
      return I18n.key(
          "skill." + statId,
          Character.toUpperCase(statId.charAt(0)) + statId.substring(1).toLowerCase());
    }
    StringBuilder sb = new StringBuilder();
    for (String word : statId.split("_")) {
      if (!word.isEmpty()) {
        if (sb.length() > 0) sb.append(' ');
        sb.append(Character.toUpperCase(word.charAt(0)));
        sb.append(word.substring(1).toLowerCase());
      }
    }
    return sb.toString();
  }

  private void rebuildList() {
    labels.removeAll(rowLabels);
    rowLabels.clear();
    plusZones.clear();
    BitmapFont font = FontManager.getInstance().getJetBrainsMonoFont(12, Color.WHITE);
    for (int i = 0; i < entries.size(); i++) {
      TrainEntry entry = entries.get(i);
      float ry = y + ROW_START_Y + i * ROW_HEIGHT;
      int current = getCurrent(entry);
      boolean capped = entry.stat.getMaxPoints() > 0 && current >= entry.stat.getMaxPoints();
      boolean canAfford = player.getGold() >= entry.stat.getCostPerPoint();
      final String label = entry.displayName;
      addRow(font, x + STAT_X, ry, () -> label, null);
      final String valueStr = String.valueOf(current);
      addRow(font, x + VALUE_X, ry, () -> valueStr, null);
      final String costStr = entry.stat.getCostPerPoint() + I18n.key("ui.gold_per_point");
      addRow(font, x + COST_X, ry, () -> costStr, canAfford ? ELIGIBLE_COLOR : BLOCKED_COLOR);
      Color plusColor = capped ? Color.WHITE : (canAfford ? ELIGIBLE_COLOR : BLOCKED_COLOR);
      final String plusLabel = capped ? "[MAX]" : "[+1]";
      addRow(font, x + PLUS_X, ry, () -> plusLabel, plusColor);
      if (!capped) {
        plusZones.add(new GuiClickZone(x + PLUS_X, ry, 50f, 20f, () -> tryTrain(entry)));
      }
    }
    BitmapFont goldFont = FontManager.getInstance().getJetBrainsMonoFont(12, ELIGIBLE_COLOR);
    GuiText goldLabel =
        new GuiText(
            goldFont,
            x + 10f,
            y + GOLD_LABEL_Y,
            () -> I18n.key("ui.gold_label") + " " + player.getGold());
    labels.add(goldLabel);
    rowLabels.add(goldLabel);
  }

  private int getCurrent(TrainEntry entry) {
    String id = entry.stat.getStatId();
    if (STAT_ACCESSORS.containsKey(id)) {
      return STAT_ACCESSORS.get(id).getter.applyAsInt(player);
    }
    return player.getSkillLevel(id);
  }

  private void increment(TrainEntry entry, int current) {
    String id = entry.stat.getStatId();
    if (STAT_ACCESSORS.containsKey(id)) {
      STAT_ACCESSORS.get(id).setter.accept(player, current + 1);
    } else {
      player.setSkillLevel(id, current + 1);
    }
  }

  private void addRow(
      BitmapFont font, float lx, float ly, java.util.function.Supplier<String> text, Color color) {
    GuiText label =
        color == null
            ? new GuiText(font, lx, ly, text)
            : new GuiText(font, lx, ly, text, () -> color);
    labels.add(label);
    rowLabels.add(label);
  }

  private void tryTrain(TrainEntry entry) {
    int current = getCurrent(entry);
    if (entry.stat.getMaxPoints() > 0 && current >= entry.stat.getMaxPoints()) {
      SystemMessage.showShared(I18n.message("message.stat_at_maximum", entry.displayName));
      return;
    }
    if (player.getGold() < entry.stat.getCostPerPoint()) {
      SystemMessage.showShared(I18n.message("message.not_enough_gold"));
      return;
    }
    player.setGold(player.getGold() - entry.stat.getCostPerPoint());
    increment(entry, current);
    PlayerStateStore.save(player);
    SystemMessage.showShared(
        I18n.message("message.stat_increased", entry.displayName, current + 1));
    rebuildList();
  }

  @Override
  public void onTouchUp(float screenX, float screenY) {
    for (GuiClickZone zone : new ArrayList<>(plusZones)) {
      if (zone.contains(screenX, screenY)) {
        zone.run();
        return;
      }
    }
    super.onTouchUp(screenX, screenY);
  }

  @Override
  public boolean onKeyDown(int keycode) {
    if (keycode == Input.Keys.ESCAPE) {
      GuiManager.close();
      return true;
    }
    return super.onKeyDown(keycode);
  }

  private static final class StatAccessor {
    private final ToIntFunction<Player> getter;
    private final BiConsumer<Player, Integer> setter;

    private StatAccessor(ToIntFunction<Player> getter, BiConsumer<Player, Integer> setter) {
      this.getter = getter;
      this.setter = setter;
    }
  }

  private static final class TrainEntry {
    private final NpcDef.TrainableStat stat;
    private final String displayName;

    private TrainEntry(NpcDef.TrainableStat stat, String displayName) {
      this.stat = stat;
      this.displayName = displayName;
    }
  }
}
