// src/main/java/com/perso/T4C/player/Player.java
package com.perso.T4C.player;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.audio.SoundManager;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.model.Coordinates;
import com.perso.T4C.model.Stats;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.helper.XpCurve;
import com.perso.T4C.spell.SpellData;
import com.perso.T4C.item.EquipmentBonusRules;
import lombok.Getter;

import java.util.concurrent.ThreadLocalRandom;
import java.util.Random;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Represents the in-game player: position, composed subsystems (animations,
 * movement, effects)
 * and simple stats. This class coordinates updates and rendering for the
 * player.
 */
public class Player extends Stats {
    /**
     * Callback interface for showing system messages.
     */
    @FunctionalInterface
    public interface MessageCallback {
        void showMessage(String message);
    }

    /**
     * Callback interface for notifying position changes.
     */
    @FunctionalInterface
    public interface PositionCallback {
        void onPositionChanged(float x, float y, int z);
    }

    /** Called exactly once when HP reaches zero. */
    @FunctionalInterface
    public interface DeathCallback {
        void onDeath(boolean pvpDeath);
    }

    @FunctionalInterface
    public interface ItemDropCallback {
        boolean drop(int inventoryIndex, String itemKey);
    }

    @FunctionalInterface
    public interface HealingCallback {
        void onHealing(int amount);
    }

    @FunctionalInterface
    public interface ManaRestoredCallback {
        void onManaRestored(int amount);
    }

    // Respawn coordinates
    
    private final Coordinates position = new Coordinates(0f, 0f, 0);
    private final Vector2 positionVector = new Vector2();

    @Getter
    private final PlayerAnimations animations;
    @Getter
    private final PlayerMovement movement;
    private final Random random = new Random();
    @Getter
    private int mapPixelWidth;
    @Getter
    private int mapPixelHeight;
    private MessageCallback messageCallback = null;
    private PositionCallback positionCallback = null;
    private final Map<String, Long> spellCooldowns = new HashMap<>();
    private final Map<String, Long> skillCooldowns = new HashMap<>();
    private long nextAttackReadyAtMs = 0L;
    private long stunnedUntilMs = 0L;
    private long mentalExhaustionUntilMs = 0L;
    private long physicalExhaustionUntilMs = 0L;
    private long attackExhaustionUntilMs = 0L;
    private boolean hidden = false;
    private long hiddenUntilMs = Long.MAX_VALUE;
    private boolean deathBeingHandled = false;
    /** Original quest flag 30419: number of rebirths/remorts completed. */
    @Getter
    private int rebirthCount = 0;
    @Getter
    private int lastDamageTaken = 0;
    @Getter
    private boolean respawnPointDefined = false;
    @Getter
    private float respawnWorldX;
    @Getter
    private float respawnWorldY;
    @Getter
    private int respawnWorldZ;
    private Runnable actionInterruptedCallback = null;
    private DeathCallback deathCallback = null;
    private ItemDropCallback itemDropCallback = null;
    private HealingCallback healingCallback = null;
    private ManaRestoredCallback manaRestoredCallback = null;
    private final List<ActiveBuff> activeBuffs = new ArrayList<>();
    private final Map<String, Integer> buffStatBonuses = new HashMap<>();
    private float buffSpeedMultiplier = 1.0f;
    private float gmSpeedMultiplier = 1.0f;
    private boolean playerCollisionsEnabled = true;
    /** Original client's Ctrl+C "AttackMode"/NMCombatMode: gates PvP-only interactions between players. */
    @Getter
    private boolean combatMode = false;
    private int buffDamageResistFlat = 0;
    private float buffDamageResistPercent = 0f;
    private int buffMaxHpBonus = 0;
    private float buffXpMultiplier = 0f;
    private int buffUnlimitedResourcesCount = 0;
    private final PlayerProgression progression = new PlayerProgression();
    @Getter
    private Map<String, Integer> questFlags = new HashMap<>();
    /** MainObject talk text: the original client keeps it visible for 10 s. */
    private String talkText;
    private long talkTextExpiresAt;
    @Getter
    private List<String> inventory = new ArrayList<>();
    @Getter
    private Map<BodyPart, String> equippedItems = new HashMap<>();
    /** Total remaining uses for each charged item stack. */
    @Getter
    private Map<String, Integer> itemCharges = new HashMap<>();

    /**
     * Construct player with ordered pairs of BodyPart and base sprite name.
     * Example: new Player(BodyPart.BODY, "HERO", BodyPart.HEAD, "HAT")
     */
    public Player(Object... parts) throws GameException {
        this.animations = new PlayerAnimations(parts);
        this.movement = new PlayerMovement();
        this.position.set(0f, 0f, 0);
    }

