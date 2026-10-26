package com.perso.T4C.gui.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Align;
import com.perso.T4C.config.MacroBinding;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.gui.core.CharacterPanel;
import com.perso.T4C.gui.core.GuiDraw;
import com.perso.T4C.gui.core.GuiManager;
import com.perso.T4C.gui.core.JournalTheme;
import com.perso.T4C.gui.widget.GuiButton;
import com.perso.T4C.helper.PlayerStateStore;
import com.perso.T4C.helper.SpriteLoader;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.player.Player;
import com.perso.T4C.spell.SpellData;
import com.perso.T4C.spell.SpellRegistry;
import com.perso.T4C.ui.FontManager;
import com.perso.T4C.ui.PlayerHUD;
import java.util.Collections;
import java.util.List;

/** Character journal page for binding and reordering keyboard spell and item macros. */
public class MacrosScreen extends CharacterPanel {
  static final int ROWS_VISIBLE = 6;
  private static final float ROW_PITCH = 54;
  private static final float ROW_Y = 136;
  private static final float ROW_H = 46;
  private static final float NAME_X = 84;
  private static final float NAME_W = 230;
  private static final float KEY_X = 324;
  private static final float KEY_W = 160;
  private static final Rectangle SCROLL = new Rectangle(502, 136, 12, 316);
  private final Player player;
  private final PlayerHUD hud;
  private final GlyphLayout layout = new GlyphLayout();
  private final GuiButton bindButton;
  private final GuiButton clearButton;
  private final GuiButton removeButton;
  private final GuiButton upButton;
  private final GuiButton downButton;
  private int firstVisible;
  private int selected;
  private int listeningIndex = -1;
  private float mouseX = -1;
  private float mouseY = -1;

  public MacrosScreen() {
    this(null);
  }

  public MacrosScreen(Player player) {
    this(player, null);
  }

  public MacrosScreen(Player player, PlayerHUD hud) {
    super(player, hud, "ui.journal.macros", "macros");
    this.player = player;
    this.hud = hud;
    text("ui.macros.spell", 84, 108, 220, 20, 12, JournalTheme.DIM);
    text("ui.macros.key", 324, 108, 160, 20, 12, JournalTheme.DIM);
    text("ui.macros.selected", 534, 108, 154, 20, 12, JournalTheme.DIM);
    value(
        () ->
            hasSelection()
                ? displayName(macros().get(selected))
                : I18n.key("ui.macros.no_selection"),
        532,
        138,
        158,
        46,
        14,
        JournalTheme.GOLD);
    bindButton = addAction("ui.macros.bind", 530, 200, 160, 32, () -> listeningIndex = selected);
    clearButton = addAction("ui.macros.clear", 530, 242, 160, 30, () -> clearKey(selected));
    upButton = addAction("ui.macros.move_up", 530, 284, 76, 30, () -> move(-1));
    downButton = addAction("ui.macros.move_down", 614, 284, 76, 30, () -> move(1));
    removeButton = addAction("ui.macros.remove", 530, 326, 160, 30, () -> removeEntry(selected));
    addAction(
        "ui.macros.open_spells",
        530,
        370,
        160,
        30,
        () -> GuiManager.open(new SpellBook(player, hud)));
    addAction("ui.macros.controls", 530, 412, 160, 30, () -> GuiManager.open(new ControlsScreen()));
    value(
        () -> I18n.key(listeningIndex >= 0 ? "ui.macros.hint_listening" : "ui.macros.hint"),
        28,
        485,
        664,
        24,
        11,
        JournalTheme.DIM);
  }

  private GuiButton addAction(String key, int dx, int dy, int width, int height, Runnable action) {
    GuiButton button = theme.button(x + dx, y + dy, width, height, () -> I18n.key(key), action);
    buttons.add(button);
    return button;
  }

  private List<MacroBinding> macros() {
    return player == null ? List.of() : player.getMacros();
  }

  private boolean hasSelection() {
    return selected >= 0 && selected < macros().size();
  }

  private Rectangle abs(Rectangle r) {
    return new Rectangle(x + r.x, y + r.y, r.width, r.height);
  }

  private Rectangle rowCell(int row, float cellX, float cellW) {
    return new Rectangle(x + cellX, y + ROW_Y + row * ROW_PITCH, cellW, ROW_H);
  }

  @Override
  public void render(SpriteBatch batch) {
    renderPanelBackground(batch);
    boolean active = hasSelection() && listeningIndex < 0;
    bindButton.setEnabled(active);
    clearButton.setEnabled(active && macros().get(selected).getKeycode() != MacroBinding.UNBOUND);
    removeButton.setEnabled(active);
    upButton.setEnabled(active && selected > 0);
    downButton.setEnabled(active && selected < macros().size() - 1);
    well(batch, 526, 133, 168, 54);
    drawRows(batch);
    drawScrollTick(batch);
    for (var element : orderedElements()) element.render(batch);
  }

