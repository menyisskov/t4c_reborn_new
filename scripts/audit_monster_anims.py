#!/usr/bin/env python3
"""Compare Java MonsterDef animations with original T4C VisualObjectList + sprite bins."""
from __future__ import annotations

import pathlib
import re
import struct
import zlib

ROOT = pathlib.Path(__file__).resolve().parents[1]
VOL = pathlib.Path(r"c:\T4C\t4c_client_linux_ref\client\T4C Client\VisualObjectList.cpp")
LISTING = pathlib.Path(r"c:\T4C\t4c_client_linux_ref\client\T4C Client\ObjectListing.h")
MONSTER_DIR = ROOT / "src/main/java/com/perso/T4C/monster"
SPRITES = ROOT / "assets/sprites"


def letter(n: int) -> str:
    return chr(ord("a") + n - 1) if n > 0 else ""


def expected_patterns(base: str, walk: int, attack: int, death: int) -> tuple[str, str, str]:
    w = f"{base}#{letter(walk)}" if walk > 1 else (f"{base}#{letter(walk)}" if walk == 1 else "")
    if walk == 1:
        w = f"{base}#a"
    a = f"{base}A#{letter(attack)}" if attack > 1 else (f"{base}A#a" if attack == 1 else "")
    if death > 1:
        d = f"{base}C!{letter(death)}"
    elif death == 1:
        d = f"{base}C!a"
    else:
        d = ""
    return w, a, d


def parse_ids() -> dict[str, int]:
    ids = {}
    for m in re.finditer(
        r"const unsigned short (__MONSTER_[A-Z0-9_]+)\s*=\s*(\d+);", LISTING.read_text(encoding="utf-8", errors="ignore")
    ):
        ids[m.group(1)] = int(m.group(2))
    return ids


def parse_set_map() -> dict[int, int]:
    """appearance id -> LoadObject set index."""
    ids = parse_ids()
    text = VOL.read_text(encoding="utf-8", errors="ignore")
    # The compact mapping near Set = N
    out = {}
    for m in re.finditer(r"case (__MONSTER_[A-Z0-9_]+)\s*:\s*Set\s*=\s*(\d+)", text):
        name, set_id = m.group(1), int(m.group(2))
        if name in ids:
            out[ids[name]] = set_id
    return out


def parse_load_sprite() -> dict[int, tuple[str, int, int, int]]:
    """set index -> (base, walk, attack, death)."""
    text = VOL.read_text(encoding="utf-8", errors="ignore")
    load_fn = text[text.find("void VisualObjectList::LoadObject") :]
    load_fn = load_fn[: load_fn.find("\nvoid ")]
    out = {}
    current = None
    for line in load_fn.splitlines():
        cm = re.search(r"case\s+(\d+)\s*:", line)
        if cm:
            current = int(cm.group(1))
        if current is None or "LoadSprite3D" not in line:
            continue
        # 9-arg: (walk, attack, sta, stm, death, "Name"
        m9 = re.search(
            r"LoadSprite3D\s*\(\s*(\d+)\s*,\s*(\d+)\s*,\s*(\d+)\s*,\s*(\d+)\s*,\s*(\d+)\s*,\s*\"([^\"]+)\"",
            line,
        )
        if m9:
            walk, attack, _sta, _stm, death, name = (
                int(m9.group(1)),
                int(m9.group(2)),
                int(m9.group(3)),
                int(m9.group(4)),
                int(m9.group(5)),
                m9.group(6),
            )
            out[current] = (name, walk, attack, death)
            continue
        # 4-arg: (walk, stm, death, "Name"
        m4 = re.search(r"LoadSprite3D\s*\(\s*(\d+)\s*,\s*(\d+)\s*,\s*(\d+)\s*,\s*\"([^\"]+)\"", line)
        if m4:
            walk, _stm, death, name = int(m4.group(1)), int(m4.group(2)), int(m4.group(3)), m4.group(4)
            out[current] = (name, walk, 0, death)
    return out


def appearance_catalog() -> dict[int, tuple[str, int, int, int]]:
    sets = parse_set_map()
    loads = parse_load_sprite()
    cat = {}
    for appear, set_id in sets.items():
        if set_id in loads:
            cat[appear] = loads[set_id]
    return cat


def load_sprite_names() -> set[str]:
    names = set()
    for path in sorted(SPRITES.glob("sprites_*.bin")):
        raw = path.read_bytes()
        if not raw.startswith(b"T4CBIN"):
            continue
        data = zlib.decompress(raw[8:])
        off = 4
        ver, count = struct.unpack_from(">II", data, off)
        off += 8
        for _ in range(count):
            nlen = struct.unpack_from(">I", data, off)[0]
            off += 4
            name = data[off : off + nlen].decode("utf-8", "replace")
            off += nlen
            off += 32 + struct.unpack_from(">8I", data, off - 32 + 28)[0] if False else 0
            # re-read pnglen properly
            w, h, o1x, o1y, o2x, o2y, typ, pnglen = struct.unpack_from(">8I", data, off)
            off += 32 + pnglen
            if "Shd" not in name:
                names.add(name)
    return names


