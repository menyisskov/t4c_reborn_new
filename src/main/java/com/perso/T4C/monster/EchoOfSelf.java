package com.perso.T4C.monster;

import static com.perso.T4C.config.GameConstants.GRID_W;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.combat.CombatProfile;
import com.perso.T4C.combat.CombatProfiles;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.helper.PlayerAppearanceDefaults;
import com.perso.T4C.helper.XpCurve;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.mirror.MirrorTrials;
import com.perso.T4C.monster.core.BaseMonster;
import com.perso.T4C.monster.core.DamageCallback;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.player.Player;
import com.perso.T4C.player.PlayerAnimations;
import com.perso.T4C.spell.SpellData;
import com.perso.T4C.ui.SystemMessage;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;

/**
 * T4C-0042: the Echo a player faces in the Mirror of Echoes. It has no fixed stats: the first
 * time it updates, it copies its maker (the player it was summoned for) - their paperdoll, name,
 * level, attack/archery skill, agility and dodge, and best attack spell - and scales its blows by
 * the trial being fought (see {@link MirrorTrials}). Its hits are measured against the maker's own
 * life rather than a dice table, and its health is set by the strongest of the maker's opening
 * blows, so the fight is equally hard for a level-20 archer and a level-400 archmage.
 *
 * <p>No {@code @Spawn}: the Mirrorwarden summons it on demand and it never respawns. It fades
 * (without paying anything) if its maker falls, walks away or takes too long.
 */
