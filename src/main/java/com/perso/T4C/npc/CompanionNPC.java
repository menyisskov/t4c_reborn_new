package com.perso.T4C.npc;

import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.combat.CombatProfiles;
import com.perso.T4C.combat.CombatResolver;
import com.perso.T4C.combat.CombatResult;
import com.perso.T4C.combat.PhysicalAttackRequest;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.helper.CollisionManager;
import com.perso.T4C.helper.Pathfinding;
import com.perso.T4C.helper.XpCurve;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.monster.BaseMonster;
import com.perso.T4C.player.Player;
import com.perso.T4C.spell.CompanionCastVfxHook;
import com.perso.T4C.spell.SpellData;
import com.perso.T4C.spell.SpellRegistry;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static com.perso.T4C.config.GameConstants.COMPANION_ATTACK_COOLDOWN;
import static com.perso.T4C.config.GameConstants.COMPANION_BASE_HP;
import static com.perso.T4C.config.GameConstants.COMPANION_COMBAT_LEASH_RANGE;
import static com.perso.T4C.config.GameConstants.COMPANION_DAMAGE_MAX;
import static com.perso.T4C.config.GameConstants.COMPANION_DAMAGE_MIN;
import static com.perso.T4C.config.GameConstants.COMPANION_DAMAGE_PER_LEVEL;
import static com.perso.T4C.config.GameConstants.COMPANION_FOLLOW_START_DISTANCE;
import static com.perso.T4C.config.GameConstants.COMPANION_FOLLOW_STOP_DISTANCE;
import static com.perso.T4C.config.GameConstants.COMPANION_HP_PER_PLAYER_LEVEL;
import static com.perso.T4C.config.GameConstants.COMPANION_SPEED;
import static com.perso.T4C.config.GameConstants.COMPANION_TRAIL_SAMPLE_INTERVAL;
import static com.perso.T4C.config.GameConstants.COMPANION_TRAIL_SAMPLES;
import static com.perso.T4C.config.GameConstants.ENTITY_COLLISION_CLEARANCE_TILES;
import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;
import static com.perso.T4C.config.GameConstants.MONSTER_ATTACK_RANGE;

/**
 * Ally NPC summoned to fight alongside the player.
 *
 * <p>It walks the player's own recent trail rather than heading straight at
 * them, which keeps it visibly behind the player, off their tile, and along
 * paths the player actually took. When the player engages a monster the
 * companion joins in on the same target until it dies or the fight drifts too
 * far from the player.
 */
@Slf4j
@Getter
public class CompanionNPC extends BaseNPC {
    private final CompanionDef def;
    private final Player owner;
    private final XpCurve xpCurve;

    /** Monster the companion is currently helping to fight; null while following. */
    private BaseMonster combatTarget;

    /** Remaining cooldown per spell entry, indexed like {@code def.getSpells()}. */
    private final float[] spellCooldowns;

    // Follow state: the player's recent positions, oldest first.
    private final Deque<Vector2> playerTrail = new ArrayDeque<>();
    private float trailSampleTimer;

    // Path state, mirroring BaseNPC's patrol pathing but against a moving target.
    private final List<Vector2> plannedPath = new ArrayList<>();
    private Vector2 plannedPathTarget;
    private boolean plannedPathUnavailable;

    @Setter
    private boolean dead;

    public CompanionNPC(CompanionDef def, Player owner, XpCurve xpCurve) throws GameException {
        super(def.getId(), def.getSpriteBase(), buildParts(def));
        this.def = def;
        this.owner = owner;
        this.xpCurve = xpCurve;
        this.spellCooldowns = new float[def.getSpells().size()];
        if (def.getDisplayName() != null && !def.getDisplayName().isEmpty()) {
            setDisplayName(I18n.resolve(def.getDisplayName()));
        }
        // BaseNPC gives every NPC a 1 HP shell; a companion must survive real fights.
        this.level = owner != null ? Math.max(1, owner.getLevel()) : 1;
        this.maxHp = def.resolveMaxHp(this.level);
        this.currentHp = this.maxHp;
    }

    /** Flattens the definition's parts into the (BodyPart, spriteBase, ...) form BaseNPC expects. */
    private static Object[] buildParts(CompanionDef def) {
        List<Object> parts = new ArrayList<>();
        for (CompanionDef.Part part : def.getParts()) {
            if (part != null && part.getBodyPart() != null) {
                parts.add(part.getBodyPart());
                parts.add(part.getSpriteBase());
            }
        }
        return parts.toArray();
    }

