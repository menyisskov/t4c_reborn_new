package com.perso.T4C.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuickSlotEntry {
  private int slot;
  private String spell;
  private String item;

  public QuickSlotEntry(int slot, String spell) {
    this.slot = slot;
    this.spell = spell;
  }
}
