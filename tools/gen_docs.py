#!/usr/bin/env python3
"""Generate catalogue-driven documentation from games/*/game.properties.

  * games/<game>/media/       screenshots, demo GIF, icon and MicroEmulator shot
                              (copied from build output when present)
  * games/<game>/README.md    per-game page
  * README.md                 root README with the full catalogue table
  * docs/download.md          download page (per-game release assets + ZIP)
  * docs/verification.md      honest verification status of every game
  * website/games.json        data file the website generator reads

Verification facts come only from reports/test-report.json (project
reference runtime) and reports/microemu-report.json (MicroEmulator).
Nothing here claims real-hardware testing; that list lives in
docs/real-device-reports.json and is empty until someone actually reports.

usage: python tools/gen_docs.py [--no-media]
"""
import json
import os
import shutil
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import catalog  # noqa: E402
from catalog import PAGES_URL, REPO_URL, RESOLUTIONS, asset_url, release_url  # noqa: E402

ROOT = catalog.ROOT
ZIP_NAME = "j2me-100-games-v%s.zip" % catalog.COLLECTION_VERSION
ZIP_URL = "%s/releases/download/v%s/%s" % (REPO_URL, catalog.COLLECTION_VERSION, ZIP_NAME)

KEY_NAMES = {"*": "`*`", "#": "`#`"}


def load_json(rel, default):
    p = os.path.join(ROOT, rel)
    if os.path.exists(p):
        with open(p, encoding="utf-8") as f:
            return json.load(f)
    return default


def write(rel, text):
    p = os.path.join(ROOT, rel)
    os.makedirs(os.path.dirname(p), exist_ok=True)
    with open(p, "w", encoding="utf-8", newline="\n") as f:
        f.write(text)


def copy_media(g):
    src_media = os.path.join(g["dir"], "build", "media", "176x208")
    dst = os.path.join(g["dir"], "media")
    os.makedirs(dst, exist_ok=True)
    pairs = [
        (os.path.join(src_media, "title.png"), "title.png"),
        (os.path.join(src_media, "play.png"), "play.png"),
        (os.path.join(src_media, "demo.gif"), "demo.gif"),
        (os.path.join(g["dir"], "build", "res", "icon.png"), "icon.png"),
        (os.path.join(g["dir"], "build", "microemu", "microemu-play.png"), "microemu.png"),
    ]
    for s, name in pairs:
        if os.path.exists(s):
            shutil.copyfile(s, os.path.join(dst, name))


def jar_size(g):
    p = os.path.join(g["dir"], "dist", g["jar"] + ".jar")
    return os.path.getsize(p) if os.path.exists(p) else None


def verification(g, tests, me, devices):
    t = tests.get("games", {}).get(g["id"])
    m = {r["id"]: r for r in me.get("games", [])}.get(g["id"])
    dev = [d for d in devices.get("reports", []) if d.get("id") == g["id"]]
    built = jar_size(g) is not None
    ref_sizes = [r["size"] for r in (t or {}).get("results", []) if r.get("ok")]
    return {
        "build": built,
        "reference": bool(t and t.get("ok")),
        "reference_sizes": ref_sizes,
        "emulator": bool(m and m.get("status") == "pass"),
        "emulator_name": "MicroEmulator 2.0.4" if m else None,
        "device": [d.get("device") for d in dev if d.get("result") == "pass"],
    }


def level(v):
    if v["device"]:
        return "Real device verified"
    if v["emulator"]:
        return "Emulator verified"
    if v["build"]:
        return "Build verified"
    return "Not built"


def controls_md(g):
    rows = ["| Key | Action |", "|---|---|"]
    for k, v in g["controls_list"]:
        rows.append("| %s | %s |" % (k.replace("|", "/"), v))
    return "\n".join(rows)


def demo_url(g):
    return "%s/games/%s/" % (PAGES_URL, g["slug"])


