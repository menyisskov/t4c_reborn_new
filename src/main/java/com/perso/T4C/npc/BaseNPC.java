package com.perso.T4C.npc;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.entity.Nameable;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.helper.CollisionManager;
import com.perso.T4C.helper.Pathfinding;
import com.perso.T4C.model.Stats;
import com.perso.T4C.player.Player;
import com.perso.T4C.ui.SystemMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Pattern;

import static com.perso.T4C.config.GameConstants.ENTITY_COLLISION_CLEARANCE_TILES;
import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;
import static com.perso.T4C.config.GameConstants.MONSTER_ATTACK_COOLDOWN;
import static com.perso.T4C.config.GameConstants.MONSTER_ATTACK_RANGE;
import static com.perso.T4C.config.GameConstants.NPC_HOSTILE_DAMAGE_MAX;
import static com.perso.T4C.config.GameConstants.NPC_HOSTILE_DAMAGE_MIN;
import static com.perso.T4C.config.GameConstants.NPC_HOSTILE_LEASH_RANGE;
import static com.perso.T4C.config.GameConstants.NPC_INTERACTION_RANGE_TILES_SQUARED;
import static com.perso.T4C.config.GameConstants.NPC_PATROL_PAUSE_MAX;
import static com.perso.T4C.config.GameConstants.NPC_PATROL_PAUSE_MIN;
import static com.perso.T4C.config.GameConstants.NPC_PATROL_RADIUS;
import static com.perso.T4C.config.GameConstants.NPC_SPEED;

/**
 * Base class for all NPCs with patrol, interaction, and rendering.
 */
@Slf4j
@Getter
public abstract class BaseNPC extends Stats implements Nameable {
    private static final int DIALOG_CONTENT_LINES_PER_PAGE = 12;

    private static final long INTERACTION_RETRY_DELAY_MS = 5_000L;

    protected final String name;
    protected String displayName;
    protected final Vector2 position = new Vector2();
    protected final Vector2 initialPosition = new Vector2();

    protected final NPCAnimations animations;
    protected final NPCMovement movement;

    // Patrol state
    protected Vector2 patrolTarget = null;
    private final List<Vector2> plannedPath = new ArrayList<>();
    private Vector2 plannedPathTarget = null;
    private boolean plannedPathUnavailable = false;
    protected float pauseTimer;
    protected boolean isPaused;
    protected Random random = new Random();

    // Interaction state
    @Setter
    protected boolean isHovered = false;
    /** Scratch rectangle for hit-testing object appearances, to avoid allocating per mouse move. */
    private final Rectangle mouseOverBounds = new Rectangle();
    protected boolean isInteracting = false;
    protected Vector2 savedPatrolTarget = null;
    protected float savedPauseTimer = 0f;
    protected boolean wasPaused = false;
    protected List<String> dialogLines = null;
    protected int dialogPageIndex = 0;
    protected boolean dialogActive = false;
    protected String dialogKeyword = null;
    protected final List<Rectangle> dialogKeywordBounds = new ArrayList<>();
    protected final Map<String, List<Rectangle>> dialogLinkBounds = new LinkedHashMap<>();
    protected boolean stationary = false;
    private long lastInteractionRequestAt = 0L;
    protected volatile long nameDisplayUntil = 0L;
    private long shoutUntil = 0L;
    protected boolean isHostile = false;
    protected float attackCooldownTimer = 0f;
    @Setter
    private NpcDamageCallback damageCallback = null;

    /**
     * Constructor for BaseNPC.
     *
     * @param name  NPC name
     * @param parts body parts configuration (BodyPart, String baseName, ...)
     */
    protected BaseNPC(String name, Object... parts) throws GameException {
        this(name, null, parts);
    }

