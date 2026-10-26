"""Refresh English hunting directions from the validated, runtime spawn manifest."""
import jsonimport re
from pathlib import Path
root=Path(__file__).resolve().parents[1]
layout=json.loads((root/'assets/maps/worldmap/hunting-grounds.json').read_text())island_population=next(len(g['points']) for g in layout['grounds'] if g['x']>=3072)monster_rate=int(re.search(r'MULTIPLIER = (\d+)',(root/'src/main/java/com/perso/T4C/monster/core/MonsterExperience.java').read_text()).group(1))server_rate=float(re.search(r'SERVER_XP_RATE = ([\d.]+)f',(root/'src/main/java/com/perso/T4C/config/GameConstants.java').read_text()).group(1))player_cap=int(re.search(r'MAX_PLAYER_LEVEL = (\d+)',(root/'src/main/java/com/perso/T4C/config/GameConstants.java').read_text()).group(1))# The export already includes the combat multiplier and registry hunting curve.monsters={m['name']:m for m in json.loads((root/'compendium/data/monsters.json').read_text(encoding='utf-8'))}def training_xp(tier, family='Tarantula'):    return round(monsters[f'Training {tier} {family}']['xpOnDeath']*server_rate)
path=root/'assets/i18n/lang.json'
lang=json.loads(path.read_text(encoding='utf-8'))
regions={'witness_hunts':[], 'threnody_hunts':[], 'kraanhold_hunts':[], 'mainland_hunts':[]}
for g in layout['grounds']:
    group='threnody_hunts' if g['x']>=5120 else 'witness_hunts' if g['x']>=3072 else 'kraanhold_hunts' if g['x']>=2100 and g['y']>=2300 else 'mainland_hunts'
    regions[group].append(f'{g["monster"]}: ({g["x"]}, {g["y"]})')
for group,entries in regions.items():
    lang['npc.celestial_chronicler.keyword.'+group]=group.replace('_',' ')
    population=island_population if group in ('witness_hunts','threnody_hunts') else 40    lang['npc.celestial_chronicler.'+group]=f'Dense hunting clearings, each with {population} regular enemies: '+ '; '.join(entries)+'. The clearing guards pursue within their hunting district; towns and shrines remain protected.'
lang['npc.celestial_chronicler.keyword.hunting']='hunting'
lang['tooltip.training_return_scroll']='Reusable return to this training ground.\nBound to backpack; survives death and rebirth.\nFive-second cooldown.'
lang['npc.celestial_chronicler.hunting']='There are 40-monster leveling clearings for every new regular monster type. Ask "witness hunts", "threnody hunts", "kraanhold hunts" or "mainland hunts" for coordinates. The Witness Training Grounds need about 150 AC; the Threnody Training Grounds need about 250 AC. Both appear in Locations as soon as their island is unlocked. Their tarantula, skraug and skeleton champions are peaceful until challenged, have high HP and modest melee damage, and always drop a reusable, bound return scroll.'
if '"hunting"' not in lang['npc.celestial_chronicler.welcome']:
    lang['npc.celestial_chronicler.welcome']+=' Ask "hunting" for dense leveling fields and training champions.'
names={1:['Silkbrood Matriarch','Skraug Pitfighter','Boneguard Sentinel'],2:['Ancient Silkbrood','Skraug Ironhide','Ossuary Champion']}

lang['location.hunting.name']='Hunt: %s'
lang['location.training_hunt.name']='Train: %s'
lang['locations.coordinates']='Coordinates: %d, %d, %d'
lang['npc.celestial_chronicler.hunting']=f'Every hunting field appears in Locations. New-island fields contain {island_population} normal enemies, with one larger family boss in each training field. Witness training fields need about 150 AC; Threnody training fields need about 250 AC. Bosses are peaceful until attacked, award at least 100,000 gold and substantial kill XP, and guarantee a reusable bound return scroll for their own field. Training opens with the island, without an additional quest.'
lines=['# Hunting fields and family bosses','',
       f'Witness Isles and Threnody fields have {island_population} regular enemies each; mainland and Kraanhold fields retain 40. Training fields also have one larger boss of the same family. All fields appear in Locations; island fields require only the corresponding island unlock. Normal packs refill in 8–12 seconds. Existing wilderness encounters remain. Coordinates are world-0 tiles.','',
       '| Field | Coordinates | Population |','|---|---|---|']challenge_fields=[f'{g["monster"]} (level {monsters[g["monster"]]["level"]})' for g in layout['grounds'] if monsters[g['monster']]['level']>player_cap]if challenge_fields:    lines[2]+=' '+', '.join(challenge_fields)+' are above-cap challenges, not normal leveling farms.'
