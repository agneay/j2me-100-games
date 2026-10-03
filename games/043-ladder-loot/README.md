# Ladder Loot

<img src="media/icon.png" width="24" height="24" alt=""> **Platformer** &middot; Game 043 of 100 &middot; v1.0.0

> Grab every gold bar on a screen of ladders, ropes and bricks while guards chase you - dig holes to trap them.

<p><img src="media/title.png" alt="Ladder Loot title screen" width="176"> <img src="media/play.png" alt="Ladder Loot gameplay" width="176"> <img src="media/demo.gif" alt="Ladder Loot gameplay animation" width="176"></p>

<sub>Screenshots at 176x208, captured from the real JAR on the project's reference runtime.</sub>

## About

A single-screen action puzzle platformer. Run, climb ladders and hang from ropes to collect the gold; dig through the brick floor beside you with 1 and 3 to trap pursuing guards, but get clear before the hole fills back in. Taking the last bar raises an escape ladder to the next level. Levels come from a generator that always joins every floor with a ladder, and more guards join as you progress.

**Objective:** Collect all the gold on each level and escape up the ladder that appears.

## Controls

| Key | Action |
|---|---|
| 4/6 | Run |
| 2/8 | Climb up / down, drop |
| 1 | Dig hole to the left |
| 3 | Dig hole to the right |
| * | Pause menu |

Every game also supports: `5` to select in menus, `*` to pause, `0` on the title screen to toggle sound, and `#` on the title screen for help. Joystick/D-pad and the 2/4/6/8 keys are interchangeable.

## Download

| File | Link |
|---|---|
| JAR (install this) | [LadderLoot.jar](https://github.com/agneay/j2me-100-games/releases/download/game-043-v1.0.0/LadderLoot.jar) |
| JAD (descriptor for OTA install) | [LadderLoot.jad](https://github.com/agneay/j2me-100-games/releases/download/game-043-v1.0.0/LadderLoot.jad) |
| Release page | [game-043-v1.0.0](https://github.com/agneay/j2me-100-games/releases/tag/game-043-v1.0.0) |
| All 100 games | [j2me-100-games-v1.0.0.zip](https://github.com/agneay/j2me-100-games/releases/download/v1.0.0/j2me-100-games-v1.0.0.zip) |

**Browser Demo:** [play in your browser](https://agneay.github.io/j2me-100-games/games/ladder-loot/). The browser demo is the same Java source compiled to JavaScript with TeaVM and run on the project's MIDP reference runtime; it is a convenience preview, not the original Java ME version. For the authentic experience install the JAR on a phone or in an emulator.

Installation help: [docs/installing.md](../../docs/installing.md) and [docs/emulator.md](../../docs/emulator.md).

## Technical details

| | |
|---|---|
| MIDlet class | `ladderloot.LadderLootMIDlet` |
| Platform | Java ME, CLDC 1.0, MIDP 2.0 |
| JAR size | 19.5 KB |
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
tools/build-game.sh 043
```

Output: `games/043-ladder-loot/dist/LadderLoot.jar` and `.jad`. See [docs/building.md](../../docs/building.md).

---

Part of [J2ME 100 Games](../../README.md). Original game, released under the [MIT License](../../LICENSE).
