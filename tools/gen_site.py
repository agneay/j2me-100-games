#!/usr/bin/env python3
"""Generate the GitHub Pages site into website/_site from website/games.json.

Standard library only. Inputs:
  website/games.json            catalogue + verification data (tools/gen_docs.py)
  website/src/assets/           CSS, JS, fonts-free static assets, og.png
  games/<game>/media/           screenshots, demo GIF, icon
  games/<game>/build/web/game.js  browser demo (optional; built by `ant web`)
  docs/*.md                     rendered as documentation pages

usage: python tools/gen_site.py [--out website/_site]
"""
import html
import json
import os
import re
import shutil
import sys
from datetime import date

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import catalog  # noqa: E402

ROOT = catalog.ROOT
SRC = os.path.join(ROOT, "website", "src")
OUT = os.path.join(ROOT, "website", "_site")
REPO = catalog.REPO_URL
SITE = catalog.PAGES_URL
BASE_PATH = "/" + SITE.split("/", 3)[3].strip("/") + "/" if SITE.count("/") >= 3 else "/"

CAT_COLORS = {
    "Arcade": "#ff5c9d", "Puzzle": "#4de1ff", "Platformer": "#ffc24d", "Racing": "#ff8a4d", "Strategy": "#b18cff",
    "Board": "#4dff9a", "Card": "#ff6b6b", "RPG": "#d98cff", "Sports": "#7dd3fc", "Simulation": "#a3e635",
    "Experimental": "#fde047",
}

DOCS = [
    ("installing", "Install", "Installing on a phone"),
    ("emulator", "Emulators", "Running in an emulator"),
    ("supported-devices", "Devices", "Supported devices"),
    ("verification", "Verification", "Verification"),
    ("building", "Building", "Building from source"),
    ("development", "Development", "Development guide"),
    ("game-design-guidelines", "Design", "Game design guidelines"),
]

e = html.escape


def write(rel, text):
    p = os.path.join(OUT, rel)
    os.makedirs(os.path.dirname(p), exist_ok=True)
    with open(p, "w", encoding="utf-8", newline="\n") as f:
        f.write(text)


def phone_svg():
    return ('<svg viewBox="0 0 22 30" aria-hidden="true"><rect x="1" y="1" width="20" height="28" rx="4" fill="#172340" '
            'stroke="#4dff9a" stroke-width="1.5"/><rect x="4" y="4" width="14" height="10" fill="#4dff9a"/>'
            '<g fill="#9aa8cc"><rect x="4" y="17" width="3" height="2"/><rect x="9.5" y="17" width="3" height="2"/>'
            '<rect x="15" y="17" width="3" height="2"/><rect x="4" y="21" width="3" height="2"/><rect x="9.5" y="21" '
            'width="3" height="2"/><rect x="15" y="21" width="3" height="2"/><rect x="9.5" y="25" width="3" '
            'height="2"/></g></svg>')


def layout(rel_root, path, title, desc, body, current="", image=None, extra_head="", scripts=()):
    url = SITE + "/" + path
    og_image = image or SITE + "/assets/og.png"
    full_title = title if title.startswith("J2ME 100 Games") else "%s | J2ME 100 Games" % title
    nav = [("games", "Games", rel_root + "#catalogue"), ("download", "Download", rel_root + "download/"),
           ("installing", "Install", rel_root + "docs/installing/"),
           ("verification", "Compatibility", rel_root + "docs/verification/"),
           ("building", "Build", rel_root + "docs/building/"), ("github", "GitHub", REPO)]
    nav_html = "".join('<a href="%s"%s>%s</a>' % (e(h), ' aria-current="page"' if k == current else "", n)
                       for k, n, h in nav)
    script_tags = "".join('<script src="%s"></script>' % e(rel_root + s) for s in scripts)
    return """<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>{title}</title>
<meta name="description" content="{desc}">
<link rel="canonical" href="{url}">
<meta property="og:type" content="website">
<meta property="og:site_name" content="J2ME 100 Games">
<meta property="og:title" content="{title}">
<meta property="og:description" content="{desc}">
<meta property="og:url" content="{url}">
<meta property="og:image" content="{og}">
<meta name="twitter:card" content="summary_large_image">
<meta name="theme-color" content="#07090f">
<link rel="icon" href="{root}assets/favicon.svg" type="image/svg+xml">
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&family=Press+Start+2P&family=VT323&display=swap">
<link rel="stylesheet" href="{root}assets/css/site.css">
{extra}
</head>
<body>
<a class="skip" href="#main">Skip to content</a>
<header class="site-header">
  <div class="wrap">
    <a class="brand" href="{root}">{logo}<span>J2ME <b>100</b> Games</span></a>
    <button class="menu-btn" type="button" aria-expanded="false" aria-controls="site-nav">Menu</button>
    <nav class="nav" id="site-nav" aria-label="Main">{nav}</nav>
  </div>
</header>
<main id="main">
{body}
</main>
<footer class="site-footer">
  <div class="wrap">
    <div>
      <p><strong>J2ME 100 Games</strong> &middot; v{version} &middot; MIT License</p>
      <p>100 original games for Java ME (CLDC 1.0 / MIDP 2.0) button phones.<br>
      Not affiliated with any phone maker or game publisher. Java is a trademark of Oracle.</p>
    </div>
    <div>
      <p><a href="{repo}">Source on GitHub</a> &middot; <a href="{repo}/releases">Releases</a> &middot;
      <a href="{root}docs/verification/">Verification</a> &middot; <a href="{repo}/issues/new?template=device-report.yml">Report a device</a></p>
      <p>Tip: phones had secret codes. Try dialling one on your keyboard.</p>
    </div>
  </div>
</footer>
<div id="egg" class="egg" role="status" aria-live="polite"></div>
<script src="{root}assets/js/site.js"></script>
{scripts}
</body>
</html>
""".format(title=e(full_title), desc=e(desc), url=e(url), og=e(og_image), root=rel_root, logo=phone_svg(),
           nav=nav_html, body=body, version=catalog.COLLECTION_VERSION, repo=REPO, extra=extra_head,
           scripts=script_tags)