    /**
     * Called when runtime textures were reloaded/cleared so animations can be
     * rebuilt.
     */
    public void onResourcesReloaded() {
        try {
            animations.refresh();
        } catch (Throwable t) {
            // best-effort: don't crash the game on reload
        }
    }

    /**
     * Set the pixel bounds of the current map for clamping position.
     */
    public void setMapBounds(int w, int h) {
        this.mapPixelWidth = w;
        this.mapPixelHeight = h;
    }

    /**
     * Trigger an attack using the provided movement state to determine angle.
     */
    public void attack(PlayerMovement movement) {
        attack(movement, false);
    }

    /**
     * Trigger an attack using the provided movement state to determine angle.
     *
     * @param movement The movement state (for facing angle).
     * @param bow      Whether this is a bow attack (uses the "B" frame variant).
     */
    public void attack(PlayerMovement movement, boolean bow) {
        // An attack is an exclusive action in the original client: cancel any
        // keyboard/click-to-move animation before starting the attack sequence.
        // Player.move() also ignores movement input until that sequence ends so
        // holding a direction cannot restart the walk animation on the next tick.
        movement.stop();
        String angle = movement.getCurrentAngle();
        animations.startAttack(angle, bow);

        // Play random whoosh sound effect for attack
        String[] whooshSounds = {"Whooshh 1.wav", "Whooshh 2.wav", "Whooshh 3.wav"};
        String sound = whooshSounds[random.nextInt(whooshSounds.length)];
        SoundManager.animateSound(sound);
    }

    public void setWorldPosition(float x, float y, int z) {
        int previousZ = position.getZ();
        position.set(x, y, z);
        positionVector.set(x, y);
        if (positionCallback != null && previousZ != z) {
            positionCallback.onPositionChanged(x, y, z);
        }
    }

    public Coordinates getCoordinates() {
        return position;
    }

    public void setInventory(List<String> inventory) {
        this.inventory = inventory == null ? new ArrayList<>() : new ArrayList<>(inventory);
    }

    public void setEquippedItems(Map<BodyPart, String> equippedItems) {
        this.equippedItems = equippedItems == null ? new HashMap<>() : new HashMap<>(equippedItems);
    }

    public void showTalkText(String text) {
        if (text == null || text.isBlank()) {
            stopTalkText();
            return;
        }
        talkText = text;
        talkTextExpiresAt = System.currentTimeMillis() + 10_000L;
    }

    public String getTalkText() {
        if (talkText != null && System.currentTimeMillis() >= talkTextExpiresAt) {
            stopTalkText();
        }
        return talkText;
    }

    public void stopTalkText() {
        talkText = null;
        talkTextExpiresAt = 0L;
    }

    public int getQuestFlag(String flag) {
        return flag == null ? 0 : questFlags.getOrDefault(flag.trim(), 0);
    }

    public void setQuestFlag(String flag, int value) {
        if (flag == null || flag.isBlank()) return;
        questFlags.put(flag.trim(), value);
    }

    public void setQuestFlags(Map<String, Integer> flags) {
        questFlags = flags == null ? new HashMap<>() : new HashMap<>(flags);
    }

    public void setItemCharges(Map<String, Integer> itemCharges) {
        this.itemCharges = itemCharges == null ? new HashMap<>() : new HashMap<>(itemCharges);
    }

    public void setRespawnPoint(float worldX, float worldY, int worldZ) {
        respawnPointDefined = true;
        respawnWorldX = worldX;
        respawnWorldY = worldY;
        respawnWorldZ = worldZ;
    }

    public void clearRespawnPoint() {
        respawnPointDefined = false;
    }

    public float resolveRespawnWorldX() {
        return respawnPointDefined ? respawnWorldX
                : com.perso.T4C.config.GameConstants.PLAYER_RESPAWN_TILE_X * com.perso.T4C.config.GameConstants.GRID_W;
    }

    public float resolveRespawnWorldY() {
        return respawnPointDefined ? respawnWorldY
                : com.perso.T4C.config.GameConstants.PLAYER_RESPAWN_TILE_Y * com.perso.T4C.config.GameConstants.GRID_H;
    }

    public int resolveRespawnWorldZ() {
        return respawnPointDefined ? respawnWorldZ
                : com.perso.T4C.config.GameConstants.PLAYER_RESPAWN_TILE_Z;
    }

    public Vector2 getPositionVector() {
        positionVector.set(position.getX(), position.getY());
        return positionVector;
    }