    /** Targets the monster the player just attacked, ignoring redundant retargets. */
    public void setCombatTarget(BaseMonster monster) {
        if (monster == null || monster.isDead() || combatTarget == monster) {
            return;
        }
        combatTarget = monster;
        clearPlannedPath();
    }

    /**
     * Applies monster damage to the companion. Returns true once the blow kills
     * it, letting the caller despawn it.
     */
    public boolean takeDamage(int damage) {
        if (dead || damage <= 0) {
            return false;
        }
        currentHp = Math.max(0, currentHp - damage);
        if (currentHp == 0) {
            dead = true;
            combatTarget = null;
            movement.stop();
            animations.clearAttackPose();
            log.info("Companion {} was killed", getName());
            return true;
        }
        return false;
    }

    @Override
    public void update(float delta, Vector2 playerPosition) {
        if (dead || playerPosition == null) {
            animations.update(delta, false);
            return;
        }

        if (attackCooldownTimer > 0) {
            attackCooldownTimer -= delta;
        }
        for (int i = 0; i < spellCooldowns.length; i++) {
            if (spellCooldowns[i] > 0) spellCooldowns[i] -= delta;
        }
        recordPlayerTrail(delta, playerPosition);

        // Healing outranks fighting: a dead companion helps nobody.
        if (castFirstReadySupportSpell()) {
            animations.update(delta, movement.isMoving());
            return;
        }

        if (isValidTarget(combatTarget, playerPosition)) {
            updateCompanionCombat(delta);
        } else {
            combatTarget = null;
            updateFollow(delta, playerPosition);
        }
        animations.update(delta, movement.isMoving());
    }

    /**
     * A target stays valid while alive, attackable, and fought near the player,
     * so the companion never chases a fleeing monster across the map.
     */
    private boolean isValidTarget(BaseMonster monster, Vector2 playerPosition) {
        return monster != null
                && !monster.isDead()
                && monster.canBeAttackedByPlayer()
                && playerPosition.dst(monster.getPosition()) <= COMPANION_COMBAT_LEASH_RANGE;
    }

    /** Samples the player position periodically to build the trail to walk along. */
    private void recordPlayerTrail(float delta, Vector2 playerPosition) {
        trailSampleTimer -= delta;
        if (trailSampleTimer > 0f) {
            return;
        }
        trailSampleTimer = COMPANION_TRAIL_SAMPLE_INTERVAL;
        if (playerTrail.isEmpty() || playerTrail.peekLast().dst2(playerPosition) > 1f) {
            playerTrail.addLast(new Vector2(playerPosition));
            while (playerTrail.size() > COMPANION_TRAIL_SAMPLES) {
                playerTrail.removeFirst();
            }
        }
    }

    private void updateFollow(float delta, Vector2 playerPosition) {
        float distanceToPlayer = position.dst(playerPosition);
        if (distanceToPlayer <= COMPANION_FOLLOW_STOP_DISTANCE) {
            // Close enough: hold position and just turn toward the player.
            movement.stop();
            movement.faceToward(position, playerPosition);
            clearPlannedPath();
            playerTrail.clear();
            return;
        }
        if (distanceToPlayer < COMPANION_FOLLOW_START_DISTANCE && !movement.isMoving()) {
            // Inside the hysteresis band and already stopped: stay put.
            movement.faceToward(position, playerPosition);
            return;
        }

        Vector2 followTarget = playerTrail.isEmpty() ? playerPosition : playerTrail.peekFirst();
        // Drop trail points already reached so the companion advances along it.
        while (!playerTrail.isEmpty() && position.dst2(playerTrail.peekFirst()) < GRID_W * GRID_W) {
            playerTrail.removeFirst();
            followTarget = playerTrail.isEmpty() ? playerPosition : playerTrail.peekFirst();
        }
        stepToward(delta, followTarget);
    }

    private void updateCompanionCombat(float delta) {
        Vector2 targetPosition = combatTarget.getPosition();
        float distanceToTarget = position.dst(targetPosition);

        // A ready offensive spell fires from range, so the mage keeps its distance
        // instead of closing in for melee it is bad at.
        if (castFirstReadyAttackSpell(distanceToTarget)) {
            movement.stop();
            movement.faceToward(position, targetPosition);
            clearPlannedPath();
            return;
        }

        if (distanceToTarget <= MONSTER_ATTACK_RANGE) {
            movement.stop();
            movement.faceToward(position, targetPosition);
            clearPlannedPath();
            if (attackCooldownTimer <= 0) {
                performCompanionAttack(combatTarget);
                attackCooldownTimer = def.getAttackCooldown();
            }
            return;
        }
        stepToward(delta, targetPosition);
    }