def size_kb(n):
    return "%.1f KB" % (n / 1024.0) if n else "-"


def badge_for(v):
    if v["devices"]:
        return '<span class="badge ok">Real device verified</span>'
    if v["emulator"]:
        return '<span class="badge ok">Emulator verified</span>'
    if v["build"]:
        return '<span class="badge warn">Build verified</span>'
    return '<span class="badge no">Not built</span>'


def card(g, rel_root, lazy=True):
    search = " ".join([g["name"], g["category"], g["summary"], " ".join(g["tags"]), g["id"]]).lower()
    return """<article class="card" data-id="{id}" data-name="{name_attr}" data-category="{cat}" data-size="{size}" data-search="{search}">
  <a class="shot scan" href="{root}games/{slug}/" tabindex="-1" aria-hidden="true"><img src="{root}media/{slug}/title.png" alt="" width="176" height="208"{lazy}></a>
  <div class="body">
    <span class="num">#{id}</span>
    <h3><a href="{root}games/{slug}/">{name}</a></h3>
    <p>{summary}</p>
    <div class="meta"><span class="chip cat" style="--cat:{color}">{cat}</span><span class="chip">{kb}</span></div>
  </div>
</article>""".format(id=g["id"], name=e(g["name"]), name_attr=e(g["name"].lower()), cat=e(g["category"]),
                     size=g["jarSize"] or 0, search=e(search), root=rel_root, slug=g["slug"],
                     lazy=' loading="lazy"' if lazy else "", summary=e(g["summary"]),
                     color=CAT_COLORS.get(g["category"], "#4de1ff"), kb=size_kb(g["jarSize"]))


def count_java_lines():
    n = 0
    for base in ("games", "common"):
        for dp, _, fs in os.walk(os.path.join(ROOT, base)):
            if os.sep + "build" in dp or os.sep + "dist" in dp:
                continue
            for f in fs:
                if f.endswith(".java"):
                    with open(os.path.join(dp, f), encoding="utf-8", errors="replace") as fh:
                        n += sum(1 for line in fh if line.strip())
    return n


def pick_featured(games, n=8):
    out = []
    for cat in catalog.CATEGORIES:
        for g in games:
            if g["featured"] and g["category"] == cat and g not in out:
                out.append(g)
                break
        if len(out) >= n:
            break
    return out


