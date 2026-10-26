package com.perso.T4C.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MacroBinding {
  public static final int UNBOUND = -1;
  public static final int MOD_CTRL = 1;
  public static final int MOD_SHIFT = 2;
  public static final int MOD_ALT = 4;

  private String spellName;
  private int keycode = UNBOUND;
  private int modifiers = 0;
  private String itemKey;

  /** Older spell macros keep their original three-field construction and save shape. */
  public MacroBinding(String spellName, int keycode, int modifiers) {
    this(spellName, keycode, modifiers, null);
  }

  public static MacroBinding forItem(String itemKey) {
    return new MacroBinding(
        null, UNBOUND, 0, com.perso.T4C.item.ItemDefinition.normalizeKey(itemKey));
  }

  public boolean isItem() {
    return itemKey != null && !itemKey.isBlank();
  }

  public boolean matches(int keycode, int modifiers) {
    return this.keycode == keycode && this.modifiers == modifiers;
  }
}
