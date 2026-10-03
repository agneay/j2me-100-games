# Land Grab

<img src="media/icon.png" width="24" height="24" alt=""> **Strategy** &middot; Game 045 of 100 &middot; v1.0.0

> Pick a colour to flood your territory into matching neighbours - out-grow the CPU to own the board.

<p><img src="media/title.png" alt="Land Grab title screen" width="176"> <img src="media/play.png" alt="Land Grab gameplay" width="176"> <img src="media/demo.gif" alt="Land Grab gameplay animation" width="176"></p>

<sub>Screenshots at 176x208, captured from the real JAR on the project's reference runtime.</sub>

## About

A two-player territory game on an 11x11 board of six colours. You expand from the bottom-left, the CPU from the top-right; each turn you choose a colour (never your own or your opponent's) and absorb every touching tile of it. Press 1-6 to pick instantly. The clever CPU weighs not only its own gain but also which colours would most help you next turn.

**Objective:** Own more than half of the board.

**Modes:** vs CPU, vs Clever CPU

## Controls

| Key | Action |
|---|---|
| 1-6 | Choose colour |
| 4/6 | Highlight colour |
| 5 | Confirm highlighted colour |
| * | Pause menu |

Every game also supports: `5` to select in menus, `*` to pause, `0` on the title screen to toggle sound, and `#` on the title screen for help. Joystick/D-pad and the 2/4/6/8 keys are interchangeable.

## Download

| File | Link |
|---|---|
| JAR (install this) | [LandGrab.jar](https://github.com/agneay/j2me-100-games/releases/download/game-045-v1.0.0/LandGrab.jar) |
| JAD (descriptor for OTA install) | [LandGrab.jad](https://github.com/agneay/j2me-100-games/releases/download/game-045-v1.0.0/LandGrab.jad) |
| Release page | [game-045-v1.0.0](https://github.com/agneay/j2me-100-games/releases/tag/game-045-v1.0.0) |
| All 100 games | [j2me-100-games-v1.0.0.zip](https://github.com/agneay/j2me-100-games/releases/download/v1.0.0/j2me-100-games-v1.0.0.zip) |

**Browser Demo:** [play in your browser](https://agneay.github.io/j2me-100-games/games/land-grab/). The browser demo is the same Java source compiled to JavaScript with TeaVM and run on the project's MIDP reference runtime; it is a convenience preview, not the original Java ME version. For the authentic experience install the JAR on a phone or in an emulator.

Installation help: [docs/installing.md](../../docs/installing.md) and [docs/emulator.md](../../docs/emulator.md).

## Technical details

| | |
|---|---|
| MIDlet class | `landgrab.LandGrabMIDlet` |
| Platform | Java ME, CLDC 1.0, MIDP 2.0 |
| JAR size | 17.4 KB |
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
tools/build-game.sh 045
```

Output: `games/045-land-grab/dist/LandGrab.jar` and `.jad`. See [docs/building.md](../../docs/building.md).

---

Part of [J2ME 100 Games](../../README.md). Original game, released under the [MIT License](../../LICENSE).