def index_page(data, games, has_demo):
    v = [g["verification"] for g in games]
    total = len(games)
    sizes = [g["jarSize"] for g in games if g["jarSize"]]
    cats = {}
    for g in games:
        cats[g["category"]] = cats.get(g["category"], 0) + 1
    built = sum(1 for x in v if x["build"])
    ref = sum(1 for x in v if x["reference"])
    emu = sum(1 for x in v if x["emulator"])
    dev = sum(1 for x in v if x["devices"])
    demos = sum(1 for g in games if has_demo(g))
    java_lines = count_java_lines()
    featured = pick_featured(games)
    hero = featured[0] if featured else games[0]
    stats = [
        (str(total), "original games"),
        (str(len(cats)), "categories"),
        ("%.1f MB" % (sum(sizes) / 1048576.0), "all JARs together"),
        ("%.1f KB" % (sum(sizes) / 1024.0 / max(1, len(sizes))), "average JAR"),
        ("{:,}".format(java_lines), "lines of Java"),
        ("5", "screen sizes tested"),
        (str(demos), "browser demos"),
        ("0", "floats used"),
    ]
    stats_html = "".join('<div class="stat"><b>%s</b><span>%s</span></div>' % (a, b) for a, b in stats)
    cat_buttons = '<button type="button" data-cat="all" aria-pressed="true">All %d</button>' % total + "".join(
        '<button type="button" data-cat="%s" aria-pressed="false">%s %d</button>' % (c, c, cats[c])
        for c in catalog.CATEGORIES if c in cats)
    pm_games = json.dumps([{"id": g["id"], "name": g["name"], "short": short_name(g["name"]),
                            "url": "games/%s/" % g["slug"], "icon": "media/%s/icon.png" % g["slug"]} for g in games])

    def level(n, label, text, cls):
        return ('<div class="level %s"><div class="n">%d/%d</div><strong>%s</strong><p>%s</p></div>'
                % (cls if n else "zero", n, total, label, text))

    body = """
<section class="hero">
  <div class="wrap">
    <div>
      <p class="pixel" style="font-size:11px;color:var(--amber)">JAVA ME &middot; CLDC 1.0 &middot; MIDP 2.0</p>
      <h1>100 original games for <span class="glow">button phones</span><span class="blink" aria-hidden="true">_</span></h1>
      <p class="lead">Arcade, puzzle, board, card, racing, sports, RPG and experimental games, written from scratch
      for classic Java feature phones. Each one is a tiny JAR + JAD you can install on a real handset or an emulator,
      and each one has a browser demo you can try right here.</p>
      <div class="cta">
        <a class="btn primary" href="#catalogue">Browse the games</a>
        <a class="btn amber" href="{zip}">Download all ({zipname})</a>
        <button class="btn" type="button" data-open-phone-menu>Phone menu mode</button>
      </div>
      <div class="stats">{stats}</div>
    </div>
    <div>
      <div class="phone" aria-label="{hero_name} running">
        <div class="speaker"></div>
        <div class="screen scan">
          <picture>
            <source srcset="media/{hero_slug}/play.png" media="(prefers-reduced-motion: reduce)">
            <img src="media/{hero_slug}/demo.gif" alt="{hero_name} gameplay" width="176" height="208">
          </picture>
        </div>
        <p class="label">{hero_name_upper}</p>
      </div>
      <p style="text-align:center;margin-top:12px"><a href="games/{hero_slug}/">Play {hero_name} in your browser</a></p>
    </div>
  </div>
</section>

<section class="wrap featured" aria-labelledby="featured-h">
  <h2 id="featured-h">Featured</h2>
  <p class="section-intro">A taste of each category. Every game has a title screen, help, pause menu, restart and
  game-over screens, and plays entirely on the 12-key keypad.</p>
  <div class="grid">{featured}</div>
</section>

<section class="wrap" id="catalogue" aria-labelledby="catalogue-h">
  <h2 id="catalogue-h">All {total} games</h2>
  <div class="controls">
    <label class="sr-only" for="q">Search games</label>
    <input type="search" id="q" placeholder="Search by name, tag or category..." autocomplete="off">
    <label class="sr-only" for="sort">Sort</label>
    <select id="sort">
      <option value="id">Sort: number</option>
      <option value="name">Sort: name</option>
      <option value="category">Sort: category</option>
      <option value="size">Sort: smallest JAR</option>
      <option value="size-desc">Sort: largest JAR</option>
    </select>
    <span id="result-count" class="result-count" role="status" aria-live="polite"></span>
  </div>
  <div class="cats" role="group" aria-label="Filter by category">{cat_buttons}</div>
  <div class="grid" id="catalog-grid">{cards}</div>
  <p class="empty" id="catalog-empty" hidden>No games match. Try another search.</p>
</section>

<section class="wrap" id="install" aria-labelledby="install-h">
  <h2 id="install-h">Install on a phone</h2>
  <div class="two-col">
    <div class="panel">
      <h3>1. Download</h3>
      <p>Grab a game's <strong>.jar</strong> from its page or the <a href="download/">download list</a>, or every game
      at once in the <a href="{zip}">all-games ZIP</a>.</p>
      <h3>2. Copy to the phone</h3>
      <p>USB mass storage, a memory card, Bluetooth, or a vendor PC suite all work. For over-the-air installs serve
      the .jad and .jar from your own plain-HTTP server.</p>
      <h3>3. Open it</h3>
      <p>Select the file on the phone and accept the install prompt. The game appears under Applications or Games.</p>
      <p><a class="btn small" href="docs/installing/">Full install guide</a></p>
    </div>
    <div class="panel">
      <h3>No phone? Use an emulator</h3>
      <p>MicroEmulator (desktop), J2ME Loader (Android), KEmulator and FreeJ2ME all run JAR files. The project runs
      every game on MicroEmulator automatically.</p>
      <p><a class="btn small" href="docs/emulator/">Emulator guide</a></p>
      <h3>Requirements</h3>
      <p>MIDP 2.0 and CLDC 1.0 or later, about 512 KB free heap, any screen from 128x128 to 240x320.</p>
      <p><a class="btn small" href="docs/supported-devices/">Supported devices</a></p>
    </div>
  </div>
</section>

<section class="wrap" id="compatibility" aria-labelledby="compat-h">
  <h2 id="compat-h">Compatibility, honestly</h2>
  <p class="section-intro">We only claim what has actually been checked. These numbers are generated from the
  test reports in the repository.</p>
  <div class="levels">
    {lv_build}
    {lv_ref}
    {lv_emu}
    {lv_dev}
  </div>
  <p class="notice" style="margin-top:16px">{device_note}</p>
  <p><a href="docs/verification/">Per-game verification table and test methods</a></p>
</section>

<section class="wrap" id="technology" aria-labelledby="tech-h">
  <h2 id="tech-h">Under the hood</h2>
  <div class="two-col">
    <div class="panel">
      <h3>Built for 2005 phones</h3>
      <p>Java 1.3 language level, compiled by ECJ straight to preverified CLDC class files against the CLDC 1.0 and
      MIDP 2.0 APIs, so nothing a phone lacks can sneak in. No floating point: positions are fixed-point integers,
      trigonometry comes from a 64-entry sine table.</p>
      <p>A shared framework, <code>gamekit</code>, provides the game loop, menus, input latching, fonts, sound and
      high scores, and is compiled into each JAR so every game stands alone.</p>
    </div>
    <div class="panel">
      <h3>Tested and playable everywhere</h3>
      <p>A headless MIDP runtime written for this project runs each real JAR at five screen sizes. The same runtime,
      compiled to JavaScript with TeaVM together with the game's own source, powers the
      <strong>Browser Demos</strong> on this site. They are previews, not the original Java ME version.</p>
      <p>Every JAR is also smoke-tested on the independent MicroEmulator 2.0.4.</p>
    </div>
  </div>
</section>

<div class="phone-menu" id="phone-menu" aria-hidden="true" role="dialog" aria-modal="true" aria-label="Phone menu mode">
  <button class="btn small pm-close" type="button">Close</button>
  <div class="phone">
    <div class="speaker"></div>
    <div class="screen">
      <div class="pm-screen" tabindex="0" aria-label="Game menu. Use arrow keys, 5 or Enter to open, Escape to close.">
        <div class="pm-title">GAMES <span class="pm-page"></span></div>
        <div class="pm-grid"></div>
        <div class="pm-foot"><span>Select</span><span class="pm-name" aria-live="polite" style="overflow:hidden;text-overflow:ellipsis"></span><span>Back</span></div>
      </div>
    </div>
    <div class="softrow">
      <button type="button" data-pm="ok">Select</button>
      <div class="dpad">
        <span></span><button type="button" data-pm="up" aria-label="Up">&#9650;</button><span></span>
        <button type="button" data-pm="left" aria-label="Left">&#9664;</button><button type="button" class="c" data-pm="ok" aria-label="Open">OK</button><button type="button" data-pm="right" aria-label="Right">&#9654;</button>
        <span></span><button type="button" data-pm="down" aria-label="Down">&#9660;</button><span></span>
      </div>
      <button type="button" data-pm="back">Back</button>
    </div>
  </div>
</div>
<script>window.GAMES = {pm_games};</script>
""".format(zip=data["collection"]["zip"], zipname="ZIP", stats=stats_html, hero_slug=hero["slug"],
           hero_name=e(hero["name"]), hero_name_upper=e(hero["name"].upper()),
           featured="".join(card(g, "", lazy=False) for g in featured), total=total, cat_buttons=cat_buttons,
           cards="".join(card(g, "") for g in games),
           lv_build=level(built, "Build verified", "Compiled against the CLDC 1.0 / MIDP 2.0 API; JAR and JAD produced.", "ok"),
           lv_ref=level(ref, "Reference runtime", "Real JAR run automatically at all five screen sizes on the project's headless MIDP runtime.", "ok"),
           lv_emu=level(emu, "Emulator verified", "Automated smoke test on MicroEmulator 2.0.4: launch, start, random keypad input, no errors.", "ok"),
           lv_dev=level(dev, "Real device verified", "Reported working on physical phone hardware.", "ok"),
           device_note=("No game has been tested on a real phone yet. If you have a Java phone, "
                        '<a href="%s/issues/new?template=device-report.yml">please report how it went</a>.' % REPO)
           if dev == 0 else "%d games have real-device reports." % dev,
           pm_games=pm_games.replace("</", "<\\/"))
    return layout("", "", "J2ME 100 Games: original games for Java ME button phones",
                  "100 original retro games for classic Java ME / J2ME button phones, with JAR/JAD downloads and "
                  "browser demos.", body, current="games")


