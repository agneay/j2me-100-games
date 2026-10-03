# Dungeon Quest

<img src="media/icon.png" width="24" height="24" alt=""> **RPG** &middot; Game 015 of 100 &middot; v1.0.0

> A pocket roguelike: explore ten generated dungeon floors, level up, and slay the dragon at the bottom.

<p><img src="media/title.png" alt="Dungeon Quest title screen" width="176"> <img src="media/play.png" alt="Dungeon Quest gameplay" width="176"> <img src="media/demo.gif" alt="Dungeon Quest gameplay animation" width="176"></p>

<sub>Screenshots at 176x208, captured from the real JAR on the project's reference runtime.</sub>

## About

Every floor is a fresh set of rooms and corridors revealed by line-of-sight field of view. Movement is turn-based, so take your time: bump into monsters to fight them, gather potions, gold, weapons and armour, and level up as you descend past rats, goblins, skeletons, orcs, trolls and wraiths. Press # for a map of everything you have explored.

**Objective:** Reach floor 10 and defeat the dragon.

## Controls

| Key | Action |
|---|---|
| 2/4/6/8 | Move / attack |
| 1/3/7/9 | Move diagonally |
| 5 | Wait / take stairs |
| 0 | Drink potion |
| # | Dungeon map |
| * | Pause menu |

Every game also supports: `5` to select in menus, `*` to pause, `0` on the title screen to toggle sound, and `#` on the title screen for help. Joystick/D-pad and the 2/4/6/8 keys are interchangeable.

## Download

| File | Link |
|---|---|
| JAR (install this) | [DungeonQuest.jar](https://github.com/agneay/j2me-100-games/releases/download/game-015-v1.0.0/DungeonQuest.jar) |
| JAD (descriptor for OTA install) | [DungeonQuest.jad](https://github.com/agneay/j2me-100-games/releases/download/game-015-v1.0.0/DungeonQuest.jad) |
| Release page | [game-015-v1.0.0](https://github.com/agneay/j2me-100-games/releases/tag/game-015-v1.0.0) |
| All 100 games | [j2me-100-games-v1.0.0.zip](https://github.com/agneay/j2me-100-games/releases/download/v1.0.0/j2me-100-games-v1.0.0.zip) |

**Browser Demo:** [play in your browser](https://agneay.github.io/j2me-100-games/games/dungeon-quest/). The browser demo is the same Java source compiled to JavaScript with TeaVM and run on the project's MIDP reference runtime; it is a convenience preview, not the original Java ME version. For the authentic experience install the JAR on a phone or in an emulator.

Installation help: [docs/installing.md](../../docs/installing.md) and [docs/emulator.md](../../docs/emulator.md).

## Technical details

| | |
|---|---|
| MIDlet class | `dungeonquest.DungeonQuestMIDlet` |
| Platform | Java ME, CLDC 1.0, MIDP 2.0 |
| JAR size | 21.2 KB |
| Designed for | 128x128, 128x160, 176x208, 176x220, 240x320 |
| Storage | RMS record store `gk` (settings and best score) |
| Floating point | none (integer maths only) |

## Verification

| Level | Status |
|---|---|
| Build verified | Yes: compiled against the CLDC 1.0 / MIDP 2.0 API, JAR + JAD generated |
| Reference runtime | Passed at 128x128, 128x160, 176x208, 176x220, 240x320 (automated, project's headless MIDP runtime) |
| Emulator verified | Yes: automated smoke test on MicroEmulator 2.0.4 (176x220) |
| Real device verified | Not yet tested on real hardware |

Tested it on a real phone? Please [report it](https://github.com/agneay/j2me-100-games/issues/new?template=device-report.yml) so this table can be updated honestly.

## Build from source

```bash
python tools/bootstrap.py
tools/build-game.sh 015
```

Output: `games/015-dungeon-quest/dist/DungeonQuest.jar` and `.jad`. See [docs/building.md](../../docs/building.md).

---

Part of [J2ME 100 Games](../../README.md). Original game, released under the [MIT License](../../LICENSE).
