#!/usr/bin/env python3
"""Game catalogue loader: games/*/game.properties is the single source of truth.

Used by the test runner, the validator and the documentation / website
generators. Keys lose their "game." prefix, so game.name -> game["name"].
"""
import os
import re

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
GAMES_DIR = os.path.join(ROOT, "games")

REPO_SLUG = "agneay/j2me-100-games"
REPO_URL = "https://github.com/" + REPO_SLUG
PAGES_URL = "https://agneay.github.io/j2me-100-games"
COLLECTION_VERSION = "1.0.0"

CATEGORIES = ["Arcade", "Puzzle", "Platformer", "Racing", "Strategy", "Board", "Card",
              "RPG", "Sports", "Simulation", "Experimental"]

RESOLUTIONS = ["128x128", "128x160", "176x208", "176x220", "240x320"]

REQUIRED = ["id", "slug", "name", "jar", "midlet", "category", "version", "summary",
            "description", "objective", "controls"]


def parse_properties(path):
    props = {}
    with open(path, encoding="utf-8") as f:
        for raw in f:
            line = raw.strip()
            if not line or line.startswith("#") or line.startswith("!"):
                continue
            m = re.match(r"([^=:]+?)\s*[=:]\s*(.*)$", line)
            if m:
                props[m.group(1).strip()] = m.group(2).strip()
    return props


def load_games():
    games = []
    for d in sorted(os.listdir(GAMES_DIR)):
        path = os.path.join(GAMES_DIR, d, "game.properties")
        if not os.path.isfile(path):
            continue
        raw = parse_properties(path)
        g = {k[5:] if k.startswith("game.") else k: v for k, v in raw.items()}
        g["dir"] = os.path.join(GAMES_DIR, d)
        g["folder"] = d
        g["controls_list"] = []
        for part in g.get("controls", "").split("|"):
            if ":" in part:
                k, v = part.split(":", 1)
                g["controls_list"].append((k.strip(), v.strip()))
        g["tags_list"] = [t.strip() for t in g.get("tags", "").split(",") if t.strip()]
        g["modes_list"] = [t.strip() for t in g.get("modes", "").split("|") if t.strip()]
        g["featured"] = g.get("featured", "false").lower() == "true"
        g["tag"] = "game-%s-v%s" % (g.get("id"), g.get("version"))
        games.append(g)
    return games


def release_url(g):
    return "%s/releases/tag/%s" % (REPO_URL, g["tag"])


def asset_url(g, ext):
    return "%s/releases/download/%s/%s.%s" % (REPO_URL, g["tag"], g["jar"], ext)


def source_url(g):
    return "%s/tree/main/games/%s" % (REPO_URL, g["folder"])


if __name__ == "__main__":
    for g in load_games():
        print(g["id"], g["name"], g["category"])
