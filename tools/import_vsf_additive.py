"""Import every sprite from one decrypted VSF into an existing sprites.bin,
additively: VSF sprites replace same-name entries, everything else is kept.

Usage: py import_vsf_additive.py <decrypted.vsf> <sprites.bin> [<V2ColorI.dpd>] [--prefix P] [--exclude-dirs Dir1,Dir2]
"""
import sys
from pathlib import Path
import merge_vsf_to_sprite_bin as M

def main(vsf_path, bin_path, dpd_path, prefix=None, exclude_dirs=None):
    vsf = Path(vsf_path)
    bin_path = Path(bin_path)
    b, entries, vsf_pals = M.read_vsf(vsf)
    if prefix:
        entries = [e for e in entries if e[0].startswith(prefix)]
    if exclude_dirs:
        dirs = M.read_vsf_folder_tree(vsf)
        excluded = M.excluded_folder_ids(dirs, exclude_dirs)
        before = len(entries)
        entries = [e for e in entries if e[12] not in excluded]
        print(f'excluded_dirs={exclude_dirs} excluded_folder_ids={len(excluded)} '
              f'sprites_skipped={before - len(entries)}')
    # palette table expected by palette_for(): name -> (id, colors, priority)
    palettes = {name: (pid, colors, 1) for name, (pid, colors) in vsf_pals.items()}
    dpd = Path(dpd_path)
    if dpd.is_file():
        for name, colors in M.read_dpd(dpd):
            palettes.setdefault(name.casefold(), (-1, colors, 0))

    original = M.read_sprite_bin(bin_path)
    by_key = {key: record for key, name, record in original}
    order = [key for key, name, record in original]

    replaced = added = 0
    for e in entries:
        key = e[0].casefold()
        record = M.encode_vsf_sprite(e, b, palettes)
        if key in by_key:
            replaced += 1
        else:
            order.append(key)
            added += 1
        by_key[key] = record

    output = [by_key[key] for key in order]
    M.write_sprite_bin(bin_path, output)
    print(f'vsf_sprites={len(entries)} replaced={replaced} added={added} '
          f'total={len(output)} output={bin_path}')

if __name__ == '__main__':
    a = sys.argv[1:]
    prefix = None
    if '--prefix' in a:
        i = a.index('--prefix'); prefix = a[i + 1]; del a[i:i + 2]
    exclude_dirs = None
    if '--exclude-dirs' in a:
        i = a.index('--exclude-dirs'); exclude_dirs = a[i + 1].split(','); del a[i:i + 2]
    dpd = a[2] if len(a) > 2 else r'C:\T4C\T4C TOOLS\T4C_V1R7X\GoN\bin\client\Game Files\V2ColorI.dpd'
    main(a[0], a[1], dpd, prefix, exclude_dirs)