def short_name(name):
    return name if len(name) <= 9 else max(name.split(), key=len)[:9]


def key_label(k):
    return "".join("<kbd>%s</kbd>" % e(p) if p.strip() else e(p) for p in re.split(r"([/,+ ])", k) if p != "")


def keypad_html():
    keys = [("1", 49, ""), ("2", 50, "ABC"), ("3", 51, "DEF"), ("4", 52, "GHI"), ("5", 53, "JKL"), ("6", 54, "MNO"),
            ("7", 55, "PQRS"), ("8", 56, "TUV"), ("9", 57, "WXYZ"), ("*", 42, "PAUSE"), ("0", 48, "SPACE"), ("#", 35, "")]
    pad = "".join('<button type="button" data-key="%d" aria-label="Key %s">%s<small>%s</small></button>'
                  % (c, e(k), e(k), s or "&nbsp;") for k, c, s in keys)
    return """<div class="softrow">
  <button type="button" data-key="-6" aria-label="Left soft key">&#9644;</button>
  <div class="dpad">
    <span></span><button type="button" data-key="-1" aria-label="Up">&#9650;</button><span></span>
    <button type="button" data-key="-3" aria-label="Left">&#9664;</button><button type="button" class="c" data-key="-5" aria-label="Fire">OK</button><button type="button" data-key="-4" aria-label="Right">&#9654;</button>
    <span></span><button type="button" data-key="-2" aria-label="Down">&#9660;</button><span></span>
  </div>
  <button type="button" data-key="-7" aria-label="Right soft key">&#9644;</button>
</div>
<div class="keys">%s</div>""" % pad


