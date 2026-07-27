package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.monster.loot.LootTable;
import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.npc.NPCAnimations;
import com.perso.T4C.player.BodyPart;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import java.util.ArrayList;
import java.util.List;

/**
 * Concrete monster built from a {@link MonsterDef}. Replaces the former
 * hardcoded {@code com.perso.T4C.monster.types.*} subclasses.
 */
public class DataMonster extends BaseMonster {

    private final boolean animateWhileStationary;
    private final float stationaryAnimationPauseSeconds;
    private final String translationIdentity;
    private final String untranslatedDisplayName;
    private final NPCAnimations humanoidAnimations;

    public DataMonster(MonsterDef def, float initialX, float initialY) throws GameException {
        super(def.getName(), initialX, initialY,
                def.getHealth(), def.getMana(), def.getXpPerHit(), def.getXpOnDeath(),
                def.getHitDamageMin(), def.getHitDamageMax(), def.getRespawnTime(),
                def.getWalkPattern(), def.getAttackPattern(), def.getDeathPattern(),
                def.getSoundAttack(), def.getSoundDeath(), def.getSoundHit());

        translationIdentity = def.getName();
        untranslatedDisplayName = def.getDisplayName() == null || def.getDisplayName().isEmpty()
                ? def.getName() : def.getDisplayName();
        setDisplayName(untranslatedDisplayName);
        humanoidAnimations = hasMonsterAnimation(def) ? null : buildHumanoidAnimations(def);
        applyCombatDefinition(def);

        LootTable.Builder builder = LootTable.builder().gold(def.getGoldMin(), def.getGoldMax());
        if (def.getLoot() != null) {
            for (MonsterDef.LootDrop drop : def.getLoot()) {
                if (drop != null && drop.getItem() != null) {
                    builder.item(drop.getItem(), drop.getChance());
                }
            }
        }
        this.lootTable = builder.build();

        if (!def.isDefaultAggressive()) {
            setAggressive(false);
        }
        if (def.getAttacks() != null && !def.getAttacks().isEmpty()) {
            this.attacks = def.getAttacks();
        }
        this.animateWhileStationary = def.isAnimateWhileStationary();
        this.stationaryAnimationPauseSeconds = def.getStationaryAnimationPauseSeconds();
    }

    /** Resolve at display time so a language loaded/reloaded after monster creation is respected. */
    @Override
    public String getName() {
        return I18n.monster(translationIdentity, untranslatedDisplayName);
    }

    @Override
    public String getDisplayName() {
        return getName();
    }

    @Override
    public void update(float delta, com.badlogic.gdx.math.Vector2 playerPosition, List<BaseMonster> nearbyMonsters) {
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
        humanoidAnimations.render(batch, position, movement.getCurrentAngle(), movement.isFlipX(),
                movement.isMoving(), isHovered || selected, outlineShader, getName(), isNameVisible() || selected);
    }

    public boolean usesHumanoidAnimations() {
        return humanoidAnimations != null;
    }

    @Override
    protected void startAttackAnimation() {
        super.startAttackAnimation();
        if (humanoidAnimations != null) {
            humanoidAnimations.startAttack(movement.getCurrentAngle());
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

    private static boolean hasMonsterAnimation(MonsterDef def) {
        return def.getWalkPattern() != null && !def.getWalkPattern().isBlank();
    }

    private static NPCAnimations buildHumanoidAnimations(MonsterDef def) throws GameException {
        int[] ids = {def.getItemBody(), def.getItemFeet(), def.getItemHands(), def.getItemHead(),
                def.getItemLegs(), def.getItemWeapon(), def.getItemShield(), def.getItemBack()};
        List<Object> parts = new ArrayList<>();
        for (int id : ids) {
            if (id <= 0) continue;
            ItemDefinition item = findItem(id);
            if (item == null) continue;
            addPart(parts, item.getBodyPart(), item.getAppearanceEquippedPrimary());
            addPart(parts, item.getSecondaryBodyPart(), item.getAppearanceEquippedSecondary());
        }
        return parts.isEmpty() ? null : new NPCAnimations(null, parts.toArray());
    }

    private static ItemDefinition findItem(int numId) {
        for (ItemDefinition item : ItemRegistry.load()) if (item.getNumId() == numId) return item;
        return null;
    }

    private static void addPart(List<Object> parts, BodyPart bodyPart, String sprite) {
        if (bodyPart == null || sprite == null || sprite.isBlank()) return;
        parts.add(bodyPart);
        parts.add(sprite);
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
