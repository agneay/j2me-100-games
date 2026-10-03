# Noughts Grid

<img src="media/icon.png" width="24" height="24" alt=""> **Board** &middot; Game 021 of 100 &middot; v1.0.0

> Noughts and crosses where the keypad is the board - plus a 5x5 four-in-a-row variant.

<p><img src="media/title.png" alt="Noughts Grid title screen" width="176"> <img src="media/play.png" alt="Noughts Grid gameplay" width="176"> <img src="media/demo.gif" alt="Noughts Grid gameplay animation" width="176"></p>

<sub>Screenshots at 176x208, captured from the real JAR on the project's reference runtime.</sub>

## About

On the classic board, keys 1-9 map one-to-one onto the squares, so playing feels like pressing the grid itself. The Perfect CPU uses full minimax and never loses; Easy makes human mistakes. The 5x5 board needs four in a row and has a heuristic CPU, and both boards support two players on one phone.

**Objective:** Get three (or four on 5x5) of your marks in a row.

**Modes:** 3x3 vs CPU Easy, 3x3 vs CPU Perfect, 3x3 2 Players, 5x5 vs CPU, 5x5 2 Players

## Controls

| Key | Action |
|---|---|
| 1-9 | Play that square (3x3) |
| 2/4/6/8 | Move cursor |
| 5 | Play at cursor |
| * | Pause menu |

Every game also supports: `5` to select in menus, `*` to pause, `0` on the title screen to toggle sound, and `#` on the title screen for help. Joystick/D-pad and the 2/4/6/8 keys are interchangeable.

## Download

| File | Link |
|---|---|
| JAR (install this) | [NoughtsGrid.jar](https://github.com/agneay/j2me-100-games/releases/download/game-021-v1.0.0/NoughtsGrid.jar) |
| JAD (descriptor for OTA install) | [NoughtsGrid.jad](https://github.com/agneay/j2me-100-games/releases/download/game-021-v1.0.0/NoughtsGrid.jad) |
| Release page | [game-021-v1.0.0](https://github.com/agneay/j2me-100-games/releases/tag/game-021-v1.0.0) |
| All 100 games | [j2me-100-games-v1.0.0.zip](https://github.com/agneay/j2me-100-games/releases/download/v1.0.0/j2me-100-games-v1.0.0.zip) |

**Browser Demo:** [play in your browser](https://agneay.github.io/j2me-100-games/games/noughts-grid/). The browser demo is the same Java source compiled to JavaScript with TeaVM and run on the project's MIDP reference runtime; it is a convenience preview, not the original Java ME version. For the authentic experience install the JAR on a phone or in an emulator.

Installation help: [docs/installing.md](../../docs/installing.md) and [docs/emulator.md](../../docs/emulator.md).

## Technical details

| | |
|---|---|
| MIDlet class | `noughtsgrid.NoughtsGridMIDlet` |
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
tools/build-game.sh 021
```

Output: `games/021-noughts-grid/dist/NoughtsGrid.jar` and `.jad`. See [docs/building.md](../../docs/building.md).

---

Part of [J2ME 100 Games](../../README.md). Original game, released under the [MIT License](../../LICENSE).
