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
}