def game_page(g, games, idx, has_demo):
    rr = "../../"
    v = g["verification"]
    prev_g = games[idx - 1] if idx > 0 else None
    next_g = games[idx + 1] if idx + 1 < len(games) else None
    controls = "".join("<tr><td>%s</td><td>%s</td></tr>" % (key_label(c["key"]), e(c["action"])) for c in g["controls"])
    modes = ("<p><strong>Modes:</strong> %s</p>" % e(", ".join(g["modes"]))) if g["modes"] else ""
    tags = " ".join('<span class="chip">%s</span>' % e(t) for t in g["tags"])
    demo = has_demo(g)
    microemu = os.path.exists(os.path.join(ROOT, "games", "%s-%s" % (g["id"], g["slug"]), "media", "microemu.png"))
    vt = [
        ("Build verified", v["build"], "Compiled against CLDC 1.0 / MIDP 2.0; JAR + JAD generated"),
        ("Reference runtime", v["reference"], "Passed at " + ", ".join(v["referenceSizes"]) if v["reference"] else "Not run"),
        ("Emulator verified", v["emulator"], "Automated smoke test on MicroEmulator 2.0.4 (176x220)" if v["emulator"] else "Not yet"),
        ("Real device verified", bool(v["devices"]), ", ".join(v["devices"]) if v["devices"] else "Not yet tested on real hardware"),
    ]
    vrows = "".join('<tr><td>%s</td><td>%s</td><td>%s</td></tr>' % (
        a, '<span class="badge ok">Yes</span>' if ok else '<span class="badge no">No</span>', e(t)) for a, ok, t in vt)
    if demo:
        sim = """<div class="sim-wrap" id="sim" data-slug="{slug}" data-src="game.js">
  <div class="sim-tag"><span class="badge warn">Browser Demo</span><span class="badge no">not the original Java ME version</span></div>
  <div class="phone">
    <div class="speaker"></div>
    <div class="screen">
      <canvas tabindex="0" aria-label="{name} browser demo screen" width="176" height="208"></canvas>
      <div class="sim-overlay">
        <button class="btn primary" type="button" data-start>&#9654; Play demo</button>
        <p>Runs in your browser. Sound on.</p>
      </div>
    </div>
    {pad}
  </div>
  <div class="sim-tools">
    <label class="sr-only" for="size">Screen size</label>
    <select name="size" id="size">{opts}</select>
    <button type="button" data-sound aria-pressed="true">Sound: on</button>
  </div>
  <p class="sim-status" role="status" aria-live="polite" style="font-size:13px;color:var(--muted);text-align:center"></p>
  <p style="font-size:13px;color:var(--muted)">Keyboard: arrow keys move, <kbd>Enter</kbd> = 5, digits are digits,
  <kbd>*</kbd>/<kbd>P</kbd> pause, <kbd>#</kbd>, <kbd>Esc</kbd>/<kbd>E</kbd> soft keys.</p>
</div>""".format(slug=g["slug"], name=e(g["name"]), pad=keypad_html(),
                 opts="".join('<option value="%s">%s</option>' % (s, s) for s in catalog.RESOLUTIONS))
        scripts = ("assets/js/j2me-runtime.js", "assets/js/sim.js")
    else:
        sim = ('<div class="sim-wrap"><div class="panel"><strong>Browser Demo not available</strong><p>The demo for '
               'this game has not been built for this site version. The JAR download works on phones and '
               'emulators.</p></div></div>')
        scripts = ()
    shots = """<div class="shots">
  <figure><img src="{rr}media/{slug}/title.png" alt="{name} title screen" width="176" height="208"><figcaption>Title screen</figcaption></figure>
  <figure><img src="{rr}media/{slug}/play.png" alt="{name} gameplay" width="176" height="208" loading="lazy"><figcaption>Gameplay</figcaption></figure>
  {me}
</div>
<p style="font-size:13px;color:var(--muted)">Screenshots captured from the real JAR at 176x208 on the project's reference runtime{me_note}.</p>""".format(
        rr=rr, slug=g["slug"], name=e(g["name"]),
        me=('<figure><img src="%smedia/%s/microemu.png" alt="%s running in MicroEmulator" width="176" height="220" '
            'loading="lazy"><figcaption>In MicroEmulator 2.0.4</figcaption></figure>' % (rr, g["slug"], e(g["name"])))
        if microemu else "",
        me_note="; the third image is rendered by MicroEmulator 2.0.4" if microemu else "")
    body = """
<div class="wrap">
  <div class="detail-head">
    <div>
      <p class="crumbs"><a href="{rr}">Home</a> / <a href="{rr}?cat={cat}#catalogue">{cat}</a> / #{id}</p>
      <h1>{name}</h1>
      <p style="font-size:18px;color:var(--muted);max-width:720px">{summary}</p>
      <p><span class="chip cat" style="--cat:{color}">{cat}</span> {badge} <span class="chip">v{version}</span> <span class="chip">{kb}</span></p>
      <div class="downloads">
        <a class="btn primary" href="{jar}">Download JAR</a>
        <a class="btn" href="{jad}">Download JAD</a>
        <a class="btn" href="{release}">Release page</a>
        <a class="btn" href="{source}">Source code</a>
      </div>
      <p style="font-size:13px;color:var(--muted)">The JAR and JAD are the <strong>Original Java ME Version</strong> for phones and emulators.
      <a href="{rr}docs/installing/">How to install</a>.</p>
    </div>
  </div>
  <div class="detail">
    <div>
      <h2>About</h2>
      <p>{description}</p>
      <p><strong>Objective:</strong> {objective}</p>
      {modes}
      <p>{tags}</p>
      {shots}
      <h2>Controls</h2>
      <div class="table-scroll"><table><thead><tr><th>Key</th><th>Action</th></tr></thead><tbody>{controls}</tbody></table></div>
      <p style="font-size:14px;color:var(--muted)">In every game: <kbd>5</kbd> selects in menus, <kbd>*</kbd> pauses, and on the title screen
      <kbd>#</kbd> opens help and <kbd>0</kbd> toggles sound. The joystick and 2/4/6/8 are interchangeable.</p>
      <h2>Verification</h2>
      <div class="table-scroll"><table><thead><tr><th>Level</th><th>Status</th><th>Details</th></tr></thead><tbody>{vrows}</tbody></table></div>
      <p style="font-size:14px">Tried it on a real phone? <a href="{repo}/issues/new?template=device-report.yml">Send a device report</a>.</p>
      <h2>Technical details</h2>
      <div class="table-scroll"><table><tbody>
        <tr><th>MIDlet</th><td><code>{midlet}</code></td></tr>
        <tr><th>Platform</th><td>Java ME, CLDC 1.0, MIDP 2.0</td></tr>
        <tr><th>JAR size</th><td>{kb}</td></tr>
        <tr><th>Screens</th><td>128x128, 128x160, 176x208, 176x220, 240x320</td></tr>
        <tr><th>Storage</th><td>RMS record store <code>gk</code> (settings, best score)</td></tr>
      </tbody></table></div>
    </div>
    <aside aria-label="Browser demo">{sim}</aside>
  </div>
  <nav class="pager" aria-label="More games">
    {prev}
    <a class="btn" href="{rr}#catalogue">All games</a>
    {next}
  </nav>
</div>
""".format(rr=rr, cat=e(g["category"]), id=g["id"], name=e(g["name"]), summary=e(g["summary"]),
           color=CAT_COLORS.get(g["category"], "#4de1ff"), badge=badge_for(v), version=e(g["version"]),
           kb=size_kb(g["jarSize"]), jar=e(g["downloads"]["jar"]), jad=e(g["downloads"]["jad"]),
           release=e(g["downloads"]["release"]), source=e(g["source"]), description=e(g["description"]),
           objective=e(g["objective"]), modes=modes, tags=tags, shots=shots, controls=controls, vrows=vrows,
           repo=REPO, midlet=e(g["midlet"]), sim=sim,
           prev=('<a class="btn" href="%sgames/%s/" rel="prev">&larr; %s</a>' % (rr, prev_g["slug"], e(prev_g["name"]))) if prev_g else "<span></span>",
           next=('<a class="btn" href="%sgames/%s/" rel="next">%s &rarr;</a>' % (rr, next_g["slug"], e(next_g["name"]))) if next_g else "<span></span>")
    return layout(rr, "games/%s/" % g["slug"], "%s (%s game for Java ME phones)" % (g["name"], g["category"]),
                  g["summary"], body, image=SITE + "/media/%s/title.png" % g["slug"], scripts=scripts)