    /**
     * Resolves one companion blow through the shared combat engine. Damage is
     * credited to the owner so kills still grant them XP and loot.
     */
    private void performCompanionAttack(BaseMonster monster) {
        animations.startAttack(movement.getCurrentAngle());
        int levelBonus = Math.round(def.getDamagePerLevel() * (level - 1));
        int low = Math.min(def.getDamageMin(), def.getDamageMax());
        int high = Math.max(def.getDamageMin(), def.getDamageMax());
        int rawDamage = (low >= high ? low
                : ThreadLocalRandom.current().nextInt(low, high + 1)) + levelBonus;
        CombatResult result = CombatResolver.resolve(
                new PhysicalAttackRequest(
                        CombatProfiles.fromCompanion(this),
                        CombatProfiles.fromMonster(monster), rawDamage, 0, false),
                ThreadLocalRandom.current());
        // Striking a monster draws its attention onto the companion, hit or miss.
        monster.aggroOnCompanion(this);
        if (!result.hit() || monster.isDead()) {
            log.info("Companion {} misses {}", getName(), monster.getName());
            return;
        }
        if (monster.isStunned()) monster.clearStun();
        monster.stunFor(result.stunDurationMillis());
        if (result.damage() > 0) {
            monster.applyPlayerDamage(result.damage(), owner, xpCurve);
            log.info("Companion {} hits {} for {} damage", getName(), monster.getName(), result.damage());
        }
    }

    /**
     * Casts the best ready healing spell, if any is warranted. Entries are
     * scanned by descending priority so a definition can rank self-preservation
     * against keeping the owner alive.
     */
    private boolean castFirstReadySupportSpell() {
        CompanionDef.SpellEntry best = null;
        int bestIndex = -1;
        for (int i = 0; i < def.getSpells().size(); i++) {
            CompanionDef.SpellEntry entry = def.getSpells().get(i);
            if (spellCooldowns[i] > 0 || !supportTriggerHolds(entry)) {
                continue;
            }
            if (best == null || entry.getPriority() > best.getPriority()) {
                best = entry;
                bestIndex = i;
            }
        }
        if (best == null) {
            return false;
        }
        castSupportSpell(best);
        spellCooldowns[bestIndex] = best.getCooldownSeconds();
        return true;
    }

    private boolean supportTriggerHolds(CompanionDef.SpellEntry entry) {
        return switch (entry.getTrigger()) {
            case HEAL_SELF -> maxHp > 0 && currentHp < maxHp
                    && (float) currentHp / maxHp <= entry.getHealthThreshold();
            case HEAL_OWNER -> owner != null && owner.getMaxHp() > 0
                    && owner.getCurrentHp() < owner.getMaxHp()
                    && (float) owner.getCurrentHp() / owner.getMaxHp() <= entry.getHealthThreshold();
            case ATTACK -> false;
        };
    }

    private void castSupportSpell(CompanionDef.SpellEntry entry) {
        SpellData spell = SpellRegistry.findByName(entry.getSpellKey());
        int amount = entry.rollAmount(ThreadLocalRandom.current(), level);
        animations.startAttack(movement.getCurrentAngle());

        if (entry.getTrigger() == CompanionSpellTrigger.HEAL_SELF) {
            currentHp = Math.min(maxHp, currentHp + amount);
            if (spell != null) {
                CompanionCastVfxHook.playHeal(spell, position.x, position.y);
            }
            log.info("Companion {} heals itself for {} ({}/{})", getName(), amount, currentHp, maxHp);
            return;
        }

        if (owner != null) {
            int before = owner.getCurrentHp();
            owner.applyHeal(amount, amount);
            int healed = owner.getCurrentHp() - before;
            // applyHeal does not notify, so the floating heal number needs this.
            if (healed > 0) owner.notifyHealing(healed);
            if (spell != null) {
                Vector2 ownerPosition = owner.getPositionVector();
                CompanionCastVfxHook.playHeal(spell, ownerPosition.x, ownerPosition.y);
            }
            log.info("Companion {} heals its owner for {}", getName(), healed);
        }
    }

