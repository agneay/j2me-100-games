# Mine Sweep

<img src="media/icon.png" width="24" height="24" alt=""> **Puzzle** &middot; Game 004 of 100 &middot; v1.0.0

> Deduce where the mines are from the numbers and open every safe square.

<p><img src="media/title.png" alt="Mine Sweep title screen" width="176"> <img src="media/play.png" alt="Mine Sweep gameplay" width="176"> <img src="media/demo.gif" alt="Mine Sweep gameplay animation" width="176"></p>

<sub>Screenshots at 176x208, captured from the real JAR on the project's reference runtime.</sub>

## About

The logic classic for a keypad: move with 2/4/6/8 (or diagonally with 1/3/7/9), open with 5 and flag with 0 or #. The first square is always safe, opened numbers can be chorded, and boards larger than the screen scroll with the cursor so even the 16x16 field works on a 128x128 display.

**Objective:** Open every square that does not contain a mine, as fast as you can.

**Modes:** Easy 8x8, Medium 12x12, Hard 16x16

## Controls

| Key | Action |
|---|---|
| 2/4/6/8 | Move cursor |
| 1/3/7/9 | Move diagonally |
| 5 | Open square / chord |
| 0/# | Flag |
| * | Pause menu |

Every game also supports: `5` to select in menus, `*` to pause, `0` on the title screen to toggle sound, and `#` on the title screen for help. Joystick/D-pad and the 2/4/6/8 keys are interchangeable.

## Download

| File | Link |
|---|---|
| JAR (install this) | [MineSweep.jar](https://github.com/agneay/j2me-100-games/releases/download/game-004-v1.0.0/MineSweep.jar) |
| JAD (descriptor for OTA install) | [MineSweep.jad](https://github.com/agneay/j2me-100-games/releases/download/game-004-v1.0.0/MineSweep.jad) |
| Release page | [game-004-v1.0.0](https://github.com/agneay/j2me-100-games/releases/tag/game-004-v1.0.0) |
| All 100 games | [j2me-100-games-v1.0.0.zip](https://github.com/agneay/j2me-100-games/releases/download/v1.0.0/j2me-100-games-v1.0.0.zip) |

**Browser Demo:** [play in your browser](https://agneay.github.io/j2me-100-games/games/mine-sweep/). The browser demo is the same Java source compiled to JavaScript with TeaVM and run on the project's MIDP reference runtime; it is a convenience preview, not the original Java ME version. For the authentic experience install the JAR on a phone or in an emulator.

Installation help: [docs/installing.md](../../docs/installing.md) and [docs/emulator.md](../../docs/emulator.md).

## Technical details

| | |
|---|---|
| MIDlet class | `minesweep.MineSweepMIDlet` |
| Platform | Java ME, CLDC 1.0, MIDP 2.0 |
| JAR size | 18.1 KB |
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
tools/build-game.sh 004
```

Output: `games/004-mine-sweep/dist/MineSweep.jar` and `.jad`. See [docs/building.md](../../docs/building.md).

---

Part of [J2ME 100 Games](../../README.md). Original game, released under the [MIT License](../../LICENSE).
