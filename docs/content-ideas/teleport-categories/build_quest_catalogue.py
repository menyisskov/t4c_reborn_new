"""Build staged quest waypoints from checked-in spawn declarations, never invented coordinates.

Run from the repository root. This updates the design preview, not live game assets.
Source coordinates identify a target; final walkable arrival tiles are a separate install check.
"""
import json
import re
from pathlib import Path

ROOT = Path.cwd()
JAVA = ROOT / 'src/main/java/com/perso/T4C'
OUT = ROOT / 'docs/spell-animations'
LANG = json.loads((ROOT / 'assets/i18n/lang.json').read_text(encoding='utf-8'))
ROUTES = {'Good Seraph Quest': [], 'Evil Seraph Quest': [], 'Olin Haad Quest': []}


def add(category, chapter, file, purpose, *, package='npc', name=None, fallback=None):
    path = JAVA / package / (file + '.java')
    text = path.read_text(encoding='utf-8')
    points = []
    declarations = re.findall(r'@Spawn\((.*?)\)', text, re.S)
    declarations.sort(key=lambda declaration: f'type = "{file}"' not in declaration)
    for declaration in declarations:
        numbers = [re.search(r'\b' + axis + r'\s*=\s*(\d+)', declaration) for axis in ('x', 'y', 'z')]
        if all(numbers):
            point = tuple(int(number.group(1)) for number in numbers)
            if point[:2] != (0, 0) and point not in points:
                points.append(point)
    if name is None:
        display = re.search(r'DISPLAY_NAME\s*=\s*\"\$\{([^}]+)\}', text)
        name = LANG.get(display.group(1)) if display else None
        name = name or re.sub(r'(?<=[a-z])(?=[A-Z])', ' ', file)
    warning = None
    source = str(path.relative_to(ROOT)).replace('\\', '/')
    if not points and fallback:
        other = JAVA / fallback
        s = other.read_text(encoding='utf-8')
        for declaration in re.findall(r'@Spawn\((.*?)\)', s, re.S):
            values = [re.search(r'\b' + a + r'\s*=\s*(\d+)', declaration) for a in ('x', 'y', 'z')]
            if all(values):
                point = tuple(int(v.group(1)) for v in values)
                if point[:2] != (0, 0):
                    points.append(point)
                    break
        source += ' + ' + str(other.relative_to(ROOT)).replace('\\', '/')
        warning = 'The monster location is verified; the scripted NPC still has an unplaced spawn. Quest behavior needs repair before this stop can be enabled.'
    point = points[0] if points else (None, None, None)
    if not points:
        warning = 'No placed spawn in the current game. This stop stays unavailable until its location is repaired and checked.'
        if package == 'monster':
            canonical = re.search(r'new MonsterDef\(\s*"([^"]+)"', text)
            if canonical:
                for group in (JAVA/'spawn/definition').glob('SpawnGroup*.java'):
                    group_text = group.read_text(encoding='utf-8')
                    member_list = re.search(r'List.of\(([^)]*)\)', group_text)
                    if member_list and f'"{canonical.group(1)}"' in member_list.group(1):
                        legacy = re.search(r'new SpawnGroup.SpawnPoint\((\d+),\s*(\d+),\s*(\d+)\)', group_text)
                        if legacy:
                            point = tuple(map(int,legacy.groups()))
                            source += ' + ' + str(group.relative_to(ROOT)).replace('\\','/')
                            warning = 'Legacy spawn-group anchor only. The live SpawnRegistry does not load these groups; this monster needs an active spawn before travel is enabled.'
                            break
    row = dict(id=file, name=name, chapter=chapter, x=point[0], y=point[1], z=point[2],
               note=purpose, source=source, warning=warning)
    ROUTES[category].append(row)
    return row


def npcs(category, chapter, entries):
    for file, purpose in entries:
        add(category, chapter, file, purpose)


GOOD = 'Good Seraph Quest'
EVIL = 'Evil Seraph Quest'
OLIN = 'Olin Haad Quest'
npcs(GOOD, 'Begin the good path', [
    ('NissusHaloseeker', 'Guidance, Soul of Artherk and Fangs of True Resolve.'),
    ('BeltiganWhitesword', 'Lightbringer allegiance, Will instructions and final Key of Artherk.')])
