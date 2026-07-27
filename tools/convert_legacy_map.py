import struct
import sys
import xml.etree.ElementTree as ET
import re
import zlib
import os
from collections import defaultdict
from pathlib import Path

W = H = 3072
BLOCK = 128
BLOCKS = 24 * 24

LEGACY_SPRITE_ALIASES = {
    'cemetery gates 1': 'Cemetery Gates /^',
    'cemetery gates 2': 'Cemetery Gates /',
    'cemetery gates 3': 'Cemetery Gates \\v',
    # Gate 4 is the three-way junction. Gate 8 is the lower point where the
    # two diagonal runs meet; these two packed aliases used to be swapped.
    'cemetery gates 4': 'Cemetery Gates X',
    'cemetery gates 5': 'Cemetery Gates -',
    # Gate 6 is the upper junction between the two diagonal fence runs. The
    # packed ">" sprite is only a half-section and leaves a visible gap there;
    # the packed "^" sprite is the matching complete junction.
    'cemetery gates 6': 'Cemetery Gates ^',
    # Gate 7 is the mirrored/unmirrored lateral junction used where the two
    # diagonal runs turn back from the left or right edge.
    'cemetery gates 7': 'Cemetery Gates >',
    'cemetery gates 8': 'Cemetery Gates v',
    'cemetery gates 9': 'Cemetery Gates .|',
}

# The packed replacement for the historical "Cemetery Gates 3" sprite has
# the opposite native orientation. VSFID's reverse bit is correct for the
# original artwork, but must be inverted when that name is resolved through
# LEGACY_SPRITE_ALIASES; otherwise both diagonal sides of the cemetery fence
# face the wrong way (IDs 0x0C0E and 0x0C0F).
LEGACY_ALIAS_INVERTED_MIRRORS = {
    'cemetery gates 3',
}

def read_compact(path):
    b = path.read_bytes(); p = 0
    if b[:6] != b'T4CMAP' or struct.unpack_from('<H', b, 6)[0] != 6:
        raise ValueError(f'{path}: expected T4CMAP v6')
    width, height = struct.unpack_from('<II', b, 8); p = 16
    dictionary_count, = struct.unpack_from('<I', b, p); p += 4
    names = ['']
    for _ in range(dictionary_count):
        n, = struct.unpack_from('<I', b, p); p += 4
        names.append(b[p:p+n].decode('utf-8')); p += n
    total = width * height; out = [''] * total
    runs, = struct.unpack_from('<I', b, p); p += 4
    for _ in range(runs):
        start, length, sid = struct.unpack_from('<III', b, p); p += 12
        if sid: out[start:start+length] = [names[sid]] * length
    scales = {}
    scale_count, = struct.unpack_from('<I', b, p); p += 4
    for _ in range(scale_count):
        idx, sx, sy = struct.unpack_from('<Iff', b, p); p += 12
        scales[idx] = (sx, sy)
    offsets = {}
    offset_count, = struct.unpack_from('<I', b, p); p += 4
    for _ in range(offset_count):
        idx, ox, oy = struct.unpack_from('<Iff', b, p); p += 12
        offsets[idx] = (ox, oy)
    z_orders = {}
    z_order_count, = struct.unpack_from('<I', b, p); p += 4
    for _ in range(z_order_count):
        idx, z_order = struct.unpack_from('<Ii', b, p); p += 8
        z_orders[idx] = z_order
    if p != len(b):
        raise ValueError(f'{path}: {len(b) - p} trailing bytes')
    return width, height, out, scales, offsets, z_orders

