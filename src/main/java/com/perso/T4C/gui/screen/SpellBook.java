package com.perso.T4C.gui.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.perso.T4C.audio.SoundManager;
import com.perso.T4C.config.MacroBinding;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.gui.core.GuiManager;
import com.perso.T4C.gui.core.CharacterPanel;
import com.perso.T4C.gui.core.JournalTheme;
import com.perso.T4C.gui.core.GuiElement;
import com.perso.T4C.gui.core.GuiScreenBase;
import com.perso.T4C.gui.core.GuiSprites;
import com.perso.T4C.gui.widget.GuiAnimatedSprite;
import com.perso.T4C.gui.widget.GuiBoxedText;
import com.perso.T4C.gui.widget.GuiButton;
import com.perso.T4C.gui.widget.GuiText;
import com.perso.T4C.gui.widget.SpellTooltipText;
import com.perso.T4C.helper.SpriteLoader;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.model.QuickSlotEntry;
import com.perso.T4C.player.Player;
import com.perso.T4C.spell.SpellData;
import com.perso.T4C.spell.SpellEffectManager;
import com.perso.T4C.spell.SpellRegistry;
import com.perso.T4C.ui.FontManager;
import com.perso.T4C.ui.HudTooltip;
import com.perso.T4C.ui.PlayerHUD;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SpellBook extends CharacterPanel {
  private static final int PAGE_SIZE = 10;
  private static final float[][] SLOT_POSITIONS = {
    {28, 150}, {366, 150}, {28, 210}, {366, 210}, {28, 270}, {366, 270},
    {28, 330}, {366, 330}, {28, 390}, {366, 390}
  };
  private static final float ICON_ZONE_W = 38;
  private static final float ICON_ZONE_H = 38;
  private static final float FRAME_TIME = .1f;
  private static final String[] SCHOOLS = {"all", "fire", "earth", "air", "water", "light", "dark", "other"};
  private final List<SpellBookEntry> allSpells = new ArrayList<>();
  private int school;
  private final Player player;
  private final PlayerHUD hud;
  private final List<GuiText> pageLabels = new ArrayList<>();
  private final List<GuiAnimatedSprite> pageSprites = new ArrayList<>();
  private final List<GuiButton> pageButtons = new ArrayList<>();
  private final List<SpellBookEntry> spells = new ArrayList<>();
  private final com.badlogic.gdx.graphics.g2d.GlyphLayout hintLayout =
      new com.badlogic.gdx.graphics.g2d.GlyphLayout();
  private int currentPage = 0;
  private SpellBookEntry draggedSpell;
  private TextureRegion draggedIcon;
  private float draggedX;
  private float draggedY;
  private static final Map<String, SpellData> spellByName = new HashMap<>();
  private static boolean registryReady = false;
  private final HudTooltip tooltip = new HudTooltip();

  public SpellBook(Player player) {
    this(player, null);
  }

  public SpellBook(Player player, PlayerHUD hud) {
    super(player, hud, "ui.journal.spellbook", "spells");
    this.player = player;
    this.hud = hud;
    for (int i = 0; i < SCHOOLS.length; i++) {
      int selected = i;
      buttons.add(theme.button(x + 28 + i * 84, y + 108, 80, 28,
          () -> I18n.key(selected > 0 && selected < 7 ? "element." + SCHOOLS[selected]
              : "ui.journal." + SCHOOLS[selected]), () -> selectSchool(selected)));
    }
    buttons.add(theme.button(x + 28, y + 485, 32, 24, () -> "<", () -> turnPage(-1)));
    buttons.add(theme.button(x + 660, y + 485, 32, 24, () -> ">", () -> turnPage(1)));
    loadSpells();
    selectSchool(0);
  }

  private void selectSchool(int selected) {
    school = selected;
    currentPage = 0;
    spells.clear();
    for (SpellBookEntry entry : allSpells) {
      int element = entry.spell.getElement();
      if (school == 0 || (school == 7 ? element < 1 || element > 6 : element == school)) {
        spells.add(entry);
      }
    }
    rebuildPage();
  }

  private void loadSpells() {
    allSpells.clear();
    if (player == null || player.getSpells() == null) {
      return;
    }
    for (String spellName : player.getSpells()) {
      SpellData data = resolveSpellData(spellName);
      // T4C-0054: some characters saved before the isPlayerCastable denylist existed still have
      // internal-only spells (wrath_of_the_ancients, remort_aura, level_up) in their spell list;
      // keep hiding them here too so those old saves don't keep showing them in the spellbook.
      if (data != null && SpellRegistry.playerCastableSpells().contains(data)) {
        allSpells.add(new SpellBookEntry(spellName, data));
      }
    }
  }

  private SpellData resolveSpellData(String spellName) {
    if (spellName == null || spellName.isEmpty()) {
      return null;
    }
    ensureRegistry();
    SpellData data = spellByName.get(spellName);
    return data != null ? data : SpellRegistry.findByName(spellName);
  }

  private void rebuildPage() {
    if (!pageLabels.isEmpty()) {
      labels.removeAll(pageLabels);
      pageLabels.clear();
    }
    if (!pageSprites.isEmpty()) {
      animatedSprites.removeAll(pageSprites);
      pageSprites.clear();
    }
    if (!pageButtons.isEmpty()) {
      buttons.removeAll(pageButtons);
      pageButtons.clear();
    }
    int start = currentPage * PAGE_SIZE;
    for (int i = 0; i < PAGE_SIZE; i++) {
      int index = start + i;
      if (index >= spells.size()) {
        break;
      }
      float slotX = x + SLOT_POSITIONS[i][0];
      float slotY = y + SLOT_POSITIONS[i][1];
      addSpellEntry(spells.get(index), i, slotX, slotY);
    }
  }

  private void addSpellEntry(SpellBookEntry entry, int slotIndex, float slotX, float slotY) {
    SpellData spell = entry.spell;
    TextureRegion icon = loadSpellIcon(spell);
    if (icon != null) {
      GuiAnimatedSprite sprite = new GuiAnimatedSprite(List.of(icon), slotX + 7, slotY + 8,
          FRAME_TIME).boxed(ICON_ZONE_W, ICON_ZONE_H);
      animatedSprites.add(sprite);
      pageSprites.add(sprite);
    }
    var nameFont = FontManager.getInstance().getTahomaFont(14, JournalTheme.TEXT, false);
    var detailFont = FontManager.getInstance().getTahomaFont(11, JournalTheme.DIM, false);
    addPageBox(nameFont, slotX + 52, slotY + 3, 230, 20,
        () -> I18n.resolve(spell.getName()), GuiBoxedText.Align.LEFT);
    String type = I18n.key(spell.getAttackType() == SpellData.ATTACK_MENTAL
        ? "ui.spell_type.mental" : "ui.spell_type.physical");
    addPageBox(detailFont, slotX + 52, slotY + 24, 152, 13,
        () -> type + " / " + formatDuration(spell), GuiBoxedText.Align.LEFT);
    addPageBox(detailFont, slotX + 52, slotY + 38, 232, 13,
        () -> I18n.key("ui.spell_stat.mana") + " " + spell.getManaCost(), GuiBoxedText.Align.LEFT);
    GuiButton button = theme.button(slotX + 291, slotY + 16, 24, 24,
        () -> isMacro(entry.quickSlotSpellName) ? "-" : "+",
        () -> toggleMacro(entry.quickSlotSpellName));
    buttons.add(button);
    pageButtons.add(button);
  }

  private boolean isMacro(String spellName) {
    return player.getMacros().stream()
        .anyMatch(m -> spellName.equals(m.getSpellName()));
  }

  private void toggleMacro(String spellName) {
    List<MacroBinding> macros = player.getMacros();
    if (!macros.removeIf(m -> spellName.equals(m.getSpellName()))) {
      macros.add(new MacroBinding(spellName, MacroBinding.UNBOUND, 0));
    }
    PlayerHUD.persistenceOf(hud).save(player);
  }

  private void addPageBox(
      com.badlogic.gdx.graphics.g2d.BitmapFont font,
      float bx,
      float by,
      float bw,
      float bh,
      java.util.function.Supplier<String> text,
      GuiBoxedText.Align align) {
    GuiBoxedText label = new GuiBoxedText(font, bx, by, bw, bh, text, null).align(align).shrinkToFit();
    labels.add(label);
    pageLabels.add(label);
  }

  private void turnPage(int delta) {
    int maxPage = spells.isEmpty() ? 0 : (spells.size() - 1) / PAGE_SIZE;
    int next = Math.max(0, Math.min(maxPage, currentPage + delta));
    if (next == currentPage) {
      return;
    }
    SoundManager.animateSound("Page turning sound.wav");
    currentPage = next;
    rebuildPage();
  }

  /** The spell's duration for this character, as a player would say it ("45 s", "5 min"). */
  private String formatDuration(SpellData spell) {
    int seconds = player == null ? 0 : new SpellEffectManager().resolveDurationSeconds(spell, player);
    return formatSeconds(seconds);
  }

  static String formatSeconds(int seconds) {
    if (seconds <= 0) return I18n.key("ui.spell_duration.instant");
    if (seconds < 60) return seconds + " s";
    if (seconds < 3600) {
      int minutes = seconds / 60;
      int rest = seconds % 60;
      return rest == 0 ? minutes + " min" : minutes + " min " + rest + " s";
    }
    int hours = seconds / 3600;
    int minutes = (seconds % 3600) / 60;
    return minutes == 0 ? hours + " h" : hours + " h " + minutes + " min";
  }

  private int pageCount() {
    return Math.max(1, (spells.size() + PAGE_SIZE - 1) / PAGE_SIZE);
  }

  /** The spell whose "+"/"-" macro button is under the mouse, for the hint line. */
  private String hoveredMacroSpell() {
    for (int i = 0; i < pageButtons.size(); i++) {
      if (pageButtons.get(i).contains(Gdx.input.getX(), Gdx.input.getY())) {
        return spells.get(currentPage * PAGE_SIZE + i).quickSlotSpellName;
      }
    }
    return null;
  }

  @Override
  public void dispose() {
    tooltip.dispose();
    super.dispose();
  }

  @Override
  public void render(com.badlogic.gdx.graphics.g2d.SpriteBatch batch) {
    renderPanelBackground(batch);
    theme.rect(batch, x + 28 + school * 84, y + 135, 80, 2, JournalTheme.GOLD);
    for (int i = 0; i < PAGE_SIZE; i++) {
      well(batch, SLOT_POSITIONS[i][0], SLOT_POSITIONS[i][1], 326, 55);
    }
    for (GuiElement element : orderedElements()) element.render(batch);
    if (background != null) {
      // Status line in the dark band above the pages: page number and what the controls do.
      String macroSpell = hoveredMacroSpell();
      String hint;
      if (spells.isEmpty()) {
        hint = I18n.key(allSpells.isEmpty() ? "ui.spellbook.empty" : "ui.journal.no_spells");
      } else if (macroSpell != null) {
        hint = I18n.key(isMacro(macroSpell) ? "ui.spellbook.remove_macro" : "ui.spellbook.add_macro");
      } else {
        hint =
            I18n.message(
                "ui.spellbook.status",
                String.valueOf(currentPage + 1),
                String.valueOf(pageCount()));
      }
      var font = FontManager.getInstance().getJetBrainsMonoFont(11, Color.valueOf("E6D8BC"));
      hintLayout.setText(font, hint);
      font.draw(batch, hintLayout, x + 360f - hintLayout.width / 2f, y + 491f);
    }
    updateSpellTooltip();
    tooltip.render(batch);
    if (draggedSpell != null && draggedIcon != null) {
      float w = draggedIcon.getRegionWidth();
      float h = draggedIcon.getRegionHeight();
      batch.draw(
          draggedIcon.getTexture(),
          draggedX - w * 0.5f,
          draggedY - h * 0.5f,
          w,
          h,
          draggedIcon.getRegionX(),
          draggedIcon.getRegionY(),
          draggedIcon.getRegionWidth(),
          draggedIcon.getRegionHeight(),
          false,
          true);
    }
  }

  @Override
  public void onScroll(float amountY, float screenX, float screenY) {
    turnPage(amountY > 0 ? 1 : -1);
  }

  @Override
  public boolean onKeyDown(int keycode) {
    if (keycode == com.badlogic.gdx.Input.Keys.ESCAPE) {
      GuiManager.close();
      return true;
    }
    if (keycode == Input.Keys.PAGE_DOWN || keycode == Input.Keys.PAGE_UP) {
      turnPage(keycode == Input.Keys.PAGE_DOWN ? 1 : -1);
      return true;
    }
    return super.onKeyDown(keycode);
  }

  private void updateSpellTooltip() {
    if (draggedSpell != null) {
      tooltip.clear();
      return;
    }
    float mx = Gdx.input.getX();
    float my = Gdx.input.getY();
    SpellBookEntry hovered = findSpellAt(mx, my);
    if (hovered == null) {
      tooltip.clear();
      return;
    }
    tooltip.show(SpellTooltipText.build(hovered.spell), mx, my);
  }

  private SpellBookEntry findSpellAt(float screenX, float screenY) {
    int start = currentPage * PAGE_SIZE;
    for (int i = 0; i < PAGE_SIZE; i++) {
      int index = start + i;
      if (index >= spells.size()) {
        break;
      }
      SpellBookEntry entry = spells.get(index);
      TextureRegion icon = loadSpellIcon(entry.spell);
      if (icon == null) {
        continue;
      }
      float slotX = x + SLOT_POSITIONS[i][0];
      float slotY = y + SLOT_POSITIONS[i][1];
      float iconX = slotX + 7;
      float iconY = slotY + 8;
      if (screenX >= iconX
          && screenX <= iconX + ICON_ZONE_W
          && screenY >= iconY
          && screenY <= iconY + ICON_ZONE_H) {
        return entry;
      }
    }
    return null;
  }

  private TextureRegion loadSpellIcon(SpellData spell) {
    if (spell == null || spell.getIconId() == null || spell.getIconId().isEmpty()) {
      return null;
    }
    try {
      return SpriteLoader.getInstance().getRegionFromSpriteName(spell.getIconId());
    } catch (GameException ignored) {
      return null;
    }
  }

  private void assignSpellToQuickSlot(String spellName, int slot) {
    if (player == null || spellName == null || spellName.isEmpty() || slot <= 0) {
      return;
    }
    List<QuickSlotEntry> quickSlots = player.getQuickSlots();
    if (quickSlots == null) {
      quickSlots = new ArrayList<>();
      player.setQuickSlots(quickSlots);
    }
    QuickSlotEntry target = null;
    for (Iterator<QuickSlotEntry> it = quickSlots.iterator(); it.hasNext(); ) {
      QuickSlotEntry entry = it.next();
      if (entry == null) {
        it.remove();
        continue;
      }
      if (entry.getSlot() == slot) {
        target = entry;
      } else if (spellName.equals(entry.getSpell())) {
        it.remove();
      }
    }
    if (target == null) {
      quickSlots.add(new QuickSlotEntry(slot, spellName));
    } else {
      target.setSpell(spellName);
    }
    PlayerHUD.persistenceOf(hud).save(player);
  }

  private static void ensureRegistry() {
    if (registryReady) {
      return;
    }
    registryReady = true;
    for (SpellData data : SpellRegistry.load()) {
      if (data != null && data.getName() != null && !data.getName().isEmpty()) {
        spellByName.put(data.getName(), data);
        if (data.getKey() != null) {
          spellByName.put(data.getKey(), data);
        }
      }
    }
  }

  private static final class SpellBookEntry {
    private final String quickSlotSpellName;
    private final SpellData spell;

    private SpellBookEntry(String quickSlotSpellName, SpellData spell) {
      this.quickSlotSpellName = quickSlotSpellName;
      this.spell = spell;
    }
  }
}
