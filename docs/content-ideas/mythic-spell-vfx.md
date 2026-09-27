# Mythic spell impacts (T4C-0082) — SpriteCook runbook

Third step of the high-tier spell VFX ladder (see `DESIGN_GUIDELINES.md`, "High-tier spell VFX
escalation"). Tiers 150+ get the burst shower and tiers 250+ the Ascended recolor. Tiers 350+
(the level 350 and 400 nukes) get a **newly drawn** impact per element, generated with the
SpriteCook plugin and packed by `tools.MythicVfxPacker`.

## Status

All six elements are generated, packed and listed in `MYTHIC_IMPACTS` (2026-09-27). The
source stills and 16-frame sheets are kept next to this file (`mythic-spell-vfx/`), and their
SpriteCook asset IDs are in `mythic-spell-vfx/spritecook-assets.json`. Re-pack from those
sheets instead of paying to regenerate. Every element was packed with the same command:

`MythicVfxPacker <Base>-sheet.png <Base> --frames 16 --cols 16 --strip-matte --grow-in 3 --hold 2 --ground 0`

Cost of the whole pass: 192 credits, out of the 3,000 the account had. That was 6 stills at 12
credits each (72) and 6 animations at 20 each (120). The two reference uploads were free.
Everything went on the first try, and nothing was re-rolled.

Lessons from the run:
- **Animations end on a matte frame.** SpriteCook composites frames over #808080 before
  removing the background, and the last frame of fire, air and light came back as nothing but
  that gray. `--strip-matte` clears the narrow matte band (within 3 of 128 in every channel).
- **Generated sequences open at full size.** The "starts as a small spark" part of the prompt
  was ignored, so `--grow-in 3` builds the eruption from the first frame instead.
- **Still sizes vary a lot** for the same 128x128 hint: 100 px for air, 232 px for water.
  Packing them at native size was acceptable. Ask for a size in the prompt if it matters.

## Spend credits carefully

SpriteCook bills per generation. Pilot fire alone, and only move on to the other five elements
once fire is packed and looks right in game.

1. `list_generation_models` once. Pick a pixel-art-capable model and note its per-image cost.
2. Always inspect a still before animating it. Animation is the expensive step, and animating a
   bad still wastes it.
3. Use `variations: 1`. If the still misses, fix the prompt rather than asking for 4 variations.

## Steps (as run for fire, then repeated per element)

All commands run from the repo root after `mvn -q compile`, with `CP` holding the Maven classpath
(`mvn -q dependency:build-classpath -Dmdep.outputFile=cp.txt`). Keep outputs in a scratch
directory, not the repo.

1. **Export style references** from the legacy art so the new art matches the game's look:
   `java -cp "target/classes;$CP" com.perso.T4C.tools.MythicVfxPacker --export GreatExplosion <scratch>/ref`
   (use `:` instead of `;` on Linux/macOS). Upload 2-3 frames spanning the animation
   (e.g. `GreatExplosion-e`, `-s`, `-2a`) with the `spritecook-upload-assets` workflow. Keep their
   asset IDs; they serve all six elements.
2. **Generate the peak still** with `generate_game_art`:
   - `pixel: true`, `width: 128`, `height: 128`, `bg_mode: "transparent"`, `variations: 1`,
     `style_asset_ids`: the uploaded references.
   - `theme`: "late-90s isometric dark fantasy MMORPG spell effect".
   - `prompt` (fire): "A towering pillar of white-hot flame erupting upward from the ground, a
     blazing white-gold core wrapped in roaring orange and crimson fire, with a ring of molten
     embers bursting outward at its base. Seen from a 3/4 isometric camera. A single spell impact
     effect, no character, no ground tile."
   - `colors` (fire): `["#1a0000", "#5a0a00", "#a01800", "#e04000", "#ff8000", "#ffc040", "#fff0b0", "#ffffff"]`.
3. **Inspect the still.** Download it, nearest-upscale it for viewing, and check: pixel-crisp, no
   background matte left behind, reads as fire at a glance, and the silhouette differs from the
   legacy round explosion.
4. **Animate** with `animate_game_art`:
   - `asset_id`: the still, `pixel: true`, `output_frames: 16` (the pixel-mode maximum),
     `output_format: "spritesheet"`, `edge_margin: 6`, `removebg: "Basic"`, `auto_enhance_prompt: false`.
   - `prompt`: "The flame pillar starts as a small bright spark on the ground, then erupts
     violently upward into the full white-gold column of fire with embers bursting outward, holds
     at full intensity for a moment, then collapses and dissipates into drifting embers and thin
     smoke until nothing remains. The effect stays centered in place; it does not travel."
   - `negative_prompt`: "character, person, ground tiles, camera movement, text".
5. **Dry-run the packer** and inspect the contact sheet:
   `java -cp "target/classes;$CP" com.perso.T4C.tools.MythicVfxPacker <sheet.png> MythicFire --frames 16 --cols 16 --strip-matte --grow-in 3 --hold 2 --ground 0 --dry-run --preview <scratch>/preview`
   - Add `--downscale k` if the sheet's cells are upscaled (the pixel grid is k screen pixels wide).
   - `--hold 2` makes 16 frames last about as long as the 33-frame GreatExplosion, since impact
     frames play at one fixed rate.
   - Check that the fire sits over the tile where GreatExplosion does, and that the first and last
     frames are small or empty (a clean start and end).
6. **Pack** by re-running without `--dry-run`, then set
   `MYTHIC_IMPACTS = Map.of(FIRE, "MythicFire")` in `HighTierSpellCurve`, update the status table
   above, and run `mvn -q test` (`MythicVfxAssetTest` and `HighTierSpellLadderTest` cover it).
7. **See it in game** if possible. Then add the changelog entry (player-facing) under T4C-0082,
   or under a new ID if T4C-0082 is already closed.

## Other elements (after fire)

Reuse the same references, settings and prompt shape. Change the subject and palette:

| Element | Subject | Palette (dark → bright) |
|---|---|---|
| Earth | jagged obsidian spires bursting up from cracked ground, glowing magma seams | `#0a0005 #2a0a0a #5a1a10 #8a3a20 #c06030 #e09050 #f0c080` |
| Air | a spiraling vortex of violet lightning with forking arcs | `#0f0023 #2a0a60 #5020a0 #8050e0 #b090ff #e0d0ff #ffffff` |
| Water | a crashing tidal spike of deep blue water freezing into ice shards | `#000519 #001a50 #0040a0 #2070e0 #60a0ff #b0e0ff #ffffff` |
| Light | a radiant descending column of golden holy light with a halo ring | `#231000 #603a00 #a07000 #e0a800 #ffd850 #fff0a0 #ffffff` |
| Dark | a writhing eruption of black-violet shadow tendrils around a void core | `#050008 #1a0028 #3a0050 #600080 #9020b0 #c040e0 #f0a0ff` |
