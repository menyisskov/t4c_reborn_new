const state = {
  section: "sprites",
  maps: [],
  mapPath: "",
  items: [],
  selectedIndex: -1,
  editorTab: "details",
  dirty: false,
  sprites: [],
  spriteBins: [],
  currentSpriteBin: "",
  spritePage: 0,
  spritePageSize: 250,
  selectedSprite: null,
  spriteSelectedSet: new Set(),
  spriteIndexLoaded: false,
  npcOptions: [],
  monsterOptions: [],
  clanOptions: [],
  objectDefinitionOptions: [],
  spellNameOptions: [],
  itemKeyOptions: [],
  trainableStatIdOptions: [],
  spellIconOptions: [],
  spellSpriteOptions: [],
  spellEditorOptionsLoaded: false,
  itemSpriteOptions: [],
  itemEquippedSpriteOptions: [],
  monsterPatternOptions: [],
  animatedSpriteBaseOptions: [],
  puppetSpriteBaseOptions: [],
  soundOptions: [],
  placementPreviewZoom: 1,
  spritePreviewCache: new Map(),
  collisionRules: null,
  collisionDefinedOnly: false,
  collisionPaintMode: "collision",
  collisionDragMode: null,
  actionTypeOptions: [],
  questIdOptions: [],
  questFlagNameOptions: [],
  dialogueEditorOptionsLoaded: false,
  dialogueEditorOptionsLoading: false,
  dialogueLayout: {},
  dialogueLayoutFor: null,
  dialogueSelectedNodeId: null,
};

const sectionGroups = [
  {
    title: "Assets",
    items: [
      { id: "sprites", label: "Sprites", kind: "sprites" },
      { id: "objects", label: "Object Definitions", endpoint: "/api/object-mappings", key: "logicalName", subtitle: "sprite" },
      { id: "objectPositions", label: "Object Positions", endpoint: "/api/object-positions", key: "name", subtitle: "x,y,z" },
      { id: "itemIcons", label: "Item Icons", endpoint: "/api/item-icons", key: "appearanceId", subtitle: "sprite" },
      { id: "groundMosaics", label: "Ground Mosaics", endpoint: "/api/ground-mosaics", key: "id", subtitle: "width,height,frameCount" },
    ],
  },
  {
    title: "Entities",
    items: [
      { id: "npcs", label: "NPC Editor", endpoint: "/api/npcs", key: "displayName", subtitle: "name" },
      { id: "monsters", label: "Monster Editor", endpoint: "/api/monsters", key: "name", subtitle: "displayName" },
      { id: "quests", label: "Quest Editor", endpoint: "/api/quests", key: "id", subtitle: "title" },
      { id: "items", label: "Item Editor", endpoint: "/api/items", key: "key", subtitle: "name" },
      { id: "spells", label: "Spell Editor", endpoint: "/api/spells", key: "key", subtitle: "name" },
      { id: "monsterSpawns", label: "Monster Placement", endpoint: "/api/spawns?kind=monster", key: "type", subtitle: "x,y,z", mapScoped: true },
      { id: "npcSpawns", label: "NPC Placement", endpoint: "/api/spawns?kind=npc", key: "type", subtitle: "x,y,z", mapScoped: true },
    ],
  },
  {
    title: "Rules",
    items: [
      { id: "collisionRules", label: "Collision Rules", endpoint: "/api/collision-rules", key: "sprite", kind: "collisionRules" },
      { id: "decorRules", label: "Player Above Decor", endpoint: "/api/decor-layer-rules", key: "sprite", subtitle: "sprite" },
      { id: "clanRelations", label: "Clan Relations", endpoint: "/api/clan-relations", key: "source", subtitle: "target" },
      { id: "appearanceDefaults", label: "Naked Body Parts", endpoint: "/api/appearance-defaults", key: "bodyPart", subtitle: "gender,sprite" },
      { id: "concealmentRules", label: "Concealment Rules", endpoint: "/api/concealment", key: "appearance", subtitle: "triggerSlot,hiddenParts" },
      { id: "xpCurve", label: "XP Curve", endpoint: "/api/xp-curve", key: "level", subtitle: "xpToNextLevel,totalXp" },
    ],
  },
  {
    title: "World",
    items: [
      { id: "teleports", label: "Teleport Editor", endpoint: "/api/teleports", key: "id", subtitle: "sourceX,sourceY,targetX,targetY" },
    ],
  },
];

const fields = {
  npcs: ["name", "displayName", "spriteBase", "patrolRadiusTiles:number"],
  monsters: ["name", "displayName", "health:number", "mana:number", "xpPerHit:number", "xpOnDeath:number", "hitDamageMin:number", "hitDamageMax:number", "respawnTime:number", "walkPattern", "attackPattern", "deathPattern", "soundAttack", "soundDeath", "soundHit", "goldMin:number", "goldMax:number", "defaultAggressive:boolean", "animateWhileStationary:boolean", "stationaryAnimationPauseSeconds:number"],
  quests: ["id", "title", "giverNpc", "targetMonster", "requiredKills:number", "targetWorldZ:number", "areaCenterX:number", "areaCenterY:number", "areaRadiusTiles:number", "rewardGold:number", "rewardXp:number", "offerText:textarea", "completionText:textarea", "completedText:textarea"],
  items: ["key", "name", "bodyPart", "appearanceEquippedPrimary", "appearanceInventory", "price:number", "weight:number", "armorClass:number", "dodgeLost:number", "minEnd:number", "reqAttack:number", "reqStr:number", "reqAgi:number", "minInt:number", "minWis:number", "dmgFormula", "atkDelay", "attackSpeed:number", "unique:boolean", "bow:boolean", "unlimitedUse:boolean", "canSummon:boolean", "radiance:number", "nbCharges:number", "lockName", "lockDiff:number", "signText", "containerGold:number", "globalRespawn:number", "localRespawn:number"],
  spells: ["key", "name", "description:textarea", "manaCost", "price:number", "radius:number", "minInt:number", "minWis:number", "minLevel:number", "attack:boolean", "lineOfSight:boolean", "iconId", "projectileSpell", "impactSpell", "minDamage:number", "maxDamage:number", "sound", "soundImpact", "cooldownSeconds:number", "duration"],
  monsterSpawns: ["type", "x:number", "y:number", "z:number", "stationary:boolean", "aggressive:boolean"],
  npcSpawns: ["type", "x:number", "y:number", "z:number", "stationary:boolean", "aggressive:boolean"],
  objects: ["logicalName", "sprite", "displayName", "clickAnimate:boolean", "mirror:boolean", "animateSound", "reverseAnimateSound", "alwaysBehindEntities:boolean", "depthTileOffsetY:number"],
  objectPositions: ["name", "x:number", "y:number", "z:number"],
  teleports: ["id:number", "sourceZ:number", "sourceX:number", "sourceY:number", "targetZ:number", "targetX:number", "targetY:number"],
  clanRelations: ["source", "target"],
  decorRules: ["sprite"],
  itemIcons: ["appearanceId:number", "sprite"],
  appearanceDefaults: ["gender", "bodyPart", "sprite"],
  concealmentRules: ["triggerSlot", "appearance", "hiddenParts", "hidesExplicit:boolean"],
  groundMosaics: ["id", "width:number", "height:number", "frames:textarea"],
  xpCurve: ["level:number", "xpToNextLevel:number", "totalXp:number"],
};

const NPC_PREVIEW_BODY_ORDER = [
  "CAPE",
  "BACK",
  "FEET",
  "LEGS",
  "BOOT",
  "ROBELEGS",
  "BODY",
  "CAPE",
  "LEFT_HAND",
  "LEFT_ARM",
  "HEAD",
  "HAIR",
  "HAT",
  "MASK",
  "RIGHT_HAND",
  "RIGHT_ARM",
  "SHIELD",
  "WEAPON",
  "WEAPON2",
];

const FIELD_META = {
  key: ["Key", "Unique internal identifier used by references and saves."],
  name: ["Name", "Main display or lookup name for this record."],
  displayName: ["Display name", "Name shown to players in game."],
  id: ["Identifier", "Stable internal identifier used by references and persistent state."],
  title: ["Quest title", "Player-facing title of this quest."],
  giverNpc: ["Giver / turn-in NPC", "NPC that gives the quest and receives it when complete."],
  targetMonster: ["Target monster", "Canonical monster definition counted by this quest."],
  requiredKills: ["Required kills", "Number of matching monsters the player must kill."],
  targetWorldZ: ["Target world Z", "World layer on which matching kills count."],
  areaCenterX: ["Area center X", "Horizontal tile coordinate at the center of the objective area."],
  areaCenterY: ["Area center Y", "Vertical tile coordinate at the center of the objective area."],
  areaRadiusTiles: ["Area radius", "Objective radius around the center, in tiles."],
  rewardGold: ["Gold reward", "Exact amount of gold granted once at turn-in."],
  rewardXp: ["XP reward", "Exact experience granted once at turn-in, without XP multipliers."],
  offerText: ["Offer text", "Dialogue shown when the player accepts the quest."],
  completionText: ["Completion text", "Dialogue shown when the reward is handed in."],
  completedText: ["Already completed text", "Dialogue shown if the player asks for this one-time quest again."],
  description: ["Description", "Text shown in UI/tooltips or learning screens."],
  spriteBase: ["Sprite base", "Animation base used to resolve directional sprite frames."],
  patrolRadiusTiles: ["Patrol radius", "How far this NPC can wander from spawn, in tiles. 0 uses the engine default."],
  health: ["Health", "Maximum hit points."],
  mana: ["Mana", "Maximum mana points."],
  xpPerHit: ["XP per hit", "Experience granted when the monster is hit."],
  xpOnDeath: ["XP on death", "Experience granted when the monster dies."],
  hitDamageMin: ["Minimum hit damage", "Lowest melee damage this monster can deal."],
  hitDamageMax: ["Maximum hit damage", "Highest melee damage this monster can deal."],
  respawnTime: ["Respawn time", "Delay before respawn, in seconds."],
  walkPattern: ["Walk animation", "Base animation used while moving."],
  attackPattern: ["Attack animation", "Base animation used when attacking."],
  deathPattern: ["Death animation", "Base animation used when dying."],
  soundAttack: ["Attack sound", "Sound played when the monster attacks."],
  soundDeath: ["Death sound", "Sound played when the monster dies."],
  soundHit: ["Hit sound", "Sound associated with taking or dealing a hit."],
  goldMin: ["Minimum gold", "Lowest gold amount dropped."],
  goldMax: ["Maximum gold", "Highest gold amount dropped."],
  defaultAggressive: ["Aggressive by default", "Whether this monster starts hostile without being provoked."],
  animateWhileStationary: ["Idle animation", "Keeps animation playing even when the monster is not moving."],
  stationaryAnimationPauseSeconds: ["Idle pause", "Pause between stationary animation loops, in seconds."],
  bodyPart: ["Equipment slot", "Body slot where this item is equipped."],
  appearanceEquippedPrimary: ["Equipped appearance", "Sprite base applied to the player/NPC when equipped."],
  appearanceInventory: ["Inventory icon", "Sprite shown for this item in inventory."],
  price: ["Price", "Gold value or spell learning cost, depending on the editor."],
  weight: ["Weight", "Inventory weight value."],
  armorClass: ["Armor class", "Defense bonus provided by this item."],
  dodgeLost: ["Dodge penalty", "Dodge value lost while this item is equipped."],
  minEnd: ["Minimum endurance", "Endurance required to use or equip this."],
  reqAttack: ["Required attack", "Attack stat required to use or equip this."],
  reqStr: ["Required strength", "Strength required to use or equip this."],
  reqAgi: ["Required agility", "Agility required to use or equip this."],
  minInt: ["Minimum intelligence", "Intelligence required to learn or use this spell/item."],
  minWis: ["Minimum wisdom", "Wisdom required to learn or use this spell/item."],
  dmgFormula: ["Damage formula", "Dice formula for weapon damage, e.g. \"2d6+1\" or \"1d8+self.str\"."],
  atkDelay: ["Attack delay", "Delay formula between attacks (T4C exhaust string)."],
  attackSpeed: ["Attack speed", "Weapon cooldown speed used by melee and bow attacks."],
  unique: ["Unique item", "Prevents normal stacking or duplicate handling where supported."],
  bow: ["Bow weapon", "Marks this weapon as a ranged bow."],
  unlimitedUse: ["Unlimited use", "Item can be used without being consumed."],
  canSummon: ["Can summon", "Allows summoning a creature when used."],
  radiance: ["Radiance", "Light radius emitted by this item when equipped or held."],
  nbCharges: ["Charges", "Number of uses before the item is consumed (0 = unlimited)."],
  lockName: ["Lock name", "Identifier of the key required to open this container."],
  lockDiff: ["Lock difficulty", "Difficulty of the lock for skill checks."],
  signText: ["Sign text", "Text displayed when reading this sign or book."],
  containerGold: ["Container gold", "Gold amount found inside this container."],
  globalRespawn: ["Global respawn", "Respawn delay in seconds across all instances."],
  localRespawn: ["Local respawn", "Respawn delay in seconds for this specific instance."],
  manaCost: ["Mana cost", "Mana required to cast this spell."],
  radius: ["Radius", "Stored spell radius value. Currently not used by the main runtime."],
  minLevel: ["Minimum level", "Player level required to learn or use this spell."],
  attack: ["Attack spell", "Targets monsters and applies offensive spell logic."],
  lineOfSight: ["Needs line of sight", "Requires a clear path to the target before casting."],
  iconId: ["Spell icon", "Sprite used as the spell icon."],
  projectileSpell: ["Projectile effect", "Base projectile animation launched toward the target."],
  impactSpell: ["Impact effect", "Animation played when the spell lands."],
  minDamage: ["Minimum effect", "Minimum damage for attack spells, or minimum heal for defensive spells."],
  maxDamage: ["Maximum effect", "Maximum damage for attack spells, or maximum heal for defensive spells."],
  sound: ["Cast sound", "Sound played when the action starts."],
  soundImpact: ["Impact sound", "Sound played when the spell lands."],
  cooldownSeconds: ["Cooldown", "Seconds before the spell can be cast again."],
  duration: ["Duration", "Human-readable or legacy duration field for spell effects."],
  type: ["Type", "Referenced NPC or monster definition."],
  x: ["X", "Tile X coordinate."],
  y: ["Y", "Tile Y coordinate."],
  z: ["Z", "Map layer/level coordinate."],
  stationary: ["Stationary", "Keeps this spawn from moving around."],
  aggressive: ["Aggressive", "Spawn starts hostile."],
  logicalName: ["Logical name", "Object mapping key used by object positions."],
  id: ["ID", "Numeric identifier."],
  sprite: ["Sprite", "Sprite used by this rule or object."],
  appearanceId: ["Appearance id", "Item appearance group (__OBJGROUP_* in the C++ client). Several appearances may deliberately share one generic icon."],
  gender: ["Gender", "Puppet this fallback belongs to: MALE (Pup* sprites) or FEMALE (Wo* sprites). Players always use the male puppet."],
  bodyPart: ["Body part", "Slot this sprite fills when nothing is equipped there."],
  appearance: ["Appearance", "Equipped sprite appearance that triggers this concealment rule."],
  triggerSlot: ["Trigger slot", "Body slot on which the appearance must be worn."],
  hiddenParts: ["Hidden parts", "Body parts concealed by this appearance."],
  hidesExplicit: ["Hide explicit parts", "Also removes body parts explicitly present in an NPC outfit."],
  width: ["Block width", "Mosaic block width in tiles. X selects the outer block."],
  height: ["Block height", "Mosaic block height in tiles. Y selects the frame inside the block."],
  frameCount: ["Frames", "Number of frames stored for this mosaic."],
  frames: ["Frames", "One sprite name per line, in X-major order (width x height entries). A single line containing &x / &y is a coordinate template expanded at lookup time."],
  clickAnimate: ["Click animation", "Object can animate when clicked."],
  mirror: ["Mirror sprite", "Render this object mirrored."],
  animateSound: ["Open sound", "Sound played when click animation starts."],
  reverseAnimateSound: ["Close sound", "Sound played when click animation reverses."],
  alwaysBehindEntities: ["Always behind entities", "Forces this object to render behind characters."],
  depthTileOffsetY: ["Depth Y offset", "Tile offset used for object depth sorting."],
  source: ["Source clan", "Clan relation source."],
  target: ["Target clan", "Clan relation target."],
  sourceX: ["Source X", "Teleport trigger X coordinate."],
  sourceY: ["Source Y", "Teleport trigger Y coordinate."],
  sourceZ: ["Source Z", "Teleport trigger Z coordinate."],
  targetX: ["Target X", "Teleport destination X coordinate."],
  targetY: ["Target Y", "Teleport destination Y coordinate."],
  targetZ: ["Target Z", "Teleport destination Z coordinate."],
};

const el = {
  nav: document.getElementById("sectionNav"),
  title: document.getElementById("sectionTitle"),
  eyebrow: document.getElementById("sectionEyebrow"),
  mapSelect: document.getElementById("mapSelect"),
  spriteBinPicker: document.getElementById("spriteBinPicker"),
  spriteBinSelect: document.getElementById("spriteBinSelect"),
  reloadBtn: document.getElementById("reloadBtn"),
  saveBtn: document.getElementById("saveBtn"),
  count: document.getElementById("recordCount"),
  selectedLabel: document.getElementById("selectedLabel"),
  status: document.getElementById("statusText"),
  dataWorkspace: document.getElementById("dataWorkspace"),
  spriteWorkspace: document.getElementById("spriteWorkspace"),
  filter: document.getElementById("filterInput"),
  recordList: document.getElementById("recordList"),
  newBtn: document.getElementById("newBtn"),
  duplicateBtn: document.getElementById("duplicateBtn"),
  deleteBtn: document.getElementById("deleteBtn"),
  editorTitle: document.getElementById("editorTitle"),
  dirtyBadge: document.getElementById("dirtyBadge"),
  entityPreview: document.getElementById("entityPreview"),
  entityPreviewImage: document.getElementById("entityPreviewImage"),
  entityPreviewComposite: document.getElementById("entityPreviewComposite"),
  entityPreviewPlaceholder: document.getElementById("entityPreviewPlaceholder"),
  entityPreviewTitle: document.getElementById("entityPreviewTitle"),
  entityPreviewHint: document.getElementById("entityPreviewHint"),
  formFields: document.getElementById("formFields"),
  relationEditorBlock: document.getElementById("relationEditorBlock"),
  editorTabs: document.getElementById("editorTabs"),
  editorTabDetails: document.getElementById("editorTabDetails"),
  editorTabDialogue: document.getElementById("editorTabDialogue"),
  jsonBlock: document.getElementById("jsonEditorBlock"),
  jsonEditor: document.getElementById("jsonEditor"),
  placementPreview: document.getElementById("placementPreview"),
  placementPreviewStage: document.getElementById("placementPreviewStage"),
  placementPreviewTitle: document.getElementById("placementPreviewTitle"),
  placementPreviewImage: document.getElementById("placementPreviewImage"),
  spriteFilter: document.getElementById("spriteFilterInput"),
  spriteList: document.getElementById("spriteList"),
  spritePager: document.getElementById("spritePager"),
  spritePrevBtn: document.getElementById("spritePrevBtn"),
  spriteNextBtn: document.getElementById("spriteNextBtn"),
  spritePageLabel: document.getElementById("spritePageLabel"),
  spriteExportBtn: document.getElementById("spriteExportBtn"),
  spriteDeleteBtn: document.getElementById("spriteDeleteBtn"),
  uploadZone: document.getElementById("uploadZone"),
  fileInput: document.getElementById("fileInput"),
  previewImg: document.getElementById("previewImage"),
  previewPlaceholder: document.getElementById("previewPlaceholder"),
  spriteName: document.getElementById("spriteName"),
  spriteMeta: document.getElementById("spriteMeta"),
  renameInput: document.getElementById("renameInput"),
  off1x: document.getElementById("off1xInput"),
  off1y: document.getElementById("off1yInput"),
  off2x: document.getElementById("off2xInput"),
  off2y: document.getElementById("off2yInput"),
  saveSpriteBtn: document.getElementById("saveSpriteBtn"),
  toast: document.getElementById("actionToast"),
  toastBody: document.getElementById("toastBody"),
  pageLoader: document.getElementById("pageLoader"),
  loaderText: document.getElementById("loaderText"),
};