    /**
     * Request a movement update using input deltas.
     */
    public void move(float dx, float dy, float delta) {
        if (isStunned() || isPhysicallyExhausted() || animations.isAttacking()) {
            movement.stop();
            return;
        }
        movement.move(this, dx, dy, delta);
    }

    /**
     * Update the player state, including animations.
     */
    public void update(float delta) {
        if (isStunned() || isPhysicallyExhausted()) {
            movement.stop();
        }
        animations.update(delta, movement.isMoving());
        tickRegenBuffs(delta);
        removeExpiredBuffs();
    }

    public void render(SpriteBatch batch) {
        // Render everything by default
        positionVector.set(position.getX(), position.getY());
        animations.render(batch, positionVector, movement);
    }

    public void render(SpriteBatch batch, ShaderProgram outlineShader, boolean hovered) {
        positionVector.set(position.getX(), position.getY());
        float healthPercent = maxHp <= 0 ? 0f : (float) currentHp / (float) maxHp;
        animations.render(batch, positionVector, movement, outlineShader, hovered, healthPercent);
    }

    public void renderOcclusionReveal(SpriteBatch batch) {
        positionVector.set(position.getX(), position.getY());
        animations.renderComposite(batch, positionVector, movement);
    }

    public boolean isMouseOver(float worldX, float worldY) {
        positionVector.set(position.getX(), position.getY());
        return animations.isMouseOver(worldX, worldY, positionVector, movement);
    }

    public void dispose() {
        animations.dispose();
    }

    /**
     * Set the callback to be invoked to show system messages.
     */
    public void setMessageCallback(MessageCallback callback) {
        this.messageCallback = callback;
    }

    /**
     * Set the callback to be invoked when position changes (e.g., teleport).
     */
    public void setPositionCallback(PositionCallback callback) {
        this.positionCallback = callback;
    }

    public void setDeathCallback(DeathCallback callback) {
        this.deathCallback = callback;
    }

    public void setActionInterruptedCallback(Runnable callback) {
        this.actionInterruptedCallback = callback;
    }

    public void setItemDropCallback(ItemDropCallback callback) {
        this.itemDropCallback = callback;
    }

    public void setHealingCallback(HealingCallback callback) {
        this.healingCallback = callback;
    }

    public void notifyHealing(int amount) {
        if (amount > 0 && healingCallback != null) {
            healingCallback.onHealing(amount);
        }
    }

    public void setManaRestoredCallback(ManaRestoredCallback callback) {
        this.manaRestoredCallback = callback;
    }

    public void notifyManaRestored(int amount) {
        if (amount > 0 && manaRestoredCallback != null) {
            manaRestoredCallback.onManaRestored(amount);
        }
    }

    public boolean dropInventoryItem(int inventoryIndex, String itemKey) {
        return itemDropCallback != null && itemDropCallback.drop(inventoryIndex, itemKey);
    }

    /**
     * Apply damage to the player.
     *
     * @param damage The amount of damage to apply
     */
    public void takeDamage(int damage) {
        takeDamage(damage, false);
    }

    /** Apply damage and retain whether the killing blow was PvP. */
    public void takeDamage(int damage, boolean pvpDamage) {
        lastDamageTaken = 0;
        if (currentHp <= 0 || damage <= 0 || hasUnlimitedResources()) return;
        int mitigated = damage;
        if (buffDamageResistPercent > 0f)
            mitigated = (int) Math.ceil(mitigated * (1f - Math.min(buffDamageResistPercent, 0.99f)));
        mitigated = Math.max(1, mitigated - buffDamageResistFlat);
        mitigated = Math.min(currentHp, mitigated);
        lastDamageTaken = mitigated;
        currentHp -= mitigated;
        if (currentHp < 0) currentHp = 0;
        if (currentHp <= 0) handleDeath(pvpDamage);
    }

    /**
     * Handle player death: teleport to respawn point and show message.
     */
    private void handleDeath(boolean pvpDeath) {
        if (deathBeingHandled) return;
        deathBeingHandled = true;
        if (deathCallback != null) {
            deathCallback.onDeath(pvpDeath);
            deathBeingHandled = false;
            return;
        }
        // Teleport to respawn point
        float respawnX = resolveRespawnWorldX();
        float respawnY = resolveRespawnWorldY();
        setWorldPosition(respawnX, respawnY, resolveRespawnWorldZ());

        // Notify position change for camera update
        if (positionCallback != null) {
            positionCallback.onPositionChanged(respawnX, respawnY, position.getZ());
        }

        // Respawn with full health
        respawn();

        // Show system message
        if (messageCallback != null) {
            messageCallback.showMessage(I18n.message("message.spirit_rebirth"));
        }
        deathBeingHandled = false;
    }