npcs(GOOD, 'Soul · Rings of Pure Faith', [
    ('TheoranIronfaith', 'Ring of Pure Faith and Holy Water crafting.'),
    ('BrotherThorkas', 'Ring of Faith and Holy Water crafting.')])
npcs(GOOD, 'Soul · Staves of Hope', [
    ('DionysusSilverstream', 'Staff of Hope crafting.'),
    ('ChryseidaYolangda', 'Symbols of Hope and Mandrakes vendor.')])
npcs(GOOD, 'Soul · Pearls of Wisdom', [
    ('AnnithaeTeardrop', 'Tear-Shaped Pearls vendor.'),
    ('Moonrock', 'Pearl of Wisdom crafting using the Lost Psalms.')])
npcs(GOOD, 'Heart · Gems, Tomes and Swords', [
    ('MithannaSnowraven', 'Heart of Artherk hand-in and Fang referral.'),
    ('OmarHald', 'Gems of Courage from Raw Crystals.'),
    ('Eldantor', 'Magic Scripting Kits vendor.'),
    ('LiurnClar', 'Tomes of Valor crafting.'),
    ('LanceSilversmith', 'Sword of Majesty referral and Hammers of Finality crafting.'),
    ('ThorGlarefire', 'Swords of Majesty crafting and Hammer referral.')])
npcs(GOOD, 'Will · Fangs and Hammers', [
    ('KingAedenasGravesoul', 'Will of Artherk sequence and Red-Eyed Centaurs task.'),
    ('Dragon', 'Dark Fang: Tomb Raider prerequisite and Ruby Dragon Fangs.'),
    ('Samuel', 'Candles of Eternity crafting.'),
    ('TtayhMark', 'Fine Steel Warhammers vendor.'),
    ('Araknor', 'Scroll of Enchantment prerequisite.'),
    ('Iraltok', 'Runic Scripting Kits vendor.'),
    ('MarsacCred', 'Scrolls of Enchantment crafting.'),
    ('RylethCth', 'Plain Blank Scrolls vendor.'),
    ('RoenGreenleaf', 'Optional Ring of the Seraph.')])
npcs(GOOD, 'Will · Drums and Nether Isles', [
    ('AnrakBrownbark', 'Drums of Fate crafting.'),
    ('Menark', 'Hourglasses of Essence exchange.'),
    ('BastienMilandra', 'Ethereal Key repair if required.'),
    ('EtherealPortal', 'Nether Isles entrance; use the quest portal normally.'),
    ('Glaenshenmilandira', 'Will of Artherk hand-in in the Nether Isles.')])

npcs(EVIL, 'Begin the evil path', [
    ('Julian', 'Mordenthal referral, Blood instructions and Spider Venom crafting.'),
    ('Mordenthal', 'Dark Knight allegiance, Bones investigation and final Key of Ogrimar.')])
npcs(EVIL, 'Skull · Crafting and temple chest', [
    ('ZionThunderheart', 'Skull of Ogrimar hand-in.'),
    ('TrackerOoglaThraaglurh', 'Skull of Evil crafting.'),
    ('ShamanWeethgwotha', 'Scrolls of Hate and Alchemy Kits vendor.'),
    ('ChryseidaYolangda', 'Mandrakes vendor.'),
    ('MeltarWinterstorm', 'Gem donations and Crown of Corruption.'),
    ('Zhakar', 'Summon Sweepers for Arcane Scrolls; craft Arcane Spellbooks.'),
    ('VargusChest', 'Skull of Ogrimar chest; approach the chest before speaking to Vargus.')])
npcs(EVIL, 'Blood · Crafting', [
    ('Vargus', 'Blood of Ogrimar hand-in.'),
    ('WitchDoctorKwarlgloth', 'Demonic Blood crafting.'),
    ('Xanth', 'Foul Potions and Ancient Ring identification.'),
    ('Araknor', 'Necromantic Scrolls crafting.')])
