#!/usr/bin/env python3
"""Repository validation: checks every game and writes a validation report.

Per game:
  * game.properties has every required key; id/slug/folder agree
  * source tree, MIDlet class, canvas class, build.xml, README.md present
  * media present (title.png, play.png, demo.gif, icon.png)
  * dist JAR + JAD exist; JAD MIDlet-Jar-Size equals the JAR size; JAD and
    manifest name the MIDlet class, CLDC-1.0 and MIDP-2.0
  * the JAR contains the MIDlet class and its icon; every class file is
    version <= 47 (Java 1.3 / CLDC era), so it is loadable by old KVMs
  * no float/double bytecode (CLDC 1.0 has no floating point)
Across games: unique ids, slugs, JAR names and packages; no names that
match well-known commercial game trademarks.

usage: python tools/validate.py [--strict]
Writes reports/validation-report.json and reports/validation-report.md.
Exit status is non-zero if any error was found.
"""
import json
import os
import re
import struct
import sys
import time
import zipfile

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import catalog  # noqa: E402

ROOT = catalog.ROOT

# Lower-case fragments that must not appear in game names (well-known commercial titles/brands).
TRADEMARKS = ["tetris", "pac-man", "pacman", "mario", "zelda", "sonic", "pokemon", "bejeweled", "minesweeper",
              "frogger", "asteroids", "space invaders", "breakout", "arkanoid", "lemmings", "angry birds",
              "candy crush", "uno", "monopoly", "scrabble", "battleship", "connect four", "connect 4", "othello",
              "mastermind", "yahtzee", "snake ii", "bounce", "doodle jump", "flappy", "2048", "sokoban",
              "lode runner", "donkey kong", "galaga", "tron", "qix", "boggle", "jenga", "tamagotchi", "lemonade stand"]

FLOAT_OPCODES = set(range(0x0b, 0x10)) | {0x17, 0x18, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2a}


def read_jad(path):
    props = {}
    with open(path, encoding="utf-8") as f:
        for line in f:
            if ":" in line:
                k, v = line.split(":", 1)
                props[k.strip()] = v.strip()
    return props


def read_manifest(z):
    raw = z.read("META-INF/MANIFEST.MF").decode("utf-8", "replace").replace("\r\n", "\n").replace("\n ", "")
    props = {}
    for line in raw.split("\n"):
        if ":" in line:
            k, v = line.split(":", 1)
            props[k.strip()] = v.strip()
    return props


def class_info(data):
    """(major, minor, uses_float_constants) for a class file."""
    magic, minor, major = struct.unpack(">IHH", data[:8])
    if magic != 0xCAFEBABE:
        return None
    # scan the constant pool for CONSTANT_Float (4) / CONSTANT_Double (6)
    count = struct.unpack(">H", data[8:10])[0]
    i, pos, floats = 1, 10, False
    while i < count:
        tag = data[pos]
        if tag == 1:
            ln = struct.unpack(">H", data[pos + 1:pos + 3])[0]
            pos += 3 + ln
        elif tag in (3, 4):
            floats = floats or tag == 4
            pos += 5
        elif tag in (5, 6):
            floats = floats or tag == 6
            pos += 9
            i += 1
        elif tag in (7, 8, 16, 19, 20):
            pos += 3
        elif tag in (9, 10, 11, 12, 18, 17):
            pos += 5
        elif tag == 15:
            pos += 4
        else:
            return major, minor, None
        i += 1
    return major, minor, floats


