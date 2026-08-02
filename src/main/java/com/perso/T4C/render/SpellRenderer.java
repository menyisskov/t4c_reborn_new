package com.perso.T4C.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.SpriteLoader;
import com.perso.T4C.monster.BaseMonster;
import com.perso.T4C.audio.SoundManager;
import com.perso.T4C.npc.BaseNPC;
import com.perso.T4C.player.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
/**
 * Class representing SpellRenderer.
 */

public class SpellRenderer {
    private final SpriteLoader spriteLoader;
    private final Map<String, List<ImpactFrame>> impactFramesCache = new HashMap<>();
    private final Map<String, List<ProjectileFrame>> projectileFramesCache = new HashMap<>();
    private final Map<String, SpriteLoader.Sprite> spriteMetaCache = new HashMap<>();
    private final List<SpellImpact> activeImpacts = new ArrayList<>();
    private final List<SpellProjectile> activeProjectiles = new ArrayList<>();
    private final List<ChannelEffect> activeChannels = new ArrayList<>();
    private final float impactXOffset = 0f;
    private final float impactYOffset = 0f;
    private final float projectileSpeed = 500f;
    private ShaderProgram maskShader;
    private boolean maskShaderLoaded = false;

    public SpellRenderer(SpriteLoader spriteLoader) {
        this.spriteLoader = spriteLoader;
    }

    public void render(SpriteBatch batch) {
        renderProjectiles(batch);
        renderImpacts(batch);
        renderChannels(batch);
    }

    /** Starts a looping spell effect that follows a moving caster. */
    public ChannelHandle startChannel(String effect, Supplier<Vector2> positionSupplier) {
        if (effect == null || effect.isBlank() || positionSupplier == null) return null;
        List<ImpactFrame> frames = getImpactFrames(effect);
        if (frames.isEmpty()) return null;
        ChannelHandle handle = new ChannelHandle();
        activeChannels.add(new ChannelEffect(handle, frames, positionSupplier));
        return handle;
    }

    public void stopChannel(ChannelHandle handle) {
        if (handle != null) activeChannels.removeIf(channel -> channel.handle == handle);
    }

    public void playLaunchSound(String sound) {
        if (sound == null || sound.isEmpty()) {
            return;
        }
        SoundManager.animateSound(sound);
    }

    public void playImpactSound(String sound) {
        if (sound == null || sound.isEmpty()) {
            return;
        }
        SoundManager.animateSound(sound);
    }

    public void triggerImpactSpell(String impactSpell, Player player) {
        if (impactSpell == null || impactSpell.isEmpty() || player == null) {
            return;
        }
        List<ImpactFrame> frames = getImpactFrames(impactSpell);
        if (frames.isEmpty()) {
            return;
        }
        float x = player.getCoordinates().getX();
        float y = player.getCoordinates().getY();
        activeImpacts.add(new SpellImpact(impactSpell, frames, x + impactXOffset, y + impactYOffset));
    }

    public void triggerImpactSpell(String impactSpell, float worldX, float worldY) {
        if (impactSpell == null || impactSpell.isEmpty()) {
            return;
        }
        List<ImpactFrame> frames = getImpactFrames(impactSpell);
        if (frames.isEmpty()) {
            return;
        }
        activeImpacts.add(new SpellImpact(impactSpell, frames, worldX + impactXOffset, worldY + impactYOffset));
    }

    public void triggerImpactSpell(String impactSpell, float worldX, float worldY, String soundImpact) {
        playImpactSound(soundImpact);
        triggerImpactSpell(impactSpell, worldX, worldY);
    }

    public boolean launchProjectile(String projectileSpell, BaseMonster target, float startX, float startY, boolean flipX, Runnable onImpact) {
        if (projectileSpell == null || projectileSpell.isEmpty() || target == null || onImpact == null) {
            return false;
        }
        List<ProjectileFrame> frames = getProjectileFrames(projectileSpell);
        if (frames.isEmpty()) {
            return false;
        }
        activeProjectiles.add(new SpellProjectile(projectileSpell, frames, target, startX, startY, flipX, onImpact));
        return true;
    }

    /** Launches a player spell whose target is a data-driven NPC. */
    public boolean launchProjectile(String projectileSpell, BaseNPC target, float startX, float startY,
                                    boolean flipX, Runnable onImpact) {
        if (projectileSpell == null || projectileSpell.isEmpty() || target == null || onImpact == null) {
            return false;
        }
        List<ProjectileFrame> frames = getProjectileFrames(projectileSpell);
        if (frames.isEmpty()) {
            return false;
        }
        activeProjectiles.add(new SpellProjectile(projectileSpell, frames, target,
                target::getPosition, startX, startY, flipX, onImpact));
        return true;
    }

