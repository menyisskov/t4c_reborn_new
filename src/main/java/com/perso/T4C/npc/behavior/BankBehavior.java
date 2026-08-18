package com.perso.T4C.npc.behavior;

import java.util.Locale;

public class BankBehavior implements NpcBehavior {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  private final String bankFlag;

  public BankBehavior(String bankFlag) {

    this.bankFlag = bankFlag;
  }

  @Override
  public boolean onKeyword(NpcBehaviorContext c, String text) {

    String k = text == null ? "" : text.trim().toUpperCase(Locale.ROOT);

    if (k.equals("BALANCE")) {

      c.sayKey("npc.bank.balance", c.flag(bankFlag));

      return true;
    }

    if (k.equals("DEPOSIT") || k.equals("WITHDRAW")) {

      c.sayKey(k.equals("DEPOSIT") ? "npc.bank.deposit.help" : "npc.bank.withdraw.help");

      return true;
    }

    if (k.matches("DEPOSIT\\s+[-+]?\\d+\\s+GOLD")) return transfer(c, k, true);

    if (k.matches("WITHDRAW\\s+[-+]?\\d+\\s+GOLD")) return transfer(c, k, false);

    return false;
  }

  private boolean transfer(NpcBehaviorContext c, String command, boolean deposit) {

    String[] parts = command.split("\\s+");

    int amount;

    try {

      amount = Integer.parseInt(parts[1]);

    } catch (RuntimeException e) {

      amount = 0;
    }

    if (amount < 1) {

      c.sayKey("npc.bank.invalid");

      return true;
    }

    if (deposit) {

      if (c.player().getGold() < amount) c.sayKey("npc.bank.deposit.poor");
      else {

        c.player().addGold(-amount);

        c.flag(bankFlag, c.flag(bankFlag) + amount);

        c.sayKey("npc.bank.deposit.done");
      }

    } else if (c.flag(bankFlag) < amount) c.sayKey("npc.bank.withdraw.poor");
    else {

      c.player().addGold(amount);

      c.flag(bankFlag, c.flag(bankFlag) - amount);

      c.sayKey("npc.bank.withdraw.done");
    }

    return true;
  }
}
