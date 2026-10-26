"""Rebuild the Divine Concord's equipment, monster entry points, and English names.

Run from the repository root. Combat, scrolls and lore live in the shared Java catalog.
Numeric item IDs 55000-55999 and boost IDs 50000-51999 belong to this content line.
"""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / 'src/main/java/com/perso/T4C'
GODS = [
    # id, name, school, tier, level, x, y, female, robe palette, wing palette
    ('demeter', 'Demeter', 'earth', 1, 240, 3835, 1440, True, 92, 92),
    ('njord', 'Njord', 'water', 1, 250, 3738, 780, False, 93, 93),
    ('ra', 'Ra', 'fire', 1, 260, 4080, 2530, False, 94, 94),
    ('artemis', 'Artemis', 'archer', 1, 270, 4050, 675, True, 92, 92),
    ('athena', 'Athena', 'warrior', 2, 340, 4140, 1432, True, 95, 95),
    ('apollo', 'Apollo', 'light', 2, 350, 3888, 720, False, 97, 97),
    ('anubis', 'Anubis', 'dark', 2, 360, 4465, 2060, False, 96, 96),
    ('thor', 'Thor', 'air', 2, 370, 5540, 1320, False, 95, 95),
    ('artherk', 'Artherk', 'light', 0, 100, 5370, 1660, False, 90, 90),
    ('ogrimar', 'Ogrimar', 'dark', 0, 110, 5880, 2000, False, 91, 91),
]

def write(path, text):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text, encoding='utf-8', newline='\n')

lang_path = ROOT / 'assets/i18n/lang.json'
lang = json.loads(lang_path.read_text(encoding='utf-8'))
boost_id = 50000
slot_ac = {'BODY':31,'FEET':9.28,'LEFT_HAND':9.28,'HEAD':8.94,'LEGS':10.32,
           'BACK':9,'NECK':5,'RING1':5,'BRACER':5,'WEAPON':0}
archetypes = {'fire':'INT_MAGE','water':'INT_MAGE','dark':'INT_MAGE','earth':'WIS_MAGE',
              'light':'WIS_MAGE','air':'HYBRID_MAGE','warrior':'WARRIOR','archer':'ARCHER'}
power = {'air':16,'fire':17,'water':18,'earth':19,'light':23,'dark':24}

