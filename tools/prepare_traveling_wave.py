"""Place a fully formed breaker on a fixed left-to-right path across the target.

Source frame dimensions stay constant: no grow-in or vertical drop is introduced.
The common 384px canvas centres the target halfway along the wave's travel.
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
    parser.add_argument("--crash-sheet", type=Path)
    args = parser.parse_args()
    source = Image.open(args.sheet).convert("RGBA")
    if not 2 <= args.frames <= 28 or source.width % args.frames:
        parser.error("Expected 2–28 frames on a horizontal sheet")
    width = source.width // args.frames
    crash = Image.open(args.crash_sheet).convert("RGBA") if args.crash_sheet else None
    args.output.mkdir(parents=True, exist_ok=True)
    for i in range(args.frames):
        frame = source.crop((i * width, 0, (i + 1) * width, source.height))
        if crash is not None and i >= args.frames - 4:
            # Finish the travelling crest with a broad surf impact, not a closing water ring.
            cell = crash.width // 16
            n = 12 + i - (args.frames - 4)
            frame = crash.crop((n * cell, 0, (n + 1) * cell, crash.height))
        frame.thumbnail((240, 240), Image.Resampling.NEAREST)
        pixels = np.array(frame)
        rgb = pixels[:, :, :3].astype(np.int16)
        matte = (rgb.max(axis=2) - rgb.min(axis=2) < 6) & (
            np.abs(rgb.mean(axis=2) - 128) < 12
        )
        if matte.mean() > 0.98:
            pixels[:, :, 3] = 0
        frame = Image.fromarray(pixels)
        canvas = Image.new("RGBA", (384, 320))
        x = round((384 - frame.width) * i / (args.frames - 1))
        canvas.alpha_composite(frame, (x, 320 - frame.height))
        canvas.save(args.output / f"frame_{i:02d}.png")
    print(f"Prepared {args.frames} traveling-wave frames without vertical scaling")


if __name__ == "__main__":
    main()
