package com.perso.T4C.monster;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.audio.SoundManager;
import com.perso.T4C.entity.Nameable;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.helper.CollisionManager;
import com.perso.T4C.monster.MonsterDef;
import com.perso.T4C.helper.Pathfinding;
import com.perso.T4C.helper.XpCurve;
import com.perso.T4C.player.Player;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import static com.perso.T4C.config.GameConstants.ENTITY_COLLISION_CLEARANCE_TILES;
import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;
import static com.perso.T4C.config.GameConstants.MONSTER_AGGRO_LEASH_RANGE;
import static com.perso.T4C.config.GameConstants.MONSTER_AGGRO_RANGE;
import static com.perso.T4C.config.GameConstants.MONSTER_RETALIATION_LEASH_RANGE;
import static com.perso.T4C.config.GameConstants.MONSTER_ATTACK_COOLDOWN;
import static com.perso.T4C.config.GameConstants.MONSTER_ATTACK_RANGE;
import static com.perso.T4C.config.GameConstants.MONSTER_PATROL_PAUSE_MAX;
import static com.perso.T4C.config.GameConstants.MONSTER_PATROL_PAUSE_MIN;
import static com.perso.T4C.config.GameConstants.MONSTER_PATROL_RADIUS;
import static com.perso.T4C.config.GameConstants.MONSTER_SPEED;


/**
 * Base class for all monsters with patrol, combat, and rendering.
 */
@Slf4j
@Getter
public abstract class BaseMonster implements Nameable {

    protected final String name;
    // Display name shown on right-click; defaults to internal name but can be customized by subclasses
    protected String displayName;
    protected final Vector2 position = new Vector2();
    protected final Vector2 initialPosition = new Vector2();

    protected final MonsterAnimations animations;
    protected final MonsterMovement movement;

    // Monster stats
    protected int health;
    protected int maxHealth;
    protected int mana;
    protected int maxMana;
    protected int xpPerHit;
    protected int xpOnDeath;
    protected int hitDamageMin;
    protected int hitDamageMax;
    protected int combatLevel = 1;
    protected int combatStrength = 1;
    protected int combatEndurance = 1;
    protected int combatAgility = 1;
    protected int combatIntelligence = 1;
    protected int combatAttack = 1;
    protected int combatDodge = 1;
    protected int combatArmorMin = 0;
    protected int combatArmorMax = 0;
    protected int[] combatResists = new int[12];
    protected long stunnedUntilMs = 0L;
    protected boolean lastAttackRanged = false;
    protected int lastAttackSpellId = 0;
    protected String soundHit;
    protected List<MonsterDef.Attack> attacks = new ArrayList<>();
    protected final MonsterClan clan;

    // Loot dropped on death (defaults to nothing; subclasses may override in their constructor)
    protected com.perso.T4C.monster.loot.LootTable lootTable = com.perso.T4C.monster.loot.LootTable.empty();

    // Patrol configuration

    // Patrol state
    protected Vector2 patrolTarget = null;
    private final List<Vector2> plannedPath = new ArrayList<>();
    private Vector2 plannedPathTarget = null;
    private boolean plannedPathUnavailable = false;
    protected float pauseTimer;
    protected boolean isPaused;
    protected Random random = new Random();

    // Combat state
    protected float attackCooldownTimer = 0f;
    protected boolean isAggro = false;
    protected boolean aggressive = true;
    protected Vector2 aggroTarget = null;
    /** True when the player explicitly provoked this monster by attacking it. */
    protected boolean retaliatingAgainstPlayer = false;
    protected BaseMonster monsterAggroTarget = null;
    /**
     * Ally companion this monster fights instead of the player. Chasing reuses
     * the player combat path (both are tracked through a live position vector);
     * only the damage recipient differs.
     */
    protected com.perso.T4C.npc.CompanionNPC companionAggroTarget = null;
    private final Vector2 fallbackDir = new Vector2();
    private float fallbackTimer = 0f;
    private float stuckCooldown = 0f;
    private final Vector2 lastFallbackDir = new Vector2();
    private int blockedStreak = 0;
    private float blockedLogTimer = 0f;
    private float pathRetryTimer = 0f;
    private float unreachableTargetTimer = 0f;

    private static final int BLOCKED_STREAK_BEFORE_REPATH = 6;
    private static final float BLOCKED_PATH_RETRY_DELAY = 0.5f;
    private static final float UNAVAILABLE_PATH_RETRY_DELAY = 0.75f;
    private static final float UNREACHABLE_TARGET_GIVE_UP_DELAY = 0.75f;

    @Setter
    private DamageCallback damageCallback = null;
    /** Applies a blow landed on the player's ally companion. */
    @Setter
    private java.util.function.IntConsumer companionDamageCallback = null;
    private java.util.function.Consumer<BaseMonster> deathCallback = null;

    // Interaction state
    @Setter
    protected boolean isHovered = false;
    /**
     * Keyboard selection (Tab). Independent from {@link #isHovered}, which the mouse
     * recomputes on every move and would otherwise wipe the selection instantly.
     */
    @Setter
    protected boolean selected = false;
    protected boolean isDead = false;
    protected long deathTime = 0L;
    protected long respawnTime;
    protected boolean stationary = false;
    private float stationaryAnimationPauseTimer = 0f;

    protected volatile long nameDisplayUntil = 0L;

