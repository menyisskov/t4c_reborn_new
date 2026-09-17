package com.perso.T4C.gui.screen;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.perso.T4C.config.GamePreferencesStore;
import com.perso.T4C.config.MacroBinding;
import com.perso.T4C.gui.core.GuiElement;
import com.perso.T4C.gui.core.GuiManager;
import com.perso.T4C.gui.core.GuiScreenBase;
import com.perso.T4C.gui.core.GuiSprites;
import com.perso.T4C.gui.widget.GuiBoxedText;
import com.perso.T4C.gui.widget.GuiButton;
import com.perso.T4C.gui.widget.GuiText;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.spell.SpellData;
import com.perso.T4C.spell.SpellRegistry;
import com.perso.T4C.ui.FontManager;
import java.util.ArrayList;
import java.util.List;

public class MacrosScreen extends GuiScreenBase {
  private static final int ROWS_VISIBLE = 6;
  private static final float ROW_0_Y = 62f;
  private static final float ROW_PITCH = 38f;
  private static final Color GOLD = Color.valueOf("DF9D00");
  private static final Color WHITE = Color.WHITE;
  private static final Color DIM = Color.valueOf("AAAAAA");

  private final List<GuiText> rowLabels = new ArrayList<>();
  private final List<GuiButton> rowButtons = new ArrayList<>();
  private final List<GuiElement> controls = new ArrayList<>();
  private int page = 0;
  private int listeningIndex = -1;

  public MacrosScreen() {
    background = GuiSprites.load("GUI_BackOption");
    centerOnScreen();
    if (background == null) {
      return;
    }
    addCloseButton(484f, 0f);
    addHeader();
    addNavButtons();
    rebuildRows();
  }

  private void addHeader() {
    var header = FontManager.getInstance().getHaettenschweilerFont(17, GOLD);
    labels.add(
        new GuiBoxedText(header, x + 207f, y + 2f, 101f, 19f, () -> I18n.key("ui.macros"), () -> GOLD)
            .shrinkToFit());
    var small = FontManager.getInstance().getT4CBeaulieuFont(13, GOLD);
    labels.add(new GuiBoxedText(small, x + 22f, y + 40f, 180f, 16f, () -> I18n.key("ui.macros.spell"), () -> GOLD));
    labels.add(new GuiBoxedText(small, x + 210f, y + 40f, 90f, 16f, () -> I18n.key("ui.macros.key"), () -> GOLD));
  }

  private void addNavButtons() {
    controls.add(navButton(310f, 300f, "<", () -> turnPage(-1)));
    controls.add(navButton(370f, 300f, ">", () -> turnPage(1)));
  }

  private GuiButton navButton(float dx, float dy, String label, Runnable action) {
    var normal = GuiSprites.load("GUI_ButtonUp");
    var hover = GuiSprites.load("GUI_ButtonHUp");
    var pressed = GuiSprites.load("GUI_ButtonDown");
    var font = FontManager.getInstance().getT4CBeaulieuFont(15, Color.BLACK);
    GuiButton button = new GuiButton(normal, hover, pressed, x + dx, y + dy, action).setSize(30f, 24f);
    if (normal != null && hover != null && pressed != null) {
      button.withLabel(font, () -> label);
    }
    return button;
  }

  private void turnPage(int delta) {
    List<MacroBinding> macros = GamePreferencesStore.get().getMacros();
    int maxPage = macros.isEmpty() ? 0 : (macros.size() - 1) / ROWS_VISIBLE;
    int next = Math.max(0, Math.min(maxPage, page + delta));
    if (next == page) {
      return;
    }
    page = next;
    rebuildRows();
  }

  private void rebuildRows() {
    labels.removeAll(rowLabels);
    rowLabels.clear();
    buttons.removeAll(rowButtons);
    rowButtons.clear();

    List<MacroBinding> macros = GamePreferencesStore.get().getMacros();
    if (macros.isEmpty()) {
      var text = FontManager.getInstance().getT4CBeaulieuFont(14, DIM);
      GuiBoxedText empty =
          new GuiBoxedText(text, x + 22f, y + ROW_0_Y, 460f, 16f, () -> I18n.key("ui.macros.empty"), () -> DIM);
      labels.add(empty);
      rowLabels.add(empty);
      return;
    }

    int start = page * ROWS_VISIBLE;
    int end = Math.min(start + ROWS_VISIBLE, macros.size());
    for (int i = start; i < end; i++) {
      addRow(macros, i, ROW_0_Y + (i - start) * ROW_PITCH);
    }
  }

