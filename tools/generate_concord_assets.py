"""Author the Concord campaign's English catalogue, NPC registrations and reward equipment."""
import json
import math
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
catalog = ROOT / 'assets/i18n/lang.json'
lang = json.loads(catalog.read_text(encoding='utf-8'))

def text(key, value):
    lang[key] = value

sites = [
    ('ArchivistAveline', 'Archivist Aveline', -1, 4044, 1480),
    ('OathwoodArchivist', 'Oathwood Archivist', 0, 3774, 1214),
    ('MoonwakeBellkeeper', 'Moonwake Bellkeeper', 1, 3946, 631),
    ('EmberglassConfessor', 'Emberglass Confessor', 2, 4326, 2726),
    ('ThrenodyRefugee', 'Refugee Speaker Neris', 3, 5508, 1154),
    ('BellwardTuner', 'Bellward Tuner', 4, 5705, 1555),
]
for cls, name, index, x, y in sites:
    path = ROOT / f'src/main/java/com/perso/T4C/npc/{cls}.java'
    path.write_text(f'''package com.perso.T4C.npc;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "{cls}", x = {x}, y = {y}, z = 0, stationary = true, aggressive = false)
public final class {cls} extends ConcordStoryNpc {{
  public static final String ID = "{cls}";
  public static final String DISPLAY_NAME = "${{npc.concord.{cls}.name}}";
  public static final String SPRITE_BASE = null;

  public {cls}(NpcContext context) throws GameException {{ super(spec(), context, {index}); }}
  public static NpcSpec spec() {{ return storySpec(ID); }}
}}
''', encoding='utf-8')
    text(f'npc.concord.{cls}.name', name)
    text(f'npc.concord.{cls}.welcome',
         'Rhunor feeds on erased names. Artherk would preserve every name; Ogrimar would release every echo. Their Concord forbids the gods from making that choice for mortals. The Names We Choose begins here: ask "story", then "accept". Use "route", "rewards" or "report" as you progress. I can arrange "passage" to unlocked Threnody Reach.' if index == -1 else
         'Every silence has a history. Examine the "record" and listen to the "echo" before choosing your answer.' + (' Say "return" for passage to Aveline in the sanctuary.' if index == 3 else ''))

words = ['story','accept','report','route','rewards','record','echo','names','shelter','truth','mercy','reveal','choice','ring','remember','passage','return','restore','release','warrior','archer','fire','water','earth','air','light','dark']
for word in words:
    text('npc.concord.keyword.' + word, word)
text('quest.objective.evidence', '%s: %d/%d')
text('quest.objective.story', 'Follow the story instructions in your journal.')
text('player.default_name', 'Character')
messages = {
 'next_note': 'The bell holds that note. Speak the next word of the reconstructed oath.',
 'help': 'Ask "record" and "echo" at a witness, or "story", "accept", "report" and "rewards" with Aveline.',
 'accept_first': 'Say "accept" before beginning this chapter. Your journal records its route and reward.',
 'site_locked': 'This testimony belongs to another chapter. Ask Aveline for your current "route", and "accept" it before returning.',
 'approach': 'Come closer to examine this evidence.',
 'passage_locked': 'First reconcile the Moonwake and Emberglass accounts. Completing A Charter for the Living opens Threnody Reach.',
 'capacity': 'Make room for your reward, then try again. Your evidence and progress are safe.',
 'style_selected': 'Your combat style is recorded. You may change it before saying "report"; each checkpoint awards one matching equipment item.',
 'rewards': 'Every chapter grants gold, base XP and equipment. Choose "warrior", "archer", "fire", "water", "earth", "air", "light" or "dark", then "report". Chapters 3, 6 and 8 award progressively stronger weapons; other checkpoints grant a matching ring, bracelet, necklace, mantle and signet. XP follows the server rate and level cap.',
 'next': 'This checkpoint is secure. Ask "story" for the next chapter, then "accept". Your journal keeps its directions.',
 'bell_remaining': 'One bell answers. The others still need their own voices: visit Moonwake, Emberglass and Bellward, read each "record" and "echo", and answer the question there. No monster can supply these voices.',
 'choose_ending': 'After Rhunor falls, decide: "restore" the dead names to the public record, accepting their grief, or "release" the trapped echoes to rest while preserving the living witnesses. Both endings award the same final equipment, gold and XP.',
 'ending_locked': 'That choice becomes yours after you defeat Rhunor during the final chapter.',
 'ending_selected': 'Your final choice is recorded. Choose your weapon style and say "report" to seal the dawn.',
 'ending_restore': 'You restored the names. At Moonwake, the bells now speak each lost family aloud. Emberglass publishes the warders\' confession. The gods witness the reading, but mortals decide what to remember. Rhunor is defeated; grief has a name, and no ruler may erase it again.',
 'ending_release': 'You released the echoes. The dead leave the bells in peace, while living witnesses carry their stories by choice. Emberglass opens its doors to those it once silenced. The gods lower their wings as the first unbound dawn reaches the castles. Rhunor is defeated; remembrance is no longer a prison.',
 'refuge_mercy': 'Neris keeps the refugees\' names in a sealed archive. They return to their homes when they feel safe; the Concord protects their right to remain unnamed.',
 'refuge_reveal': 'Neris signs the public charter with the refugees who consented. Their testimony becomes the foundation of a council that answers to survivors instead of kings.',
}
for key, value in messages.items(): text('npc.concord.' + key, value)