    /**
     * Constructor for BaseMonster.
     *
     * @param name          Monster name
     * @param initialX      initial X position (pixel)
     * @param initialY      initial Y position (pixel)
     * @param health        health points
     * @param mana          mana points
     * @param xpPerHit      XP given per hit
     * @param xpOnDeath     XP given on death
     * @param hitDamageMin  minimum damage per hit
     * @param hitDamageMax  maximum damage per hit
     * @param respawnTime   time in milliseconds before respawning
     * @param walkPattern   walk animation pattern
     * @param attackPattern attack animation pattern
     * @param deathPattern  death animation pattern
     * @param soundAttack   attack sound
     * @param soundDeath    death sound
     */
    protected BaseMonster(String name, float initialX, float initialY, int health, int mana, int xpPerHit, int xpOnDeath, int hitDamageMin, int hitDamageMax, long respawnTime, String walkPattern, String attackPattern, String deathPattern, String soundAttack, String soundDeath) throws GameException {
        this(name, initialX, initialY, health, mana, xpPerHit, xpOnDeath, hitDamageMin, hitDamageMax, respawnTime, walkPattern, attackPattern, deathPattern, soundAttack, soundDeath, null);
    }

    /**
     * Constructor for BaseMonster with a distinct hit sound.
     *
     * @param name          Monster name
     * @param initialX      initial X position (pixel)
     * @param initialY      initial Y position (pixel)
     * @param health        health points
     * @param mana          mana points
     * @param xpPerHit      XP given per hit
     * @param xpOnDeath     XP given on death
     * @param hitDamageMin  minimum damage per hit
     * @param hitDamageMax  maximum damage per hit
     * @param respawnTime   time in milliseconds before respawning
     * @param walkPattern   walk animation pattern
     * @param attackPattern attack animation pattern
     * @param deathPattern  death animation pattern
     * @param soundAttack   attack sound
     * @param soundDeath    death sound
     * @param soundHit      sound played when taking non-lethal damage
     */
    protected BaseMonster(String name, float initialX, float initialY, int health, int mana, int xpPerHit, int xpOnDeath, int hitDamageMin, int hitDamageMax, long respawnTime, String walkPattern, String attackPattern, String deathPattern, String soundAttack, String soundDeath, String soundHit) throws GameException {
        this.name = name;
        this.displayName = name; // default display name
        this.initialPosition.set(initialX, initialY);
        this.position.set(initialX, initialY);

        this.maxHealth = health;
        this.health = health;
        this.maxMana = mana;
        this.mana = mana;
        this.xpPerHit = xpPerHit;
        this.xpOnDeath = xpOnDeath;
        this.hitDamageMin = hitDamageMin;
        this.hitDamageMax = hitDamageMax;
        this.respawnTime = respawnTime;
        this.soundHit = resolveSoundName(name, "Hit", soundHit);
        this.clan = MonsterClanRelations.resolveClan(getClass().getSimpleName(), name);

        this.animations = new MonsterAnimations(
                walkPattern,
                attackPattern,
                deathPattern,
                resolveSoundName(name, "Attack", soundAttack),
                resolveSoundName(name, "Dying", soundDeath)
        );
        this.movement = new MonsterMovement();

        // Start patrolling immediately
        isPaused = false;
        pauseTimer = 0f;
    }

    private static String resolveSoundName(String monsterName, String action, String explicitSound) {
        if (explicitSound != null && !explicitSound.isBlank()) {
            return explicitSound;
        }

        String family = resolveSoundFamily(monsterName);
        if (family == null || family.isBlank()) {
            return null;
        }
        return family + " " + action + ".wav";
    }

    private static String resolveSoundFamily(String monsterName) {
        if (monsterName == null) {
            return null;
        }
        String normalized = monsterName.replace("64k", "").trim();
        String lower = normalized.toLowerCase();

        if (lower.contains("atrocity")) return "Atrocity";
        if (lower.contains("beholder")) return "Beholder";
        if (lower.contains("bat")) return "Bat";
        if (lower.contains("centaur")) return "Centaur";
        if (lower.contains("demon")) return "Demon";
        if (lower.contains("goblin")) return "Goblin";
        if (lower.contains("kobold")) return "Kobold";
        if (lower.contains("kraanian")) return "Kraanian";
        if (lower.contains("minotaur")) return "Minotaur";
        if (lower.contains("mummy")) return "Mummy";
        if (lower.contains("orc")) return "Orc";
        if (lower.contains("pig")) return "Pig";
        if (lower.contains("rat")) return "Rat";
        if (lower.contains("scorpion")) return "Scorpion";
        if (lower.contains("skeleton")) return "Skeleton";
        if (lower.contains("slime")) return "Ooze";
        if (lower.contains("snake")) return "Snake";
        if (lower.contains("spider") || lower.contains("tarantula")) return "Spider";
        if (lower.contains("taunting")) return "Taunting";
        if (lower.contains("tree ent")) return "Tree Ent";
        if (lower.contains("troll")) return "Troll";
        if (lower.contains("wasp")) return "Wasp";
        if (lower.contains("worm")) return "Worm";
        if (lower.contains("zombie")) return "Zombie";
        return normalized;
    }

    /**
     * Update monster state (patrol, aggro, combat, animations).
     */
    public void update(float delta, Vector2 playerPosition) {
        update(delta, playerPosition, null);
    }