  private void addRow(List<MacroBinding> macros, int index, float rowY) {
    MacroBinding macro = macros.get(index);
    var nameFont = FontManager.getInstance().getT4CBeaulieuFont(14, WHITE);
    var keyFont = FontManager.getInstance().getT4CBeaulieuFont(14, GOLD);

    GuiBoxedText nameLabel =
        new GuiBoxedText(nameFont, x + 22f, y + rowY, 180f, 16f, () -> displayName(macro), () -> WHITE)
            .shrinkToFit();
    labels.add(nameLabel);
    rowLabels.add(nameLabel);

    GuiBoxedText keyLabel =
        new GuiBoxedText(keyFont, x + 210f, y + rowY, 90f, 16f, () -> keyDisplay(macro, index), () -> GOLD)
            .shrinkToFit();
    labels.add(keyLabel);
    rowLabels.add(keyLabel);

    GuiButton bind = rowButton(304f, rowY - 4f, "ui.macros.bind", () -> beginListening(index));
    GuiButton clear = rowButton(372f, rowY - 4f, "ui.macros.clear", () -> clearKey(index));
    clear.setEnabled(macro.getKeycode() != MacroBinding.UNBOUND);
    GuiButton remove = rowButton(440f, rowY - 4f, "ui.macros.remove", () -> removeEntry(index));

    buttons.add(bind);
    buttons.add(clear);
    buttons.add(remove);
    rowButtons.add(bind);
    rowButtons.add(clear);
    rowButtons.add(remove);
  }

  private GuiButton rowButton(float dx, float dy, String labelKey, Runnable action) {
    var normal = GuiSprites.load("GUI_ButtonUp");
    var hover = GuiSprites.load("GUI_ButtonHUp");
    var pressed = GuiSprites.load("GUI_ButtonDown");
    var font = FontManager.getInstance().getTahomaFont(11, Color.BLACK, true);
    GuiButton button =
        new GuiButton(normal, hover, pressed, x + dx, y + dy, action).setSize(60f, 22f);
    if (normal != null && hover != null && pressed != null) {
      button.withLabel(font, () -> I18n.key(labelKey));
    }
    return button;
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
    return modifierPrefix(macro.getModifiers()) + Input.Keys.toString(macro.getKeycode());
  }

  private static String modifierPrefix(int modifiers) {
    StringBuilder prefix = new StringBuilder();
    if ((modifiers & MacroBinding.MOD_CTRL) != 0) prefix.append("Ctrl+");
    if ((modifiers & MacroBinding.MOD_SHIFT) != 0) prefix.append("Shift+");
    if ((modifiers & MacroBinding.MOD_ALT) != 0) prefix.append("Alt+");
    return prefix.toString();
  }

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
    if (com.badlogic.gdx.Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)
        || com.badlogic.gdx.Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) {
      modifiers |= MacroBinding.MOD_CTRL;
    }
    if (com.badlogic.gdx.Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
        || com.badlogic.gdx.Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
      modifiers |= MacroBinding.MOD_SHIFT;
    }
    if (com.badlogic.gdx.Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT)
        || com.badlogic.gdx.Gdx.input.isKeyPressed(Input.Keys.ALT_RIGHT)) {
      modifiers |= MacroBinding.MOD_ALT;
    }
    return modifiers;
  }

  private void beginListening(int index) {
    listeningIndex = index;
  }

  private void assignKey(int index, int keycode, int modifiers) {
    List<MacroBinding> macros = GamePreferencesStore.get().getMacros();
    if (index < 0 || index >= macros.size()) {
      return;
    }
    for (MacroBinding other : macros) {
      if (other.getKeycode() == keycode && other.getModifiers() == modifiers) {
        other.setKeycode(MacroBinding.UNBOUND);
        other.setModifiers(0);
      }
    }
    macros.get(index).setKeycode(keycode);
    macros.get(index).setModifiers(modifiers);
    GamePreferencesStore.save();
    rebuildRows();
  }

  private void clearKey(int index) {
    List<MacroBinding> macros = GamePreferencesStore.get().getMacros();
    if (index < 0 || index >= macros.size()) {
      return;
    }
    macros.get(index).setKeycode(MacroBinding.UNBOUND);
    macros.get(index).setModifiers(0);
    GamePreferencesStore.save();
    rebuildRows();
  }

  private void removeEntry(int index) {
    List<MacroBinding> macros = GamePreferencesStore.get().getMacros();
    if (index < 0 || index >= macros.size()) {
      return;
    }
    macros.remove(index);
    GamePreferencesStore.save();
    rebuildRows();
  }

  @Override
  protected List<GuiElement> extraElements() {
    return controls;
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