def read_legacy(path):
    b = path.read_bytes(); offsets = struct.unpack_from('<576I', b, 0)
    ids = [0] * (W * H)
    for bi, off in enumerate(offsets):
        x0, y0 = (bi % 24) * BLOCK, (bi // 24) * BLOCK
        vals = []
        q = off
        while len(vals) < BLOCK * BLOCK:
            v, = struct.unpack_from('<H', b, q); q += 2
            if v < 0x1000:
                vals.append(v)
            else:
                repeat = v - 0x1000
                value, = struct.unpack_from('<H', b, q); q += 2
                vals.extend([value] * repeat)
        if len(vals) != BLOCK * BLOCK:
            raise ValueError(f'{path}: invalid block {bi}')
        for row in range(BLOCK):
            dst = (y0 + row) * W + x0
            ids[dst:dst+BLOCK] = vals[row*BLOCK:(row+1)*BLOCK]
    return ids

def write_compact(path, values, width=W, height=H, scales=None, offsets=None, z_orders=None):
    scales = scales or {}
    offsets = offsets or {}
    z_orders = z_orders or {}
    dictionary = []; index = {}
    for v in values:
        if v and v not in index:
            index[v] = len(dictionary) + 1; dictionary.append(v)
    runs = []
    i = 0
    while i < len(values):
        if not values[i]: i += 1; continue
        sid = index[values[i]]; start = i; i += 1
        while i < len(values) and values[i] == values[start]: i += 1
        runs.append((start, i-start, sid))
    with path.open('wb') as f:
        f.write(b'T4CMAP' + struct.pack('<HII', 6, width, height))
        f.write(struct.pack('<I', len(dictionary)))
        for name in dictionary:
            raw = name.encode('utf-8'); f.write(struct.pack('<I', len(raw))); f.write(raw)
        f.write(struct.pack('<I', len(runs)))
        for run in runs: f.write(struct.pack('<III', *run))
        f.write(struct.pack('<I', len(scales)))
        for idx, (sx, sy) in sorted(scales.items()):
            f.write(struct.pack('<Iff', idx, sx, sy))
        f.write(struct.pack('<I', len(offsets)))
        for idx, (ox, oy) in sorted(offsets.items()):
            f.write(struct.pack('<Iff', idx, ox, oy))
        f.write(struct.pack('<I', len(z_orders)))
        for idx, z_order in sorted(z_orders.items()):
            f.write(struct.pack('<Ii', idx, z_order))

def sprite_pack_shards(path):
    """Files making up the library, in numeric order.

    It is split into sprites_0.bin, sprites_1.bin, ... to stay under the 100 MB Git/LFS limit;
    falls back to the historical monolith when there is no shard.
    """
    base = path.stem; shards = []
    for candidate in path.parent.glob(f'{base}_*.bin'):
        suffix = candidate.stem[len(base)+1:]
        if suffix.isdigit():
            shards.append((int(suffix), candidate))
    if shards:
        return [p for _, p in sorted(shards)]
    return [path] if path.exists() else []

def read_sprite_pack(path):
    entries = []
    for shard in sprite_pack_shards(path):
        entries.extend(read_sprite_pack_shard(shard))
    return entries

def read_sprite_pack_shard(path):
    raw = path.read_bytes()
    data = zlib.decompress(raw[8:]) if raw.startswith(b'T4CBIN\x01\x01') else raw
    if data[:4] != b'T4C1':
        raise ValueError(f'{path}: invalid sprite pack')
    count, = struct.unpack_from('>I', data, 8); p = 12; entries = []
    for _ in range(count):
        n, = struct.unpack_from('>I', data, p); p += 4
        name = data[p:p+n].decode('utf-8'); p += n
        values = struct.unpack_from('>8i', data, p); p += 32 + values[-1]
        entries.append({
            'name': name,
            'width': values[0],
            'height': values[1],
            'offsets': tuple(values[2:6]),
        })
    return entries

def read_sprite_names(path):
    return [entry['name'] for entry in read_sprite_pack(path)]

def normalized_name(name):
    return re.sub(r'[^a-z0-9]+', '', name.lower())

def build_pack_index(sprite_pack):
    entries = read_sprite_pack(sprite_pack)
    exact = {entry['name'].lower(): entry for entry in entries}
    normalized = {}
    for entry in entries:
        normalized.setdefault(normalized_name(entry['name']), entry)
    return exact, normalized

def legacy_palette_base(name):
    match = re.match(r'^(.*?)\s+(MA|GR|BR|BL)$', name.strip(), re.I)
    return match.group(1).strip() if match else None

def packed_sprite_for_name(name, exact, normalized):
    entry = exact.get(name.lower())
    if entry is not None:
        return entry
    alias = LEGACY_SPRITE_ALIASES.get(name.lower())
    if alias is not None:
        entry = exact.get(alias.lower())
        if entry is not None:
            return entry
    palette_base = legacy_palette_base(name)
    if palette_base:
        entry = exact.get(palette_base.lower())
        if entry is not None:
            return entry
    return normalized.get(normalized_name(name))

def read_vsfid(path, sprite_pack):
    exact_entries, normalized_entries = build_pack_index(sprite_pack)
    def actual_name(name):
        exact = exact_entries.get(name.lower())
        if exact is not None:
            return exact['name']
        alias = LEGACY_SPRITE_ALIASES.get(name.lower())
        if alias is not None and alias.lower() in exact_entries:
            return exact_entries[alias.lower()]['name']
        normalized = normalized_entries.get(normalized_name(name))
        return normalized['name'] if normalized is not None else name
    result = {}
    for sprite in ET.parse(path).getroot().findall('.//sprite'):
        ident = int(sprite.get('id', '0'), 16)
        frames = []
        for frame in sprite.findall('frame'):
            raw_name = frame.get('name')
            if not raw_name:
                continue
            reverse = frame.get('reverse') == '1'
            if raw_name.lower() in LEGACY_ALIAS_INVERTED_MIRRORS:
                reverse = not reverse
            frames.append({
                'name': actual_name(raw_name),
                'reverse': reverse,
                'offsets': tuple(int(frame.get(attr, '0')) for attr in ('x1', 'y1', 'x2', 'y2')),
            })
        template = int(sprite.get('template', '0'))
        result[ident] = {
            'frames': frames,
            'ground': template > 0 or any(f.get('floor') is not None for f in sprite.findall('frame')),
            'template': template,
            'mosaic_x': int(sprite.get('mosaic_x', '0')),
            'mosaic_y': int(sprite.get('mosaic_y', '0')),
            'random': sprite.get('random') is not None,
            'id1': int(sprite.get('id1', '0'), 16),
            'id2': int(sprite.get('id2', '0'), 16),
        }
    return result

def selected_frame(entry, x, y):
    if entry is None or entry['template']:
        return None
    frames = entry['frames']
    if not frames:
        return None
    if entry['random']:
        frame_index = (x * 73856093 ^ y * 19349663) % len(frames)
    elif entry['mosaic_x'] > 0 and entry['mosaic_y'] > 0 and len(frames) >= entry['mosaic_x'] * entry['mosaic_y']:
        frame_index = (x % entry['mosaic_x']) * entry['mosaic_y'] + (y % entry['mosaic_y'])
    else:
        frame_index = 0
    frame = frames[frame_index]
    name = frame['name']
    if '&x' in name or '&y' in name:
        mx = max(1, entry['mosaic_x']); my = max(1, entry['mosaic_y'])
        name = name.replace('&x', str(x % mx + 1)).replace('&y', str(y % my + 1))
    return name, frame['reverse'], frame['offsets']

def sprite_name(entry, x, y):
    template = entry['template']
    if template:
        family, variant = divmod(template, 100)
        mask_family = {1: 1, 2: 3, 3: 4}.get(family)
        return f'Tmpl{mask_family} {variant}' if mask_family else None
    frame = selected_frame(entry, x, y)
    if frame is None:
        return None
    name, reverse, _ = frame
    return name + 'M' if reverse else name

def frame_offset_delta(frame, pack_exact, pack_normalized):
    if frame is None:
        return None
    name, reverse, expected = frame
    packed = packed_sprite_for_name(name, pack_exact, pack_normalized)
    if packed is None:
        return None
    expected_x, expected_y = expected[2:4] if reverse else expected[0:2]
    packed_offsets = packed['offsets']
    packed_x, packed_y = packed_offsets[2:4] if reverse else packed_offsets[0:2]
    return float(expected_x - packed_x), float(expected_y - packed_y)

def build_ground_retiler(mapping):
    exact = {}
    patterns = []
    for entry in mapping.values():
        if not entry['ground']:
            continue
        if entry['template']:
            exact.setdefault(sprite_name(entry, 0, 0).lower(), entry)
        for frame in entry['frames']:
            frame_name = frame['name']
            reverse = frame['reverse']
            suffix = 'M' if reverse else ''
            if '&x' in frame_name or '&y' in frame_name:
                pattern = re.escape(frame_name)
                pattern = pattern.replace(re.escape('&x'), r'\d+')
                pattern = pattern.replace(re.escape('&y'), r'\d+')
                patterns.append((re.compile(r'^' + pattern + suffix + r'$', re.I), entry))
            else:
                exact.setdefault((frame_name + suffix).lower(), entry)

    def retile(name, x, y):
        entry = exact.get(name.lower())
        if entry is None:
            entry = next((candidate for pattern, candidate in patterns if pattern.match(name)), None)
        return sprite_name(entry, x, y) if entry is not None else name

    return retile

def concrete_ground_family(name):
    if not name or re.fullmatch(r'[0-9a-f]{4}', name, re.I):
        return None
    if re.match(r'^tmpl\d+\s+\d+$', name, re.I):
        return None
    base = re.sub(r'\s*\(\s*\d+\s*,\s*\d+\s*\)M?$', '', name, flags=re.I)
    base = re.sub(r'\s+\d+M?$', '', base, flags=re.I)
    base = re.sub(r'\s+separationM?$', '', base, flags=re.I)
    return normalized_name(base)

def fill_from_local_concrete_ground(ground, retile):
    # Resolve decor cells touching real terrain before applying the long-range
    # fallback. Tmpl tiles describe a transition and must not vote as terrain.
    # The majority family wins; equal votes use the stable visual priority
    # right, bottom, left, top, then diagonals.
    directions = (
        (1, 0), (0, 1), (-1, 0), (0, -1),
        (1, 1), (-1, 1), (1, -1), (-1, -1),
    )
    updates = []
    for y in range(H):
        row_start = y * W
        for x in range(W):
            idx = row_start + x
            if ground[idx] is not None:
                continue
            families = {}
            for rank, (dx, dy) in enumerate(directions):
                nx, ny = x + dx, y + dy
                if nx < 0 or ny < 0 or nx >= W or ny >= H:
                    continue
                candidate = ground[ny * W + nx]
                family = concrete_ground_family(candidate)
                if family is None:
                    continue
                count, best_rank, source = families.get(family, (0, rank, candidate))
                if rank < best_rank:
                    best_rank, source = rank, candidate
                families[family] = (count + 1, best_rank, source)
            if families:
                _, (_, _, source) = min(
                    families.items(),
                    key=lambda item: (-item[1][0], item[1][1], item[0]))
                updates.append((idx, retile(source, x, y)))
    for idx, name in updates:
        ground[idx] = name

def fill_ground_under_decors(ground, mapping):
    # A legacy cell stores either ground or decor. First infer the terrain from
    # its local 2D context, then fill any remaining interior gaps from the
    # closest horizontal/vertical terrain. Re-resolve mosaics and random floors
    # at the target coordinate instead of copying a neighbour variant verbatim.
    retile = build_ground_retiler(mapping)
    fill_from_local_concrete_ground(ground, retile)
    for y in range(H):
        row_start = y * W
        x = 0
        while x < W:
            if ground[row_start + x] is not None:
                x += 1
                continue
            run_start = x
            while x < W and ground[row_start + x] is None:
                x += 1
            left_x = run_start - 1
            right_x = x
            left_name = ground[row_start + left_x] if left_x >= 0 else None
            right_name = ground[row_start + right_x] if right_x < W else None
            for fill_x in range(run_start, right_x):
                use_right = right_name is not None and (
                    left_name is None or right_x - fill_x <= fill_x - left_x)
                source = right_name if use_right else left_name
                if source is not None:
                    ground[row_start + fill_x] = retile(source, fill_x, y)

    # Only rows containing no original ground remain empty after the horizontal
    # pass. Resolve those gaps vertically, with the bottom terrain winning ties.
    for x in range(W):
        y = 0
        while y < H:
            idx = y * W + x
            if ground[idx] is not None:
                y += 1
                continue
            run_start = y
            while y < H and ground[y * W + x] is None:
                y += 1
            top_y = run_start - 1
            bottom_y = y
            top_name = ground[top_y * W + x] if top_y >= 0 else None
            bottom_name = ground[bottom_y * W + x] if bottom_y < H else None
            for fill_y in range(run_start, bottom_y):
                use_bottom = bottom_name is not None and (
                    top_name is None or bottom_y - fill_y <= fill_y - top_y)
                source = bottom_name if use_bottom else top_name
                if source is not None:
                    ground[fill_y * W + x] = retile(source, x, fill_y)

def main(src, ground_out, decor_out, mapping_path):
    ids = read_legacy(src)
    ground_out.parent.mkdir(parents=True, exist_ok=True)
    mapping = read_vsfid(mapping_path, mapping_path.parent / 'sprites' / 'sprites.bin')
    pack_path = mapping_path.parent / 'sprites' / 'sprites.bin'
    pack_exact, pack_normalized = build_pack_index(pack_path)
    ground = [None] * len(ids); decor = [''] * len(ids); decor_offsets = {}
    unresolved = set(); templates = 0
    for i, ident in enumerate(ids):
        x, y = i % W, i // W
        entry = mapping.get(ident)
        name = sprite_name(entry, x, y) if entry else None
        if not name:
            ground[i] = f'{ident:04X}'; unresolved.add(ident)
        elif entry['ground']:
            ground[i] = name
            templates += int(entry['template'] > 0)
        else:
            decor[i] = name
            delta = frame_offset_delta(selected_frame(entry, x, y), pack_exact, pack_normalized)
            if delta is not None and delta != (0.0, 0.0):
                decor_offsets[i] = delta
    fill_ground_under_decors(ground, mapping)
    write_compact(ground_out, ground)
    write_compact(decor_out, decor, offsets=decor_offsets)
    print(f'IDs={len(set(ids))}, templates={templates}, non résolus={len(unresolved)}, '
          f'offsets décor={len(decor_offsets)}')

def canonical_map_name(name):
    if not name:
        return ''
    value = re.sub(r'@-?\d+,-?\d+$', '', name.strip())
    return re.sub(r'\s+', ' ', value).lower()

def resolve_map_name(name, pack_exact, pack_normalized):
    value = re.sub(r'@-?\d+,-?\d+$', '', (name or '').strip())
    exact = pack_exact.get(value.lower())
    mirror = False
    if exact is None and value.endswith('M'):
        candidate = value[:-1].strip()
        candidate_entry = packed_sprite_for_name(candidate, pack_exact, pack_normalized)
        if candidate_entry is not None:
            value = candidate
            exact = candidate_entry
            mirror = True
    if exact is None:
        exact = packed_sprite_for_name(value, pack_exact, pack_normalized)
    return (exact['name'].lower(), mirror) if exact is not None else (value.lower(), mirror)

def names_match(current_name, expected_name, pack_exact, pack_normalized):
    if canonical_map_name(current_name) == canonical_map_name(expected_name):
        return True
    current_palette = legacy_palette_base(re.sub(r'M$', '', current_name.strip()))
    expected_palette = legacy_palette_base(re.sub(r'M$', '', expected_name.strip()))
    if current_palette and expected_palette:
        current_suffix = current_name.strip().split()[-1].rstrip('M').lower()
        expected_suffix = expected_name.strip().split()[-1].rstrip('M').lower()
        if current_suffix != expected_suffix:
            return False
    return resolve_map_name(current_name, pack_exact, pack_normalized) == resolve_map_name(
        expected_name, pack_exact, pack_normalized)

def build_offset_candidates(mapping, pack_exact, pack_normalized):
    exact_candidates = defaultdict(set)
    packed_candidates = defaultdict(set)
    for entry in mapping.values():
        if entry['ground'] or entry['template']:
            continue
        for frame in entry['frames']:
            mx = max(1, entry['mosaic_x']) if ('&x' in frame['name'] or '&y' in frame['name']) else 1
            my = max(1, entry['mosaic_y']) if ('&x' in frame['name'] or '&y' in frame['name']) else 1
            for x in range(mx):
                for y in range(my):
                    name = frame['name'].replace('&x', str(x + 1)).replace('&y', str(y + 1))
                    concrete = (name, frame['reverse'], frame['offsets'])
                    delta = frame_offset_delta(concrete, pack_exact, pack_normalized)
                    if delta is None:
                        continue
                    raw_name = name + ('M' if frame['reverse'] else '')
                    exact_candidates[canonical_map_name(raw_name)].add(delta)
                    packed_candidates[resolve_map_name(raw_name, pack_exact, pack_normalized)].add(delta)
    return exact_candidates, packed_candidates

def migrate_decor_offsets(src, decor_path, mapping_path, sprite_pack):
    ids = read_legacy(src)
    width, height, decor, scales, offsets, z_orders = read_compact(decor_path)
    if width != W or height != H or len(ids) != len(decor):
        raise ValueError(f'Dimension mismatch: legacy={W}x{H}, decor={width}x{height}')
    mapping = read_vsfid(mapping_path, sprite_pack)
    pack_exact, pack_normalized = build_pack_index(sprite_pack)
    exact_candidates, packed_candidates = build_offset_candidates(mapping, pack_exact, pack_normalized)

    migrated = source_matches = inferred = zero_deltas = preserved = unresolved = 0
    new_offsets = dict(offsets)
    for idx, current_name in enumerate(decor):
        if not current_name:
            continue
        existing = offsets.get(idx, (0.0, 0.0))
        if existing != (0.0, 0.0):
            preserved += 1
            continue
        x, y = idx % width, idx // width
        entry = mapping.get(ids[idx])
        frame = selected_frame(entry, x, y) if entry and not entry['ground'] else None
        delta = None
        if frame is not None:
            expected_name = frame[0] + ('M' if frame[1] else '')
            if names_match(current_name, expected_name, pack_exact, pack_normalized):
                delta = frame_offset_delta(frame, pack_exact, pack_normalized)
                source_matches += int(delta is not None)
        if delta is None:
            candidates = exact_candidates.get(canonical_map_name(current_name), set())
            if len(candidates) != 1:
                candidates = packed_candidates.get(resolve_map_name(current_name, pack_exact, pack_normalized), set())
            if len(candidates) == 1:
                delta = next(iter(candidates))
                inferred += 1
        if delta is None:
            unresolved += 1
        elif delta == (0.0, 0.0):
            zero_deltas += 1
        else:
            new_offsets[idx] = delta
            migrated += 1

    temp = decor_path.with_suffix(decor_path.suffix + '.tmp')
    write_compact(temp, decor, width, height, scales, new_offsets, z_orders)
    os.replace(temp, decor_path)
    print(f'{decor_path}: migrated={migrated}, source={source_matches}, inferred={inferred}, '
          f'zero={zero_deltas}, preserved={preserved}, unresolved={unresolved}')

if __name__ == '__main__':
    if len(sys.argv) == 6 and sys.argv[1] == '--migrate-decor-offsets':
        migrate_decor_offsets(Path(sys.argv[2]), Path(sys.argv[3]), Path(sys.argv[4]), Path(sys.argv[5]))
    elif len(sys.argv) == 5:
        main(Path(sys.argv[1]), Path(sys.argv[2]), Path(sys.argv[3]), Path(sys.argv[4]))
    else:
        raise SystemExit(
            'Usage: convert_legacy_map.py LEGACY_MAP GROUND_OUT DECOR_OUT VSFID_XML\n'
            '   or: convert_legacy_map.py --migrate-decor-offsets LEGACY_MAP CURRENT_DECOR VSFID_XML SPRITES_BIN')