    /** Launches a projectile whose target is the player (monster spell path). */
    public boolean launchProjectile(String projectileSpell, Player target, float startX, float startY,
                                    boolean flipX, Runnable onImpact) {
        if (projectileSpell == null || projectileSpell.isEmpty() || target == null || onImpact == null) return false;
        List<ProjectileFrame> frames = getProjectileFrames(projectileSpell);
        if (frames.isEmpty()) return false;
        activeProjectiles.add(new SpellProjectile(projectileSpell, frames, target,
                target::getPositionVector, startX, startY, flipX, onImpact));
        return true;
    }

    public boolean launchProjectileIfAbsent(String projectileSpell, BaseMonster target, float startX, float startY, boolean flipX, Runnable onImpact) {
        if (hasActiveProjectile(projectileSpell, target)) {
            return false;
        }
        return launchProjectile(projectileSpell, target, startX, startY, flipX, onImpact);
    }

    public boolean hasActiveProjectile(String projectileSpell, BaseMonster target) {
        if (projectileSpell == null || target == null) {
            return false;
        }
        for (SpellProjectile projectile : activeProjectiles) {
            if (projectile.matches(projectileSpell, target)) {
                return true;
            }
        }
        return false;
    }

    private void renderImpacts(SpriteBatch batch) {
        if (activeImpacts.isEmpty()) {
            return;
        }
        float delta = Gdx.graphics.getDeltaTime();
        for (int i = activeImpacts.size() - 1; i >= 0; i--) {
            SpellImpact impact = activeImpacts.get(i);
            if (!impact.update(delta)) {
                activeImpacts.remove(i);
                continue;
            }
            renderImpactFrame(batch, impact);
        }
    }

    private void renderProjectiles(SpriteBatch batch) {
        if (activeProjectiles.isEmpty()) {
            return;
        }
        float delta = Gdx.graphics.getDeltaTime();
        for (int i = activeProjectiles.size() - 1; i >= 0; i--) {
            SpellProjectile projectile = activeProjectiles.get(i);
            if (!projectile.update(delta, projectileSpeed)) {
                activeProjectiles.remove(i);
                continue;
            }
            projectile.render(batch);
        }
    }

    private void renderChannels(SpriteBatch batch) {
        if (activeChannels.isEmpty()) return;
        float delta = Gdx.graphics.getDeltaTime();
        for (ChannelEffect channel : activeChannels) {
            channel.update(delta);
            Vector2 position = channel.positionSupplier.get();
            ImpactFrame frame = channel.currentFrame();
            if (position != null && frame != null) renderImpactFrame(batch, frame, position.x, position.y);
        }
    }

    /**
     * Dessine la frame courante d'un impact. Quand la frame porte un masque de transparence, le
     * sprite couleur est rendu avec le shader qui module son alpha par le masque, comme le
     * client d'origine le fait pixel par pixel.
     */
    private void renderImpactFrame(SpriteBatch batch, SpellImpact impact) {
        ImpactFrame frame = impact.currentFrame();
        if (frame == null) {
            return;
        }
        renderImpactFrame(batch, frame, impact.worldX, impact.worldY);
    }

    private void renderImpactFrame(SpriteBatch batch, ImpactFrame frame, float worldX, float worldY) {
        float w = frame.region.getRegionWidth();
        float h = frame.region.getRegionHeight();
        float x = worldX + frame.offsetX;
        float y = worldY + frame.offsetY;

        ShaderProgram shader = frame.hasMask() ? maskShader() : null;
        if (shader == null) {
            batch.draw(frame.region, x, y + h, w, -h);
            return;
        }

        // Le masque couvre une zone plus large que la flamme : ses coordonnées se déduisent de
        // l'écart entre les deux offsets de dessin, exprimé en fraction de la taille du masque.
        float maskW = frame.mask.getRegionWidth();
        float maskH = frame.mask.getRegionHeight();
        float scaleX = w / maskW;
        float scaleY = h / maskH;
        float offsetX = (frame.offsetX - frame.maskOffsetX) / maskW;
        float offsetY = (frame.offsetY - frame.maskOffsetY) / maskH;

        batch.setShader(shader);
        frame.mask.getTexture().bind(1);
        shader.setUniformi("u_mask", 1);
        shader.setUniform2fv("u_maskScale", new float[] {scaleX, scaleY}, 0, 2);
        shader.setUniform2fv("u_maskOffset", new float[] {offsetX, offsetY}, 0, 2);
        // Le batch dessine sur l'unité 0 : elle doit redevenir l'unité active.
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
        batch.draw(frame.region, x, y + h, w, -h);
        batch.setShader(null);
    }