npcs(EVIL, 'Bones · Ancient Ring investigation', [
    ('Khimtesar', 'Gateway teacher landmark for the cavern approach.'),
    ('KhimtesarWell', 'Well giving directions toward the Bones cavern.'),
    ('Drardos', 'Ancient Ring investigation.'),
    ('BrotherThorkas', 'Ancient Ring identification.'),
    ('Greyleaf', 'Optional Bloodstone Ring source.')])
add(EVIL, 'Bones · Ancient Ring investigation', 'Venadar', 'Frightening Beast encounter and Ancient Ring.',
    name='Venadar · A Frightening Beast', fallback='monster/VENADAR.java')

farms_good = [
    ('r184Pilferer', 'Pilferers', 'Lost Psalms pages and other quest materials.'),
    ('CentaurAvenger', 'Centaur Avengers', 'Grails of Purity.'),
    ('DemonTree', 'Demon Trees', 'Demon Tree Wood.'),
    ('SKRAUGGRUBBRINGAH', 'Kahp Leth Skraugs', 'Raw Crystals, Blades of Heroism and Human Bones.'),
    ('SKRAUGMOONFIREDANSAH', 'Skraug Mages · Moon Tug', 'Blank Magical Tomes and Red Spellbooks.'),
    ('SKRAUGTOTEMHERBDOKTOR', 'Skraug Mages · Kahp Leth', 'Blank Magical Tomes and Red Spellbooks.'),
    ('ChaosSpawn', 'Chaos Spawns', 'Chaos Keys.'),
    ('r237Tarantula', 'Tarantulas', 'Tarantula Eyes.'),
    ('Grott', 'Grott', 'Jade Ring of Sorcery.'),
    ('DeathStinger', 'Death Stingers', 'Wasp Wax.'),
    ('ColonySentinel', 'Colony Sentinels', 'Wasp Wax.'),
    ('r269WaspDrone', 'Wasp Drones', 'Wasp Wax.'),
    ('GiantWasp', 'Giant Wasps', 'Wasp Wax.'),
    ('DarkWarlord', 'Dark Warlords', 'Gems of the Immortal.'),
    ('Brigand', 'Brigands', 'Finely Crafted Drums: current game monster source.'),
    ('r248TombRaider', 'Tomb Raiders', 'Dark Fang’s prerequisite quest.'),
    ('RedEyedCentaur', 'Red-Eyed Centaurs', 'Mind-Control Talismans; use the Staff of Submission.')]
for file, name, purpose in farms_good:
    add(GOOD, 'Quest material farming', file, purpose, package='monster', name=name)
for file, name, purpose in [
    ('r184Pilferer', 'Pilferers', 'Villain Skulls and Sacrificial Daggers.'),
    ('r160Nightcreeper', 'Nightcreepers', 'Villain Skulls and Sacrificial Daggers.'),
    ('Bloodlust', 'Bloodlust', 'Essence of Bloodlust and Bloodstone Rings.'),
    ('Arachnofiend', 'Arachnofiends', 'Tarantula Fangs.'),
    ('GiantBlackWidow', 'Giant Black Widows', 'Spider Venom.'),
    ('r155Necrospider', 'Necrospiders', 'Spider Venom.')]:
    add(EVIL, 'Quest material farming', file, purpose, package='monster', name=name)

npcs(OLIN, 'Arakas · Letter and merchant investigation', [
    ('Kilhiam', 'Crimsonscale Letter: begin the storyline.'),
    ('Efnisien', 'Assassins, Olin Haad investigation and final temple access.'),
    ('DoorOlinHaad1', 'Olin Haad castle entrance.'),
    ('OlinHaad1', 'Castle dialogue and scripted encounters; return here when instructed.'),
    ('HighPriestGunthar', 'Heartstone investigation and Caledbolg instructions.'),
    ('GuildLeaderEthan', 'Nomad investigation and house key.'),
    ('Urik', 'Merchant debt encounter.'),
    ('DoorNomad1', 'Nomad house entrance.'),
    ('MetalChestNomad1', 'Nomad house search.'),
    ('MetalChestNomad2', 'Nomad house search.'),
    ('MetalChestNomad3', 'Owain Letter search.'),
    ('Yrian', 'Druid camp: stolen staff and crypt directions.'),
    ('Gypsy', 'Nomad directions, Darkstone investigation and final rewards.')])