def game_readme(g, v, idx, total):
    size = jar_size(g)
    lines = [
        "# %s" % g["name"],
        "",
        '<img src="media/icon.png" width="24" height="24" alt=""> **%s** &middot; Game %s of %d &middot; v%s'
        % (g["category"], g["id"], total, g["version"]),
        "",
        "> %s" % g["summary"],
        "",
        '<p><img src="media/title.png" alt="%s title screen" width="176"> '
        '<img src="media/play.png" alt="%s gameplay" width="176"> '
        '<img src="media/demo.gif" alt="%s gameplay animation" width="176"></p>' % (g["name"], g["name"], g["name"]),
        "",
        "<sub>Screenshots at 176x208, captured from the real JAR on the project's reference runtime.</sub>",
        "",
        "## About",
        "",
        g["description"],
        "",
        "**Objective:** " + g["objective"],
        "",
    ]
    if g["modes_list"]:
        lines += ["**Modes:** " + ", ".join(g["modes_list"]), ""]
    lines += [
        "## Controls",
        "",
        controls_md(g),
        "",
        "Every game also supports: `5` to select in menus, `*` to pause, `0` on the title screen to toggle sound, "
        "and `#` on the title screen for help. Joystick/D-pad and the 2/4/6/8 keys are interchangeable.",
        "",
        "## Download",
        "",
        "| File | Link |",
        "|---|---|",
        "| JAR (install this) | [%s.jar](%s) |" % (g["jar"], asset_url(g, "jar")),
        "| JAD (descriptor for OTA install) | [%s.jad](%s) |" % (g["jar"], asset_url(g, "jad")),
        "| Release page | [%s](%s) |" % (g["tag"], release_url(g)),
        "| All 100 games | [%s](%s) |" % (ZIP_NAME, ZIP_URL),
        "",
        "**Browser Demo:** [play in your browser](%s). The browser demo is the same Java source compiled to "
        "JavaScript with TeaVM and run on the project's MIDP reference runtime; it is a convenience preview, "
        "not the original Java ME version. For the authentic experience install the JAR on a phone or in an "
        "emulator." % demo_url(g),
        "",
        "Installation help: [docs/installing.md](../../docs/installing.md) and "
        "[docs/emulator.md](../../docs/emulator.md).",
        "",
        "## Technical details",
        "",
        "| | |",
        "|---|---|",
        "| MIDlet class | `%s` |" % g["midlet"],
        "| Platform | Java ME, CLDC 1.0, MIDP 2.0 |",
        "| JAR size | %s |" % ("%.1f KB" % (size / 1024.0) if size else "not built"),
        "| Designed for | %s |" % ", ".join(RESOLUTIONS),
        "| Storage | RMS record store `gk` (settings and best score) |",
        "| Floating point | none (integer maths only) |",
        "",
        "## Verification",
        "",
        "| Level | Status |",
        "|---|---|",
        "| Build verified | %s |" % ("Yes: compiled against the CLDC 1.0 / MIDP 2.0 API, JAR + JAD generated" if v["build"] else "No"),
        "| Reference runtime | %s |" % ("Passed at %s (automated, project's headless MIDP runtime)" % ", ".join(v["reference_sizes"]) if v["reference"] else "Not run"),
        "| Emulator verified | %s |" % ("Yes: automated smoke test on MicroEmulator 2.0.4 (176x220)" if v["emulator"] else "Not yet"),
        "| Real device verified | %s |" % (", ".join(v["device"]) if v["device"] else "Not yet tested on real hardware"),
        "",
        "Tested it on a real phone? Please [report it](%s/issues/new?template=device-report.yml) so this table "
        "can be updated honestly." % REPO_URL,
        "",
        "## Build from source",
        "",
        "```bash",
        "python tools/bootstrap.py",
        "tools/build-game.sh %s" % g["id"],
        "```",
        "",
        "Output: `games/%s/dist/%s.jar` and `.jad`. See [docs/building.md](../../docs/building.md)." % (g["folder"], g["jar"]),
        "",
        "---",
        "",
        "Part of [J2ME 100 Games](../../README.md). Original game, released under the [MIT License](../../LICENSE).",
        "",
    ]
    return "\n".join(lines)


def counts(games, vs):
    return {
        "total": len(games),
        "build": sum(1 for g in games if vs[g["id"]]["build"]),
        "reference": sum(1 for g in games if vs[g["id"]]["reference"]),
        "emulator": sum(1 for g in games if vs[g["id"]]["emulator"]),
        "device": sum(1 for g in games if vs[g["id"]]["device"]),
    }