    /** Charge le shader de masque au premier usage, et le désactive s'il ne compile pas. */
    private ShaderProgram maskShader() {
        if (maskShaderLoaded) {
            return maskShader;
        }
        maskShaderLoaded = true;
        FileHandle vertex = Gdx.files.internal(Paths.SHADERS_SPELL_MASK_VERT);
        FileHandle fragment = Gdx.files.internal(Paths.SHADERS_SPELL_MASK_FRAG);
        if (!vertex.exists() || !fragment.exists()) {
            return null;
        }
        ShaderProgram program = new ShaderProgram(vertex, fragment);
        if (!program.isCompiled()) {
            Gdx.app.error("SpellRenderer", "Spell mask shader not compiled: " + program.getLog());
            program.dispose();
            return null;
        }
        maskShader = program;
        return maskShader;
    }

    /** Libère le shader de masque. */
    public void dispose() {
        if (maskShader != null) {
            maskShader.dispose();
            maskShader = null;
        }
    }

    private List<ImpactFrame> getImpactFrames(String impactSpell) {
        List<ImpactFrame> cached = impactFramesCache.get(impactSpell);
        if (cached != null) {
            return cached;
        }
        List<ImpactFrame> frames = new ArrayList<>();
        List<String> names = resolveFrameNames(spriteLoader, impactSpell);
        // Les effets d'origine peuvent fournir un masque de transparence par frame, sous le nom
        // de l'effet suivi d'un 'A'. Il module l'alpha du sprite couleur pixel par pixel.
        List<String> maskNames = resolveFrameNames(spriteLoader, toMaskVariantName(impactSpell));
        for (int i = 0; i < names.size(); i++) {
            try {
                TextureRegion region = spriteLoader.getRegionFromSpriteName(names.get(i));
                if (region == null) {
                    continue;
                }
                SpriteLoader.Sprite meta = getSpriteMeta(names.get(i));
                TextureRegion mask = null;
                SpriteLoader.Sprite maskMeta = null;
                if (i < maskNames.size()) {
                    mask = spriteLoader.getRegionFromSpriteName(maskNames.get(i));
                    maskMeta = getSpriteMeta(maskNames.get(i));
                }
                frames.add(new ImpactFrame(region, meta, mask, maskMeta));
            } catch (Exception ignored) {
            }
        }
        impactFramesCache.put(impactSpell, frames);
        return frames;
    }

    private List<ProjectileFrame> getProjectileFrames(String projectileSpell) {
        List<ProjectileFrame> cached = projectileFramesCache.get(projectileSpell);
        if (cached != null) {
            return cached;
        }
        List<ProjectileFrame> frames = new ArrayList<>();
        List<String> names = resolveFrameNames(spriteLoader, projectileSpell);
        for (String name : names) {
            try {
                TextureRegion region = spriteLoader.getRegionFromSpriteName(name);
                if (region != null) {
                    SpriteLoader.Sprite meta = getSpriteMeta(name);
                    frames.add(new ProjectileFrame(region, meta));
                }
            } catch (Exception ignored) {
            }
        }
        projectileFramesCache.put(projectileSpell, frames);
        return frames;
    }

    private SpriteLoader.Sprite getSpriteMeta(String spriteName) {
        if (spriteName == null || spriteName.isEmpty()) {
            return null;
        }
        if (spriteMetaCache.isEmpty()) {
            for (SpriteLoader.Sprite sprite : spriteLoader.getSprites()) {
                if (sprite.getName() != null) {
                    spriteMetaCache.put(sprite.getName().toLowerCase(Locale.ROOT), sprite);
                }
            }
        }
        return spriteMetaCache.get(spriteName.toLowerCase(Locale.ROOT));
    }

    /**
     * Nom des frames de masque d'un effet : la librairie d'origine insère un 'A' avant le tiret
     * des frames, par exemple {@code BoulderFire-} donne {@code BoulderFireA-}.
     */
    private static String toMaskVariantName(String baseName) {
        if (baseName.endsWith("-")) {
            return baseName.substring(0, baseName.length() - 1) + "A-";
        }
        return baseName + "A";
    }

