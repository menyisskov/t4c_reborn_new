package com.perso.T4C.item;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;

import com.perso.T4C.i18n.I18n;
import com.perso.T4C.player.Player;
import com.perso.T4C.spell.SpellCastingService;
import com.perso.T4C.spell.SpellData;
import com.perso.T4C.spell.SpellEffectManager;
import com.perso.T4C.spell.SpellRegistry;
import java.util.random.RandomGenerator;

public final class ItemUseService {
  public enum Failure {
    NONE,
    NOT_OWNED,
    NO_EFFECT,
    NO_HEALING_NEEDED,
    NO_MANA_NEEDED,
    SPELL_FAILED,
    CHANCE_FAILED
  }

  public record Result(
      boolean success, Failure failure, String spellName, SpellCastingService.Failure castFailure) {
    private static Result failure(Failure failure) {
      return new Result(false, failure, null, SpellCastingService.Failure.NONE);
    }

    private static Result success(String spell) {
      return new Result(true, Failure.NONE, spell, SpellCastingService.Failure.NONE);
    }
  }

  private ItemUseService() {}

  public static String failureMessage(Result result, String itemKey) {
    return switch (result.failure()) {
      case NONE -> "";
      case NO_HEALING_NEEDED -> I18n.message("message.no_healing_needed");
      case NO_MANA_NEEDED -> I18n.message("message.no_mana_needed");
      case NOT_OWNED -> I18n.message("message.item_not_owned");
      case SPELL_FAILED ->
          result.castFailure() != SpellCastingService.Failure.NONE
              ? SpellCastingService.message(result.castFailure())
              : cannotUseMessage(itemKey);
      default -> cannotUseMessage(itemKey);
    };
  }

  private static String cannotUseMessage(String itemKey) {
    var definition = ItemRegistry.findByKey(itemKey);
    return I18n.message(
        "message.item_cannot_use",
        I18n.resolve(definition == null ? itemKey : definition.getName()));
  }

  public static Result useOnSelf(Player player, String itemKey, RandomGenerator random) {
    ItemDefinition definition = ItemRegistry.findByKey(itemKey);
    if (player == null || definition == null || InventoryService.count(player, itemKey) <= 0) {
      return Result.failure(Failure.NOT_OWNED);
    }
    if (definition.getSpells().isEmpty()) return Result.failure(Failure.NO_EFFECT);
    ItemDefinition.ItemSpell selected = null;
    for (ItemDefinition.ItemSpell candidate : definition.getSpells()) {
      int chance = candidate.getChance() <= 0 ? 100 : Math.min(100, candidate.getChance());
      if (random.nextInt(100) < chance) {
        selected = candidate;
        break;
      }
    }
    if (selected == null) return Result.failure(Failure.CHANCE_FAILED);
    SpellData spell = SpellRegistry.findById(selected.getSpellId());
    if (spell == null) return Result.failure(Failure.NO_EFFECT);
    SpellEffectManager manager = new SpellEffectManager();
    int healthDelta = manager.resolvePlayerHealthDelta(spell, player);
    int manaDelta = manager.resolvePlayerManaDelta(spell, player);
    if (healthDelta > 0 && player.getCurrentHp() >= player.getMaxHp())
      return Result.failure(Failure.NO_HEALING_NEEDED);
    if (manaDelta > 0 && player.getMana() >= player.getMaxMana())
      return Result.failure(Failure.NO_MANA_NEEDED);
    SpellCastingService.Result cast =
        SpellCastingService.beginItemUse(
            new SpellCastingService.Request(
                spell, player, SpellCastingService.TargetKind.SELF, 0f, true, false, false));
    if (!cast.success())
      return new Result(false, Failure.SPELL_FAILED, spell.getName(), cast.failure());
    if (healthDelta > 0) {
      int healthBefore = player.getCurrentHp();
      player.applyHeal(healthDelta, healthDelta);
      player.notifyHealing(player.getCurrentHp() - healthBefore);
    } else if (healthDelta < 0) player.takeDamage(-healthDelta);
    if (manaDelta != 0) {
      int manaBefore = player.getMana();
      player.setMana(Math.max(0, Math.min(player.getMaxMana(), manaBefore + manaDelta)));
      player.notifyManaRestored(player.getMana() - manaBefore);
    }
    var originalBuffs = manager.resolvePlayerBuffEffects(spell, player);
    if (!originalBuffs.isEmpty()) {
      player.applyBuff(
          spell.getName(),
          spell.getDescription(),
          spell.getIconId(),
          manager.resolveDurationSeconds(spell, player),
          false,
          originalBuffs);
    } else {
      applyBuff(player, spell);
    }
    SpellEffectManager.PlayerUtility utility = manager.applyPlayerUtilityEffects(spell, player);
    if (utility.teleportTileX() != null
        && utility.teleportTileY() != null
        && utility.teleportWorldZ() != null) {
      player.setWorldPosition(
          utility.teleportTileX() * GRID_W,
          utility.teleportTileY() * GRID_H,
          utility.teleportWorldZ());
    }
    InventoryService.useCharge(player, itemKey);
    return Result.success(spell.getName());
  }

  private static void applyBuff(Player player, SpellData spell) {
    SpellData.SpellBuff buff = spell.getBuff();
    if (buff == null) return;
    player.applyBuff(
        spell.getName(),
        spell.getDescription(),
        spell.getIconId(),
        buff.getDurationSeconds(),
        Boolean.TRUE.equals(buff.getUnlimited()),
        buff.getEffects());
  }
}
