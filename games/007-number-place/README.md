# Number Place

<img src="media/icon.png" width="24" height="24" alt=""> **Puzzle** &middot; Game 007 of 100 &middot; v1.0.0

> Fill the 9x9 grid so every row, column and box holds 1-9. Fresh puzzles generated on the phone, always with exactly one solution.

<p><img src="media/title.png" alt="Number Place title screen" width="176"> <img src="media/play.png" alt="Number Place gameplay" width="176"> <img src="media/demo.gif" alt="Number Place gameplay animation" width="176"></p>

<sub>Screenshots at 176x208, captured from the real JAR on the project's reference runtime.</sub>

## About

A number-placement logic puzzle with an on-device generator: a valid grid is shuffled, then clues are removed one at a time while a bitmask backtracking solver proves the solution stays unique. Two input styles cover every handset: Keypad mode (2/4/6/8 move, 5 opens a number picker) for phones without a joystick, and Direct mode (# toggles) where digits write straight into the cell. Clashes are highlighted and matching digits glow.

**Objective:** Complete the grid correctly in the shortest time.

**Modes:** Easy, Medium, Hard

## Controls

| Key | Action |
|---|---|
| 2/4/6/8 | Move cursor (keypad mode) |
| 5 | Open number picker |
| 1-9 | Write digit (direct mode / picker) |
| 0 | Clear cell |
| # | Toggle keypad / direct mode |
| * | Pause menu |

Every game also supports: `5` to select in menus, `*` to pause, `0` on the title screen to toggle sound, and `#` on the title screen for help. Joystick/D-pad and the 2/4/6/8 keys are interchangeable.

## Download

| File | Link |
|---|---|
| JAR (install this) | [NumberPlace.jar](https://github.com/agneay/j2me-100-games/releases/download/game-007-v1.0.0/NumberPlace.jar) |
| JAD (descriptor for OTA install) | [NumberPlace.jad](https://github.com/agneay/j2me-100-games/releases/download/game-007-v1.0.0/NumberPlace.jad) |
| Release page | [game-007-v1.0.0](https://github.com/agneay/j2me-100-games/releases/tag/game-007-v1.0.0) |
| All 100 games | [j2me-100-games-v1.0.0.zip](https://github.com/agneay/j2me-100-games/releases/download/v1.0.0/j2me-100-games-v1.0.0.zip) |

**Browser Demo:** [play in your browser](https://agneay.github.io/j2me-100-games/games/number-place/). The browser demo is the same Java source compiled to JavaScript with TeaVM and run on the project's MIDP reference runtime; it is a convenience preview, not the original Java ME version. For the authentic experience install the JAR on a phone or in an emulator.

Installation help: [docs/installing.md](../../docs/installing.md) and [docs/emulator.md](../../docs/emulator.md).

## Technical details

| | |
|---|---|
| MIDlet class | `numberplace.NumberPlaceMIDlet` |
| Platform | Java ME, CLDC 1.0, MIDP 2.0 |
| JAR size | 18.5 KB |
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
tools/build-game.sh 007
```

Output: `games/007-number-place/dist/NumberPlace.jar` and `.jad`. See [docs/building.md](../../docs/building.md).

---

Part of [J2ME 100 Games](../../README.md). Original game, released under the [MIT License](../../LICENSE).
