package com.perso.T4C.gui.widget;

import com.perso.T4C.i18n.I18n;
import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.ItemDurabilityService;

/** Multi-line hover text describing an item: name, slot, price, durability, stats, requirements. */
public final class ItemTooltipText {
  private ItemTooltipText() {}

  public static String build(String itemName, double durability) {
    ItemDefinition def = ItemDefinition.get(itemName);
    if (def == null) {
      return itemName;
    }
    StringBuilder text = new StringBuilder();
    text.append(
        I18n.resolve(def.getName() != null && !def.getName().isEmpty() ? def.getName() : itemName));
    appendLine(text, "type", def.getBodyPart() == null ? null : def.getBodyPart().name());
    appendLine(text, "price", def.getPrice() > 0 ? def.getPrice() + " gold" : null);
    appendLine(text, "weight", def.getWeight() > 0 ? String.valueOf(def.getWeight()) : null);
    if (ItemDurabilityService.isRepairable(def)) {
      String formatted = ItemDurabilityService.format(durability);
      text.append('\n').append(I18n.message("tooltip.durability", formatted, formatted));
      if (durability <= 0d) text.append(" - ").append(I18n.key("tooltip.broken"));
    }
    appendLine(
        text, "armor_class", def.getArmorClass() != 0d ? formatDouble(def.getArmorClass()) : null);
    appendLine(
        text, "dodge_penalty", def.getDodgeLost() != 0 ? String.valueOf(def.getDodgeLost()) : null);
    appendLine(text, "damage", nonBlank(def.getDmgFormula()));
    appendLine(
        text,
        "attack_speed",
        def.getAttackSpeed() > 0d ? formatDouble(def.getAttackSpeed()) : null);
    appendLine(text, "attack_delay", nonBlank(def.getAtkDelay()));
    appendLine(text, "req_end", def.getMinEnd() > 0 ? String.valueOf(def.getMinEnd()) : null);
    appendLine(text, "req_att", def.getReqAttack() > 0 ? String.valueOf(def.getReqAttack()) : null);
    appendLine(text, "req_str", def.getReqStr() > 0 ? String.valueOf(def.getReqStr()) : null);
    appendLine(text, "req_agi", def.getReqAgi() > 0 ? String.valueOf(def.getReqAgi()) : null);
    appendLine(text, "req_int", def.getMinInt() > 0 ? String.valueOf(def.getMinInt()) : null);
    appendLine(text, "req_wis", def.getMinWis() > 0 ? String.valueOf(def.getMinWis()) : null);
    appendLine(text, "charges", def.getNbCharges() > 0 ? String.valueOf(def.getNbCharges()) : null);
    appendLine(text, "radiance", def.getRadiance() > 0 ? String.valueOf(def.getRadiance()) : null);
    if (def.isBow()) appendFlag(text, "bow");
    if (def.isUnique()) appendFlag(text, "unique");
    if (def.isUnlimitedUse()) appendFlag(text, "unlimited_use");
    if (def.isCanSummon()) appendFlag(text, "can_summon");
    return text.toString();
  }

  private static void appendLine(StringBuilder text, String slug, String value) {
    if (value == null || value.isEmpty()) {
      return;
    }
    text.append('\n').append(I18n.key("tooltip." + slug)).append(": ").append(value);
  }

  private static void appendFlag(StringBuilder text, String slug) {
    text.append('\n').append(I18n.key("tooltip." + slug));
  }

  private static String nonBlank(String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }

  private static String formatDouble(double value) {
    if (value == Math.rint(value)) {
      return String.valueOf((long) value);
    }
    return String.format(java.util.Locale.ROOT, "%.2f", value);
  }
}