def root_readme(games, vs):
    c = counts(games, vs)
    cats = {}
    for g in games:
        cats[g["category"]] = cats.get(g["category"], 0) + 1
    cat_line = " &middot; ".join("%s %d" % (k, cats[k]) for k in catalog.CATEGORIES if k in cats)
    out = [
        "# J2ME 100 Games",
        "",
        "**100 original retro games for classic Java ME / J2ME button phones.**",
        "",
        "[![Build](%s/actions/workflows/build.yml/badge.svg)](%s/actions/workflows/build.yml) "
        "[![Pages](%s/actions/workflows/pages.yml/badge.svg)](%s) "
        "[![License: MIT](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)" % (REPO_URL, REPO_URL, REPO_URL, PAGES_URL),
        "",
        "Every game is written from scratch in Java for CLDC 1.0 / MIDP 2.0, uses only integer maths, ships as a "
        "small JAR + JAD pair, and is playable with nothing but a 12-key keypad. Layouts adapt to 128x128, "
        "128x160, 176x208, 176x220 and 240x320 screens.",
        "",
        "* **Website and browser demos:** %s" % PAGES_URL,
        "* **Downloads:** [docs/download.md](docs/download.md) or the "
        "[all-games ZIP](%s)" % ZIP_URL,
        "* **Install on a phone or emulator:** [docs/installing.md](docs/installing.md), [docs/emulator.md](docs/emulator.md)",
        "",
        "%s" % cat_line,
        "",
        "## Verification status",
        "",
        "We separate what has been checked from what has not. See [docs/verification.md](docs/verification.md).",
        "",
        "| Level | Games | What it means |",
        "|---|---|---|",
        "| Build verified | %d / %d | Compiles against the CLDC 1.0 / MIDP 2.0 API; JAR + JAD produced |" % (c["build"], c["total"]),
        "| Reference runtime | %d / %d | Automated run of the real JAR on the project's headless MIDP runtime at all 5 resolutions (crash, stall and blank-screen checks) |" % (c["reference"], c["total"]),
        "| Emulator verified | %d / %d | Automated smoke test on the third-party MicroEmulator 2.0.4 (176x220): launch, start, random keypad input, no exceptions |" % (c["emulator"], c["total"]),
        "| Real device verified | %d / %d | Reported working on physical hardware |" % (c["device"], c["total"]),
        "",
        "No game has been tested on a real phone yet. If you try one, please "
        "[file a device report](%s/issues/new?template=device-report.yml)." % REPO_URL if c["device"] == 0 else
        "Real-device reports are listed in [docs/verification.md](docs/verification.md).",
        "",
        "## Catalogue",
        "",
        "| # | Game | Category | Description | Download | Browser demo |",
        "|---|---|---|---|---|---|",
    ]
    for g in games:
        out.append("| %s | [%s](games/%s/README.md) | %s | %s | [JAR](%s) &middot; [JAD](%s) | [Play](%s) |" % (
            g["id"], g["name"], g["folder"], g["category"], g["summary"].replace("|", "/"),
            asset_url(g, "jar"), asset_url(g, "jad"), demo_url(g)))
    out += [
        "",
        "## Quick start",
        "",
        "Requirements: JDK 17 or newer and Python 3.8+. Everything else (Ant, the Eclipse compiler, the CLDC/MIDP "
        "API stubs) is downloaded by the bootstrap script with pinned SHA-256 checksums.",
        "",
        "```bash",
        "python tools/bootstrap.py          # fetch the toolchain into tools/lib",
        "tools/build-all.sh                 # build all 100 JAR + JAD pairs (tools\\build-all.cmd on Windows)",
        "tools/build-game.sh 001            # build one game",
        "python tools/run_tests.py          # reference-runtime smoke tests",
        "python tools/microemu_check.py     # MicroEmulator smoke tests",
        "python tools/validate.py           # repository validation report",
        "tools/package.sh                   # release/ folder + all-games ZIP",
        "```",
        "",
        "More in [docs/building.md](docs/building.md) and [docs/development.md](docs/development.md).",
        "",
        "## Repository layout",
        "",
        "```",
        "common/src/gamekit/   shared MIDP game framework (canvas loop, menus, input, fonts, RMS, sound)",
        "games/NNN-slug/       one folder per game: src/, game.properties, build.xml, README.md, media/",
        "emu/core/             MIDP reference runtime used by the test harness and the browser demos",
        "emu/desktop/          headless harness, icon generator, contact sheets",
        "emu/web/              browser backend for the TeaVM demos",
        "tools/                bootstrap, build scripts, tests, validation, docs and website generators",
        "website/              GitHub Pages site source (generated into website/_site)",
        "docs/                 building, installing, emulators, devices, development, design guidelines",
        "```",
        "",
        "## Documentation",
        "",
        "* [Building](docs/building.md)",
        "* [Installing on phones](docs/installing.md)",
        "* [Running in emulators](docs/emulator.md)",
        "* [Supported devices](docs/supported-devices.md)",
        "* [Development guide](docs/development.md)",
        "* [Game design guidelines](docs/game-design-guidelines.md)",
        "* [Verification](docs/verification.md)",
        "* [Download](docs/download.md)",
        "",
        "## Contributing",
        "",
        "New games, fixes and especially real-device test reports are welcome. Read [CONTRIBUTING.md](CONTRIBUTING.md) "
        "and the [Code of Conduct](CODE_OF_CONDUCT.md). Security issues: see [SECURITY.md](SECURITY.md).",
        "",
        "## License and trademarks",
        "",
        "MIT, see [LICENSE](LICENSE). All games, code and graphics are original to this project. Game names are "
        "descriptive and are not affiliated with any commercial title. Java is a trademark of Oracle; Nokia and other "
        "phone brands mentioned in the docs are trademarks of their owners. This project is not affiliated with or "
        "endorsed by any of them.",
        "",
    ]
    return "\n".join(out)


