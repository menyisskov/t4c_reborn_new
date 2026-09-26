package com.perso.T4C.gui.widget;

import com.perso.T4C.i18n.I18n;
import com.perso.T4C.spell.SpellData;

/** Multi-line hover text describing a spell: what it does, its element, requirements and cost. */
public final class SpellTooltipText {
  private SpellTooltipText() {}

  public static String build(SpellData spell) {
    if (spell == null) {
      return "";
    }
    StringBuilder text = new StringBuilder();
    text.append(I18n.resolve(spell.getName()));
    String description = I18n.resolve(spell.getDescription());
    if (description != null
        && !description.isBlank()
        && !description.equals(spell.getDescription())) {
      text.append('\n').append(description);
    }
    appendLine(text, "element", elementName(spell.getElement()));
    appendLine(
        text,
        "cast_type",
        spell.getAttackType() == SpellData.ATTACK_MENTAL
            ? I18n.key("tooltip.cast_type.mental")
            : I18n.key("tooltip.cast_type.physical"));
    appendLine(text, "req_level", spell.getMinLevel() > 0 ? String.valueOf(spell.getMinLevel()) : null);
    appendLine(text, "req_int", spell.getMinInt() > 0 ? String.valueOf(spell.getMinInt()) : null);
    appendLine(text, "req_wis", spell.getMinWis() > 0 ? String.valueOf(spell.getMinWis()) : null);
    appendLine(text, "mana_cost", nonZero(spell.getManaCost()));
    return text.toString();
  }

  private static String elementName(int element) {
    return switch (element) {
      case 1 -> I18n.key("element.fire");
      case 2 -> I18n.key("element.earth");
      case 3 -> I18n.key("element.air");
      case 4 -> I18n.key("element.water");
      case 5 -> I18n.key("element.light");
      case 6 -> I18n.key("element.dark");
      default -> null;
    };
  }

  private static String nonZero(String diceFormula) {
    return diceFormula == null || diceFormula.isBlank() || "0".equals(diceFormula.trim())
        ? null
        : diceFormula.trim();
  }

  private static void appendLine(StringBuilder text, String slug, String value) {
    if (value == null || value.isEmpty()) {
      return;
    }
    text.append('\n').append(I18n.key("tooltip." + slug)).append(": ").append(value);
  }
}