# Each puzzle has two independent clues; the answer is explicitly present in the evidence.
evidence = [
 ('The ledger lists taxes but leaves every household blank. Examine the "record" and the "echo".',
  'In the "record", someone crossed out names before Rhunor came. A margin reads: A kingdom is its people, not its tribute.',
  'The "echo" repeats the oath: We guard their "names", even when no crown remembers them.',
  'What must the charter protect: gold, crowns, or "names"?',
  'The restored charter records the erased households. Bring this evidence to Aveline and "report"; your first ring awaits.'),
 ('The drowned bell has a crack. Compare its "record" with its "echo".',
  'The "record" says the bell summoned a fleet to war. Its date was altered after the evacuation.',
  'The "echo" is a mother calling boats to "shelter" the children. The bell warned people away from battle.',
  'Was this a summons to conquest, or a call for "shelter"?',
  'You restore the evacuation account. The bell was a refuge, not a weapon. Take the testimony to Aveline and "report".'),
 ('The warders swear the fire destroyed the records. Read the "record" and hear the "echo".',
  'The "record" orders the warders to burn refugee names. A signature survives: the mortal warden who became the Dusk Regent.',
  'The "echo" is a confessor: Obedience fed the fire. Only "truth" can end our oath.',
  'Will another oath of obedience mend this, or must the warders speak the "truth"?',
  'The confessor signs the truth. Return to Aveline and "report" for a chosen weapon and access to Threnody Reach.'),
 ('Neris will not give you a list of people to sacrifice. Read the "record" and hear the "echo" before deciding.',
  'The "record" is a demand from the Regent: hand over the refugees, and the camp may survive. Neris refuses.',
  'The "echo" records consent: some survivors wish to testify publicly; others fear that a spoken name will draw Rhunor. Choose "mercy" to seal the list, or "reveal" to publish only consenting testimony.',
  'Choose "mercy" for a protected archive, or "reveal" for voluntary public testimony. Neither choice delivers anyone to the Regent.',
  'Neris records your decision without surrendering a single refugee. Say "return" to reach Aveline, then "report" for the Refuge necklace.'),
 ('The Bellward mechanism answers only a reconstructed sequence. Study its "record" and "echo".',
  'The "record" says: first remember those who are gone; next shelter those who live; last leave their future to their choice.',
  'The "echo" follows the same order: "remember", "shelter", "choice". Speak each word in that order to open the Regent\'s oath to challenge.',
  'That note breaks the chord; start again. Speak "remember", then "shelter", then "choice", one word at a time.',
  'The three notes expose the Regent\'s stolen oath. Bring the resonance rubbing to Aveline and "report". The Bell mantle will protect you on the approach.'),
]
for i, rows in enumerate(evidence):
    for field, value in zip(['welcome','record','echo','question','success'], rows):
        text(f'npc.concord.site.{i}.{field}', value)
for site, answer, meaning in [(1,'shelter','The drowned bell must shelter the living, not summon an army.'),(2,'truth','The ember bell must speak truth, not bind obedience.'),(4,'choice','The last bell must preserve choice, not impose a single memory.')]:
    for field, value in {
        'welcome': 'The Regent is fallen. Rebind this bell by comparing its "record" and "echo".',
        'record': meaning + f' Its new oath is "{answer}".',
        'echo': f'The free voices answer "{answer}". Will you bind the bell to that purpose?',
        'question': f'Choose "{answer}" to bind this bell without feeding Rhunor.',
        'success': 'All three bells answer freely. Bring the Concord seal to Aveline and "report" to open The Hollow Dawn. Rhunor can now be reached through named travel.'
    }.items(): text(f'npc.concord.site.{site}.ritual.{field}', value)