def download_md(games):
    out = [
        "# Download",
        "",
        "Every game is published as its own GitHub Release with a JAR and a JAD file. Most phones only need the "
        "**JAR**; the JAD is the descriptor used for over-the-air (OTA) installs and by some emulators.",
        "",
        "**All games in one archive:** [%s](%s) (all 100 JAR + JAD pairs, the license and the install guide)." % (ZIP_NAME, ZIP_URL),
        "",
        "Browser demos for every game are on the [website](%s). See [installing.md](installing.md) for help." % PAGES_URL,
        "",
    ]
    for cat in catalog.CATEGORIES:
        gs = [g for g in games if g["category"] == cat]
        if not gs:
            continue
        out += ["## %s" % cat, "", "| # | Game | JAR | JAD | Size | Release |", "|---|---|---|---|---|---|"]
        for g in gs:
            s = jar_size(g)
            out.append("| %s | [%s](../games/%s/README.md) | [%s.jar](%s) | [%s.jad](%s) | %s | [%s](%s) |" % (
                g["id"], g["name"], g["folder"], g["jar"], asset_url(g, "jar"), g["jar"], asset_url(g, "jad"),
                "%.1f KB" % (s / 1024.0) if s else "-", g["tag"], release_url(g)))
        out.append("")
    return "\n".join(out)


def secs(me):
    import re
    m = re.search(r"(\d+) s of random", me.get("method", ""))
    return "%s seconds" % m.group(1) if m else "several seconds"


