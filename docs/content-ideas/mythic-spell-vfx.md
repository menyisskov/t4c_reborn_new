# High-tier ladder spell impacts — art log and SpriteCook runbook (T4C-0082, T4C-0085)

The rules (one animation per ladder spell, which family serves which level, packing options) are
in `DESIGN_GUIDELINES.md`, "High-tier spell VFX". This file records how the art was made, so it
can be reproduced, extended or re-packed without paying for it twice.

## What each level plays

| Level | Families | Made how |
|---|---|---|
| 150 | `Strike<Element>` | SpriteCook, painted (detailed), new prompt styled on the element's original |
| 200 | `Eruption<Element>` | SpriteCook, pixel art (T4C-0082). Sheets kept here as `Eruption*-sheet.png` |
| 250 | `<legacy>-Ascended` | `tools.AscendedVfxGenerator` recolor (T4C-0077), no generation |
| 300 | `Grand*`, `BoulderFire`, `iceTree` | The original game's animations; `tools.GrandVfxGenerator` makes the `Grand*` copies |
| 350 | `Mythic<Element>` | SpriteCook, painted: edit of the original's peak frame, then animated |

All SpriteCook asset IDs are in `mythic-spell-vfx/spritecook-assets.json`. A sheet can be
re-downloaded from its animation ID (`get_asset_metadata` -> `spritesheet_url`); only the small
pixel-art Eruption sheets are kept in the repo, because the painted sheets are ~10 MB each.

## Cost

About 384 credits for T4C-0085, on top of 192 for T4C-0082:

| Item | Count | Credits |
|---|---|---|
| Stills (12 each): 6 Mythic upgrade edits, 6 Strikes | 12 | 144 |
| Animations (20 each, detailed): 6 Mythic (24 frames), 6 Strikes (16 frames) | 12 | 240 |
| Uploads of original peak frames | 6 | 0 |

Nothing was re-rolled. Every still was inspected over grass before its animation was paid for.

## Recipes

Commands run from the repo root after `mvn -q compile`, with `CP` holding the Maven classpath
(use `:` instead of `;` on Linux/macOS).

**Export an original's frames** (to pick a peak frame to upload):
`java -cp "target/classes;$CP" com.perso.T4C.tools.MythicVfxPacker --export GrandFire <dir>`

**Level 350 upgrade** (fire shown):
1. Upload the peak frame (`GrandFire-2f`), `pixel: false`.
2. `generate_game_art`: `edit_asset_id` = that upload, `pixel: false`, 384x384 hint, one
   variation, transparent. Prompt: "Turn this ... into a far more powerful, cataclysmic version
   of the same spell in the exact same painterly pre-rendered style: ... Keep the same colors
   ... and soft glowing edges."
3. `animate_game_art`: 24 frames, `pixel: false`, spritesheet, `removebg: Basic`. The prompt
   describes a small start, the full blast, then a collapse "until nothing remains".
4. Pack: `MythicVfxPacker <sheet> MythicFire --frames 24 --cols 24 --scale 0.45 --soft-alpha
   --strip-matte --grow-in 3 --fade-out 3 --match GrandFire` (dry-run with `--preview` first).

**Level 150 strike:** `generate_game_art` with a new prompt named after the spell (Scorchbrand,
Stonefang, Galespike, Rime Lance, Sunscour, Nightfang), `style_asset_ids` = the element's
original peak frame, 256x256 hint. Animate at 16 frames, detailed. Pack with `--match` on the
element's legacy bolt impact, so it lands where bolts always have.

**Level 200 eruption** (from the kept sheets, no credits):
`MythicVfxPacker <Eruption*-sheet.png> EruptionFire --frames 16 --cols 16 --strip-matte
--grow-in 3 --hold 2 --ground 0`

## Lessons

- **Generated sequences open at full size and end on visible smoke**, whatever the prompt says:
  `--grow-in` and `--fade-out` fix both.
- **The "NM" originals are additive art.** Drawn with normal blending they get black halos;
  `lumaAlpha` converts them exactly. `BoulderFire` has its own mask and `iceTree` is not additive.
- **Placeholder frames skew alignment**: NM_Fire000 opens with two 32x16 frames at x=-305, so
  reference boxes ignore frames under 5% of the largest one's area.
- **Painted frames are heavy** (~100 KB each): never `--hold` them.
- **Stills vary in size** for the same hint (pixel art: 100 px air, 232 px water; painted:
  ~800-1000 px); size them at pack time with `--scale`.
- **The comparison composites lie about tiny frames**: a 10x7 closing wisp scaled up to 150 px
  looks like a smear. Check the packer log's frame sizes before worrying.
