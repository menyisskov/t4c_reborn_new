(function () {
  "use strict";

  var DATA = window.T4C_DATA || {};
  var MONSTERS = DATA.monsters || [];
  var SPELLS = DATA.spells || [];
  var QUESTS = DATA.quests || [];
  var NPCS = DATA.npcs || [];
  var ITEMS = DATA.items || [];
  var SHOPS = DATA.shops || {};
  var LOOT_SOURCES = DATA.lootSources || [];
  var ZONES = DATA.zones || [];
  var MAPS = DATA.maps || [];
  var STAT_IDS = DATA.statIds || {};
  var META = DATA.meta || {};
  // The world's narrative spine: an ordered list of chapters, each a stretch of the world a
  // player moves through. Zones, maps and quests are all grouped and ordered by it so the site
  // reads as "where do I go next", never as an alphabetical dump (see the technical-writer
  // skill). A zone with no chapter still renders, in an "Elsewhere" group at the end.
  var CHAPTERS = META.chapters || [];

  // ---------------------------------------------------------------- indexes

  var byKey = {};
  function indexBy(list, field) {
    var m = {};
    list.forEach(function (x) { m[x[field]] = x; });
    return m;
  }
  byKey.monster = indexBy(MONSTERS, "name");
  byKey.spell = indexBy(SPELLS, "key");
  byKey.quest = indexBy(QUESTS, "id");
  byKey.npc = indexBy(NPCS, "id");
  byKey.item = indexBy(ITEMS, "key");
  byKey.zone = indexBy(ZONES, "id");
  byKey.map = indexBy(MAPS, "zoneId");

  // loot: item key -> [{monster, chance}]. Sourced from lootSources (every real drop, scanning
  // the full monster registry) rather than MONSTERS (only the ones with their own compendium
  // page) - a pre-existing/legacy monster can still be a real acquisition path for a tracked
  // item even without a documented page of its own.
  var itemDroppedBy = {};
  LOOT_SOURCES.forEach(function (l) {
    itemDroppedBy[l.item] = itemDroppedBy[l.item] || [];
    itemDroppedBy[l.item].push({ monster: l.monster, monsterDisplayName: l.monsterDisplayName, chance: l.chance });
  });

  // item key -> [npcId] selling it
  var itemSoldBy = {};
  Object.keys(SHOPS).forEach(function (npcId) {
    (SHOPS[npcId] || []).forEach(function (itemKey) {
      itemSoldBy[itemKey] = itemSoldBy[itemKey] || [];
      itemSoldBy[itemKey].push(npcId);
    });
  });

  // monster name -> zone id
  var monsterZone = {};
  var itemZone = {};
  var npcZone = {};
  var spellZone = {};
  ZONES.forEach(function (z) {
    (z.monsters || []).forEach(function (n) { monsterZone[n] = z.id; });
    (z.items || []).forEach(function (k) { itemZone[k] = z.id; });
    (z.npcs || []).forEach(function (id) { if (!npcZone[id]) npcZone[id] = []; npcZone[id].push(z.id); });
    (z.spells || []).forEach(function (k) { spellZone[k] = z.id; });
  });

  // quest id -> zone id (via giverNpc's zone, or matching target monster)
  var questZone = {};
  ZONES.forEach(function (z) { (z.quests || []).forEach(function (q) { questZone[q] = z.id; }); });

  // ------------------------------------------------------------------ utils

  function esc(s) {
    if (s === null || s === undefined) return "";
    return String(s).replace(/[&<>"']/g, function (c) {
      return { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c];
    });
  }

  function slug(s) { return encodeURIComponent(s); }

  function fmtNum(n) {
    if (n === null || n === undefined) return "—";
    return Number(n).toLocaleString("en-US");
  }

  function fmtPct(chance) {
    return (chance * 100).toFixed(chance < 0.01 ? 2 : 1) + "%";
  }

  // Element code 0 is unaligned/default (e.g. Tame Beast) - deliberately not mapped, so it
  // shows no element tag instead of being folded into Water.
  var ELEMENT_NAMES = { 1: "fire", 2: "earth", 3: "air", 4: "water", 5: "light", 6: "dark" };
  function elementName(code) { return ELEMENT_NAMES[code] || null; }

  function elPill(el) {
    if (!el) return "";
    return '<span class="el-pill el-' + el + '">' + el.charAt(0).toUpperCase() + el.slice(1) + "</span>";
  }

  // "activated" means "already existed in the codebase, this fork finally placed it in the
  // world" - repo history, meaningless to a player, so it renders as nothing. Only "New" is
  // shown, because that one genuinely tells a returning player something.
  function originTag(origin) {
    if (origin === "new") return '<span class="tag origin-new">New</span>';
    return "";
  }

  // ------------------------------------------------------ chapters (the spine)

  var chapterById = indexBy(CHAPTERS, "id");
  var chapterRank = {};
  CHAPTERS.forEach(function (c, i) { chapterRank[c.id] = i; });

  function chapterOf(zoneId) {
    var z = byKey.zone[zoneId];
    return z && z.chapter ? chapterById[z.chapter] : null;
  }

  // Sorts anything that carries a zone id into reading order: chapter first, then the zone's own
  // position inside that chapter. Zones with no chapter sort last rather than disappearing.
  function zoneReadingOrder(zoneId) {
    var z = byKey.zone[zoneId];
    if (!z) return [999, 999];
    var r = chapterRank[z.chapter];
    return [r === undefined ? 998 : r, z.chapterOrder || 0];
  }

  function byZoneOrder(zoneIdOf) {
    return function (a, b) {
      var x = zoneReadingOrder(zoneIdOf(a));
      var y = zoneReadingOrder(zoneIdOf(b));
      return x[0] - y[0] || x[1] - y[1];
    };
  }

  // Renders an ordered list of chapters, each with its intro paragraph and whatever cards belong
  // to it. `cardsFor(chapterId)` returns the HTML for that chapter; a chapter with nothing in it
  // is skipped. Anything whose chapter is missing lands in a trailing "Elsewhere" group, so a
  // newly added zone can never silently vanish from the page just for lacking a chapter.
  function chapterSections(cardsFor, wrapperClass) {
    var out = CHAPTERS.map(function (c) {
      var cards = cardsFor(c.id);
      if (!cards) return "";
      return (
        '<section class="chapter">' +
        '<div class="chapter-head"><p class="eyebrow">' + esc(c.levels) + "</p>" +
        "<h2>" + esc(c.name) + "</h2>" +
        '<p class="lead">' + esc(c.intro) + "</p></div>" +
        '<div class="' + (wrapperClass || "card-grid") + '">' + cards + "</div></section>"
      );
    }).join("");
    var orphans = cardsFor(null);
    if (orphans) {
      out +=
        '<section class="chapter"><div class="chapter-head"><h2>Elsewhere</h2>' +
        '<p class="lead">Places that don\'t sit on the main road through the world.</p></div>' +
        '<div class="' + (wrapperClass || "card-grid") + '">' + orphans + "</div></section>";
    }
    return out;
  }

  // ------------------------------------------- humanising engine expressions

  // Mana cost, damage and effect parameters are stored as expressions the combat code evaluates
  // ("self.maxmana", "-((1d17+6+self.int/23)*self.fire/target.r_fire)"). They used to be printed
  // raw, so a spell page literally read "Mana cost: self.maxmana". Anything we can state plainly,
  // we state plainly; anything we can't, the caller words around rather than showing the reader
  // an expression they have no way to interpret.
  var PLAIN_EXPRESSIONS = {
    "self.maxmana": "All your mana",
    "self.mana": "All your mana",
  };
  function plainCost(expr) {
    if (expr === null || expr === undefined || expr === "") return "—";
    var s = String(expr).trim();
    if (PLAIN_EXPRESSIONS[s]) return PLAIN_EXPRESSIONS[s];
    if (/^\d+$/.test(s)) return fmtNum(s);
    return null; // an expression we have no plain wording for
  }

  // Turns a dice formula the combat code rolls ("1d40+29", "2d6") into the range a reader can
  // actually use. Returns null for anything more complicated than NdM(+K), so the caller can fall
  // back rather than print a half-parsed lie.
  function diceRange(formula) {
    var m = /^\s*(\d+)d(\d+)\s*([+-]\s*\d+)?\s*$/.exec(String(formula || ""));
    if (!m) return null;
    var count = Number(m[1]);
    var sides = Number(m[2]);
    var offset = m[3] ? Number(m[3].replace(/\s+/g, "")) : 0;
    return { min: count + offset, max: count * sides + offset };
  }

  function damageWording(formula) {
    var r = diceRange(formula);
    return r ? fmtNum(r.min) + "–" + fmtNum(r.max) + " damage a hit" : "Damage varies";
  }

  function rarityOf(item) {
    if (item.key && item.key.indexOf("godsforged_") === 0) return { tier: "godsforged", label: "Godsforged" };
    if (item.unique === true) return { tier: "legendary", label: "Legendary" };
    if (item.key && (item.key.indexOf("ancient_celestial_") === 0 || item.key.indexOf("empyrean_") === 0)) {
      return { tier: "set", label: "Set piece" };
    }
    if (Number(item.price) === 0) return { tier: "rare", label: "Rare (drop only)" };
    return { tier: "common", label: "Common (shop)" };
  }
  var RARITY_RANK = { godsforged: 5, legendary: 4, set: 3, rare: 2, common: 1 };

  function rarityTag(item) {
    var r = rarityOf(item);
    return '<span class="tag tier-' + r.tier + '">' + r.label + "</span>";
  }

  // Mirrors item/ItemBalance.java's archetype(): the item's class comes from its requirements
  // (a bow is always archer gear regardless of its stat requirements).
  function archetypeOf(item) {
    var r = item.requirements || {};
    var str = Number(r.strength || 0), agi = Number(r.agility || 0);
    var intel = Number(r.intelligence || 0), wis = Number(r.wisdom || 0);
    if (item.isBow) return "Archer";
    var physical = Math.max(str, agi), mental = Math.max(intel, wis);
    if (physical === 0 && mental === 0) return "—";
    if (physical >= mental) return str >= agi ? "Warrior" : "Archer";
    if (intel > 0 && wis > 0 && Math.min(intel, wis) >= 0.8 * Math.max(intel, wis)) return "Hybrid mage";
    return intel > wis ? "Intelligence mage" : "Wisdom mage";
  }

  // Groups the BodyPart slots into the three broad shopping categories the Items page tabs by.
  var ARMOR_SLOTS = { HEAD: 1, BELT: 1, LEFT_HAND: 1, RIGHT_HAND: 1, LEGS: 1, FEET: 1, BODY: 1, BACK: 1, SHIELD: 1 };
  var ACCESSORY_SLOTS = { NECK: 1, RING1: 1, RING2: 1, BRACER: 1 };
  function categoryOf(item) {
    if (item.bodyPart === "WEAPON" || item.bodyPart === "WEAPON2") return "Weapons";
    if (ACCESSORY_SLOTS[item.bodyPart]) return "Accessories";
    if (ARMOR_SLOTS[item.bodyPart]) return "Armor";
    return "Other";
  }

  var REQ_ABBR = { endurance: "END", strength: "STR", agility: "AGI", intelligence: "INT", wisdom: "WIS", attack: "ATK" };
  function reqSummary(item) {
    var r = item.requirements || {};
    var parts = Object.keys(r).filter(function (k) { return r[k] > 0; })
      .map(function (k) { return (REQ_ABBR[k] || k) + " " + fmtNum(r[k]); });
    return esc(parts.join(" · ")) || "—";
  }

  function boostSummary(item) {
    var boosts = item.boosts || [];
    if (!boosts.length) return "—";
    return boosts.map(function (b) {
      var meta = STAT_IDS[String(b.statId)] || { label: "Stat #" + b.statId };
      return esc(meta.label) + " +" + esc(b.expression);
    }).join(", ");
  }

  function dmgSummary(item) {
    if (!item.dmgFormula) return "—";
    return esc(item.dmgFormula) + (item.atkDelay ? " (" + esc(item.atkDelay) + "ms)" : "");
  }

  function flagsSummary(item) {
    var flags = [];
    if (item.unlimitedUse === false) flags.push("Limited use");
    if (item.undroppable) flags.push("Undroppable");
    return flags.join(", ") || "—";
  }

  // Where an item actually comes from - named monster(s)/boss(es) it drops from, not a generic
  // "monster drop" label, so the flat items table answers "who drops this" without a click-through.
  // Drop data names the exact monster/boss and is always more specific than a zone tag, so it
  // takes precedence - an item can be both zone-tracked content and a real boss drop, and the
  // whole point of this column is naming who actually drops it (T4C-0028 Codex review).
  function sourceSummary(item) {
    var drops = itemDroppedBy[item.key];
    if (drops && drops.length) {
      return drops.map(function (d) {
        return monsterLink(d.monster, d.monsterDisplayName) + " (" + fmtPct(d.chance) + ")";
      }).join(", ");
    }
    if (itemZone[item.key]) return zoneLink(itemZone[item.key]);
    if (itemSoldBy[item.key]) return itemSoldBy[item.key].map(npcLink).join(", ");
    return "—";
  }
  function sourceSortValue(item) {
    var drops = itemDroppedBy[item.key];
    if (drops && drops.length) return drops.map(function (d) { return d.monsterDisplayName || d.monster; }).sort().join(",");
    if (itemZone[item.key]) return "zone:" + itemZone[item.key];
    if (itemSoldBy[item.key]) return "shop:" + itemSoldBy[item.key].slice().sort().join(",");
    return "";
  }

  function link(route, label, extraClass) {
    return '<a class="' + (extraClass || "") + '" href="#/' + route + '">' + label + "</a>";
  }
  function monsterLink(name, displayName) {
    if (!byKey.monster[name]) return esc(displayName || name);
    return link("monsters/" + slug(name), esc(displayName || name));
  }
  function itemLink(key) {
    var it = byKey.item[key];
    if (!it) return esc(key);
    return link("items/" + slug(key), esc(it.name || key));
  }
  function npcLink(id) {
    var n = byKey.npc[id];
    if (!n) return esc(id);
    return link("npcs/" + slug(id), esc(n.displayName || id));
  }
  function spellLink(key) {
    var s = byKey.spell[key];
    if (!s) return esc(key);
    return link("spells/" + slug(key), esc(s.name || key));
  }
  function questLink(id) {
    var q = byKey.quest[id];
    if (!q) return esc(id);
    return link("quests/" + slug(id), esc(q.title || id));
  }
  function zoneLink(id) {
    var z = byKey.zone[id];
    if (!z) return esc(id);
    return link("zones/" + slug(id), esc(z.name || id));
  }

  function statBar(label, value, max) {
    var pct = Math.max(2, Math.min(100, (value / max) * 100));
    return (
      '<div class="stat-bar"><div class="row"><span>' + esc(label) + "</span><span>" + fmtNum(value) +
      '</span></div><div class="track"><div class="fill" style="width:' + pct + '%"></div></div></div>'
    );
  }

  function panel(title, innerHtml) {
    return '<section class="panel"><h2>' + esc(title) + "</h2>" + innerHtml + "</section>";
  }

  // ----------------------------------------------------------------- router

  var routes = [];
  function route(pattern, handler) {
    var paramNames = [];
    var regex = new RegExp(
      "^" +
        pattern.replace(/:[^/]+/g, function (m) {
          paramNames.push(m.slice(1));
          return "([^/]+)";
        }) +
        "$"
    );
    routes.push({ regex: regex, paramNames: paramNames, handler: handler });
  }

  function renderRoute() {
    var hash = location.hash.replace(/^#\/?/, "");
    hash = hash || "";
    for (var i = 0; i < routes.length; i++) {
      var m = routes[i].regex.exec(hash);
      if (m) {
        var params = {};
        routes[i].paramNames.forEach(function (name, idx) { params[name] = decodeURIComponent(m[idx + 1]); });
        document.getElementById("app").innerHTML = routes[i].handler(params) || "";
        highlightNav(hash);
        window.scrollTo(0, 0);
        return;
      }
    }
    document.getElementById("app").innerHTML = renderHome();
    highlightNav("");
  }

  function highlightNav(hash) {
    var top = "/" + hash.split("/")[0];
    document.querySelectorAll("#mainnav a").forEach(function (a) {
      var href = a.getAttribute("href").replace("#", "");
      a.classList.toggle("active", href !== "/" && top.indexOf(href) === 0);
    });
  }

  // ------------------------------------------------------------------- home

  function zoneCard(z, truncateAt) {
    var summary = truncateAt && z.summary.length > truncateAt
      ? z.summary.slice(0, truncateAt).replace(/\s+\S*$/, "") + "…"
      : z.summary;
    return (
      '<a class="card" href="#/zones/' + slug(z.id) + '">' +
      "<h3>" + esc(z.name) + "</h3>" +
      '<p class="card-meta">Levels ' + esc(z.levelRange) + " · " + esc(z.biome) + "</p>" +
      "<p>" + esc(summary) + "</p></a>"
    );
  }

  function zoneCardsIn(chapterId, truncateAt) {
    return ZONES.filter(function (z) {
      return chapterId === null ? !chapterById[z.chapter] : z.chapter === chapterId;
    }).sort(function (a, b) { return (a.chapterOrder || 0) - (b.chapterOrder || 0); })
      .map(function (z) { return zoneCard(z, truncateAt); }).join("");
  }

  function renderHome() {
    var newMonsters = MONSTERS.filter(function (m) { return m.origin === "new"; }).length;
    var newSpells = SPELLS.filter(function (s) { return s.isNew; }).length;
    var first = ZONES[0];

    return (
      '<div class="hero">' +
      '<p class="eyebrow">A player\'s guide to the world</p>' +
      "<h1>Everywhere you can go, and what is waiting there</h1>" +
      "<p>The world laid out in the order you'll travel it — from the coast road outside your " +
      "first town to the peak at the end of everything. Every place, every creature in it, " +
      "every piece of gear it drops and every quest that opens the next door, with the numbers " +
      "taken straight from the game itself.</p>" +
      '<div class="hero-actions">' +
      (first ? '<a class="btn primary" href="#/zones/' + slug(first.id) + '">Start at the beginning</a>' : "") +
      '<a class="btn" href="#/zones">The whole road</a>' +
      '<a class="btn" href="#/quests">Quests</a>' +
      "</div></div>" +
      '<div class="stat-grid">' +
      statTile(ZONES.length, "Places to go") +
      statTile(newMonsters, "New creatures") +
      statTile(ITEMS.length, "Pieces of gear") +
      statTile(newSpells, "New spells") +
      statTile(NPCS.length, "People to talk to") +
      statTile(QUESTS.length, "Quests") +
      "</div>" +
      chapterSections(function (id) { return zoneCardsIn(id, 150); })
    );
  }

  function statTile(n, label) {
    return '<div class="stat-tile"><strong>' + fmtNum(n) + "</strong><span>" + esc(label) + "</span></div>";
  }

  route("", renderHome);

  // ------------------------------------------------------------------ zones

  route("zones", function () {
    return (
      '<div class="page-header"><p class="eyebrow">Zones</p><h1>The road through the world</h1>' +
      '<p class="lead">Read this top to bottom and you have the whole journey, in the order a ' +
      "character grows into it. Each place says who holds it, why they turned, and where you go " +
      "next.</p></div>" +
      chapterSections(function (id) { return zoneCardsIn(id); })
    );
  });

  route("zones/:id", function (params) {
    var z = byKey.zone[params.id];
    if (!z) return notFound("Zone");
    var monsters = (z.monsters || []).map(function (n) {
      var m = byKey.monster[n];
      return (
        '<div class="loot-row"><span>' + monsterLink(n) + (m ? " · level " + m.level : "") + "</span>" +
        (m ? originTag(m.origin) : "") + "</div>"
      );
    }).join("") || '<p class="lead">Nothing hostile that we have a page for.</p>';

    var items = (z.items || []).map(function (k) {
      var it = byKey.item[k];
      return '<div class="loot-row"><span>' + itemLink(k) + "</span>" + (it ? rarityTag(it) : "") + "</div>";
    }).join("") || '<p class="lead">Nothing here drops gear of its own.</p>';

    var spells = (z.spells || []).map(function (k) { return "<li>" + spellLink(k) + "</li>"; }).join("") ||
      "<li>Nobody teaches spells here.</li>";

    var npcs = (z.npcs || []).map(function (id) { return "<li>" + npcLink(id) + "</li>"; }).join("") ||
      "<li>Nobody friendly lives here.</li>";

    var quests = (z.quests || []).map(function (id) {
      var q = byKey.quest[id];
      return '<div class="loot-row"><span>' + questLink(id) + "</span><span>" + (q ? fmtNum(q.rewardXp) + " XP" : "") + "</span></div>";
    }).join("") || '<p class="lead">Nobody is handing out work here.</p>';

    var mapHtml = zoneMap(z);
    var ch = chapterOf(z.id);
    var next = z.nextZoneId ? byKey.zone[z.nextZoneId] : null;
    var prev = null;
    ZONES.forEach(function (o) { if (o.nextZoneId === z.id) prev = o; });

    // Where this place sits in the journey, spelled out rather than implied by ordering - it's
    // the single question a reader lands on a zone page with.
    var journey =
      '<div class="journey">' +
      (prev ? '<span class="journey-step"><span class="label">Coming from</span>' + zoneLink(prev.id) + "</span>" : "") +
      (next ? '<span class="journey-step"><span class="label">Next</span>' + zoneLink(next.id) + "</span>"
            : '<span class="journey-step"><span class="label">Next</span>Nothing. This is the end of the road.</span>') +
      "</div>";

    return (
      breadcrumb([["Zones", "zones"], [z.name, null]]) +
      '<div class="detail-head"><div><p class="eyebrow">' +
      (ch ? esc(ch.name) + " · " : "") + esc(z.biome) + '</p><h1>' + esc(z.name) + "</h1>" +
      '<div class="tags"><span class="tag plain">Levels ' + esc(z.levelRange) + "</span>" +
      (z.settlement ? '<span class="tag origin-new">Fast travel: ' + esc(z.settlement) + "</span>" : "") +
      "</div></div></div>" +
      panel("The place", '<p class="lead">' + esc(z.summary) + "</p>" + journey + mapHtml) +
      panel("What lives here", monsters) +
      panel("What it drops", items) +
      panel("Who needs your help", quests) +
      panel("Spells taught here", '<ul class="list-plain">' + spells + "</ul>") +
      panel("People", '<ul class="list-plain">' + npcs + "</ul>")
    );
  });

  function schematicDot(cx, cy, radius, caption) {
    var worldSize = 3200; // approximate worldmap extent used purely for a schematic dot placement
    var left = Math.min(94, Math.max(6, (cx / worldSize) * 100));
    var top = Math.min(94, Math.max(6, (cy / worldSize) * 100));
    var rpx = Math.min(45, (radius / worldSize) * 100 * 3);
    return (
      '<div class="zone-map-strip"><div class="minimap">' +
      '<div class="radius" style="left:' + left + "%;top:" + top + "%;width:" + rpx * 2 + "%;height:" + rpx * 2 + '%"></div>' +
      '<div class="dot" style="left:' + left + "%;top:" + top + '%"></div>' +
      '<span class="axis-label" style="left:6px;top:4px">roughly here in the world</span>' +
      "</div><div><p class=\"lead\">" + caption + "</p></div></div>"
    );
  }

  function zoneMap(z, caption) {
    if (!z.worldmapCenter) return "";
    var c = z.worldmapCenter;
    caption = caption || "Where this place sits, and how far it reaches. Quest kills only count " +
      "inside this circle, so this is the ground to work rather than wherever the creatures wander to.";
    var m = byKey.map[z.id];
    if (m) {
      return (
        '<div class="zone-map-strip">' +
        '<a class="map-thumb-link" href="#/maps/' + slug(z.id) + '">' +
        '<div class="map-thumb" style="background-image:url(data/' + m.image + ')"></div></a>' +
        '<div><p class="lead">' + caption + '</p>' +
        '<p>' + link("maps/" + slug(z.id), "Open the full map →") + "</p></div></div>"
      );
    }
    return schematicDot(c.x, c.y, c.radius, caption);
  }

  // Every quest carries its own centre/radius even when it isn't tied to a hand-authored zone
  // (older pre-zone quests, or a turn-in with radius 1 marking just the giver NPC's spot) - fall
  // back to a schematic dot from the quest's own coordinates so a location is always shown, not
  // just for quests a zone happens to list. The caption differs from the zone page's generic one
  // above, since a pure turn-in quest has no objective area to describe.
  function questMap(q, zone) {
    var caption = q.requiredKills > 0
      ? "Kills only count inside this circle. Work it rather than following anything that wanders out."
      : "Roughly where to find " + npcPlain(q.giverNpc) + ".";
    if (zone && byKey.zone[zone]) return zoneMap(byKey.zone[zone], caption);
    return schematicDot(q.areaCenterX, q.areaCenterY, q.areaRadiusTiles, caption);
  }

  // --------------------------------------------------------------------- maps

  route("maps", function () {
    function mapCardsIn(chapterId) {
      return MAPS.filter(function (m) {
        var z = byKey.zone[m.zoneId];
        if (!z) return false;
        return chapterId === null ? !chapterById[z.chapter] : z.chapter === chapterId;
      }).sort(byZoneOrder(function (m) { return m.zoneId; })).map(function (m) {
        var z = byKey.zone[m.zoneId];
        return (
          '<a class="card map-card" href="#/maps/' + slug(m.zoneId) + '">' +
          '<div class="map-thumb" style="background-image:url(data/' + m.image + ')"></div>' +
          "<h3>" + esc(z.name) + "</h3>" +
          '<p class="card-meta">Levels ' + esc(z.levelRange) + " · " + esc(z.biome) + "</p></a>"
        );
      }).join("");
    }
    return (
      '<div class="page-header"><p class="eyebrow">Maps</p><h1>Maps, in travelling order</h1>' +
      '<p class="lead">A map of every place on the road, drawn from the ground the game actually ' +
      "puts under your feet, with every creature and every person pinned where you will really " +
      "find them. Buildings and scenery are left off, so read the edges loosely — but the pins " +
      "are exact.</p></div>" +
      chapterSections(mapCardsIn)
    );
  });

  route("maps/:id", function (params) {
    var m = byKey.map[params.id];
    var z = byKey.zone[params.id];
    if (!m || !z) return notFound("Map");

    var tilesWide = m.imageWidth / m.pxPerTile;
    var tilesHigh = m.imageHeight / m.pxPerTile;
    function pct(worldX, worldY) {
      return {
        left: Math.min(100, Math.max(0, ((worldX - m.originX) / tilesWide) * 100)),
        top: Math.min(100, Math.max(0, ((worldY - m.originY) / tilesHigh) * 100))
      };
    }
    function pin(kind, x, y, linkHtml, title) {
      var p = pct(x, y);
      return (
        '<div class="map-pin pin-' + kind + '" style="left:' + p.left + "%;top:" + p.top + '%">' +
        '<span class="pin-dot" title="' + esc(title) + '"></span>' +
        '<span class="pin-label">' + linkHtml + "</span></div>"
      );
    }

    var pins = "";
    (m.namedLocations || []).forEach(function (loc) {
      var p = pct(loc.x, loc.y);
      pins += '<div class="map-area-label" style="left:' + p.left + "%;top:" + p.top + '%">' + esc(loc.name) + "</div>";
    });
    (m.monsters || []).filter(function (mo) { return mo.tier === "trash"; }).forEach(function (mo) {
      pins += pin("trash", mo.x, mo.y, monsterLink(mo.name, mo.displayName), mo.displayName);
    });
    (m.monsters || []).filter(function (mo) { return mo.tier === "boss"; }).forEach(function (mo) {
      pins += pin("boss", mo.x, mo.y, monsterLink(mo.name, mo.displayName), mo.displayName);
    });
    (m.npcs || []).forEach(function (n) {
      pins += pin("npc", n.x, n.y, npcLink(n.id), n.displayName);
    });

    return (
      breadcrumb([["Maps", "maps"], [z.name, null]]) +
      '<div class="detail-head"><div><p class="eyebrow">' +
      (chapterOf(z.id) ? esc(chapterOf(z.id).name) + " · " : "") + esc(z.biome) + '</p><h1>' + esc(z.name) + "</h1>" +
      '<div class="tags"><span class="tag plain">Levels ' + esc(z.levelRange) + "</span>" +
      '<span class="tag plain">' + zoneLink(z.id) + "</span></div></div></div>" +
      panel(
        "The lay of the land",
        '<p class="lead">Drawn from the ground the game really puts under your feet. Buildings ' +
          "and scenery are left off, so the edges are approximate — but every pin is exactly " +
          "where you will find it. " +
          '<span class="pin-legend"><span class="pin-dot pin-boss"></span> named and dangerous</span> ' +
          '<span class="pin-legend"><span class="pin-dot pin-npc"></span> someone to talk to</span> ' +
          '<span class="pin-legend"><span class="pin-dot pin-trash"></span> common creature (hover for its name)</span></p>' +
          '<div class="map-frame" style="aspect-ratio:' + m.imageWidth + "/" + m.imageHeight + '">' +
          '<img class="map-image" src="data/' + m.image + '" alt="' + esc(z.name) + ' map" loading="lazy">' +
          pins +
          "</div>"
      )
    );
  });

  // --------------------------------------------------------------- monsters

  route("monsters", function () {
    return listPage({
      title: "Monster roster",
      eyebrow: "Monsters",
      lead: "Everything that will try to kill you, sorted by how hard it hits back. Search by name, or sort by level to find something your size.",
      rows: MONSTERS,
      columns: [
        { key: "displayName", label: "Name", render: function (m) { return monsterLink(m.name); } },
        { key: "level", label: "Level", numeric: true },
        { key: "health", label: "HP", numeric: true },
        { key: "hitDamageMax", label: "Hardest hit", numeric: true },
        { key: "xpOnDeath", label: "XP", numeric: true },
        { key: "aggro", label: "Aggression", numeric: true },
        { key: "zone", label: "Found in", render: function (m) { return monsterZone[m.name] ? zoneLink(monsterZone[m.name]) : "—"; }, sortValue: function (m) { var o = zoneReadingOrder(monsterZone[m.name]); return o[0] * 100 + o[1]; } },
      ],
      searchFields: ["displayName", "name"],
      filters: [],
      defaultSort: "level",
    });
  });

  route("monsters/:name", function (params) {
    var m = byKey.monster[params.name];
    if (!m) return notFound("Monster");
    var zone = monsterZone[m.name];

    var statsHtml = Object.keys(m.stats).map(function (k) { return statBar(k.toUpperCase(), m.stats[k], 1000); }).join("");
    var resistLabels = { air: "Air", fire: "Fire", water: "Water", earth: "Earth", light: "Light", dark: "Dark" };
    var resistHtml = Object.keys(m.resists).map(function (k) {
      return '<div class="kv"><span class="el-' + k + '">' + resistLabels[k] + " resist</span><strong>" + fmtNum(m.resists[k]) + "</strong></div>";
    }).join("");

    var lootHtml = (m.loot || []).length
      ? m.loot.map(function (l) {
          return (
            '<div class="loot-row"><span>' + itemLink(l.item) + '</span><span style="display:flex;align-items:center;gap:8px">' +
            fmtPct(l.chance) + '<span class="chance-bar"><i style="width:' + Math.min(100, l.chance * 100 * 4) + '%"></i></span></span></div>'
          );
        }).join("")
      : '<p class="lead">Drops nothing worth carrying home.</p>';

    // The raw damage formula ("1d40+29") means nothing to most readers, so the row leads with
    // what it does - in close or at range, and roughly how hard - and keeps the formula in the
    // for-the-curious block at the bottom of the panel.
    var attackHtml = (m.attacks || []).length
      ? m.attacks.map(function (a) {
          var range = a.isSpell
            ? "Casts at you from " + a.rangeMinTiles + "–" + a.rangeMaxTiles + " paces away"
            : "Fights you up close";
          return (
            '<div class="attack-row"><span>' + range + "</span><span>" +
            esc(damageWording(a.formula)) + "</span></div>"
          );
        }).join("") +
        '<details class="curious"><summary>For the curious: the exact damage rolls</summary>' +
        '<div class="kv-grid">' +
        m.attacks.map(function (a) { return kv(a.isSpell ? "Spell" : "Melee", "<code>" + esc(a.formula) + "</code>"); }).join("") +
        "</div></details>"
      : '<p class="lead">Does not fight back.</p>';

    return (
      breadcrumb([["Monsters", "monsters"], [m.displayName, null]]) +
      '<div class="detail-head"><div><p class="eyebrow">Monster · Level ' + m.level +
      (zone ? " · " + esc((byKey.zone[zone] || {}).name || "") : "") + '</p>' +
      "<h1>" + esc(m.displayName) + "</h1>" +
      '<div class="tags">' + originTag(m.origin) +
      (zone ? '<span class="tag plain">' + zoneLink(zone) + "</span>" : "") +
      (m.tameable ? '<span class="tag origin-new">Can be tamed up to level ' + m.tameMaxLevel + "</span>" : "") +
      "</div></div></div>" +
      panel("What you're up against", '<div class="kv-grid">' +
        kv("Health", fmtNum(m.health)) + kv("Mana", fmtNum(m.mana)) +
        kv("Damage a hit", fmtNum(m.hitDamageMin) + "–" + fmtNum(m.hitDamageMax)) +
        kv("Dodge", fmtNum(m.dodge)) + kv("Armor", fmtNum(m.acMin) + "–" + fmtNum(m.acMax)) +
        kv("How readily it attacks", fmtNum(m.aggro)) + kv("Speed", fmtNum(m.speed)) +
        kv("XP per hit you land", fmtNum(m.xpPerHit)) + kv("XP for the kill", fmtNum(m.xpOnDeath)) +
        kv("Gold it carries", fmtNum(m.goldMin) + "–" + fmtNum(m.goldMax)) +
        kv("Comes back after", (m.respawnTimeMs / 1000).toFixed(0) + " seconds") +
        "</div>") +
      panel("Attributes", '<div class="stat-bars">' + statsHtml + "</div>") +
      panel("How well it shrugs off each element",
        '<p class="lead">Higher means your spells of that element hurt it less. 100 is ordinary; ' +
        "anything far above that is a wall, anything far below it is a weakness worth exploiting.</p>" +
        '<div class="kv-grid">' + resistHtml + "</div>") +
      panel("How it fights", attackHtml) +
      panel("What it drops", lootHtml)
    );
  });

  function kv(label, value) {
    return '<div class="kv"><span>' + esc(label) + "</span><strong>" + value + "</strong></div>";
  }

  // ------------------------------------------------------------------ items

  route("items", function () {
    return listPage({
      title: "Items",
      eyebrow: "Items",
      lead: "Every weapon, every piece of armor, every ring and amulet. The table has the numbers; click any row to see who drops it or sells it.",
      rows: ITEMS,
      tabs: [
        { key: "all", label: "All" },
        { key: "weapons", label: "Weapons", filter: function (it) { return categoryOf(it) === "Weapons"; } },
        { key: "armor", label: "Armor", filter: function (it) { return categoryOf(it) === "Armor"; } },
        { key: "accessories", label: "Accessories", filter: function (it) { return categoryOf(it) === "Accessories"; } },
      ],
      columns: [
        { key: "name", label: "Name", render: function (it) { return itemLink(it.key); } },
        { key: "bodyPart", label: "Slot" },
        { key: "class", label: "Suits", render: function (it) { return esc(archetypeOf(it)); }, sortValue: archetypeOf },
        { key: "requirements", label: "You'll need", render: reqSummary, sortValue: reqSummary },
        { key: "armorClass", label: "Armor", numeric: true, render: function (it) { return it.armorClass ? fmtNum(it.armorClass) : "—"; } },
        { key: "damage", label: "Damage", render: dmgSummary, sortValue: dmgSummary },
        { key: "boosts", label: "What it grants", render: boostSummary, sortValue: boostSummary },
        { key: "flags", label: "Notes", render: flagsSummary, sortValue: flagsSummary },
        { key: "price", label: "Price", numeric: true, render: function (it) { return it.price ? fmtNum(it.price) : "—"; } },
        { key: "rarity", label: "Rarity", render: function (it) { return rarityTag(it); }, sortValue: function (it) { return RARITY_RANK[rarityOf(it).tier] || 0; } },
        { key: "zone", label: "Where from", render: sourceSummary, sortValue: sourceSortValue },
      ],
      searchFields: ["name", "key"],
      filters: [
        { label: "Worn on", field: "bodyPart", options: uniq(ITEMS.map(function (i) { return i.bodyPart; })) },
        { label: "Suits", field: "__class", options: uniq(ITEMS.map(archetypeOf)), computed: archetypeOf },
        { label: "Rarity", field: "__rarity", options: ["godsforged", "legendary", "set", "rare", "common"], computed: function (it) { return rarityOf(it).tier; } },
      ],
      defaultSort: "name",
    });
  });

  route("items/:key", function (params) {
    var it = byKey.item[params.key];
    if (!it) return notFound("Item");
    var r = rarityOf(it);
    var zone = itemZone[it.key];

    var reqs = it.requirements || {};
    var reqHtml = Object.keys(reqs).filter(function (k) { return reqs[k] > 0; }).map(function (k) {
      return kv(k.charAt(0).toUpperCase() + k.slice(1), fmtNum(reqs[k]));
    }).join("") || '<p class="lead">Anyone can wear this.</p>';

    var boostHtml = (it.boosts || []).length
      ? it.boosts.map(function (b) {
          var meta = STAT_IDS[String(b.statId)] || { label: "An unnamed bonus", group: "other" };
          var elClass = meta.element ? " el-" + meta.element : "";
          return (
            '<div class="boost-row"><span class="' + elClass.trim() + '">' + esc(meta.label) + '</span><span class="value">+' +
            esc(b.expression) + "</span></div>"
          );
        }).join("")
      : '<p class="lead">Nothing beyond what it is: no bonuses attached.</p>';

    var dropSources = (itemDroppedBy[it.key] || []).map(function (d) {
      return '<div class="loot-row"><span>Dropped by ' + monsterLink(d.monster, d.monsterDisplayName) + "</span><span>" + fmtPct(d.chance) + " of the time</span></div>";
    }).join("");
    // Quest rewards are a real acquisition path the page could never show before, because the
    // exporter didn't emit rewardItemKey - for the Godsforged pieces it is the *only* path.
    var questSources = QUESTS.filter(function (q) { return q.rewardItemKey === it.key; })
      .map(function (q) { return '<div class="loot-row"><span>Given for ' + questLink(q.id) + "</span><span>on turn-in</span></div>"; }).join("");
    var shopSources = (itemSoldBy[it.key] || []).map(function (id) { return "<li>Sold by " + npcLink(id) + "</li>"; }).join("");
    var sourcesHtml = (dropSources || shopSources || questSources)
      ? questSources + dropSources + (shopSources ? '<ul class="list-plain">' + shopSources + "</ul>" : "")
      : '<p class="lead">Nothing sells this and nothing is known to drop it — it most likely comes from talking to the right person.</p>';

    return (
      breadcrumb([["Items", "items"], [it.name, null]]) +
      '<div class="detail-head"><div><p class="eyebrow">' + esc(it.bodyPart || "Item") + (it.isBow ? " · Bow" : "") + '</p>' +
      "<h1>" + esc(it.name) + "</h1>" +
      '<div class="tags">' + rarityTag(it) +
      (zone ? '<span class="tag plain">' + zoneLink(zone) + "</span>" : "") +
      (it.unlimitedUse === false ? '<span class="tag plain">Wears out with use</span>' : "") +
      (it.undroppable ? '<span class="tag plain">Can\'t be dropped or traded</span>' : "") +
      "</div></div></div>" +
      panel("The piece itself", '<div class="kv-grid">' +
        kv("Price", it.price ? fmtNum(it.price) + " gold" : "Not for sale") +
        kv("Weight", fmtNum(it.weight)) +
        kv("Armor", it.armorClass ? fmtNum(it.armorClass) : "—") +
        (it.dmgFormula ? kv("Damage", esc(damageWording(it.dmgFormula))) : "") +
        (it.atkDelay ? kv("Swing speed", (it.atkDelay / 1000).toFixed(1) + " seconds between blows") : "") +
        ((it.useEffects || []).length ? kv("When you use it", esc(it.useEffects.join("; "))) : "") +
        "</div>" +
        (it.dmgFormula
          ? '<details class="curious"><summary>For the curious: the exact damage roll</summary>' +
            "<p><code>" + esc(it.dmgFormula) + "</code></p></details>"
          : "")) +
      panel("What you need to wear it", '<div class="kv-grid">' + reqHtml + "</div>") +
      panel("What it grants you", boostHtml) +
      panel("Where to get it", sourcesHtml)
    );
  });

  // ----------------------------------------------------------------- spells

  route("spells", function () {
    return listPage({
      title: "Spells",
      eyebrow: "Spells",
      lead: "Every spell a character can learn, in the order you grow into them. Sort by element to find your school, or by level to see what's next.",
      rows: SPELLS,
      columns: [
        { key: "name", label: "Name", render: function (s) { return spellLink(s.key); } },
        { key: "element", label: "Element", render: function (s) { return elPill(elementName(s.element)); } },
        { key: "minLevel", label: "Learn at level", numeric: true },
        // Not `numeric`: a handful of spells cost an expression ("self.maxmana"), which must read
        // as words, and would sort as NaN. Those sort to the end instead.
        { key: "manaCost", label: "Mana", render: function (s) { var p = plainCost(s.manaCost); return p === null ? "Varies" : esc(p); },
          sortValue: function (s) { return /^\d+$/.test(String(s.manaCost)) ? Number(s.manaCost) : 1e9; } },
        { key: "isNew", label: "", render: function (s) { return s.isNew ? '<span class="tag origin-new">New</span>' : ""; } },
      ],
      searchFields: ["name", "description"],
      filters: [
        { label: "Element", field: "__element", options: ["fire", "water", "earth", "air", "light", "dark"], computed: function (s) { return elementName(s.element); } },
        { label: "New only", field: "isNew", options: [true] },
      ],
      defaultSort: "minLevel",
    });
  });

  route("spells/:key", function (params) {
    var s = byKey.spell[params.key];
    if (!s) return notFound("Spell");
    var zone = spellZone[s.key];
    var el = elementName(s.element);

    // The effect rows are raw engine data - a numeric effect type and a list of unlabelled
    // expressions ("Effect type 9 | 10239, OnTimer, 100, 10000"). There is nothing a reader can
    // do with that, so it lives behind a disclosure instead of being the last thing on the page.
    var effectsHtml = (s.effects || []).length
      ? '<details class="curious"><summary>For the curious: the raw effect data behind this spell</summary>' +
        s.effects.map(function (e) {
          var params2 = (e.parameters || []).map(function (p) { return esc(p.expression); })
            .filter(function (x) { return x; }).join(", ");
          return '<div class="loot-row"><span>Effect ' + e.effectType + "</span><span><code>" + params2 + "</code></span></div>";
        }).join("") + "</details>"
      : "";

    var mana = plainCost(s.manaCost);
    var cost = mana === null ? "Varies with your character" : (mana === "—" ? "Free" : mana);
    var landChance = esc(String(s.successRate).replace(/^(\d+)$/, "$1%"));

    return (
      breadcrumb([["Spells", "spells"], [s.name, null]]) +
      '<div class="detail-head"><div><p class="eyebrow">' + (el ? el.charAt(0).toUpperCase() + el.slice(1) + " spell" : "Spell") + '</p>' +
      "<h1>" + esc(s.name) + "</h1>" +
      '<div class="tags">' + (s.isNew ? '<span class="tag origin-new">New</span>' : "") +
      (zone ? '<span class="tag plain">' + zoneLink(zone) + "</span>" : "") +
      (s.isAttack ? '<span class="tag tier-legendary">Does harm</span>' : '<span class="tag tier-uncommon">Helps you or your allies</span>') +
      (s.pvp === false ? '<span class="tag plain">Won\'t work on other players</span>' : "") +
      "</div></div></div>" +
      panel("What it does", '<p class="lead">' + esc(s.description || "No description written for this one yet.") + "</p>") +
      panel("What it takes to cast", '<div class="kv-grid">' +
        kv("Level", s.minLevel) + kv("Intelligence", s.minInt) + kv("Wisdom", s.minWis) +
        kv("Mana each cast", esc(cost)) +
        kv("Wait between casts", s.cooldownSeconds ? s.cooldownSeconds + " seconds" : "None") +
        kv("Needs a clear line to the target", s.lineOfSight ? "Yes" : "No") +
        kv("Costs to learn", s.price ? fmtNum(s.price) + " gold" : "Nothing") +
        "</div>") +
      (s.isAttack ? panel("How hard it hits", (s.damageAtReference ?
        '<div class="kv-grid">' +
          kv("Damage", fmtNum(s.damageAtReference.min) + "–" + fmtNum(s.damageAtReference.max)) +
          kv("Resisted by", s.attackType === 1 ? "The target's body" : "The target's mind") +
          kv("Chance to land", landChance) +
        "</div>" +
        '<p class="lead">That range is what a character who has only just met this spell\'s ' +
        "requirements would do to an ordinary, unresisting target. A trained caster does more, " +
        "and a target that resists this element takes less — treat it as a way to compare " +
        "spells against each other, not a promise.</p>" +
        '<details class="curious"><summary>For the curious: the exact damage roll</summary>' +
        "<p><code>" + esc(s.damageAtReference.formula) + "</code></p></details>"
        : '<div class="kv-grid">' +
          kv("Resisted by", s.attackType === 1 ? "The target's body" : "The target's mind") +
          kv("Chance to land", landChance) + "</div>")
      ) : "") +
      effectsHtml
    );
  });

  // ------------------------------------------------------------------- npcs

  route("npcs", function () {
    return listPage({
      title: "NPCs",
      eyebrow: "NPCs",
      lead: "The people worth stopping for: who hands out work, who teaches, who sells, and where to find each of them.",
      rows: NPCS,
      columns: [
        { key: "displayName", label: "Name", render: function (n) { return npcLink(n.id); } },
        { key: "zone", label: "Where", render: function (n) { return npcZone[n.id] ? npcZone[n.id].map(zoneLink).join(", ") : "—"; }, sortValue: function (n) { var o = zoneReadingOrder((npcZone[n.id] || [])[0]); return o[0] * 100 + o[1]; } },
        { key: "topics", label: "Things to ask about", numeric: true, render: function (n) { return (n.topics || []).length; }, sortValue: function (n) { return (n.topics || []).length; } },
        { key: "shop", label: "Sells", render: function (n) { return SHOPS[n.id] ? SHOPS[n.id].length + " things" : "—"; }, sortValue: function (n) { return (SHOPS[n.id] || []).length; } },
      ],
      searchFields: ["displayName", "id"],
      filters: [],
      defaultSort: "displayName",
    });
  });

  route("npcs/:id", function (params) {
    var n = byKey.npc[params.id];
    if (!n) return notFound("NPC");
    var zones = npcZone[n.id] || [];

    var topicsHtml = (n.topics || []).map(function (t) {
      return (
        '<div class="dialogue-topic"><div class="keywords">' + t.keywords.map(esc).join(" · ") + "</div>" +
        '<div class="response">“' + esc(t.response) + "”</div>" +
        (t.actions.length ? t.actions.map(function (a) { return '<span class="action-tag">' + esc(a) + "</span>"; }).join(" ") : "") +
        "</div>"
      );
    }).join("") || '<p class="lead">Nothing to say beyond a greeting.</p>';

    var shopItems = (SHOPS[n.id] || []).map(function (k) { return "<li>" + itemLink(k) + "</li>"; }).join("");
    var combat = n.combatProfile;

    return (
      breadcrumb([["NPCs", "npcs"], [n.displayName, null]]) +
      '<div class="detail-head"><div><p class="eyebrow">NPC</p><h1>' + esc(n.displayName) + "</h1>" +
      '<div class="tags">' + originTag(n.origin) +
      zones.map(function (z) { return '<span class="tag plain">' + zoneLink(z) + "</span>"; }).join("") +
      "</div></div></div>" +
      (n.welcomeText ? panel("How they greet you", '<p class="lead">“' + esc(n.welcomeText) + '”</p>') : "") +
      panel("What you can ask them about",
        '<p class="lead">Say any of the words on the left and you get the answer beside it.</p>' +
        topicsHtml) +
      (shopItems ? panel("What they sell", '<ul class="list-plain">' + shopItems + "</ul>") : "") +
      (combat ? panel("If it comes to a fight", '<div class="kv-grid">' +
        kv("Level", combat.level) + kv("Health", fmtNum(combat.maxHp)) +
        kv("Strength", combat.strength) + kv("Endurance", combat.endurance) +
        kv("Dexterity", combat.dexterity) + kv("Armor", combat.armorClass) +
        kv("Accuracy", combat.attackSkill) + kv("Dodge", combat.dodge) +
        kv("Damage", esc(damageWording(combat.damageFormula))) + "</div>") : "")
    );
  });

  // ----------------------------------------------------------------- quests

  route("quests", function () {
    function questCardsIn(chapterId) {
      return QUESTS.filter(function (q) {
        var z = byKey.zone[questZone[q.id]];
        return chapterId === null ? !(z && chapterById[z.chapter]) : (z && z.chapter === chapterId);
      }).sort(byZoneOrder(function (q) { return questZone[q.id]; })).map(function (q) {
        return (
          '<a class="card" href="#/quests/' + slug(q.id) + '">' +
          "<h3>" + esc(q.title) + "</h3>" +
          '<p class="card-meta">' + npcPlain(q.giverNpc) + "</p>" +
          "<p>" + (q.requiredKills > 0
            ? "Kill " + q.requiredKills + " " + esc(q.targetMonster) + (q.requiredKills > 1 ? "s" : "")
            : "Bring what they ask for") +
          (q.requiredItemKey ? ", and bring " + itemPlain(q.requiredItemKey) : "") + ".</p>" +
          '<div class="tags"><span class="tag tier-legendary">' + fmtNum(q.rewardGold) + " gold</span>" +
          '<span class="tag tier-uncommon">' + fmtNum(q.rewardXp) + " XP</span>" +
          (q.rewardItemKey ? '<span class="tag tier-godsforged">' + itemPlain(q.rewardItemKey) + "</span>" : "") +
          (q.minLevel > 0 ? '<span class="tag plain">Level ' + q.minLevel + " and up</span>" : "") +
          "</div></a>"
        );
      }).join("");
    }
    return (
      '<div class="page-header"><p class="eyebrow">Quests</p><h1>Everyone who needs something doing</h1>' +
      '<p class="lead">Laid out along the same road as everything else, so you can read down the ' +
      "page and see what to take on next. Each one tells you who to talk to, exactly what they " +
      "want, what you have to hand over, and what you walk away with.</p></div>" +
      chapterSections(questCardsIn)
    );
  });

  function npcPlain(id) {
    var n = byKey.npc[id];
    return n ? esc(n.displayName) : esc(id);
  }

  function itemPlain(key) {
    var it = byKey.item[key];
    return it ? esc(it.name) : esc(key);
  }

  route("quests/:id", function (params) {
    var q = byKey.quest[params.id];
    if (!q) return notFound("Quest");
    var zone = questZone[q.id];
    var mapHtml = questMap(q, zone);

    // Two lists that are easy to confuse, and were confused for a long time: what you hand OVER
    // (requiredItemKey / alsoRequiresItemKey, consumed by QuestService.consumeRequiredItem) and
    // what you are GIVEN (rewardItemKey, granted by QuestService.complete). Keeping them in
    // separate blocks, each labelled with its verb, is the whole point - the walkthrough text
    // used to describe the first as the second.
    var bring = [];
    if (q.requiredItemKey) {
      bring.push((q.requiredItemQty > 1 ? q.requiredItemQty + " × " : "") + itemLink(q.requiredItemKey));
    }
    if (q.alsoRequiresItemKey) bring.push(itemLink(q.alsoRequiresItemKey));

    var bringHtml = bring.length
      ? '<p class="lead"><strong>Bring with you:</strong> ' + bring.join(", ") + ". " +
        (bring.length > 1 || q.requiredItemQty > 1
          ? "These are handed over and gone — you do not keep them."
          : "It is handed over and gone — you do not keep it.") + "</p>"
      : "";

    return (
      breadcrumb([["Quests", "quests"], [q.title, null]]) +
      '<div class="detail-head"><div><p class="eyebrow">Quest' + (zone ? " · " + esc((byKey.zone[zone] || {}).name || "") : "") + '</p>' +
      "<h1>" + esc(q.title) + "</h1>" +
      '<div class="tags"><span class="tag plain">Given by ' + npcLink(q.giverNpc) + "</span>" +
      (q.minLevel > 0 ? '<span class="tag plain">Level ' + q.minLevel + " and up</span>" : "") +
      "</div></div></div>" +
      (q.walkthroughText ? panel("How to do it", '<p class="lead">' + esc(q.walkthroughText) + "</p>") : "") +
      panel("Step by step", "" +
        '<div class="quest-step"><span class="num">1</span><div>' +
        "<strong>Talk to " + npcLink(q.giverNpc) + "</strong>" +
        '<div class="quest-text-block"><span class="label">What they ask</span>“' + esc(q.offerText) + "”</div>" +
        "</div></div>" +
        '<div class="quest-step"><span class="num">2</span><div>' +
        (q.requiredKills > 0
          ? "<strong>Kill " + q.requiredKills + " " + monsterLink(q.targetMonster) + "</strong>" +
            '<p class="lead">Only kills inside the area on the map below count, so work that ground.</p>'
          : "<strong>Gather what they asked for</strong>") +
        bringHtml +
        "</div></div>" +
        '<div class="quest-step"><span class="num">3</span><div>' +
        "<strong>Go back to " + npcLink(q.giverNpc) + "</strong>" +
        '<div class="quest-text-block"><span class="label">What they say</span>“' + esc(q.completionText) + "”</div>" +
        '<div class="quest-text-block"><span class="label">And if you call in again later</span>“' + esc(q.completedText) + "”</div>" +
        '<div class="rewards-row"><span class="reward gold">✦ ' + fmtNum(q.rewardGold) + " gold</span>" +
        '<span class="reward xp">✦ ' + fmtNum(q.rewardXp) + " XP</span>" +
        (q.rewardItemKey ? '<span class="reward item">✦ ' + itemLink(q.rewardItemKey) + "</span>" : "") +
        "</div>" +
        (q.minLevel > 0
          ? '<p class="lead">They will not settle up until you are level ' + q.minLevel +
            ". You can do all the fighting and collecting before then; only the hand-over waits.</p>"
          : "") +
        (q.unlockZoneId
          ? '<p class="lead">Finishing it opens the fast road to ' + zoneLink(q.unlockZoneId) + ".</p>"
          : "") +
        "</div></div>") +
      panel(
        q.requiredKills > 0 ? "Where to do it" : "Where to hand it in",
        '<div class="kv-grid">' +
          (q.requiredKills > 0 ? kv("What to kill", monsterLink(q.targetMonster)) + kv("How many", q.requiredKills) : "") +
          (bring.length ? kv("What to hand over", bring.join(", ")) : "") +
          (q.rewardItemKey ? kv("What you're given", itemLink(q.rewardItemKey)) : "") +
          (q.minLevel > 0 ? kv("Level needed to finish", q.minLevel) : "") +
          (q.unlockZoneId ? kv("Opens the road to", zoneLink(q.unlockZoneId)) : "") +
          "</div>" +
          mapHtml)
    );
  });

  // ---------------------------------------------------------- xp curve chart

  var XP_CURVE = DATA.xpCurve || { serverXpRate: 1, entries: [] };
  var REBIRTHS = DATA.rebirths || { rows: [], energyShop: [] };
  var XP_CHART_W = 900;
  var XP_CHART_H = 320;
  var XP_CHART_PAD = { l: 66, r: 16, t: 14, b: 34 };

  function xpCompact(n) {
    n = Number(n);
    var val, suffix;
    if (n >= 1e12) { val = n / 1e12; suffix = "T"; }
    else if (n >= 1e9) { val = n / 1e9; suffix = "B"; }
    else if (n >= 1e6) { val = n / 1e6; suffix = "M"; }
    else if (n >= 1e3) { val = n / 1e3; suffix = "K"; }
    else return String(n);
    // Exact powers of ten (every axis gridline) round-trip to a whole number - drop the
    // trailing ".0" for those, but keep one decimal of precision for in-between values
    // (a curve endpoint like 4.9T) since flooring would lose exactly what makes it interesting.
    return val.toFixed(val >= 10 ? 0 : 1).replace(/\.0$/, "") + suffix;
  }

  // Precomputes the scales shared by the static render and the hover layer, so the two never
  // drift apart. The level cap (xpToNextLevel 0, currently 400) is excluded from the curve itself
  // and called out in the caption instead - log(0) is undefined and it would otherwise plot as
  // a cliff to the axis floor.
  function xpChartScales() {
    var entries = (XP_CURVE.entries || []).filter(function (e) { return e.xpToNextLevel > 0; });
    if (!entries.length) return null;
    var minLevel = entries[0].level;
    var maxLevel = entries[entries.length - 1].level;
    var maxXp = entries.reduce(function (m, e) { return Math.max(m, e.xpToNextLevel); }, 0);
    var logMin = 2; // 10^2 = 100, level 1's cost
    var logMax = Math.max(logMin + 1, Math.ceil(Math.log(maxXp) / Math.LN10));
    var plotW = XP_CHART_W - XP_CHART_PAD.l - XP_CHART_PAD.r;
    var plotH = XP_CHART_H - XP_CHART_PAD.t - XP_CHART_PAD.b;
    return {
      entries: entries, minLevel: minLevel, maxLevel: maxLevel, maxXp: maxXp, logMin: logMin, logMax: logMax,
      xPix: function (level) { return XP_CHART_PAD.l + ((level - minLevel) / (maxLevel - minLevel)) * plotW; },
      yPix: function (xp) {
        var t = (Math.log(Math.max(xp, 1)) / Math.LN10 - logMin) / (logMax - logMin);
        return XP_CHART_PAD.t + (1 - t) * plotH;
      }
    };
  }

  function xpCurveChartHtml() {
    var s = xpChartScales();
    if (!s) return "";

    var path = s.entries.map(function (e, i) {
      return (i === 0 ? "M" : "L") + s.xPix(e.level).toFixed(1) + "," + s.yPix(e.xpToNextLevel).toFixed(1);
    }).join(" ");

    var gridlines = "";
    for (var p = s.logMin; p <= s.logMax; p++) {
      var y = s.yPix(Math.pow(10, p)).toFixed(1);
      gridlines +=
        '<line class="chart-grid" x1="' + XP_CHART_PAD.l + '" x2="' + (XP_CHART_W - XP_CHART_PAD.r) + '" y1="' + y + '" y2="' + y + '"></line>' +
        '<text class="chart-axis-label" x="' + (XP_CHART_PAD.l - 8) + '" y="' + (Number(y) + 4).toFixed(1) + '" text-anchor="end">' + xpCompact(Math.pow(10, p)) + "</text>";
    }

    var levelTicks = [s.minLevel];
    var step = s.maxLevel <= 200 ? 25 : s.maxLevel <= 500 ? 50 : 100;
    for (var lvl = step; lvl < s.maxLevel; lvl += step) levelTicks.push(lvl);
    levelTicks.push(s.maxLevel);
    var xTicks = levelTicks.map(function (lvl) {
      return '<text class="chart-axis-label" x="' + s.xPix(lvl).toFixed(1) + '" y="' + (XP_CHART_H - XP_CHART_PAD.b + 18) + '" text-anchor="middle">' + lvl + "</text>";
    }).join("");

    var milestones = [1, 5, 10, 25, 50];
    for (var m = 100; m < s.maxLevel; m += 50) milestones.push(m);
    milestones.push(s.maxLevel);
    var milestoneRows = milestones.map(function (lvl) {
      var e = s.entries[lvl - s.minLevel];
      return (
        "<tr><td>" + e.level + "</td><td class=\"num\">" + fmtNum(e.xpToNextLevel) + "</td><td class=\"num\">" + fmtNum(e.totalXp) + "</td></tr>"
      );
    }).join("");

    return (
      '<section class="panel" id="xpCurvePanel">' +
      "<h2>How long each level takes</h2>" +
      '<p class="lead">Experience needed to go from one level to the next. The scale is squashed, ' +
      "because the climb runs from " + xpCompact(s.entries[0].xpToNextLevel) + " at level 1 to " +
      xpCompact(s.maxXp) + " at level " + s.maxLevel + " — a flat chart would be one long wall. " +
      "Everything you kill is worth " + XP_CURVE.serverXpRate + " times its listed experience, so " +
      "the climb is gentler than these numbers look. Level " + (s.maxLevel + 1) + " is as high as " +
      "anyone goes. Hover anywhere on the line to read off a level.</p>" +
      '<div class="chart-wrap">' +
      '<svg id="xpCurveSvg" viewBox="0 0 ' + XP_CHART_W + " " + XP_CHART_H + '" role="img" aria-label="XP required to reach each level, log scale">' +
      gridlines + xTicks +
      '<path class="chart-line" d="' + path + '"></path>' +
      '<circle id="xpCurveDot" class="chart-dot" r="4" style="display:none"></circle>' +
      '<line id="xpCurveCrosshair" class="chart-crosshair" y1="' + XP_CHART_PAD.t + '" y2="' + (XP_CHART_H - XP_CHART_PAD.b) + '" style="display:none"></line>' +
      '<rect id="xpCurveHit" tabindex="0" x="' + XP_CHART_PAD.l + '" y="' + XP_CHART_PAD.t + '" width="' + (XP_CHART_W - XP_CHART_PAD.l - XP_CHART_PAD.r) +
      '" height="' + (XP_CHART_H - XP_CHART_PAD.t - XP_CHART_PAD.b) + '" fill="transparent"></rect>' +
      "</svg>" +
      '<div id="xpCurveTooltip" class="chart-tooltip" role="status" style="display:none"></div>' +
      "</div>" +
      "<details class=\"xp-table-toggle\"><summary>View milestone levels as a table</summary>" +
      '<div class="table-wrap"><table class="data-table"><thead><tr><th>Level</th><th>XP to next level</th><th>Total XP so far</th></tr></thead>' +
      "<tbody>" + milestoneRows + "</tbody></table></div></details>" +
      "</section>"
    );
  }

  function wireXpCurveChart() {
    var s = xpChartScales();
    var svg = document.getElementById("xpCurveSvg");
    if (!s || !svg) return;

    var hit = document.getElementById("xpCurveHit");
    var dot = document.getElementById("xpCurveDot");
    var crosshair = document.getElementById("xpCurveCrosshair");
    var tooltip = document.getElementById("xpCurveTooltip");
    var wrap = svg.parentElement;

    function show(level) {
      level = Math.max(s.minLevel, Math.min(s.maxLevel, Math.round(level)));
      var entry = s.entries[level - s.minLevel];
      if (!entry) return;
      var x = s.xPix(entry.level);
      var y = s.yPix(entry.xpToNextLevel);
      dot.setAttribute("cx", x);
      dot.setAttribute("cy", y);
      dot.style.display = "";
      crosshair.setAttribute("x1", x);
      crosshair.setAttribute("x2", x);
      crosshair.style.display = "";

      tooltip.innerHTML =
        "<strong>Level " + entry.level + "</strong>" +
        '<div class="row"><span>XP to next level</span><span>' + fmtNum(entry.xpToNextLevel) + "</span></div>" +
        '<div class="row"><span>Total XP so far</span><span>' + fmtNum(entry.totalXp) + "</span></div>";
      tooltip.style.display = "";

      var svgRect = svg.getBoundingClientRect();
      var wrapRect = wrap.getBoundingClientRect();
      var pxX = (x / XP_CHART_W) * svgRect.width + (svgRect.left - wrapRect.left);
      var pxY = (y / XP_CHART_H) * svgRect.height + (svgRect.top - wrapRect.top);
      var left = pxX + 14;
      if (left + 190 > wrapRect.width) left = pxX - 204;
      tooltip.style.left = Math.max(0, left) + "px";
      tooltip.style.top = Math.max(0, pxY - 54) + "px";
    }

    function hide() {
      dot.style.display = "none";
      crosshair.style.display = "none";
      tooltip.style.display = "none";
    }

    function levelFromClientX(clientX) {
      var rect = svg.getBoundingClientRect();
      var svgX = ((clientX - rect.left) / rect.width) * XP_CHART_W;
      return s.minLevel + ((svgX - XP_CHART_PAD.l) / (XP_CHART_W - XP_CHART_PAD.l - XP_CHART_PAD.r)) * (s.maxLevel - s.minLevel);
    }

    hit.addEventListener("pointermove", function (evt) { show(levelFromClientX(evt.clientX)); });
    hit.addEventListener("pointerdown", function (evt) { show(levelFromClientX(evt.clientX)); });
    hit.addEventListener("pointerleave", hide);
    hit.addEventListener("focus", function () { show((s.minLevel + s.maxLevel) / 2); });
    hit.addEventListener("blur", hide);
    hit.addEventListener("keydown", function (evt) {
      var current = tooltip.style.display === "none" ? (s.minLevel + s.maxLevel) / 2 : Number((tooltip.querySelector("strong") || {}).textContent.replace(/\D+/g, "")) || s.minLevel;
      if (evt.key === "ArrowRight") { show(current + 1); evt.preventDefault(); }
      else if (evt.key === "ArrowLeft") { show(current - 1); evt.preventDefault(); }
    });
  }

  // ---------------------------------------------------------------- systems

  route("systems", function () {
    setTimeout(wireXpCurveChart, 0);
    var timeline = (META.updates || []).map(function (p) {
      return (
        '<div class="timeline-item"><h3>' + esc(p.title) + "</h3>" +
        '<div class="date">' + esc(p.date) + "</div>" +
        "<ul>" + p.bullets.map(function (b) { return "<li>" + esc(b) + "</li>"; }).join("") + "</ul></div>"
      );
    }).join("");

    var collections = (META.collections || []).map(function (c) {
      var count = ITEMS.filter(function (it) { return it.key.indexOf(c.itemKeyPrefix) === 0; }).length;
      return panel(c.name + " (" + count + " pieces)", '<p class="lead">' + esc(c.summary) + "</p><p>" +
        ITEMS.filter(function (it) { return it.key.indexOf(c.itemKeyPrefix) === 0; }).slice(0, 8)
          .map(function (it) { return itemLink(it.key); }).join(" · ") + (count > 8 ? " · …" : "") + "</p>");
    }).join("");

    var standalone = (META.standaloneItems || []).map(function (s) {
      return '<div class="loot-row"><span>' + itemLink(s.key) + "</span><span>" + esc(s.note) + "</span></div>";
    }).join("");

    var arenaMobs = MONSTERS.filter(function (m) { return m.name.indexOf((META.colosseum || {}).monsterNamePrefix || "\0") === 0; });
    var colosseumHtml = META.colosseum
      ? panel(META.colosseum.name, '<p class="lead">' + esc(META.colosseum.summary) + "</p><div class=\"pill-row\">" +
          arenaMobs.map(function (m) { return '<span class="pill">' + monsterLink(m.name) + " (lvl " + m.level + ")</span>"; }).join("") + "</div>")
      : "";

    return (
      '<div class="page-header"><p class="eyebrow">How the game works</p><h1>Levelling, gear and what has changed lately</h1>' +
      '<p class="lead">The parts of the game that are not tied to any one place: how long the ' +
      "climb takes, what starting over buys you, the armor sets, and what has changed recently." +
      "</p></div>" +
      '<div class="section-title">The climb</div>' + xpCurveChartHtml() +
      panel("Starting over", '<p class="lead">When a character has gone as far as they can, they ' +
        "can be reborn: back to level 1, but permanently stronger, with points to spend on what " +
        "kind of character they become next. " + link("rebirths", "How rebirth works, step by step") + ".</p>") +
      '<div class="section-title">Armor sets</div>' + collections +
      '<div class="section-title">Gear that stands on its own</div>' +
      panel("Not part of a set, not tied to a place", standalone) +
      '<div class="section-title">The arena</div>' + colosseumHtml +
      '<div class="section-title">What has changed lately</div><div class="timeline">' + timeline + "</div>"
    );
  });

  // -------------------------------------------------------------- rebirths

  route("rebirths", function () {
    var R = REBIRTHS;
    if (!R.rows || !R.rows.length) return notFound("Rebirth data");
    var last = R.rows[R.rows.length - 1];

    var howTo =
      "<ol class=\"rebirth-steps\">" +
      "<li>Defeat Gabriel Archonis in the Oracle's trial duel (once - it stays done for every later rebirth).</li>" +
      "<li>Reach the level in the table below for your next rebirth, then tell the Oracle you are <em>ready to be reborn</em>.</li>" +
      "<li>You return to level 1 with every attribute reset to the starting value shown, your spells and skills cleared, and the Seraph wings and ring equipped.</li>" +
      "<li>Speak with Alphan, then spend all of your energy points with his associates (below). Alphan sends you back to Lighthaven once every point is spent.</li>" +
      "</ol>" +
      '<p class="lead">You can be reborn at most ' + R.maxRebirths + " times. The level cap is " + R.maxLevel +
      ", and the last rebirth needs level " + last.requiredLevel + ".</p>";

    var shop = (R.energyShop || []).map(function (v) {
      return '<div class="rebirth-vendor"><strong>' + esc(v.npc) + "</strong> &middot; " + esc(v.buys) + "<p>" + esc(v.details) + "</p></div>";
    }).join("") +
      '<p class="lead">Everything bought with energy lasts one life: your next rebirth resets attributes, elemental power and resistance, and maximum health and mana before granting fresh energy.</p>';

    var rows = R.rows.map(function (r) {
      return "<tr><td class=\"num\">" + r.rebirth + "</td><td class=\"num\">" + r.requiredLevel +
        "</td><td class=\"num\">" + r.startingAttributes + "</td><td class=\"num\">" + r.energyPoints +
        "</td><td class=\"num\">" + r.auraHealChance + "%</td><td class=\"num\">" + r.auraRetaliationChance +
        "%</td><td class=\"num\">" + r.auraBurstChance + "%</td></tr>";
    }).join("");

    return (
      '<div class="page-header"><p class="eyebrow">Systems</p><h1>Rebirths</h1>' +
      '<p class="lead">What each rebirth asks for and what it gives. Every rebirth needs ' +
      R.levelPerPreviousRebirth + " more levels than the one before, starts you with " +
      "higher attributes, grants more energy to spend, and strengthens your Seraph aura.</p></div>" +
      panel("How to be reborn", howTo) +
      panel("Spending energy points", shop) +
      '<div class="section-title">Every rebirth <span class="count">' + R.rows.length + "</span></div>" +
      '<div class="table-wrap"><table class="data-table"><thead><tr>' +
      "<th>Rebirth</th><th>Level required</th><th>Starting attributes</th><th>Energy points</th>" +
      "<th>Aura heal chance</th><th>Aura burn chance</th><th>Aura fire burst chance</th>" +
      "</tr></thead><tbody>" + rows + "</tbody></table></div>" +
      '<p class="lead">Starting attributes apply to all five (strength, endurance, agility, intelligence, wisdom). ' +
      "Aura chances: heal is rolled when you are hit and heals you and nearby allies; burn is rolled when you are hit and damages the attacker; " +
      "fire burst is rolled when you land a hit and damages enemies around you. " +
      "These are the real odds per roll (the game rolls 0-100 inclusive), rounded to one decimal.</p>"
    );
  });

  // -------------------------------------------------------------- list page

  function looksNumeric(v) {
    return typeof v === "number" || (typeof v === "string" && /^-?\d+(\.\d+)?$/.test(v.trim()));
  }

  function compareValues(av, bv) {
    if (looksNumeric(av) && looksNumeric(bv)) return parseFloat(av) - parseFloat(bv);
    if (typeof av === "boolean" || typeof bv === "boolean") return (av ? 1 : 0) - (bv ? 1 : 0);
    return String(av || "").localeCompare(String(bv || ""));
  }

  function uniq(arr) {
    var seen = {};
    var out = [];
    arr.forEach(function (v) {
      var k = String(v);
      if (v !== undefined && v !== null && !seen[k]) { seen[k] = true; out.push(v); }
    });
    return out;
  }

  var listPageState = {};

  function listPage(cfg) {
    var stateKey = cfg.title;
    listPageState[stateKey] = listPageState[stateKey] ||
      { search: "", sort: cfg.defaultSort, dir: 1, filters: {}, tab: cfg.tabs ? cfg.tabs[0].key : null };
    var st = listPageState[stateKey];

    var tabControls = cfg.tabs
      ? '<div class="category-tabs">' + cfg.tabs.map(function (t) {
          return '<button type="button" data-tab-key="' + esc(t.key) + '"' +
            (t.key === st.tab ? ' class="active"' : "") + ">" + esc(t.label) + "</button>";
        }).join("") + "</div>"
      : "";

    var filterControls = (cfg.filters || []).map(function (f, fi) {
      var opts = f.options.map(function (o) {
        return '<option value="' + esc(o) + '">' + esc(o) + "</option>";
      }).join("");
      return (
        '<select data-filter-idx="' + fi + '"><option value="">' + esc(f.label) + ": all</option>" + opts + "</select>"
      );
    }).join("");

    var columns = cfg.columns.map(function (c) {
      return '<th data-sort-key="' + c.key + '">' + esc(c.label) + "</th>";
    }).join("");

    var html =
      '<div class="page-header"><p class="eyebrow">' + esc(cfg.eyebrow) + "</p><h1>" + esc(cfg.title) + "</h1>" +
      '<p class="lead">' + esc(cfg.lead) + "</p></div>" +
      tabControls +
      '<div class="toolbar"><input type="search" id="listSearch" placeholder="Search ' + esc(cfg.title.toLowerCase()) + '…" value="' + esc(st.search) + '">' +
      filterControls +
      '<span class="result-count" id="listCount"></span></div>' +
      '<div class="table-wrap"><table class="data-table"><thead><tr>' + columns + '</tr></thead><tbody id="listBody"></tbody></table></div>';

    setTimeout(function () { wireListPage(cfg, st); }, 0);
    return html;
  }

  function wireListPage(cfg, st) {
    var searchEl = document.getElementById("listSearch");
    var bodyEl = document.getElementById("listBody");
    var countEl = document.getElementById("listCount");
    var selects = document.querySelectorAll('[data-filter-idx]');
    var ths = document.querySelectorAll("th[data-sort-key]");
    var tabButtons = document.querySelectorAll("[data-tab-key]");

    function apply() {
      var rows = cfg.rows.slice();
      if (cfg.tabs && st.tab) {
        var activeTab = cfg.tabs.filter(function (t) { return t.key === st.tab; })[0];
        if (activeTab && activeTab.filter) rows = rows.filter(activeTab.filter);
      }
      if (st.search) {
        var q = st.search.toLowerCase();
        rows = rows.filter(function (r) {
          return (cfg.searchFields || []).some(function (f) { return String(r[f] || "").toLowerCase().indexOf(q) !== -1; });
        });
      }
      (cfg.filters || []).forEach(function (f, fi) {
        var val = st.filters[fi];
        if (val) {
          rows = rows.filter(function (r) {
            var actual = f.computed ? f.computed(r) : r[f.field];
            return String(actual) === val;
          });
        }
      });
      if (st.sort) {
        var sortCol = cfg.columns.filter(function (c) { return c.key === st.sort; })[0];
        var accessor = sortCol && sortCol.sortValue ? sortCol.sortValue : function (r) { return r[st.sort]; };
        rows.sort(function (a, b) { return compareValues(accessor(a), accessor(b)) * st.dir; });
      }
      countEl.textContent = rows.length + " / " + cfg.rows.length;
      bodyEl.innerHTML = rows.map(function (r) {
        var target = r.name !== undefined && cfg.title === "Monster roster" ? r.name : (r.key || r.id);
        return (
          '<tr data-target="' + esc(target) + '">' +
          cfg.columns.map(function (c) {
            var val = c.render ? c.render(r) : esc(r[c.key]);
            return "<td" + (c.numeric ? ' class="num"' : "") + ">" + val + "</td>";
          }).join("") +
          "</tr>"
        );
      }).join("") || '<tr><td colspan="' + cfg.columns.length + '"><div class="empty-state">No matches.</div></td></tr>';

      bodyEl.querySelectorAll("tr[data-target]").forEach(function (tr) {
        tr.addEventListener("click", function () {
          var base = cfg.title === "Monster roster" ? "monsters" : cfg.title === "Items" ? "items" : cfg.title === "Spells" ? "spells" : "npcs";
          location.hash = "#/" + base + "/" + slug(tr.getAttribute("data-target"));
        });
      });
    }

    searchEl.addEventListener("input", function () { st.search = searchEl.value; apply(); });
    selects.forEach(function (sel, fi) {
      sel.addEventListener("change", function () { st.filters[fi] = sel.value; apply(); });
    });
    tabButtons.forEach(function (btn) {
      btn.addEventListener("click", function () {
        st.tab = btn.getAttribute("data-tab-key");
        tabButtons.forEach(function (b) { b.classList.toggle("active", b === btn); });
        apply();
      });
    });
    ths.forEach(function (th) {
      th.addEventListener("click", function () {
        var key = th.getAttribute("data-sort-key");
        if (st.sort === key) st.dir *= -1; else { st.sort = key; st.dir = 1; }
        apply();
      });
    });
    apply();
  }

  // ------------------------------------------------------------------ misc

  function breadcrumb(parts) {
    return '<div class="breadcrumb">' + parts.map(function (p) {
      return p[1] ? '<a href="#/' + p[1] + '">' + esc(p[0]) + "</a>" : esc(p[0]);
    }).join(" / ") + "</div>";
  }

  function notFound(kind) {
    return '<div class="empty-state"><h2>' + esc(kind) + " not found</h2><p>" + link("", "Back to home") + "</p></div>";
  }

  // ---------------------------------------------------------- global search

  function buildSearchIndex() {
    var idx = [];
    MONSTERS.forEach(function (m) { idx.push({ group: "Monsters", label: m.displayName, sub: "Level " + m.level, hash: "#/monsters/" + slug(m.name) }); });
    ITEMS.forEach(function (it) { idx.push({ group: "Items", label: it.name, sub: it.bodyPart, hash: "#/items/" + slug(it.key) }); });
    SPELLS.forEach(function (s) { idx.push({ group: "Spells", label: s.name, sub: "Lvl " + s.minLevel, hash: "#/spells/" + slug(s.key) }); });
    NPCS.forEach(function (n) { idx.push({ group: "NPCs", label: n.displayName, sub: n.origin, hash: "#/npcs/" + slug(n.id) }); });
    QUESTS.forEach(function (q) { idx.push({ group: "Quests", label: q.title, sub: "Given by " + npcPlain(q.giverNpc), hash: "#/quests/" + slug(q.id) }); });
    ZONES.forEach(function (z) { idx.push({ group: "Zones", label: z.name, sub: z.levelRange, hash: "#/zones/" + slug(z.id) }); });
    return idx;
  }
  var SEARCH_INDEX = buildSearchIndex();

  function wireSearch() {
    var input = document.getElementById("globalSearch");
    var results = document.getElementById("searchResults");

    function run() {
      var q = input.value.trim().toLowerCase();
      if (!q) { results.classList.add("hidden"); results.innerHTML = ""; return; }
      var hits = SEARCH_INDEX.filter(function (h) { return h.label.toLowerCase().indexOf(q) !== -1; }).slice(0, 30);
      var groups = {};
      hits.forEach(function (h) { groups[h.group] = groups[h.group] || []; groups[h.group].push(h); });
      var html = Object.keys(groups).map(function (g) {
        return '<div class="search-group-label">' + g + "</div>" + groups[g].map(function (h) {
          return '<a class="search-hit" href="' + h.hash + '"><span>' + esc(h.label) + "</span><small>" + esc(h.sub || "") + "</small></a>";
        }).join("");
      }).join("");
      results.innerHTML = html || '<div class="search-group-label">No matches</div>';
      results.classList.remove("hidden");
    }

    input.addEventListener("input", run);
    input.addEventListener("focus", run);
    document.addEventListener("click", function (e) {
      if (!results.contains(e.target) && e.target !== input) results.classList.add("hidden");
    });
    results.addEventListener("click", function () { results.classList.add("hidden"); input.blur(); });
    document.addEventListener("keydown", function (e) {
      if (e.key === "/" && document.activeElement !== input) { e.preventDefault(); input.focus(); }
      if (e.key === "Escape") { results.classList.add("hidden"); input.blur(); }
    });
  }

  // ------------------------------------------------------------------- init

  window.addEventListener("hashchange", renderRoute);
  document.addEventListener("DOMContentLoaded", function () {
    wireSearch();
    renderRoute();
  });
})();