def download_page(data, games):
    rr = "../"
    rows = []
    for cat in catalog.CATEGORIES:
        gs = [g for g in games if g["category"] == cat]
        if not gs:
            continue
        rows.append('<h2 id="%s">%s</h2><div class="table-scroll"><table><thead><tr><th>#</th><th>Game</th><th>JAR</th>'
                    '<th>JAD</th><th>Size</th><th>Release</th></tr></thead><tbody>' % (cat.lower(), cat))
        for g in gs:
            rows.append('<tr><td>%s</td><td><a href="%sgames/%s/">%s</a></td><td><a href="%s">%s.jar</a></td>'
                        '<td><a href="%s">%s.jad</a></td><td>%s</td><td><a href="%s">%s</a></td></tr>' % (
                            g["id"], rr, g["slug"], e(g["name"]), e(g["downloads"]["jar"]), e(g["jar"]),
                            e(g["downloads"]["jad"]), e(g["jar"]), size_kb(g["jarSize"]),
                            e(g["downloads"]["release"]), "game-%s-v%s" % (g["id"], g["version"])))
        rows.append("</tbody></table></div>")
    body = """<div class="wrap doc">
<h1>Download</h1>
<p>Each game is its own GitHub Release with a JAR and a JAD. Most phones only need the <strong>JAR</strong>; the JAD is
the descriptor used for over-the-air installs and by some emulators.</p>
<p><a class="btn amber" href="{zip}">Download all {n} games (ZIP)</a> <a class="btn" href="{repo}/releases">All releases</a></p>
<p class="notice">These files are the original Java ME versions. They have been build-verified and smoke-tested on
MicroEmulator but not yet on real phones; see <a href="{rr}docs/verification/">verification</a>.</p>
{rows}
</div>""".format(zip=e(data["collection"]["zip"]), n=len(games), repo=REPO, rr=rr, rows="".join(rows))
    return layout(rr, "download/", "Download", "Download JAR and JAD files for all 100 J2ME games.", body,
                  current="download")


# --- tiny markdown renderer for docs pages -------------------------------------------------------

def md_inline(text, link_fix):
    parts = re.split(r"(`[^`]+`)", text)
    out = []
    for p in parts:
        if p.startswith("`") and p.endswith("`") and len(p) > 1:
            out.append("<code>%s</code>" % e(p[1:-1]))
            continue
        if p.lstrip().startswith("<") and re.match(r"\s*</?(img|sub|p|br|kbd)\b", p):
            out.append(p)
            continue
        s = e(p, quote=False)
        s = re.sub(r"!\[([^\]]*)\]\(([^)]+)\)", lambda m: '<img src="%s" alt="%s">' % (link_fix(m.group(2)), m.group(1)), s)
        s = re.sub(r"\[([^\]]+)\]\(([^)]+)\)", lambda m: '<a href="%s">%s</a>' % (link_fix(m.group(2)), m.group(1)), s)
        s = re.sub(r"\*\*([^*]+)\*\*", r"<strong>\1</strong>", s)
        s = re.sub(r"(?<![\w*])\*([^*\s][^*]*)\*(?![\w*])", r"<em>\1</em>", s)
        out.append(s)
    return "".join(out)


