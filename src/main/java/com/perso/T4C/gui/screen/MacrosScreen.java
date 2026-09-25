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
import com.perso.T4C.config.GamePreferencesStore;
import com.perso.T4C.config.MacroBinding;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.gui.core.GuiDraw;
import com.perso.T4C.gui.core.GuiManager;
import com.perso.T4C.gui.core.GuiScreenBase;
import com.perso.T4C.gui.core.GuiSprites;
import com.perso.T4C.helper.SpriteLoader;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.player.Player;
import com.perso.T4C.spell.SpellData;
import com.perso.T4C.spell.SpellRegistry;
import com.perso.T4C.ui.FontManager;
import java.util.Collections;
import java.util.List;

/**
 * Macro list (Ctrl+M) on the original macro window art: one row per macro with the spell's icon,
 * name and key. Select a row, then Bind (or click its key box) and press the key to use; the
 * arrows reorder, Clear unbinds, Remove deletes. Macros are added from the spell book's "+".
 */
public class MacrosScreen extends GuiScreenBase {
  static final int ROWS_VISIBLE = 6;
  // Window-relative geometry of GUI_BackMacro (576x368).
  private static final float ROW_PITCH = 46f;
  private static final float ROW_Y = 60f;
  private static final float ROW_H = 18f;
  private static final float ICON_CX = 42f;
  private static final float ICON_CY = 68f;
  private static final float NAME_X = 78f;
  private static final float NAME_W = 180f;
  private static final float KEY_X = 265f;
  private static final float KEY_W = 108f;
  private static final Rectangle SCROLL = new Rectangle(380f, 38f, 26f, 270f);
  private static final Rectangle BOOK_ICON = new Rectangle(420f, 40f, 38f, 40f);
  private static final Rectangle HELP_ICON = new Rectangle(518f, 40f, 38f, 40f);
  private static final Rectangle MOVE_DOWN = new Rectangle(446f, 146f, 38f, 38f);
  private static final Rectangle MOVE_UP = new Rectangle(494f, 146f, 38f, 38f);
  private static final Rectangle BIND = new Rectangle(420f, 96f, 72f, 26f);
  private static final Rectangle CLEAR = new Rectangle(496f, 96f, 72f, 26f);
  private static final Rectangle REMOVE = new Rectangle(420f, 188f, 148f, 22f);
  private static final Rectangle HINT_PANEL = new Rectangle(418f, 214f, 152f, 112f);
  private static final Color GOLD = Color.valueOf("F2B705");
  private static final Color TEXT = Color.valueOf("E6D8BC");
  private static final Color DIM = Color.valueOf("A89E86");

  private final Player player;
  private final GlyphLayout layout = new GlyphLayout();
  private final TextureRegion[] button = new TextureRegion[3];
  private final TextureRegion scrollTick;
  private int firstVisible;
  private int selected;
  private int listeningIndex = -1;
  private float mouseX = -1f;
  private float mouseY = -1f;

  public MacrosScreen() {
    this(null);
  }

  public MacrosScreen(Player player) {
    this.player = player;
    background = GuiSprites.load("GUI_BackMacro");
    scrollTick = GuiSprites.load("GUI_ScrollTick");
    button[0] = GuiSprites.load("GUI_ButtonUp");
    button[1] = GuiSprites.load("GUI_ButtonHUp");
    button[2] = GuiSprites.load("GUI_ButtonDown");
    centerOnScreen();
    if (background == null) return;
    addCloseButton(548f, 5f);
  }

  private static List<MacroBinding> macros() {
    return GamePreferencesStore.get().getMacros();
  }

  private Rectangle abs(Rectangle r) {
    return new Rectangle(x + r.x, y + r.y, r.width, r.height);
  }

  private Rectangle rowCell(int row, float cellX, float cellW) {
    return new Rectangle(x + cellX, y + ROW_Y + row * ROW_PITCH, cellW, ROW_H);
  }

  // ---------------------------------------------------------------- rendering

