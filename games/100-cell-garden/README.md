# Cell Garden

<img src="media/icon.png" width="24" height="24" alt=""> **Experimental** &middot; Game 100 of 100 &middot; v1.0.0

> Plant a handful of seeds and let them grow by cellular-automaton rules; score the plants that end up on golden soil.

<p><img src="media/title.png" alt="Cell Garden title screen" width="176"> <img src="media/play.png" alt="Cell Garden gameplay" width="176"> <img src="media/demo.gif" alt="Cell Garden gameplay animation" width="176"></p>

<sub>Screenshots at 176x208, captured from the real JAR on the project's reference runtime.</sub>

## About

A gardening puzzle built on a classic cellular automaton. Each generation, an empty plot with exactly three living neighbours sprouts, and a plant with two or three neighbours survives while the rest wither. In Challenge mode you get a limited number of seeds and a set number of generations; plan a pattern that blooms across the golden fertile plots and holds there when the growing stops. Five gardens per game. Sandbox mode gives unlimited seeds with run, pause and single-step controls for free experimenting.

**Objective:** Have as many plants as possible on golden plots when the final generation ends.

**Modes:** Challenge, Sandbox

## Controls

| Key | Action |
|---|---|
| 2/4/6/8 | Move cursor |
| 5 | Plant / remove seed |
| 0 | Grow (Sandbox: run / pause) |
| 1 | Single step (Sandbox) |
| # | Clear (Sandbox) |
| * | Pause menu |

Every game also supports: `5` to select in menus, `*` to pause, `0` on the title screen to toggle sound, and `#` on the title screen for help. Joystick/D-pad and the 2/4/6/8 keys are interchangeable.

## Download

| File | Link |
|---|---|
| JAR (install this) | [CellGarden.jar](https://github.com/agneay/j2me-100-games/releases/download/game-100-v1.0.0/CellGarden.jar) |
| JAD (descriptor for OTA install) | [CellGarden.jad](https://github.com/agneay/j2me-100-games/releases/download/game-100-v1.0.0/CellGarden.jad) |
| Release page | [game-100-v1.0.0](https://github.com/agneay/j2me-100-games/releases/tag/game-100-v1.0.0) |
| All 100 games | [j2me-100-games-v1.0.0.zip](https://github.com/agneay/j2me-100-games/releases/download/v1.0.0/j2me-100-games-v1.0.0.zip) |

**Browser Demo:** [play in your browser](https://agneay.github.io/j2me-100-games/games/cell-garden/). The browser demo is the same Java source compiled to JavaScript with TeaVM and run on the project's MIDP reference runtime; it is a convenience preview, not the original Java ME version. For the authentic experience install the JAR on a phone or in an emulator.

Installation help: [docs/installing.md](../../docs/installing.md) and [docs/emulator.md](../../docs/emulator.md).

## Technical details

| | |
|---|---|
| MIDlet class | `cellgarden.CellGardenMIDlet` |
| Platform | Java ME, CLDC 1.0, MIDP 2.0 |
| JAR size | 17.6 KB |
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
tools/build-game.sh 100
```

Output: `games/100-cell-garden/dist/CellGarden.jar` and `.jad`. See [docs/building.md](../../docs/building.md).

---

Part of [J2ME 100 Games](../../README.md). Original game, released under the [MIT License](../../LICENSE).
