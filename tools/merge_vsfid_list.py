import copy
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

from apply_vsfid_offsets import key, read_pack


def clean_label(label):
    mirror = bool(re.search(r'\s*\(M\)\s*$', label, re.I))
    floor = bool(re.search(r'\s*\(F\)\s*$', label, re.I))
    label = re.sub(r'\s*\((?:M|F)\)\s*$', '', label, flags=re.I).strip()
    return label, mirror, floor


def effective_mapping_offsets(root):
    """Return unambiguous authored offsets for each sprite name/direction."""
    candidates = {}
    for frame in root.findall('.//frame'):
        name = frame.get('name')
        if not name or '&x' in name or '&y' in name:
            continue
        reverse = frame.get('reverse') == '1'
        attrs = tuple(int(frame.get(attr, '0')) for attr in ('x1', 'y1', 'x2', 'y2'))
        effective = attrs[2:4] if reverse else attrs[0:2]
        # Frames carrying a color key come from the authored VSFID mapping.
        # Generated singleton aliases do not, and must not become candidates
        # merely because they copied a non-zero offset from sprites.bin.
        if frame.get('colorkey_r') is not None:
            candidates.setdefault((key(name), reverse), set()).add(effective)
    return {
        candidate_key: next(iter(values))
        for candidate_key, values in candidates.items()
        if len(values) == 1
    }


def main(list_path, xml_path, sprite_path):
    rows = []
    for line in list_path.read_text(encoding='utf-8', errors='replace').splitlines():
        match = re.match(r'^\s*(\d+)\s+(.+?)\s*$', line)
        if match:
            rows.append((int(match.group(1)), match.group(2)))

    tree = ET.parse(xml_path); root = tree.getroot()
    existing = {int(node.get('id'), 16) for node in root.findall('sprite')}
    authored_offsets = effective_mapping_offsets(root)
    _, sprites, _ = read_pack(sprite_path)
    by_key = {}
    for name, values, _ in sprites:
        by_key.setdefault(key(name), []).append((name, values))

    # Existing floor definitions are authoritative for mosaic/random behavior.
    floor_defs = {}
    for node in root.findall('sprite'):
        if node.get('template'):
            continue
        frames = node.findall('frame')
        if not frames or not any(f.get('floor') is not None for f in frames):
            continue
        first = frames[0].get('name', '')
        base = re.sub(r'\s*\([^)]*(?:&x|\d+)\s*,[^)]*(?:&y|\d+)\)\s*$', '', first).strip()
        floor_defs.setdefault(key(base), node)

    additions = []; skipped = []
    for ident, raw_label in rows:
        if ident in existing:
            continue
        if 'UNDEFINE' in raw_label or raw_label.startswith('Color ') or raw_label.upper() == 'A FAIRE':
            continue
        label, mirror, floor = clean_label(raw_label)
        source = floor_defs.get(key(label)) if floor else None
        if source is not None:
            node = copy.deepcopy(source); node.set('id', hex(ident))
            additions.append(node); existing.add(ident); continue

        matches = by_key.get(key(label), [])
        unique = {(name, tuple(values[2:6])) for name, values in matches}
        if len(unique) != 1:
            skipped.append((ident, raw_label)); continue
        name, offsets = next(iter(unique))
        authored = authored_offsets.get((key(name), mirror))
        if authored is not None:
            offsets = ((offsets[0], offsets[1], authored[0], authored[1]) if mirror
                       else (authored[0], authored[1], offsets[2], offsets[3]))
        node = ET.Element('sprite', {'id': hex(ident), 'world': '15'})
        attrs = {
            'name': name,
            'x1': str(offsets[0]), 'x2': str(offsets[2]),
            'y1': str(offsets[1]), 'y2': str(offsets[3]),
        }
        if floor: attrs['floor'] = '0'
        if mirror: attrs['reverse'] = '1'
        ET.SubElement(node, 'frame', attrs)
        additions.append(node); existing.add(ident)

    text = xml_path.read_text(encoding='utf-8')
    lines = []
    for node in sorted(additions, key=lambda n: int(n.get('id'), 16)):
        rendered = ET.tostring(node, encoding='unicode', short_empty_elements=True)
        lines.append('    ' + rendered)
    insertion = '\n'.join(lines)
    text = text.replace('</spriteslist>', insertion + ('\n' if insertion else '') + '</spriteslist>')
    xml_path.write_text(text, encoding='utf-8')
    print(f'added={len(additions)}, skipped_unverified_or_ambiguous={len(skipped)}')
    for ident, label in skipped[:30]:
        print(f'skipped {ident} ({hex(ident)}): {label}')


if __name__ == '__main__':
    main(Path(sys.argv[1]), Path(sys.argv[2]), Path(sys.argv[3]))
