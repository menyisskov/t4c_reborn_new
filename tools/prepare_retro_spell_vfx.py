"""Prepare generated VFX for T4C's gritty, low-resolution pre-rendered sprite style.

Keeps the original sheet intact. One shared palette across the entire animation avoids
colour flicker; a fixed grain pattern keeps roughness stable between frames.
"""

import argparse
from pathlib import Path

import numpy as np
from PIL import Image


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("sheet", type=Path)
    parser.add_argument("output", type=Path)
    parser.add_argument("--frames", type=int, required=True)
    parser.add_argument("--size", type=int, default=320)
    parser.add_argument("--colors", type=int, default=96)
    parser.add_argument("--legacy-impact", type=Path)
    parser.add_argument("--eruption", action="store_true")
    args = parser.parse_args()
    if not 1 <= args.frames <= 30:
        parser.error("Use between 1 and 30 distinct frames")
    source = Image.open(args.sheet).convert("RGBA")
    if source.width % args.frames:
        parser.error("Expected a horizontal sheet with equal-width frames")
    width = source.width // args.frames
    frames = []
    grain = np.random.default_rng(1999).integers(-5, 6, (args.size, args.size, 1))
    for i in range(args.frames):
        frame = source.crop((i * width, 0, (i + 1) * width, source.height))
        frame = frame.resize((args.size, args.size), Image.Resampling.BOX)
        pixels = np.array(frame)
        rgb = pixels[:, :, :3].astype(np.int16)
        # Some providers return a full matte frame after the effect has disappeared.
        # Only discard that whole-frame failure, never grey pixels in actual stone or smoke.
        neutral_matte = (rgb.max(axis=2) - rgb.min(axis=2) < 6) & (
            np.abs(rgb.mean(axis=2) - 128) < 12
        )
        if neutral_matte.mean() > 0.98:
            pixels[:, :, 3] = 0
        pixels[:, :, :3] = np.clip(rgb + grain, 0, 255).astype(np.uint8)
        pixels[pixels[:, :, 3] < 12] = 0
        frames.append(Image.fromarray(pixels))

    atlas = Image.new("RGB", (args.size * args.frames, args.size))
    for i, frame in enumerate(frames):
        atlas.paste(frame.convert("RGB"), (i * args.size, 0))
    palette = atlas.quantize(colors=args.colors, method=Image.Quantize.MEDIANCUT)
    args.output.mkdir(parents=True, exist_ok=True)
    legacy = sorted(args.legacy_impact.glob("frame_*.png")) if args.legacy_impact else []
    if args.legacy_impact and not legacy:
        parser.error("No anchored legacy impact frames found")
    for i, frame in enumerate(frames):
        if args.eruption:
            # Cut the model's long idle hold into a quick heave, peak and collapse.
            if args.frames != 24:
                parser.error("The eruption timing requires the 24-frame source")
            source_index = min(11, i * 2) if i < 6 else min(23, i + 2)
            frame = frames[source_index]
        retro = frame.convert("RGB").quantize(
            palette=palette, dither=Image.Dither.FLOYDSTEINBERG
        ).convert("RGBA")
        retro.putalpha(frame.getchannel("A"))
        if args.eruption and i < 6:
            rise = (i + 1) / 6
            w = round(args.size * (0.55 + 0.45 * rise))
            h = round(args.size * (0.12 + 0.88 * rise))
            emerged = retro.resize((w, h), Image.Resampling.NEAREST)
            retro = Image.new("RGBA", frame.size)
            retro.alpha_composite(emerged, ((args.size - w) // 2, args.size - h))
        if args.eruption and i >= 22:
            keep = (24 - i) / 3
            retro.putalpha(retro.getchannel("A").point(lambda a: round(a * keep)))
        if args.legacy_impact and i >= 5:
            # The original blast gives the new comet T4C's familiar turbulent impact.
            progress = (i - 5) / max(1, args.frames - 6)
            blast = Image.open(legacy[round(progress * (len(legacy) - 1))]).convert("RGBA")
            if blast.size != retro.size:
                parser.error("Legacy impact canvases must match --size")
            # Legacy fire is drawn against a black matte. Convert its additive glow
            # to straight alpha so black fringes do not become opaque in the new pack.
            glow = np.array(blast).astype(np.float32)
            brightness = glow[:, :, :3].max(axis=2)
            glow[:, :, :3] *= 255 / np.maximum(1, brightness[:, :, None])
            glow[:, :, 3] *= brightness / 255
            blast = Image.fromarray(np.clip(glow, 0, 255).astype(np.uint8))
            if i >= 12:
                keep = max(0, (args.frames - 1 - i) / (args.frames - 13))
                retro.putalpha(retro.getchannel("A").point(lambda a: round(a * keep)))
            retro = Image.alpha_composite(retro, blast)
        retro.save(args.output / f"frame_{i:02d}.png")
    print(f"Prepared {len(frames)} unique frames at {args.size}px with a shared palette")


if __name__ == "__main__":
    main()
