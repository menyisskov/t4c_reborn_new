package com.perso.T4C.objects;

import com.perso.T4C.helper.DiceFormula;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.player.Player;
import com.perso.T4C.spell.SpellData;
import com.perso.T4C.spell.SpellEffectManager;
import com.perso.T4C.spell.SpellRegistry;
import java.util.List;

/**
 * The blessing chest that stands outside each town's temple (T4C-0060). Clicking it lays the town's
 * whole set of wards on the clicker at once, for free, cast by an unseen priest rather than by the
 * player - so a character too low to have learned any of these spells still benefits, and nobody
 * spends mana.
 *
 * <p>How strong the wards are is the only thing that differs between towns, and it comes entirely
 * from the stats the priest casts with. Every town but Avalon Sanctuary is served by a competent
 * journeyman mage ({@link #TOWN_CASTER_INTELLIGENCE}/{@link #TOWN_CASTER_WISDOM}); Avalon's priest
 * is an archmage ({@link #AVALON_CASTER_INTELLIGENCE}/{@link #AVALON_CASTER_WISDOM}), which is what
 * makes the walk out there worth making.
 */
public final class TempleBlessingService {
  /** Intelligence the priest casts with in every town but Avalon Sanctuary. */
  public static final int TOWN_CASTER_INTELLIGENCE = 200;

  /** Wisdom the priest casts with in every town but Avalon Sanctuary. */
  public static final int TOWN_CASTER_WISDOM = 200;

  /** Intelligence Avalon Sanctuary's archmage casts with. */
  public static final int AVALON_CASTER_INTELLIGENCE = 500;

  /** Wisdom Avalon Sanctuary's archmage casts with. */
  public static final int AVALON_CASTER_WISDOM = 1500;

  /** The object name every blessing chest shares. */
  public static final String OBJECT_NAME = "TEMPLE BLESSING CHEST";

  /**
   * The wards laid on the clicker, in the order they land. Held as keys rather than {@link
   * SpellData} so the list stays readable, and so a renamed spell still resolves - {@link
   * SpellRegistry#findByName} carries its own alias table for old keys.
   */
  public static final List<String> BLESSING_SPELLS =
      List.of(
          "spell.bless",
          "spell.barrier",
          "spell.protection",
          "spell.mana_shield",
          "spell.mana_surge",
          "spell.earthen_strength",
          "spell.stone_skin",
          "spell.tranquility",
          "spell.clear_thought");

  /**
   * A blessing chest: which tile it stands on, and the intelligence and wisdom its town's priest
   * casts with. The stats are held per chest rather than derived from a town rank, because that is
   * the whole of what varies and spelling it out keeps the strength of each chest readable here.
   */
  public record Shrine(
      long tileX, long tileY, long worldZ, int casterIntelligence, int casterWisdom) {}

  private static Shrine townShrine(long tileX, long tileY, long worldZ) {
    return new Shrine(tileX, tileY, worldZ, TOWN_CASTER_INTELLIGENCE, TOWN_CASTER_WISDOM);
  }

  /**
   * One chest outside each town's temple. The five ordinary towns all give the same blessing; only
   * Avalon Sanctuary's is stronger.
   *
   * <p>Windhowl, Stonecrest and the Oracle have no temple standing in the world yet, so their
   * chests sit at the town's own gathering point instead - the gate sentry, the trade row, and the
   * spot travellers arrive at.
   */
  public static final List<Shrine> SHRINES =
      List.of(
          // Lighthaven - outside the temple Kilhiam and Brother Kiran keep (2955,1048).
          townShrine(2954, 1050, 0),
          // Silversky - outside the temple Aquinos serves in (1557,2405).
          townShrine(1556, 2407, 0),
          // Windhowl - beside the sentry post at the town gate (1816,1297).
          townShrine(1815, 1299, 0),
          // Stonecrest - on the trade row, where the town's own people stand (210,735).
          townShrine(209, 737, 0),
          // The Oracle - where travellers arrive in the cavern (2968,2141).
          townShrine(2970, 2143, 2),
          // Avalon Sanctuary - outside the Temple door Sister Ilyndra keeps (1340,1479). The one
          // archmage, and the only chest whose blessing is worth the journey.
          new Shrine(1339, 1481, 0, AVALON_CASTER_INTELLIGENCE, AVALON_CASTER_WISDOM));

  public enum Failure {
    NONE,
    NOT_A_BLESSING_CHEST
  }

  public record Result(boolean blessed, Failure failure, int spellCount, int casterWisdom) {
    private static Result failure(Failure failure) {
      return new Result(false, failure, 0, 0);
    }
  }

  /** The chest standing on this exact tile, or {@code null} if this object is not one. */
  public static Shrine shrineAt(ObjectPos object) {
    if (object == null || !OBJECT_NAME.equalsIgnoreCase(object.name())) return null;
    for (Shrine shrine : SHRINES) {
      if (shrine.tileX() == object.x()
          && shrine.tileY() == object.y()
          && shrine.worldZ() == object.z()) {
        return shrine;
      }
    }
    return null;
  }

  /**
   * Lays every ward in {@link #BLESSING_SPELLS} on {@code player}. Clicking again simply refreshes
   * them: there is no cooldown, because a second click buys nothing that a first click and a short
   * wait would not.
   */
  public Result bless(Player player, ObjectPos object) {
    Shrine shrine = shrineAt(object);
    if (shrine == null || player == null) return Result.failure(Failure.NOT_A_BLESSING_CHEST);
    SpellEffectManager manager = new SpellEffectManager();
    DiceFormula.Context casterContext =
        SpellEffectManager.externalCasterContext(
            player, shrine.casterIntelligence(), shrine.casterWisdom());
    int applied = 0;
    for (String key : BLESSING_SPELLS) {
      SpellData spell = SpellRegistry.findByName(key);
      if (spell == null) continue;
      List<SpellData.SpellEffect> effects = manager.resolvePlayerBuffEffects(spell, casterContext);
      if (effects.isEmpty()) continue;
      // The caster's stats ride along with the buff so that saving and loading rebuilds it at the
      // strength the priest granted, rather than rescaling it to the blessed character's own stats.
      player.applyBuff(
          spell.getName(),
          spell.getDescription(),
          spell.getIconId(),
          manager.resolveDurationSeconds(spell, casterContext),
          false,
          effects,
          shrine.casterIntelligence(),
          shrine.casterWisdom());
      applied++;
    }
    if (applied == 0) return Result.failure(Failure.NOT_A_BLESSING_CHEST);
    return new Result(true, Failure.NONE, applied, shrine.casterWisdom());
  }

  public String message(Result result) {
    return I18n.message("message.temple_blessing", result.spellCount());
  }
}