def md_to_html(md, link_fix):
    lines = md.replace("\r\n", "\n").split("\n")
    out, i = [], 0
    para = []

    def flush():
        if para:
            out.append("<p>%s</p>" % md_inline(" ".join(para), link_fix))
            del para[:]

    while i < len(lines):
        line = lines[i]
        st = line.strip()
        if st.startswith("```"):
            flush()
            lang = st[3:].strip()
            i += 1
            code = []
            while i < len(lines) and not lines[i].strip().startswith("```"):
                code.append(lines[i])
                i += 1
            i += 1
            out.append('<pre><code%s>%s</code></pre>' % (' class="language-%s"' % e(lang) if lang else "", e("\n".join(code))))
            continue
        m = re.match(r"^(#{1,4})\s+(.*)$", st)
        if m:
            flush()
            lvl = len(m.group(1))
            txt = m.group(2)
            anchor = re.sub(r"[^a-z0-9]+", "-", txt.lower()).strip("-")
            out.append('<h%d id="%s">%s</h%d>' % (lvl, anchor, md_inline(txt, link_fix), lvl))
            i += 1
            continue
        if st.startswith("|") and i + 1 < len(lines) and re.match(r"^\|?\s*:?-{2,}", lines[i + 1].strip()):
            flush()
            head = [c.strip() for c in st.strip("|").split("|")]
            i += 2
            rows = []
            while i < len(lines) and lines[i].strip().startswith("|"):
                rows.append([c.strip() for c in lines[i].strip().strip("|").split("|")])
                i += 1
            t = ['<div class="table-scroll"><table><thead><tr>']
            t += ["<th>%s</th>" % md_inline(h, link_fix) for h in head]
            t.append("</tr></thead><tbody>")
            for r in rows:
                t.append("<tr>%s</tr>" % "".join("<td>%s</td>" % md_inline(c, link_fix) for c in r))
            t.append("</tbody></table></div>")
            out.append("".join(t))
            continue
        if st.startswith(">"):
            flush()
            q = []
            while i < len(lines) and lines[i].strip().startswith(">"):
                q.append(lines[i].strip()[1:].strip())
                i += 1
            out.append("<blockquote><p>%s</p></blockquote>" % md_inline(" ".join(q), link_fix))
            continue
        lm = re.match(r"^(\s*)([*-]|\d+\.)\s+(.*)$", line)
        if lm:
            flush()
            ordered = lm.group(2)[0].isdigit()
            items = []
            while i < len(lines):
                lm = re.match(r"^(\s*)([*-]|\d+\.)\s+(.*)$", lines[i])
                if lm and len(lm.group(1)) < 2:
                    items.append([lm.group(3)])
                    i += 1
                elif items and lines[i].strip() and (lines[i].startswith("  ") or lines[i].startswith("\t")):
                    # continuation or nested block: keep code fences intact
                    if lines[i].strip().startswith("```"):
                        fence = [lines[i].strip()]
                        i += 1
                        while i < len(lines) and not lines[i].strip().startswith("```"):
                            fence.append(lines[i][3:] if lines[i].startswith("   ") else lines[i].lstrip())
                            i += 1
                        fence.append("```")
                        i += 1
                        items[-1].append("\n" + "\n".join(fence) + "\n")
                    else:
                        items[-1].append(lines[i].strip())
                        i += 1
                elif not lines[i].strip() and i + 1 < len(lines) and (lines[i + 1].startswith("   ") or re.match(r"^([*-]|\d+\.)\s", lines[i + 1])):
                    i += 1
                else:
                    break
            tag = "ol" if ordered else "ul"
            li = []
            for it in items:
                text_parts, blocks = [], []
                for part in it:
                    if part.startswith("\n"):
                        blocks.append(md_to_html(part, link_fix))
                    else:
                        text_parts.append(part)
                li.append("<li>%s%s</li>" % (md_inline(" ".join(text_parts), link_fix), "".join(blocks)))
            out.append("<%s>%s</%s>" % (tag, "".join(li), tag))
            continue
        if st.startswith("<"):
            flush()
            out.append(st)
            i += 1
            continue
        if st == "---":
            flush()
            out.append("<hr>")
            i += 1
            continue
        if not st:
            flush()
            i += 1
            continue
        para.append(st)
        i += 1
    flush()
    return "\n".join(out)


def make_link_fix(games_by_folder, rr):
    names = {d[0] for d in DOCS}

    def fix(url):
        if re.match(r"^[a-z]+:", url) or url.startswith("#"):
            return url
        path, _, frag = url.partition("#")
        frag = ("#" + frag) if frag else ""
        base = os.path.basename(path)
        if base.endswith(".md") and base[:-3] in names:
            return "%sdocs/%s/%s" % (rr, base[:-3], frag)
        if base == "download.md":
            return rr + "download/" + frag
        m = re.search(r"games/([^/]+)/README\.md$", path)
        if m and m.group(1) in games_by_folder:
            return "%sgames/%s/%s" % (rr, games_by_folder[m.group(1)], frag)
        resolved = os.path.normpath(os.path.join("docs", path)).replace(os.sep, "/")
        if resolved == "README.md":
            return rr
        return "%s/blob/main/%s%s" % (REPO, resolved, frag)
    return fix