    public void update(float delta, Vector2 playerPosition, List<BaseMonster> nearbyMonsters) {
        if (stuckCooldown > 0f) {
            stuckCooldown = Math.max(0f, stuckCooldown - delta);
        }
        if (blockedLogTimer > 0f) {
            blockedLogTimer = Math.max(0f, blockedLogTimer - delta);
        }
        if (pathRetryTimer > 0f) {
            pathRetryTimer = Math.max(0f, pathRetryTimer - delta);
        }
        if (isDead) {
            animations.update(delta, false);
            return;
        }
        if (isStunned()) {
            movement.stop();
            animations.update(delta, false);
            return;
        }

        if (stationary) {
            isAggro = false;
            aggroTarget = null;
            retaliatingAgainstPlayer = false;
            monsterAggroTarget = null;
            companionAggroTarget = null;
            patrolTarget = null;
            clearPlannedPath();
            movement.stop();
            updateStationaryAnimation(delta);
            return;
        }

        // Update attack cooldown
        if (attackCooldownTimer > 0) {
            attackCooldownTimer -= delta;
        }

        float distanceToPlayer = playerPosition == null ? Float.MAX_VALUE : position.dst(playerPosition);
        BaseMonster enemyTarget = findNearestEnemyMonster(nearbyMonsters);

        if (companionAggroTarget != null && companionAggroTarget.isDead()) {
            // The ally fell: drop back to the player as the natural next target.
            companionAggroTarget = null;
            aggroTarget = playerPosition;
        }

        if (isAggro) {
            if (isValidMonsterTarget(monsterAggroTarget)) {
                float distanceToMonster = position.dst(monsterAggroTarget.position);
                if (distanceToMonster <= MONSTER_AGGRO_LEASH_RANGE) {
                    updateMonsterCombat(delta, monsterAggroTarget);
                } else {
                    loseAggroAndReturnHome();
                    updatePatrol(delta);
                }
            } else if (isFightingCompanion()) {
                // Chase and leash against the companion itself, not the player,
                // otherwise the monster drifts toward a distant player mid-fight.
                Vector2 companionPosition = companionAggroTarget.getPosition();
                if (position.dst(companionPosition) <= playerAggroLeashRange()) {
                    updateCombat(delta, companionPosition);
                } else {
                    loseAggroAndReturnHome();
                    updatePatrol(delta);
                }
            } else if (aggroTarget != null && distanceToPlayer <= playerAggroLeashRange()) {
                updateCombat(delta, playerPosition);
            } else {
                loseAggroAndReturnHome();
                updatePatrol(delta);
            }
        } else if (enemyTarget != null) {
            aggroOnMonster(enemyTarget);
            updateMonsterCombat(delta, enemyTarget);
        } else if (aggressive && distanceToPlayer <= MONSTER_AGGRO_RANGE && hasLineOfSight(playerPosition)) {
            // Player in range, aggro!
            if (!isAggro) {
                resetChaseNavigation();
                isAggro = true;
                aggroTarget = playerPosition;
                retaliatingAgainstPlayer = false;
                monsterAggroTarget = null;
                log.info("{} aggroed player at distance {}", name, String.format("%.1f", distanceToPlayer));
            }
            updateCombat(delta, playerPosition);
        } else {
            updatePatrol(delta);
        }

        animations.update(delta, movement.isMoving());
    }

    private BaseMonster findNearestEnemyMonster(List<BaseMonster> monsters) {
        if (!canAttackClanTarget() || monsters == null || monsters.isEmpty()) {
            return null;
        }

        BaseMonster nearest = null;
        float nearestDistance2 = MONSTER_AGGRO_RANGE * MONSTER_AGGRO_RANGE;
        for (BaseMonster other : monsters) {
            if (!isValidMonsterTarget(other)) {
                continue;
            }
            float distance2 = position.dst2(other.position);
            if (distance2 > nearestDistance2) {
                continue;
            }
            if (!hasLineOfSight(other.position)) {
                continue;
            }
            nearest = other;
            nearestDistance2 = distance2;
        }
        return nearest;
    }

    private boolean isValidMonsterTarget(BaseMonster other) {
        return other != null
                && other != this
                && !other.isDead
                && isEnemyOf(other);
    }

    public boolean isEnemyOf(BaseMonster other) {
        return other != null && MonsterClanRelations.areEnemies(clan, other.clan);
    }

    private boolean canAttackClanTarget() {
        return aggressive && !stationary && animations.hasAttackAnimation();
    }

    private void aggroOnMonster(BaseMonster target) {
        if (!canAttackClanTarget() || !isValidMonsterTarget(target)) {
            return;
        }
        if (!isAggro || monsterAggroTarget != target) {
            log.info("{} aggroed {} from clan hostility ({} -> {})", name, target.name, clan, target.clan);
            resetChaseNavigation();
        }
        isAggro = true;
        aggroTarget = null;
        retaliatingAgainstPlayer = false;
        monsterAggroTarget = target;
    }

