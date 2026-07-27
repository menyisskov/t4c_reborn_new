import re, struct, sys, zlib
from collections import Counter
from pathlib import Path
from PIL import Image
from io import BytesIO

def u(b, off, n): return int.from_bytes(b[off:off+n], 'little', signed=False)
def s16(b, off): return int.from_bytes(b[off:off+2], 'little', signed=True)

def read_vsf(path):
    b = path.read_bytes(); index = u(b, 12, 4); count = u(b, index, 4)
    names = []; palettes = {}
    p = index + 4
    for _ in range(count):
        typ = b[p]; off = u(b, p+1, 4); n = u(b, p+5, 2); name = b[p+7:p+7+n].decode('latin1').rstrip('\0'); p += 7+n
        if typ == 1:
            h = b[off:off+32]
            names.append((name, off, u(h,4,1), u(h,6,2), u(h,8,2), s16(h,10), s16(h,12), s16(h,14), s16(h,16), u(h,20,4), u(h,24,1), u(h,28,4), u(h,0,4)))
        else:
            raw = b[off+4:off+4+768]
            palettes[name.lower()] = (u(b, off, 4), [(raw[i], raw[i+1], raw[i+2], 255) for i in range(0,768,3)])
    return b, names, palettes

def read_vsf_folder_tree(path):
    """Returns {folder_id: (parent_id, name)} for the directory tree preceding
    the sprite/palette index (the header lists Sprites/Palettes root folders
    plus every user-visible subfolder, e.g. V2_Floor, V2_Static)."""
    b = Path(path).read_bytes()
    index = u(b, 12, 4)
    pos = 20
    dirs = {}
    while pos < index:
        folder_id = u(b, pos+1, 4)
        parent_id = u(b, pos+5, 4)
        nlen = u(b, pos+9, 2)
        name = b[pos+11:pos+11+nlen].decode('latin1', errors='replace')
        dirs[folder_id] = (parent_id, name)
        pos += 11 + nlen
    return dirs

def excluded_folder_ids(dirs, excluded_names):
    lowered = {name.lower() for name in excluded_names}
    excluded = {folder_id for folder_id, (parent_id, name) in dirs.items() if name.lower() in lowered}
    changed = True
    while changed:
        changed = False
        for folder_id, (parent_id, name) in dirs.items():
            if parent_id in excluded and folder_id not in excluded:
                excluded.add(folder_id)
                changed = True
    return excluded

def pixels(b, e):
    name, off, comp, w, h, x1, y1, x2, y2, typ, key, length, parent = e
    data = b[off+32:off+32+length]; out = bytearray([key])*(w*h)
    shadow = bytearray(w*h)
    if comp == 1: out[:min(len(out),len(data))] = data[:len(out)]
    elif comp == 2:
        pos=y=0
        while pos+5 <= len(data) and y < h:
            x = u(data,pos,2); n = (data[pos+2]<<2)|data[pos+3]; flag=data[pos+4]; pos+=5
            start = max(0, y*w+x); end = min(start+n, len(out))
            if flag == 1:
                if end > start: shadow[start:end] = b'\x01' * (end-start)
            else:
                if end > start: out[start:end] = data[pos:pos+(end-start)]
                pos += n
            if pos >= len(data): break
            marker=data[pos]; pos+=1
            if marker == 2: y += 1
            elif marker == 0: break
    return w,h,x1,y1,x2,y2,typ,key,bytes(out),bytes(shadow)

def read_dpd(path):
    b = Path(path).read_bytes(); unpacked = int.from_bytes(b[16:20], 'little')
    data = bytearray(zlib.decompress(b[41:41+int.from_bytes(b[20:24], 'little')]))
    for i in range(len(data)): data[i] ^= 0x66
    result = []; size = 64 + 768
    for p in range(0, min(len(data), unpacked), size):
        raw_name = data[p:p+64].split(b'\0', 1)[0].decode('latin1').strip()
        if raw_name.endswith('P'): raw_name = raw_name[:-1]
        colors = [tuple(data[p+64+i:p+67+i]) + (255,) for i in range(0, 768, 3)]
        result.append((raw_name, colors))
    return result

