import io
import os
import struct
import sys
import zlib
from pathlib import Path

from PIL import Image


def read_pack(path):
    raw = path.read_bytes()
    wrapped = raw.startswith(b'T4CBIN\x01\x01')
    data = zlib.decompress(raw[8:]) if wrapped else raw
    if data[:4] != b'T4C1':
        raise ValueError(f'{path}: invalid sprite pack')
    version, count = struct.unpack_from('>II', data, 4)
    position = 12
    records = []
    for _ in range(count):
        start = position
        name_length, = struct.unpack_from('>I', data, position)
        position += 4
        name = data[position:position + name_length].decode('utf-8')
        position += name_length
        values = struct.unpack_from('>8i', data, position)
        position += 32 + values[7]
        records.append((name, data[start:position]))
    if position != len(data):
        raise ValueError(f'{path}: unexpected trailing bytes')
    return version, wrapped, records


def record_png(record):
    name_length, = struct.unpack_from('>I', record, 0)
    metadata = 4 + name_length
    values = struct.unpack_from('>8i', record, metadata)
    png_start = metadata + 32
    return record[png_start:png_start + values[7]]


def is_visible(record):
    image = Image.open(io.BytesIO(record_png(record))).convert('RGBA')
    return image.getchannel('A').getbbox() is not None


def write_pack(path, version, wrapped, records):
    data = bytearray(b'T4C1' + struct.pack('>II', version, len(records)))
    for _, record in records:
        data.extend(record)
    output = b'T4CBIN\x01\x01' + zlib.compress(bytes(data), 9) if wrapped else bytes(data)
    temporary = path.with_suffix(path.suffix + '.tmp')
    temporary.write_bytes(output)
    os.replace(temporary, path)


def repair(current_path, backup_path, requested_names):
    version, wrapped, current = read_pack(current_path)
    _, _, backup = read_pack(backup_path)
    candidates = {}
    for name, record in backup:
        if name.casefold() in requested_names and is_visible(record):
            # The last valid duplicate has the highest VSF priority.
            candidates[name.casefold()] = (name, record)

    repaired = []
    output = []
    for name, record in current:
        key = name.casefold()
        if key not in requested_names:
            output.append((name, record))
            continue
        if is_visible(record):
            raise ValueError(f'{name}: current sprite is not fully transparent')
        replacement = candidates.get(key)
        if replacement is None:
            raise ValueError(f'{name}: no visible backup candidate')
        output.append(replacement)
        repaired.append(name)

    missing = requested_names - {name.casefold() for name in repaired}
    if missing:
        raise ValueError(f'sprites not repaired: {sorted(missing)}')
    write_pack(current_path, version, wrapped, output)
    print(f'repaired={len(repaired)}: {", ".join(repaired)}')


if __name__ == '__main__':
    if len(sys.argv) < 4:
        raise SystemExit('Usage: repair_transparent_sprites.py CURRENT BACKUP NAME [NAME ...]')
    repair(Path(sys.argv[1]), Path(sys.argv[2]), {name.casefold() for name in sys.argv[3:]})