chapters = [
 ('charter', 'The Empty Charter', 'At Oathwood Castle, find the Oathwood Archivist by the southeast gate (3774, 1214). Compare "record" and "echo", then restore what the charter erased.', 'Oath ring', 'The first names return. This was a deliberate erasure, not a curse without a cause.'),
 ('drowned', 'The Bell That Saved Them', 'Find the Moonwake Bellkeeper by the southeast gate of Moonwake Castle (3946, 631). Compare "record" and "echo" and decide why the bell first rang.', 'Tide bracelet', 'The drowned were protecting children. Emberglass has been telling a different story.'),
 ('embers', 'A Charter for the Living', 'Find the Emberglass Confessor at the citadel\'s southeast gate (4326, 2726). Compare "record" and "echo", and confront the warders\' false account. Return to Aveline, choose a weapon style and "report".', 'Concord weapon, tier I; Threnody Reach access', 'The two shores agree: the Dusk Regent erased the names before Rhunor consumed them. Threnody Reach is open. Ask Aveline for "passage".'),
 ('refuge', 'No Names for the Regent', 'Use Aveline\'s "passage" or named travel to Threnody Reach. Find Neris in the arrival camp (5508, 1154); compare "record" and "echo", then choose "mercy" or "reveal". Say "return" to reach Aveline.', 'Refuge necklace', 'The refugees remain free. Your decision will shape the settlement after the final battle.'),
 ('resonance', 'The Unfinished Chord', 'Find the Bellward Tuner at Bellward Castle\'s southeast gate (5705, 1555). Compare "record" and "echo" to reconstruct the three-note oath. Speak "remember", then "shelter", then "choice", one word at a time. A wrong note restarts the sequence.', 'Bell mantle', 'The oath is exposed. Its master must now answer for what he made of it.'),
 ('regent', 'The Mortal Behind the Crown', 'Confront The Dusk Regent near (5860, 2040) in Threnody Reach. Defeat him once while this chapter is active. This breaks his oath; no collection of ordinary monster kills will substitute. Return to Aveline, choose a weapon style and "report".', 'Concord weapon, tier II', 'Beneath the crown was a mortal who traded other people\'s names for immortality. His fall frees the bells, but Rhunor is waking.'),
 ('three_bells', 'Three Voices at Dawn', 'Revisit the Moonwake Bellkeeper (3946, 631), Emberglass Confessor (4326, 2726) and Bellward Tuner (5705, 1555), in any order. At each, compare the new "record" and "echo", then choose the purpose of its bell. All three are required. Bring the resulting seal to Aveline and "report".', 'Dawn signet; The Hollow Dawn access', 'Shelter, truth and choice ring together. The sealed road to The Hollow Dawn opens. Prepare your equipment, then accept the last chapter.'),
 ('dawn', 'The Names We Choose', 'Use named travel to The Hollow Dawn after completing Three Voices at Dawn. Defeat Rhunor, the Hollow Dawn once near (5650, 2290) while this chapter is active. Return to Aveline: "restore" the names or "release" the echoes, choose a weapon style and "report" for the ending.', 'Concord weapon, tier III; your chosen ending', 'You have broken Rhunor\'s claim. The next dawn belongs to those who survived, and the final choice is yours.'),
]
text('quest.chain.concord', 'The Names We Choose')
for i, (suffix,title,route,reward,completion) in enumerate(chapters):
    prefix = 'quest.concord_' + suffix
    for field, value in {
        'title': title,
        'offer': route + ' Return to Archivist Aveline in the Witness sanctuary (4044, 1480); choose your combat style through "rewards", then "report".',
        'walkthrough': f'Chapter {i+1}/8: {title}. ' + route + f' Checkpoint reward: {reward} for your chosen style, {(i+1)*150000:,} gold, and {[30,50,75,100,150,200,175,250][i]} million base XP (server rate and level cap apply). Report to Aveline (4044, 1480), then ask "story" and "accept" for the next chapter. No repeated monster quota is required.',
        'completion': completion,
        'completed': 'This chapter is complete. Its reward cannot be claimed twice. Ask Aveline for your next "story".'
    }.items(): text(prefix + '.' + field, value)

# Use native, already shipped inventory/equipment art. IDs occupy a dedicated new block.
used_ids = set()
used_boosts = set()
for path in (ROOT / 'assets/items').glob('*.json'):
    if path.stem.startswith('concord_'): continue
    item = json.loads(path.read_text(encoding='utf-8-sig'))
    used_ids.add(item.get('numId', 0))
    used_boosts.update(b.get('boostId') for b in item.get('boosts', []))
next_id, boost_id = 59000, 61000

def item(key, name, slot, icon, stats, **extra):
    global next_id, boost_id
    if next_id in used_ids: raise ValueError(f'Item id collision: {next_id}')
    rows = []
    for stat, value in stats.items():
        if boost_id in used_boosts: raise ValueError(f'Boost collision: {boost_id}')
        rows.append(dict(boostId=boost_id, statId=stat, expression=str(value)))
        boost_id += 1
    data = dict(key=key, name='${item.'+key+'}', bodyPart=slot,
                appearanceInventory=icon, numId=next_id, appearanceId=next_id,
                weight=1, price=0, boosts=rows, **extra)
    next_id += 1
    (ROOT / f'assets/items/{key}.json').write_text(json.dumps(data, indent=2)+'\n', encoding='utf-8')
    text('item.'+key, name)