    /**
     * Respawn the player with full health.
     */
    public void respawn() {
        currentHp = maxHp;
        stunnedUntilMs = 0L;
        deathBeingHandled = false;
    }

    /** Original death flow restores half HP at the place of rebirth. */
    public void respawnAfterDeath() {
        currentHp = Math.max(1, maxHp / 2);
        stunnedUntilMs = 0L;
        deathBeingHandled = false;
    }

    public boolean hasEnoughMana(String costRaw) {
        if (costRaw == null || costRaw.isEmpty()) {
            return true;
        }
        try {
            int cost = Integer.parseInt(costRaw.trim());
            return mana >= cost;
        } catch (NumberFormatException ignored) {
            return true;
        }
    }

    public void spendMana(String costRaw) {
        if (costRaw == null || costRaw.isEmpty()) {
            return;
        }
        try {
            int cost = Integer.parseInt(costRaw.trim());
            if (cost <= 0) {
                return;
            }
            mana = Math.max(0, mana - cost);
        } catch (NumberFormatException ignored) {
        }
    }

    public void applyHeal(int min, int max) {
        int low = Math.min(min, max);
        int high = Math.max(min, max);
        if (high <= 0) {
            return;
        }
        low = Math.max(1, low);
        int amount = (low == high) ? low : ThreadLocalRandom.current().nextInt(low, high + 1);
        currentHp = Math.min(maxHp, currentHp + amount);
    }

    public void addXp(int amount, XpCurve xpCurve) {
        progression.addXp(this, amount, xpCurve, true);
    }

    /** Grants an exact XP amount while still applying normal level-up handling. */
    public void addXpExact(int amount, XpCurve xpCurve) {
        progression.addXp(this, amount, xpCurve, false);
    }

    public com.perso.T4C.skill.SkillService.Result learnSkill(String skillId) {
        return com.perso.T4C.skill.SkillService.learn(this, skillId);
    }

    public com.perso.T4C.skill.SkillService.Result useSkill(String skillId) {
        return com.perso.T4C.skill.SkillService.use(this, skillId);
    }

    public com.perso.T4C.skill.SkillService.Result trainSkill(String skillId, int points, int maximum) {
        return com.perso.T4C.skill.SkillService.train(this, skillId, points, maximum);
    }

    /** Add gold to the player's purse (clamped at >= 0). */
    public void addGold(int amount) {
        if (amount == 0) {
            return;
        }
        setGold(Math.max(0, getGold() + amount));
    }

    void showLevelUpMessage(int newLevel) {
        if (messageCallback != null) {
            messageCallback.showMessage(I18n.message("message.level_up",  newLevel));
        }
    }

    public boolean isSpellOnCooldown(String spellName) {
        if (spellName == null || spellName.isEmpty()) {
            return false;
        }
        Long until = spellCooldowns.get(spellName);
        return until != null && until > System.currentTimeMillis();
    }

    public float getSpellCooldownRemainingSeconds(String spellName) {
        if (spellName == null || spellName.isEmpty()) {
            return 0f;
        }
        Long until = spellCooldowns.get(spellName);
        if (until == null) {
            return 0f;
        }
        long remainingMs = until - System.currentTimeMillis();
        return remainingMs > 0 ? remainingMs / 1000f : 0f;
    }

    public void triggerSpellCooldown(String spellName, int cooldownSeconds) {
        if (spellName == null || spellName.isEmpty() || cooldownSeconds <= 0) {
            return;
        }
        long until = System.currentTimeMillis() + (long) cooldownSeconds * 1000L;
        spellCooldowns.put(spellName, until);
    }

    /** Whether the player's weapon attack cooldown has elapsed. */
    public boolean isAttackReady() {
        long now = System.currentTimeMillis();
        return now >= nextAttackReadyAtMs && now >= attackExhaustionUntilMs && now >= stunnedUntilMs;
    }

    /**
     * Starts the attack cooldown based on the weapon's attack speed.
     *
     * @param attacksPerSecond Attacks per second (<= 0 falls back to 1.0).
     */
    public void triggerAttackCooldown(double attacksPerSecond) {
        double aps = attacksPerSecond <= 0 ? 1.0 : attacksPerSecond;
        nextAttackReadyAtMs = System.currentTimeMillis() + (long) (1000.0 / aps);
    }