  @Override
  public void render(SpriteBatch batch) {
    if (background == null) {
      super.render(batch);
      return;
    }
    TextureRegion art = background;
    background = null;
    GuiDraw.drawOverlayWithPatch(batch, art, x, y, 419, 215, 149, 113, 420, 88, 140, 52);
    super.render(batch);
    background = art;

    BitmapFont title = FontManager.getInstance().getHaettenschweilerFont(18, GOLD);
    centered(batch, title, I18n.key("ui.macros"), x + 237f, y + 3f, 101f);
    BitmapFont header = FontManager.getInstance().getTahomaFont(12, GOLD, true);
    centered(batch, header, I18n.key("ui.macros.spell"), x + 116f, y + 40f, 103f);
    centered(batch, header, I18n.key("ui.macros.key"), x + 282f, y + 40f, 72f);

    drawRows(batch);
    drawScrollTick(batch);
    boolean hasSelection = selected >= 0 && selected < macros().size();
    MacroBinding current = hasSelection ? macros().get(selected) : null;
    drawButton(batch, BIND, I18n.key("ui.macros.bind"), hasSelection);
    drawButton(
        batch,
        CLEAR,
        I18n.key("ui.macros.clear"),
        current != null && current.getKeycode() != MacroBinding.UNBOUND);
    drawButton(batch, REMOVE, I18n.key("ui.macros.remove"), hasSelection);

    BitmapFont hint = FontManager.getInstance().getJetBrainsMonoFont(10, DIM);
    hint.draw(
        batch,
        I18n.key(hoverHintKey()),
        x + HINT_PANEL.x + 6f,
        y + HINT_PANEL.y + 6f,
        HINT_PANEL.width - 12f,
        Align.left,
        true);
  }

  private void drawRows(SpriteBatch batch) {
    List<MacroBinding> macros = macros();
    BitmapFont name = FontManager.getInstance().getTahomaFont(12, TEXT, true);
    BitmapFont hovered = FontManager.getInstance().getTahomaFont(12, Color.WHITE, true);
    BitmapFont active = FontManager.getInstance().getTahomaFont(12, GOLD, true);
    BitmapFont key = FontManager.getInstance().getJetBrainsMonoFont(11, GOLD);
    BitmapFont unbound = FontManager.getInstance().getJetBrainsMonoFont(11, DIM);
    if (macros.isEmpty()) {
      BitmapFont dim = FontManager.getInstance().getJetBrainsMonoFont(11, DIM);
      dim.draw(
          batch, I18n.key("ui.macros.empty"), x + NAME_X, y + ROW_Y + 2f, 290f, Align.left, true);
      return;
    }
    for (int row = 0; row < ROWS_VISIBLE; row++) {
      int index = firstVisible + row;
      if (index >= macros.size()) break;
      MacroBinding macro = macros.get(index);
      TextureRegion icon = icon(macro);
      if (icon != null) {
        float size = 30f;
        float scale = Math.min(size / icon.getRegionWidth(), size / icon.getRegionHeight());
        float w = icon.getRegionWidth() * scale;
        float h = icon.getRegionHeight() * scale;
        GuiDraw.drawRegionFlipped(
            batch, icon, x + ICON_CX - w / 2f, y + ICON_CY + row * ROW_PITCH - h / 2f, w, h);
      }
      Rectangle nameCell = rowCell(row, NAME_X, NAME_W);
      boolean hover = nameCell.contains(mouseX, mouseY) || rowCell(row, KEY_X, KEY_W).contains(mouseX, mouseY);
      BitmapFont nameFont = index == selected ? active : hover ? hovered : name;
      layout.setText(nameFont, displayName(macro), nameFont.getColor(), NAME_W - 10f, Align.left, false);
      nameFont.draw(batch, layout, nameCell.x + 6f, nameCell.y + 3f);
      String keyText = keyDisplay(macro, index);
      boolean isUnbound = macro.getKeycode() == MacroBinding.UNBOUND && listeningIndex != index;
      BitmapFont keyFont = isUnbound ? unbound : key;
      if (listeningIndex == index && (System.currentTimeMillis() / 400L) % 2 == 1) keyText = "";
      Rectangle keyCell = rowCell(row, KEY_X, KEY_W);
      centered(batch, keyFont, keyText, keyCell.x, keyCell.y + 3f, keyCell.width);
    }
  }

  private void drawScrollTick(SpriteBatch batch) {
    int maxFirst = Math.max(0, macros().size() - ROWS_VISIBLE);
    if (scrollTick == null || maxFirst == 0) return;
    float travel = SCROLL.height - 60f - scrollTick.getRegionHeight();
    GuiDraw.drawRegionFlipped(
        batch,
        scrollTick,
        x + SCROLL.x + (SCROLL.width - scrollTick.getRegionWidth()) / 2f,
        y + SCROLL.y + 30f + travel * firstVisible / maxFirst);
  }

