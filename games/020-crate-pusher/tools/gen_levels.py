#!/usr/bin/env python3
"""Generate original Crate Pusher levels.

Levels are built by *reverse play*: crates start on their goals and the
player pulls them around, so every generated level is solvable by
construction. A breadth-first solver then measures the minimum number of
pushes; levels are sorted by that difficulty. Output is Java source for
Levels.java. Deterministic for a given seed.

usage: python3 gen_levels.py > ../src/cratepusher/Levels.java
"""
import random
import sys
from collections import deque

DIRS = [(0, -1), (1, 0), (0, 1), (-1, 0)]


def make_room(rng, w, h):
    g = [["#"] * w for _ in range(h)]
    for y in range(1, h - 1):
        for x in range(1, w - 1):
            g[y][x] = " "
    # sprinkle wall blocks
    for _ in range(rng.randint(4, 9)):
        bw, bh = rng.choice([(1, 1), (1, 2), (2, 1), (2, 2), (1, 3), (3, 1)])
        x, y = rng.randint(1, w - 1 - bw), rng.randint(1, h - 1 - bh)
        for yy in range(y, y + bh):
            for xx in range(x, x + bw):
                g[yy][xx] = "#"
    floors = [(x, y) for y in range(h) for x in range(w) if g[y][x] == " "]
    if not floors:
        return None
    # connectivity check
    seen = {floors[0]}
    q = deque([floors[0]])
    while q:
        x, y = q.popleft()
        for dx, dy in DIRS:
            n = (x + dx, y + dy)
            if g[n[1]][n[0]] == " " and n not in seen:
                seen.add(n)
                q.append(n)
    if len(seen) != len(floors) or len(floors) < 16:
        return None
    return g


def reachable(g, boxes, start):
    seen = {start}
    q = deque([start])
    while q:
        x, y = q.popleft()
        for dx, dy in DIRS:
            n = (x + dx, y + dy)
            if g[n[1]][n[0]] != "#" and n not in boxes and n not in seen:
                seen.add(n)
                q.append(n)
    return seen


def reverse_play(rng, g, goals, steps):
    boxes = set(goals)
    floors = [(x, y) for y in range(len(g)) for x in range(len(g[0])) if g[y][x] != "#" and (x, y) not in boxes]
    player = rng.choice(floors)
    for _ in range(steps):
        reach = list(reachable(g, boxes, player))
        rng.shuffle(reach)
        moved = False
        for p in reach:
            for dx, dy in rng.sample(DIRS, 4):
                b = (p[0] + dx, p[1] + dy)          # box next to player
                back = (p[0] - dx, p[1] - dy)       # where the player steps back to
                if b in boxes and g[back[1]][back[0]] != "#" and back not in boxes:
                    boxes.remove(b)
                    boxes.add(p)
                    player = back
                    moved = True
                    break
            if moved:
                break
        if not moved:
            break
    return boxes, player


def norm_player(g, boxes, player):
    return min(reachable(g, boxes, player))


def solve(g, goals, boxes, player, limit=200000):
    """Minimum pushes via BFS over (normalised player, boxes)."""
    goals = frozenset(goals)
    start = (norm_player(g, boxes, player), frozenset(boxes))
    seen = {start}
    q = deque([(start, 0)])
    while q:
        (p, bs), d = q.popleft()
        if bs == goals:
            return d
        if len(seen) > limit:
            return None
        reach = reachable(g, bs, p)
        for b in bs:
            for dx, dy in DIRS:
                stand = (b[0] - dx, b[1] - dy)
                dest = (b[0] + dx, b[1] + dy)
                if stand in reach and g[dest[1]][dest[0]] != "#" and dest not in bs:
                    nb = set(bs)
                    nb.remove(b)
                    nb.add(dest)
                    nbf = frozenset(nb)
                    st = (norm_player(g, nbf, b), nbf)
                    if st not in seen:
                        seen.add(st)
                        q.append((st, d + 1))
    return None


def render(g, goals, boxes, player):
    rows = []
    for y in range(len(g)):
        s = ""
        for x in range(len(g[0])):
            c = (x, y)
            if g[y][x] == "#":
                s += "#"
            elif c in boxes:
                s += "*" if c in goals else "$"
            elif c == player:
                s += "+" if c in goals else "@"
            else:
                s += "." if c in goals else " "
        rows.append(s)
    return rows


def main():
    rng = random.Random(20261003)
    found = []
    tries = 0
    while len(found) < 120 and tries < 60000:
        tries += 1
        w, h = rng.randint(7, 9), rng.randint(7, 9)
        g = make_room(rng, w, h)
        if g is None:
            continue
        floors = [(x, y) for y in range(h) for x in range(w) if g[y][x] == " "]
        nbox = rng.choice([2, 3, 3, 3, 4, 4])
        goals = rng.sample(floors, nbox)
        best = None
        for _ in range(6):  # several reverse walks per room, keep the hardest
            boxes, player = reverse_play(rng, g, goals, rng.randint(80, 400))
            if len(set(boxes) & set(goals)) > 0:
                continue
            pushes = solve(g, goals, boxes, player)
            if pushes is not None and (best is None or pushes > best[0]):
                best = (pushes, nbox, render(g, set(goals), boxes, player))
        if best is None or best[0] < 8:
            continue
        found.append(best)
    found.sort(key=lambda t: (t[0], t[1]))
    # the 20 hardest, easiest first; keep one easy intro level
    picks = [found[0]] + found[-19:]
    out = sys.stdout
    out.write("package cratepusher;\n\n")
    out.write("/** Generated by tools/gen_levels.py (reverse-play generator + BFS solver). Do not edit. */\n")
    out.write("final class Levels {\n    private Levels() {}\n\n")
    out.write("    /** Minimum number of pushes for each level, found by the solver. */\n")
    out.write("    static final int[] PAR = { %s };\n\n" % ", ".join(str(p[0]) for p in picks))
    out.write("    static final String[][] DATA = {\n")
    for pushes, nbox, rows in picks:
        out.write("        { %s },\n" % ", ".join('"%s"' % r for r in rows))
    out.write("    };\n}\n")


if __name__ == "__main__":
    main()