    /**
     * Casts the highest-priority ready offensive spell that reaches the current
     * target. Returns false so the caller falls back to melee.
     */
    private boolean castFirstReadyAttackSpell(float distanceToTarget) {
        CompanionDef.SpellEntry best = null;
        int bestIndex = -1;
        for (int i = 0; i < def.getSpells().size(); i++) {
            CompanionDef.SpellEntry entry = def.getSpells().get(i);
            if (entry.getTrigger() != CompanionSpellTrigger.ATTACK || spellCooldowns[i] > 0) {
                continue;
            }
            if (distanceToTarget > entry.getRangeTiles() * GRID_W) {
                continue;
            }
            if (best == null || entry.getPriority() > best.getPriority()) {
                best = entry;
                bestIndex = i;
            }
        }
        if (best == null) {
            return false;
        }
        castAttackSpell(best, combatTarget);
        spellCooldowns[bestIndex] = best.getCooldownSeconds();
        return true;
    }

    private void castAttackSpell(CompanionDef.SpellEntry entry, BaseMonster monster) {
        SpellData spell = SpellRegistry.findByName(entry.getSpellKey());
        int damage = entry.rollAmount(ThreadLocalRandom.current(), level);
        animations.startAttack(movement.getCurrentAngle());
        // Casting draws aggro just like a melee blow does.
        monster.aggroOnCompanion(this);

        Runnable onImpact = () -> applySpellDamage(monster, damage);
        // With no renderer registered the projectile never lands, so apply now.
        if (spell == null || !CompanionCastVfxHook.playAttack(spell, monster, position, onImpact)) {
            onImpact.run();
        }
        log.info("Companion {} casts {} at {} for {}", getName(), entry.getSpellKey(),
                monster.getName(), damage);
    }

    /** Applies spell damage, credited to the owner so kills still grant XP and loot. */
    private void applySpellDamage(BaseMonster monster, int damage) {
        if (dead || monster.isDead() || damage <= 0) {
            return;
        }
        monster.applyPlayerDamage(damage, owner, xpCurve);
    }

    /** Walks one frame toward the target along the planned path, stopping at walls. */
    private void stepToward(float delta, Vector2 target) {
        Vector2 moveTarget = nextPathWaypoint(target);
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
        float step = Math.min(def.getSpeed() * delta, distance);
        float newX = position.x + ndx * step;
        float newY = position.y + ndy * step;

        CollisionManager collisionManager = CollisionManager.getInstance();
        if (collisionManager.isInitialized()
                && collisionManager.hasCollisionNear(newX, newY, ENTITY_COLLISION_CLEARANCE_TILES)) {
            clearPlannedPath();
            movement.stop();
            return;
        }
        position.x = newX;
        position.y = newY;
        movement.setDirection(ndx, ndy);
    }

    private Vector2 nextPathWaypoint(Vector2 target) {
        if (target == null) {
            clearPlannedPath();
            return null;
        }
        // Only re-run A* once the goal drifted meaningfully, as BaseNPC does.
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
        int targetTileX = (int) (target.x / GRID_W);
        int targetTileY = (int) (target.y / GRID_H);
        List<Pathfinding.GridPoint> path = Pathfinding.findPath(
                getTileX(),
                getTileY(),
                targetTileX,
                targetTileY,
                collisionManager.getCollisionWidth(),
                collisionManager.getCollisionHeight(),
                collisionManager,
                ENTITY_COLLISION_CLEARANCE_TILES
        );
        plannedPathUnavailable = path.isEmpty()
                && (getTileX() != targetTileX || getTileY() != targetTileY);
        for (Pathfinding.GridPoint point : path) {
            plannedPath.add(new Vector2(point.x * GRID_W + GRID_W * 0.5f, point.y * GRID_H + GRID_H * 0.5f));
        }
    }

    private void clearPlannedPath() {
        plannedPath.clear();
        plannedPathTarget = null;
        plannedPathUnavailable = false;
    }

    /** Teleports the companion beside the player, used after a map transition. */
    public void warpTo(float worldX, float worldY) {
        setSpawnPosition(worldX, worldY);
        combatTarget = null;
        playerTrail.clear();
        clearPlannedPath();
    }

    @Override
    public List<Vector2> getDebugPath() {
        return plannedPath;
    }

    @Override
    public Vector2 getDebugPathTarget() {
        return plannedPathUnavailable ? null : plannedPathTarget;
    }

    /** A companion is an ally: it never turns on the player when clicked. */
    @Override
    public void provoke() {
        // Intentionally inert.
    }
}
