#!/usr/bin/env python3
"""Create the boilerplate for games that have a game.properties.

For every games/NNN-slug/ with a game.properties this writes, if missing:
  build.xml                         3-line Ant file importing tools/ant/game-build.xml
  src/<pkg>/<Name>MIDlet.java       MIDlet returning new <game.canvas>()

game.canvas must name the Game subclass, e.g. game.canvas=pixelpaddle.PaddleGame

usage: python3 tools/scaffold.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import catalog  # noqa: E402

BUILD = """<?xml version="1.0" encoding="UTF-8"?>
<project name="game-%s" default="dist" basedir=".">
    <import file="../../tools/ant/game-build.xml"/>
</project>
"""

MIDLET = """package %(pkg)s;

import gamekit.Game;
import gamekit.GameMIDlet;

/** %(name)s - MIDlet entry point. */
public class %(cls)s extends GameMIDlet {
    protected Game createGame() {
        return new %(canvas)s();
    }
}
"""


def main():
    made = 0
    for g in catalog.load_games():
        b = os.path.join(g["dir"], "build.xml")
        if not os.path.exists(b):
            with open(b, "w", encoding="utf-8", newline="\n") as f:
                f.write(BUILD % g["id"])
            made += 1
        pkg, cls = g["midlet"].rsplit(".", 1)
        m = os.path.join(g["dir"], "src", *pkg.split("."), cls + ".java")
        if not os.path.exists(m):
            canvas = g.get("canvas")
            if not canvas:
                print("skip %s: no game.canvas" % g["id"])
                continue
            canvas = canvas.split(".")[-1] if canvas.rsplit(".", 1)[0] == pkg else canvas
            os.makedirs(os.path.dirname(m), exist_ok=True)
            with open(m, "w", encoding="utf-8", newline="\n") as f:
                f.write(MIDLET % {"pkg": pkg, "cls": cls, "canvas": canvas, "name": g["name"]})
            made += 1
    print("scaffolded %d files" % made)


if __name__ == "__main__":
    main()