def doc_page(name, label, title, games_by_folder):
    rr = "../../"
    with open(os.path.join(ROOT, "docs", name + ".md"), encoding="utf-8") as f:
        md = f.read()
    body_html = md_to_html(md, make_link_fix(games_by_folder, rr))
    side = " &middot; ".join('<a href="%sdocs/%s/"%s>%s</a>' % (rr, n, ' aria-current="page"' if n == name else "", l)
                             for n, l, _ in DOCS)
    body = '<div class="wrap doc"><p class="crumbs" style="margin-top:24px">%s</p>%s<p style="margin-top:40px;color:var(--muted);font-size:14px">' \
           'Source: <a href="%s/blob/main/docs/%s.md">docs/%s.md</a></p></div>' % (side, body_html, REPO, name, name)
    first = re.search(r"^#\s+(.*)$", md, re.M)
    desc = re.sub(r"\s+", " ", re.sub(r"[#>*`\[\]()|]", "", md.split("\n\n", 2)[1] if md.count("\n\n") > 1 else title))[:155]
    return layout(rr, "docs/%s/" % name, first.group(1) if first else title, desc.strip(), body,
                  current=name if name in ("installing", "verification", "building") else "")


def not_found_page():
    body = """<div class="wrap" style="text-align:center;padding:60px 0">
  <div class="phone" style="--w:176px;--h:208px">
    <div class="speaker"></div>
    <div class="screen"><div class="pm-screen" style="justify-content:center;align-items:center;font-size:22px;padding:12px">
      <div class="pm-title" style="align-self:stretch">ERROR 404</div>
      <p style="margin:auto 0">No signal.<br>This page is out of coverage.</p>
    </div></div>
    <p class="label">NO SERVICE</p>
  </div>
  <h1 style="margin-top:28px">Page not found</h1>
  <p>The page you dialled does not exist. <a href="{base}">Back to the games</a>.</p>
</div>""".format(base=BASE_PATH)
    return layout(BASE_PATH, "404.html", "Page not found", "This page does not exist.", body)


def favicon():
    return ('<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 22 30"><rect x="1" y="1" width="20" height="28" rx="4" '
            'fill="#172340" stroke="#4dff9a" stroke-width="1.5"/><rect x="4" y="4" width="14" height="10" '
            'fill="#4dff9a"/><g fill="#9aa8cc"><rect x="4" y="17" width="3" height="2"/><rect x="9.5" y="17" '
            'width="3" height="2"/><rect x="15" y="17" width="3" height="2"/><rect x="4" y="21" width="3" '
            'height="2"/><rect x="9.5" y="21" width="3" height="2"/><rect x="15" y="21" width="3" height="2"/></g></svg>\n')


def main():
    global OUT
    if "--out" in sys.argv:
        OUT = os.path.abspath(sys.argv[sys.argv.index("--out") + 1])
    with open(os.path.join(ROOT, "website", "games.json"), encoding="utf-8") as f:
        data = json.load(f)
    games = data["games"]
    if os.path.isdir(OUT):
        shutil.rmtree(OUT)
    shutil.copytree(os.path.join(SRC, "assets"), os.path.join(OUT, "assets"))
    write("assets/favicon.svg", favicon())
    games_by_folder = {"%s-%s" % (g["id"], g["slug"]): g["slug"] for g in games}
    demo_ok = {}
    for g in games:
        folder = os.path.join(ROOT, "games", "%s-%s" % (g["id"], g["slug"]))
        media = os.path.join(folder, "media")
        dst = os.path.join(OUT, "media", g["slug"])
        os.makedirs(dst, exist_ok=True)
        for name in ("title.png", "play.png", "demo.gif", "icon.png", "microemu.png"):
            p = os.path.join(media, name)
            if os.path.exists(p):
                shutil.copyfile(p, os.path.join(dst, name))
        js = os.path.join(folder, "build", "web", "game.js")
        demo_ok[g["slug"]] = os.path.exists(js)
        if demo_ok[g["slug"]]:
            os.makedirs(os.path.join(OUT, "games", g["slug"]), exist_ok=True)
            shutil.copyfile(js, os.path.join(OUT, "games", g["slug"], "game.js"))

    def has_demo(g):
        return demo_ok.get(g["slug"], False)

    write("index.html", index_page(data, games, has_demo))
    for i, g in enumerate(games):
        write("games/%s/index.html" % g["slug"], game_page(g, games, i, has_demo))
    write("download/index.html", download_page(data, games))
    for name, label, title in DOCS:
        write("docs/%s/index.html" % name, doc_page(name, label, title, games_by_folder))
    write("404.html", not_found_page())
    write(".nojekyll", "")
    write("robots.txt", "User-agent: *\nAllow: /\nSitemap: %s/sitemap.xml\n" % SITE)
    urls = [""] + ["games/%s/" % g["slug"] for g in games] + ["download/"] + ["docs/%s/" % d[0] for d in DOCS]
    today = date.today().isoformat()
    write("sitemap.xml", '<?xml version="1.0" encoding="UTF-8"?>\n<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">\n'
          + "".join("  <url><loc>%s/%s</loc><lastmod>%s</lastmod></url>\n" % (SITE, u, today) for u in urls) + "</urlset>\n")
    write("games.json", json.dumps(data, indent=1) + "\n")
    print("site written to %s: %d game pages, %d browser demos" % (OUT, len(games), sum(demo_ok.values())))


if __name__ == "__main__":
    main()