for index, (key,name,school,tier,level,x,y,female,robe,wing) in enumerate(GODS):
    base = 55000 + index * 100
    primary, endurance = {0:(180,100),1:(380,220),2:(650,400),3:(900,550)}[tier]
    archetype = archetypes[school]
    physical = school in ('warrior','archer')
    req = {'endurance':endurance}
    if school=='warrior': req['strength']=primary
    elif school=='archer': req['agility']=primary
    elif school=='air': req.update(intelligence=round(primary/1.6),wisdom=round(primary/1.6))
    elif archetype=='INT_MAGE': req['intelligence']=primary
    else: req['wisdom']=primary
    p = (req['intelligence']+req['wisdom'])*.8 if school=='air' else primary
    white_black = key in ('artherk','ogrimar')
    palette = f'__pal{robe}'
    plate = lambda base: base + palette
    # Every god owns a full outfit, wings, a weapon and three accessories.
    pieces = [
      ('weapon','WEAPON', 'PupLongBow' if school=='archer' else 'PupNormalSword' if school=='warrior' else 'PupWoodenStaff',
       '64kInvLongBow' if school=='archer' else '64kInvShortSword' if school=='warrior' else '64kInvWoodenStaff',None,None),
      ('armor' if physical else 'robe','BODY',plate('PupPlateBody') if physical else f'PupWhiteRobe__pal{robe}',
       plate('64kInvPlateArmorSleeves') if physical else f'64kInvWhiteRobe__pal{robe}',None,None),
      ('boots','FEET',plate('PupPlateFoot'),'64kInvPlateArmorFeet'+palette,None,None),
      ('gauntlets','LEFT_HAND',plate('PupPlateGloveL'),'64kInvPlateGlove'+palette,'RIGHT_HAND',plate('PupPlateGloveR')),
      ('helm','HEAD',plate('PupPlateHelm'),'64kInvPlateArmorHelm'+palette,None,None),
      ('leggings','LEGS',plate('PupPlateLegs'),'64kInvPlateArmorLegs'+palette,None,None),
      ('wings','BACK',f'PupSeraphWhiteWings__pal{wing}',f'64kInvSeraphWhiteWings__pal{wing}',None,None),
      ('necklace','NECK','', '64kInvNecklace 2'+palette,None,None),
      ('ring','RING1','', '64kInvRings 4'+palette,None,None),
      ('bracelet','BRACER','', '64kInvBelt'+palette,None,None),
    ]
    if white_black:
      pieces += [
        ('plate','BODY',plate('PupPlateBody'),plate('64kInvPlateArmorSleeves'),None,None),
        ('warblade','WEAPON','PupNormalSword','64kInvShortSword',None,None),
        ('plate_boots','FEET',plate('PupPlateFoot'),plate('64kInvPlateArmorFeet'),None,None),
        ('plate_gauntlets','LEFT_HAND',plate('PupPlateGloveL'),plate('64kInvPlateGlove'),'RIGHT_HAND',plate('PupPlateGloveR')),
        ('plate_helm','HEAD',plate('PupPlateHelm'),plate('64kInvPlateArmorHelm'),None,None),
        ('plate_leggings','LEGS',plate('PupPlateLegs'),plate('64kInvPlateArmorLegs'),None,None),
      ]
    for offset,(suffix,slot,sprite,icon,secondary,second_sprite) in enumerate(pieces):
      if slot=='WEAPON':
        sprite += palette
        icon += palette
      itemkey=f'divine_{key}_{suffix}'
      itemreq=dict(req)
      itemclass=archetype
      itemp=p
      # Their white/black plate and warblade form a physical alternative to their caster regalia.
      if white_black and (suffix.startswith('plate') or suffix=='warblade'):
        itemreq={'strength':primary,'endurance':endurance}
        itemclass='WARRIOR'
      multiplier={'WARRIOR':1.1,'ARCHER':.85,'INT_MAGE':.8,'WIS_MAGE':1,'HYBRID_MAGE':.9}[itemclass]
      boosts={}
      main=round(itemp/(10 if itemclass=='INT_MAGE' else 24 if itemclass=='HYBRID_MAGE' else 12))
      if itemclass=='WARRIOR': boosts.update({3:main,8:round(itemp/5)})
      elif itemclass=='ARCHER': boosts.update({6:main,10035:round(itemp/5)})
      elif itemclass=='HYBRID_MAGE': boosts.update({1:main,4:main,16:round(itemp/10)})
      else: boosts.update({1 if itemclass=='INT_MAGE' else 4:main,power[school]:round(itemp/10)})
      for resist in (12,13,14,15,22): boosts[resist]=round(itemp/(40 if itemclass in ('WARRIOR','ARCHER') else 25))
      boost_rows=[]
      for stat,value in boosts.items():
        boost_rows.append({'boostId':boost_id,'statId':stat,'expression':str(value)})
        boost_id+=1
      label={'weapon': 'Longbow' if school=='archer' else 'Warblade' if school=='warrior' else 'Sceptre',
             'plate':'Plate Armor','warblade':'Warblade'}.get(suffix,suffix.replace('_',' ').title())
      color='White ' if key=='artherk' else 'Black ' if key=='ogrimar' else ''
      lang['item.'+itemkey]=f"{name}'s {color}{label}"
      obj={'key':itemkey,'name':'${item.'+itemkey+'}','bodyPart':slot,
           'appearanceEquippedPrimary':sprite,'appearanceInventory':icon,
           'numId':base+offset,'appearanceId':base+offset,'weight':4 if slot=='BODY' else 1,
           'armorClass':round(slot_ac[slot]*endurance/100*multiplier,1),
           'requirements':itemreq,'unique':tier==3,'boosts':boost_rows}
      if secondary: obj.update(secondaryBodyPart=secondary,appearanceEquippedSecondary=second_sprite)
      if slot=='WEAPON':
        obj['requirements']['endurance']=0
        obj.update(dmgFormula=f'1d{primary//2}+{primary//3}' if itemclass in ('WARRIOR','ARCHER') else f'1d{primary//12}+{primary//10}',atkDelay='1300')
        if school=='archer': obj['isBow']=True
      write(ROOT/f'assets/items/{itemkey}.json',json.dumps(obj,indent=2)+'\n')
    lang[f'monster.divine_{key}']=name
    lang[f'item.divine_{key}_return_scroll']=f"{name}'s Bound Return Scroll"
    lang[f'spell.item_divine_{key}_return']=f"Return to {name}"
    lang[f'spell.description.item_divine_{key}_return']=f"Return to {name}'s shrine. Reusable, bound to your backpack, and preserved through rebirth."
    lang[f'spell.mob_divine_{key}_judgment']=f"{name}'s Judgment"
    lang[f'spell.description.mob_divine_{key}_judgment']=f"The {school} trial of {name}."
    class_name='Divine'+name
    write(JAVA/f'monster/{class_name}.java',f'''package com.perso.T4C.monster;

import com.perso.T4C.divine.DivinePantheon;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.divine.DivineAvatar;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

// Generated entry point; shared combat and loot are owned by DivinePantheon.
@Spawn(type = "Divine {name}", x = {x}, y = {y}, z = 0, stationary = false, aggressive = false)
public final class {class_name} extends DivineAvatar {{
  public {class_name}(MonsterDef definition, float x, float y) throws GameException {{
    super(definition, x, y);
  }}

  public static MonsterDef definition() {{
    return DivinePantheon.definition("{key}");
  }}
}}
''')

