package com.perso.T4C.online;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.CreatureView;
import com.perso.T4C.online.model.SpellContent;
import com.perso.T4C.online.model.SpellContentV2;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.function.Function;

/**
 * Key-state logic and text for the native spell and potion lists. The lists are plain HUD text; this
 * class holds which list is open, builds its lines and turns a digit into a cast or use request. It
 * never talks to the network: the caller sends the returned {@link Action} through the connection and
 * the server stays the authority on mana, cooldown, exhaustion and effect.
 */
public final class OnlineAbilityMenu {
  public enum Kind { NONE, SPELLS, POTIONS }

  /** A spell key or potion identity plus the target form needed by the selected cast. */
  public record Action(
      Kind kind,
      String spellKey,
      UUID item,
      boolean requiresCreatureTarget,
      boolean requiresPositionTarget) {}

  private final Function<String, ItemDefinition> items;
  private Kind open = Kind.NONE;

  public OnlineAbilityMenu() {
    this(ItemRegistry::findByKey);
  }

  OnlineAbilityMenu(Function<String, ItemDefinition> items) {
    this.items = items;
  }

  public boolean isOpen() {
    return open != Kind.NONE;
  }

  public Kind kind() {
    return open;
  }

  public void close() {
    open = Kind.NONE;
  }

  /**
   * Pressing the key of the open list closes it, the other key switches lists. When the character is
   * dead or disconnected the press is ignored and nothing stays open.
   */
  public void toggle(Kind requested, boolean available) {
    if (requested == Kind.NONE || !available || open == requested) open = Kind.NONE;
    else open = requested;
  }

  /** True only for a connected character that is alive. */
  public static boolean available(String status, CharacterProgress progress) {
    return "connected".equals(status) && progress != null && progress.vitals().hp() > 0;
  }

  /** Resolves only the currently selected, still-living creature from the latest server snapshot. */
  public static OptionalLong liveCreatureTarget(long selectedId, List<CreatureView> creatures) {
    if (selectedId <= 0 || creatures == null) return OptionalLong.empty();
    return creatures.stream()
        .filter(creature -> creature.id() == selectedId && creature.health() > 0)
        .mapToLong(CreatureView::id)
        .findFirst();
  }

  /**
   * Digit 1-9 picks a row of the open list. A chosen row closes the list; a digit with no row, or a
   * press while unavailable, returns empty (an unavailable state also closes the list).
   */
  public Optional<Action> choose(int digit, CharacterProgress progress, SpellContentV2 engineCatalog,
      SpellContent potionAllowlist, boolean available) {
    if (!isOpen()) return Optional.empty();
    if (!available) {
      close();
      return Optional.empty();
    }
    Optional<Action> picked =
        open == Kind.SPELLS
            ? OnlineAbilityChoices.choose(OnlineAbilityChoices.castableSpells(progress, engineCatalog), digit)
                .map(
                    row ->
                        new Action(
                            Kind.SPELLS,
                            row.key(),
                            null,
                            row.requiresCreatureTarget(),
                            row.requiresPositionTarget()))
            : OnlineAbilityChoices.choose(
                    OnlineAbilityChoices.usablePotions(progress, potionAllowlist, items), digit)
                .map(row -> new Action(Kind.POTIONS, null, row.instance(), false, false));
    picked.ifPresent(action -> close());
    return picked;
  }

  /** Uses the same bundled spell catalogs as the native online client. */
  public Optional<Action> choose(int digit, CharacterProgress progress, boolean available) {
    return choose(digit, progress, OnlineSpellAllowlistV2.bundled(), OnlineSpellAllowlist.bundled(), available);
  }

  /** Compatibility overload for the preview harness; spells use the bundled v2 engine catalog. */
  List<String> lines(CharacterProgress progress, SpellContent potionAllowlist) {
    return lines(progress, OnlineSpellAllowlistV2.bundled(), potionAllowlist);
  }

  /** Title, one line per row (first nine), an overflow note if needed, and the close hint. */
  List<String> lines(CharacterProgress progress, SpellContentV2 engineCatalog, SpellContent potionAllowlist) {
    var lines = new ArrayList<String>();
    if (open == Kind.SPELLS) {
      lines.add(I18n.key("online.ability.spells_title"));
      var rows = OnlineAbilityChoices.castableSpells(progress, engineCatalog);
      if (rows.isEmpty()) lines.add(I18n.key("online.ability.spells_empty"));
      for (int i = 0; i < rows.size() && i < OnlineAbilityChoices.MAX_CHOICES; i++)
        lines.add(
            I18n.message("online.ability.spell_row", i + 1, rows.get(i).name(), rows.get(i).manaCostLabel()));
      more(lines, rows.size());
      lines.add(I18n.key("online.ability.spells_close"));
    } else if (open == Kind.POTIONS) {
      lines.add(I18n.key("online.ability.potions_title"));
      var rows = OnlineAbilityChoices.usablePotions(progress, potionAllowlist, items);
      if (rows.isEmpty()) lines.add(I18n.key("online.ability.potions_empty"));
      for (int i = 0; i < rows.size() && i < OnlineAbilityChoices.MAX_CHOICES; i++)
        lines.add(
            I18n.message("online.ability.potion_row", i + 1, rows.get(i).name(), rows.get(i).quantity()));
      more(lines, rows.size());
      lines.add(I18n.key("online.ability.potions_close"));
    }
    return lines;
  }

  /** Builds the current list using the same bundled catalogs as the native online client. */
  public List<String> lines(CharacterProgress progress) {
    return lines(progress, OnlineSpellAllowlistV2.bundled(), OnlineSpellAllowlist.bundled());
  }

  private static void more(List<String> lines, int total) {
    if (total > OnlineAbilityChoices.MAX_CHOICES)
      lines.add(I18n.message("online.ability.more", total - OnlineAbilityChoices.MAX_CHOICES));
  }

  /** Draws the lines as plain HUD text in the inventory list's style: title, rows, then the hint. */
  static void draw(SpriteBatch batch, BitmapFont font, List<String> lines, float x, float top) {
    float line = font.getLineHeight(), y = top;
    for (int i = 0; i < lines.size(); i++) {
      if (i == lines.size() - 1 && i > 0) y -= line * 0.5f;
      font.draw(batch, lines.get(i), x, y);
      y -= i == 0 ? line * 1.5f : line;
    }
  }
}