def palette_for(name, palettes):
    special = name
    for old, new in (("BlancNoir ", "STuileTmpl1"), ("BlancNoirBig ", "TuileTmpl1"),
                     ("RougeBeige ", "STuileTmpl2"), ("Wooden ", "Floor Wooden"),
                     ("Rock ", "RockFloor"), ("Wooden2 ", "2Wooden"),
                     ("Wooden3 ", "3Wooden"), ("DGrass", "Dgrass"), ("Stone3", "stone3")):
        if old in special: special = new
    lower = special.strip().lower()
    def get(candidate):
        value = palettes.get(candidate.lower())
        if value is None: return None
        return value[1]
    for candidate in (name + 'P', 'P' + name):
        if get(candidate): return get(candidate)
    # Ground/mosaic sprites may carry a tile coordinate suffix, while the
    # palette is registered for the logical base name (e.g. "Town Road Dale").
    base_coord = re.sub(r'\s*\(\s*\d+\s*,\s*\d+\s*\)\s*$', '', name)
    if base_coord != name:
        for candidate in (base_coord + 'P', 'P' + base_coord):
            if get(candidate): return get(candidate)
    for direction in ('000', '045', '090', '135', '180'):
        i = lower.find(direction)
        if i >= 0 and get(name[:i] + 'P'): return get(name[:i] + 'P')
    space = name.find(' ')
    if space >= 0:
        base = name[:space]
        if get(base): return get(base)
        if get(base + 'P'): return get(base + 'P')
    dash = name.find('-')
    if dash >= 0 and get(name[:dash] + 'P'): return get(name[:dash] + 'P')
    # OpenMMO's reference reader uses substring matching after its special
    # name normalization. Prefer the longest palette name to avoid generic
    # palettes winning over object/ground-specific palettes.
    contained = [(n, value[1], value[2]) for n, value in palettes.items() if n in lower]
    if contained:
        return max(contained, key=lambda item: (len(item[0]), item[2]))[1]
    for candidate in ('Bright1', 'bright1'):
        if get(candidate): return get(candidate)
    for palette_name, value in palettes.items():
        colors = value[1]
        if 'bright' in palette_name.lower(): return colors
    return max(palettes.values(), key=lambda value: value[2])[1]

def vsf_number(path):
    match = re.search(r'(\d+)\.vsf$', path.name, re.I)
    return int(match.group(1)) if match else 1

def load_vsf_catalog(src, dpd):
    chosen={}; palettes={}; duplicate_occurrences=0
    files=sorted(Path(src).glob('decrypt*.vsf'), key=vsf_number)
    if not files:
        raise SystemExit(f'No decrypted VSF found in {src}')
    for f in files:
        priority=vsf_number(f)
        b, entries, pals=read_vsf(f)
        for palette_name, (palette_id, colors) in pals.items():
            palettes[palette_name] = (palette_id, colors, priority)
        for e in entries:
            key=e[0].casefold()
            if key in chosen:
                duplicate_occurrences += 1
            chosen[key] = (e,b,f.name,priority)
    dpd_path=Path(dpd)
    if dpd_path.is_file():
        priority=max(vsf_number(f) for f in files)+1
        for palette_name, colors in read_dpd(dpd_path):
            palettes[palette_name.casefold()] = (-1, colors, priority)
    return files, chosen, palettes, duplicate_occurrences

def encode_vsf_sprite(e, b, palettes):
    name=e[0]; w,h,x1,y1,x2,y2,typ,transparent,px,shadow=pixels(b,e)
    pal=palette_for(name, palettes)
    rgba=bytearray()
    for v, is_shadow in zip(px, shadow):
        # Native compression type 2 uses flag 1 as a half-tone black shadow:
        # it clears alternating destination pixels. Alpha 128 is the direct
        # RGBA equivalent and avoids tying the dither phase to screen parity.
        rgba.extend((0,0,0,128) if is_shadow else ((0,0,0,0) if v==transparent else pal[v]))
    expected = max(1, w) * max(1, h) * 4
    rgba.extend(b'\0' * max(0, expected - len(rgba)))
    del rgba[expected:]
    bio=BytesIO(); Image.frombytes('RGBA',(max(1,w),max(1,h),),bytes(rgba)).save(bio,'PNG'); png=bio.getvalue()
    nb=name.encode('utf-8')
    return struct.pack('>I',len(nb))+nb+struct.pack('>8i',w,h,x1,y1,x2,y2,0 if (w,h)==(32,16) else 1,len(png))+png

# The library is split into shards sprites_0.bin, sprites_1.bin, ... each under the 100 MB limit
# imposed by Git/LFS. Every shard is a complete T4C1 v1 payload; the global logical order is their
# concatenation by increasing numeric index. Must stay aligned with SpriteBinIO.java.
SHARD_MAX_BYTES = 90*1024*1024

def shard_paths(path):
    """Files to read for `path`, in order. Falls back to the monolith when there is no shard."""
    path=Path(path); base=path.stem; directory=path.parent
    shards=[]
    for candidate in directory.glob(f'{base}_*.bin'):
        suffix=candidate.stem[len(base)+1:]
        if suffix.isdigit(): shards.append((int(suffix),candidate))
    if shards: return [p for _,p in sorted(shards)]
    return [path] if path.exists() else []

def read_shard(path):
    raw=Path(path).read_bytes()
    data=zlib.decompress(raw[8:]) if raw.startswith(b'T4CBIN'+bytes((1,1))) else raw
    if data[:4] != b'T4C1' or struct.unpack_from('>I',data,4)[0] != 1:
        raise ValueError(f'Unsupported sprites.bin format: {path}')
    count=struct.unpack_from('>I',data,8)[0]; pos=12; records=[]
    for _ in range(count):
        start=pos
        name_len=struct.unpack_from('>I',data,pos)[0]; pos+=4
        name=data[pos:pos+name_len].decode('utf-8'); pos+=name_len
        values=struct.unpack_from('>8i',data,pos); pos+=32+values[-1]
        records.append((name.casefold(),name,data[start:pos]))
    if pos != len(data):
        raise ValueError(f'Unexpected trailing data in {path}: {len(data)-pos} bytes')
    return records

