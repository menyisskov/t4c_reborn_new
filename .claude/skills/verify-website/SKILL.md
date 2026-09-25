---
name: verify-website
description: Check that the reference website (the compendium, deployed on Vercel from main) is correct and live. It regenerates the data, renders pages in a real browser at desktop and phone widths, and confirms the live production deployment matches main. Use when the user asks "is the website up to date?", after any change that affects website-visible data, or after editing compendium/app.js, app.css or index.html.
---

# Verify the reference website

The site lives in `compendium/`: static `index.html`, `app.js` and `app.css`, plus generated
`data.js` and `data/*.json`. `tools/CompendiumExporter` builds the data from the live game
registries. The `Update compendium` workflow regenerates it on every push to `main` (committing
with `[skip compendium]`), and Vercel deploys `main` to production.

## 1. Data is current

```bash
mvn -q compile
mvn -q dependency:build-classpath -Dmdep.outputFile=/tmp/cp.txt
java -cp "target/classes:$(cat /tmp/cp.txt)" com.perso.T4C.tools.MinimapExporter compendium/data
java -cp "target/classes:$(cat /tmp/cp.txt)" com.perso.T4C.tools.CompendiumExporter compendium/data
git status --short compendium/
```

- On `main`, a clean `git status` means the committed data is current.
- On a PR branch, any diff should be exactly what the PR intends. Read it; pure reorder
  churn means an unsorted list in the exporter, which is a bug to fix.
- Spot-check the new values with a short `python3 -c "import json; ..."` against
  `compendium/data/<file>.json`.

## 2. Pages render (for any change to app.js, app.css or index.html)

Chromium is pre-installed. Use `executablePath: '/opt/pw-browsers/chromium'` and never run
`playwright install`. Render the changed route at 1280px and 390px wide, and check for:
- no page errors (`page.on('pageerror')`)
- no horizontal scroll (`document.documentElement.scrollWidth` equals the viewport width)
- the expected row/section counts
- links to the page from the nav and from related pages

```js
const { chromium } = require('playwright');
(async () => {
  const b = await chromium.launch({ executablePath: '/opt/pw-browsers/chromium' });
  for (const [w, h] of [[1280, 1800], [390, 1600]]) {
    const p = await b.newPage({ viewport: { width: w, height: h } });
    const errs = []; p.on('pageerror', e => errs.push(e.message));
    // REPO is the checkout root, passed in by the run command below - never hard-code a path.
    await p.goto(`file://${process.env.REPO}/compendium/index.html#/<route>`);
    await p.waitForTimeout(800);
    await p.screenshot({ path: `<scratchpad>/<route>-${w}.png` });
    console.log(w, errs, await p.evaluate(() => document.documentElement.scrollWidth));
  }
  await b.close();
})();
```

Run it from the scratchpad with
`REPO=$(git -C <checkout> rev-parse --show-toplevel) NODE_PATH=$(npm root -g) node shot.js`, so
it renders whichever checkout you're in. Then look at the
screenshots: text alignment, readable tables, and nothing cut off on the phone view.

## 3. The live site is current (answering "is it up to date?")

"Up to date" means **both** of these:
1. The latest Vercel **production** deployment (`list_deployments`, `target: production`) has
   `githubCommitSha` equal to `origin/main`'s head (or its own `[skip compendium]` data commit),
   and its state is `READY`.
2. The latest `Update compendium` workflow run on `main` succeeded (`actions_list`,
   `resource_id: compendium.yml`), and step 1 on `main` shows no diff.

A PR's Vercel **preview** URL (posted by the Vercel bot on the PR) is where to point the user to
look at a change before it merges. After merge it goes live on production.