    public boolean isSkillReady(String skillId) {
        if (skillId == null) return false;
        return skillCooldowns.getOrDefault(skillId, 0L) <= System.currentTimeMillis() && !isStunned();
    }

    public void triggerSkillCooldown(String skillId, long cooldownMillis) {
        if (skillId != null && cooldownMillis > 0L) {
            skillCooldowns.put(skillId, System.currentTimeMillis() + cooldownMillis);
        }
    }

    public boolean isStunned() {
        return stunnedUntilMs > System.currentTimeMillis();
    }

    public void stunFor(long durationMillis) {
        if (durationMillis <= 0L) return;
        stunnedUntilMs = Math.max(stunnedUntilMs, System.currentTimeMillis() + durationMillis);
    }

    public void clearStun() {
        stunnedUntilMs = 0L;
    }

    public boolean isHidden() {
        if (hidden && hiddenUntilMs != Long.MAX_VALUE && hiddenUntilMs <= System.currentTimeMillis()) {
            hidden = false;
        }
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
        this.hiddenUntilMs = hidden ? Long.MAX_VALUE : 0L;
    }

    public void setHiddenFor(long durationMillis) {
        hidden = durationMillis > 0L;
        hiddenUntilMs = hidden ? System.currentTimeMillis() + durationMillis : 0L;
    }

    public long getHiddenRemainingMillis() {
        if (!isHidden()) return 0L;
        return hiddenUntilMs == Long.MAX_VALUE ? Long.MAX_VALUE
                : Math.max(0L, hiddenUntilMs - System.currentTimeMillis());
    }

    public void toggleCombatMode() {
        setCombatMode(!combatMode);
    }

    public void setCombatMode(boolean combatMode) {
        this.combatMode = combatMode;
        if (messageCallback != null) {
            messageCallback.showMessage(combatMode
                    ? I18n.message("message.combat.enter")
                    : I18n.message("message.combat.leave"));
        }
    }

    public void disturb() {
        setHidden(false);
        interruptActions();
    }

    public void interruptActions() {
        if (actionInterruptedCallback != null) actionInterruptedCallback.run();
    }

    public boolean isMentallyExhausted() {
        return mentalExhaustionUntilMs > System.currentTimeMillis();
    }

    public boolean isPhysicallyExhausted() {
        return physicalExhaustionUntilMs > System.currentTimeMillis();
    }

    public void applyExhaustion(long mentalMillis, long physicalMillis, long attackMillis) {
        long now = System.currentTimeMillis();
        mentalExhaustionUntilMs = Math.max(mentalExhaustionUntilMs, now + Math.max(0L, mentalMillis));
        physicalExhaustionUntilMs = Math.max(physicalExhaustionUntilMs, now + Math.max(0L, physicalMillis));
        attackExhaustionUntilMs = Math.max(attackExhaustionUntilMs, now + Math.max(0L, attackMillis));
        if (physicalMillis > 0L) {
            movement.stop();
        }
    }