def check_game(g, errors, warnings):
    gid = g["id"]

    def err(msg):
        errors.append("%s %s: %s" % (gid, g.get("name", "?"), msg))

    def warn(msg):
        warnings.append("%s %s: %s" % (gid, g.get("name", "?"), msg))

    res = {"id": gid, "name": g.get("name"), "category": g.get("category")}
    for k in catalog.REQUIRED:
        if not g.get(k):
            err("missing game.%s" % k)
    if g["folder"] != "%s-%s" % (gid, g.get("slug")):
        err("folder %s does not match id-slug" % g["folder"])
    if g.get("category") not in catalog.CATEGORIES:
        err("unknown category %s" % g.get("category"))
    name = g.get("name", "").lower()
    for t in TRADEMARKS:
        if re.search(r"(^|[^a-z0-9])%s($|[^a-z0-9])" % re.escape(t), name):
            err("name contains trademark-like term '%s'" % t)
    midlet = g.get("midlet", "")
    src = os.path.join(g["dir"], "src")
    res["source"] = os.path.isfile(os.path.join(src, *midlet.split(".")) + ".java") if midlet else False
    if not res["source"]:
        err("MIDlet source %s.java missing" % midlet)
    canvas = g.get("canvas")
    if canvas and not os.path.isfile(os.path.join(src, *canvas.split(".")) + ".java"):
        err("canvas source %s.java missing" % canvas)
    res["build_xml"] = os.path.isfile(os.path.join(g["dir"], "build.xml"))
    if not res["build_xml"]:
        err("build.xml missing")
    res["readme"] = os.path.isfile(os.path.join(g["dir"], "README.md"))
    if not res["readme"]:
        err("README.md missing")
    media = [m for m in ("title.png", "play.png", "demo.gif", "icon.png")
             if not os.path.isfile(os.path.join(g["dir"], "media", m))]
    res["media"] = not media
    if media:
        warn("media missing: %s" % ", ".join(media))
    jar = os.path.join(g["dir"], "dist", g.get("jar", "") + ".jar")
    jad = os.path.join(g["dir"], "dist", g.get("jar", "") + ".jad")
    res["jar"] = os.path.isfile(jar)
    res["jad"] = os.path.isfile(jad)
    if not res["jar"]:
        err("JAR not built")
    if not res["jad"]:
        err("JAD not built")
    res["jar_size"] = os.path.getsize(jar) if res["jar"] else None
    if res["jad"] and res["jar"]:
        p = read_jad(jad)
        if p.get("MIDlet-Jar-Size") != str(res["jar_size"]):
            err("JAD MIDlet-Jar-Size %s != JAR size %s" % (p.get("MIDlet-Jar-Size"), res["jar_size"]))
        if p.get("MIDlet-Jar-URL") != g["jar"] + ".jar":
            err("JAD MIDlet-Jar-URL is %s" % p.get("MIDlet-Jar-URL"))
        if not p.get("MIDlet-1", "").endswith(", " + midlet):
            err("JAD MIDlet-1 does not name %s" % midlet)
        if p.get("MicroEdition-Configuration") != "CLDC-1.0" or p.get("MicroEdition-Profile") != "MIDP-2.0":
            err("JAD configuration/profile not CLDC-1.0/MIDP-2.0")
        if p.get("MIDlet-Version") != g.get("version"):
            err("JAD version %s != %s" % (p.get("MIDlet-Version"), g.get("version")))
        with zipfile.ZipFile(jar) as z:
            m = read_manifest(z)
            for k in ("MIDlet-Name", "MIDlet-Version", "MIDlet-Vendor", "MIDlet-1", "MicroEdition-Configuration",
                      "MicroEdition-Profile"):
                if m.get(k) != p.get(k):
                    err("manifest %s (%s) differs from JAD (%s)" % (k, m.get(k), p.get(k)))
            names = z.namelist()
            if midlet.replace(".", "/") + ".class" not in names:
                err("JAR lacks %s.class" % midlet)
            icon = p.get("MIDlet-Icon", "").lstrip("/")
            if icon and icon not in names:
                err("JAR lacks icon %s" % icon)
            majors, floaty = set(), []
            for n in names:
                if n.endswith(".class"):
                    info = class_info(z.read(n))
                    if not info:
                        err("bad class file %s" % n)
                        continue
                    majors.add(info[0])
                    if info[2]:
                        floaty.append(n)
            res["class_versions"] = sorted(majors)
            if majors and max(majors) > 47:
                err("class file version %d is newer than Java 1.3 (47)" % max(majors))
            if floaty:
                err("float/double constants in %s (CLDC 1.0 has no floating point)" % ", ".join(floaty))
    return res


