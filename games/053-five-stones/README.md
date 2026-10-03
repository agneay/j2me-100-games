# Five Stones

<img src="media/icon.png" width="24" height="24" alt=""> **Board** &middot; Game 053 of 100 &middot; v1.0.0

> Five in a row on an 11x11 board, against a CPU that weighs every open three and four.

<p><img src="media/title.png" alt="Five Stones title screen" width="176"> <img src="media/play.png" alt="Five Stones gameplay" width="176"> <img src="media/demo.gif" alt="Five Stones gameplay animation" width="176"></p>

<sub>Screenshots at 176x208, captured from the real JAR on the project's reference runtime.</sub>

## About

The ancient five-in-a-row stone game. Place black stones on the intersections and be first to an unbroken line of five. The CPU scores every candidate point for both attack and defence using open-ended run patterns, so it will block your open threes and pounce on your mistakes; Easy is more forgiving. A two-player mode lets friends share the phone. The winning line is drawn and the last stone is marked.

**Objective:** Be the first to place five stones in an unbroken line.

**Modes:** vs CPU Easy, vs CPU Hard, 2 Players

## Controls

| Key | Action |
|---|---|
| 2/4/6/8 | Move cursor |
| 1/3/7/9 | Move diagonally |
| 5 | Place stone |
| * | Pause menu |

Every game also supports: `5` to select in menus, `*` to pause, `0` on the title screen to toggle sound, and `#` on the title screen for help. Joystick/D-pad and the 2/4/6/8 keys are interchangeable.

## Download

| File | Link |
|---|---|
| JAR (install this) | [FiveStones.jar](https://github.com/agneay/j2me-100-games/releases/download/game-053-v1.0.0/FiveStones.jar) |
| JAD (descriptor for OTA install) | [FiveStones.jad](https://github.com/agneay/j2me-100-games/releases/download/game-053-v1.0.0/FiveStones.jad) |
| Release page | [game-053-v1.0.0](https://github.com/agneay/j2me-100-games/releases/tag/game-053-v1.0.0) |
| All 100 games | [j2me-100-games-v1.0.0.zip](https://github.com/agneay/j2me-100-games/releases/download/v1.0.0/j2me-100-games-v1.0.0.zip) |

**Browser Demo:** [play in your browser](https://agneay.github.io/j2me-100-games/games/five-stones/). The browser demo is the same Java source compiled to JavaScript with TeaVM and run on the project's MIDP reference runtime; it is a convenience preview, not the original Java ME version. For the authentic experience install the JAR on a phone or in an emulator.

Installation help: [docs/installing.md](../../docs/installing.md) and [docs/emulator.md](../../docs/emulator.md).

## Technical details

| | |
|---|---|
| MIDlet class | `fivestones.FiveStonesMIDlet` |
| Platform | Java ME, CLDC 1.0, MIDP 2.0 |
| JAR size | 17.5 KB |
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
tools/build-game.sh 053
```

Output: `games/053-five-stones/dist/FiveStones.jar` and `.jad`. See [docs/building.md](../../docs/building.md).

---

Part of [J2ME 100 Games](../../README.md). Original game, released under the [MIT License](../../LICENSE).