    protected BaseNPC(String name, String spriteBase, Object... parts) throws GameException {
        this.name = name;
        this.displayName = name; // default display name
        this.initialPosition.set(0f, 0f);
        this.position.set(0f, 0f);
        this.animations = new NPCAnimations(spriteBase, parts);
        this.movement = new NPCMovement();
        this.level = 1;
        this.currentHp = 1;
        this.maxHp = 1;
        this.mana = 0;
        this.maxMana = 0;
        pauseTimer = random.nextFloat() * (NPC_PATROL_PAUSE_MAX - NPC_PATROL_PAUSE_MIN) + NPC_PATROL_PAUSE_MIN;
        isPaused = true;
    }

    public void setSpawnPosition(float worldX, float worldY) {
        initialPosition.set(worldX, worldY);
        position.set(worldX, worldY);
        patrolTarget = null;
        clearPlannedPath();
        pauseTimer = random.nextFloat() * (NPC_PATROL_PAUSE_MAX - NPC_PATROL_PAUSE_MIN) + NPC_PATROL_PAUSE_MIN;
        isPaused = true;
        movement.stop();
    }

    public void setStationary(boolean stationary) {
        this.stationary = stationary;
        if (stationary) {
            patrolTarget = null;
            savedPatrolTarget = null;
            clearPlannedPath();
            isPaused = true;
            movement.stop();
        }
        animations.setStandingIdle(stationary && animations.hasSingleSpriteBase());
    }

    /**
     * Return the name to display for this NPC (used by Nameable).
     */
    @Override
    public String getName() {
        if (displayName != null && displayName.matches("^\\$\\{[^{}]+}$")) {
            return name;
        }
        return displayName == null || displayName.isBlank() ? name : displayName;
    }

    /**
     * Allow subclasses to customize the display name.
     */
    protected void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Update NPC state (patrol, interaction, animations).
     */
    public void update(float delta, Vector2 playerPosition) {
        if (isInteracting) {
            if (!stationary) {
                movement.faceToward(position, playerPosition);
            }
            animations.update(delta, false);
            return;
        }

        if (isHostile && playerPosition != null) {
            updateHostileCombat(delta, playerPosition);
            animations.update(delta, movement.isMoving());
            return;
        }

        if (stationary) {
            patrolTarget = null;
            clearPlannedPath();
            movement.stop();
            animations.update(delta, animations.hasSingleSpriteBase());
            return;
        }

        // Normal patrol behavior
        updatePatrol(delta);
        animations.update(delta, movement.isMoving());
    }

    /**
     * Update patrol logic with collision detection.
     */
    private void updatePatrol(float delta) {
        if (isPaused) {
            pauseTimer -= delta;
            if (pauseTimer <= 0) {
                isPaused = false;
                pickNewPatrolTarget();
            }
            movement.stop();
            return;
        }

        // Move toward patrol target
        if (patrolTarget != null) {
            float dx = patrolTarget.x - position.x;
            float dy = patrolTarget.y - position.y;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            if (distance < 2f) {
                // Reached target, pause
                position.set(patrolTarget);
                isPaused = true;
                pauseTimer = random.nextFloat() * (NPC_PATROL_PAUSE_MAX - NPC_PATROL_PAUSE_MIN) + NPC_PATROL_PAUSE_MIN;
                movement.stop();
                patrolTarget = null;
                clearPlannedPath();
            } else {
                Vector2 moveTarget = nextPathWaypoint(patrolTarget);
                if (moveTarget == null) {
                    pickNewPatrolTarget();
                    movement.stop();
                    return;
                }
                dx = moveTarget.x - position.x;
                dy = moveTarget.y - position.y;
                distance = (float) Math.sqrt(dx * dx + dy * dy);
                if (distance <= 0.001f) {
                    movement.stop();
                    return;
                }
                float ndx = dx / distance;
                float ndy = dy / distance;
                float step = Math.min(NPC_SPEED * delta, distance);
                float newX = position.x + ndx * step;
                float newY = position.y + ndy * step;

                // Check collision before moving
                CollisionManager collisionManager = CollisionManager.getInstance();
                if (collisionManager.isInitialized()) {
                    if (collisionManager.hasCollisionNear(newX, newY, ENTITY_COLLISION_CLEARANCE_TILES)) {
                        // Can't move to target, pick a new one
                        pickNewPatrolTarget();
                        movement.stop();
                        return;
                    }
                }

                position.x = newX;
                position.y = newY;
                movement.setDirection(ndx, ndy);
            }
        } else {
            pickNewPatrolTarget();
        }
    }

