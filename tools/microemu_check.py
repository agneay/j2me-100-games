#!/usr/bin/env python3
"""Independent smoke test of every built game on MicroEmulator 2.0.4.

This is separate from tools/run_tests.py, which uses the project's own
reference runtime. Here each JAD/JAR is loaded by the third-party
MicroEmulator in headless mode (its default 176x220 device), a game is
started with the 5 key and random keypad input is fed for a few seconds.
See tools/microemu/MicroEmuCheck.java for the exact pass criteria.

Usage: python tools/microemu_check.py [--jobs N] [--seconds S] [ids...]
Writes reports/microemu-report.json and per-game screenshots under
games/<game>/build/microemu/.
"""
import argparse
import concurrent.futures
import json
import os
import shutil
import subprocess
import sys
import tempfile
import time
from pathlib import Path

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import catalog  # noqa: E402

ROOT = Path(catalog.ROOT)
ME_JAR = ROOT / "tools" / "lib" / "microemu" / "microemulator-2.0.4.jar"
SRC = ROOT / "tools" / "microemu" / "MicroEmuCheck.java"
CLASSES = ROOT / "tools" / "build" / "microemu-check"


def java_cmd(name="java"):
    """MicroEmulator 2.0.4 is old; prefer a Java 21 runtime when one is configured."""
    env = os.environ.get("MICROEMU_JAVA") or os.environ.get("TEAVM_JAVA")
    if env:
        return env
    for p in (ROOT / "tools" / "jre21" / "bin" / "java.exe", ROOT / "tools" / "jre21" / "bin" / "java"):
        if p.exists():
            return str(p)
    return name


def compile_check():
    stamp = CLASSES / "MicroEmuCheck.class"
    if stamp.exists() and stamp.stat().st_mtime >= SRC.stat().st_mtime:
        return
    CLASSES.mkdir(parents=True, exist_ok=True)
    subprocess.run(["javac", "-nowarn", "--release", "8", "-cp", str(ME_JAR), "-d", str(CLASSES), str(SRC)],
                   check=True)


def check(g, seconds, work):
    dist = Path(g["dir"]) / "dist"
    jad = dist / (g["jar"] + ".jad")
    if not jad.exists():
        return {"id": g["id"], "name": g["name"], "status": "skipped", "detail": "not built"}
    # MicroEmulator's URL handling breaks on paths with spaces: run from a scratch copy.
    run_dir = Path(work) / g["id"]
    run_dir.mkdir(parents=True, exist_ok=True)
    shutil.copy(jad, run_dir)
    shutil.copy(dist / (g["jar"] + ".jar"), run_dir)
    url = (run_dir / jad.name).resolve().as_uri()
    out = Path(g["dir"]) / "build" / "microemu"
    if out.exists():
        shutil.rmtree(out)
    cmd = [java_cmd(), "-Djava.awt.headless=true", "-cp", os.pathsep.join([str(ME_JAR), str(CLASSES)]),
           "MicroEmuCheck", url, str(out), str(seconds)]
    t0 = time.time()
    try:
        p = subprocess.run(cmd, capture_output=True, text=True, timeout=seconds + 60)
        line = (p.stdout.strip().splitlines() or ["FAIL no output"])[-1]
    except subprocess.TimeoutExpired:
        line = "FAIL timed out"
    status = "pass" if line == "PASS" else "fail"
    return {"id": g["id"], "name": g["name"], "status": status,
            "detail": "" if status == "pass" else line[5:], "seconds": round(time.time() - t0, 1)}


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--jobs", type=int, default=4)
    ap.add_argument("--seconds", type=int, default=6)
    ap.add_argument("ids", nargs="*")
    a = ap.parse_args()
    if not ME_JAR.exists():
        sys.exit("MicroEmulator missing: run python tools/bootstrap.py")
    compile_check()
    games = [g for g in catalog.load_games() if not a.ids or g["id"] in a.ids]
    results = []
    with tempfile.TemporaryDirectory(prefix="microemu") as work:
        with concurrent.futures.ThreadPoolExecutor(a.jobs) as ex:
            futs = [ex.submit(check, g, a.seconds, work) for g in games]
            for f in concurrent.futures.as_completed(futs):
                r = f.result()
                results.append(r)
                print("%-4s %s %s %s" % (r["status"].upper(), r["id"], r["name"], r.get("detail", "")), flush=True)
    results.sort(key=lambda r: r["id"])
    report_path = ROOT / "reports" / "microemu-report.json"
    old = {}
    if report_path.exists() and a.ids:
        old = {r["id"]: r for r in json.loads(report_path.read_text(encoding="utf-8")).get("games", [])}
    for r in results:
        old[r["id"]] = r
    merged = [old[k] for k in sorted(old)]
    report_path.parent.mkdir(exist_ok=True)
    report_path.write_text(json.dumps({
        "emulator": "MicroEmulator 2.0.4 (headless, default 176x220 device)",
        "method": "load JAD, press 5, %d s of random keypad input; fail on exceptions, crashed game loop, blank screen" % a.seconds,
        "generated": time.strftime("%Y-%m-%d"),
        "games": merged,
    }, indent=1) + "\n", encoding="utf-8")
    bad = [r for r in results if r["status"] != "pass"]
    print("\n%d checked, %d passed, %d failed" % (len(results), len(results) - len(bad), len(bad)))
    sys.exit(1 if bad else 0)


if __name__ == "__main__":
    main()