def read_sprite_bin(path):
    records=[]
    for shard in shard_paths(path): records.extend(read_shard(shard))
    return records

def write_shard(dst, records):
    out=bytearray(b'T4C1'+struct.pack('>II',1,len(records)))
    for record in records: out.extend(record)
    compressed=b'T4CBIN'+bytes((1,1))+zlib.compress(out,9)
    tmp=Path(dst).with_suffix('.tmp'); tmp.write_bytes(compressed); tmp.replace(dst)

def write_sprite_bin(dst, records):
    """Writes `records` into shards under SHARD_MAX_BYTES, never splitting an entry."""
    dst=Path(dst); base=dst.stem; directory=dst.parent
    groups=[[]]; size=0
    for record in records:
        if groups[-1] and size+len(record) > SHARD_MAX_BYTES:
            groups.append([]); size=0
        groups[-1].append(record); size+=len(record)
    for index,group in enumerate(groups):
        write_shard(directory/f'{base}_{index}.bin', group)
    # Leftover higher-index shards would be read back as ghost sprites.
    index=len(groups)
    while True:
        stale=directory/f'{base}_{index}.bin'
        if not stale.exists(): break
        stale.unlink(); index+=1
    if dst.exists(): dst.unlink()

def rebuild_duplicates_only(src, dst, dpd):
    original=read_sprite_bin(dst)
    counts=Counter(key for key,name,record in original)
    duplicate_keys={key for key,count in counts.items() if count > 1}
    files, chosen, palettes, _=load_vsf_catalog(src,dpd)
    missing=sorted(duplicate_keys-chosen.keys())
    if missing:
        raise ValueError(f'{len(missing)} duplicate sprite(s) absent from VSF, first: {missing[:5]}')
    emitted=set(); output=[]
    for key,name,record in original:
        if key not in duplicate_keys:
            output.append(record)
        elif key not in emitted:
            e,b,source_name,priority=chosen[key]
            output.append(encode_vsf_sprite(e,b,palettes))
            emitted.add(key)
    write_sprite_bin(dst,output)
    removed=len(original)-len(output)
    print(f'VSF={len(files)} duplicate_groups={len(duplicate_keys)} duplicates_removed={removed} '
          f'unchanged_unique={len(output)-len(duplicate_keys)} output={dst}')

def rebuild_suffix_only(src, dst, dpd, suffix):
    original=read_sprite_bin(dst)
    files, chosen, palettes, _=load_vsf_catalog(src,dpd)
    suffix_key=suffix.casefold()
    selected={key:value for key,value in chosen.items() if key.endswith(suffix_key)}
    if not selected:
        raise ValueError(f'No VSF sprite ends with {suffix!r}')
    emitted=set(); output=[]; replaced=removed_duplicates=0
    for key,name,record in original:
        if not key.endswith(suffix_key):
            output.append(record)
            continue
        if key in emitted:
            removed_duplicates += 1
            continue
        source=selected.get(key)
        if source is None:
            output.append(record)
        else:
            e,b,source_name,priority=source
            output.append(encode_vsf_sprite(e,b,palettes))
            replaced += 1
        emitted.add(key)
    appended=0
    for key,(e,b,source_name,priority) in selected.items():
        if key not in emitted:
            output.append(encode_vsf_sprite(e,b,palettes))
            emitted.add(key); appended += 1
    write_sprite_bin(dst,output)
    print(f'VSF={len(files)} suffix={suffix!r} replaced={replaced} appended={appended} '
          f'duplicates_removed={removed_duplicates} untouched={len(original)-replaced-removed_duplicates} output={dst}')

def main(src, dst, dpd, duplicates_only=False, suffix_only=None):
    dst=Path(dst)
    if suffix_only is not None:
        rebuild_suffix_only(src,dst,dpd,suffix_only)
        return
    if duplicates_only:
        rebuild_duplicates_only(src,dst,dpd)
        return
    files, chosen, palettes, duplicate_occurrences=load_vsf_catalog(src,dpd)
    records=[]
    for key,(e,b,source_name,priority) in chosen.items():
        records.append(encode_vsf_sprite(e,b,palettes))
    write_sprite_bin(dst,records)
    print(f'VSF={len(files)} sprites={len(chosen)} duplicates_removed={duplicate_occurrences} output={dst}')
if __name__=='__main__':
    raw_args=sys.argv[1:]
    suffix_only=None
    if '--suffix-only' in raw_args:
        option=raw_args.index('--suffix-only')
        suffix_only=raw_args[option+1]
        del raw_args[option:option+2]
    args=[arg for arg in raw_args if arg != '--duplicates-only']
    main(args[0],args[1],args[2] if len(args)>2 else r'C:\T4C\T4C TOOLS\T4C_V1R7X\GoN\bin\client\Game Files\V2ColorI.dpd',
         '--duplicates-only' in raw_args, suffix_only)