    private static List<String> resolveFrameNames(SpriteLoader spriteLoader, String baseName) {
        List<String> names = new ArrayList<>();
        if (baseName == null || baseName.isEmpty() || spriteLoader == null) {
            return names;
        }
        // Strip trailing '-' so "HealSerious-" matches frames named "HealSerious-001"
        if (baseName.endsWith("-")) {
            baseName = baseName.substring(0, baseName.length() - 1);
        }
        String lower = baseName.toLowerCase(Locale.ROOT);
        Map<FrameKey, String> indexed = new HashMap<>();
        for (SpriteLoader.Sprite sprite : spriteLoader.getSprites()) {
            String name = sprite.getName();
            if (name == null) {
                continue;
            }
            String nameLower = name.toLowerCase(Locale.ROOT);
            if (!nameLower.startsWith(lower)) {
                continue;
            }
            FrameKey key = parseFrameKey(baseName, name);
            if (key != null) {
                indexed.put(key, name);
            }
        }
        if (!indexed.isEmpty()) {
            indexed.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> names.add(entry.getValue()));
        } else {
            for (SpriteLoader.Sprite sprite : spriteLoader.getSprites()) {
                String name = sprite.getName();
                if (name != null && name.equalsIgnoreCase(baseName)) {
                    names.add(name);
                    break;
                }
            }
        }
        return names;
    }

    private static FrameKey parseFrameKey(String baseName, String spriteName) {
        if (!spriteName.regionMatches(true, 0, baseName, 0, baseName.length())) {
            return null;
        }
        int idx = baseName.length();
        if (idx >= spriteName.length() || spriteName.charAt(idx) != '-') {
            return null;
        }
        idx++;
        int number = 1;
        int numberStart = idx;
        while (idx < spriteName.length() && Character.isDigit(spriteName.charAt(idx))) {
            idx++;
        }
        if (idx > numberStart) {
            try {
                number = Integer.parseInt(spriteName.substring(numberStart, idx));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        if (idx >= spriteName.length()) {
            return null;
        }
        char letter = Character.toLowerCase(spriteName.charAt(idx));
        if (letter < 'a' || letter > 'z') {
            return null;
        }
        return new FrameKey(number, letter);
    }
/**
 * Class representing FrameKey.
 */

    private static final class FrameKey implements Comparable<FrameKey> {
        private final int number;
        private final char letter;

        private FrameKey(int number, char letter) {
            this.number = number;
            this.letter = letter;
        }

        @Override
        public int compareTo(FrameKey other) {
            int cmp = Integer.compare(this.number, other.number);
            if (cmp != 0) {
                return cmp;
            }
            return Character.compare(this.letter, other.letter);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof FrameKey other)) {
                return false;
            }
            return number == other.number && letter == other.letter;
        }

        @Override
        public int hashCode() {
            return 31 * number + letter;
        }
    }
/**
 * Class representing SpellImpact.
 */

    private static final class SpellImpact {
        private final String impactSpell;
        private final List<ImpactFrame> frames;
        private final float worldX;
        private final float worldY;
        private int frameIndex = 0;
        private float timer = 0f;
        private final float frameDuration = 0.08f;

        private SpellImpact(String impactSpell, List<ImpactFrame> frames, float worldX, float worldY) {
            this.impactSpell = impactSpell;
            this.frames = frames;
            this.worldX = worldX;
            this.worldY = worldY;
        }

        private boolean update(float delta) {
            if (frames.isEmpty()) {
                return false;
            }
            timer += delta;
            if (timer >= frameDuration) {
                timer = 0f;
                frameIndex++;
                if (frameIndex >= frames.size()) {
                    return false;
                }
            }
            return true;
        }

        private ImpactFrame currentFrame() {
            if (frames.isEmpty() || frameIndex >= frames.size()) {
                return null;
            }
            return frames.get(frameIndex);
        }
    }

    public static final class ChannelHandle {
        private ChannelHandle() {}
    }

    private static final class ChannelEffect {
        private final ChannelHandle handle;
        private final List<ImpactFrame> frames;
        private final Supplier<Vector2> positionSupplier;
        private int frameIndex;
        private float timer;

        private ChannelEffect(ChannelHandle handle, List<ImpactFrame> frames, Supplier<Vector2> positionSupplier) {
            this.handle = handle;
            this.frames = frames;
            this.positionSupplier = positionSupplier;
        }

        private void update(float delta) {
            timer += delta;
            while (timer >= .08f) {
                timer -= .08f;
                frameIndex = (frameIndex + 1) % frames.size();
            }
        }

        private ImpactFrame currentFrame() { return frames.get(frameIndex); }
    }