    public int getEffectiveStrength()     { return strength     + buffStatBonuses.getOrDefault("str", 0) + EquipmentBonusRules.bonus(this, 3); }
    public int getEffectiveDexterity()    { return dexterity    + buffStatBonuses.getOrDefault("dex", 0) + EquipmentBonusRules.bonus(this, 6); }
    public int getEffectiveEndurance()    { return endurance    + buffStatBonuses.getOrDefault("end", 0) + EquipmentBonusRules.bonus(this, 2); }
    public int getEffectiveIntelligence() { return intelligence + buffStatBonuses.getOrDefault("int", 0) + EquipmentBonusRules.bonus(this, 1); }
    public int getEffectiveWisdom()       { return wisdom       + buffStatBonuses.getOrDefault("wis", 0) + EquipmentBonusRules.bonus(this, 4); }
    /** Equivalent of QueryBoost(STAT_AC); excludes the obsolete persisted base-AC field. */
    public int getArmorClassBoost()       { return buffStatBonuses.getOrDefault("armorClass", 0) + EquipmentBonusRules.bonus(this, 20); }
    public int getElementResistance(String element) {
        int stat = switch (element == null ? "" : element.toLowerCase()) {
            case "air" -> 12; case "fire" -> 13; case "water" -> 14;
            case "earth" -> 15; case "light" -> 21; case "dark" -> 22;
            default -> Integer.MIN_VALUE;
        };
        String normalized = element == null ? "" : element.toLowerCase();
        return 100 + buffStatBonuses.getOrDefault("resist:" + normalized, 0)
                + EquipmentBonusRules.bonus(this, stat);
    }
    public int getElementPower(String element) {
        int stat = switch (element == null ? "" : element.toLowerCase()) {
            case "air" -> 16; case "fire" -> 17; case "water" -> 18;
            case "earth" -> 19; case "light" -> 23; case "dark" -> 24;
            default -> Integer.MIN_VALUE;
        };
        int base = getSkillLevel(element);
        String normalized = element == null ? "" : element.toLowerCase();
        return (base <= 0 ? 100 : base) + buffStatBonuses.getOrDefault("power:" + normalized, 0)
                + EquipmentBonusRules.bonus(this, stat);
    }
    public int getEffectiveSkillLevel(String skillId) {
        return getSkillLevel(skillId) + buffStatBonuses.getOrDefault("skill:" + skillId, 0)
                + EquipmentBonusRules.bonus(this, directSkillStatId(skillId))
                + EquipmentBonusRules.bonus(this, skillBoostStatId(skillId));
    }
    private static int directSkillStatId(String skillId) {
        if (skillId == null) return Integer.MIN_VALUE;
        return switch (skillId) {
            case "attack" -> 8; case "dodge" -> 9;
            default -> Integer.MIN_VALUE;
        };
    }
    private static int skillBoostStatId(String skillId) {
        if (skillId == null) return Integer.MIN_VALUE;
        return switch (skillId) {
            case "stun_blow" -> 10001; case "powerful_blow" -> 10002;
            case "parry" -> 10008; case "dodge" -> 10011; case "attack" -> 10012;
            case "hide" -> 10014; case "rob" -> 10015; case "sneak" -> 10016;
            case "picklock" -> 10026; case "armor_penetration" -> 10027;
            case "peek" -> 10028; case "rapid_healing" -> 10029;
            case "archery" -> 10035; case "two_weapons" -> 10036;
            default -> Integer.MIN_VALUE;
        };
    }
    public float getEffectiveSpeedMultiplier() { return buffSpeedMultiplier * gmSpeedMultiplier; }

    public float getGmSpeedMultiplier() { return gmSpeedMultiplier; }

    public void setGmSpeedMultiplier(float gmSpeedMultiplier) {
        this.gmSpeedMultiplier = Math.max(0.1f, Math.min(10.0f, gmSpeedMultiplier));
    }

    public boolean isPlayerCollisionsEnabled() { return playerCollisionsEnabled; }

    public void setPlayerCollisionsEnabled(boolean playerCollisionsEnabled) {
        this.playerCollisionsEnabled = playerCollisionsEnabled;
    }

    public List<ActiveBuff> getActiveBuffs() {
        return activeBuffs;
    }

    public void setRebirthCount(int rebirthCount) {
        this.rebirthCount = Math.max(0, rebirthCount);
    }

    public boolean hasBuff(String spellName) {
        if (spellName == null || spellName.isEmpty()) {
            return false;
        }
        for (ActiveBuff buff : activeBuffs) {
            if (buff != null && spellName.equals(buff.getSpellName())) {
                return true;
            }
        }
        return false;
    }

    /** Returns whether an active buff currently emits Radiance. */
    public boolean hasRadianceBuff() {
        for (ActiveBuff buff : activeBuffs) {
            if (buff == null || buff.getEffects() == null) continue;
            for (SpellData.SpellEffect effect : buff.getEffects()) {
                if (effect != null && "radiance".equals(normalizeAttr(effect.getAttribute()))
                        && parseEffectFloat(effect.getAmount()) > 0f) {
                    return true;
                }
            }
        }
        return false;
    }

    public void applyBuff(String spellName, String description, String iconId, Integer durationSeconds, boolean unlimited) {
        applyBuff(spellName, description, iconId, durationSeconds, unlimited, Collections.emptyList());
    }

    public void applyBuff(String spellName, String description, String iconId, Integer durationSeconds, boolean unlimited,
                          List<SpellData.SpellEffect> effects) {
        if (spellName == null || spellName.isEmpty()) {
            return;
        }
        List<SpellData.SpellEffect> safeEffects = effects != null ? effects : Collections.emptyList();
        long durationMillis = unlimited || durationSeconds == null || durationSeconds <= 0
                ? Long.MAX_VALUE
                : (long) durationSeconds * 1000L;
        long expiresAt = unlimited || durationSeconds == null || durationSeconds <= 0
                ? Long.MAX_VALUE
                : System.currentTimeMillis() + durationMillis;
        for (ActiveBuff buff : activeBuffs) {
            if (spellName.equals(buff.getSpellName())) {
                removeBuffContributions(buff);
                buff.refresh(description, iconId, expiresAt, durationMillis, safeEffects);
                addBuffContributions(buff);
                return;
            }
        }
        ActiveBuff newBuff = new ActiveBuff(spellName, description, iconId, expiresAt, durationMillis, safeEffects);
        activeBuffs.add(newBuff);
        addBuffContributions(newBuff);
    }

