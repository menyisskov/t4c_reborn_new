"""Export the reviewed quest destinations to the game's English catalogue."""
import json
import re
from pathlib import Path

root=Path.cwd()
source=json.loads((root/'docs/spell-animations/quest-teleport-catalogue.json').read_text(encoding='utf-8'))
labels={
 'travel.category.cities':'Cities','travel.category.hunting':'Hunting Areas',
 'travel.category.training':'Training Fields','travel.category.good':'Good Seraph Quest',
 'travel.category.evil':'Evil Seraph Quest','travel.category.olin':'Olin Haad Quest',
 'travel.category.gods':'Gods','travel.category.islands':'New Islands',
 'travel.category.landmarks':'Other Landmarks',
 'travel.categories':'Categories','travel.destinations':'Destinations',
 'travel.search':'Find destination...','travel.undiscovered':'Undiscovered',
 'travel.discovered':'Discovered','travel.gm_access':'GM access',
 'travel.discovery_hint':'Explore within 50 coordinates of this destination on the same map to unlock fast travel permanently.',
 'travel.unmapped':'This destination has no verified playable map location yet.',
 'travel.no_landing':'No clear arrival spot found nearby.',
 'travel.pending_coordinates':'Coordinates pending',
 'travel.empty':'No matching destinations.',
 'travel.hint':'Select a destination, then Travel. Scroll the list for more.',
 'travel.generic':'Visit this landmark to discover its fast-travel destination.',
 'travel.god':'Divine shrine.', 'travel.arch_drake':"Arch Drake · Drake’s Lair",
 'travel.field':'Dense hunting field.',
 'travel.chapter':'%s · %d destinations',
}
for god_id,god_name in re.findall(r'new God\("([^"]+)", "([^"]+)"', (root/'src/main/java/com/perso/T4C/divine/DivinePantheon.java').read_text()):
 labels['travel.god.'+god_id]=god_name
rows=[]
for category,entries in source['routes'].items():
 cat={'Good Seraph Quest':'good','Evil Seraph Quest':'evil','Olin Haad Quest':'olin'}[category]
 for row in entries:
  slug=cat+'.'+re.sub(r'[^a-z0-9]+','_',row['id'].lower())
  name_key='travel.destination.'+slug+'.name'
  desc_key='travel.destination.'+slug+'.desc'
  labels[name_key]=row['name']
  labels[desc_key]=row['chapter']+'\n\n'+row['note']+ ('\n\n'+row['warning'] if row['warning'] else '')
  rows.append(dict(id=slug,category=cat,nameKey=name_key,descriptionKey=desc_key,
      x=row['x'],y=row['y'],z=row['z']))
out=root/'assets/teleports/quest-destinations.json'
out.parent.mkdir(parents=True,exist_ok=True)
out.write_text(json.dumps(rows,indent=2)+'\n',encoding='utf-8')
path=root/'assets/i18n/lang.json'
raw=path.read_text(encoding='utf-8');existing=json.loads(raw)
for key,value in labels.items():
 if key in existing:
  if existing[key]!=value:
   raw=re.sub(r'('+re.escape(json.dumps(key))+r'\s*:\s*)"(?:[^"\\]|\\.)*"',lambda m:m.group(1)+json.dumps(value,ensure_ascii=False),raw)
new={k:v for k,v in labels.items() if k not in existing}
if new:
 tail=',\n'+',\n'.join('  '+json.dumps(k)+': '+json.dumps(v,ensure_ascii=False) for k,v in new.items())+'\n'
 raw=raw.rstrip();raw=raw[:raw.rfind('}')].rstrip()+tail+'}\n'
json.loads(raw)
path.write_text(raw,encoding='utf-8')
print('Exported',len(rows),'quest destinations and',len(new),'English labels.')
