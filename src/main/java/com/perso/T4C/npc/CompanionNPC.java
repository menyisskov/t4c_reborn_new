package com.perso.T4C.npc;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.combat.CombatProfiles;
import com.perso.T4C.combat.CombatResolver;
import com.perso.T4C.combat.CombatResult;
import com.perso.T4C.combat.PhysicalAttackRequest;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.helper.CollisionManager;
import com.perso.T4C.helper.DiceFormula;
import com.perso.T4C.helper.Pathfinding;
import com.perso.T4C.helper.XpCurve;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.monster.BaseMonster;
import com.perso.T4C.monster.MonsterAnimations;
import com.perso.T4C.monster.MonsterDef;
import com.perso.T4C.monster.MonsterRegistry;
import com.perso.T4C.player.Player;
import com.perso.T4C.spell.CompanionCastVfxHook;
import com.perso.T4C.spell.SpellData;
import com.perso.T4C.spell.SpellRegistry;
import com.perso.T4C.ui.SystemMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

import static com.perso.T4C.config.GameConstants.COMPANION_AGGRESSIVE_DETECTION_RANGE;
import static com.perso.T4C.config.GameConstants.COMPANION_AGGRESSIVE_SCAN_INTERVAL;
import static com.perso.T4C.config.GameConstants.COMPANION_COMBAT_LEASH_RANGE;
import static com.perso.T4C.config.GameConstants.COMPANION_FOLLOW_START_DISTANCE;
import static com.perso.T4C.config.GameConstants.COMPANION_FOLLOW_STOP_DISTANCE;
import static com.perso.T4C.config.GameConstants.COMPANION_TRAIL_SAMPLE_INTERVAL;
import static com.perso.T4C.config.GameConstants.COMPANION_TRAIL_SAMPLES;
import static com.perso.T4C.config.GameConstants.DEFAULT_CAST_MENTAL_EXHAUSTION_MS;
import static com.perso.T4C.config.GameConstants.DEFAULT_CAST_PHYSICAL_EXHAUSTION_MS;
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
    /** Tamed beasts retain the complete walk/attack/death animation set of their species. */
    private final MonsterAnimations monsterAnimations;

    /** Monster the companion is currently helping to fight; null while following. */
    private BaseMonster combatTarget;

    /** Behaviour ordered by the player through dialogue. */
    private CompanionMode mode = CompanionMode.SUPPORT;

    /**
     * Supplies the live monster list for AGGRESSIVE scanning. Resolved per frame
     * rather than held, because the whole MonsterManager is rebuilt on map change.
     */
    @Setter
    private Supplier<List<BaseMonster>> monsterSupplier;

    /** Throttles the AGGRESSIVE target scan, which walks the whole monster list. */
    private float aggressiveScanTimer;

    /**
     * Invoked when the player dismisses the companion in conversation. The
     * manager owns despawning, so the NPC only reports the request.
     */
    @Setter
    private Runnable dismissRequestHandler;

    /** Remaining cooldown per spell entry, indexed like {@code def.getSpells()}. */
    private final float[] spellCooldowns;

    /**
     * Casting exhaustion, mirroring the player's: mental gates the next cast,
     * physical roots the caster in place. Both are wall-clock deadlines fed by
     * the spell's own exhaustion formulas, so a companion pays what a player pays.
     */
    private long mentalExhaustionUntilMs;
    private long physicalExhaustionUntilMs;

    // Follow state: the player's recent positions, oldest first.
    private final Deque<Vector2> playerTrail = new ArrayDeque<>();
    private float trailSampleTimer;

    // Path state, mirroring BaseNPC's patrol pathing but against a moving target.
    private final List<Vector2> plannedPath = new ArrayList<>();
    private Vector2 plannedPathTarget;
    private boolean plannedPathUnavailable;

    @Setter
    private boolean dead;

    /** Snapshot of this companion for player-state persistence, or {@code null} once dead. */
    public com.perso.T4C.helper.PlayerStateDto.CompanionState toSaveState() {
        if (isDead()) {
            return null;
        }
        boolean tamed = def.getId().startsWith(TamedCompanionFactory.ID_PREFIX);
        com.perso.T4C.helper.PlayerStateDto.CompanionState saved =
                new com.perso.T4C.helper.PlayerStateDto.CompanionState();
        saved.tamed = tamed;
        saved.speciesName = tamed ? def.getId().substring(TamedCompanionFactory.ID_PREFIX.length()) : def.getId();
        saved.level = getLevel();
        saved.currentHp = getCurrentHp();
        saved.mode = getMode().name();
        return saved;
    }

    public CompanionNPC(CompanionDef def, Player owner, XpCurve xpCurve) throws GameException {
        super(def.getId(), def.getSpriteBase(), buildParts(def));
        this.def = def;
        this.owner = owner;
        this.xpCurve = xpCurve;
        this.monsterAnimations = createMonsterAnimations(def);
        this.spellCooldowns = new float[def.getSpells().size()];
        if (def.getDisplayName() != null && !def.getDisplayName().isEmpty()) {
            setDisplayName(I18n.resolve(def.getDisplayName()));
        }
        // BaseNPC gives every NPC a 1 HP shell; a companion must survive real fights.
        this.level = owner != null ? Math.max(1, owner.getLevel()) : 1;
        this.maxHp = def.resolveMaxHp(this.level);
        this.currentHp = this.maxHp;
    }

    private static MonsterAnimations createMonsterAnimations(CompanionDef companionDef) throws GameException {
        if (companionDef == null || !companionDef.getId().startsWith(TamedCompanionFactory.ID_PREFIX)) return null;
        String species = companionDef.getId().substring(TamedCompanionFactory.ID_PREFIX.length());
        MonsterDef monster = MonsterRegistry.findByName(species);
        if (monster == null) return null;
        return new MonsterAnimations(monster.getWalkPattern(), monster.getAttackPattern(), monster.getDeathPattern(),
                monster.getSoundAttack(), monster.getSoundDeath());
    }

    private void updateVisualAnimations(float delta, boolean moving) {
        if (monsterAnimations != null) monsterAnimations.update(delta, moving);
        else animations.update(delta, moving);
    }

    private void startAttackAnimation() {
        if (monsterAnimations != null) monsterAnimations.startAttack();
        else animations.startAttack(movement.getCurrentAngle());
    }

    @Override
    public void render(SpriteBatch batch, ShaderProgram outlineShader) {
        if (monsterAnimations == null) {
            super.render(batch, outlineShader);
            return;
        }
        float healthPercent = maxHp <= 0 ? 0f : Math.max(0f, Math.min(1f, currentHp / (float) maxHp));
        monsterAnimations.render(batch, position, movement.getCurrentAngle(), movement.isFlipX(),
                movement.isMoving(), isHovered, outlineShader, healthPercent, getName(), isNameVisible());
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

    /**
     * Targets the monster the player just attacked, ignoring redundant retargets.
     * A passive companion refuses the order and keeps following.
     */
    public void setCombatTarget(BaseMonster monster) {
        if (mode == CompanionMode.PASSIVE
                || monster == null || monster.isDead() || combatTarget == monster) {
            return;
        }
        combatTarget = monster;
        clearPlannedPath();
    }

    /** Switches behaviour, dropping the current fight when going passive. */
    public void setMode(CompanionMode newMode) {
        if (newMode == null || newMode == mode) {
            return;
        }
        mode = newMode;
        if (mode == CompanionMode.PASSIVE) {
            combatTarget = null;
            clearPlannedPath();
        }
        log.info("Companion {} switched to {} mode", getName(), mode);
    }

    public void setCurrentHp(int hp) {
        currentHp = Math.max(1, Math.min(maxHp, hp <= 0 ? maxHp : hp));
    }

    /**
     * Picks the closest attackable monster around the player, so an aggressive
     * companion opens fights on its own. Scanning is throttled and anchored on
     * the player rather than the companion, which keeps it from wandering off.
     */
    private void acquireAggressiveTarget(float delta, Vector2 playerPosition) {
        aggressiveScanTimer -= delta;
        if (aggressiveScanTimer > 0f || monsterSupplier == null) {
            return;
        }
        aggressiveScanTimer = COMPANION_AGGRESSIVE_SCAN_INTERVAL;
        List<BaseMonster> monsters = monsterSupplier.get();
        if (monsters == null) {
            return;
        }
        BaseMonster closest = null;
        float closestDistance = Float.MAX_VALUE;
        for (BaseMonster monster : monsters) {
            if (monster == null || monster.isDead() || !monster.canBeAttackedByPlayer()) {
                continue;
            }
            float distance = playerPosition.dst(monster.getPosition());
            if (distance <= COMPANION_AGGRESSIVE_DETECTION_RANGE && distance < closestDistance) {
                closest = monster;
                closestDistance = distance;
            }
        }
        if (closest != null) {
            combatTarget = closest;
            clearPlannedPath();
        }
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
            if (monsterAnimations != null) monsterAnimations.clearAttackPose();
            else animations.clearAttackPose();
            log.info("Companion {} was killed", getName());
            return true;
        }
        return false;
    }

    @Override
    public void update(float delta, Vector2 playerPosition) {
        if (dead || playerPosition == null) {
            updateVisualAnimations(delta, false);
            return;
        }

        if (attackCooldownTimer > 0) {
            attackCooldownTimer -= delta;
        }
        for (int i = 0; i < spellCooldowns.length; i++) {
            if (spellCooldowns[i] > 0) spellCooldowns[i] -= delta;
        }
        // Stand still and face the player while taking orders, otherwise the
        // follow logic would walk it out of range and end its own conversation.
        if (isInteracting) {
            movement.stop();
            movement.faceToward(position, playerPosition);
            clearPlannedPath();
            updateVisualAnimations(delta, false);
            return;
        }

        recordPlayerTrail(delta, playerPosition);

        // Healing outranks fighting: a dead companion helps nobody. A passive
        // companion still heals -- "do nothing" means "do not fight".
        if (castFirstReadySupportSpell()) {
            updateVisualAnimations(delta, movement.isMoving());
            return;
        }

        if (mode == CompanionMode.PASSIVE) {
            combatTarget = null;
            updateFollow(delta, playerPosition);
            updateVisualAnimations(delta, movement.isMoving());
            return;
        }
        if (mode == CompanionMode.AGGRESSIVE && !isValidTarget(combatTarget, playerPosition)) {
            acquireAggressiveTarget(delta, playerPosition);
        }

        if (isValidTarget(combatTarget, playerPosition)) {
            updateCompanionCombat(delta);
        } else {
            combatTarget = null;
            updateFollow(delta, playerPosition);
        }
        updateVisualAnimations(delta, movement.isMoving());
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
        startAttackAnimation();
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
        if (isMentallyExhausted()) {
            return false;
        }
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
        // No attack pose while casting: the puppet sheets only ship melee ("A")
        // and bow ("B") sequences, so reusing one makes a spell look like a
        // sword swing. The player casts the same way -- VFX only. Clearing is
        // required because a finished melee swing holds its last frame until
        // the companion moves again.
        animations.clearAttackPose();
        applyCastExhaustion(spell);

        if (entry.getTrigger() == CompanionSpellTrigger.HEAL_SELF) {
            currentHp = Math.min(maxHp, currentHp + amount);
            if (spell != null) {
                CompanionCastVfxHook.playSelfHeal(spell, position.x, position.y);
            }
            log.info("Companion {} heals itself for {} ({}/{})", getName(), amount, currentHp, maxHp);
            return;
        }

        if (owner != null) {
            // The heal now travels to the owner, so it must land with the
            // projectile rather than the instant the cast starts.
            if (spell != null && CompanionCastVfxHook.playHeal(
                    spell, owner, position, () -> applyOwnerHeal(amount))) {
                return;
            }
            applyOwnerHeal(amount);
        }
    }

    /** Restores owner health and shows the floating number; runs on projectile impact. */
    private void applyOwnerHeal(int amount) {
        if (owner == null) {
            return;
        }
        int before = owner.getCurrentHp();
        owner.applyHeal(amount, amount);
        int healed = owner.getCurrentHp() - before;
        // applyHeal does not notify, so the floating heal number needs this.
        if (healed > 0) owner.notifyHealing(healed);
        log.info("Companion {} heals its owner for {}", getName(), healed);
    }

    /**
     * Casts the highest-priority ready offensive spell that reaches the current
     * target. Returns false so the caller falls back to melee.
     */
    private boolean castFirstReadyAttackSpell(float distanceToTarget) {
        if (isMentallyExhausted()) {
            return false;
        }
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
        // Casting shows no melee pose, and drops any held one; see castSupportSpell.
        animations.clearAttackPose();
        applyCastExhaustion(spell);
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

    /** True while a previous cast still forbids casting again. */
    private boolean isMentallyExhausted() {
        return mentalExhaustionUntilMs > System.currentTimeMillis();
    }

    /** True while a cast still roots the companion in place. */
    private boolean isPhysicallyExhausted() {
        return physicalExhaustionUntilMs > System.currentTimeMillis();
    }

    /**
     * Charges the spell's own exhaustion, evaluated against the companion's level
     * exactly as {@link com.perso.T4C.spell.SpellCastingService} does for the player.
     * A missing spell still costs a default beat, so an unknown key cannot turn
     * into a free unlimited-cast loop.
     */
    private void applyCastExhaustion(SpellData spell) {
        long mental = DEFAULT_CAST_MENTAL_EXHAUSTION_MS;
        long physical = DEFAULT_CAST_PHYSICAL_EXHAUSTION_MS;
        if (spell != null) {
            DiceFormula.Context context = new DiceFormula.Context(
                    0, 0, 0, 0, 0, 0, 0, Math.max(1, level));
            mental = evaluateExhaustionMillis(spell.getMentalExhaustion(), context, mental);
            physical = evaluateExhaustionMillis(spell.getPhysicalExhaustion(), context, physical);
        }
        long now = System.currentTimeMillis();
        mentalExhaustionUntilMs = Math.max(mentalExhaustionUntilMs, now + mental);
        physicalExhaustionUntilMs = Math.max(physicalExhaustionUntilMs, now + physical);
        if (physical > 0L) {
            movement.stop();
            clearPlannedPath();
        }
    }

    private static long evaluateExhaustionMillis(String formula, DiceFormula.Context context,
                                                 long fallback) {
        if (formula == null || formula.isBlank()) {
            return fallback;
        }
        return Math.max(0L, DiceFormula.of(formula).evaluate(context));
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
        // Rooted mid-cast, exactly like the player: you cannot walk and cast.
        if (isPhysicallyExhausted()) {
            movement.stop();
            return;
        }
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
        float ownerSpeed = com.perso.T4C.config.GameConstants.PLAYER_SPEED
                * (owner == null ? 1f : owner.getEffectiveSpeedMultiplier());
        float step = Math.min(ownerSpeed * delta, distance);
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

    /** Greets the player with its current stance and the orders it accepts. */
    @Override
    protected void onInteractStart(Player player) {
        showDialog(I18n.message("message.companion_greeting",
                I18n.message(modeMessageKey(mode))), 0L);
    }

    @Override
    protected List<String> getDialogKeywords() {
        List<String> keywords = new ArrayList<>(super.getDialogKeywords());
        for (CompanionMode candidate : CompanionMode.values()) {
            keywords.add(I18n.message(modeKeywordKey(candidate)));
        }
        keywords.add(I18n.message("companion.dismiss.keyword"));
        return keywords;
    }

    @Override
    protected boolean onDialogKeywordClick(String keyword, Player player) {
        if (I18n.message("companion.dismiss.keyword").equalsIgnoreCase(keyword)) {
            // Say goodbye through the shared feed rather than the head bubble:
            // the bubble would disappear with the companion on the same frame.
            SystemMessage.showShared(I18n.message("companion.dismiss.ack"));
            if (dismissRequestHandler != null) {
                dismissRequestHandler.run();
            }
            return true;
        }
        for (CompanionMode candidate : CompanionMode.values()) {
            if (I18n.message(modeKeywordKey(candidate)).equalsIgnoreCase(keyword)) {
                setMode(candidate);
                showDialog(I18n.message(modeAcknowledgeKey(candidate)), 0L);
                return true;
            }
        }
        return false;
    }

    private static String modeKeywordKey(CompanionMode mode) {
        return "companion.mode." + mode.name().toLowerCase(java.util.Locale.ROOT) + ".keyword";
    }

    private static String modeMessageKey(CompanionMode mode) {
        return "companion.mode." + mode.name().toLowerCase(java.util.Locale.ROOT) + ".current";
    }

    private static String modeAcknowledgeKey(CompanionMode mode) {
        return "companion.mode." + mode.name().toLowerCase(java.util.Locale.ROOT) + ".ack";
    }
}