npcs(OLIN, 'Arakas · Nomad crypt and Darkstone', [
    ('OfferingAltar', 'Nomad crypt altar: use the heart and blood normally.'),
    ('SnakeFountain', 'Cave of Corruption: Blood of Gluriurl.'),
    ('Ribcage', 'Darkstone Crypt: Darkstone objective.'),
    ('BrotherKiran', 'Missing Gunthar investigation.'),
    ('KirlorDhul', 'Bank witness for Gunthar investigation.'),
    ('Amelia', 'Witness directing you to Gunthar’s captors.'),
    ('Tarnian', 'Runed Stone Tablet.'),
    ('Antonian', 'Hermit: Nexus directions and tablet activation.')])
for i in range(1, 16):
    add(OLIN, 'Nexus indexes · Arakas' if i<=5 else 'Nexus indexes · Raven’s Dust',
        f'NexusStone{i}', 'Interact with the stone using the Runed Stone Tablet.', name=f'Nexus Index {i}')
npcs(OLIN, 'Raven’s Dust · Bard and Mausoleum', [
    ('Aquinos', 'Scholar in Silversky: Nameless Bard directions.'),
    ('NamelessBard', 'Bard cave: word-of-power investigation.'),
    ('TreeOfWisdom', 'Branch of Wisdom.'),
    ('FountainOfKnowledge', 'Waters of Knowledge.'),
    ('SanctuaryGuardian', 'Mausoleum gatekeeper; speak the word of power normally.'),
    ('Crimsonscale', 'Dragon: Council of Eight and Great Library instructions.')])
npcs(OLIN, 'Great Library · Riddle and revelations', [
    ('Filandrius', 'Great Library landmark and associated lore.'),
    ('Custodian', 'Rite of Passage riddle and top-floor access.'),
    ('Scholar1', 'Library scholar: riddle translation.'),
    ('Scholar2', 'Library scholar: riddle translation.'),
    ('Scholar3', 'Library scholar: riddle translation.'),
    ('Scholar4', 'Library scholar: riddle translation.'),
    ('Librarian1', 'Top-floor librarian: Olin Haad revelations.'),
    ('Librarian2', 'Top-floor librarian: Darkstone and history.'),
    ('Librarian3', 'Top-floor librarian: Gluriurl revelations.')])
npcs(OLIN, 'Caledbolg · Desert shrine', [
    ('Ramirgo', 'Shrine guardian: enter through the normal quest conversation.'),
    ('Lothar', 'Caledbolg hand-in.')])
npcs(OLIN, 'Final temple · Shards and Burial Chamber', [
    ('DoorGluriurl1', 'Sacred Temple entrance.'),
    ('DoorGluriurl2', 'Sacred Temple doorway.'),
    ('DraconisGuardian', 'Burial Chamber instructions and central temple landmark.'),
    ('BlueShardChest', 'Blue Shard objective.'),
    ('GreenShardChest', 'Green Shard objective.'),
    ('RedShardChest', 'Red Shard objective.')])
for i in range(1,8):
    add(OLIN, 'Final temple · Violet Shard search', f'StrongholdRandomChest{i}',
        'One of the seven possible Violet Shard chests.', name=f'Violet Shard Chest {i}')
add(OLIN, 'Final temple · Shards and Burial Chamber', 'StrongholdToOlinPortal',
    'Burial Chamber portal: collect the shards and use the portal normally.', name='Burial Chamber entrance')
