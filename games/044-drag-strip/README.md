# Drag Strip

<img src="media/icon.png" width="24" height="24" alt=""> **Racing** &middot; Game 044 of 100 &middot; v1.0.0

> Quarter-mile drag races won on launch revs and perfectly timed gear shifts, with a garage full of upgrades.

<p><img src="media/title.png" alt="Drag Strip title screen" width="176"> <img src="media/play.png" alt="Drag Strip gameplay" width="176"> <img src="media/demo.gif" alt="Drag Strip gameplay animation" width="176"></p>

<sub>Screenshots at 176x208, captured from the real JAR on the project's reference runtime.</sub>

## About

A timing-based racer built around a tachometer. Rev the engine during the countdown to nail the launch, then shift up through five gears when the needle hits the green band - too early bogs the engine, too late bounces off the limiter. Beat five rivals of rising skill, and spend your prize money on engine, gearbox and tyre upgrades between races.

**Objective:** Beat all five rivals to become drag strip champion.

## Controls

| Key | Action |
|---|---|
| 5 (hold) | Rev during the countdown |
| 5/6 | Shift up |
| 2/8 | Choose upgrade (garage) |
| 5 | Buy upgrade (garage) |
| # | Start the race |
| * | Pause menu |

Every game also supports: `5` to select in menus, `*` to pause, `0` on the title screen to toggle sound, and `#` on the title screen for help. Joystick/D-pad and the 2/4/6/8 keys are interchangeable.

## Download

| File | Link |
|---|---|
| JAR (install this) | [DragStrip.jar](https://github.com/agneay/j2me-100-games/releases/download/game-044-v1.0.0/DragStrip.jar) |
| JAD (descriptor for OTA install) | [DragStrip.jad](https://github.com/agneay/j2me-100-games/releases/download/game-044-v1.0.0/DragStrip.jad) |
| Release page | [game-044-v1.0.0](https://github.com/agneay/j2me-100-games/releases/tag/game-044-v1.0.0) |
| All 100 games | [j2me-100-games-v1.0.0.zip](https://github.com/agneay/j2me-100-games/releases/download/v1.0.0/j2me-100-games-v1.0.0.zip) |

**Browser Demo:** [play in your browser](https://agneay.github.io/j2me-100-games/games/drag-strip/). The browser demo is the same Java source compiled to JavaScript with TeaVM and run on the project's MIDP reference runtime; it is a convenience preview, not the original Java ME version. For the authentic experience install the JAR on a phone or in an emulator.

Installation help: [docs/installing.md](../../docs/installing.md) and [docs/emulator.md](../../docs/emulator.md).

## Technical details

| | |
|---|---|
| MIDlet class | `dragstrip.DragStripMIDlet` |
| Platform | Java ME, CLDC 1.0, MIDP 2.0 |
| JAR size | 19.0 KB |
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
tools/build-game.sh 044
```

Output: `games/044-drag-strip/dist/DragStrip.jar` and `.jad`. See [docs/building.md](../../docs/building.md).

---

Part of [J2ME 100 Games](../../README.md). Original game, released under the [MIT License](../../LICENSE).
