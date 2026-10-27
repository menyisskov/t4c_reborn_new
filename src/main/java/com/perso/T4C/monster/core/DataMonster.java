package com.perso.T4C.monster.core;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Rectangle;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.helper.AppearanceDefaultsCatalog;
import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.loot.MonsterLootTables;
import com.perso.T4C.npc.core.NPCAnimations;
import com.perso.T4C.player.BodyPart;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DataMonster extends BaseMonster {
  private final java.util.Map<String, String> sourceEvents;
  private final boolean animateWhileStationary;
  private final float stationaryAnimationPauseSeconds;
  private final String translationIdentity;
  private final String untranslatedDisplayName;
  private final NPCAnimations humanoidAnimations;
  private final boolean humanoidBow;
  private final MonsterDef definition;
  private final Rectangle mouseOverBounds = new Rectangle();

  public DataMonster(MonsterDef def, float initialX, float initialY) throws GameException {
    super(
        def.getName(),
        initialX,
        initialY,
        def.getHealth(),
        def.getMana(),
        def.getXpPerHit(),
        def.getXpOnDeath(),
        def.getHitDamageMin(),
        def.getHitDamageMax(),
        def.getRespawnTime(),
        def.getWalkPattern(),
        def.getAttackPattern(),
        def.getDeathPattern(),
        def.getSoundAttack(),
        def.getSoundDeath(),
        def.getSoundHit());
    definition = def;
    translationIdentity = def.getName();
    untranslatedDisplayName =
        def.getDisplayName() == null || def.getDisplayName().isEmpty()
            ? def.getName()
            : def.getDisplayName();
    setDisplayName(untranslatedDisplayName);
    humanoidAnimations = hasMonsterAnimation(def) ? null : buildHumanoidAnimations(def);
    ItemDefinition weapon =
        humanoidAnimations == null ? null : MonsterPuppetDress.find(def.getItemWeapon());
    humanoidBow = weapon != null && weapon.isBow();
    if (humanoidAnimations != null)
      humanoidAnimations.getPlayerAnimations().setBowEquipped(humanoidBow);
    applyCombatDefinition(def);
    this.lootTable = MonsterLootTables.of(def);
    if (!def.isDefaultAggressive()) {
      setAggressive(false);
    }
    if (def.getAttacks() != null && !def.getAttacks().isEmpty()) {
      this.attacks = def.getAttacks();
    }
    this.animateWhileStationary = def.isAnimateWhileStationary();
    this.stationaryAnimationPauseSeconds = def.getStationaryAnimationPauseSeconds();
    this.sourceEvents = def.getSourceEvents() == null ? java.util.Map.of() : def.getSourceEvents();
  }

  public MonsterDef getDefinition() {
    return definition;
  }

  public java.util.Map<String, String> getSourceEvents() {
    return sourceEvents;
  }

  @Override
  public String getName() {
    if (I18n.keyOf(untranslatedDisplayName) != null) {
      return I18n.resolve(untranslatedDisplayName);
    }
    return I18n.key("monster." + I18n.normalizedKey(translationIdentity), untranslatedDisplayName);
  }

  @Override
  public String getDisplayName() {
    return getName();
  }

  @Override
  public String getCanonicalName() {
    return translationIdentity;
  }

  @Override
  public void update(
      float delta, com.badlogic.gdx.math.Vector2 playerPosition, List<BaseMonster> nearbyMonsters) {
    super.update(delta, playerPosition, nearbyMonsters);
    if (humanoidAnimations != null) humanoidAnimations.update(delta, movement.isMoving());
  }

  @Override
  public void render(SpriteBatch batch, ShaderProgram outlineShader) {
    if (humanoidAnimations == null) {
      super.render(batch, outlineShader);
      return;
    }
    if (isDead) return;
    humanoidAnimations.render(
        batch,
        position,
        movement.getCurrentAngle(),
        movement.isFlipX(),
        movement.isMoving(),
        isHovered || selected,
        outlineShader,
        null,
        false,
        maxHealth > 0 ? (float) health / maxHealth : 0f);
  }

  public boolean usesHumanoidAnimations() {
    return humanoidAnimations != null;
  }

  public Rectangle getRenderBounds(Rectangle out) {
    if (humanoidAnimations == null) return out.set(0f, 0f, 0f, 0f);
    return humanoidAnimations.getRenderBounds(
        position, movement.getCurrentAngle(), movement.isFlipX(), movement.isMoving(), out);
  }

  @Override
  public boolean isMouseOver(float mouseX, float mouseY) {
    if (humanoidAnimations != null) {
      Rectangle bounds = getRenderBounds(mouseOverBounds);
      if (bounds.width > 0f && bounds.height > 0f) return bounds.contains(mouseX, mouseY);
    }
    return super.isMouseOver(mouseX, mouseY);
  }

  @Override
  protected void startAttackAnimation() {
    super.startAttackAnimation();
    if (humanoidAnimations != null) {
      humanoidAnimations.startAttack(movement.getCurrentAngle(), humanoidBow);
    }
  }

  @Override
  protected void clearAttackAnimationPose() {
    super.clearAttackAnimationPose();
    if (humanoidAnimations != null) {
      humanoidAnimations.clearAttackPose();
    }
  }

  @Override
  public void respawn() {
    super.respawn();
    if (humanoidAnimations != null) {
      humanoidAnimations.clearAttackPose();
    }
  }

  private static final int APPEAR_PUPPET = 10011;
  private static final int APPEAR_FEMALE_PUPPET = 10012;

  private static boolean hasMonsterAnimation(MonsterDef def) {
    return def.getWalkPattern() != null && !def.getWalkPattern().isBlank();
  }

  private static boolean isPuppetAppearance(MonsterDef def) {
    int appearance = def.getAppearance();
    return appearance >= 10001 && appearance <= 10012;
  }

  private static NPCAnimations buildHumanoidAnimations(MonsterDef def) throws GameException {
    int[] ids = {
      def.getItemBody(),
      def.getItemFeet(),
      def.getItemHands(),
      def.getItemHead(),
      def.getItemLegs(),
      def.getItemWeapon(),
      def.getItemShield(),
      def.getItemBack()
    };
    List<Object> parts = new ArrayList<>();
    for (int id : ids) {
      ItemDefinition item = MonsterPuppetDress.find(id);
      if (item == null) continue;
      addHumanoidPart(parts, def, item.getBodyPart(), item.getAppearanceEquippedPrimary());
      addHumanoidPart(parts, def, item.getSecondaryBodyPart(), item.getAppearanceEquippedSecondary());
    }
    if (def.getAppearance() == APPEAR_FEMALE_PUPPET) {
      MonsterPuppetDress.addPart(parts, BodyPart.HAIR, "WoHairPonyTail");
    }
    if (parts.isEmpty() && !isPuppetAppearance(def)) {
      return null;
    }
    return new NPCAnimations(null, parts.toArray());
  }

  private static void addHumanoidPart(List<Object> parts, MonsterDef def, BodyPart slot, String sprite) {
    if (def.getAppearance() == APPEAR_FEMALE_PUPPET) {
      var appearance = AppearanceDefaultsCatalog.equippedAppearance("FEMALE", slot, sprite);
      MonsterPuppetDress.addPart(parts, appearance.bodyPart(), appearance.sprite());
    } else MonsterPuppetDress.addPart(parts, slot, sprite);
  }

  @Override
  protected boolean shouldAnimateWhileStationary() {
    return animateWhileStationary;
  }

  @Override
  protected float getStationaryAnimationPauseSeconds() {
    return stationaryAnimationPauseSeconds;
  }
}