let loadingDepth = 0;
let spriteFilterTimer = null;

function sectionConfig() {
  return sectionGroups.flatMap((g) => g.items).find((item) => item.id === state.section);
}

function endpointFor(config) {
  let endpoint = config.endpoint;
  if (config.mapScoped) {
    endpoint += `&map=${encodeURIComponent(state.mapPath || "")}`;
  }
  return endpoint;
}

async function apiJson(url, options = {}) {
  const res = await fetch(url, options);
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

function setStatus(text) {
  el.status.textContent = text;
}

function toast(text) {
  el.toastBody.textContent = text;
  window.bootstrap.Toast.getOrCreateInstance(el.toast, { autohide: true, delay: 1800 }).show();
}

function showLoader(text = "Loading...") {
  loadingDepth++;
  el.loaderText.textContent = text;
  el.pageLoader.classList.remove("hidden");
}

function hideLoader() {
  loadingDepth = Math.max(0, loadingDepth - 1);
  if (loadingDepth === 0) {
    el.pageLoader.classList.add("hidden");
  }
}

async function withLoader(text, task) {
  showLoader(text);
  try {
    return await task();
  } finally {
    hideLoader();
  }
}

function buildNav() {
  el.nav.replaceChildren();
  sectionGroups.forEach((group) => {
    const title = document.createElement("div");
    title.className = "nav-group-title";
    title.textContent = group.title;
    el.nav.appendChild(title);
    group.items.forEach((item) => {
      const button = document.createElement("button");
      button.className = "nav-button";
      button.textContent = item.label;
      button.addEventListener("click", () => selectSection(item.id));
      el.nav.appendChild(button);
    });
  });
}

function updateNav() {
  const buttons = el.nav.querySelectorAll(".nav-button");
  const items = sectionGroups.flatMap((g) => g.items);
  buttons.forEach((button, index) => button.classList.toggle("active", items[index]?.id === state.section));
}

async function loadMaps() {
  const data = await apiJson("/api/maps");
  state.maps = data.items || [];
  el.mapSelect.replaceChildren();
  state.maps.forEach((map) => {
    const option = document.createElement("option");
    option.value = map.path;
    option.textContent = map.displayName || map.fileName || map.path;
    el.mapSelect.appendChild(option);
  });
  const worldMap = state.maps.find((map) =>
    String(map.path || "").toLowerCase().endsWith("/worldmap.mapbin") ||
    String(map.fileName || "").toLowerCase() === "worldmap.mapbin"
  );
  state.mapPath = worldMap?.path || state.maps[0]?.path || "";
  el.mapSelect.value = state.mapPath;
}

async function selectSection(id) {
  state.section = id;
  state.selectedIndex = -1;
  state.dirty = false;
  updateNav();
  await withLoader(`Loading ${sectionConfig().label}...`, loadCurrent);
}

async function loadCurrent() {
  const config = sectionConfig();
  el.title.textContent = config.label;
  el.eyebrow.textContent = config.kind === "sprites" ? "Assets" : "Content";
  el.selectedLabel.textContent = "None";
  el.dirtyBadge.classList.add("hidden");
  el.placementPreview.classList.add("hidden");
  if (config.kind === "sprites") {
    el.dataWorkspace.classList.add("hidden");
    el.spriteWorkspace.classList.remove("hidden");
    el.spriteBinPicker.classList.remove("hidden");
    await loadSpriteBins();
    await loadSprites();
    return;
  }
  el.spriteBinPicker.classList.add("hidden");
  el.spriteWorkspace.classList.add("hidden");
  el.dataWorkspace.classList.remove("hidden");
  if (config.kind === "collisionRules") {
    await ensureSpriteIndex();
    state.collisionRules = await apiJson(endpointFor(config));
    state.items = state.sprites.map((sprite) => ({ sprite: sprite.name }));
    state.items.sort((a, b) => a.sprite.localeCompare(b.sprite));
    state.selectedIndex = state.items.length ? 0 : -1;
    renderList();
    renderEditor();
    setStatus(`Loaded ${state.items.length} sprite(s)`);
    return;
  }
  const data = await apiJson(endpointFor(config));
  if (config.mode === "json") {
    state.items = [data];
  } else {
    state.items = data.items || [];
  }
  if (state.section === "clanRelations") {
    state.clanOptions = (data.clans || []).filter(Boolean).sort((a, b) => a.localeCompare(b));
  }
  state.selectedIndex = state.items.length ? 0 : -1;
  renderList();
  renderEditor();
  setStatus(`Loaded ${state.items.length} record(s)`);
}

function renderList() {
  const config = sectionConfig();
  const term = el.filter.value.trim().toLowerCase();
  el.recordList.replaceChildren();
  state.items.forEach((item, index) => {
    const title = displayContentValue(item[config.key] ?? `${config.label} ${index + 1}`);
    const subtitle = subtitleFor(item, config);
    if (state.section === "collisionRules" && state.collisionDefinedOnly && !collisionHasRule(item.sprite)) return;
    if (term && `${title} ${subtitle}`.toLowerCase().indexOf(term) < 0) return;
    const row = document.createElement("button");
    row.type = "button";
    row.className = "record-row";
    row.classList.toggle("active", index === state.selectedIndex);
    row.innerHTML = `<strong></strong><span></span>`;
    row.querySelector("strong").textContent = title;
    row.querySelector("span").textContent = subtitle;
    row.addEventListener("click", () => {
      state.selectedIndex = index;
      renderList();
      renderEditor();
    });
    el.recordList.appendChild(row);
  });
  el.count.textContent = String(el.recordList.children.length);
}

function subtitleFor(item, config) {
  if (state.section === "collisionRules") {
    const rule = collisionExactRule(item.sprite);
    if (rule) return `${(rule.tiles || []).length} collision / ${(rule.clearTiles || []).length} clear`;
    return collisionInheritedRule(item.sprite) ? "Inherited rule" : "";
  }
  if (!config.subtitle) return "";
  return config.subtitle.split(",")
    .map((key) => displayContentValue(item[key] ?? ""))
    .filter((v) => String(v).length)
    .join(" / ");
}

function collisionExactRule(spriteName) {
  const rules = state.collisionRules?.exactSprites || [];
  const key = normalizeContentKey(spriteName);
  return rules.find((rule) => normalizeContentKey(rule.sprite) === key) || null;
}

function collisionInheritedRule(spriteName) {
  const rules = state.collisionRules?.nameContainsRules || [];
  const key = normalizeContentKey(spriteName);
  return rules.find((entry) => {
    const contains = entry.contains || [];
    return contains.some((part) => key.includes(normalizeContentKey(part)));
  }) || null;
}

function collisionHasRule(spriteName) {
  return Boolean(collisionExactRule(spriteName) || collisionInheritedRule(spriteName));
}

function ensureCollisionExactRule(spriteName) {
  if (!state.collisionRules) state.collisionRules = {};
  if (!Array.isArray(state.collisionRules.exactSprites)) state.collisionRules.exactSprites = [];
  let rule = collisionExactRule(spriteName);
  if (!rule) {
    rule = { sprite: spriteName, value: 1, tiles: [], clearTiles: [] };
    state.collisionRules.exactSprites.push(rule);
    state.collisionRules.exactSprites.sort((a, b) => String(a.sprite || "").localeCompare(String(b.sprite || "")));
  }
  if (!Array.isArray(rule.tiles)) rule.tiles = [];
  if (!Array.isArray(rule.clearTiles)) rule.clearTiles = [];
  return rule;
}

function removeCollisionExactRule(spriteName) {
  if (!Array.isArray(state.collisionRules?.exactSprites)) return;
  const key = normalizeContentKey(spriteName);
  state.collisionRules.exactSprites = state.collisionRules.exactSprites
    .filter((rule) => normalizeContentKey(rule.sprite) !== key);
}

function collisionTileKey(tile) {
  return `${Number(tile.x ?? tile[0]) || 0},${Number(tile.y ?? tile[1]) || 0}`;
}

function collisionTilesToSet(tiles) {
  return new Set((tiles || []).map(collisionTileKey));
}

function collisionSetToTiles(set) {
  return Array.from(set)
    .map((key) => key.split(",").map((part) => Number(part)))
    .filter(([x, y]) => Number.isFinite(x) && Number.isFinite(y))
    .sort((a, b) => a[1] - b[1] || a[0] - b[0])
    .map(([x, y]) => ({ x, y }));
}

function collisionSpriteMeta(spriteName) {
  return state.sprites.find((sprite) => normalizeContentKey(sprite.name) === normalizeContentKey(spriteName)) || null;
}

function collisionGridBounds(spriteName, rule) {
  const meta = collisionSpriteMeta(spriteName);
  const width = Math.max(1, Math.ceil((meta?.width || 64) / 32));
  const height = Math.max(1, Math.ceil((meta?.height || 64) / 16));
  const minX = Math.floor((meta?.off1X || 0) / 32);
  const minY = Math.floor((meta?.off1Y || 0) / 16) + 1 - height;
  const maxX = minX + width;
  const maxY = minY + height;
  const keys = [...(rule?.tiles || []), ...(rule?.clearTiles || [])];
  const bounds = keys.reduce((bounds, tile) => {
    const x = Number(tile.x ?? tile[0]) || 0;
    const y = Number(tile.y ?? tile[1]) || 0;
    bounds.minX = Math.min(bounds.minX, x);
    bounds.minY = Math.min(bounds.minY, y);
    bounds.maxX = Math.max(bounds.maxX, x + 1);
    bounds.maxY = Math.max(bounds.maxY, y + 1);
    return bounds;
  }, { minX, minY, maxX, maxY });
  const minCellsX = 10;
  const minCellsY = 8;
  const padX = Math.max(1, Math.ceil((minCellsX - (bounds.maxX - bounds.minX)) / 2));
  const padY = Math.max(1, Math.ceil((minCellsY - (bounds.maxY - bounds.minY)) / 2));
  return {
    minX: bounds.minX - padX,
    minY: bounds.minY - padY,
    maxX: bounds.maxX + padX,
    maxY: bounds.maxY + padY,
  };
}

function renderCollisionRuleEditor(item) {
  const spriteName = item.sprite;
  const exactRule = collisionExactRule(spriteName);
  const inherited = exactRule ? null : collisionInheritedRule(spriteName);
  const rule = exactRule || inherited || { sprite: spriteName, value: 1, tiles: [], clearTiles: [] };
  const shell = document.createElement("div");
  shell.className = "collision-editor";

  const toolbar = document.createElement("div");
  toolbar.className = "collision-toolbar";
  const definedToggle = createCollisionToggle("Defined only", state.collisionDefinedOnly, (checked) => {
    state.collisionDefinedOnly = checked;
    renderList();
  });
  toolbar.appendChild(definedToggle);
  toolbar.appendChild(createCollisionModeButton("collision", "Paint collision"));
  toolbar.appendChild(createCollisionModeButton("clear", "Paint clear"));
  toolbar.appendChild(createCollisionModeButton("erase", "Erase"));
  const clearBtn = document.createElement("button");
  clearBtn.type = "button";
  clearBtn.className = "btn btn-danger";
  clearBtn.textContent = "Clear rule";
  clearBtn.addEventListener("click", () => {
    removeCollisionExactRule(spriteName);
    markDirty();
    renderList();
    renderEditor();
  });
  toolbar.appendChild(clearBtn);
  shell.appendChild(toolbar);

  const note = document.createElement("div");
  note.className = "collision-note";
  note.textContent = inherited && !exactRule ? "Inherited from a name-contains rule. Drawing here creates an exact rule for this sprite." : "Red blocks collision. Green explicitly clears collision.";
  shell.appendChild(note);

  const stage = document.createElement("div");
  stage.className = "collision-stage";
  const canvas = document.createElement("canvas");
  canvas.className = "collision-canvas";
  stage.appendChild(canvas);
  shell.appendChild(stage);
  el.formFields.appendChild(shell);

  drawCollisionCanvas(canvas, spriteName, rule);
  centerCollisionStage(stage);

  const paintAt = (event, isFirst) => {
    const tile = collisionTileFromEvent(canvas, event);
    if (!tile) return;
    const activeRule = ensureCollisionExactRule(spriteName);
    const collisionSet = collisionTilesToSet(activeRule.tiles);
    const clearSet = collisionTilesToSet(activeRule.clearTiles);
    const key = `${tile.x},${tile.y}`;
    if (isFirst) {
      const activeSet = state.collisionPaintMode === "clear" ? clearSet : collisionSet;
      state.collisionDragMode = state.collisionPaintMode === "erase" || activeSet.has(key) ? "erase" : state.collisionPaintMode;
    }
    const mode = state.collisionDragMode || state.collisionPaintMode;
    if (mode === "clear") {
      clearSet.add(key);
      collisionSet.delete(key);
    } else if (mode === "erase") {
      collisionSet.delete(key);
      clearSet.delete(key);
    } else {
      collisionSet.add(key);
      clearSet.delete(key);
    }
    activeRule.tiles = collisionSetToTiles(collisionSet);
    activeRule.clearTiles = collisionSetToTiles(clearSet);
    if (!activeRule.tiles.length && !activeRule.clearTiles.length) {
      removeCollisionExactRule(spriteName);
    }
    markDirty();
    renderList();
    drawCollisionCanvas(canvas, spriteName, collisionExactRule(spriteName) || { tiles: [], clearTiles: [] });
    centerCollisionStage(stage);
  };

  canvas.addEventListener("pointerdown", (event) => {
    event.preventDefault();
    canvas.setPointerCapture(event.pointerId);
    paintAt(event, true);
  });
  canvas.addEventListener("pointermove", (event) => {
    if (event.buttons !== 1) return;
    paintAt(event, false);
  });
  canvas.addEventListener("pointerup", () => {
    state.collisionDragMode = null;
  });
  canvas.addEventListener("contextmenu", (event) => event.preventDefault());
}

function createCollisionToggle(text, checked, onChange) {
  const label = document.createElement("label");
  label.className = "collision-toggle";
  const input = document.createElement("input");
  input.type = "checkbox";
  input.className = "toggle-input";
  input.checked = checked;
  const toggle = document.createElement("span");
  toggle.className = "ios-toggle";
  const caption = document.createElement("span");
  caption.textContent = text;
  label.append(input, toggle, caption);
  input.addEventListener("change", () => onChange(input.checked));
  return label;
}

function createCollisionModeButton(mode, text) {
  const button = document.createElement("button");
  button.type = "button";
  button.className = `btn btn-sm ${state.collisionPaintMode === mode ? "btn-dark" : "btn-outline-dark"}`;
  button.textContent = text;
  button.addEventListener("click", () => {
    state.collisionPaintMode = mode;
    renderEditor();
  });
  return button;
}

function drawCollisionCanvas(canvas, spriteName, rule) {
  const gridW = 32;
  const gridH = 16;
  const bounds = collisionGridBounds(spriteName, rule);
  const cellsX = Math.max(1, bounds.maxX - bounds.minX);
  const cellsY = Math.max(1, bounds.maxY - bounds.minY);
  const scale = 2;
  canvas.dataset.minX = String(bounds.minX);
  canvas.dataset.minY = String(bounds.minY);
  canvas.dataset.cellsY = String(cellsY);
  canvas.dataset.scale = String(scale);
  canvas.width = cellsX * gridW * scale;
  canvas.height = cellsY * gridH * scale;
  canvas.style.width = `${canvas.width}px`;
  canvas.style.height = `${canvas.height}px`;
  const ctx = canvas.getContext("2d");
  ctx.imageSmoothingEnabled = false;
  ctx.clearRect(0, 0, canvas.width, canvas.height);
  ctx.fillStyle = "#0f172a";
  ctx.fillRect(0, 0, canvas.width, canvas.height);

  const img = new Image();
  img.onload = () => {
    ctx.globalAlpha = 0.72;
    const meta = collisionSpriteMeta(spriteName);
    const logicalW = img.width / gridW;
    const logicalH = img.height / gridH;
    const logicalX = (meta?.off1X || 0) / gridW;
    const logicalY = (meta?.off1Y || 0) / gridH + 1 - logicalH;
    const x = (logicalX - bounds.minX) * gridW * scale;
    const y = (logicalY - bounds.minY) * gridH * scale;
    ctx.drawImage(img, x, y, img.width * scale, img.height * scale);
    ctx.globalAlpha = 1;
    drawCollisionGridOverlay(ctx, bounds, rule, gridW, gridH, scale);
  };
  img.onerror = () => drawCollisionGridOverlay(ctx, bounds, rule, gridW, gridH, scale);
  img.src = spriteImageUrl(spriteName);
}

function centerCollisionStage(stage) {
  requestAnimationFrame(() => {
    stage.scrollLeft = Math.max(0, (stage.scrollWidth - stage.clientWidth) / 2);
    stage.scrollTop = Math.max(0, (stage.scrollHeight - stage.clientHeight) / 2);
  });
}

function drawCollisionGridOverlay(ctx, bounds, rule, gridW, gridH, scale) {
  const cellsX = Math.max(1, bounds.maxX - bounds.minX);
  const cellsY = Math.max(1, bounds.maxY - bounds.minY);
  const collisionSet = collisionTilesToSet(rule.tiles);
  const clearSet = collisionTilesToSet(rule.clearTiles);
  for (let gy = 0; gy < cellsY; gy += 1) {
    for (let gx = 0; gx < cellsX; gx += 1) {
      const dx = bounds.minX + gx;
      const dy = bounds.minY + gy;
      const key = `${dx},${dy}`;
      const x = gx * gridW * scale;
      const y = gy * gridH * scale;
      if (collisionSet.has(key)) {
        ctx.fillStyle = "rgba(239, 68, 68, 0.48)";
        ctx.fillRect(x + 2, y + 2, gridW * scale - 4, gridH * scale - 4);
      } else if (clearSet.has(key)) {
        ctx.fillStyle = "rgba(34, 197, 94, 0.46)";
        ctx.fillRect(x + 2, y + 2, gridW * scale - 4, gridH * scale - 4);
      }
    }
  }
  ctx.strokeStyle = "rgba(226, 232, 240, 0.45)";
  ctx.lineWidth = 1;
  for (let x = 0; x <= cellsX; x += 1) {
    const px = x * gridW * scale + 0.5;
    ctx.beginPath();
    ctx.moveTo(px, 0);
    ctx.lineTo(px, cellsY * gridH * scale);
    ctx.stroke();
  }
  for (let y = 0; y <= cellsY; y += 1) {
    const py = y * gridH * scale + 0.5;
    ctx.beginPath();
    ctx.moveTo(0, py);
    ctx.lineTo(cellsX * gridW * scale, py);
    ctx.stroke();
  }
}

function collisionTileFromEvent(canvas, event) {
  const rect = canvas.getBoundingClientRect();
  const scale = Number(canvas.dataset.scale || 1);
  const gridW = 32 * scale;
  const gridH = 16 * scale;
  const x = (event.clientX - rect.left) * (canvas.width / rect.width);
  const y = (event.clientY - rect.top) * (canvas.height / rect.height);
  const gx = Math.floor(x / gridW);
  const gy = Math.floor(y / gridH);
  const cellsY = Number(canvas.dataset.cellsY || 1);
  if (gx < 0 || gy < 0 || gy >= cellsY) return null;
  return {
    x: Number(canvas.dataset.minX || 0) + gx,
    y: Number(canvas.dataset.minY || 0) + gy,
  };
}

function renderEditor() {
  const config = sectionConfig();
  const item = state.items[state.selectedIndex];
  el.formFields.replaceChildren();
  el.relationEditorBlock.classList.add("hidden");
  el.relationEditorBlock.replaceChildren();
  el.jsonBlock.classList.add("hidden");
  el.entityPreview.classList.add("hidden");
  el.editorTabDialogue.replaceChildren();
  el.editorTabs.classList.add("hidden");
  if (!item) {
    el.editorTitle.textContent = "No record selected";
    el.selectedLabel.textContent = "None";
    showEditorTab("details");
    return;
  }
  el.editorTitle.textContent = displayContentValue(item[config.key] ?? config.label);
  el.selectedLabel.textContent = displayContentValue(item[config.key] ?? state.selectedIndex + 1);
  if (config.kind === "collisionRules") {
    renderCollisionRuleEditor(item);
    return;
  }
  if (config.mode === "json") {
    el.jsonBlock.classList.remove("hidden");
    el.jsonEditor.value = JSON.stringify(item, null, 2);
    return;
  }
  if (state.section === "npcSpawns" && !state.npcOptions.length) {
    ensureNpcOptions().then(() => {
      if (state.section === "npcSpawns" && state.items[state.selectedIndex] === item) {
        renderEditor();
      }
    });
  }
  if (state.section === "monsterSpawns" && !state.monsterOptions.length) {
    ensureMonsterOptions().then(() => {
      if (state.section === "monsterSpawns" && state.items[state.selectedIndex] === item) {
        renderEditor();
      }
    });
  }
  if (state.section === "quests" && (!state.npcOptions.length || !state.monsterOptions.length)) {
    Promise.all([ensureNpcOptions(), ensureMonsterOptions()]).then(() => {
      if (state.section === "quests" && state.items[state.selectedIndex] === item) {
        renderEditor();
      }
    });
  }
  if (state.section === "monsters" && (!state.monsterPatternOptions.length || !state.soundOptions.length)) {
    ensureMonsterEditorOptions().then(() => {
      if (state.section === "monsters" && state.items[state.selectedIndex] === item) {
        renderEditor();
      }
    });
  }
  if (state.section === "spells" && !state.spellEditorOptionsLoaded) {
    ensureSpellEditorOptions().then(() => {
      if (state.section === "spells" && state.items[state.selectedIndex] === item) {
        renderEditor();
      }
    });
  }
  if (state.section === "items" && !state.itemSpriteOptions.length) {
    ensureItemSpriteOptions().then(() => {
      if (state.section === "items" && state.items[state.selectedIndex] === item) {
        renderEditor();
      }
    });
  }
  if (state.section === "npcs" && !state.animatedSpriteBaseOptions.length) {
    ensureAnimatedSpriteBaseOptions().then(() => {
      if (state.section === "npcs" && state.items[state.selectedIndex] === item) {
        renderEditor();
      }
    });
  }
  if (state.section === "npcs" && !state.spellNameOptions.length) {
    ensureSpellNameOptions().then(() => {
      if (state.section === "npcs" && state.items[state.selectedIndex] === item) {
        renderEditor();
      }
    });
  }
  if (state.section === "npcs"
      && (!state.itemKeyOptions.length || !state.trainableStatIdOptions.length)) {
    ensureNpcShopTrainOptions().then(() => {
      if (state.section === "npcs" && state.items[state.selectedIndex] === item) {
        renderEditor();
      }
    });
  }
  if (["objects", "decorRules", "itemIcons", "appearanceDefaults"].includes(state.section) && !state.itemSpriteOptions.length) {
    ensureItemSpriteOptions().then(() => {
      if (["objects", "decorRules", "itemIcons", "appearanceDefaults"].includes(state.section) && state.items[state.selectedIndex] === item) {
        renderEditor();
      }
    });
  }
  if (state.section === "objects" && !state.soundOptions.length) {
    ensureSoundOptions().then(() => {
      if (state.section === "objects" && state.items[state.selectedIndex] === item) {
        renderEditor();
      }
    });
  }
  if (state.section === "objectPositions" && !state.objectDefinitionOptions.length) {
    ensureObjectDefinitionOptions().then(() => {
      if (state.section === "objectPositions" && state.items[state.selectedIndex] === item) {
        renderEditor();
      }
    });
  }
  const visible = fields[state.section] || Object.keys(item).filter((key) => typeof item[key] !== "object");
  visible.forEach((descriptor) => {
    const [name, type = "text"] = descriptor.split(":");
    if (!shouldShowField(name, item)) {
      return;
    }
    const label = document.createElement("label");
    label.appendChild(createFieldLabel(name, item));
    let input;
    if (state.section === "spells" && type === "boolean" && (name === "attack" || name === "lineOfSight")) {
      input = document.createElement("input");
      input.type = "checkbox";
      input.className = "toggle-input";
      input.checked = Boolean(item[name]);
      const toggle = document.createElement("span");
      toggle.className = "ios-toggle";
      const toggleLabel = document.createElement("span");
      toggleLabel.className = "toggle-row";
      toggleLabel.appendChild(input);
      toggleLabel.appendChild(toggle);
      label.appendChild(toggleLabel);
      input.addEventListener("change", () => {
        item[name] = input.checked;
        renderEntityPreview(item);
        markDirty();
        renderList();
      });
      el.formFields.appendChild(label);
      return;
    }
    if (state.section === "spells" && ["iconId", "impactSpell", "projectileSpell"].includes(name)) {
      label.appendChild(createSpriteDropdown(item, name));
      el.formFields.appendChild(label);
      return;
    }
    if (state.section === "items" && ["appearanceInventory", "appearanceEquippedPrimary"].includes(name)) {
      label.appendChild(createSpriteDropdown(item, name));
      el.formFields.appendChild(label);
      return;
    }
    if (state.section === "monsters" && ["walkPattern", "attackPattern", "deathPattern"].includes(name)) {
      label.appendChild(createSpriteDropdown(item, name));
      el.formFields.appendChild(label);
      return;
    }
    if (state.section === "npcs" && name === "spriteBase") {
      label.appendChild(createSpriteDropdown(item, name));
      el.formFields.appendChild(label);
      return;
    }
    if (["objects", "decorRules", "itemIcons", "appearanceDefaults"].includes(state.section) && name === "sprite") {
      label.appendChild(createSpriteDropdown(item, name));
      el.formFields.appendChild(label);
      return;
    }
    if (state.section === "spells" && ["sound", "soundImpact"].includes(name)) {
      input = document.createElement("select");
      appendOptions(input, ["", ...state.soundOptions], item[name] || "");
    } else if (state.section === "monsters" && ["soundAttack", "soundDeath", "soundHit"].includes(name)) {
      input = document.createElement("select");
      appendOptions(input, ["", ...state.soundOptions], item[name] || "");
    } else if (state.section === "objects" && ["animateSound", "reverseAnimateSound"].includes(name)) {
      input = document.createElement("select");
      appendOptions(input, ["", ...state.soundOptions], item[name] || "");
    } else if (state.section === "items" && name === "bodyPart") {
      input = document.createElement("select");
      appendOptions(input, itemBodyPartOptions(item[name]), item[name] || "");
    } else if (state.section === "concealmentRules" && name === "triggerSlot") {
      input = document.createElement("select");
      appendOptions(input, itemBodyPartOptions(item[name]), item[name] || "");
    } else if (state.section === "concealmentRules" && name === "hiddenParts") {
      input = document.createElement("select");
      input.multiple = true;
      const selected = new Set(String(item[name] || "").split(",").map((value) => value.trim()).filter(Boolean));
      itemBodyPartOptions("").filter(Boolean).forEach((bodyPart) => {
        const option = document.createElement("option");
        option.value = bodyPart;
        option.textContent = bodyPart;
        option.selected = selected.has(bodyPart);
        input.appendChild(option);
      });
    } else if (state.section === "objectPositions" && name === "name") {
      input = document.createElement("select");
      appendOptions(input, objectDefinitionNameOptions(item[name]), item[name] || "");
    } else if (state.section === "clanRelations" && ["source", "target"].includes(name)) {
      input = document.createElement("select");
      appendOptions(input, clanNameOptions(item[name]), item[name] || "NEUTRAL");
    } else if (type === "textarea") {
      input = document.createElement("textarea");
      input.rows = 4;
    } else if (type === "boolean" && isToggleField(name)) {
      input = document.createElement("input");
      input.type = "checkbox";
      input.className = "toggle-input";
      input.checked = Boolean(item[name]);
      const toggle = document.createElement("span");
      toggle.className = "ios-toggle";
      const toggleLabel = document.createElement("span");
      toggleLabel.className = "toggle-row";
      toggleLabel.appendChild(input);
      toggleLabel.appendChild(toggle);
      label.appendChild(toggleLabel);
      input.addEventListener("change", () => {
        item[name] = input.checked;
        renderEntityPreview(item);
        markDirty();
        renderList();
      });
      el.formFields.appendChild(label);
      return;
    } else if (type === "boolean") {
      input = document.createElement("select");
      ["false", "true"].forEach((value) => {
        const option = document.createElement("option");
        option.value = value;
        option.textContent = value;
        input.appendChild(option);
      });
    } else if (state.section === "npcSpawns" && name === "type") {
      input = document.createElement("select");
      if (!state.npcOptions.length) {
        const option = document.createElement("option");
        option.value = item[name] || "";
        option.textContent = item[name] || "Loading NPCs...";
        input.appendChild(option);
      } else {
        state.npcOptions.forEach((npcName) => {
          const option = document.createElement("option");
          option.value = npcName;
          option.textContent = npcName;
          input.appendChild(option);
        });
      }
    } else if (state.section === "monsterSpawns" && name === "type") {
      input = document.createElement("select");
      if (!state.monsterOptions.length) {
        const option = document.createElement("option");
        option.value = item[name] || "";
        option.textContent = item[name] || "Loading monsters...";
        input.appendChild(option);
      } else {
        if (item[name] && !state.monsterOptions.includes(item[name])) {
          const option = document.createElement("option");
          option.value = item[name];
          option.textContent = item[name];
          input.appendChild(option);
        }
        state.monsterOptions.forEach((monsterName) => {
          const option = document.createElement("option");
          option.value = monsterName;
          option.textContent = monsterName;
          input.appendChild(option);
        });
      }
    } else if (state.section === "quests" && name === "giverNpc") {
      input = document.createElement("select");
      appendOptions(input, ["", ...state.npcOptions], item[name] || "");
    } else if (state.section === "quests" && name === "targetMonster") {
      input = document.createElement("select");
      appendOptions(input, ["", ...state.monsterOptions], item[name] || "");
    } else {
      input = document.createElement("input");
      input.type = type === "number" ? "number" : "text";
      if (type === "number") input.step = "any";
    }
    input.className = `form-control form-control-sm ${type === "text" ? "mono" : ""}`;
    if (!input.multiple) input.value = item[name] ?? "";
    input.addEventListener("input", () => {
      item[name] = input.multiple
        ? Array.from(input.selectedOptions).map((option) => option.value).join(",")
        : type === "number" ? Number(input.value || 0) : type === "boolean" ? input.value === "true" : input.value;
      if (!el.jsonBlock.classList.contains("hidden")) {
        el.jsonEditor.value = JSON.stringify(item, null, 2);
      }
      renderEntityPreview(item);
      renderPlacementPreview(item);
      markDirty();
      renderList();
      if (shouldRerenderForContext(name)) {
        renderEditor();
      }
    });
    label.appendChild(input);
    el.formFields.appendChild(label);
  });
  const complexKeys = Object.keys(item).filter((key) => typeof item[key] === "object" && item[key] !== null && shouldShowComplexKey(key, item));
  if (complexKeys.length) {
    el.jsonBlock.classList.remove("hidden");
    el.jsonEditor.value = JSON.stringify(item, null, 2);
  }
  renderRelationEditor(item);
  renderDialogGraph(item);
  // Keep the dialogue tab selected while browsing NPCs that have one, but fall
  // back to details when the newly selected record has no graph.
  showEditorTab(state.editorTab === "dialogue" && !el.editorTabs.classList.contains("hidden")
    ? "dialogue" : "details");
  renderEntityPreview(item);
  renderPlacementPreview(item);
}

function shouldShowField(name, item) {
  if (state.section === "npcs" && name === "welcomeText") {
    return false;
  }
  if (state.section === "objects" && ["animateSound", "reverseAnimateSound"].includes(name)) {
    return Boolean(item.clickAnimate);
  }
  if (state.section === "monsters" && name === "stationaryAnimationPauseSeconds") {
    return Boolean(item.animateWhileStationary);
  }
  if (state.section === "spells" && ["lineOfSight", "projectileSpell"].includes(name)) {
    return Boolean(item.attack);
  }
  if (state.section === "items" && name === "appearanceEquippedPrimary") {
    return Boolean(String(item.bodyPart || "").trim());
  }
  if (state.section === "items" && name === "attackSpeed") {
    return item.bodyPart === "WEAPON" || Boolean(item.bow);
  }
  if (state.section === "npcs" && name === "spriteBase") {
    return !Array.isArray(item.parts) || item.parts.length === 0;
  }
  return true;
}

function shouldShowComplexKey(key, item) {
  if (state.section === "npcs" && ["parts", "topics"].includes(key)) {
    // Each has its own dedicated editor; raw JSON here would be unreadable.
    return false;
  }
  return true;
}

function shouldRerenderForContext(name) {
  return (state.section === "objects" && name === "clickAnimate")
    || (state.section === "monsters" && name === "animateWhileStationary")
    || (state.section === "spells" && name === "attack")
    || (state.section === "items" && ["bodyPart", "bow"].includes(name));
}

function displayFieldName(name, item) {
  return fieldMeta(name, item).label;
}

function createFieldLabel(name, item) {
  const meta = fieldMeta(name, item);
  return createStaticFieldLabel(meta.label, meta.help);
}

function createStaticFieldLabel(labelText, helpText) {
  const row = document.createElement("span");
  row.className = "field-label-row";
  const text = document.createElement("span");
  text.className = "field-label";
  text.textContent = labelText;
  row.appendChild(text);
  if (helpText) {
    const help = document.createElement("span");
    help.className = "field-help";
    help.tabIndex = 0;
    help.setAttribute("aria-label", helpText);
    help.dataset.tooltip = helpText;
    help.textContent = "?";
    row.appendChild(help);
  }
  return row;
}

function fieldMeta(name, item) {
  if (state.section === "groundMosaics" && name === "id") {
    return { label: "Mosaic id", help: "Legacy mapping id declaring this mosaic, as hexadecimal (e.g. 0x2c)." };
  }
  if (state.section === "appearanceDefaults" && name === "sprite") {
    return { label: "Sprite", help: "Animated body-part base drawn when this slot is empty (e.g. PupNakedBody). Not an item key." };
  }
  const [label, help] = FIELD_META[name] || [humanizeFieldName(name), "Editor field used by this content record."];
  return { label, help };
}

function humanizeFieldName(name) {
  return String(name || "")
    .replace(/([a-z0-9])([A-Z])/g, "$1 $2")
    .replace(/[_-]+/g, " ")
    .replace(/\b\w/g, (char) => char.toUpperCase());
}

let floatingTooltip;

function showFieldTooltip(target) {
  const text = target?.dataset?.tooltip;
  if (!text) return;
  if (!floatingTooltip) {
    floatingTooltip = document.createElement("div");
    floatingTooltip.className = "floating-tooltip";
    document.body.appendChild(floatingTooltip);
  }
  floatingTooltip.textContent = text;
  floatingTooltip.classList.add("visible");
  positionFieldTooltip(target);
}

function positionFieldTooltip(target) {
  if (!floatingTooltip || !target) return;
  const rect = target.getBoundingClientRect();
  floatingTooltip.style.left = "0px";
  floatingTooltip.style.top = "0px";
  const tooltipRect = floatingTooltip.getBoundingClientRect();
  let left = rect.left + rect.width / 2 - tooltipRect.width / 2;
  left = Math.max(12, Math.min(window.innerWidth - tooltipRect.width - 12, left));
  let top = rect.top - tooltipRect.height - 10;
  if (top < 12) top = rect.bottom + 10;
  floatingTooltip.style.left = `${left}px`;
  floatingTooltip.style.top = `${top}px`;
}

function hideFieldTooltip() {
  if (floatingTooltip) floatingTooltip.classList.remove("visible");
}

function renderRelationEditor(item) {
  if (state.section !== "npcs" || !item) return;
  if (!Array.isArray(item.parts)) item.parts = [];
  el.relationEditorBlock.classList.remove("hidden");
  el.relationEditorBlock.replaceChildren();

  const header = document.createElement("div");
  header.className = "relation-editor-header";
  const title = document.createElement("div");
  title.appendChild(createStaticFieldLabel("NPC parts", "Body-part composition used when the NPC does not use a single sprite base."));
  const strong = document.createElement("strong");
  strong.textContent = "Composite appearance";
  title.appendChild(strong);
  const addButton = document.createElement("button");
  addButton.type = "button";
  addButton.className = "btn btn-outline-dark btn-sm";
  addButton.textContent = "Add part";
  addButton.addEventListener("click", () => {
    item.parts.push({ bodyPart: "BODY", spriteBase: "" });
    item.spriteBase = "";
    syncJsonEditor(item);
    markDirty();
    renderEditor();
  });
  header.appendChild(title);
  header.appendChild(addButton);
  el.relationEditorBlock.appendChild(header);

  if (!item.parts.length) {
    const empty = document.createElement("p");
    empty.className = "muted mb-0";
    empty.textContent = "This NPC uses a single spriteBase. Add a part to switch to a composite NPC.";
    el.relationEditorBlock.appendChild(empty);
  } else {
    item.parts.forEach((part, index) => {
    const row = document.createElement("div");
    row.className = "relation-row npc-part-row";

      const bodyWrap = document.createElement("label");
      bodyWrap.appendChild(createFieldLabel("bodyPart", item));
      const bodySelect = document.createElement("select");
      bodySelect.className = "form-control form-control-sm";
      appendOptions(bodySelect, itemBodyPartOptions(part.bodyPart), part.bodyPart || "BODY");
      bodySelect.addEventListener("input", () => {
        part.bodyPart = bodySelect.value;
        syncJsonEditor(item);
        markDirty();
      });
      bodyWrap.appendChild(bodySelect);

      const spriteCell = document.createElement("div");
      spriteCell.appendChild(createStaticFieldLabel("Sprite base", "Animation base used for this body part."));
      spriteCell.appendChild(createPartSpriteDropdown(item, part));

      const deleteButton = document.createElement("button");
      deleteButton.type = "button";
      deleteButton.className = "btn btn-danger btn-sm";
      deleteButton.textContent = "Delete";
      deleteButton.addEventListener("click", () => {
        item.parts.splice(index, 1);
        syncJsonEditor(item);
        markDirty();
        renderEditor();
      });

      row.appendChild(bodyWrap);
      row.appendChild(spriteCell);
      row.appendChild(deleteButton);
      el.relationEditorBlock.appendChild(row);
    });
  }

}

/** Switches the editor between the record form and the dialogue graph. */
function showEditorTab(tab) {
  state.editorTab = tab;
  el.editorTabDetails.classList.toggle("hidden", tab !== "details");
  el.editorTabDialogue.classList.toggle("hidden", tab !== "dialogue");
  el.editorTabs.querySelectorAll(".editor-tab").forEach((button) => {
    button.classList.toggle("is-active", button.dataset.tab === tab);
  });
}

/**
 * Visual node graph for an NPC's dialogue (see {@code NpcDef.DialogNode} /
 * {@code ActionType} on the Java side). Nodes are free-form draggable boxes
 * connected by an SVG line per {@code GOTO_NODE} action or {@code fallbackNode}
 * pointer; clicking a node opens a full-field inspector below the canvas.
 *
 * <p>Layout is session-only (never sent to the server): positions live in
 * {@code state.dialogueLayout}, rebuilt with a simple auto-layout whenever a
 * different NPC is selected. All edits mutate {@code item.dialogNodes} in
 * place, picked up by the normal save flow like any other field.
 */
function renderDialogGraph(item) {
  return renderSimpleDialogueEditor(item);
  /* Legacy graph renderer kept below temporarily for source-history clarity; it
     is unreachable and will be removed once the reset format has shipped. */
  const nodes = item && Array.isArray(item.dialogNodes) ? item.dialogNodes : null;
  if (!nodes) return;
  el.editorTabs.classList.remove("hidden");
  ensureDialogueEditorOptions();

  if (state.dialogueLayoutFor !== item) {
    state.dialogueLayout = autoLayoutDialogueNodes(nodes);
    state.dialogueLayoutFor = item;
    state.dialogueSelectedNodeId = nodes.length ? nodes[0].id : null;
  }

  const section = document.createElement("div");
  section.className = "dialogue-editor";

  const header = document.createElement("div");
  header.className = "dialog-graph-header";
  const strong = document.createElement("strong");
  strong.textContent = `${nodes.length} node${nodes.length === 1 ? "" : "s"}`;
  header.appendChild(strong);
  const addButton = document.createElement("button");
  addButton.type = "button";
  addButton.className = "btn btn-outline-dark btn-sm";
  addButton.textContent = "Add node";
  addButton.addEventListener("click", () => addDialogueNode(item));
  header.appendChild(addButton);
  section.appendChild(header);

  const canvas = document.createElement("div");
  canvas.className = "dialogue-canvas";
  const svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
  svg.setAttribute("class", "dialogue-edges");
  canvas.appendChild(svg);
  nodes.forEach((node) => {
    canvas.appendChild(dialogueNodeBox(item, node, svg, canvas));
  });
  section.appendChild(canvas);
  el.editorTabDialogue.appendChild(section);

  requestAnimationFrame(() => drawDialogueEdges(item, svg, canvas));

  const inspector = document.createElement("div");
  inspector.className = "dialogue-inspector";
  section.appendChild(inspector);
  renderDialogueInspector(item, inspector, svg, canvas);
}

function renderSimpleDialogueEditor(item) {
  if (!item || !Array.isArray(item.topics)) return;
  el.editorTabs.classList.remove("hidden");
  ensureDialogueEditorOptions();

  const editor = document.createElement("section");
  editor.className = "dialogue-editor";

  const welcome = document.createElement("label");
  welcome.appendChild(createStaticFieldLabel("Welcome dialogue", "Text shown at the start of each conversation."));
  const welcomeInput = document.createElement("textarea");
  welcomeInput.className = "form-control dialogue-node-response-input";
  welcomeInput.rows = 4;
  welcomeInput.required = true;
  welcomeInput.value = item.welcomeText || "";
  welcomeInput.addEventListener("input", () => {
    item.welcomeText = welcomeInput.value;
    syncJsonEditor(item);
    markDirty();
  });
  welcome.appendChild(welcomeInput);
  editor.appendChild(welcome);

  const header = document.createElement("div");
  header.className = "dialog-graph-header";
  const title = document.createElement("strong");
  title.textContent = `${item.topics.length} topic${item.topics.length === 1 ? "" : "s"}`;
  header.appendChild(title);
  const addTopic = document.createElement("button");
  addTopic.type = "button";
  addTopic.className = "btn btn-outline-dark btn-sm";
  addTopic.textContent = "Add topic";
  addTopic.addEventListener("click", () => {
    item.topics.push({ keywords: [], response: "", actions: [] });
    markDirty();
    renderEditor();
  });
  header.appendChild(addTopic);
  editor.appendChild(header);

  item.topics.forEach((topic, topicIndex) => {
    const card = document.createElement("div");
    card.className = "dialogue-inspector";
    const topicHeader = document.createElement("div");
    topicHeader.className = "relation-editor-header";
    const topicTitle = document.createElement("strong");
    topicTitle.textContent = `Topic ${topicIndex + 1}`;
    topicHeader.appendChild(topicTitle);
    topicHeader.appendChild(dialogueMoveButton("↑", topicIndex > 0, () => {
      [item.topics[topicIndex - 1], item.topics[topicIndex]] = [item.topics[topicIndex], item.topics[topicIndex - 1]];
      markDirty(); renderEditor();
    }));
    topicHeader.appendChild(dialogueMoveButton("↓", topicIndex < item.topics.length - 1, () => {
      [item.topics[topicIndex + 1], item.topics[topicIndex]] = [item.topics[topicIndex], item.topics[topicIndex + 1]];
      markDirty(); renderEditor();
    }));
    const removeTopic = dialogueMoveButton("Delete", true, () => {
      item.topics.splice(topicIndex, 1); markDirty(); renderEditor();
    });
    removeTopic.className = "btn btn-danger btn-sm";
    topicHeader.appendChild(removeTopic);
    card.appendChild(topicHeader);

    card.appendChild(dialogueTextField("Keywords (comma-separated)",
      (topic.keywords || []).join(", "), (value) => {
        topic.keywords = value.split(",").map((part) => part.trim()).filter(Boolean);
        syncJsonEditor(item); markDirty();
      }));
    const response = document.createElement("label");
    response.appendChild(createStaticFieldLabel("Optional response", "Shown before the actions."));
    const responseInput = document.createElement("textarea");
    responseInput.className = "form-control dialogue-node-response-input";
    responseInput.rows = 2;
    responseInput.value = topic.response || "";
    responseInput.addEventListener("input", () => {
      topic.response = responseInput.value; syncJsonEditor(item); markDirty();
    });
    response.appendChild(responseInput);
    card.appendChild(response);

    const actionHeader = document.createElement("div");
    actionHeader.className = "relation-editor-header";
    actionHeader.appendChild(createStaticFieldLabel("Actions", "Executed from top to bottom."));
    const addAction = document.createElement("button");
    addAction.type = "button";
    addAction.className = "btn btn-outline-dark btn-sm";
    addAction.textContent = "Add action";
    addAction.addEventListener("click", () => {
      if (!Array.isArray(topic.actions)) topic.actions = [];
      topic.actions.push({ type: "OPEN_SPELL_LEARNING", targets: [] });
      markDirty(); renderEditor();
    });
    actionHeader.appendChild(addAction);
    card.appendChild(actionHeader);
    (topic.actions || []).forEach((action, actionIndex) => {
      card.appendChild(renderSimpleDialogueAction(item, topic, action, actionIndex));
    });
    editor.appendChild(card);
  });
  el.editorTabDialogue.appendChild(editor);
}

function dialogueMoveButton(text, enabled, handler) {
  const button = document.createElement("button");
  button.type = "button";
  button.className = "btn btn-outline-dark btn-sm";
  button.textContent = text;
  button.disabled = !enabled;
  button.addEventListener("click", handler);
  return button;
}

function renderSimpleDialogueAction(item, topic, action, index) {
  const row = document.createElement("div");
  row.className = "dialogue-action-row";
  const type = document.createElement("select");
  type.className = "form-control form-control-sm";
  (state.actionTypeOptions.length ? state.actionTypeOptions : [action.type]).forEach((value) => {
    const option = document.createElement("option");
    option.value = value;
    option.textContent = ({
      OPEN_SPELL_LEARNING: "Teach spells",
      OPEN_SKILL_LEARNING: "Teach skills",
      OPEN_SHOP: "Open shop",
      GIVE_ITEM: "Give item",
      GIVE_QUEST: "Give quest",
      END_CONVERSATION: "End conversation",
      HEAL: "Heal player",
    })[value] || value;
    option.selected = value === action.type;
    type.appendChild(option);
  });
  type.addEventListener("change", () => {
    action.type = type.value; action.targets = []; markDirty(); renderEditor();
  });
  row.appendChild(type);

  const choices = action.type === "OPEN_SPELL_LEARNING" ? state.spellNameOptions
    : action.type === "OPEN_SKILL_LEARNING" ? state.trainableStatIdOptions
      : ["OPEN_SHOP", "GIVE_ITEM"].includes(action.type) ? state.itemKeyOptions
        : action.type === "GIVE_QUEST" ? state.questIdOptions : null;
  if (choices && ["GIVE_ITEM", "GIVE_QUEST"].includes(action.type)) {
    const select = document.createElement("select");
    select.className = "form-control form-control-sm";
    ["", ...choices].forEach((value) => {
      const option = document.createElement("option");
      option.value = value;
      option.textContent = value || (action.type === "GIVE_QUEST" ? "(quest)" : "(item)");
      option.selected = value === (action.targets || [])[0];
      select.appendChild(option);
    });
    select.addEventListener("change", () => {
      action.targets = select.value ? [select.value] : []; syncJsonEditor(item); markDirty();
    });
    row.appendChild(select);
  } else if (choices) {
    row.appendChild(dialogueTargetPicker(item, action, choices));
  }

  row.appendChild(dialogueMoveButton("↑", index > 0, () => {
    [topic.actions[index - 1], topic.actions[index]] = [topic.actions[index], topic.actions[index - 1]];
    markDirty(); renderEditor();
  }));
  row.appendChild(dialogueMoveButton("↓", index < topic.actions.length - 1, () => {
    [topic.actions[index + 1], topic.actions[index]] = [topic.actions[index], topic.actions[index + 1]];
    markDirty(); renderEditor();
  }));
  const remove = dialogueMoveButton("Delete", true, () => {
    topic.actions.splice(index, 1); markDirty(); renderEditor();
  });
  remove.className = "btn btn-danger btn-sm";
  row.appendChild(remove);
  return row;
}

function dialogueTargetPicker(item, action, choices) {
  const picker = document.createElement("div");
  picker.className = "dialogue-target-picker";
  const selected = new Set(action.targets || []);
  const title = document.createElement("div");
  title.className = "dialogue-target-picker-title";
  const count = document.createElement("strong");
  const updateCount = () => {
    count.textContent = `${selected.size} selected`;
  };
  updateCount();
  title.appendChild(count);
  const clear = document.createElement("button");
  clear.type = "button"; clear.className = "btn btn-link btn-sm"; clear.textContent = "Clear all";
  clear.addEventListener("click", () => {
    selected.clear(); action.targets = []; updateCount(); renderChoices(""); syncJsonEditor(item); markDirty();
  });
  title.appendChild(clear); picker.appendChild(title);
  const search = document.createElement("input");
  search.type = "search"; search.className = "form-control form-control-sm";
  search.placeholder = "Search by name or identifier…";
  picker.appendChild(search);
  const list = document.createElement("div");
  list.className = "dialogue-target-picker-list"; picker.appendChild(list);
  function renderChoices(filter) {
    list.replaceChildren();
    const needle = filter.trim().toLocaleLowerCase();
    choices.filter(value => !needle || value.toLocaleLowerCase().includes(needle)).forEach(value => {
      const label = document.createElement("label"); label.className = "dialogue-target-option";
      label.title = value;
      const checkbox = document.createElement("input"); checkbox.type = "checkbox"; checkbox.checked = selected.has(value);
      checkbox.addEventListener("change", () => {
        if (checkbox.checked) selected.add(value); else selected.delete(value);
        action.targets = Array.from(selected); updateCount(); syncJsonEditor(item); markDirty();
      });
      label.appendChild(checkbox); label.appendChild(document.createTextNode(value)); list.appendChild(label);
    });
    if (!list.children.length) { const empty = document.createElement("span"); empty.className = "muted"; empty.textContent = "No results"; list.appendChild(empty); }
  }
  search.addEventListener("input", () => renderChoices(search.value));
  renderChoices("");
  return picker;
}

/** Simple layered layout keyed by GOTO_NODE/fallbackNode edges; disconnected nodes fall back to a grid. */
function autoLayoutDialogueNodes(nodes) {
  const layout = {};
  const byId = new Map(nodes.map((n) => [n.id, n]));
  const depth = new Map();
  const targetsOf = (node) => {
    const targets = (node.actions || [])
      .filter((a) => a.type === "GOTO_NODE" && a.stringParam1)
      .map((a) => a.stringParam1);
    if (node.fallbackNode) targets.push(node.fallbackNode);
    return targets.filter((id) => byId.has(id));
  };
  const visited = new Set();
  const assignDepth = (id, d) => {
    if (visited.has(id) || d > nodes.length) return;
    visited.add(id);
    depth.set(id, Math.max(depth.get(id) || 0, d));
    targetsOf(byId.get(id)).forEach((targetId) => assignDepth(targetId, d + 1));
  };
  const greeting = nodes.find((n) => n.greeting);
  (greeting ? [greeting, ...nodes.filter((n) => n !== greeting)] : nodes).forEach((n) => {
    if (!visited.has(n.id)) assignDepth(n.id, 0);
  });

  const columnCounts = {};
  const colWidth = 280;
  const rowHeight = 150;
  nodes.forEach((node) => {
    const col = depth.get(node.id) || 0;
    const row = columnCounts[col] || 0;
    columnCounts[col] = row + 1;
    layout[node.id] = { x: 24 + col * colWidth, y: 24 + row * rowHeight };
  });
  return layout;
}

function dialogueNodeBox(item, node, svg, canvas) {
  const box = document.createElement("div");
  box.className = "dialogue-node";
  box.classList.toggle("is-greeting", Boolean(node.greeting));
  box.classList.toggle("is-selected", state.dialogueSelectedNodeId === node.id);
  box.dataset.nodeId = node.id;
  const pos = state.dialogueLayout[node.id] || { x: 24, y: 24 };
  box.style.left = `${pos.x}px`;
  box.style.top = `${pos.y}px`;

  const head = document.createElement("div");
  head.className = "dialogue-node-head";
  const idLabel = document.createElement("span");
  idLabel.className = "dialogue-node-id";
  idLabel.textContent = node.greeting ? `${node.id} (greeting)` : node.id;
  head.appendChild(idLabel);
  box.appendChild(head);

  if ((node.keywords || []).length) {
    const chips = document.createElement("div");
    chips.className = "dialogue-node-keywords";
    node.keywords.forEach((keyword) => {
      const chip = document.createElement("span");
      chip.className = "dialog-trigger";
      chip.textContent = keyword;
      chips.appendChild(chip);
    });
    box.appendChild(chips);
  }

  const response = document.createElement("p");
  response.className = "dialogue-node-response-preview";
  response.textContent = node.response || "(no reply)";
  box.appendChild(response);

  const actionCount = (node.actions || []).length;
  if (actionCount) {
    const summary = document.createElement("span");
    summary.className = "dialog-tag dialog-tag-set";
    summary.textContent = `${actionCount} action${actionCount === 1 ? "" : "s"}`;
    box.appendChild(summary);
  }

  box.addEventListener("mousedown", (event) => {
    if (event.button !== 0) return;
    event.preventDefault();
    startDialogueNodeDrag(event, item, node, box, svg, canvas);
  });
  box.addEventListener("click", () => {
    if (state.dialogueSelectedNodeId === node.id) return;
    state.dialogueSelectedNodeId = node.id;
    renderEditor();
  });

  return box;
}

function startDialogueNodeDrag(event, item, node, box, svg, canvas) {
  const startX = event.clientX;
  const startY = event.clientY;
  const origin = { ...(state.dialogueLayout[node.id] || { x: 24, y: 24 }) };
  let moved = false;

  function onMove(moveEvent) {
    const dx = moveEvent.clientX - startX;
    const dy = moveEvent.clientY - startY;
    if (Math.abs(dx) > 2 || Math.abs(dy) > 2) moved = true;
    const next = { x: Math.max(0, origin.x + dx), y: Math.max(0, origin.y + dy) };
    state.dialogueLayout[node.id] = next;
    box.style.left = `${next.x}px`;
    box.style.top = `${next.y}px`;
    drawDialogueEdges(item, svg, canvas);
  }
  function onUp() {
    document.removeEventListener("mousemove", onMove);
    document.removeEventListener("mouseup", onUp);
    if (!moved) {
      state.dialogueSelectedNodeId = node.id;
      renderEditor();
    }
  }
  document.addEventListener("mousemove", onMove);
  document.addEventListener("mouseup", onUp);
}

/** Redraws one SVG line per GOTO_NODE action / fallbackNode pointer, anchored to each box's edges. */
function drawDialogueEdges(item, svg, canvas) {
  const nodes = item.dialogNodes || [];
  const boxes = new Map();
  canvas.querySelectorAll(".dialogue-node").forEach((box) => boxes.set(box.dataset.nodeId, box));
  svg.replaceChildren();
  svg.setAttribute("width", canvas.scrollWidth);
  svg.setAttribute("height", canvas.scrollHeight);

  const edges = [];
  nodes.forEach((node) => {
    (node.actions || []).forEach((action) => {
      if (action.type === "GOTO_NODE" && action.stringParam1) {
        edges.push({ from: node.id, to: action.stringParam1, dashed: false });
      }
    });
    if (node.fallbackNode) {
      edges.push({ from: node.id, to: node.fallbackNode, dashed: true });
    }
  });

  edges.forEach(({ from, to, dashed }) => {
    const fromBox = boxes.get(from);
    const toBox = boxes.get(to);
    if (!fromBox || !toBox) return;
    const x1 = fromBox.offsetLeft + fromBox.offsetWidth;
    const y1 = fromBox.offsetTop + fromBox.offsetHeight / 2;
    const x2 = toBox.offsetLeft;
    const y2 = toBox.offsetTop + toBox.offsetHeight / 2;
    const line = document.createElementNS("http://www.w3.org/2000/svg", "path");
    const midX = (x1 + x2) / 2;
    line.setAttribute("d", `M ${x1} ${y1} C ${midX} ${y1}, ${midX} ${y2}, ${x2} ${y2}`);
    line.setAttribute("class", dashed ? "dialogue-edge dialogue-edge-fallback" : "dialogue-edge");
    line.setAttribute("marker-end", "url(#dialogue-arrow)");
    svg.appendChild(line);
  });

  const defs = document.createElementNS("http://www.w3.org/2000/svg", "defs");
  defs.innerHTML = `<marker id="dialogue-arrow" markerWidth="8" markerHeight="8" refX="7" refY="4" orient="auto">
    <path d="M0,0 L8,4 L0,8 Z" class="dialogue-edge-arrowhead"></path>
  </marker>`;
  svg.prepend(defs);
}

async function ensureDialogueEditorOptions() {
  if (state.dialogueEditorOptionsLoaded || state.dialogueEditorOptionsLoading) return;
  state.dialogueEditorOptionsLoading = true;
  let data;
  try {
    data = await apiJson("/api/npc-options");
  } finally {
    state.dialogueEditorOptionsLoading = false;
    state.dialogueEditorOptionsLoaded = true;
  }
  if (!state.actionTypeOptions.length) {
    state.actionTypeOptions = data.actionTypes || [];
  }
  if (!state.questFlagNameOptions.length) {
    state.questFlagNameOptions = data.questFlagNames || [];
  }
  if (!state.questIdOptions.length) {
    state.questIdOptions = (data.questIds || []).filter(Boolean).sort((a, b) => a.localeCompare(b));
  }
  if (!state.itemKeyOptions.length) {
    state.itemKeyOptions = (data.itemKeys || []).filter(Boolean).sort((a, b) => a.localeCompare(b));
  }
  if (!state.spellNameOptions.length) {
    state.spellNameOptions = (data.spells || []).filter(Boolean).sort((a, b) => a.localeCompare(b));
  }
  if (state.editorTab === "dialogue") renderEditor();
}

function addDialogueNode(item) {
  const id = `node-${Math.random().toString(36).slice(2, 8)}`;
  item.dialogNodes.push({
    id, keywords: [], response: "", requiredFlag: "", requiredFlagValue: 0,
    requiredItem: "", greeting: false, fallbackNode: "", actions: [],
  });
  state.dialogueLayoutFor = null;
  state.dialogueSelectedNodeId = id;
  markDirty();
  renderEditor();
}

/** Removes a node and strips any dangling reference to it elsewhere in the graph. */
function deleteDialogueNode(item, nodeId) {
  item.dialogNodes = item.dialogNodes.filter((n) => n.id !== nodeId);
  item.dialogNodes.forEach((n) => {
    if (n.fallbackNode === nodeId) n.fallbackNode = "";
    n.actions = (n.actions || []).filter((a) => !(a.type === "GOTO_NODE" && a.stringParam1 === nodeId));
  });
  state.dialogueLayoutFor = null;
  state.dialogueSelectedNodeId = item.dialogNodes.length ? item.dialogNodes[0].id : null;
  markDirty();
  renderEditor();
}

function renderDialogueInspector(item, container, svg, canvas) {
  const node = (item.dialogNodes || []).find((n) => n.id === state.dialogueSelectedNodeId);
  if (!node) return;

  const rerenderGraph = () => {
    if (!el.jsonBlock.classList.contains("hidden")) {
      el.jsonEditor.value = JSON.stringify(item, null, 2);
    }
    markDirty();
    const graphContainer = el.editorTabDialogue.querySelector(".dialogue-canvas");
    if (graphContainer) drawDialogueEdges(item, svg, canvas);
    const box = el.editorTabDialogue.querySelector(`.dialogue-node[data-node-id="${CSS.escape(node.id)}"]`);
    if (box) {
      const response = box.querySelector(".dialogue-node-response-preview");
      if (response) response.textContent = node.response || "(no reply)";
    }
  };

  const header = document.createElement("div");
  header.className = "relation-editor-header";
  const title = document.createElement("strong");
  title.textContent = `Node: ${node.id}`;
  header.appendChild(title);
  const deleteButton = document.createElement("button");
  deleteButton.type = "button";
  deleteButton.className = "btn btn-danger btn-sm";
  deleteButton.textContent = "Delete node";
  deleteButton.addEventListener("click", () => deleteDialogueNode(item, node.id));
  header.appendChild(deleteButton);
  container.appendChild(header);

  container.appendChild(dialogueTextField("Keywords (comma-separated)", (node.keywords || []).join(", "), (value) => {
    node.keywords = value.split(",").map((s) => s.trim()).filter(Boolean);
    rerenderGraph();
  }));

  const responseLabel = document.createElement("label");
  responseLabel.appendChild(createStaticFieldLabel("Response", "The line spoken when this node is entered."));
  const responseInput = document.createElement("textarea");
  responseInput.className = "form-control form-control-sm dialogue-node-response-input";
  responseInput.rows = 3;
  responseInput.value = node.response || "";
  responseInput.addEventListener("input", () => {
    node.response = responseInput.value;
    rerenderGraph();
  });
  responseLabel.appendChild(responseInput);
  container.appendChild(responseLabel);

  container.appendChild(dialogueCheckboxField("Greeting", node.greeting, (value) => {
    node.greeting = value;
    markDirty();
    renderList();
  }));

  container.appendChild(dialogueTextField("Required flag", node.requiredFlag || "", (value) => {
    node.requiredFlag = value;
    markDirty();
  }, state.questFlagNameOptions));
  container.appendChild(dialogueNumberField("Required flag value", node.requiredFlagValue || 0, (value) => {
    node.requiredFlagValue = value;
    markDirty();
  }));
  container.appendChild(dialogueTextField("Required item", node.requiredItem || "", (value) => {
    node.requiredItem = value;
    markDirty();
  }, state.itemKeyOptions));
  container.appendChild(dialogueSelectField("Fallback node (if nothing matched)", node.fallbackNode || "",
    ["", ...item.dialogNodes.map((n) => n.id).filter((id) => id !== node.id)], (value) => {
      node.fallbackNode = value;
      rerenderGraph();
    }));

  container.appendChild(renderDialogueActionsEditor(item, node, rerenderGraph));
}

function dialogueTextField(labelText, value, onChange, datalistOptions) {
  const label = document.createElement("label");
  label.appendChild(createStaticFieldLabel(labelText, ""));
  const input = document.createElement("input");
  input.type = "text";
  input.className = "form-control form-control-sm";
  input.value = value;
  if (datalistOptions && datalistOptions.length) {
    const listId = `dl-${Math.random().toString(36).slice(2, 8)}`;
    input.setAttribute("list", listId);
    const datalist = document.createElement("datalist");
    datalist.id = listId;
    datalistOptions.forEach((option) => {
      const opt = document.createElement("option");
      opt.value = option;
      datalist.appendChild(opt);
    });
    label.appendChild(datalist);
  }
  input.addEventListener("input", () => onChange(input.value));
  label.appendChild(input);
  return label;
}

function dialogueNumberField(labelText, value, onChange) {
  const label = document.createElement("label");
  label.appendChild(createStaticFieldLabel(labelText, ""));
  const input = document.createElement("input");
  input.type = "number";
  input.step = "any";
  input.className = "form-control form-control-sm";
  input.value = value;
  input.addEventListener("input", () => onChange(Number(input.value || 0)));
  label.appendChild(input);
  return label;
}

function dialogueCheckboxField(labelText, checked, onChange) {
  const label = document.createElement("label");
  label.appendChild(createStaticFieldLabel(labelText, ""));
  const input = document.createElement("input");
  input.type = "checkbox";
  input.checked = Boolean(checked);
  input.addEventListener("change", () => onChange(input.checked));
  label.appendChild(input);
  return label;
}

function dialogueSelectField(labelText, value, options, onChange) {
  const label = document.createElement("label");
  label.appendChild(createStaticFieldLabel(labelText, ""));
  const select = document.createElement("select");
  select.className = "form-control form-control-sm";
  options.forEach((option) => {
    const opt = document.createElement("option");
    opt.value = option;
    opt.textContent = option || "(none)";
    if (option === value) opt.selected = true;
    select.appendChild(opt);
  });
  select.addEventListener("change", () => onChange(select.value));
  label.appendChild(select);
  return label;
}

/** Params relevant to each action type, so the row only shows inputs that mean something. */
const ACTION_PARAM_FIELDS = {
  HEAL: ["intParam1", "intParam2"],
  GIVE_ITEM: ["stringParam1"],
  GIVE_QUEST: ["stringParam1"],
  TAKE_ITEM: ["stringParam1"],
  SET_FLAG: ["stringParam1", "intParam1"],
  LEARN: [],
  TEACH: ["stringParam1"],
  SHOP: [],
  TRAIN: [],
  CAST: ["stringParam1", "intParam1"],
  KILL: [],
  END_CONVERSATION: [],
  GOTO_NODE: ["stringParam1"],
};

function renderDialogueActionsEditor(item, node, rerenderGraph) {
  const section = document.createElement("div");
  section.className = "relation-subsection";
  const header = document.createElement("div");
  header.className = "relation-editor-header";
  const title = document.createElement("div");
  title.appendChild(createStaticFieldLabel("Actions", "Effects fired, in order, when this node is entered."));
  header.appendChild(title);
  const addButton = document.createElement("button");
  addButton.type = "button";
  addButton.className = "btn btn-outline-dark btn-sm";
  addButton.textContent = "Add action";
  addButton.addEventListener("click", () => {
    if (!Array.isArray(node.actions)) node.actions = [];
    node.actions.push({ type: state.actionTypeOptions[0] || "SET_FLAG", stringParam1: "", stringParam2: "", intParam1: 0, intParam2: 0 });
    markDirty();
    renderEditor();
  });
  header.appendChild(addButton);
  section.appendChild(header);

  (node.actions || []).forEach((action, index) => {
    section.appendChild(renderDialogueActionRow(item, node, action, index, rerenderGraph));
  });

  return section;
}

function renderDialogueActionRow(item, node, action, index, rerenderGraph) {
  const row = document.createElement("div");
  row.className = "dialogue-action-row";

  const typeSelect = document.createElement("select");
  typeSelect.className = "form-control form-control-sm";
  (state.actionTypeOptions.length ? state.actionTypeOptions : [action.type]).forEach((type) => {
    const opt = document.createElement("option");
    opt.value = type;
    opt.textContent = type;
    if (type === action.type) opt.selected = true;
    typeSelect.appendChild(opt);
  });
  typeSelect.addEventListener("change", () => {
    action.type = typeSelect.value;
    markDirty();
    renderEditor();
  });
  row.appendChild(typeSelect);

  const params = ACTION_PARAM_FIELDS[action.type] || [];
  if (params.includes("stringParam1")) {
    row.appendChild(dialogueActionParamInput(action, "stringParam1", stringParamOptionsFor(action.type, node, item), rerenderGraph));
  }
  if (params.includes("stringParam2")) {
    row.appendChild(dialogueActionParamInput(action, "stringParam2", null, rerenderGraph));
  }
  if (params.includes("intParam1")) {
    row.appendChild(dialogueActionNumberInput(action, "intParam1", rerenderGraph));
  }
  if (params.includes("intParam2")) {
    row.appendChild(dialogueActionNumberInput(action, "intParam2", rerenderGraph));
  }

  const deleteButton = document.createElement("button");
  deleteButton.type = "button";
  deleteButton.className = "btn btn-danger btn-sm";
  deleteButton.textContent = "Delete";
  deleteButton.addEventListener("click", () => {
    node.actions.splice(index, 1);
    rerenderGraph();
    renderEditor();
  });
  row.appendChild(deleteButton);

  const wrap = document.createElement("div");
  wrap.appendChild(row);
  if (action.type === "LEARN") {
    wrap.appendChild(renderLearnCurriculumHint(item));
  }
  return wrap;
}

/**
 * LEARN has no params of its own — DataNpc opens LearnScreen with the NPC's
 * whole taughtSpells list (see renderTaughtSpellsEditor), so this shows that
 * curriculum inline right where the action was added, instead of leaving the
 * user hunting for the separate "Taught spells" section below the graph.
 */
function renderLearnCurriculumHint(item) {
  if (!Array.isArray(item.taughtSpells)) item.taughtSpells = [];
  const box = document.createElement("div");
  box.className = "dialogue-learn-hint";

  const label = document.createElement("div");
  label.className = "dialogue-learn-hint-label";
  label.textContent = "Spells offered by LEARN:";
  box.appendChild(label);

  if (!item.taughtSpells.length) {
    const empty = document.createElement("span");
    empty.className = "muted";
    empty.textContent = "None yet — add one below.";
    box.appendChild(empty);
  } else {
    const list = document.createElement("span");
    list.textContent = item.taughtSpells.map((s) => s.spellName || "(unset)").join(", ");
    box.appendChild(list);
  }

  const addButton = document.createElement("button");
  addButton.type = "button";
  addButton.className = "btn btn-outline-dark btn-sm";
  addButton.textContent = "Add spell";
  addButton.addEventListener("click", () => {
    item.taughtSpells.push({ spellName: state.spellNameOptions[0] || "" });
    syncJsonEditor(item);
    markDirty();
    renderEditor();
  });
  box.appendChild(addButton);

  return box;
}

function stringParamOptionsFor(type, node, item) {
  if (type === "GOTO_NODE") return item.dialogNodes.map((n) => n.id).filter((id) => id !== node.id);
  if (type === "GIVE_ITEM" || type === "TAKE_ITEM") return state.itemKeyOptions;
  if (type === "GIVE_QUEST") return state.questIdOptions;
  if (type === "TEACH" || type === "CAST") return state.spellNameOptions;
  if (type === "SET_FLAG") return state.questFlagNameOptions;
  return [];
}

function dialogueActionParamInput(action, key, options, rerenderGraph) {
  if (options && options.length) {
    const select = document.createElement("select");
    select.className = "form-control form-control-sm";
    const current = action[key] || "";
    if (!options.includes(current)) {
      const blank = document.createElement("option");
      blank.value = "";
      blank.textContent = "(choose)";
      select.appendChild(blank);
    }
    options.forEach((option) => {
      const opt = document.createElement("option");
      opt.value = option;
      opt.textContent = option;
      if (option === current) opt.selected = true;
      select.appendChild(opt);
    });
    select.addEventListener("change", () => {
      action[key] = select.value;
      rerenderGraph();
    });
    return select;
  }
  const input = document.createElement("input");
  input.type = "text";
  input.className = "form-control form-control-sm";
  input.value = action[key] || "";
  input.addEventListener("input", () => {
    action[key] = input.value;
    rerenderGraph();
  });
  return input;
}

function dialogueActionNumberInput(action, key, rerenderGraph) {
  const input = document.createElement("input");
  input.type = "number";
  input.step = "any";
  input.className = "form-control form-control-sm";
  input.value = action[key] || 0;
  input.addEventListener("input", () => {
    action[key] = Number(input.value || 0);
    rerenderGraph();
  });
  return input;
}

function renderTaughtSpellsEditor(item) {
  if (!Array.isArray(item.taughtSpells)) item.taughtSpells = [];

  const section = document.createElement("div");
  section.className = "relation-subsection";
  const header = document.createElement("div");
  header.className = "relation-editor-header";
  const title = document.createElement("div");
  title.appendChild(createStaticFieldLabel("Taught spells", "Spells this NPC can teach. The learning price is configured on each spell."));
  const strong = document.createElement("strong");
  strong.textContent = "Spell lessons";
  title.appendChild(strong);
  const addButton = document.createElement("button");
  addButton.type = "button";
  addButton.className = "btn btn-outline-dark btn-sm";
  addButton.textContent = "Add spell";
  addButton.addEventListener("click", () => {
    item.taughtSpells.push({ spellName: state.spellNameOptions[0] || "" });
    syncJsonEditor(item);
    markDirty();
    renderRelationEditor(item);
  });
  header.appendChild(title);
  header.appendChild(addButton);
  section.appendChild(header);

  if (!item.taughtSpells.length) {
    const empty = document.createElement("p");
    empty.className = "muted mb-0";
    empty.textContent = state.spellNameOptions.length ? "No spell lessons configured." : "Loading spell list...";
    section.appendChild(empty);
  }

  item.taughtSpells.forEach((spell, index) => {
    const row = document.createElement("div");
    row.className = "relation-row taught-spell-row";

    const spellWrap = document.createElement("label");
    spellWrap.appendChild(createStaticFieldLabel("Spell", "Spell made available by this NPC."));
    const spellSelect = document.createElement("select");
    spellSelect.className = "form-control form-control-sm";
    appendOptions(spellSelect, taughtSpellOptions(spell.spellName), spell.spellName || "");
    spellSelect.addEventListener("input", () => {
      spell.spellName = spellSelect.value;
      syncJsonEditor(item);
      markDirty();
      renderList();
    });
    spellWrap.appendChild(spellSelect);

    const deleteButton = document.createElement("button");
    deleteButton.type = "button";
    deleteButton.className = "btn btn-danger btn-sm";
    deleteButton.textContent = "Delete";
    deleteButton.addEventListener("click", () => {
      item.taughtSpells.splice(index, 1);
      syncJsonEditor(item);
      markDirty();
      renderRelationEditor(item);
    });

    row.appendChild(spellWrap);
    row.appendChild(deleteButton);
    section.appendChild(row);
  });

  el.relationEditorBlock.appendChild(section);
}

function taughtSpellOptions(currentValue) {
  const options = ["", ...state.spellNameOptions];
  const current = String(currentValue || "").trim();
  if (current && !options.includes(current)) options.splice(1, 0, current);
  return options;
}

async function ensureNpcShopTrainOptions() {
  if (state.itemKeyOptions.length && state.trainableStatIdOptions.length) return;
  const data = await apiJson("/api/npc-options");
  if (!state.itemKeyOptions.length) {
    state.itemKeyOptions = (data.itemKeys || []).filter(Boolean).sort((a, b) => a.localeCompare(b));
  }
  if (!state.trainableStatIdOptions.length) {
    state.trainableStatIdOptions = (data.trainableStatIds || []).filter(Boolean);
  }
}

function renderShopItemsEditor(item) {
  if (!Array.isArray(item.shopItems)) item.shopItems = [];

  const section = document.createElement("div");
  section.className = "relation-subsection";
  const header = document.createElement("div");
  header.className = "relation-editor-header";
  const title = document.createElement("div");
  title.appendChild(createStaticFieldLabel("Shop items", "Items this NPC sells. Price 0 means use the item's default price."));
  const strong = document.createElement("strong");
  strong.textContent = "Items for sale";
  title.appendChild(strong);
  const addButton = document.createElement("button");
  addButton.type = "button";
  addButton.className = "btn btn-outline-dark btn-sm";
  addButton.textContent = "Add item";
  addButton.addEventListener("click", () => {
    item.shopItems.push({ itemKey: state.itemKeyOptions[0] || "", price: 0 });
    syncJsonEditor(item);
    markDirty();
    renderRelationEditor(item);
  });
  header.appendChild(title);
  header.appendChild(addButton);
  section.appendChild(header);

  if (!item.shopItems.length) {
    const empty = document.createElement("p");
    empty.className = "muted mb-0";
    empty.textContent = state.itemKeyOptions.length ? "No items configured." : "Loading item list...";
    section.appendChild(empty);
  }

  item.shopItems.forEach((si, index) => {
    const row = document.createElement("div");
    row.className = "relation-row taught-spell-row";

    const keyWrap = document.createElement("label");
    keyWrap.appendChild(createStaticFieldLabel("Item", "Item key sold by this NPC."));
    const keySelect = document.createElement("select");
    keySelect.className = "form-control form-control-sm";
    const keyOptions = ["", ...state.itemKeyOptions];
    if (si.itemKey && !keyOptions.includes(si.itemKey)) keyOptions.splice(1, 0, si.itemKey);
    appendOptions(keySelect, keyOptions, si.itemKey || "");
    keySelect.addEventListener("input", () => {
      si.itemKey = keySelect.value;
      syncJsonEditor(item);
      markDirty();
      renderList();
    });
    keyWrap.appendChild(keySelect);

    const priceWrap = document.createElement("label");
    priceWrap.appendChild(createStaticFieldLabel("Price", "Gold price (0 = item default)."));
    const priceInput = document.createElement("input");
    priceInput.type = "number";
    priceInput.min = "0";
    priceInput.className = "form-control form-control-sm";
    priceInput.value = si.price || 0;
    priceInput.addEventListener("input", () => {
      si.price = parseInt(priceInput.value, 10) || 0;
      syncJsonEditor(item);
      markDirty();
    });
    priceWrap.appendChild(priceInput);

    const deleteButton = document.createElement("button");
    deleteButton.type = "button";
    deleteButton.className = "btn btn-danger btn-sm";
    deleteButton.textContent = "Delete";
    deleteButton.addEventListener("click", () => {
      item.shopItems.splice(index, 1);
      syncJsonEditor(item);
      markDirty();
      renderRelationEditor(item);
    });

    row.appendChild(keyWrap);
    row.appendChild(priceWrap);
    row.appendChild(deleteButton);
    section.appendChild(row);
  });

  el.relationEditorBlock.appendChild(section);
}

function renderTrainableStatsEditor(item) {
  if (!Array.isArray(item.trainableStats)) item.trainableStats = [];

  const section = document.createElement("div");
  section.className = "relation-subsection";
  const header = document.createElement("div");
  header.className = "relation-editor-header";
  const title = document.createElement("div");
  title.appendChild(createStaticFieldLabel("Trainable stats/skills", "Skills or stats this NPC can train. maxPoints 0 = no cap."));
  const strong = document.createElement("strong");
  strong.textContent = "Training list";
  title.appendChild(strong);
  const addButton = document.createElement("button");
  addButton.type = "button";
  addButton.className = "btn btn-outline-dark btn-sm";
  addButton.textContent = "Add skill";
  addButton.addEventListener("click", () => {
    item.trainableStats.push({ statId: state.trainableStatIdOptions[0] || "attack", costPerPoint: 10, maxPoints: 0 });
    syncJsonEditor(item);
    markDirty();
    renderRelationEditor(item);
  });
  header.appendChild(title);
  header.appendChild(addButton);
  section.appendChild(header);

  if (!item.trainableStats.length) {
    const empty = document.createElement("p");
    empty.className = "muted mb-0";
    empty.textContent = state.trainableStatIdOptions.length ? "No skills configured." : "Loading skill list...";
    section.appendChild(empty);
  }

  item.trainableStats.forEach((ts, index) => {
    const row = document.createElement("div");
    row.className = "relation-row taught-spell-row";

    const statWrap = document.createElement("label");
    statWrap.appendChild(createStaticFieldLabel("Skill / Stat", "Skill or stat id to train."));
    const statSelect = document.createElement("select");
    statSelect.className = "form-control form-control-sm";
    const statOptions = [...state.trainableStatIdOptions];
    if (ts.statId && !statOptions.includes(ts.statId)) statOptions.unshift(ts.statId);
    appendOptions(statSelect, statOptions, ts.statId || "");
    statSelect.addEventListener("input", () => {
      ts.statId = statSelect.value;
      syncJsonEditor(item);
      markDirty();
      renderList();
    });
    statWrap.appendChild(statSelect);

    const costWrap = document.createElement("label");
    costWrap.appendChild(createStaticFieldLabel("Cost/pt", "Gold per skill point."));
    const costInput = document.createElement("input");
    costInput.type = "number";
    costInput.min = "1";
    costInput.className = "form-control form-control-sm";
    costInput.value = ts.costPerPoint || 10;
    costInput.addEventListener("input", () => {
      ts.costPerPoint = parseInt(costInput.value, 10) || 1;
      syncJsonEditor(item);
      markDirty();
    });
    costWrap.appendChild(costInput);

    const maxWrap = document.createElement("label");
    maxWrap.appendChild(createStaticFieldLabel("Max pts", "Cap (0 = unlimited)."));
    const maxInput = document.createElement("input");
    maxInput.type = "number";
    maxInput.min = "0";
    maxInput.className = "form-control form-control-sm";
    maxInput.value = ts.maxPoints || 0;
    maxInput.addEventListener("input", () => {
      ts.maxPoints = parseInt(maxInput.value, 10) || 0;
      syncJsonEditor(item);
      markDirty();
    });
    maxWrap.appendChild(maxInput);

    const deleteButton = document.createElement("button");
    deleteButton.type = "button";
    deleteButton.className = "btn btn-danger btn-sm";
    deleteButton.textContent = "Delete";
    deleteButton.addEventListener("click", () => {
      item.trainableStats.splice(index, 1);
      syncJsonEditor(item);
      markDirty();
      renderRelationEditor(item);
    });

    row.appendChild(statWrap);
    row.appendChild(costWrap);
    row.appendChild(maxWrap);
    row.appendChild(deleteButton);
    section.appendChild(row);
  });

  el.relationEditorBlock.appendChild(section);
}

function createPartSpriteDropdown(item, part) {
  const proxy = { spriteBase: part.spriteBase || "" };
  const dropdown = createSpriteDropdown(proxy, "spriteBase", {
    options: state.puppetSpriteBaseOptions,
    suppressAutoPreview: true,
  });
  dropdown.addEventListener("sprite-dropdown-change", () => {
    part.spriteBase = proxy.spriteBase;
    syncJsonEditor(item);
    renderEntityPreview(item);
    markDirty();
    renderList();
  });
  return dropdown;
}

function syncJsonEditor(item) {
  if (!el.jsonBlock.classList.contains("hidden")) {
    el.jsonEditor.value = JSON.stringify(item, null, 2);
  }
}

function isToggleField(name) {
  return true;
}

async function ensureNpcOptions() {
  if (state.npcOptions.length) return;
  const data = await apiJson("/api/npcs");
  state.npcOptions = (data.items || [])
    .map((npc) => npc.name)
    .filter(Boolean)
    .sort((a, b) => a.localeCompare(b));
}

async function ensureMonsterOptions() {
  if (state.monsterOptions.length) return;
  const data = await apiJson("/api/monsters");
  state.monsterOptions = (data.items || [])
    .map((monster) => monster.name)
    .filter(Boolean)
    .sort((a, b) => a.localeCompare(b));
}

async function ensureObjectDefinitionOptions() {
  if (state.objectDefinitionOptions.length) return;
  const data = await apiJson("/api/object-mappings");
  state.objectDefinitionOptions = (data.items || [])
    .map((object) => object.logicalName)
    .filter(Boolean)
    .sort((a, b) => a.localeCompare(b));
}

function objectDefinitionNameOptions(currentValue) {
  const options = ["", ...state.objectDefinitionOptions];
  const current = String(currentValue || "").trim();
  if (current && !options.includes(current)) {
    options.splice(1, 0, current);
  }
  return options;
}

function clanNameOptions(currentValue) {
  const options = state.clanOptions.length ? state.clanOptions.slice() : ["NEUTRAL"];
  const current = String(currentValue || "").trim();
  if (current && !options.includes(current)) {
    options.unshift(current);
  }
  return options;
}

async function ensureSpellNameOptions() {
  if (state.spellNameOptions.length) return;
  const data = await apiJson("/api/npc-options");
  state.spellNameOptions = (data.spells || []).filter(Boolean).sort((a, b) => a.localeCompare(b));
}

async function ensureMonsterEditorOptions() {
  await Promise.all([ensureSpriteIndex(), ensureSoundOptions()]);
  if (!state.monsterPatternOptions.length) {
    state.monsterPatternOptions = baseMonsterPatternOptions(state.sprites);
  }
}

async function ensureAnimatedSpriteBaseOptions() {
  if (state.animatedSpriteBaseOptions.length) return;
  await ensureSpriteIndex();
  state.animatedSpriteBaseOptions = baseEquippedSpriteOptions(state.sprites);
  state.puppetSpriteBaseOptions = state.animatedSpriteBaseOptions.filter((sprite) => isPuppetSpriteBase(sprite.name));
}

async function ensureSpellEditorOptions() {
  if (state.spellEditorOptionsLoaded) return;
  await Promise.all([ensureSpriteIndex(), ensureSoundOptions()]);
  if (!state.spellSpriteOptions.length) {
    state.spellIconOptions = state.sprites
      .filter((sprite) => String(sprite.name || "").toLowerCase().includes("64kspellicon"))
      .sort((a, b) => a.name.localeCompare(b.name));
    state.spellSpriteOptions = baseSpellSpriteOptions(state.sprites);
  }
  state.spellEditorOptionsLoaded = true;
}

async function ensureItemSpriteOptions() {
  if (state.itemSpriteOptions.length) return;
  await ensureSpriteIndex();
  state.itemSpriteOptions = state.sprites
    .map((sprite) => ({ name: sprite.name, previewName: sprite.name }))
    .sort((a, b) => a.name.localeCompare(b.name));
  state.itemEquippedSpriteOptions = baseEquippedSpriteOptions(state.sprites);
}

function baseEquippedSpriteOptions(sprites) {
  const byBase = new Map();
  sprites.forEach((sprite) => {
    const name = String(sprite.name || "").trim();
    const base = equippedSpriteBaseName(name);
    if (!base) return;
    if (!byBase.has(base)) {
      byBase.set(base, { name: base, previewName: name, previewArea: spriteArea(sprite) });
      return;
    }
    const current = byBase.get(base);
    const area = spriteArea(sprite);
    if (area > current.previewArea || (area === current.previewArea && name.localeCompare(current.previewName) < 0)) {
      current.previewName = name;
      current.previewArea = area;
    }
  });
  return Array.from(byBase.values())
    .map(({ name, previewName }) => ({ name, previewName }))
    .sort((a, b) => a.name.localeCompare(b.name));
}

function baseMonsterPatternOptions(sprites) {
  const byBase = new Map();
  sprites.forEach((sprite) => {
    const name = String(sprite.name || "").trim();
    const match = name.match(/^(.*?)(?:(000|045|090|135|180|225|270|315)-)?([a-z])$/i);
    if (!match || !match[1]) return;
    const base = match[1];
    if (!byBase.has(base)) {
      byBase.set(base, { name: base, previewName: name, previewArea: spriteArea(sprite) });
      return;
    }
    const current = byBase.get(base);
    const area = spriteArea(sprite);
    if (area > current.previewArea || (area === current.previewArea && name.localeCompare(current.previewName) < 0)) {
      current.previewName = name;
      current.previewArea = area;
    }
  });
  return Array.from(byBase.values())
    .map(({ name, previewName }) => ({ name, previewName }))
    .sort((a, b) => a.name.localeCompare(b.name));
}

function isPuppetSpriteBase(value) {
  return /^(Pup|Wo)/i.test(String(value || "").trim());
}

function equippedSpriteBaseName(spriteName) {
  const match = String(spriteName || "").trim().match(/^(.*?)(000|045|090|135|180|225|270|315)-[a-z]$/i);
  return match && match[1] ? match[1] : "";
}

function itemBodyPartOptions(currentValue) {
  const options = [
    "",
    "LEGS",
    "FEET",
    "BODY",
    "HEAD",
    "BELT",
    "NECK",
    "BRACER",
    "BACK",
    "RING1",
    "RING2",
    "LEFT_ARM",
    "RIGHT_ARM",
    "LEFT_HAND",
    "RIGHT_HAND",
    "SHIELD",
    "WEAPON",
    "WEAPON2",
    "BOOT",
    "ROBELEGS",
    "HAIR",
    "HAT",
    "MASK",
    "CAPE",
  ];
  const current = String(currentValue || "").trim();
  if (current && !options.includes(current)) {
    options.splice(1, 0, current);
  }
  return options;
}

function baseSpellSpriteOptions(sprites) {
  const byBase = new Map();
  const animatedBases = findAnimatedSpellBases(sprites);
  sprites
    .forEach((sprite) => {
      const name = String(sprite.name || "").trim();
      if (!name) return;
      const base = spellOptionBaseName(name, animatedBases);
      if (!byBase.has(base)) {
        byBase.set(base, { name: base, previewName: name, previewArea: spriteArea(sprite) });
        return;
      }
      const current = byBase.get(base);
      const area = spriteArea(sprite);
      if (area > current.previewArea || (area === current.previewArea && name.localeCompare(current.previewName) < 0)) {
        current.previewName = name;
        current.previewArea = area;
      }
    });
  return Array.from(byBase.values())
    .map(({ name, previewName }) => ({ name, previewName }))
    .sort((a, b) => a.name.localeCompare(b.name));
}

function spriteArea(sprite) {
  return Math.max(0, Number(sprite?.width) || 0) * Math.max(0, Number(sprite?.height) || 0);
}

function findAnimatedSpellBases(sprites) {
  const grouped = new Map();
  sprites.forEach((sprite) => {
    const name = String(sprite.name || "").trim();
    const directional = name.match(/^(.*?)(000|045|090|135|180|225|270|315)(?:-\d*[a-z])?$/i);
    if (directional && directional[1]) {
      const key = directional[1].toLowerCase();
      grouped.set(key, (grouped.get(key) || 0) + 1);
      return;
    }
    const dashed = name.match(/^(.*?)-\d*[a-z]$/i);
    if (dashed && dashed[1]) {
      const key = dashed[1].toLowerCase();
      grouped.set(key, (grouped.get(key) || 0) + 1);
    }
  });
  const bases = new Set();
  grouped.forEach((count, base) => {
    if (count > 1) bases.add(base);
  });
  return bases;
}

function spellAnimationBaseName(spriteName) {
  const value = String(spriteName || "").trim();
  const match = value.match(/^(.*?)(?:-\d*[a-z])$/i);
  return match && match[1] ? match[1] : value;
}

function spellOptionBaseName(spriteName, animatedBases = null) {
  const value = String(spriteName || "").trim();
  const directional = value.match(/^(.*?)(000|045|090|135|180|225|270|315)(?:-\d*[a-z])?$/i);
  if (directional && directional[1]) {
    const base = directional[1];
    return !animatedBases || animatedBases.has(base.toLowerCase()) ? base : value;
  }
  if (isSpellAnimationFrameName(value)) {
    const base = spellAnimationBaseName(value);
    return !animatedBases || animatedBases.has(base.toLowerCase()) ? base : value;
  }
  return value;
}

function isSpellAnimationFrameName(spriteName) {
  return /^(.*?)-\d*[a-z]$/i.test(String(spriteName || "").trim());
}

async function ensureSoundOptions() {
  if (state.soundOptions.length) return;
  const data = await apiJson("/api/sounds");
  state.soundOptions = (data.items || []).filter(Boolean).sort((a, b) => a.localeCompare(b));
}

function appendOptions(select, options, currentValue) {
  const seen = new Set();
  options.forEach((value) => {
    if (seen.has(value)) return;
    seen.add(value);
    const option = document.createElement("option");
    option.value = value;
    option.textContent = displayContentValue(value) || "None";
    select.appendChild(option);
  });
  if (currentValue && !seen.has(currentValue)) {
    const option = document.createElement("option");
    option.value = currentValue;
    option.textContent = displayContentValue(currentValue);
    select.prepend(option);
  }
  select.value = currentValue || "";
}

function createSpriteDropdown(item, fieldName, config = {}) {
  const root = document.createElement("div");
  root.className = "sprite-dropdown";
  const button = document.createElement("div");
  button.className = "form-control form-control-sm sprite-dropdown-button";
  const icon = document.createElement("img");
  const text = document.createElement("input");
  text.type = "text";
  text.autocomplete = "off";
  text.spellcheck = false;
  button.appendChild(icon);
  button.appendChild(text);
  const menu = document.createElement("div");
  menu.className = "sprite-dropdown-menu hidden";
  root.appendChild(button);
  root.appendChild(menu);
  let menuRendered = false;

  const options = (config.options || spriteDropdownOptions(fieldName)).slice();
  let currentValue = item[fieldName] || "";
  if (state.section === "monsters" && ["walkPattern", "attackPattern", "deathPattern"].includes(fieldName) && currentValue) {
    const normalizedValue = monsterPatternBaseName(currentValue);
    if (normalizedValue !== currentValue && options.some((sprite) => sprite.name === normalizedValue)) {
      item[fieldName] = normalizedValue;
      currentValue = normalizedValue;
    }
  }
  if (state.section === "spells" && fieldName !== "iconId" && currentValue) {
    const normalizedValue = spellOptionBaseName(currentValue);
    if (normalizedValue !== currentValue && options.some((sprite) => sprite.name === normalizedValue)) {
      item[fieldName] = normalizedValue;
      currentValue = normalizedValue;
    }
  }
  if (currentValue && !options.some((sprite) => sprite.name === currentValue)) {
    options.unshift({ name: currentValue });
  }

  const updateButton = () => {
    const value = item[fieldName] || "";
    text.value = displaySpriteDropdownValue(value, fieldName);
    text.placeholder = "Search...";
    icon.style.visibility = value ? "visible" : "hidden";
    if (value) {
      const previewName = options.find((sprite) => sprite.name === value)?.previewName || previewSpriteNameForDropdownValue(value, fieldName);
      if (previewName) {
        icon.src = spriteImageUrl(previewName);
      } else {
        icon.removeAttribute("src");
        icon.style.visibility = "hidden";
      }
    }
  };

  const filterOptions = () => {
    renderSpriteDropdownMenu();
    const query = text.value.trim().toLowerCase();
    menu.querySelectorAll(".sprite-dropdown-option").forEach((row) => {
      const value = row.dataset.value || "";
      const displayValue = row.dataset.display || value;
      row.classList.toggle("hidden", query && !value.toLowerCase().includes(query) && !displayValue.toLowerCase().includes(query));
    });
  };

  const choose = (value) => {
    item[fieldName] = value;
    if (state.section === "monsters" && fieldName === "walkPattern" && value) {
      if (!item.attackPattern) item.attackPattern = value + "A";
      if (!item.deathPattern)  item.deathPattern  = value + "C-";
      renderEditor();
    }
    menu.classList.add("hidden");
    updateButton();
    if (!el.jsonBlock.classList.contains("hidden")) {
      el.jsonEditor.value = JSON.stringify(item, null, 2);
    }
    if (!config.suppressAutoPreview) {
      renderEntityPreview(item);
      markDirty();
      renderList();
    }
    root.dispatchEvent(new CustomEvent("sprite-dropdown-change", { bubbles: true, detail: { fieldName, value } }));
  };

  const renderSpriteDropdownMenu = () => {
    if (menuRendered) return;
    menuRendered = true;
    const fragment = document.createDocumentFragment();
    const none = document.createElement("button");
    none.type = "button";
    none.className = `sprite-dropdown-option ${!currentValue ? "active" : ""}`;
    none.dataset.value = "";
    none.textContent = "None";
    none.addEventListener("click", () => choose(""));
    fragment.appendChild(none);

    options.forEach((sprite) => {
      const row = document.createElement("button");
      row.type = "button";
      row.className = `sprite-dropdown-option ${sprite.name === currentValue ? "active" : ""}`;
      row.dataset.value = sprite.name;
      row.dataset.display = displaySpriteDropdownValue(sprite.name, fieldName);
      const img = document.createElement("img");
      img.loading = "lazy";
      img.dataset.spritePreview = previewSpriteNameForDropdownValue(sprite.name, fieldName);
      const label = document.createElement("span");
      label.textContent = displaySpriteDropdownValue(sprite.name, fieldName);
      row.appendChild(img);
      row.appendChild(label);
      row.addEventListener("click", () => choose(sprite.name));
      fragment.appendChild(row);
    });
    menu.appendChild(fragment);
  };

  const loadVisibleSpriteDropdownImages = () => {
    menu.querySelectorAll("img[data-sprite-preview]").forEach((img) => {
      if (img.src) return;
      const previewName = img.dataset.spritePreview;
      if (previewName) {
        img.src = spriteImageUrl(previewName);
      } else {
        img.style.visibility = "hidden";
      }
    });
  };

  text.addEventListener("focus", () => {
    renderSpriteDropdownMenu();
    document.querySelectorAll(".sprite-dropdown-menu").forEach((other) => {
      if (other !== menu) other.classList.add("hidden");
    });
    menu.classList.remove("hidden");
    filterOptions();
    loadVisibleSpriteDropdownImages();
  });
  text.addEventListener("input", () => {
    filterOptions();
    loadVisibleSpriteDropdownImages();
  });
  button.addEventListener("click", () => {
    text.focus();
  });
  updateButton();
  return root;
}

function monsterPatternBaseName(pattern) {
  return String(pattern || "").trim().replace(/#[a-z]$/i, "");
}

function displaySpriteDropdownValue(value, fieldName) {
  return displayContentValue(value);
}

function displayContentValue(value) {
  return String(value ?? "")
    .replace(/(?:-%s|%d)\$\d+$/i, "")
    .replace(/(?:000|045|090|135|180|225|270|315)-[a-z]$/i, "")
    .replace(/#[a-z]$/i, "")
    .replace(/-[a-z]$/i, "");
}

function normalizeContentKey(value) {
  return String(value ?? "").trim().toLowerCase();
}

function previewSpriteNameForDropdownValue(value, fieldName) {
  const options = spriteDropdownOptions(fieldName);
  const match = options.find((sprite) => sprite.name === value);
  if (match?.previewName) return match.previewName;
  const resolved = resolvePreviewSprite([spriteLookupValue(value, fieldName), value]);
  return resolved?.name || "";
}

function spriteLookupValue(value, fieldName) {
  const raw = String(value || "");
  if (fieldName === "sprite" && ["objects", "decorRules", "itemIcons", "appearanceDefaults"].includes(state.section)) {
    return displaySpriteDropdownValue(raw, fieldName);
  }
  return raw;
}

function spriteDropdownOptions(fieldName) {
  if (state.section === "monsters") return state.monsterPatternOptions;
  if (state.section === "npcs") return state.animatedSpriteBaseOptions;
  if (["objects", "decorRules", "itemIcons", "appearanceDefaults"].includes(state.section)) return state.itemSpriteOptions;
  if (state.section === "items" && fieldName === "appearanceEquippedPrimary") return state.itemEquippedSpriteOptions;
  if (state.section === "items") return state.itemSpriteOptions;
  return fieldName === "iconId" ? state.spellIconOptions : state.spellSpriteOptions;
}

function markDirty() {
  state.dirty = true;
  el.dirtyBadge.classList.remove("hidden");
}

async function ensureSpriteIndex() {
  if (state.spriteIndexLoaded) return;
  const data = await apiJson("/api/sprites");
  state.sprites = data.items || [];
  state.spriteIndexLoaded = true;
}

async function renderEntityPreview(item) {
  if (!["npcs", "monsters", "items", "spells", "itemIcons", "appearanceDefaults", "concealmentRules"].includes(state.section)) {
    el.entityPreview.classList.add("hidden");
    return;
  }
  el.entityPreview.classList.remove("hidden");
  el.entityPreviewImage.parentElement.classList.remove("hidden");
  el.entityPreviewImage.style.display = "none";
  el.entityPreviewComposite.classList.add("hidden");
  el.entityPreviewComposite.replaceChildren();
  el.entityPreviewPlaceholder.style.display = "block";
  el.entityPreviewTitle.textContent = "Resolving sprite...";
  el.entityPreviewHint.textContent = "";
  try {
    await ensureSpriteIndex();
    if (state.section === "npcs" && Array.isArray(item.parts) && item.parts.length && !String(item.spriteBase || "").trim()) {
      await ensureNakedParts();
      renderNpcCompositePreview(item);
      return;
    }
    const candidates = previewCandidates(item);
    const sprite = resolvePreviewSprite(candidates);
    if (!sprite) {
      el.entityPreviewTitle.textContent = "No sprite resolved";
      el.entityPreviewHint.textContent = candidates.filter(Boolean).map(displayContentValue).join(", ");
      return;
    }
    el.entityPreviewTitle.textContent = "";
    el.entityPreviewHint.textContent = "";
    el.entityPreviewPlaceholder.style.display = "none";
    el.entityPreviewImage.onload = () => {
      el.entityPreviewImage.style.display = "block";
    };
    el.entityPreviewImage.onerror = () => {
      el.entityPreviewImage.style.display = "none";
      el.entityPreviewPlaceholder.style.display = "block";
      el.entityPreviewTitle.textContent = "Preview unavailable";
    };
    el.entityPreviewImage.src = spriteImageUrl(sprite.name);
  } catch (error) {
    console.error(error);
    el.entityPreviewTitle.textContent = "Preview failed";
  }
}

function renderNpcCompositePreview(item) {
  const layers = npcCompositeLayers(item);
  if (!layers.length) {
    el.entityPreviewTitle.textContent = "No multipart sprite resolved";
    el.entityPreviewHint.textContent = item.parts.map((part) => part.spriteBase).filter(Boolean).map(displayContentValue).join(", ");
    return;
  }

  let minX = Infinity;
  let minY = Infinity;
  let maxX = -Infinity;
  let maxY = -Infinity;
  layers.forEach((layer) => {
    minX = Math.min(minX, layer.sprite.off1X);
    minY = Math.min(minY, layer.sprite.off1Y);
    maxX = Math.max(maxX, layer.sprite.off1X + layer.sprite.width);
    maxY = Math.max(maxY, layer.sprite.off1Y + layer.sprite.height);
  });

  const width = Math.max(1, maxX - minX);
  const height = Math.max(1, maxY - minY);
  const scale = Math.min(3, 92 / width, 86 / height);
  el.entityPreviewComposite.style.width = `${width}px`;
  el.entityPreviewComposite.style.height = `${height}px`;
  el.entityPreviewComposite.style.transform = `scale(${scale})`;
  el.entityPreviewComposite.classList.remove("hidden");
  el.entityPreviewPlaceholder.style.display = "none";
  el.entityPreviewTitle.textContent = "NPC multipart preview";
  el.entityPreviewHint.textContent = layers.map((layer) => `${layer.part.bodyPart}:${displayContentValue(layer.part.spriteBase)}`).join(", ");

  layers.forEach((layer) => {
    const img = document.createElement("img");
    img.src = spriteImageUrl(layer.sprite.name);
    img.alt = layer.sprite.name;
    img.style.left = `${layer.sprite.off1X - minX}px`;
    img.style.top = `${layer.sprite.off1Y - minY}px`;
    img.style.width = `${layer.sprite.width}px`;
    img.style.height = `${layer.sprite.height}px`;
    el.entityPreviewComposite.appendChild(img);
  });
}

function npcCompositeLayers(item) {
  const byPart = effectiveNpcCompositeParts(item);
  const renderedParts = new Set();
  return NPC_PREVIEW_BODY_ORDER
    .filter((bodyPart) => {
      if (renderedParts.has(bodyPart)) return false;
      renderedParts.add(bodyPart);
      return true;
    })
    .map((bodyPart) => byPart.get(bodyPart))
    .filter(Boolean)
    .map((part) => ({ part, sprite: resolvePreviewSprite([`${part.spriteBase}000-a`, part.spriteBase]) }))
    .filter((layer) => layer.sprite);
}

function effectiveNpcCompositeParts(item) {
  const byPart = new Map();
  const explicitParts = new Set();
  (item.parts || []).forEach((part) => {
    const bodyPart = String(part?.bodyPart || "").trim();
    const spriteBase = String(part?.spriteBase || "").trim();
    if (bodyPart && spriteBase) {
      byPart.set(bodyPart, { bodyPart, spriteBase });
      explicitParts.add(bodyPart);
    }
  });
  const feet = byPart.get("FEET");
  if (feet && !/naked(?:foot|feet)/i.test(feet.spriteBase)) {
    byPart.delete("FEET");
    explicitParts.delete("FEET");
    if (!byPart.has("BOOT")) byPart.set("BOOT", { bodyPart: "BOOT", spriteBase: feet.spriteBase });
    explicitParts.add("BOOT");
  }
  const female = Array.from(byPart.values()).some((part) => /^Wo/i.test(part.spriteBase));
  const defaults = female ? NAKED_PARTS.FEMALE : NAKED_PARTS.MALE;
  Object.entries(defaults).forEach(([bodyPart, spriteBase]) => {
    if (!byPart.has(bodyPart)) byPart.set(bodyPart, { bodyPart, spriteBase });
  });

  const snapshot = Array.from(byPart.values());
  const hideAlways = new Set();
  const hideDefaultsOnly = new Set();
  snapshot.forEach((part) => {
    const slots = part.bodyPart === "HEAD" ? ["HEAD", "HAT"]
      : part.bodyPart === "HAT" ? ["HAT", "HEAD"] : [part.bodyPart];
    slots.forEach((slot) => concealmentRulesFor(slot, part.spriteBase).forEach((rule) => {
      String(rule.hiddenParts || "").split(",").map((value) => value.trim()).filter(Boolean)
        .filter((hidden) => hidden !== part.bodyPart)
        .forEach((hidden) => (rule.hidesExplicit ? hideAlways : hideDefaultsOnly).add(hidden));
    }));
  });
  hideAlways.forEach((bodyPart) => byPart.delete(bodyPart));
  hideDefaultsOnly.forEach((bodyPart) => removeDefaultNpcPart(byPart, explicitParts, bodyPart));
  return byPart;
}

function removeDefaultNpcPart(byPart, explicitParts, bodyPart) {
  if (!explicitParts.has(bodyPart)) byPart.delete(bodyPart);
}

function concealmentRulesFor(triggerSlot, appearance) {
  const raw = `${triggerSlot}|${String(appearance || "").trim()}`.toUpperCase();
  if (CONCEALMENT_RULES.has(raw)) return CONCEALMENT_RULES.get(raw);
  const normalized = raw.split("__", 1)[0];
  return CONCEALMENT_RULES.get(normalized) || [];
}

// Naked fallbacks come from assets/mappings/appearance/appearance_defaults.bin, the same table the game
// reads, so the preview cannot drift from it. Cached because the composite preview is synchronous;
// ensureNakedParts() primes it and is awaited before a preview renders.
const NAKED_PARTS = { MALE: {}, FEMALE: {} };
const CONCEALMENT_RULES = new Map();
let nakedPartsLoaded = false;

async function ensureNakedParts() {
  if (nakedPartsLoaded) return;
  try {
    const data = await apiJson("/api/appearance-defaults");
    (data.items || []).forEach((part) => {
      const gender = String(part?.gender || "").trim().toUpperCase();
      const bodyPart = String(part?.bodyPart || "").trim();
      const sprite = String(part?.sprite || "").trim();
      if (NAKED_PARTS[gender] && bodyPart && sprite) NAKED_PARTS[gender][bodyPart] = sprite;
    });
    const concealment = await apiJson("/api/concealment");
    (concealment.items || []).forEach((rule) => {
      const triggerSlot = String(rule?.triggerSlot || "").trim().toUpperCase();
      const appearance = String(rule?.appearance || "").trim().toUpperCase();
      if (!triggerSlot || !appearance) return;
      const key = `${triggerSlot}|${appearance}`;
      if (!CONCEALMENT_RULES.has(key)) CONCEALMENT_RULES.set(key, []);
      CONCEALMENT_RULES.get(key).push(rule);
    });
  } catch (error) {
    // Preview-only data: an unreadable table degrades to equipped parts, it must not break the UI.
  }
  nakedPartsLoaded = true;
}

function spriteImageUrl(spriteName) {
  if (!spriteName) return "";
  return `/api/sprite/${encodeURIComponent(spriteName)}`;
}

function animatePreviewImage(img, frames) {
  const token = {};
  img._previewAnimationToken = token;
  Promise.all(frames.map(preloadSpriteImage))
    .then((urls) => {
      if (img._previewAnimationToken !== token || !document.body.contains(img)) return;
      let index = 0;
      img.src = urls[index];
      const timer = setInterval(() => {
        if (img._previewAnimationToken !== token || !document.body.contains(img)) {
          clearInterval(timer);
          return;
        }
        index = (index + 1) % urls.length;
        img.src = urls[index];
      }, 180);
    })
    .catch(() => {
      if (img._previewAnimationToken === token && frames.length) {
        img.src = spriteImageUrl(frames[0]);
      }
    });
}

function preloadSpriteImage(spriteName) {
  const key = String(spriteName || "");
  if (state.spritePreviewCache.has(key)) return state.spritePreviewCache.get(key);
  const promise = fetch(spriteImageUrl(key))
    .then((response) => {
      if (!response.ok) throw new Error(`Sprite ${key} failed: ${response.status}`);
      return response.blob();
    })
    .then((blob) => URL.createObjectURL(blob))
    .catch((error) => {
      state.spritePreviewCache.delete(key);
      throw error;
    });
  state.spritePreviewCache.set(key, promise);
  return promise;
}

function previewCandidates(item) {
  if (state.section === "npcs") {
    const partSprite = Array.isArray(item.parts) && item.parts.length ? item.parts[0]?.spriteBase : "";
    return [item.spriteBase, partSprite];
  }
  if (state.section === "monsters") {
    return [item.walkPattern, item.attackPattern, item.deathPattern];
  }
  if (state.section === "items") {
    return [item.appearanceInventory, item.appearanceEquippedPrimary, item.appearanceEquippedSecondary];
  }
  if (state.section === "spells") {
    return [item.iconId, item.projectileSpell, item.impactSpell];
  }
  if (state.section === "itemIcons") {
    return [item.sprite];
  }
  if (state.section === "appearanceDefaults") {
    return [item.sprite];
  }
  if (state.section === "concealmentRules") {
    return [item.appearance];
  }
  return [];
}

function resolvePreviewSprite(candidates) {
  const names = state.sprites || [];
  const byLower = new Map(names.map((sprite) => [sprite.name.toLowerCase(), sprite]));
  for (const raw of candidates) {
    const value = String(raw || "").trim();
    if (!value) continue;
    const exact = byLower.get(value.toLowerCase());
    if (exact) return exact;
    const monsterPattern = value.match(/^(.*?)#([a-z])$/i);
    if (monsterPattern && monsterPattern[1]) {
      const base = monsterPattern[1].toLowerCase();
      const frame = monsterPattern[2].toLowerCase();
      const patternMatch = names.find((sprite) => {
        const lower = sprite.name.toLowerCase();
        return lower.startsWith(`${base}000-${frame}`)
          || lower.startsWith(`${base}045-${frame}`)
          || lower.startsWith(`${base}090-${frame}`)
          || lower === `${base}-${frame}`;
      });
      if (patternMatch) return patternMatch;
    }
    const normalized = value.toLowerCase();
    const frameMatch = names.find((sprite) =>
      sprite.name.toLowerCase().match(new RegExp(`^${escapeRegExp(normalized)}-[a-z]$`, "i")));
    if (frameMatch) return frameMatch;
    const animationPrefixes = [`${normalized}000-`, `${normalized}-000-`, `${normalized}_000-`, normalized];
    const match = names.find((sprite) => animationPrefixes.some((prefix) => sprite.name.toLowerCase().startsWith(prefix)));
    if (match) return match;
  }
  return null;
}

function escapeRegExp(value) {
  return String(value || "").replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
}

function renderPlacementPreview(item) {
  if (!["npcSpawns", "monsterSpawns", "objectPositions"].includes(state.section) || !item) {
    el.placementPreview.classList.add("hidden");
    return;
  }
  const kind = state.section === "npcSpawns" ? "npc" : state.section === "objectPositions" ? "object" : "monster";
  el.placementPreview.classList.remove("hidden");
  el.placementPreviewTitle.textContent = `${item.type || item.name || "Position"} at ${item.x ?? 0}, ${item.y ?? 0}, z${item.z ?? 0}`;
  const params = new URLSearchParams({
    map: state.mapPath || "",
    kind,
    x: String(item.x ?? ""),
    y: String(item.y ?? ""),
    t: String(Date.now()),
  });
  resetPlacementPreviewZoom();
  el.placementPreviewImage.src = `/api/map-preview?${params.toString()}`;
}

function resetPlacementPreviewZoom() {
  state.placementPreviewZoom = 1;
  applyPlacementPreviewZoom();
  if (el.placementPreviewStage) {
    el.placementPreviewStage.scrollLeft = 0;
    el.placementPreviewStage.scrollTop = 0;
  }
}

function applyPlacementPreviewZoom() {
  if (!el.placementPreviewImage) return;
  el.placementPreviewImage.style.width = `${state.placementPreviewZoom * 100}%`;
}

function handlePlacementPreviewWheel(event) {
  if (el.placementPreview.classList.contains("hidden")) return;
  event.preventDefault();
  const stage = el.placementPreviewStage;
  if (!stage) return;
  const oldZoom = state.placementPreviewZoom;
  const direction = event.deltaY < 0 ? 1 : -1;
  const nextZoom = Math.max(1, Math.min(8, oldZoom * (direction > 0 ? 1.25 : 0.8)));
  if (Math.abs(nextZoom - oldZoom) < 0.001) return;

  const rect = stage.getBoundingClientRect();
  const cursorX = event.clientX - rect.left;
  const cursorY = event.clientY - rect.top;
  const worldX = (stage.scrollLeft + cursorX) / oldZoom;
  const worldY = (stage.scrollTop + cursorY) / oldZoom;

  state.placementPreviewZoom = nextZoom;
  applyPlacementPreviewZoom();

  stage.scrollLeft = worldX * nextZoom - cursorX;
  stage.scrollTop = worldY * nextZoom - cursorY;
}

function currentPayloadItems() {
  if (sectionConfig().kind === "collisionRules") {
    state.collisionRules.exactSprites = (state.collisionRules.exactSprites || [])
      .filter((rule) => (rule.tiles || []).length || (rule.clearTiles || []).length)
      .sort((a, b) => String(a.sprite || "").localeCompare(String(b.sprite || "")));
    return state.collisionRules;
  }
  if (!el.jsonBlock.classList.contains("hidden") && el.jsonEditor.value.trim()) {
    try {
      const parsed = JSON.parse(el.jsonEditor.value);
      if (sectionConfig().mode === "json") return parsed;
      state.items[state.selectedIndex] = parsed;
    } catch (err) {
      setStatus("Invalid JSON");
      throw err;
    }
  }
  return { items: state.items };
}

async function saveCurrent() {
  const config = sectionConfig();
  if (config.kind === "sprites") {
    await saveSprite();
    return;
  }
  const payload = currentPayloadItems();
  await apiJson(endpointFor(config), {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  if (state.section === "appearanceDefaults") {
    // The NPC composite preview caches this table; drop it so it picks the new sprites up.
    nakedPartsLoaded = false;
  }
  if (state.section === "quests") {
    // A quest may have been added, renamed or removed; reload GIVE_QUEST choices
    // the next time the NPC dialogue editor is opened.
    state.questIdOptions = [];
    state.dialogueEditorOptionsLoaded = false;
  }
  state.dirty = false;
  el.dirtyBadge.classList.add("hidden");
  toast("Saved");
  await loadCurrent();
}

function newRecord() {
  const config = sectionConfig();
  if (config.kind === "sprites") return;
  const item = {};
  (fields[state.section] || [`${config.key}`]).forEach((descriptor) => {
    const [name, type = "text"] = descriptor.split(":");
    item[name] = type === "number" ? 0 : type === "boolean" ? false : "";
  });
  if (config.key && item[config.key] === "") item[config.key] = `New${state.items.length + 1}`;
  applySelectedMapZ(item);
  state.items.push(item);
  state.selectedIndex = state.items.length - 1;
  markDirty();
  renderList();
  renderEditor();
}

async function duplicateRecord() {
  const item = state.items[state.selectedIndex];
  if (!item) return;
  const copy = JSON.parse(JSON.stringify(item));
  const config = sectionConfig();
  if (isPlacementSection()) {
    const position = await findDuplicatePlacementPosition(item);
    copy.x = position.x;
    copy.y = position.y;
    applySelectedMapZ(copy);
  } else if (copy[config.key] !== undefined) {
    copy[config.key] = `${copy[config.key]}Copy`;
  }
  state.items.push(copy);
  state.selectedIndex = state.items.length - 1;
  markDirty();
  renderList();
  renderEditor();
}

function isPlacementSection() {
  return ["monsterSpawns", "npcSpawns", "objectPositions"].includes(state.section);
}

function applySelectedMapZ(item) {
  if (!item || !["monsterSpawns", "npcSpawns", "objectPositions"].includes(state.section)) return;
  item.z = selectedMapZ();
}

function selectedMapZ() {
  const map = state.maps.find((entry) => entry.path === state.mapPath) || {};
  const value = `${map.displayName || ""} ${map.fileName || ""} ${map.path || state.mapPath || ""}`.toLowerCase();
  if (value.includes("dungeon")) return 20;
  if (value.includes("cavern")) return 30;
  if (value.includes("underworld")) return 40;
  if (value.includes("worldmap")) return 1;
  const match = value.match(/(?:^|[^0-9])z(?:one)?[_ -]?([0-9]+)/i) || value.match(/(?:^|[^0-9])([0-9]+)\.mapbin/i);
  return match ? Number(match[1]) : 0;
}

async function findDuplicatePlacementPosition(item) {
  if (!["monsterSpawns", "npcSpawns"].includes(state.section)) {
    return { x: Number(item.x) || 0, y: Number(item.y) || 0 };
  }
  const params = new URLSearchParams();
  params.set("map", state.mapPath || "");
  params.set("x", String(Number(item.x) || 0));
  params.set("y", String(Number(item.y) || 0));
  params.set("radius", "10");
  try {
    return await apiJson(`/api/valid-spawn-position?${params.toString()}`);
  } catch (err) {
    setStatus("Could not find a valid nearby tile; duplicated at original position");
    return { x: Number(item.x) || 0, y: Number(item.y) || 0 };
  }
}

function deleteRecord() {
  if (state.selectedIndex < 0) return;
  state.items.splice(state.selectedIndex, 1);
  state.selectedIndex = Math.min(state.selectedIndex, state.items.length - 1);
  markDirty();
  renderList();
  renderEditor();
}

async function loadSprites() {
  const data = await apiJson("/api/sprites");
  state.sprites = data.items || [];
  state.currentSpriteBin = data.file || state.currentSpriteBin || "";
  state.spriteIndexLoaded = true;
  clampSpritePage();
  renderSpriteList();
  el.count.textContent = String(state.sprites.length);
  setStatus(`Loaded ${state.sprites.length} sprite(s) from ${activeSpriteBinName() || "sprites.bin"}`);
}

async function loadSpriteBins() {
  const data = await apiJson("/api/sprite-bins");
  state.spriteBins = data.items || [];
  state.currentSpriteBin = data.current || state.currentSpriteBin || "";
  el.spriteBinSelect.replaceChildren();
  state.spriteBins.forEach((bin) => {
    const option = document.createElement("option");
    option.value = bin.path || bin.name;
    option.textContent = `${bin.name} (${formatBytes(bin.size || 0)})`;
    option.selected = Boolean(bin.active);
    el.spriteBinSelect.appendChild(option);
  });
  const active = state.spriteBins.find((bin) => bin.active);
  if (active) {
    state.currentSpriteBin = active.path || active.name;
    el.spriteBinSelect.value = active.path || active.name;
  }
}

async function switchSpriteBin() {
  const path = el.spriteBinSelect.value;
  if (!path || path === state.currentSpriteBin) return;
  const data = await apiJson("/api/sprite-bin", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ path }),
  });
  state.currentSpriteBin = data.file || path;
  state.selectedSprite = null;
  state.spritePage = 0;
  state.spritePreviewCache.clear();
  state.spriteIndexLoaded = false;
  state.itemSpriteOptions = [];
  state.itemEquippedSpriteOptions = [];
  state.monsterPatternOptions = [];
  state.animatedSpriteBaseOptions = [];
  state.puppetSpriteBaseOptions = [];
  state.spellIconOptions = [];
  state.spellSpriteOptions = [];
  state.spellEditorOptionsLoaded = false;
  await loadSpriteBins();
  await loadSprites();
  toast(`Sprite bin loaded: ${activeSpriteBinName() || path}`);
}

function activeSpriteBinName() {
  const active = state.spriteBins.find((bin) => bin.active || bin.path === state.currentSpriteBin);
  if (active?.name) return active.name;
  if (!state.currentSpriteBin) return "";
  return state.currentSpriteBin.split(/[\\/]/).pop();
}

function formatBytes(bytes) {
  if (!Number.isFinite(bytes) || bytes <= 0) return "0 B";
  const units = ["B", "KB", "MB", "GB"];
  let value = bytes;
  let index = 0;
  while (value >= 1024 && index < units.length - 1) {
    value /= 1024;
    index++;
  }
  return `${value.toFixed(index === 0 ? 0 : 1)} ${units[index]}`;
}

function renderSpriteList() {
  const term = el.spriteFilter.value.trim().toLowerCase();
  const filtered = term
    ? state.sprites.filter((sprite) => sprite.name.toLowerCase().includes(term))
    : state.sprites;
  clampSpritePage(filtered.length);
  const start = state.spritePage * state.spritePageSize;
  const visible = filtered.slice(start, start + state.spritePageSize);
  const fragment = document.createDocumentFragment();
  el.spriteList.replaceChildren();
  visible.forEach((sprite) => {
    const row = document.createElement("button");
    row.type = "button";
    row.className = "record-row";
    row.classList.toggle("active", state.selectedSprite?.name === sprite.name);
    row.innerHTML = `<strong></strong><span></span>`;
    row.querySelector("strong").textContent = sprite.name;
    row.querySelector("span").textContent = `${sprite.width} x ${sprite.height}`;
    row.addEventListener("click", () => showSprite(sprite));
    fragment.appendChild(row);
  });
  el.spriteList.appendChild(fragment);
  updateSpritePager(filtered.length, visible.length);
}

function clampSpritePage(total = null) {
  const count = total ?? filteredSpriteCount();
  const maxPage = Math.max(0, Math.ceil(count / state.spritePageSize) - 1);
  state.spritePage = Math.max(0, Math.min(state.spritePage, maxPage));
}

function filteredSpriteCount() {
  const term = el.spriteFilter.value.trim().toLowerCase();
  if (!term) return state.sprites.length;
  let count = 0;
  for (const sprite of state.sprites) {
    if (sprite.name.toLowerCase().includes(term)) count++;
  }
  return count;
}

function updateSpritePager(total, visibleCount) {
  const start = total === 0 ? 0 : state.spritePage * state.spritePageSize + 1;
  const end = total === 0 ? 0 : start + visibleCount - 1;
  el.spritePageLabel.textContent = `${start}-${end} / ${total}`;
  el.spritePrevBtn.disabled = state.spritePage <= 0;
  el.spriteNextBtn.disabled = (state.spritePage + 1) * state.spritePageSize >= total;
}

function showSprite(sprite) {
  state.selectedSprite = sprite;
  el.spriteName.textContent = sprite.name;
  el.spriteMeta.textContent = `${sprite.width} x ${sprite.height} / type ${sprite.type}`;
  el.renameInput.value = sprite.name;
  el.off1x.value = sprite.off1X ?? 0;
  el.off1y.value = sprite.off1Y ?? 0;
  el.off2x.value = sprite.off2X ?? 0;
  el.off2y.value = sprite.off2Y ?? 0;
  el.previewPlaceholder.style.display = "block";
  el.previewImg.style.display = "none";
  el.previewImg.onload = () => {
    el.previewPlaceholder.style.display = "none";
    el.previewImg.style.display = "block";
  };
  el.previewImg.src = `/api/sprite/${encodeURIComponent(sprite.name)}?t=${Date.now()}`;
  renderSpriteList();
}

async function saveSprite() {
  if (!state.selectedSprite) return;
  const oldName = state.selectedSprite.name;
  const newName = el.renameInput.value.trim();
  if (newName && newName !== oldName) {
    await apiJson("/api/rename", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ oldName, newName }),
    });
    state.selectedSprite.name = newName;
  }
  await apiJson("/api/offsets", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      name: newName || oldName,
      off1X: Number(el.off1x.value) || 0,
      off1Y: Number(el.off1y.value) || 0,
      off2X: Number(el.off2x.value) || 0,
      off2Y: Number(el.off2y.value) || 0,
    }),
  });
  toast("Sprite saved");
  await loadSprites();
}