for file,name,purpose,chapter in [
    ('OlinHaadCommander','Commander Owain’s camp','Stolen Solinae Staff encounter; current monster type is Olin Haad Commander.','Arakas · Stolen staff'),
    ('FallenWarrior','Tomb of the Fallen','Corrupted Heart objective.','Arakas · Nomad crypt and Darkstone'),
    ('GorlokBloodaxe','Gorlok Bloodaxe','Gunthar rescue encounter.','Arakas · Gunthar rescue'),
    ('VICARKERVIAN','Vicar Kervian','Purple Crystal Key.','Raven’s Dust · Mausoleum keys'),
    ('VICARRAMIEL','Vicar Ramiel','Green Crystal Key.','Raven’s Dust · Mausoleum keys'),
    ('VICARVHARMES','Vicar Vharmes','Blue Crystal Key.','Raven’s Dust · Mausoleum keys'),
    ('VICARASGOTH','Vicar Asgoth','Red Crystal Key.','Raven’s Dust · Mausoleum keys'),
    ('r241TerrorDemon','Terror Demon','Caledbolg shrine encounter.','Caledbolg · Desert shrine'),
    ('ChaosDemon','Chaos Demon','Caledbolg shrine encounter.','Caledbolg · Desert shrine'),
    ('DarknessDemon','Darkness Demon','Caledbolg shrine encounter.','Caledbolg · Desert shrine'),
    ('KraanianStomper','Yellow Shard hunting','Yellow Shard objective.','Final temple · Shards and Burial Chamber'),
    ('OLINHAAD3','Olin Haad · final encounter','Use Caledbolg at the appropriate quest stage; Gluriurl follows.','Final temple · Olin Haad and Gluriurl')]:
    add(OLIN,chapter,file,purpose,package='monster',name=name)

# Include placeholder entries visibly rather than silently omitting quest steps or inventing tiles.
add(OLIN,'Arakas · Gunthar rescue','HighPriestGunthar2',
    'Gunthar appears after the rescue encounter; use the Gorlok waypoint.', name='Rescued High Priest Gunthar')
ROUTES[OLIN][-1]['warning']='Triggered rescue NPC, not a permanent destination. Travel to Gorlok Bloodaxe and complete the encounter normally.'
ROUTES[OLIN].append(dict(id='gluriurl',name='Gluriurl · God of Corruption',chapter='Final temple · Olin Haad and Gluriurl',
    x=None,y=None,z=None,note='Spawned by the final scripted Olin Haad encounter; use the Burial Chamber entrance.',
    source='src/main/java/com/perso/T4C/npc/OlinHaad3.java',
    warning='Triggered boss, not a permanent spawn. The scripted Olin Haad currently has an unplaced spawn; repair that encounter before enabling a direct boss stop.'))
for category in (GOOD,EVIL):
    chapter='Shared finale · Oracle approach'
    ROUTES[category].append(dict(id='chamber_of_providence',name='Chamber of Providence',chapter=chapter,
        x=None,y=None,z=None,note='Entrance on the Sunken Woods route.',source='NamedLocations Oracle access comments + Bible walkthrough',
        warning='Entrance coordinates are not yet verified. Use Oracle access after obtaining the key.'))
    ROUTES[category].append(dict(id='oracle_access',name='Oracle access · Ivory Chest',chapter=chapter,
        x=2660,y=2610,z=2,note='Requires Key of Artherk or Key of Ogrimar.',source='src/main/java/com/perso/T4C/teleport/NamedLocations.java',warning=None))
    add(category,chapter,'Oracle','Final Oracle conversation; keep the quest and key requirements.')
    add(category,chapter,'GabrielArchonis' if category==GOOD else 'GaenenElthorn',
        'Alignment-specific final test; keep its normal quest requirements.')

for category,rows in ROUTES.items():
    ids=[row['id'] for row in rows]
    assert len(ids)==len(set(ids)),category
    assert all(row['x'] is None or (row['x'],row['y'])!=(0,0) for row in rows)
output=dict(status='source_catalogue_exported_to_runtime',sources=['https://www.t4cbible.com/StoneheimIslandQuest','https://www.t4cbible.com/Addon'],routes=ROUTES)
OUT.mkdir(parents=True,exist_ok=True)
(OUT/'quest-teleport-catalogue.json').write_text(json.dumps(output,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print({category:len(rows) for category,rows in ROUTES.items()})
for category,rows in ROUTES.items():
    for row in rows:
        if row['warning']: print('Needs repair:',category,row['name'],row['warning'])