lang.update({
 'npc.celestial_chronicler':'Celestial Chronicler',
 'npc.celestial_chronicler.welcome':'The bells have drawn the winged gods to these shores. Ask about the "Concord", the "gods", their "tiers", their "scrolls", the new "settlements", or "hunting" for leveling fields and training champions. Archivist Aveline nearby (4044, 1480) offers the progressing story: ask her "story".',
 'npc.celestial_chronicler.concord':'When Rhunor began erasing divine names, Artherk called a Concord of Witnesses. Greek, Norse and Egyptian gods sent winged human embodiments to keep their names alive. Even Ogrimar accepted the truce: a nameless world offers nothing to conquer. They guard separate shrines and fight only those who challenge them. Their bodies reform after defeat, leaving gifts for a worthy challenger.',
 'npc.celestial_chronicler.gods':'The lesser shrines belong to "Demeter", "Njord", "Ra" and "Artemis". The elder shrines belong to "Athena", "Apollo", "Anubis" and "Thor". The sovereign trials belong to "Artherk" and "Ogrimar". Name a god for the shrine, story and rewards.',
 'npc.celestial_chronicler.tiers':'Lesser gods are level 240-270 and offer equipment requiring about 380 in its main attribute. Elder gods are level 340-370 and offer equipment requiring about 650. Artherk and Ogrimar offer repeatable Seraph trials at levels 100 and 110, with equipment requiring about 180. Every defeat grants exactly one good or evil Seraph quest component, with an independent 50% chance for one equipment piece and 1% chance for a return scroll. Each has a weapon, outfit, wings and jewelry. Lesser and sovereign embodiments return five minutes after death; elders return after seven and a half minutes. Artherk and Ogrimar also offer a full warrior plate outfit and warblade.',
 'npc.celestial_chronicler.scrolls':'Every god has a one-in-one-hundred chance to leave its own Bound Return Scroll. Use it from your backpack to return beside that god\'s shrine. It is reusable, cannot be dropped, destroyed, sold or stored, and stays with you through death and rebirth. It grants no protection from other enemies at the shrine.',
 'message.bound_return_scroll_backpack':'This return scroll is bound to your backpack and cannot be stored.',
 'tooltip.divine_return_scroll':'Reusable return to this god\'s shrine.\nBound to backpack; survives death and rebirth.\nFive-second cooldown.',
 'message.gem_of_destiny_undroppable':'This bound item cannot be dropped or destroyed.',
})
stories={
 'demeter':'Demeter keeps the last living oath-seeds at Oathstone Grove in the Witness Isles. Her green wings and earth magic shelter the memories rooted there.',
 'njord':'Njord watches the western harbor of Moonwake Shoals. His blue wings and water magic preserve the sea-road that carried the first witnesses.',
 'ra':'Ra burns false names from the western clearing of Emberglass Crown. His crimson wings and fire magic keep its oath-flame from turning hollow.',
 'artemis':'Artemis hunts stolen echoes in the eastern clearing of Moonwake Shoals. Her green wings and longbow defend the paths used by the living witnesses.',
 'athena':'Athena holds the eastern approach to the Witness Isles sanctuary. Her plated angelic form tests warriors in disciplined close combat, beyond the sanctuary\'s protection.',
 'apollo':'Apollo tends the temple court of Moonwake Shoals. His radiant wings and light magic restore the names once sung at its altar.',
 'anubis':'Anubis weighs the forgotten dead at Sable Fen. His dark wings and death magic keep Rhunor from claiming souls whose names have faded.',
 'thor':'Thor stands at the northern approaches of Threnody Reach. His golden storm-wings and air magic carry the warning bells across the broken courts.',
 'artherk':'Artherk guards the western outer court of Threnody Reach in white armor and wings. His light trial rewards white robes, plate, weapons and jewelry, and exactly one randomly selected Heart, Soul or Will of Artherk used by the good-path quests. There is a 50% chance for one equipment piece and an independent 1% chance for his return scroll.',
 'ogrimar':'Ogrimar claims the southeastern outer court of Threnody Reach in black armor and wings. He honors the Concord until challenged. His dark trial rewards black robes, plate, weapons and jewelry, and exactly one randomly selected Blood, Bones or Skull of Ogrimar used by the evil-path quests. There is a 50% chance for one equipment piece and an independent 1% chance for his return scroll.',
}
for key,name,school,tier,level,x,y,*_ in GODS:
    lang[f'npc.celestial_chronicler.{key}']=stories[key]+f' Find {name} at ({x}, {y}), level {level}. Ask about "tiers" or "scrolls" for the trial rules.'
write(lang_path,json.dumps(lang,ensure_ascii=False,indent=2)+'\n')
print(f'Generated {len(GODS)} gods; equipment boosts end at {boost_id-1}.')
