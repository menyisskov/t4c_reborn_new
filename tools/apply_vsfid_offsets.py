import os
import re
import struct
import sys
import xml.etree.ElementTree as ET
import zlib
from pathlib import Path


def key(name):
    return re.sub(r'[^a-z0-9]+', '', name.lower())


def read_pack(path):
    raw = path.read_bytes()
    wrapped = raw.startswith(b'T4CBIN\x01\x01')
    data = zlib.decompress(raw[8:]) if wrapped else raw
    if data[:4] != b'T4C1':
        raise ValueError('Invalid T4C sprite pack')
    version, count = struct.unpack_from('>II', data, 4)
    p = 12; entries = []
    for _ in range(count):
        n, = struct.unpack_from('>I', data, p); p += 4
        name_raw = data[p:p+n]; p += n
        name = name_raw.decode('utf-8')
        values = list(struct.unpack_from('>8i', data, p)); p += 32
        png = data[p:p+values[7]]; p += values[7]
        entries.append([name, values, png])
    return version, entries, wrapped


def xml_offsets(path):
    result = {}
    for frame in ET.parse(path).getroot().findall('.//frame'):
        name = frame.get('name')
        if not name or '&x' in name or '&y' in name:
            continue
        attrs = [frame.get(a) for a in ('x1', 'y1', 'x2', 'y2')]
        if any(v is None for v in attrs):
            continue
        result.setdefault(key(name), set()).add(tuple(map(int, attrs)))
    return result


def write_pack(path, version, entries, wrapped):
    out = bytearray(b'T4C1' + struct.pack('>II', version, len(entries)))
    for name, values, png in entries:
        raw_name = name.encode('utf-8')
        out += struct.pack('>I', len(raw_name)) + raw_name
        values[7] = len(png)
        out += struct.pack('>8i', *values) + png
    payload = b'T4CBIN\x01\x01' + zlib.compress(bytes(out), 9) if wrapped else bytes(out)
    temp = path.with_suffix(path.suffix + '.tmp')
    temp.write_bytes(payload)
    os.replace(temp, path)


def main(xml_path, pack_path):
    offsets = xml_offsets(xml_path)
    version, entries, wrapped = read_pack(pack_path)
    changed = matched = ambiguous = 0
    for entry in entries:
        candidates = offsets.get(key(entry[0]))
        if not candidates:
            continue
        matched += 1
        if len(candidates) != 1:
            ambiguous += 1
            continue
        x1, y1, x2, y2 = next(iter(candidates))
        old = tuple(entry[1][2:6])
        new = (x1, y1, x2, y2)
        if old != new:
            entry[1][2:6] = new
            changed += 1
    write_pack(pack_path, version, entries, wrapped)
    print(f'matched={matched}, changed={changed}, ambiguous={ambiguous}, total={len(entries)}')


if __name__ == '__main__':
    main(Path(sys.argv[1]), Path(sys.argv[2]))