  private void drawRows(SpriteBatch batch) {
    var fonts = FontManager.getInstance();
    if (macros().isEmpty()) {
      well(batch, 28, ROW_Y, 466, 316);
      fonts
          .getTahomaFont(14, JournalTheme.DIM, false)
          .draw(batch, I18n.key("ui.macros.empty"), x + 60, y + 230, 400, Align.center, true);
      return;
    }
    for (int row = 0; row < ROWS_VISIBLE; row++) {
      int index = firstVisible + row;
      if (index >= macros().size()) break;
      MacroBinding macro = macros().get(index);
      Rectangle r = rowCell(row, 28, 466);
      boolean hover = r.contains(mouseX, mouseY);
      if (index == selected) {
        theme.accentWell(batch, r.x, r.y, r.width, r.height);
        theme.rect(batch, r.x + 2, r.y + 9, 2, r.height - 18, JournalTheme.GOLD);
      } else theme.well(batch, r.x, r.y, r.width, r.height);
      TextureRegion icon = icon(macro);
      if (icon != null) GuiDraw.drawRegionFlipped(batch, icon, r.x + 10, r.y + 7, 32, 32);
      drawFitted(
          batch,
          fonts.getTahomaFont(
              13,
              index == selected ? JournalTheme.GOLD : hover ? Color.WHITE : JournalTheme.TEXT,
              false),
          displayName(macro),
          x + NAME_X,
          r.y + 5,
          NAME_W,
          36,
          false);
      Rectangle key = rowCell(row, KEY_X, KEY_W);
      theme.well(batch, key.x, key.y + 7, key.width, 32);
      drawFitted(
          batch,
          fonts.getTahomaFont(
              12,
              listeningIndex == index
                  ? JournalTheme.ACCENT
                  : macro.getKeycode() == MacroBinding.UNBOUND
                      ? JournalTheme.DIM
                      : JournalTheme.TEXT,
              false),
          keyDisplay(macro, index),
          key.x + 4,
          key.y + 7,
          key.width - 8,
          32,
          true);
    }
  }

  private void drawScrollTick(SpriteBatch batch) {
    int total = macros().size();
    if (total <= ROWS_VISIBLE) return;
    Rectangle track = abs(SCROLL);
    theme.well(batch, track.x, track.y, track.width, track.height);
    float height = Math.max(20, track.height * ROWS_VISIBLE / total);
    float offset = (track.height - height) * firstVisible / (total - ROWS_VISIBLE);
    theme.rect(batch, track.x + 4, track.y + offset, 4, height, JournalTheme.DIM);
  }

  private void drawFitted(
      SpriteBatch batch,
      BitmapFont font,
      String text,
      float bx,
      float by,
      float width,
      float height,
      boolean centered) {
    float sx = font.getData().scaleX, sy = font.getData().scaleY;
    layout.setText(font, text);
    if (layout.width > width) {
      float scale = width / layout.width;
      font.getData().setScale(sx * scale, sy * scale);
      layout.setText(font, text);
    }
    font.draw(
        batch,
        layout,
        bx + (centered ? (width - layout.width) / 2 : 0),
        by + (height - layout.height) / 2);
    font.getData().setScale(sx, sy);
  }

  private static TextureRegion icon(MacroBinding macro) {
    String icon;
    if (macro.isItem()) {
      var item = com.perso.T4C.item.ItemRegistry.findByKey(macro.getItemKey());
      icon = item == null ? null : item.getAppearanceInventory();
    } else {
      SpellData spell = SpellRegistry.findByName(macro.getSpellName());
      icon = spell == null ? null : spell.getIconId();
    }
    if (icon == null || icon.isEmpty()) return null;
    try {
      return SpriteLoader.getInstance().getRegionFromSpriteName(icon);
    } catch (GameException e) {
      return null;
    }
  }

  private String displayName(MacroBinding macro) {
    if (macro.isItem()) {
      var item = com.perso.T4C.item.ItemRegistry.findByKey(macro.getItemKey());
      return item == null ? macro.getItemKey() : I18n.resolve(item.getName());
    }
    SpellData spell = SpellRegistry.findByName(macro.getSpellName());
    return spell != null ? I18n.resolve(spell.getName()) : macro.getSpellName();
  }

  private String keyDisplay(MacroBinding macro, int index) {
    if (listeningIndex == index) {
      return I18n.key("ui.macros.listening");
    }
    if (macro.getKeycode() == MacroBinding.UNBOUND) {
      return I18n.key("ui.macros.unbound");
    }
    return keyName(macro.getKeycode(), macro.getModifiers());
  }