  private void drawButton(SpriteBatch batch, Rectangle rel, String label, boolean enabled) {
    Rectangle r = abs(rel);
    boolean hover = enabled && r.contains(mouseX, mouseY);
    TextureRegion bg = hover ? button[1] : button[0];
    if (bg != null) {
      Color previous = new Color(batch.getColor());
      if (!enabled) batch.setColor(0.55f, 0.55f, 0.55f, previous.a);
      GuiDraw.drawRegionFlipped(batch, bg, r.x, r.y, r.width, r.height);
      batch.setColor(previous);
    }
    BitmapFont font =
        FontManager.getInstance()
            .getTahomaFont(12, enabled ? Color.BLACK : Color.valueOf("5A5040"), true);
    centered(batch, font, label, r.x, r.y + (r.height - 13f) / 2f, r.width);
  }

  private String hoverHintKey() {
    if (listeningIndex >= 0) return "ui.macros.hint_listening";
    if (abs(BOOK_ICON).contains(mouseX, mouseY)) return "ui.macros.hint_book";
    if (abs(HELP_ICON).contains(mouseX, mouseY)) return "ui.macros.hint_help";
    if (abs(MOVE_UP).contains(mouseX, mouseY)) return "ui.macros.hint_up";
    if (abs(MOVE_DOWN).contains(mouseX, mouseY)) return "ui.macros.hint_down";
    return "ui.macros.hint";
  }

  private void centered(SpriteBatch batch, BitmapFont font, String text, float bx, float by, float bw) {
    layout.setText(font, text);
    font.draw(batch, layout, bx + Math.round((bw - layout.width) / 2f), by);
  }

  private static TextureRegion icon(MacroBinding macro) {
    SpellData spell = SpellRegistry.findByName(macro.getSpellName());
    if (spell == null || spell.getIconId() == null || spell.getIconId().isEmpty()) return null;
    try {
      return SpriteLoader.getInstance().getRegionFromSpriteName(spell.getIconId());
    } catch (GameException e) {
      return null;
    }
  }

  private String displayName(MacroBinding macro) {
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
    GamePreferencesStore.save();
  }

  private void clearKey(int index) {
    List<MacroBinding> macros = macros();
    if (index < 0 || index >= macros.size()) return;
    macros.get(index).setKeycode(MacroBinding.UNBOUND);
    macros.get(index).setModifiers(0);
    GamePreferencesStore.save();
  }

  private void removeEntry(int index) {
    List<MacroBinding> macros = macros();
    if (index < 0 || index >= macros.size()) return;
    macros.remove(index);
    GamePreferencesStore.save();
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
    GamePreferencesStore.save();
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
      // Clicking anywhere cancels a pending key capture.
      listeningIndex = -1;
      return;
    }
    List<MacroBinding> macros = macros();
    for (int row = 0; row < ROWS_VISIBLE; row++) {
      int index = firstVisible + row;
      if (index >= macros.size()) break;
      if (rowCell(row, KEY_X, KEY_W).contains(screenX, screenY)) {
        selected = index;
        listeningIndex = index;
        return;
      }
      Rectangle wholeRow =
          new Rectangle(x + 20f, y + ICON_CY + row * ROW_PITCH - 22f, KEY_X + KEY_W - 20f, 44f);
      if (wholeRow.contains(screenX, screenY)) {
        selected = index;
        return;
      }
    }
    boolean hasSelection = selected >= 0 && selected < macros.size();
    if (hasSelection && abs(BIND).contains(screenX, screenY)) {
      listeningIndex = selected;
    } else if (hasSelection && abs(CLEAR).contains(screenX, screenY)) {
      clearKey(selected);
    } else if (hasSelection && abs(REMOVE).contains(screenX, screenY)) {
      removeEntry(selected);
    } else if (abs(MOVE_UP).contains(screenX, screenY)) {
      move(-1);
    } else if (abs(MOVE_DOWN).contains(screenX, screenY)) {
      move(1);
    } else if (abs(BOOK_ICON).contains(screenX, screenY)) {
      if (player != null) GuiManager.open(new SpellBook(player));
    } else if (abs(HELP_ICON).contains(screenX, screenY)) {
      GuiManager.open(new ControlsScreen());
    } else if (abs(SCROLL).contains(screenX, screenY)) {
      scroll(screenY < y + SCROLL.y + SCROLL.height / 2f ? -1 : 1);
    } else {
      super.onTouchDown(screenX, screenY);
    }
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