    /**
     * Pick a new random patrol target within patrol radius.
     */
    private void pickNewPatrolTarget() {
        clearPlannedPath();
        CollisionManager collisionManager = CollisionManager.getInstance();
        float patrolRadius = getPatrolRadius();
        for (int attempt = 0; attempt < 12; attempt++) {
            float angle = random.nextFloat() * 2f * (float) Math.PI;
            float radius = random.nextFloat() * patrolRadius;
            float targetX = initialPosition.x + (float) Math.cos(angle) * radius;
            float targetY = initialPosition.y + (float) Math.sin(angle) * radius;
            if (!collisionManager.isInitialized()
                    || !collisionManager.hasCollisionNear(targetX, targetY, ENTITY_COLLISION_CLEARANCE_TILES)) {
                patrolTarget = new Vector2(targetX, targetY);
                return;
            }
        }
        patrolTarget = null;
    }

    private Vector2 nextPathWaypoint(Vector2 target) {
        if (target == null) {
            clearPlannedPath();
            return null;
        }
        if (plannedPathTarget == null || plannedPathTarget.dst2(target) > 4f) {
            rebuildPlannedPath(target);
        }
        while (!plannedPath.isEmpty() && position.dst2(plannedPath.get(0)) < 4f) {
            plannedPath.remove(0);
        }
        return plannedPath.isEmpty() ? (plannedPathUnavailable ? null : target) : plannedPath.get(0);
    }

    private void rebuildPlannedPath(Vector2 target) {
        clearPlannedPath();
        plannedPathTarget = new Vector2(target);
        CollisionManager collisionManager = CollisionManager.getInstance();
        if (!collisionManager.isInitialized()) {
            return;
        }
        List<Pathfinding.GridPoint> path = Pathfinding.findPath(
                getTileX(),
                getTileY(),
                (int) (target.x / GRID_W),
                (int) (target.y / GRID_H),
                collisionManager.getCollisionWidth(),
                collisionManager.getCollisionHeight(),
                collisionManager,
                ENTITY_COLLISION_CLEARANCE_TILES
        );
        plannedPathUnavailable = path.isEmpty()
                && (getTileX() != (int) (target.x / GRID_W) || getTileY() != (int) (target.y / GRID_H));
        for (Pathfinding.GridPoint point : path) {
            plannedPath.add(new Vector2(point.x * GRID_W + GRID_W * 0.5f, point.y * GRID_H + GRID_H * 0.5f));
        }
    }

    private void clearPlannedPath() {
        plannedPath.clear();
        plannedPathTarget = null;
        plannedPathUnavailable = false;
    }

    /**
     * Clears hostility (e.g. when the player dies/respawns): the NPC stops
     * chasing/attacking and resumes normal patrol, matching the expectation that
     * a defeated player is no longer being fought.
     */
    public void resetHostility() {
        if (!isHostile) {
            return;
        }
        isHostile = false;
        attackCooldownTimer = 0f;
        movement.stop();
        animations.clearAttackPose();
    }

    /**
     * Makes the NPC hostile after being attacked: it chases and periodically
     * hits the player back until the player leaves aggro range or interaction
     * resets it. Interrupts any ongoing patrol/interaction.
     */
    public void provoke() {
        if (isHostile) {
            return;
        }
        isHostile = true;
        isInteracting = false;
        dialogActive = false;
        dialogLines = null;
        patrolTarget = null;
        clearPlannedPath();
        attackCooldownTimer = 0f;
    }