    /**
     * Update combat behavior (chase and attack player) with collision detection.
     */
    private void updateCombat(float delta, Vector2 playerPosition) {
        float distanceToPlayer = position.dst(playerPosition);

        if (distanceToPlayer <= resolveAttackRange() && hasLineOfSight(playerPosition)) {
            // In attack range, stop and attack
            unreachableTargetTimer = 0f;
            movement.stop();
            movement.faceToward(position, playerPosition);

            if (attackCooldownTimer <= 0 && !animations.isAttacking()) {
                performAttack(playerPosition);
                attackCooldownTimer = MONSTER_ATTACK_COOLDOWN;
            }
        } else {
            // Chase player
            Vector2 moveTarget = nextPathWaypoint(playerPosition);
            if (moveTarget == null) {
                movement.stop();
                unreachableTargetTimer += delta;
                if (unreachableTargetTimer >= UNREACHABLE_TARGET_GIVE_UP_DELAY) {
                    loseAggroAndReturnHome();
                }
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
            float step = MONSTER_SPEED * delta;
            if (!tryMoveWithFallback(ndx, ndy, step, delta, moveTarget)) {
                recoverBlockedChase(playerPosition);
                movement.stop();
                unreachableTargetTimer += delta;
                if (unreachableTargetTimer >= UNREACHABLE_TARGET_GIVE_UP_DELAY) {
                    loseAggroAndReturnHome();
                }
                return;
            }
            unreachableTargetTimer = 0f;
        }
    }

    private void updateMonsterCombat(float delta, BaseMonster target) {
        if (!isValidMonsterTarget(target) || !canAttackClanTarget()) {
            loseAggroAndReturnHome();
            updatePatrol(delta);
            return;
        }

        Vector2 targetPosition = target.position;
        float distanceToTarget = position.dst(targetPosition);

        if (distanceToTarget <= MONSTER_ATTACK_RANGE) {
            movement.stop();
            movement.faceToward(position, targetPosition);

            if (attackCooldownTimer <= 0 && !animations.isAttacking()) {
                performAttack(target);
                attackCooldownTimer = MONSTER_ATTACK_COOLDOWN;
            }
        } else {
            Vector2 moveTarget = nextPathWaypoint(targetPosition);
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
            float step = MONSTER_SPEED * delta;
            if (!tryMoveWithFallback(ndx, ndy, step, delta, moveTarget)) {
                recoverBlockedChase(targetPosition);
                movement.stop();
            }
        }
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
                pauseTimer = random.nextFloat() * (MONSTER_PATROL_PAUSE_MAX - MONSTER_PATROL_PAUSE_MIN) + MONSTER_PATROL_PAUSE_MIN;
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
                float step = Math.min(MONSTER_SPEED * delta, distance);
                if (!tryMoveWithFallback(ndx, ndy, step, delta, moveTarget)) {
                    pickNewPatrolTarget();
                    movement.stop();
                    return;
                }
            }
        } else {
            pickNewPatrolTarget();
        }
    }

    private boolean tryMoveWithFallback(float ndx, float ndy, float step, float delta, Vector2 target) {
        if (fallbackTimer > 0f) {
            if (tryMoveAlong(fallbackDir.x, fallbackDir.y, step)) {
                fallbackTimer = Math.max(0f, fallbackTimer - delta);
                blockedStreak = 0;
                return true;
            }
            log.debug("{} fallback blocked at ({}, {}) dir=({}, {})", name, position.x, position.y, fallbackDir.x, fallbackDir.y);
            fallbackTimer = 0f;
        }

        Vector2 bestDir = null;
        float bestDistance = Float.MAX_VALUE;

        float[][] candidates = {
            {ndx, ndy},
            {ndx, 0f},
            {0f, ndy},
            {-ndx, -ndy},
            {-ndy, ndx},
            {ndy, -ndx}
        };

        for (float[] dir : candidates) {
            if (dir[0] == 0f && dir[1] == 0f) continue;
            if (lastFallbackDir.len2() > 0f) {
                float dot = dir[0] * lastFallbackDir.x + dir[1] * lastFallbackDir.y;
                if (dot < -0.7f && fallbackTimer > 0f) {
                    continue;
                }
            }
            float nextX = position.x + dir[0] * step;
            float nextY = position.y + dir[1] * step;
            if (!canMove(nextX, nextY)) {
                log.debug("{} blocked at ({}, {}) trying dir=({}, {})", name, position.x, position.y, dir[0], dir[1]);
                continue;
            }

            float distance = 0f;
            if (target != null) {
                float tx = target.x - nextX;
                float ty = target.y - nextY;
                distance = (tx * tx) + (ty * ty);
            }

            if (distance < bestDistance) {
                bestDistance = distance;
                if (bestDir == null) {
                    bestDir = new Vector2(dir[0], dir[1]);
                } else {
                    bestDir.set(dir[0], dir[1]);
                }
            }
        }

        if (bestDir != null) {
            // Avoid near-zero fallback directions that keep the monster stuck.
            if (bestDir.len2() < 0.0005f) {
                bestDir = null;
            }
        }

        if (bestDir != null && tryMoveAlong(bestDir.x, bestDir.y, step)) {
            boolean isPrimary = bestDir.x == ndx && bestDir.y == ndy;
            if (!isPrimary) {
                fallbackDir.set(bestDir);
                fallbackTimer = 0.25f;
                lastFallbackDir.set(fallbackDir);
                log.debug("{} using fallback dir=({}, {}) for {}s", name, fallbackDir.x, fallbackDir.y,
                        String.format("%.2f", fallbackTimer));
            }
            blockedStreak = 0;
            return true;
        }

        if (stuckCooldown <= 0f) {
            float[][] escapeDirs = {
                {1f, 0f}, {-1f, 0f}, {0f, 1f}, {0f, -1f},
                {0.7f, 0.7f}, {-0.7f, 0.7f}, {0.7f, -0.7f}, {-0.7f, -0.7f}
            };
            int pick = random.nextInt(escapeDirs.length);
            fallbackDir.set(escapeDirs[pick][0], escapeDirs[pick][1]);
            fallbackTimer = 0.9f;
            stuckCooldown = 0.6f;
            lastFallbackDir.set(fallbackDir);
            log.debug("{} forcing escape dir=({}, {}) for {}s (cooldown {}s)", name, fallbackDir.x, fallbackDir.y,
                    String.format("%.2f", fallbackTimer), String.format("%.2f", stuckCooldown));
            if (tryMoveAlong(fallbackDir.x, fallbackDir.y, step)) {
                blockedStreak = 0;
                return true;
            }
        }

        blockedStreak++;
        if (blockedLogTimer <= 0f) {
            blockedLogTimer = 0.5f;
            log.debug("{} no valid move at ({}, {}) tile=({}, {}) streak={}",
                    name,
                    String.format("%.2f", position.x),
                    String.format("%.2f", position.y),
                    getTileX(),
                    getTileY(),
                    blockedStreak);
        }
        return false;
    }

    /** Rebuild a chase path that repeatedly fails instead of stopping forever. */
    private void recoverBlockedChase(Vector2 target) {
        if (target == null
                || blockedStreak < BLOCKED_STREAK_BEFORE_REPATH
                || pathRetryTimer > 0f) {
            return;
        }

        rebuildPlannedPath(target);
        pathRetryTimer = Math.max(pathRetryTimer, BLOCKED_PATH_RETRY_DELAY);
        blockedStreak = 0;
        fallbackTimer = 0f;
        lastFallbackDir.setZero();
        log.debug("{} recalculated chase path after being blocked", name);
    }

    private boolean tryMoveAlong(float dirX, float dirY, float step) {
        float newX = position.x + dirX * step;
        float newY = position.y + dirY * step;
        if (!canMove(newX, newY)) {
            return false;
        }
        position.x = newX;
        position.y = newY;
        movement.setDirection(dirX, dirY);
        return true;
    }

    private boolean canMove(float newX, float newY) {
        CollisionManager collisionManager = CollisionManager.getInstance();
        return !collisionManager.isInitialized()
                || !collisionManager.hasCollision(newX, newY);
    }

    private boolean hasLineOfSight(Vector2 target) {
        CollisionManager collisionManager = CollisionManager.getInstance();
        if (!collisionManager.isInitialized()) {
            return true;
        }

        float dx = target.x - position.x;
        float dy = target.y - position.y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        if (distance <= 0.001f) {
            return true;
        }

        float step = Math.min(GRID_W, GRID_H) * 0.5f;
        int steps = Math.max(1, (int) Math.ceil(distance / step));
        float stepX = dx / steps;
        float stepY = dy / steps;

        for (int i = 1; i < steps; i++) {
            float checkX = position.x + stepX * i;
            float checkY = position.y + stepY * i;
            if (collisionManager.hasCollision(checkX, checkY)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Pick a new random patrol target within patrol radius.
     */
    private void pickNewPatrolTarget() {
        clearPlannedPath();
        for (int attempt = 0; attempt < 12; attempt++) {
            float angle = random.nextFloat() * 2f * (float) Math.PI;
            float radius = random.nextFloat() * MONSTER_PATROL_RADIUS;
            float targetX = initialPosition.x + (float) Math.cos(angle) * radius;
            float targetY = initialPosition.y + (float) Math.sin(angle) * radius;
            if (canMove(targetX, targetY)) {
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
        if (plannedPathTarget == null
                || plannedPathTarget.dst2(target) > 4f
                || (plannedPathUnavailable && pathRetryTimer <= 0f)) {
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
        if (plannedPathUnavailable) {
            pathRetryTimer = UNAVAILABLE_PATH_RETRY_DELAY;
        }
        for (Pathfinding.GridPoint point : path) {
            plannedPath.add(new Vector2(point.x * GRID_W + GRID_W * 0.5f, point.y * GRID_H + GRID_H * 0.5f));
        }
    }

    private void clearPlannedPath() {
        plannedPath.clear();
        plannedPathTarget = null;
        plannedPathUnavailable = false;
        pathRetryTimer = 0f;
    }

    private void resetChaseNavigation() {
        clearPlannedPath();
        blockedStreak = 0;
        fallbackTimer = 0f;
        stuckCooldown = 0f;
        lastFallbackDir.setZero();
        unreachableTargetTimer = 0f;
    }

    public List<Vector2> getDebugPath() {
        return plannedPath;
    }

    public Vector2 getDebugPathTarget() {
        return plannedPathUnavailable ? null : plannedPathTarget;
    }

    /**
     * Perform attack on player.
     */
    protected void performAttack(Vector2 playerPosition) {
        float targetDistance = playerPosition == null ? Float.MAX_VALUE : position.dst(playerPosition);
        int damage = rollDamage(targetDistance);
        if (damage < 0) return;
        if (isFightingCompanion()) {
            performCompanionAttack(damage);
            return;
        }
        startAttackAnimation();
        if (damageCallback != null) {
            if (lastAttackSpellId > 0) {
                damageCallback.applySpell(this, lastAttackSpellId, damage);
            } else {
                log.info("{} attacks for {} damage!", name, damage);
                damageCallback.applyDamage(this, damage);
            }
        }
    }

    /**
     * Resolves one blow against the player's ally companion through the shared
     * combat engine, reporting the outcome so the owning manager can despawn a
     * dead companion.
     */
    private void performCompanionAttack(int rawDamage) {
        com.perso.T4C.npc.CompanionNPC target = companionAggroTarget;
        startAttackAnimation();
        com.perso.T4C.combat.CombatResult result = com.perso.T4C.combat.CombatResolver.resolve(
                new com.perso.T4C.combat.PhysicalAttackRequest(
                        com.perso.T4C.combat.CombatProfiles.fromMonster(this),
                        com.perso.T4C.combat.CombatProfiles.fromCompanion(target), rawDamage, 0, false), random);
        log.info("{} attacks companion {}: hit={}, damage={}",
                name, target.getName(), result.hit(), result.damage());
        if (result.hit() && companionDamageCallback != null) {
            companionDamageCallback.accept(result.damage());
        }
    }

    protected void performAttack(BaseMonster target) {
        if (target == null || target.isDead || !animations.hasAttackAnimation()) {
            return;
        }

        startAttackAnimation();
        int rawDamage = rollDamage();
        com.perso.T4C.combat.CombatResult result = com.perso.T4C.combat.CombatResolver.resolve(
                new com.perso.T4C.combat.PhysicalAttackRequest(
                        com.perso.T4C.combat.CombatProfiles.fromMonster(this),
                        com.perso.T4C.combat.CombatProfiles.fromMonster(target), rawDamage, 0, false), random);
        log.info("{} attacks {}: hit={}, damage={}", name, target.name, result.hit(), result.damage());
        if (result.hit()) {
            if (target.isStunned()) target.clearStun();
            target.stunFor(result.stunDurationMillis());
            target.applyClanDamage(result.damage(), this);
        }
    }

    /**
     * Roll damage for one attack. If a T4C attack list is set, picks one at random
     * weighted by value2 (rate 0-100) and parses its dice formula. Falls back to
     * hitDamageMin/hitDamageMax when the list is empty or all rates are zero.
     */
    protected int rollDamage() {
        return rollDamage(0f);
    }

    /** Starts the visible attack sequence; humanoid data monsters extend this hook. */
    protected void startAttackAnimation() {
        animations.startAttack();
    }

    /** Clears an active/held attack pose when this monster leaves combat. */
    protected void clearAttackAnimationPose() {
        animations.clearAttackPose();
    }

    private int rollDamage(float targetDistance) {
        if (attacks != null && !attacks.isEmpty()) {
            MonsterDef.Attack chosen = pickAttack(targetDistance);
            if (chosen != null) {
                combatAttack = Math.max(1, chosen.getValue1());
                lastAttackRanged = chosen.getValue5() > 1;
                lastAttackSpellId = chosen.getValue3();
                if (lastAttackSpellId > 0) return rollMonsterSpellDamage(lastAttackSpellId);
                return parseDiceRoll(chosen.getName());
            }
        }
        if (targetDistance > MONSTER_ATTACK_RANGE) return -1;
        lastAttackRanged = false;
        lastAttackSpellId = 0;
        int range = hitDamageMax - hitDamageMin;
        return hitDamageMin + (range > 0 ? random.nextInt(range + 1) : 0);
    }

    private MonsterDef.Attack pickAttack(float targetDistance) {
        List<MonsterDef.Attack> eligible = new ArrayList<>();
        for (MonsterDef.Attack attack : attacks) {
            float range = attack.getValue5() > 1
                    ? attack.getValue5() * Math.max(GRID_W, GRID_H)
                    : MONSTER_ATTACK_RANGE;
            if (targetDistance <= range) eligible.add(attack);
        }
        if (eligible.isEmpty()) return null;
        int totalWeight = 0;
        for (MonsterDef.Attack attack : eligible) totalWeight += Math.max(0, attack.getValue2());
        if (totalWeight <= 0) return eligible.get(eligible.size() - 1);
        int roll = random.nextInt(totalWeight) + 1;
        int cumulative = 0;
        for (MonsterDef.Attack a : eligible) {
            cumulative += Math.max(0, a.getValue2());
            if (roll <= cumulative) return a;
        }
        return eligible.get(eligible.size() - 1);
    }

    private int rollMonsterSpellDamage(int spellId) {
        int intelligence = Math.max(15, combatIntelligence);
        int scale = Math.max(1, intelligence - 14);
        int damage = switch (spellId) {
            case 10091 -> scale * 5 / 6 + 1 + random.nextInt(Math.max(1, scale * 5 / 6));
            case 10120 -> scale * 15 / 32 + 1 + random.nextInt(Math.max(1, scale * 15 / 16));
            default -> Math.max(1, combatLevel + random.nextInt(Math.max(1, combatLevel)));
        };
        log.info("{} casts monster spell {} for {} damage!", name, spellId, damage);
        return damage;
    }

    /** Parses "NdM+K" formulas and rolls a result. Returns 0 on parse failure. */
    private int parseDiceRoll(String formula) {
        if (formula == null || formula.isEmpty()) return 0;
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("(\\d+)d(\\d+)([+-]\\d+)?", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(formula.trim());
        if (!m.find()) return 0;
        int n = Integer.parseInt(m.group(1));
        int d = Integer.parseInt(m.group(2));
        int bonus = m.group(3) != null ? Integer.parseInt(m.group(3)) : 0;
        int total = bonus;
        for (int i = 0; i < n; i++) total += random.nextInt(d) + 1;
        return Math.max(0, total);
    }

    /**
     * Take damage from player.
     */
    public void takeDamage(int damage) {
        if (isDead) return;

        health -= damage;
        log.info("{} takes {} damage! Health: {}/{}", name, damage, health, maxHealth);

        if (health <= 0) {
            health = 0;
            die();
        } else if (soundHit != null && !soundHit.isEmpty()) {
            SoundManager.animateSound(soundHit);
        }
    }

    public void heal(int amount) {
        if (!isDead && amount > 0) health = Math.min(maxHealth, health + amount);
    }

    public void applyCombatDefinition(MonsterDef definition) {
        if (definition == null) return;
        combatLevel = Math.max(1, definition.getLevel());
        combatStrength = Math.max(1, definition.getStr());
        combatEndurance = Math.max(1, definition.getEnd());
        combatAgility = Math.max(1, definition.getAgi());
        combatIntelligence = Math.max(1, definition.getIntel());
        combatAttack = Math.max(1, definition.getAgi() + definition.getLevel());
        combatDodge = Math.max(1, definition.getDodge());
        combatArmorMin = normalizeLegacyArmor(definition.getAcMin());
        combatArmorMax = Math.max(combatArmorMin, normalizeLegacyArmor(definition.getAcMax()));
        combatResists = definition.getResists() == null ? new int[12] : definition.getResists().clone();
    }

    public double rollCombatArmorClass() {
        int spread = combatArmorMax - combatArmorMin;
        return combatArmorMin + (spread > 0 ? random.nextInt(spread + 1) : 0);
    }

    public int getElementResistance(int element) {
        int index = switch (element) {
            case 1 -> 3; // fire
            case 2 -> 1; // earth
            case 3 -> 0; // air
            case 4 -> 2; // water
            case 5 -> 5; // light
            case 6 -> 4; // dark
            default -> -1;
        };
        return index < 0 || index >= combatResists.length ? 0 : Math.max(0, combatResists[index]);
    }

    private float resolveAttackRange() {
        int maximumTiles = 0;
        for (MonsterDef.Attack attack : attacks) {
            if (attack != null) maximumTiles = Math.max(maximumTiles, attack.getValue5());
        }
        return maximumTiles > 0 ? maximumTiles * Math.max(GRID_W, GRID_H) : MONSTER_ATTACK_RANGE;
    }

    public boolean isStunned() {
        return stunnedUntilMs > System.currentTimeMillis();
    }

    public void stunFor(long durationMillis) {
        if (durationMillis > 0L) stunnedUntilMs = Math.max(stunnedUntilMs, System.currentTimeMillis() + durationMillis);
    }

    public void clearStun() {
        stunnedUntilMs = 0L;
    }

    private static int normalizeLegacyArmor(int armor) {
        if (armor >= 0 && armor < 100_000) return armor;
        float decoded = Float.intBitsToFloat(armor);
        return Float.isFinite(decoded) ? Math.max(0, Math.round(decoded)) : 0;
    }

    public void applyPlayerDamage(int damage, Player player, XpCurve xpCurve) {
        if (isDead) {
            return;
        }
        if (player != null) {
            aggroOn(player.getPositionVector());
        }
        boolean wasDead = isDead;
        takeDamage(damage);
        if (player != null && xpPerHit > 0) {
            player.addXp(xpPerHit, xpCurve);
        }
        if (!wasDead && isDead && player != null && xpOnDeath > 0) {
            player.addXp(xpOnDeath, xpCurve);
        }
    }

    public void applyClanDamage(int damage, BaseMonster attacker) {
        if (isDead) {
            return;
        }
        if (attacker != null && canAttackClanTarget() && isEnemyOf(attacker)) {
            aggroOnMonster(attacker);
        }
        takeDamage(damage);
    }

    /**
     * Monster dies.
     */
    protected void die() {
        isDead = true;
        animations.startDeath();
        deathTime = System.currentTimeMillis(); // Record the death time in milliseconds
        if (deathCallback != null) deathCallback.accept(this);
        log.info("{} has been slain!", name);
    }

    public void setDeathCallback(java.util.function.Consumer<BaseMonster> deathCallback) {
        this.deathCallback = deathCallback;
    }

    /**
     * Retaliation after being hit by the player: always aggroes back, even for
     * normally-passive monsters (e.g. town guards attacked via combat mode),
     * matching the original client where any attacked creature fights back.
     */
    public void aggroOn(Vector2 playerPosition) {
        if (isDead || stationary || playerPosition == null) {
            return;
        }
        if (!isAggro || monsterAggroTarget != null) {
            log.info("{} aggroed player from player attack", name);
            resetChaseNavigation();
        }
        isAggro = true;
        aggroTarget = playerPosition;
        retaliatingAgainstPlayer = true;
        monsterAggroTarget = null;
        companionAggroTarget = null;
    }

    /**
     * Retaliation against the player's ally companion, so the companion is a real
     * combatant that can draw and take hits rather than an untouchable damage
     * source. Chasing reuses the player combat path via the companion's live
     * position vector.
     */
    public void aggroOnCompanion(com.perso.T4C.npc.CompanionNPC companion) {
        if (isDead || stationary || companion == null || companion.isDead()) {
            return;
        }
        if (!isAggro || companionAggroTarget != companion) {
            log.info("{} aggroed companion {}", name, companion.getName());
            resetChaseNavigation();
        }
        isAggro = true;
        aggroTarget = companion.getPosition();
        retaliatingAgainstPlayer = true;
        monsterAggroTarget = null;
        companionAggroTarget = companion;
    }

    /** True while this monster is fighting the companion rather than the player. */
    public boolean isFightingCompanion() {
        return companionAggroTarget != null && !companionAggroTarget.isDead();
    }

    public com.perso.T4C.npc.CompanionNPC getCompanionAggroTarget() {
        return companionAggroTarget;
    }

    private float playerAggroLeashRange() {
        return retaliatingAgainstPlayer ? MONSTER_RETALIATION_LEASH_RANGE : MONSTER_AGGRO_LEASH_RANGE;
    }

    private void loseAggroAndReturnHome() {
        isAggro = false;
        aggroTarget = null;
        retaliatingAgainstPlayer = false;
        monsterAggroTarget = null;
        companionAggroTarget = null;
        attackCooldownTimer = 0f;
        clearAttackAnimationPose();
        isPaused = false;
        patrolTarget = initialPosition.cpy();
        clearPlannedPath();
        unreachableTargetTimer = 0f;
        log.info("{} lost aggro and returns to patrol start", name);
    }

    protected void setAggressive(boolean aggressive) {
        this.aggressive = aggressive;
        if (!aggressive) {
            isAggro = false;
            aggroTarget = null;
            retaliatingAgainstPlayer = false;
            monsterAggroTarget = null;
            attackCooldownTimer = 0f;
        }
    }

    public boolean canBeAttackedByPlayer() {
        // Aggressiveness controls spontaneous aggro, not whether the player may
        // deliberately target the creature. Passive monsters retaliate through
        // aggroOn(...) once attacked.
        return !stationary;
    }

    protected boolean shouldAnimateWhileStationary() {
        return false;
    }

    protected float getStationaryAnimationPauseSeconds() {
        return 0f;
    }

    private void updateStationaryAnimation(float delta) {
        if (!shouldAnimateWhileStationary()) {
            animations.update(delta, false);
            return;
        }
        if (stationaryAnimationPauseTimer > 0f) {
            stationaryAnimationPauseTimer = Math.max(0f, stationaryAnimationPauseTimer - delta);
            animations.update(delta, false);
            return;
        }
        boolean loopCompleted = animations.update(delta, true);
        if (loopCompleted) {
            stationaryAnimationPauseTimer = Math.max(0f, getStationaryAnimationPauseSeconds());
        }
    }

    public void setStationary(boolean stationary) {
        this.stationary = stationary;
        if (stationary) {
            isAggro = false;
            aggroTarget = null;
            retaliatingAgainstPlayer = false;
            monsterAggroTarget = null;
            companionAggroTarget = null;
            patrolTarget = null;
            clearPlannedPath();
            movement.stop();
        }
    }

    /**
     * Render the monster.
     */
    public void render(SpriteBatch batch, ShaderProgram outlineShader) {
        float healthPercent = (float) health / (float) maxHealth;
        animations.render(batch, position, movement.getCurrentAngle(), movement.isFlipX(), movement.isMoving() || (stationary && shouldAnimateWhileStationary()), isHovered || selected, outlineShader, healthPercent, getName(), isNameVisible() || selected);
    }

    /**
     * Check if mouse is hovering over monster.
     */
    public boolean isMouseOver(float mouseX, float mouseY) {
        float halfWidth = 40f;
        float heightAbove = 60f;
        float heightBelow = 10f;

        return mouseX >= position.x - halfWidth && mouseX <= position.x + halfWidth && mouseY >= position.y - heightAbove && mouseY <= position.y + heightBelow;
    }

    /**
     * Called when resources are reloaded.
     */
    public void onResourcesReloaded() {
        try {
            animations.refresh();
        } catch (GameException e) {
            log.error("Failed to refresh monster animations for {}", name, e);
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
     * Check if monster should respawn.
     */
    public boolean shouldRespawn() {
        if (!isDead) return false;
        if (!animations.isDeadComplete()) return false;

        // Check if respawn time has passed since death
        long currentTime = System.currentTimeMillis();
        return (currentTime - deathTime) >= respawnTime;
    }

    /** Schedules the next respawn delay; used by density-aware spawn rules. */
    public void setRespawnDelayMillis(long delayMillis) {
        respawnTime = Math.max(0L, delayMillis);
    }

    /**
     * Respawn the monster at its initial position.
     */
    public void respawn() {
        // Reset state
        isDead = false;
        deathTime = 0L;
        isAggro = false;
        aggroTarget = null;
        retaliatingAgainstPlayer = false;
        monsterAggroTarget = null;
        attackCooldownTimer = 0f;
        stunnedUntilMs = 0L;

        // Reset health and mana
        health = maxHealth;
        mana = maxMana;

        // Reset position
        position.set(initialPosition);
        patrolTarget = null;
        clearPlannedPath();
        isPaused = false;
        pauseTimer = 0f;

        // Reset movement
        movement.stop();

        // Reset animations
        animations.reset();

        // Reset name display
        nameDisplayUntil = 0L;

        log.info("{} has respawned at position ({}, {})", name, getTileX(), getTileY());
    }

    /**
     * Show the monster's name for the given duration (milliseconds)
     */
    @Override
    public void showNameFor(long ms) {
        nameDisplayUntil = System.currentTimeMillis() + ms;
    }

    /**
     * Whether the monster name should currently be visible
     */
    @Override
    public boolean isNameVisible() {
        return System.currentTimeMillis() < nameDisplayUntil;
    }

    /**
     * Return the name to display for this monster (used by Nameable).
     */
    @Override
    public String getName() {
        return displayName;
    }

    /** Stable definition identity used by gameplay systems (not the translated display name). */
    public String getCanonicalName() {
        return getName();
    }

    /**
     * Allow subclasses to customize the display name.
     */
    protected void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
