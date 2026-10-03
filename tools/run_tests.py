#!/usr/bin/env python3
"""Smoke-test built games on the headless reference runtime, in parallel.

Each game's JAR/JAD (games/*/dist) is run at five screen sizes with the
default key script (or game.test.script from game.properties). Results are
written to reports/test-report.json and summarised on stdout.

usage: python3 tools/run_tests.py [--jobs N] [--sizes 128x128,...] [IDS...]
  IDS  optional game ids/prefixes to test, e.g. 001 017
"""
import argparse
import concurrent.futures as cf
import json
import os
import subprocess
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
sys.path.insert(0, os.path.join(ROOT, "tools"))
import catalog  # noqa: E402

SIZES = "128x128,128x160,176x208,176x220,240x320"


def run_one(game, sizes):
    jad = os.path.join(game["dir"], "dist", game["jar"] + ".jad")
    if not os.path.exists(jad):
        return game, {"ok": False, "results": [], "error": "not built"}
    out = os.path.join(game["dir"], "build", "test")
    report = os.path.join(out, "report.json")
    cmd = ["java", "-Djava.awt.headless=true", "-cp", os.path.join(ROOT, "tools", "build", "emu-desktop.jar"),
           "emu.desktop.Harness", "--jad", jad, "--out", out, "--report", report, "--sizes", sizes,
           "--script", game.get("test.script") or "default"]
    try:
        p = subprocess.run(cmd, capture_output=True, text=True, timeout=600)
    except subprocess.TimeoutExpired:
        return game, {"ok": False, "results": [], "error": "timeout"}
    try:
        with open(report, encoding="utf-8") as f:
            data = json.load(f)
    except (OSError, ValueError):
        data = {"ok": False, "results": [], "error": (p.stderr or p.stdout)[-800:]}
    if not data.get("ok"):
        data["log"] = (p.stdout + p.stderr)[-2000:]
    return game, data


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--jobs", type=int, default=max(2, (os.cpu_count() or 4) // 2))
    ap.add_argument("--sizes", default=SIZES)
    ap.add_argument("ids", nargs="*")
    a = ap.parse_args()
    games = [g for g in catalog.load_games() if not a.ids or any(g["id"].startswith(i) for i in a.ids)]
    results = {}
    failed = []
    with cf.ThreadPoolExecutor(max_workers=a.jobs) as ex:
        for game, data in ex.map(lambda g: run_one(g, a.sizes), games):
            results[game["id"]] = data
            status = "PASS" if data.get("ok") else "FAIL"
            print("%s %s %s" % (status, game["id"], game["name"]), flush=True)
            if not data.get("ok"):
                failed.append(game["id"])
                for r in data.get("results", []):
                    if not r.get("ok"):
                        print("     %s: %s" % (r.get("size"), r.get("error")))
                if data.get("error"):
                    print("     " + str(data["error"])[:300])
    os.makedirs(os.path.join(ROOT, "reports"), exist_ok=True)
    path = os.path.join(ROOT, "reports", "test-report.json")
    existing = {}
    if a.ids and os.path.exists(path):
        with open(path, encoding="utf-8") as f:
            existing = json.load(f).get("games", {})
    existing.update(results)
    with open(path, "w", encoding="utf-8") as f:
        json.dump({"sizes": a.sizes.split(","), "games": existing}, f, indent=1, sort_keys=True)
    print("\n%d tested, %d passed, %d failed" % (len(results), len(results) - len(failed), len(failed)))
    sys.exit(1 if failed else 0)


if __name__ == "__main__":
    main()