for g in layout['grounds']:
    tier=g.get('trainingTier',0);family=g.get('family');slug='hunting_'+g['monster'].lower().replace(' ','_')
    population=len(g['points'])    lang['location.'+slug+'.desc']=f'A connected hunting field with {population} {g["monster"].lower()} enemies.'    monster_level=monsters[g['monster']]['level']    if monster_level>player_cap:        lang['location.'+slug+'.desc']+=f' Level {monster_level} enemies exceed the player level cap. This is a challenge field, not a normal leveling farm. AC alone does not protect against spells.'
    if tier:
        prefix='Witness' if tier==1 else 'Threnody';k=family.lower()
        scroll=f'training_ground_{tier}'+('' if family=='Tarantula' else '_'+k)+'_return_scroll'
        spell=f'item_training_{tier}_{k}_return'
        title=f'{prefix} {family} Field'
        lang['item.'+scroll]='Scroll of Return: '+title
        lang['spell.'+spell]='Return to '+title
        lang['spell.description.'+spell]='Returns you to '+title+'. Reusable; remains in your backpack through death and rebirth.'
        lang[f'monster.training_{tier}_{k}']=names[tier][['Tarantula','Skraug','Skeleton'].index(family)]
        lang[f'monster.training_{tier}_{k}_pack']=prefix+' '+family
        lang['location.'+slug+'.desc']=f'{population} normal {family.lower()}s and one larger {family.lower()} boss. About {150 if tier==1 else 250} AC is sufficient for the intended training tier. The boss guarantees a return scroll, {"100,000–125,000" if tier==1 else "200,000–250,000"} gold and {training_xp(tier, family):,} XP before personal bonuses.'
    lines.append(f'| {g["monster"]} | ({g["x"]}, {g["y"]}) | {population}'+(' + 1 boss' if tier else '')+' |')
lines+=['','## Training rewards','',
        '| Island | Intended AC | Normal HP | Boss HP | Boss gold | Boss XP before personal bonuses | Boss respawn |',
        '|---|---|---|---|---|---|---|',
        f'| Witness | 150 | 5,000 | 100,000 | 100,000–125,000 | {training_xp(1):,} | 30 seconds |',
        f'| Threnody | 250 | 15,000 | 250,000 | 200,000–250,000 | {training_xp(2):,} | 45 seconds |','',
        f'Each family boss guarantees its own field’s reusable return scroll. Scrolls are weightless, bound to the backpack, and survive death and rebirth. Duplicate unique drops can be collected; unique marks rarity, not an ownership limit. Bosses are 1.6 times normal size and remain peaceful until challenged. Monster combat XP is multiplied by {monster_rate} before the server rate ({server_rate:g}x) and personal bonuses. Quest XP is unchanged. Low dodge, low AC and neutral spell resistances support melee, bows and spells. Neither packs nor bosses drop healing potions or mana elixirs.','',
        'The reproducible manifest is `assets/maps/worldmap/hunting-grounds.json`. To recreate the layout, run `HuntingGroundsBuilder`, then `TrainingPackBuilder`, then `IslandHuntingExpansion --apply`, then `tools/update_hunting_guide.py`. Builders use native walkability, connected terrain and quest/NPC buffers.','']
path.write_text(json.dumps(lang,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
(root/'docs/hunting-grounds.md').write_text('\n'.join(lines),encoding='utf-8')
print('Updated all hunting destinations, family names and return-scroll text.')
