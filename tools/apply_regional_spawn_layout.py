"""Apply the validated world-detail manifest to authored hunting spawn annotations.

Run after RegionalWorldDetailBuilder --apply. Bosses, gods, NPCs and travel links
retain their authored positions. Backups are retained under target/world-detail-backup.
"""
import json
import re
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CLASSES = {
    "Fey Warden": "FeyWarden", "Moonlit Stalker": "MoonlitStalker",
    "Veilbound Wraith": "VeilboundWraith", "Sundered Sentinel": "SunderedSentinel",
    "Moonwake Revenant": "MoonwakeRevenant", "Emberglass Ashguard": "EmberglassAshguard",
    "Ashbound Exile": "AshboundExile", "Nullguard": "Nullguard", "Rift Wraith": "RiftWraith",
    "Barrow Wight": "BarrowWight", "Lesser Drake": "LesserDrake", "Greater Drake": "GreaterDrake",
}

def main():
    layout = json.loads((ROOT / "assets/maps/worldmap/world-detail-layout.json").read_text())
    changes = []
    for name, cls in CLASSES.items():
        points = [s for s in layout["hunting"] if s["type"] == name]
        if not points:
            raise ValueError(f"Missing validated spawn points for {name}")
        if name == "Moonlit Stalker":
            # Preserve the three existing Verdant Warden escorts outside the Vigil hunt area.
            points += [dict(x=x, y=y, aggressive=True)
                       for x, y in ((4150, 1595), (4170, 1595), (4160, 1613))]
        path = ROOT / f"src/main/java/com/perso/T4C/monster/{cls}.java"
        source = path.read_text()
        source, count = re.subn(r'@Spawn\([^)]*\)\s*', '', source, flags=re.S)
        if count == 0:
            raise ValueError(f"No spawn declarations in {path}")
        lines = '\n'.join(
            f'@Spawn(type = "{name}", x = {s["x"]}, y = {s["y"]}, z = 0, stationary = false, aggressive = {str(s["aggressive"]).lower()})'
            for s in points)
        at = source.index('@com.perso.T4C.monster.core.NoRecoveryLoot')
        changes.append((path, source[:at] + lines + '\n' + source[at:]))
    backup = ROOT / "target/world-detail-backup/spawns"
    backup.mkdir(parents=True, exist_ok=True)
    for path, source in changes:
        if not (backup / path.name).exists():
            shutil.copy2(path, backup / path.name)
        path.write_text(source, newline='\n')
    print(f'Applied {len(layout["hunting"])} hunting points across {len(changes)} monster types.')

if __name__ == '__main__':
    main()