    private void updateHostileCombat(float delta, Vector2 playerPosition) {
        if (attackCooldownTimer > 0) {
            attackCooldownTimer -= delta;
        }

        float distanceToPlayer = position.dst(playerPosition);
        if (distanceToPlayer > NPC_HOSTILE_LEASH_RANGE) {
            isHostile = false;
            movement.stop();
            animations.clearAttackPose();
            return;
        }

        if (distanceToPlayer <= MONSTER_ATTACK_RANGE) {
            movement.stop();
            if (!stationary) {
                movement.faceToward(position, playerPosition);
            }
            if (attackCooldownTimer <= 0) {
                performHostileAttack();
                attackCooldownTimer = MONSTER_ATTACK_COOLDOWN;
            }
            return;
        }

        if (stationary) {
            movement.stop();
            return;
        }

        Vector2 moveTarget = nextPathWaypoint(playerPosition);
        if (moveTarget == null) {
            movement.stop();
            return;
        }
        float dx = moveTarget.x - position.x;
        float dy = moveTarget.y - position.y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        if (distance <= 0.001f) {
            movement.stop();
            return;
        }
        float ndx = dx / distance;
        float ndy = dy / distance;
        float step = Math.min(NPC_SPEED * delta, distance);
        float newX = position.x + ndx * step;
        float newY = position.y + ndy * step;
        CollisionManager collisionManager = CollisionManager.getInstance();
        if (collisionManager.isInitialized() && collisionManager.hasCollisionNear(newX, newY, ENTITY_COLLISION_CLEARANCE_TILES)) {
            movement.stop();
            return;
        }
        position.x = newX;
        position.y = newY;
        movement.setDirection(ndx, ndy);
    }

    private void performHostileAttack() {
        animations.startAttack(movement.getCurrentAngle());
        int damage = rollHostileDamage();
        if (damageCallback != null) {
            damageCallback.applyDamage(this, damage);
        }
    }

    protected int rollHostileDamage() {
        return NPC_HOSTILE_DAMAGE_MIN + random.nextInt(NPC_HOSTILE_DAMAGE_MAX - NPC_HOSTILE_DAMAGE_MIN + 1);
    }

    public List<Vector2> getDebugPath() {
        return plannedPath;
    }

    public Vector2 getDebugPathTarget() {
        return plannedPathUnavailable ? null : plannedPathTarget;
    }