def load_sprite_names_fixed() -> set[str]:
    names = set()
    for path in sorted(SPRITES.glob("sprites_*.bin")):
        raw = path.read_bytes()
        if not raw.startswith(b"T4CBIN"):
            continue
        data = zlib.decompress(raw[8:])
        off = 4
        _ver, count = struct.unpack_from(">II", data, off)
        off += 8
        for _ in range(count):
            nlen = struct.unpack_from(">I", data, off)[0]
            off += 4
            name = data[off : off + nlen].decode("utf-8", "replace")
            off += nlen
            _w, _h, _a, _b, _c, _d, _t, pnglen = struct.unpack_from(">8I", data, off)
            off += 32 + pnglen
            if "Shd" not in name:
                names.add(name)
    return names


def parse_java_defs() -> list[dict]:
    defs = []
    ctor = re.compile(
        r"return new MonsterDef\(\s*"
        r"\"([^\"]+)\""  # name
        r".*?"
        r"(\d{4,5}L?)"  # respawn-ish not reliable
        ,
        re.S,
    )
    anim_re = re.compile(
        r"return new MonsterDef\(\s*"
        r"\"(?P<name>[^\"]+)\"\s*,\s*"
        r"(?P<display>[^,]+),\s*"
        r"(?P<rest>.*?)\s*\)\s*;",
        re.S,
    )
    for path in MONSTER_DIR.glob("*.java"):
        text = path.read_text(encoding="utf-8", errors="ignore")
        m = anim_re.search(text)
        if not m:
            continue
        rest = m.group("rest")
        # first 3 quoted strings after numeric block: walk, attack, death
        strings = re.findall(r"\"([^\"]*)\"", rest)
        # skip SOUND_* which are identifiers not strings typically
        # strings after display: walk, attack, death are first 3 string literals in rest
        # rest starts with health ints then 30000L then walk strings
        if len(strings) < 3:
            continue
        walk, attack, death = strings[0], strings[1], strings[2]
        # appearance: first 1xxxx or 2xxxx that is not 30000
        nums = [int(n) for n in re.findall(r"\b(\d{4,5})\b", rest)]
        appear = 0
        for n in nums:
            if 10000 <= n <= 21000:
                appear = n
                break
        defs.append(
            {
                "file": path,
                "name": m.group("name"),
                "walk": walk,
                "attack": attack,
                "death": death,
                "appearance": appear,
            }
        )
    return defs


def pattern_ok(pattern: str, names: set[str]) -> bool:
    if not pattern:
        return True
    if "#" in pattern:
        base, stop = pattern.split("#", 1)
        # angled walk/attack
        probe = f"{base}000-a"
        return probe in names
    if "!" in pattern:
        base, stop = pattern.split("!", 1)
        return f"{base}-a" in names
    if "@" in pattern:
        return True
    probe = f"{pattern}000-a"
    return probe in names or f"{pattern}-a" in names


def main() -> None:
    cat = appearance_catalog()
    names = load_sprite_names_fixed()
    defs = parse_java_defs()
    print(f"catalog appearances={len(cat)} java_defs={len(defs)} sprites={len(names)}")
    mismatches = []
    for d in defs:
        appear = d["appearance"]
        if appear not in cat:
            continue
        base, walk_n, atk_n, death_n = cat[appear]
        exp_w, exp_a, exp_d = expected_patterns(base, walk_n, atk_n, death_n)
        # if attack sprites missing, keep walk as attack
        if exp_a and not pattern_ok(exp_a, names):
            exp_a = exp_w
        if exp_w and not pattern_ok(exp_w, names):
            continue  # can't fix without sprites
        if exp_d and not pattern_ok(exp_d, names):
            # try angled death
            alt = f"{base}C#{letter(death_n)}" if death_n else ""
            if alt and pattern_ok(alt, names):
                exp_d = alt
        cur = (d["walk"], d["attack"], d["death"])
        exp = (exp_w, exp_a or d["attack"], exp_d or d["death"])
        def base_of(p: str) -> str:
            p = p.split("#")[0].split("!")[0].split("@")[0]
            return re.sub(r"A$", "", p[:-1] if p.endswith("C") else p)

        walk_base_wrong = base_of(cur[0]) != base and cur[0] not in ("", base)
        death_wrong = bool(exp_d) and cur[2] != exp[2]
        attack_wrong = bool(exp_a) and cur[1] not in (exp_a, exp_w, "")
        if walk_base_wrong or death_wrong or attack_wrong:
            mismatches.append((d, exp, cat[appear], walk_base_wrong, attack_wrong, death_wrong))
    print(f"mismatches={len(mismatches)}")
    walk_n = sum(1 for x in mismatches if x[3])
    print(f"walk-base-wrong={walk_n}")
    for d, exp, src, wb, ab, db in mismatches:
        print(f" flags walk={wb} atk={ab} death={db}")
        print(
            f"{d['file'].name:40} appear={d['appearance']} orig={src[0]}\n"
            f"  have  {d['walk']!r} {d['attack']!r} {d['death']!r}\n"
            f"  want  {exp[0]!r} {exp[1]!r} {exp[2]!r}"
        )


if __name__ == "__main__":
    main()