/**
 * Class representing SpellProjectile.
 */

    private static final class SpellProjectile {
        private final String projectileSpell;
        private final List<ProjectileFrame> frames;
        private final Object targetIdentity;
        private final Supplier<Vector2> targetPosition;
        private final boolean flipX;
        private final Runnable onImpact;
        private float x;
        private float y;
        private int frameIndex = 0;
        private float timer = 0f;
        private final float frameDuration = 0.08f;

        private SpellProjectile(String projectileSpell, List<ProjectileFrame> frames, BaseMonster target, float startX, float startY, boolean flipX, Runnable onImpact) {
            this(projectileSpell, frames, target, target::getPosition, startX, startY, flipX, onImpact);
        }

        private SpellProjectile(String projectileSpell, List<ProjectileFrame> frames, Object targetIdentity,
                                Supplier<Vector2> targetPosition, float startX, float startY,
                                boolean flipX, Runnable onImpact) {
            this.projectileSpell = projectileSpell;
            this.frames = frames;
            this.targetIdentity = targetIdentity;
            this.targetPosition = targetPosition;
            this.x = startX;
            this.y = startY;
            this.flipX = flipX;
            this.onImpact = onImpact;
        }

        private boolean update(float delta, float speed) {
            if (frames.isEmpty() || targetPosition == null) {
                return false;
            }
            timer += delta;
            if (timer >= frameDuration) {
                timer = 0f;
                frameIndex = (frameIndex + 1) % frames.size();
            }
            Vector2 target = targetPosition.get();
            if (target == null) return false;
            float targetX = target.x;
            float targetY = target.y;
            float dx = targetX - x;
            float dy = targetY - y;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            if (distance <= Math.max(1f, speed * delta)) {
                x = targetX;
                y = targetY;
                onImpact.run();
                return false;
            }
            float ndx = dx / distance;
            float ndy = dy / distance;
            x += ndx * speed * delta;
            y += ndy * speed * delta;
            return true;
        }

        private void render(SpriteBatch batch) {
            if (frames.isEmpty()) {
                return;
            }
            ProjectileFrame frame = frames.get(frameIndex);
            float w = frame.region.getRegionWidth();
            float h = frame.region.getRegionHeight();
            float drawX = x + (flipX ? frame.offsetX2 : frame.offsetX1);
            float drawY = y + (flipX ? frame.offsetY2 : frame.offsetY1);
            if (flipX) {
                batch.draw(frame.region, drawX + w, drawY + h, -w, -h);
            } else {
                batch.draw(frame.region, drawX, drawY + h, w, -h);
            }
        }

        private boolean matches(String projectileSpell, BaseMonster target) {
            return this.targetIdentity == target && this.projectileSpell.equals(projectileSpell);
        }
    }
/**
 * Class representing ImpactFrame.
 */

    private static final class ImpactFrame {
        private final TextureRegion region;
        private final int offsetX;
        private final int offsetY;
        /** Masque de transparence optionnel, appliqué par pixel au sprite couleur. */
        private final TextureRegion mask;
        private final int maskOffsetX;
        private final int maskOffsetY;

        private ImpactFrame(TextureRegion region, SpriteLoader.Sprite meta,
                            TextureRegion mask, SpriteLoader.Sprite maskMeta) {
            this.region = region;
            this.offsetX = meta == null ? 0 : meta.getDrawOffset1X();
            this.offsetY = meta == null ? 0 : meta.getDrawOffset1Y();
            this.mask = mask;
            this.maskOffsetX = maskMeta == null ? 0 : maskMeta.getDrawOffset1X();
            this.maskOffsetY = maskMeta == null ? 0 : maskMeta.getDrawOffset1Y();
        }

        private boolean hasMask() {
            return mask != null;
        }
    }
/**
 * Class representing ProjectileFrame.
 */

    private static final class ProjectileFrame {
        private final TextureRegion region;
        private final int offsetX1;
        private final int offsetY1;
        private final int offsetX2;
        private final int offsetY2;

        private ProjectileFrame(TextureRegion region, SpriteLoader.Sprite meta) {
            this.region = region;
            if (meta == null) {
                this.offsetX1 = 0;
                this.offsetY1 = 0;
                this.offsetX2 = 0;
                this.offsetY2 = 0;
            } else {
                this.offsetX1 = meta.getDrawOffset1X();
                this.offsetY1 = meta.getDrawOffset1Y();
                this.offsetX2 = meta.getDrawOffset2X();
                this.offsetY2 = meta.getDrawOffset2Y();
            }
        }
    }
}