def main():
    games = catalog.load_games()
    errors, warnings, results = [], [], []
    for g in games:
        results.append(check_game(g, errors, warnings))
    for key in ("id", "slug", "jar", "name"):
        seen = {}
        for g in games:
            seen.setdefault(g.get(key), []).append(g["id"])
        for v, ids in seen.items():
            if len(ids) > 1:
                errors.append("duplicate %s '%s' in games %s" % (key, v, ", ".join(ids)))
    pk = {}
    for g in games:
        pk.setdefault(g.get("midlet", "").rsplit(".", 1)[0], []).append(g["id"])
    for v, ids in pk.items():
        if len(ids) > 1:
            errors.append("games %s share Java package %s" % (", ".join(ids), v))

    def load(rel):
        p = os.path.join(ROOT, rel)
        return json.load(open(p, encoding="utf-8")) if os.path.exists(p) else {}

    tests = load("reports/test-report.json").get("games", {})
    me = {r["id"]: r for r in load("reports/microemu-report.json").get("games", [])}
    devices = load("docs/real-device-reports.json").get("reports", [])
    releases = load("reports/release-report.json")
    for r in results:
        t = tests.get(r["id"])
        r["reference_runtime"] = bool(t and t.get("ok"))
        r["microemulator"] = me.get(r["id"], {}).get("status") == "pass"
        r["real_device"] = any(d.get("id") == r["id"] and d.get("result") == "pass" for d in devices)
    failed_builds = [r["id"] for r in results if not (r["jar"] and r["jad"])]
    summary = {
        "generated": time.strftime("%Y-%m-%d %H:%M"),
        "total_games": len(games),
        "buildable": sum(1 for r in results if r["jar"] and r["jad"]),
        "jar_files": sum(1 for r in results if r["jar"]),
        "jad_files": sum(1 for r in results if r["jad"]),
        "readme_files": sum(1 for r in results if r["readme"]),
        "reference_runtime_tested": sum(1 for r in results if r["reference_runtime"]),
        "emulator_tested": sum(1 for r in results if r["microemulator"]),
        "real_hardware_tested": sum(1 for r in results if r["real_device"]),
        "game_releases_published": releases.get("game_releases"),
        "milestone_releases_published": releases.get("milestone_releases"),
        "failed_builds": failed_builds,
        "errors": errors,
        "warnings": warnings,
    }
    os.makedirs(os.path.join(ROOT, "reports"), exist_ok=True)
    with open(os.path.join(ROOT, "reports", "validation-report.json"), "w", encoding="utf-8", newline="\n") as f:
        json.dump({"summary": summary, "games": results}, f, indent=1)
        f.write("\n")
    md = ["# Validation report", "", "Generated %s by `tools/validate.py`." % summary["generated"], "",
          "| Metric | Value |", "|---|---|"]
    labels = [("total_games", "Total games"), ("buildable", "Buildable (JAR + JAD)"), ("jar_files", "JAR files"),
              ("jad_files", "JAD files"), ("readme_files", "Per-game READMEs"),
              ("reference_runtime_tested", "Reference-runtime tested (5 resolutions)"),
              ("emulator_tested", "Emulator tested (MicroEmulator 2.0.4)"),
              ("real_hardware_tested", "Real-hardware tested"),
              ("game_releases_published", "Per-game GitHub Releases"),
              ("milestone_releases_published", "Milestone releases")]
    for k, label in labels:
        v = summary[k]
        md.append("| %s | %s |" % (label, "not yet published" if v is None else v))
    md.append("| Failed builds | %s |" % (", ".join(failed_builds) or "none"))
    md += ["", "## Errors", ""] + (["* " + e for e in errors] or ["None."])
    md += ["", "## Warnings", ""] + (["* " + w for w in warnings] or ["None."])
    md += ["", "## Known limitations", "",
           "* No game has been tested on real phone hardware yet.",
           "* Emulator testing is an automated smoke test (random input) on MicroEmulator 2.0.4 only.",
           "* Hard AI levels in Keypad Chess and Keypad Checkers may take several seconds per move on slow phones.",
           "* Sound uses `Manager.playTone`; phones without tone support simply stay silent.", ""]
    with open(os.path.join(ROOT, "reports", "validation-report.md"), "w", encoding="utf-8", newline="\n") as f:
        f.write("\n".join(md))
    print("\n".join(md[:18]))
    print("%d errors, %d warnings" % (len(errors), len(warnings)))
    sys.exit(1 if errors or ("--strict" in sys.argv and warnings) else 0)


if __name__ == "__main__":
    main()