  static String keyName(int keycode, int modifiers) {
    StringBuilder prefix = new StringBuilder();
    if ((modifiers & MacroBinding.MOD_CTRL) != 0) prefix.append("Ctrl+");
    if ((modifiers & MacroBinding.MOD_SHIFT) != 0) prefix.append("Shift+");
    if ((modifiers & MacroBinding.MOD_ALT) != 0) prefix.append("Alt+");
    return prefix + Input.Keys.toString(keycode);
  }

  // ---------------------------------------------------------------- actions

  private static boolean isModifierKey(int keycode) {
    return keycode == Input.Keys.CONTROL_LEFT
        || keycode == Input.Keys.CONTROL_RIGHT
        || keycode == Input.Keys.SHIFT_LEFT
        || keycode == Input.Keys.SHIFT_RIGHT
        || keycode == Input.Keys.ALT_LEFT
        || keycode == Input.Keys.ALT_RIGHT;
  }

  private static int currentModifiers() {
    int modifiers = 0;
    if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)
        || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) {
      modifiers |= MacroBinding.MOD_CTRL;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
        || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
      modifiers |= MacroBinding.MOD_SHIFT;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT)
        || Gdx.input.isKeyPressed(Input.Keys.ALT_RIGHT)) {
      modifiers |= MacroBinding.MOD_ALT;
    }
    return modifiers;
  }

  private void assignKey(int index, int keycode, int modifiers) {
    List<MacroBinding> macros = macros();
    if (index < 0 || index >= macros.size()) return;
    for (MacroBinding other : macros) {
      if (other.getKeycode() == keycode && other.getModifiers() == modifiers) {
        other.setKeycode(MacroBinding.UNBOUND);
        other.setModifiers(0);
      }
    }
    macros.get(index).setKeycode(keycode);
    macros.get(index).setModifiers(modifiers);
    PlayerStateStore.save(player);
  }

  private void clearKey(int index) {
    List<MacroBinding> macros = macros();
    if (index < 0 || index >= macros.size()) return;
    macros.get(index).setKeycode(MacroBinding.UNBOUND);
    macros.get(index).setModifiers(0);
    PlayerStateStore.save(player);
  }

  private void removeEntry(int index) {
    List<MacroBinding> macros = macros();
    if (index < 0 || index >= macros.size()) return;
    macros.remove(index);
    PlayerStateStore.save(player);
    selected = Math.min(selected, macros.size() - 1);
    scroll(0);
  }

  /** Swaps the selected macro with its neighbour, keeping it selected and in view. */
  void move(int delta) {
    List<MacroBinding> macros = macros();
    int target = selected + delta;
    if (selected < 0 || selected >= macros.size() || target < 0 || target >= macros.size()) return;
    Collections.swap(macros, selected, target);
    selected = target;
    if (selected < firstVisible) firstVisible = selected;
    if (selected >= firstVisible + ROWS_VISIBLE) firstVisible = selected - ROWS_VISIBLE + 1;
    PlayerStateStore.save(player);
  }

  private void scroll(int rows) {
    int maxFirst = Math.max(0, macros().size() - ROWS_VISIBLE);
    firstVisible = Math.max(0, Math.min(maxFirst, firstVisible + rows));
  }

  // ---------------------------------------------------------------- input

  @Override
  public void onMouseMove(float screenX, float screenY) {
    mouseX = screenX;
    mouseY = screenY;
    super.onMouseMove(screenX, screenY);
  }

  @Override
  public void onTouchDown(float screenX, float screenY) {
    mouseX = screenX;
    mouseY = screenY;
    if (listeningIndex >= 0) {
      listeningIndex = -1;
      return;
    }
    for (int row = 0; row < ROWS_VISIBLE; row++) {
      int index = firstVisible + row;
      if (index >= macros().size()) break;
      if (rowCell(row, KEY_X, KEY_W).contains(screenX, screenY)) {
        selected = index;
        listeningIndex = index;
        return;
      }
      if (rowCell(row, 28, 466).contains(screenX, screenY)) {
        selected = index;
        return;
      }
    }
    if (abs(SCROLL).contains(screenX, screenY)) {
      scroll(screenY < y + SCROLL.y + SCROLL.height / 2 ? -1 : 1);
      return;
    }
    super.onTouchDown(screenX, screenY);
  }

  @Override
  public void onScroll(float amountY, float screenX, float screenY) {
    scroll(amountY > 0 ? 1 : -1);
  }

  @Override
  public boolean capturesKeyboard() {
    // While waiting for the key to bind, that key must not also walk or fire a hotkey.
    return listeningIndex >= 0;
  }

  @Override
  public boolean onKeyDown(int keycode) {
    if (listeningIndex >= 0) {
      if (isModifierKey(keycode)) {
        return true;
      }
      if (keycode != Input.Keys.ESCAPE) {
        assignKey(listeningIndex, keycode, currentModifiers());
      }
      listeningIndex = -1;
      return true;
    }
    if (keycode == Input.Keys.ESCAPE) {
      GuiManager.close();
      return true;
    }
    return false;
  }
}
