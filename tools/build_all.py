#!/usr/bin/env python3
"""Run an Ant target for many games in parallel (faster than `ant dist`).

usage: python3 tools/build_all.py [--target dist|web|media] [--jobs N] [IDS...]
"""
import argparse
import concurrent.futures as cf
import os
import subprocess
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
sys.path.insert(0, os.path.join(ROOT, "tools"))
import catalog  # noqa: E402

LIB = os.path.join(ROOT, "tools", "lib")
ANT_CP = os.pathsep.join([os.path.join(LIB, "ant-1.10.15.jar"), os.path.join(LIB, "ant-launcher-1.10.15.jar")])


def ant(game_dir, target):
    cmd = ["java", "-cp", ANT_CP, "org.apache.tools.ant.Main", "-f", os.path.join(game_dir, "build.xml"), target]
    p = subprocess.run(cmd, capture_output=True, text=True)
    return p.returncode, (p.stdout + p.stderr)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--target", default="dist")
    ap.add_argument("--jobs", type=int, default=max(2, (os.cpu_count() or 4) // 2))
    ap.add_argument("ids", nargs="*")
    a = ap.parse_args()
    if not os.path.exists(os.path.join(ROOT, "tools", "build", "emu-desktop.jar")):
        subprocess.run(["java", "-cp", ANT_CP, "org.apache.tools.ant.Main", "-q", "-f",
                        os.path.join(ROOT, "build.xml"), "tools"], check=True)
    games = [g for g in catalog.load_games() if not a.ids or any(g["id"].startswith(i) for i in a.ids)]
    failed = []
    with cf.ThreadPoolExecutor(max_workers=a.jobs) as ex:
        futs = {ex.submit(ant, g["dir"], a.target): g for g in games}
        for fut in cf.as_completed(futs):
            g = futs[fut]
            rc, out = fut.result()
            if rc == 0:
                print("ok    %s %s" % (g["id"], g["name"]), flush=True)
            else:
                failed.append(g["id"])
                print("FAIL  %s %s\n%s" % (g["id"], g["name"], out[-3000:]), flush=True)
    print("\n%s: %d ok, %d failed %s" % (a.target, len(games) - len(failed), len(failed), sorted(failed)))
    sys.exit(1 if failed else 0)


if __name__ == "__main__":
    main()