    private void removeExpiredBuffs() {
        long now = System.currentTimeMillis();
        Iterator<ActiveBuff> it = activeBuffs.iterator();
        while (it.hasNext()) {
            ActiveBuff buff = it.next();
            if (buff.getExpiresAtMillis() <= now) {
                removeBuffContributions(buff);
                it.remove();
            }
        }
    }

    private void addBuffContributions(ActiveBuff buff) {
        for (SpellData.SpellEffect effect : buff.getEffects()) {
            if (effect == null || effect.getType() == null) continue;
            switch (effect.getType()) {
                case "ATTRIBUTE" -> {
                    String key = normalizeAttr(effect.getAttribute());
                    int amount = parseAmount(effect.getAmount());
                    if ("maxHp".equals(key)) {
                        maxHp = Math.max(1, maxHp + amount);
                        buffMaxHpBonus += amount;
                    } else if ("exp".equals(key)) {
                        buffXpMultiplier += parseEffectFloat(effect.getAmount()) / 100f;
                    } else if ("unlimited".equals(key)) {
                        buffUnlimitedResourcesCount++;
                    } else if (key != null) {
                        buffStatBonuses.merge(key, amount, Integer::sum);
                    }
                }
                case "SPEED"  -> buffSpeedMultiplier += parseEffectFloat(effect.getAmount());
                case "RESIST" -> {
                    if ("percent".equalsIgnoreCase(effect.getAttribute()))
                        buffDamageResistPercent += parseEffectFloat(effect.getAmount());
                    else
                        buffDamageResistFlat += parseAmount(effect.getAmount());
                }
                default -> { /* REGEN tick-based, LIGHT/TELEPORT ignorés */ }
            }
        }
    }

    private void removeBuffContributions(ActiveBuff buff) {
        for (SpellData.SpellEffect effect : buff.getEffects()) {
            if (effect == null || effect.getType() == null) continue;
            switch (effect.getType()) {
                case "ATTRIBUTE" -> {
                    String key = normalizeAttr(effect.getAttribute());
                    int amount = parseAmount(effect.getAmount());
                    if ("maxHp".equals(key)) {
                        maxHp = Math.max(1, maxHp - amount);
                        buffMaxHpBonus -= amount;
                        currentHp = Math.min(currentHp, maxHp);
                    } else if ("exp".equals(key)) {
                        buffXpMultiplier -= parseEffectFloat(effect.getAmount()) / 100f;
                    } else if ("unlimited".equals(key)) {
                        buffUnlimitedResourcesCount = Math.max(0, buffUnlimitedResourcesCount - 1);
                    } else if (key != null) {
                        buffStatBonuses.merge(key, -amount, Integer::sum);
                    }
                }
                case "SPEED"  -> buffSpeedMultiplier -= parseEffectFloat(effect.getAmount());
                case "RESIST" -> {
                    if ("percent".equalsIgnoreCase(effect.getAttribute()))
                        buffDamageResistPercent -= parseEffectFloat(effect.getAmount());
                    else
                        buffDamageResistFlat -= parseAmount(effect.getAmount());
                }
                default -> { }
            }
        }
        // Guard against floating-point drift on speed
        if (buffSpeedMultiplier < 0.01f) buffSpeedMultiplier = 1.0f;
        if (buffDamageResistPercent < 0f) buffDamageResistPercent = 0f;
        if (buffDamageResistFlat < 0) buffDamageResistFlat = 0;
        if (buffXpMultiplier < 0f) buffXpMultiplier = 0f;
    }

    public float getBuffXpMultiplier() {
        return buffXpMultiplier;
    }

    public boolean hasUnlimitedResources() {
        return buffUnlimitedResourcesCount > 0;
    }