async function handleFiles(files) {
  const items = [];
  for (const file of files) {
    if (!file.name.toLowerCase().match(/\.(png|bmp)$/)) continue;
    const dataUrl = await new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => resolve(reader.result);
      reader.onerror = reject;
      reader.readAsDataURL(file);
    });
    items.push({ name: file.name.replace(/\.[^/.]+$/, ""), dataUrl });
  }
  if (!items.length) return;
  await apiJson("/api/upload", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ items }),
  });
  toast("Sprites imported");
  await loadSprites();
}

el.reloadBtn.addEventListener("click", () => withLoader(`Reloading ${sectionConfig().label}...`, loadCurrent));
el.saveBtn.addEventListener("click", () => withLoader("Saving...", saveCurrent));
el.filter.addEventListener("input", renderList);
el.newBtn.addEventListener("click", newRecord);
el.duplicateBtn.addEventListener("click", () => withLoader("Duplicating...", duplicateRecord));
el.deleteBtn.addEventListener("click", deleteRecord);
el.jsonEditor.addEventListener("input", markDirty);
el.mapSelect.addEventListener("change", async () => {
  state.mapPath = el.mapSelect.value;
  if (isPlacementSection() && state.items[state.selectedIndex]) {
    applySelectedMapZ(state.items[state.selectedIndex]);
  }
  await withLoader(`Loading ${sectionConfig().label}...`, loadCurrent);
});
el.spriteBinSelect.addEventListener("change", () => withLoader("Loading sprite bin...", switchSpriteBin));
el.spriteFilter.addEventListener("input", () => {
  state.spritePage = 0;
  clearTimeout(spriteFilterTimer);
  spriteFilterTimer = setTimeout(renderSpriteList, 120);
});
el.spritePrevBtn.addEventListener("click", () => {
  state.spritePage = Math.max(0, state.spritePage - 1);
  renderSpriteList();
});
el.spriteNextBtn.addEventListener("click", () => {
  state.spritePage += 1;
  renderSpriteList();
});
el.saveSpriteBtn.addEventListener("click", () => withLoader("Saving sprite...", saveSprite));
el.editorTabs.addEventListener("click", (event) => {
  const button = event.target.closest(".editor-tab");
  if (button) showEditorTab(button.dataset.tab);
});
el.uploadZone.addEventListener("click", () => el.fileInput.click());
el.fileInput.addEventListener("change", (event) => withLoader("Importing sprites...", () => handleFiles(Array.from(event.target.files || []))));
el.uploadZone.addEventListener("dragover", (event) => {
  event.preventDefault();
  el.uploadZone.classList.add("dragover");
});
el.uploadZone.addEventListener("dragleave", () => el.uploadZone.classList.remove("dragover"));
el.uploadZone.addEventListener("drop", (event) => {
  event.preventDefault();
  el.uploadZone.classList.remove("dragover");
  withLoader("Importing sprites...", () => handleFiles(Array.from(event.dataTransfer.files || [])));
});
el.spriteDeleteBtn.addEventListener("click", () => withLoader("Deleting sprite...", async () => {
  if (!state.selectedSprite) return;
  await apiJson("/api/delete", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ names: [state.selectedSprite.name] }),
  });
  state.selectedSprite = null;
  await loadSprites();
}));
el.spriteExportBtn.addEventListener("click", () => withLoader("Exporting sprite...", async () => {
  if (!state.selectedSprite) return;
  const res = await fetch("/api/export", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ names: [state.selectedSprite.name] }),
  });
  const blob = await res.blob();
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = "sprites.zip";
  document.body.appendChild(link);
  link.click();
  link.remove();
  URL.revokeObjectURL(url);
}));
el.placementPreviewStage.addEventListener("wheel", handlePlacementPreviewWheel, { passive: false });
document.addEventListener("mouseover", (event) => {
  const help = event.target.closest?.(".field-help");
  if (help) showFieldTooltip(help);
});
document.addEventListener("focusin", (event) => {
  const help = event.target.closest?.(".field-help");
  if (help) showFieldTooltip(help);
});
document.addEventListener("mousemove", (event) => {
  const help = event.target.closest?.(".field-help");
  if (help) positionFieldTooltip(help);
});
document.addEventListener("mouseout", (event) => {
  if (event.target.closest?.(".field-help")) hideFieldTooltip();
});
document.addEventListener("focusout", (event) => {
  if (event.target.closest?.(".field-help")) hideFieldTooltip();
});
window.addEventListener("scroll", hideFieldTooltip, true);
window.addEventListener("resize", hideFieldTooltip);

buildNav();
withLoader("Loading studio...", async () => {
  await loadMaps();
  await selectSection("sprites");
})
  .catch((error) => {
    console.error(error);
    setStatus("Failed to initialize");
  });
