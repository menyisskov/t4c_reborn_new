"""Build a local viewer and animated overview from the exported, native-rendered spell frames."""
import json
import hashlib
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'docs/spell-animations'
data=json.loads((OUT/'manifest.json').read_text(encoding='utf-8'))
for asset in data['assets'].values():
 if asset.get('file'):
  asset['version']=hashlib.sha256((OUT/asset['file']).read_bytes()).hexdigest()[:12]
(OUT/'manifest.json').write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
template=(ROOT/'tools/spell-animation-gallery.html').read_text(encoding='utf-8')
(OUT/'index.html').write_text(template.replace('__MANIFEST__',json.dumps(data,ensure_ascii=False).replace('</','<\\/')),encoding='utf-8')
spells=[s for s in data['spells']if s['category']=='Combat ladder']
spells.sort(key=lambda s:(s['level'],s['element']))
images={key:Image.open(OUT/a['file']).convert('RGB')for key,a in data['assets'].items()if a.get('file')}
font=ImageFont.truetype('C:/Windows/Fonts/segoeui.ttf',13)
small=ImageFont.truetype('C:/Windows/Fonts/segoeui.ttf',11)
heading=ImageFont.truetype('C:/Windows/Fonts/segoeuib.ttf',18)
schools=['','Fire','Earth','Air','Water','Light','Dark']
W,H,CW,CH=1080,954,180,176
frames=[]
for frame in range(max(data['assets'][s['impact']]['count'] for s in spells)+9):
 canvas=Image.new('RGB',(W,H),(9,11,14));draw=ImageDraw.Draw(canvas)
 draw.text((18,10),'T4C — New combat spell animations',font=heading,fill=(230,208,166))
 draw.text((18,36),'Actual game assets · level 150 → 350 · fitted previews',font=small,fill=(177,191,201))
 for index,s in enumerate(spells):
  a=data['assets'][s['impact']];n=min(a['count']-1,frame%(a['count']+9));x=(n%a['columns'])*a['cell'];y=(n//a['columns'])*a['cell']
  tile=images[s['impact']].crop((x,y,x+a['cell'],y+a['cell'])).resize((140,140),Image.Resampling.LANCZOS)
  px=(index%6)*CW;py=60+(index//6)*CH;canvas.paste(tile,(px+20,py))
  name=s['name'];name_width=draw.textlength(name,font=font)
  if name_width>172:
   words=name.split();last=words.pop();first=' '.join(words);draw.text((px+90,py+137),first,font=font,fill=(232,217,190),anchor='mt');draw.text((px+90,py+151),last,font=font,fill=(232,217,190),anchor='mt')
  else:draw.text((px+90,py+145),name,font=font,fill=(232,217,190),anchor='mt')
  draw.text((px+90,py+163),schools[s['element']]+' · '+str(s['level']),font=small,fill=(164,183,197),anchor='mt')
 frames.append(canvas)
frames[18].save(OUT/'combat-spells-overview.png')
frames[0].save(OUT/'combat-spells-overview.gif',save_all=True,append_images=frames[1:],duration=60,loop=0,optimize=True,disposal=2)
print('Built',len(data['spells']),'spell pages and a 30-spell animated overview.')

lines=['# New spell target counts','', 'Area attacks hit up to 8 eligible enemies. Single-target player attacks hit the primary enemy at full damage and up to 7 enemies on adjacent tiles at 25% damage. Range, line of sight and combat restrictions apply. The current client does not implement party-wide healing.','']
for category in ['Combat ladder','Other player spells','Divine attacks','Return scrolls']:
 lines += ['## '+category,'','| Spell | Target type | Targets per cast |','|---|---|---|']
 for spell in data['spells']:
  if spell['category']==category:lines.append('| '+spell['name']+' | '+spell.get('targeting','')+' | '+spell.get('targets','')+' |')
 lines.append('')
lines += ['Wellspring Mercy and Dawnwell Renewal have configured area radii, but the current support-casting path applies their healing only to the caster. Renew Armor refreshes multiple buffs on that same one caster; it does not affect multiple characters.','']
(OUT/'targeting.md').write_text('\n'.join(lines),encoding='utf-8')

def build_comparison(keys, name):
 selected=[next(s for s in data['spells'] if s['key']==key) for key in keys]
 cols=2;rows=(len(selected)+1)//2;frames=[]
 for index in range(max(data['assets'][s['impact']]['count'] for s in selected)+8):
  canvas=Image.new('RGB',(600,rows*306),(9,11,14));draw=ImageDraw.Draw(canvas)
  for j,s in enumerate(selected):
   a=data['assets'][s['impact']];n=min(index,a['count']-1)
   x=(n%a['columns'])*a['cell'];y=(n//a['columns'])*a['cell']
   tile=images[s['impact']].crop((x,y,x+a['cell'],y+a['cell']))
   px=(j%cols)*300;py=(j//cols)*306;canvas.paste(tile,(px+22,py))
   draw.text((px+150,py+260),s['name'],font=font,fill=(232,217,190),anchor='mt')
   draw.text((px+150,py+283),str(a['count'])+' frames',font=small,fill=(164,183,197),anchor='mt')
  frames.append(canvas)
 frames[0].save(OUT/(name+'.gif'),save_all=True,append_images=frames[1:],duration=round(1000/data['fps']),loop=0,optimize=True)
 frames[min(12,len(frames)-1)].save(OUT/(name+'.png'))

build_comparison(['spell.emberqueens_wrath','spell.worldroot_upheaval'],'fire-earth-retro')
build_comparison(['spell.emberqueens_wrath','spell.worldroot_upheaval','spell.drowning_deep','spell.hallowed_nova'],'level-350-updated')