    private static String normalizeAttr(String attr) {
        if (attr == null) return null;
        return switch (attr.toLowerCase().trim()) {
            case "str", "strength"    -> "str";
            case "dex", "dexterity"   -> "dex";
            case "end", "endurance"   -> "end";
            case "int", "intelligence"-> "int";
            case "wis", "wisdom"      -> "wis";
            case "ac", "armorclass", "armorClass" -> "armorClass";
            case "max hp", "maxhp" -> "maxHp";
            case "attack", "skill:attack" -> "skill:attack";
            case "archery", "skill 35", "skill:archery" -> "skill:archery";
            case "dodge", "skill:dodge" -> "skill:dodge";
            case "skill:9", "skill:29", "power:air", "power:fire", "power:water", "power:earth",
                    "power:light", "power:dark", "mana", "radiance" -> attr.toLowerCase().trim();
            case "exp", "experience", "xp" -> "exp";
            case "unlimited" -> "unlimited";
            case "resist:air", "resist:fire", "resist:water", "resist:earth", "resist:light", "resist:dark" ->
                    attr.toLowerCase().trim();
            default -> null;
        };
    }

    private static int parseAmount(String s) {
        if (s == null || s.isBlank()) return 0;
        try { return Integer.parseInt(s.trim()); } catch (NumberFormatException e) { return 0; }
    }

    public int getBaseMaxHp() {
        return Math.max(1, maxHp - buffMaxHpBonus);
    }

    private static float parseEffectFloat(String s) {
        if (s == null || s.isBlank()) return 0f;
        try { return Float.parseFloat(s.trim()); } catch (NumberFormatException e) { return 0f; }
    }

    private void tickRegenBuffs(float delta) {
        for (ActiveBuff buff : activeBuffs) {
            int[] gains = buff.tickRegen(delta);
            if (gains[0] > 0) currentHp = Math.min(maxHp, currentHp + gains[0]);
            if (gains[1] > 0) mana = Math.min(maxMana, mana + gains[1]);
        }
    }

    public void adjustBuffRemaining(String spellName, long remainingSeconds) {
        if (spellName == null || spellName.isEmpty() || remainingSeconds <= 0L) {
            return;
        }
        long expiresAt = System.currentTimeMillis() + remainingSeconds * 1000L;
        for (ActiveBuff buff : activeBuffs) {
            if (spellName.equals(buff.getSpellName()) && buff.getExpiresAtMillis() != Long.MAX_VALUE) {
                buff.expiresAtMillis = expiresAt;
                return;
            }
        }
    }

    public boolean dispelBuff(String spellName) {
        if (spellName == null) return false;
        Iterator<ActiveBuff> iterator = activeBuffs.iterator();
        while (iterator.hasNext()) {
            ActiveBuff buff = iterator.next();
            if (spellName.equals(buff.getSpellName())) {
                removeBuffContributions(buff);
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    @Getter
    public static final class ActiveBuff {
        private final String spellName;
        private String description;
        private String iconId;
        private long expiresAtMillis;
        private long durationMillis;
        private List<SpellData.SpellEffect> effects;
        private float regenAccHp = 0f;
        private float regenAccMana = 0f;

        private ActiveBuff(String spellName, String description, String iconId, long expiresAtMillis, long durationMillis,
                           List<SpellData.SpellEffect> effects) {
            this.spellName = spellName;
            this.description = description;
            this.iconId = iconId;
            this.expiresAtMillis = expiresAtMillis;
            this.durationMillis = durationMillis;
            this.effects = effects != null ? effects : Collections.emptyList();
        }

        private void refresh(String description, String iconId, long expiresAtMillis, long durationMillis,
                             List<SpellData.SpellEffect> effects) {
            this.description = description;
            this.iconId = iconId;
            this.expiresAtMillis = expiresAtMillis;
            this.durationMillis = durationMillis;
            this.effects = effects != null ? effects : Collections.emptyList();
            this.regenAccHp = 0f;
            this.regenAccMana = 0f;
        }

        /** Returns [hpGain, manaGain] for this frame's delta. */
        int[] tickRegen(float delta) {
            int hpGain = 0;
            int manaGain = 0;
            for (SpellData.SpellEffect effect : effects) {
                if (effect == null || !"REGEN".equals(effect.getType())) continue;
                float rate = parseEffectFloat(effect.getAmount());
                if (rate <= 0f) continue;
                boolean isMana = "mana".equalsIgnoreCase(effect.getAttribute());
                if (isMana) {
                    regenAccMana += rate * delta;
                    int ticks = (int) regenAccMana;
                    regenAccMana -= ticks;
                    manaGain += ticks;
                } else {
                    regenAccHp += rate * delta;
                    int ticks = (int) regenAccHp;
                    regenAccHp -= ticks;
                    hpGain += ticks;
                }
            }
            return new int[]{hpGain, manaGain};
        }

        private static float parseEffectFloat(String s) {
            if (s == null || s.isBlank()) return 0f;
            try { return Float.parseFloat(s.trim()); } catch (NumberFormatException e) { return 0f; }
        }

    }
}

