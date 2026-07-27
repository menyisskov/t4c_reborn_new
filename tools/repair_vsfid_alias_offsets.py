import re
import sys
import xml.etree.ElementTree as ET
from collections import defaultdict
from pathlib import Path


def normalized(name):
    return re.sub(r'[^a-z0-9]+', '', name.lower())


def repair(path):
    root = ET.parse(path).getroot()
    candidates = defaultdict(set)
    frames = []

    for sprite in root.findall('sprite'):
        sprite_frames = sprite.findall('frame')
        for frame in sprite_frames:
            name = frame.get('name')
            if not name or '&x' in name or '&y' in name:
                continue
            reverse = frame.get('reverse') == '1'
            offsets = tuple(int(frame.get(attr, '0')) for attr in ('x1', 'y1', 'x2', 'y2'))
            effective = offsets[2:4] if reverse else offsets[0:2]
            # Authored VSFID frames carry their legacy color key. Generated
            # aliases do not; their offsets may be wrong even when non-zero.
            if frame.get('colorkey_r') is not None:
                candidates[(normalized(name), reverse)].add(effective)
            frames.append((sprite, sprite_frames, frame, name, reverse, effective))

    replacements = {}
    for sprite, sprite_frames, frame, name, reverse, effective in frames:
        # Generated aliases are singleton definitions. Mosaic/random/template
        # definitions remain the authoritative source and are never rewritten.
        if len(sprite_frames) != 1 or any(sprite.get(attr) is not None
                                          for attr in ('mosaic_x', 'mosaic_y', 'random', 'template')):
            continue
        values = candidates.get((normalized(name), reverse), set())
        if frame.get('colorkey_r') is not None or len(values) != 1:
            continue
        target = next(iter(values))
        if effective == target:
            continue
        sprite_id = int(sprite.get('id'), 0)
        replacements[sprite_id] = (target, reverse, name)

    text = path.read_text(encoding='utf-8')
    changed = []
    for sprite_id, (target, reverse, name) in sorted(replacements.items()):
        id_pattern = rf'(<sprite\s+id="(?:0x{sprite_id:x}|{sprite_id})"[^>]*>\s*<frame\s+[^>]*)(/>)'
        match = re.search(id_pattern, text, flags=re.IGNORECASE)
        if not match:
            continue
        opening = match.group(1)
        attrs = ('x2', 'y2') if reverse else ('x1', 'y1')
        updated = opening
        for attr, value in zip(attrs, target):
            updated = re.sub(rf'\b{attr}="-?\d+"', f'{attr}="{value}"', updated)
        if updated != opening:
            text = text[:match.start(1)] + updated + text[match.end(1):]
            changed.append((sprite_id, name, target, reverse))

    path.write_text(text, encoding='utf-8')
    print(f'corrected={len(changed)}')
    for sprite_id, name, target, reverse in changed:
        print(f'{sprite_id:#x} {name} reverse={reverse} offset={target}')


if __name__ == '__main__':
    repair(Path(sys.argv[1]))