def verification_md(games, vs, tests, me, devices):
    c = counts(games, vs)
    out = [
        "# Verification",
        "",
        "This page states exactly what has and has not been checked. It is regenerated from the test reports by "
        "`tools/gen_docs.py`; nothing on it is written by hand.",
        "",
        "## Levels",
        "",
        "| Level | Count | Method |",
        "|---|---|---|",
        "| Build verified | %d / %d | Source compiled with ECJ (`-source 1.3 -target cldc1.1`, which emits preverified "
        "class files) against the CLDC 1.0 and MIDP 2.0 API stubs, so any call outside those APIs fails the build. "
        "JAR and JAD generated, with MIDlet-Jar-Size matching the JAR. |" % (c["build"], c["total"]),
        "| Reference runtime | %d / %d | `tools/run_tests.py`: the real JAR runs on the project's own headless MIDP "
        "implementation (emu/core) at 128x128, 128x160, 176x208, 176x220 and 240x320 with a scripted plus random key "
        "sequence. Fails on any uncaught exception, a crashed game loop, a stalled loop or a blank screen. |" % (c["reference"], c["total"]),
        "| Emulator verified | %d / %d | `tools/microemu_check.py`: the JAD/JAR is loaded by the independent "
        "MicroEmulator 2.0.4 in headless mode (default 176x220 device). The game is started with 5 and fed random "
        "keypad input for %s. Fails on exceptions, a crashed loop or a blank screen; screenshots are rendered by "
        "MicroEmulator itself. Errors from a missing sound device on the host (CI machines have none) are ignored. |" % (c["emulator"], c["total"], secs(me)),
        "| Real device verified | %d / %d | A person ran the game on physical hardware and reported it. |" % (c["device"], c["total"]),
        "",
        "### What this does not prove",
        "",
        "* Automated input is random, so a full playthrough of every game has not been verified by a machine.",
        "* Both runtimes are desktop Java. Real phones differ in heap size (some have well under 1 MB), CPU speed, "
        "fonts, key codes and sound support. The board-game AIs on their hardest settings may think for several "
        "seconds on slow phones.",
        "* No game has been run in Sun/Oracle WTK, the Nokia SDK emulators, KEmulator or J2ME Loader by the "
        "maintainers yet; those are good next steps and reports are welcome.",
        "",
        "## Per-game status",
        "",
        "| # | Game | Build | Reference runtime (5 sizes) | MicroEmulator | Real device | Level |",
        "|---|---|---|---|---|---|---|",
    ]
    for g in games:
        v = vs[g["id"]]
        out.append("| %s | %s | %s | %s | %s | %s | %s |" % (
            g["id"], g["name"], "yes" if v["build"] else "no",
            "pass (%d/5)" % len(v["reference_sizes"]) if v["reference"] else "-",
            "pass" if v["emulator"] else "-",
            ", ".join(v["device"]) if v["device"] else "not tested",
            level(v)))
    out += ["", "Report generated on %s (MicroEmulator report) from `reports/test-report.json` and "
            "`reports/microemu-report.json`." % me.get("generated", "unknown"), ""]
    return "\n".join(out)


def games_json(games, vs):
    data = []
    for g in games:
        v = vs[g["id"]]
        size = jar_size(g)
        data.append({
            "id": g["id"], "slug": g["slug"], "name": g["name"], "category": g["category"],
            "version": g["version"], "summary": g["summary"], "description": g["description"],
            "objective": g["objective"], "controls": [{"key": k, "action": a} for k, a in g["controls_list"]],
            "modes": g["modes_list"], "tags": g["tags_list"], "featured": g["featured"],
            "jar": g["jar"], "midlet": g["midlet"], "jarSize": size,
            "downloads": {"jar": asset_url(g, "jar"), "jad": asset_url(g, "jad"), "release": release_url(g)},
            "source": catalog.source_url(g),
            "verification": {"build": v["build"], "reference": v["reference"], "referenceSizes": v["reference_sizes"],
                             "emulator": v["emulator"], "emulatorName": v["emulator_name"], "devices": v["device"],
                             "level": level(v)},
        })
    return {"collection": {"name": "J2ME 100 Games", "version": catalog.COLLECTION_VERSION, "repo": REPO_URL,
                           "site": PAGES_URL, "zip": ZIP_URL, "resolutions": RESOLUTIONS,
                           "categories": catalog.CATEGORIES},
            "games": data}


def main():
    games = catalog.load_games()
    tests = load_json("reports/test-report.json", {})
    me = load_json("reports/microemu-report.json", {})
    devices = load_json("docs/real-device-reports.json", {"reports": []})
    if "--no-media" not in sys.argv:
        for g in games:
            copy_media(g)
    vs = {g["id"]: verification(g, tests, me, devices) for g in games}
    for i, g in enumerate(games):
        write(os.path.join("games", g["folder"], "README.md"), game_readme(g, vs[g["id"]], i, len(games)))
    write("README.md", root_readme(games, vs))
    write("docs/download.md", download_md(games))
    write("docs/verification.md", verification_md(games, vs, tests, me, devices))
    write("website/games.json", json.dumps(games_json(games, vs), indent=1) + "\n")
    print("docs generated for %d games" % len(games))


if __name__ == "__main__":
    main()