schools = {'warrior':('athena',3,8), 'archer':('artemis',6,10035), 'fire':('ra',1,17),
           'water':('njord',1,18), 'earth':('demeter',4,19), 'air':('thor',1,16),
           'light':('apollo',4,23), 'dark':('anubis',1,24)}

def budget(style, req, armor=False):
    """Mirror ItemBalance's ordinary, single-item formulas (Java rounding)."""
    rounded = lambda x: math.floor(x + .5)
    _, stat, power_stat = schools[style]
    requirement = {'strength' if style == 'warrior' else 'agility' if style == 'archer' else 'wisdom' if style in ('light','earth') else 'intelligence':req}
    if style == 'air': requirement = dict(intelligence=req//2, wisdom=req//2)
    p = sum(requirement.values()) * .8 if style == 'air' else req
    physical = style in ('warrior','archer')
    main = rounded(p / (24 if style == 'air' else 10 if style in ('fire','water','dark') else 12))
    stats = {stat:main, power_stat:rounded(p / (5 if physical else 10))}
    if style == 'air': stats[4] = main
    if armor:
        for resist in (12,13,14,15,22): stats[resist] = rounded(p / (40 if physical else 25))
    return requirement, stats

# Evidence is a non-equippable native material, registered by ConcordEvidence.java.
for suffix, title, *_ in chapters:
    if suffix not in ('regent','dawn'):
        text('item.concord_'+suffix+'_proof', title+' - Restored Evidence')

for key, name, slot, icon, req, endurance in [
 ('oath_ring','Ring of Remembered Names','RING1','64kInvRings 1',100,50),
 ('tide_bracelet','Bracelet of the Sheltering Tide','BRACER','64kInvBelt',160,80),
 ('refuge_necklace','Necklace of the Free Witness','NECK','64kInvNecklace 2',300,120),
 ('bell_mantle','Mantle of the Unfinished Chord','BACK','64kInvSeraphWhiteWings',400,160),
 ('dawn_signet','Signet of Three Free Voices','RING1','64kInvRings 4',550,200),
]:
    for style,(god,_,_) in schools.items():
        requirement, stats = budget(style, req, True)
        requirement['endurance'] = endurance
        multiplier = 1.1 if style == 'warrior' else .85 if style == 'archer' else .9 if style == 'air' else 1 if style in ('earth','light') else .8
        extra = dict(armorClass=math.floor((9 if slot=='BACK' else 5)*endurance/100*multiplier*10+.5)/10, requirements=requirement)
        if slot == 'BACK':
            art = json.loads((ROOT/f'assets/items/divine_{god}_wings.json').read_text())
            extra['appearanceEquippedPrimary'] = art['appearanceEquippedPrimary']
            icon_for_style = art['appearanceInventory']
        else: icon_for_style = icon
        item('concord_'+key+'_'+style, name+' ('+style.title()+')', slot, icon_for_style, stats, **extra)

for tier, primary, power, req in [(1,25,40,250),(2,45,70,450),(3,65,100,650)]:
    for style,(god,stat,power_stat) in schools.items():
        art = json.loads((ROOT/f'assets/items/divine_{god}_weapon.json').read_text())
        requirement, stats = budget(style, req)
        physical = style in ('warrior','archer')
        item(f'concord_{tier}_{style}', f'{["Witness", "Oathbreaker", "Dawnkeeper"][tier-1]} {"Warblade" if style=="warrior" else "Longbow" if style=="archer" else style.title()+" Staff"}',
             'WEAPON', art['appearanceInventory'], stats,
             appearanceEquippedPrimary=art['appearanceEquippedPrimary'], requirements=requirement,
             dmgFormula=f'1d{[150,250,350][tier-1] if physical else [40,60,80][tier-1]}+{[90,150,210][tier-1] if physical else [30,45,60][tier-1]}',
             atkDelay='1300', isBow=style=='archer')

lang['npc.celestial_chronicler.welcome'] += ' For a progressing campaign, visit Archivist Aveline nearby (4044, 1480) and ask "story": The Names We Choose.' if 'Archivist Aveline' not in lang['npc.celestial_chronicler.welcome'] else ''
catalog.write_text(json.dumps(lang, ensure_ascii=False, indent=2)+'\n', encoding='utf-8')
print('Authored 8 chapters, 6 NPCs, 6 evidence names and 64 equipment rewards.')