@Slf4j
public final class EchoOfSelf extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  /** Ghostly, slightly cold tint over the copied paperdoll. */
  private static final Color ECHO_TINT = new Color(0.55f, 0.74f, 1f, 0.68f);

  private static final long LIVE_WINDOW_MILLIS = 2000L;

  /** Casters and archers keep this distance; everyone else closes to melee. */
  private static final int RANGED_TILES = 6;

  private static volatile EchoOfSelf current;

  private Player maker;
  private PlayerAnimations mirrorAnimations;
  private boolean bowAttacks;
  private SpellData signatureSpell;
  private int tier = 1;
  private int[] hitRange = {1, 1};
  private int calibrationHits;
  private int strongestOpeningBlow;
  private int damageTaken;
  private boolean inPlayerDamage;
  private boolean spokeAtTwoThirds;
  private boolean spokeAtOneThird;
  private boolean spokeAtMakerBleeding;
  private long summonedAtMs;
  private long lastUpdateMs;
  private MirrorTrials.Archetype archetype = MirrorTrials.Archetype.WARRIOR;

  public EchoOfSelf(MonsterDef d, float x, float y) throws GameException {
    super(d, x, y);
    disableRespawn();
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        MirrorTrials.ECHO_MONSTER_NAME,
        "${monster.echo_of_self}",
        1000,
        0,
        0,
        0,
        1,
        1,
        30000L,
        "",
        null,
        null,
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        0,
        0,
        List.of(),
        false,
        0.0f,
        10,
        10,
        10,
        10,
        0,
        10,
        0,
        new int[] {100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100},
        1,
        1,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        0,
        0,
        true,
        List.of(new MonsterDef.Attack("1d1", 1, 100, 0, 0, 1)),
        false,
        0,
        List.of(),
        java.util.Map.of());
  }

  /** True while an Echo is alive in the world (it stops updating once its map unloads). */
  public static boolean isActive() {
    EchoOfSelf echo = current;
    return echo != null
        && !echo.isDead()
        && System.currentTimeMillis() - echo.lastUpdateMs <= LIVE_WINDOW_MILLIS;
  }

  /** Called when the player dies: a living Echo claims the win and fades. */
  public static void onMakerFell() {
    EchoOfSelf echo = current;
    if (echo != null && !echo.isDead() && echo.maker != null) {
      echo.say("mirror.echo.won");
      MirrorTrials.recordFall(echo.maker);
      echo.fade();
    }
  }

  public int getTier() {
    return tier;
  }

  public Player getMaker() {
    return maker;
  }

  @Override
  public String getName() {
    return maker == null
        ? super.getName()
        : I18n.message("mirror.echo.name", String.valueOf(maker.getName()));
  }

  @Override
  public String getDisplayName() {
    return getName();
  }

  @Override
  public void update(float delta, Vector2 playerPosition, List<BaseMonster> nearbyMonsters) {
    lastUpdateMs = System.currentTimeMillis();
    if (maker == null && getScriptPlayer() != null && !isDead) {
      mirror(getScriptPlayer());
    }
    if (maker != null && !isDead && current != this) {
      // A newer Echo was summoned while this one sat frozen off-screen; only one may exist.
      fade();
    }
    if (maker != null && !isDead) {
      checkFade();
      if (!isDead && !isAggro) aggroOn(maker.getPositionVector());
    }
    super.update(delta, playerPosition, nearbyMonsters);
    if (mirrorAnimations != null) {
      if (isDead) {
        mirrorAnimations.dispose();
        mirrorAnimations = null;
      } else {
        mirrorAnimations.update(delta, movement.isMoving());
      }
    }
  }

  private void mirror(Player player) {
    EchoOfSelf previous = current;
    if (previous != null && previous != this) previous.fade();
    maker = player;
    current = this;
    summonedAtMs = System.currentTimeMillis();
    int challenge = player.getQuestFlag(MirrorTrials.FLAG_CHALLENGE);
    tier =
        challenge > 0
            ? Math.min(MirrorTrials.MAX_TIER, challenge)
            : MirrorTrials.nextTier(player);
    archetype = MirrorTrials.archetypeOf(player);
    bowAttacks = PlayerAppearanceDefaults.hasBowEquipped(player);
    boolean caster =
        archetype == MirrorTrials.Archetype.INTELLIGENCE_MAGE
            || archetype == MirrorTrials.Archetype.WISDOM_MAGE
            || archetype == MirrorTrials.Archetype.HYBRID_MAGE;
    signatureSpell = caster ? MirrorTrials.signatureSpell(player) : null;

    CombatProfile profile = CombatProfiles.fromPlayer(player);
    combatLevel = Math.max(1, profile.level());
    combatStrength = Math.max(1, profile.strength());
    combatEndurance = Math.max(1, profile.endurance());
    combatAgility = Math.max(1, profile.agility());
    combatIntelligence = Math.max(1, player.getEffectiveIntelligence());
    combatAttack = Math.max(1, bowAttacks ? profile.archery() : profile.attack());
    combatDodge = Math.max(1, profile.dodge());
    // No armor of its own: its health is already sized to the maker's real damage per blow.
    combatArmorMin = 0;
    combatArmorMax = 0;
    boolean ranged = bowAttacks || signatureSpell != null;
    attacks =
        List.of(new MonsterDef.Attack("1d1", combatAttack, 100, 0, 0, ranged ? RANGED_TILES : 1));
    hitRange = MirrorTrials.hitRange(player.getMaxHp(), tier);
    maxHealth = (int) Math.min(Integer.MAX_VALUE, Math.max(1000L, player.getMaxHp() * 4L));
    health = maxHealth;
    xpPerHit = 0;
    xpOnDeath = 0;
    try {
      mirrorAnimations = new PlayerAnimations(MirrorTrials.appearanceOf(player));
      mirrorAnimations.setBowEquipped(bowAttacks);
    } catch (GameException ex) {
      log.warn("Echo could not copy {}'s appearance", player.getName(), ex);
    }
    aggroOn(player.getPositionVector());
    say(MirrorTrials.introKey(tier));
  }

  private void checkFade() {
    if (maker.getCurrentHp() <= 0) {
      onMakerFell();
      return;
    }
    float distance = position.dst(maker.getPositionVector());
    if (distance > MirrorTrials.LEASH_TILES * GRID_W) {
      say("mirror.echo.fled");
      fade();
      return;
    }
    if (System.currentTimeMillis() - summonedAtMs > MirrorTrials.MAX_FIGHT_MILLIS) {
      say("mirror.echo.bored");
      fade();
    }
  }

  /** Leaves the world without dying: no death callback, so no reward and no loot. */
  private void fade() {
    if (isDead) return;
    isDead = true;
    deathTime = System.currentTimeMillis();
    isAggro = false;
    aggroTarget = null;
    movement.stop();
    animations.startDeath();
    if (current == this) current = null;
  }

  @Override
  protected void die() {
    if (current == this) current = null;
    say(tier >= MirrorTrials.MAX_TIER ? "mirror.echo.defeated.final" : "mirror.echo.defeated");
    super.die();
  }

  @Override
  protected void performAttack(Vector2 playerPosition) {
    DamageCallback callback = getDamageCallback();
    if (maker == null || callback == null || isFightingCompanion()) {
      super.performAttack(playerPosition);
      return;
    }
    ThreadLocalRandom random = ThreadLocalRandom.current();
    int wanted = hitRange[0] + random.nextInt(Math.max(1, hitRange[1] - hitRange[0] + 1));
    if (signatureSpell != null) {
      lastAttackRanged = true;
      lastAttackSpellId = signatureSpell.getSpellId();
      clearAttackAnimationPose();
      callback.applySpell(
          this,
          signatureSpell.getSpellId(),
          MirrorTrials.spellRawDamage(wanted, signatureSpell.getElement()));
    } else {
      lastAttackRanged = bowAttacks;
      lastAttackSpellId = 0;
      startAttackAnimation();
      // Its blows are measured against the maker's life, so their own armor can't erase them.
      int armor = (int) Math.min(Integer.MAX_VALUE - (long) wanted, (long) makerArmorClass());
      callback.applyDamage(this, wanted + armor);
    }
    if (!spokeAtMakerBleeding && maker.getCurrentHp() * 4 < maker.getMaxHp()) {
      spokeAtMakerBleeding = true;
      say("mirror.echo.bleeding");
    }
  }

  private double makerArmorClass() {
    return CombatProfiles.fromPlayer(maker).armorClass();
  }

  @Override
  public void applyPlayerDamage(int damage, Player player, XpCurve xpCurve) {
    if (isDead) return;
    if (maker != null && damage > 0 && calibrationHits < MirrorTrials.CALIBRATION_HITS) {
      calibrate(damage);
    }
    inPlayerDamage = true;
    try {
      super.applyPlayerDamage(damage, player, xpCurve);
    } finally {
      inPlayerDamage = false;
    }
    if (!isDead && maker != null) speakAtThresholds();
  }

  /** The Echo sizes its health to the strongest of its maker's first few blows. */
  private void calibrate(int blow) {
    calibrationHits++;
    strongestOpeningBlow = Math.max(strongestOpeningBlow, blow);
    long wantedMax = (long) strongestOpeningBlow * MirrorTrials.blowsToWin(tier);
    int newMax = (int) Math.min(Integer.MAX_VALUE, Math.max(1L, wantedMax));
    if (calibrationHits == 1 || newMax > maxHealth) {
      maxHealth = Math.max(newMax, damageTaken + 1);
      health = Math.max(1, maxHealth - damageTaken);
    }
  }

  @Override
  public void takeDamage(int damage) {
    if (isDead) return;
    int applied = damage;
    if (!inPlayerDamage && applied >= health) {
      // Instant-kill effects can't unmake your own reflection; they only wound it.
      applied = Math.max(1, Math.min(health - 1, maxHealth / 10));
    }
    damageTaken += Math.max(0, Math.min(applied, health));
    super.takeDamage(applied);
  }

  private void speakAtThresholds() {
    if (!spokeAtTwoThirds && health * 3 <= maxHealth * 2) {
      spokeAtTwoThirds = true;
      say(MirrorTrials.tauntKey(archetype));
    } else if (!spokeAtOneThird && health * 3 <= maxHealth) {
      spokeAtOneThird = true;
      say("mirror.echo.harder");
    }
  }

  private void say(String key) {
    if (maker == null) return;
    showNameFor(5000L);
    SystemMessage.showShared(
        I18n.message("mirror.echo.says", getName(), MirrorTrials.line(key, maker, tier)));
  }

  @Override
  public void render(SpriteBatch batch, ShaderProgram outlineShader) {
    if (isDead || mirrorAnimations == null) return;
    Color previous = batch.getColor().cpy();
    batch.setColor(ECHO_TINT);
    float healthPercent = maxHealth <= 0 ? 0f : (float) health / (float) maxHealth;
    mirrorAnimations.render(
        batch,
        position,
        movement.getCurrentAngle(),
        movement.isFlipX(),
        movement.isMoving(),
        outlineShader,
        isHovered || selected,
        healthPercent);
    batch.setColor(previous);
  }

  @Override
  public boolean usesHumanoidAnimations() {
    return mirrorAnimations != null;
  }

  @Override
  protected void startAttackAnimation() {
    super.startAttackAnimation();
    if (mirrorAnimations != null) mirrorAnimations.startAttack(movement.getCurrentAngle(), bowAttacks);
  }

  @Override
  protected void clearAttackAnimationPose() {
    super.clearAttackAnimationPose();
    if (mirrorAnimations != null) mirrorAnimations.clearAttackPose();
  }
}