    /**
     * Handle player click interaction.
     *
     * @param playerPosition current player position
     * @return true if interaction started
     */
    public boolean onClick(Player player) {
        Vector2 playerPosition = player.getPositionVector();
        // Clicking a distant NPC has no equivalent in GoN, where reaching one means walking up to
        // it and typing. Requiring a clear line keeps that constraint: without it a click picks
        // targets straight through walls.
        if (isWithinTalkingRange(playerPosition)
                && com.perso.T4C.combat.CombatGeometry.hasTalkLineOfSight(playerPosition, position)) {
            if (!isInteracting) {
                // Save patrol state
                savedPatrolTarget = patrolTarget;
                savedPauseTimer = pauseTimer;
                wasPaused = isPaused;

                // Start interaction
                isInteracting = true;
                patrolTarget = null;
                movement.stop();
                lastInteractionRequestAt = System.currentTimeMillis();
                onInteractStart(player);
            } else {
                long now = System.currentTimeMillis();
                if (!dialogActive || now - lastInteractionRequestAt >= INTERACTION_RETRY_DELAY_MS) {
                    lastInteractionRequestAt = now;
                    onInteractStart(player);
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Whether the player stands close enough to hold a conversation.
     *
     * <p>Mirrors the {@code Dist < 120} guard of {@code Character::StartAsyncDirectTalk}: a squared
     * tile distance, isotropic and measured in tiles rather than world units, since tiles are twice
     * as wide as they are tall. Walls are handled by the caller, not by this reach test.
     */
    private boolean isWithinTalkingRange(Vector2 playerPosition) {
        if (playerPosition == null) return false;
        float tilesX = (position.x - playerPosition.x) / GRID_W;
        float tilesY = (position.y - playerPosition.y) / GRID_H;
        return tilesX * tilesX + tilesY * tilesY < NPC_INTERACTION_RANGE_TILES_SQUARED;
    }

    /**
     * Check if player is still in range, resume patrol if not.
     */
    public void checkPlayerRange(Vector2 playerPosition) {
        if (isInteracting) {
            // Only walking out of range ends the conversation, matching the rule used to start it.
            if (!isWithinTalkingRange(playerPosition)) {
                // Player moved away, resume patrol
                endInteraction();
            }
        }
    }

    /**
     * Resume patrol after interaction.
     */
    public void endInteraction() {
        isInteracting = false;
        patrolTarget = savedPatrolTarget;
        pauseTimer = savedPauseTimer;
        isPaused = wasPaused;
        dialogActive = false;
        dialogLines = null;
        dialogPageIndex = 0;
        dialogKeywordBounds.clear();
        dialogLinkBounds.clear();
        lastInteractionRequestAt = 0L;
    }

    /**
     * Render the NPC.
     */
    public void render(SpriteBatch batch, ShaderProgram outlineShader) {
        // Stationary single-sprite NPCs (e.g. Darkfang) never set movement.isMoving()
        // (they never walk) but still need their idle-in-place animation to advance;
        // must match the "moving" flag update() used to drive animTimer, or the frame
        // index recomputed here from that timer always resolves to 0 (frozen frame).
        boolean animate = movement.isMoving() || (stationary && animations.hasSingleSpriteBase());
        animations.render(batch, position, movement.getCurrentAngle(), movement.isFlipX(), animate, isHovered, outlineShader, getName(), isNameVisible());
    }

    public void renderOcclusionReveal(SpriteBatch batch) {
        boolean animate = movement.isMoving() || (stationary && animations.hasSingleSpriteBase());
        animations.renderComposite(batch, position, movement.getCurrentAngle(), movement.isFlipX(), animate);
    }

    public Rectangle getRenderBounds(Rectangle out) {
        return animations.getRenderBounds(
                position,
                movement.getCurrentAngle(),
                movement.isFlipX(),
                movement.isMoving(),
                out);
    }

    public void dispose() {
        animations.dispose();
    }

    /**
     * Check if mouse is hovering over NPC.
     *
     * <p>Humanoids use a fixed box rather than their sprite bounds: it is deliberately wider and
     * taller than the frame so extended arms, shoulders and helmets stay clickable whatever the
     * pose. Object appearances have no such silhouette and vary wildly in size, so a portal
     * measuring 64x119 was mostly outside that human-sized box and could only be clicked on the
     * one strip where the two happened to overlap. Those follow their drawn bounds instead.
     */
    public boolean isMouseOver(float mouseX, float mouseY) {
        if (animations.hasObjectAppearance()) {
            Rectangle bounds = getRenderBounds(mouseOverBounds);
            if (bounds.width > 0f && bounds.height > 0f) {
                return bounds.contains(mouseX, mouseY);
            }
        }
        float halfWidth = 40f;   // 80 pixels wide total (covers extended arms)
        float heightAbove = 60f; // 60 pixels above position (covers head, shoulders, helmet)
        float heightBelow = 10f; // 10 pixels below position (covers feet)

        return mouseX >= position.x - halfWidth && mouseX <= position.x + halfWidth && mouseY >= position.y - heightAbove && mouseY <= position.y + heightBelow;
    }

    /**
     * Called when resources are reloaded.
     */
    public void onResourcesReloaded() {
        try {
            com.perso.T4C.entity.NameRenderer.invalidateDialogResources();
            animations.refresh();
        } catch (GameException e) {
            log.error("Failed to refresh NPC animations for {}", name, e);
        }
    }

    /**
     * Get tile X position.
     */
    public int getTileX() {
        return (int) (position.x / GRID_W);
    }

    /**
     * Get tile Y position.
     */
    public int getTileY() {
        return (int) (position.y / GRID_H);
    }

    /**
     * Show the NPC's name for the given duration (milliseconds)
     */
    @Override
    public void showNameFor(long ms) {
        nameDisplayUntil = System.currentTimeMillis() + ms;
    }

    /**
     * Whether the NPC name should currently be visible
     */
    @Override
    public boolean isNameVisible() {
        return System.currentTimeMillis() < nameDisplayUntil;
    }

    public void renderDialogOverlay(SpriteBatch batch) {
        String text = getDialogTextToRender();
        if (text != null) {
            List<String> keywords = getDialogKeywords();
            dialogKeyword = keywords.isEmpty() ? null : keywords.get(0);
            dialogKeywordBounds.clear();
            dialogLinkBounds.clear();
            animations.renderDialogText(batch, position, movement.getCurrentAngle(), movement.isFlipX(), text, keywords);
            for (String keyword : keywords) {
                List<Rectangle> bounds = animations.getDialogWordBounds(
                        position, movement.getCurrentAngle(), movement.isFlipX(), text, keyword);
                if (!bounds.isEmpty()) {
                    dialogLinkBounds.put(keyword, bounds);
                    dialogKeywordBounds.addAll(bounds);
                }
            }
        } else {
            dialogKeywordBounds.clear();
            dialogLinkBounds.clear();
        }
    }

    protected void onInteractStart(Player player) {
    }

    protected void showDialog(String text, long ms) {
        if (text == null || text.isEmpty()) {
            return;
        }
        String displayedText = removeNpcDialogueQuotes(text);
        dialogLines = wrapDialog(displayedText, 52);
        dialogPageIndex = 0;
        dialogActive = true;
        SystemMessage.showShared(displayedText);
    }

    /**
     * Shows a short head-bubble message (e.g. a shout when attacked) that
     * auto-clears after {@code durationMs}, without requiring endInteraction()
     * like a normal conversation.
     */
    public void shout(String text, long durationMs) {
        if (text == null || text.isEmpty()) {
            return;
        }
        dialogLines = wrapDialog(removeNpcDialogueQuotes(text), 52);
        dialogPageIndex = 0;
        dialogActive = true;
        shoutUntil = System.currentTimeMillis() + durationMs;
    }

    private String getDialogTextToRender() {
        if (shoutUntil > 0L && System.currentTimeMillis() >= shoutUntil) {
            shoutUntil = 0L;
            dialogActive = false;
            dialogLines = null;
        }
        if (!dialogActive || dialogLines == null || dialogLines.isEmpty()) {
            return null;
        }
        int start = dialogPageIndex * DIALOG_CONTENT_LINES_PER_PAGE;
        if (start >= dialogLines.size()) {
            dialogActive = false;
            return null;
        }
        int end = Math.min(start + DIALOG_CONTENT_LINES_PER_PAGE, dialogLines.size());
        StringBuilder builder = new StringBuilder();
        for (int i = start; i < end; i++) {
            if (builder.length() > 0) {
                // dialogLines are soft-wrapped for pagination. Do not turn
                // those pagination boundaries into forced visual line breaks:
                // NameRenderer wraps the page using the actual font metrics.
                builder.append(' ');
            }
            builder.append(dialogLines.get(i));
        }
        // CDisplayTextBox adds the farewell link after an empty line.
        builder.append("\n\n> Adieu.");
        return stripDialogKeywordQuotes(builder.toString(), getDialogKeywords());
    }

    static String stripDialogKeywordQuotes(String text, List<String> keywords) {
        if (text == null || text.isEmpty() || keywords == null || keywords.isEmpty()) return text;
        String cleaned = text;
        for (String keyword : keywords) {
            if (keyword == null || keyword.isBlank()) continue;
            Pattern quotedKeyword = Pattern.compile(
                    "[\\\"“«]\\s*(" + Pattern.quote(keyword.trim()) + ")\\s*[\\\"”»]",
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            cleaned = quotedKeyword.matcher(cleaned).replaceAll("$1");
        }
        return cleaned;
    }

    static String removeNpcDialogueQuotes(String text) {
        return text == null ? null : text.replaceAll("[\\\"“”«»]", "");
    }

    public boolean advanceDialog() {
        if (!dialogActive || dialogLines == null || dialogLines.isEmpty()) {
            return false;
        }
        dialogPageIndex++;
        if (dialogPageIndex * DIALOG_CONTENT_LINES_PER_PAGE >= dialogLines.size()) {
            dialogActive = false;
        }
        return true;
    }

    public boolean handleDialogClick(float worldX, float worldY, Player player) {
        if (!dialogActive) {
            return false;
        }
        String text = getDialogTextToRender();
        if (text == null) {
            return false;
        }
        // Use the hit boxes produced during the actual render pass. Recomputing
        // them here can use a different animation frame/anchor and makes the
        // visible link impossible to click while an NPC is moving.
        for (Map.Entry<String, List<Rectangle>> entry : dialogLinkBounds.entrySet()) {
            for (Rectangle rect : entry.getValue()) {
                if (rect.contains(worldX, worldY)) return talk(entry.getKey(), player);
            }
        }
        return false;
    }

    /**
     * Direct player speech to this NPC, as the legacy client does while a
     * conversation target is selected.
     */
    public boolean talk(String text, Player player) {
        if (!isInteracting || text == null) {
            return false;
        }
        String normalizedText = normalizeSpeech(text);
        if (normalizedText.isEmpty()) {
            return true;
        }
        if (isConversationEnd(normalizedText)) {
            endInteraction();
            return true;
        }

        for (String keyword : getDialogKeywords()) {
            if (matchesKeyword(normalizedText, keyword)) return onDialogKeywordClick(keyword, player);
        }
        return true;
    }

    private static boolean matchesKeyword(String normalizedText, String keyword) {
        String normalizedKeyword = normalizeSpeech(keyword);
        if (normalizedKeyword.isEmpty()) {
            return false;
        }
        return (" " + normalizedText + " ").contains(" " + normalizedKeyword + " ");
    }

    private static boolean isConversationEnd(String normalizedText) {
        return matchesKeyword(normalizedText, "bye")
                || matchesKeyword(normalizedText, "adieu")
                || matchesKeyword(normalizedText, "au revoir");
    }

    private static String normalizeSpeech(String text) {
        if (text == null) {
            return "";
        }
        return text.toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{L}\\p{N}]+", " ")
                .trim()
                .replaceAll("\\s+", " ");
    }

    protected String getDialogKeyword() {
        return null;
    }

    protected List<String> getDialogKeywords() {
        String keyword = getDialogKeyword();
        if (keyword == null || keyword.isBlank()) {
            return List.of("> Adieu.");
        }
        List<String> keywords = new ArrayList<>();
        keywords.add(keyword);
        keywords.add("> Adieu.");
        return keywords;
    }

    protected boolean onDialogKeywordClick(String keyword, Player player) {
        return false;
    }

    protected float getPatrolRadius() {
        return NPC_PATROL_RADIUS;
    }

    private static List<String> wrapDialog(String text, int maxChars) {
        // Legacy dialog data marks keywords with *stars*; keyword links are
        // resolved by word matching, so the markers are display noise.
        String[] words = text.replace("*", "").trim().split("\\s+");
        StringBuilder line = new StringBuilder();
        List<String> lines = new ArrayList<>();

        for (String word : words) {
            int extra = line.length() == 0 ? 0 : 1;
            if (line.length() + extra + word.length() > maxChars) {
                lines.add(line.toString());
                line.setLength(0);
            }
            if (line.length() > 0) {
                line.append(' ');
            }
            line.append(word);
        }

        if (line.length() > 0) {
            lines.add(line.toString());
        }

        return lines;
    }
}

