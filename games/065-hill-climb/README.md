# Hill Climb

<img src="media/icon.png" width="24" height="24" alt=""> **Racing** &middot; Game 065 of 100 &middot; v1.0.0

> Drive a little 4x4 over endless rolling hills - feather the gas, tilt in the air and land on your wheels.

<p><img src="media/title.png" alt="Hill Climb title screen" width="176"> <img src="media/play.png" alt="Hill Climb gameplay" width="176"> <img src="media/demo.gif" alt="Hill Climb gameplay animation" width="176"></p>

<sub>Screenshots at 176x208, captured from the real JAR on the project's reference runtime.</sub>

## About

A side-view physics driver over procedurally generated hills that grow steeper the further you go. Gravity pulls you back on climbs and launches you off crests; in the air, 4 and 6 tilt the car so you can land on your wheels (clean landings earn a bonus, landing on your roof ends the run). Fuel cans keep the tank topped up and coins add to your score.

**Objective:** Drive as far as possible before you flip over or run out of fuel.

## Controls

| Key | Action |
|---|---|
| 6/2 | Gas |
| 4/8 | Brake / reverse |
| 4/6 (in the air) | Tilt the car |
| * | Pause menu |

Every game also supports: `5` to select in menus, `*` to pause, `0` on the title screen to toggle sound, and `#` on the title screen for help. Joystick/D-pad and the 2/4/6/8 keys are interchangeable.

## Download

| File | Link |
|---|---|
| JAR (install this) | [HillClimb.jar](https://github.com/agneay/j2me-100-games/releases/download/game-065-v1.0.0/HillClimb.jar) |
| JAD (descriptor for OTA install) | [HillClimb.jad](https://github.com/agneay/j2me-100-games/releases/download/game-065-v1.0.0/HillClimb.jad) |
| Release page | [game-065-v1.0.0](https://github.com/agneay/j2me-100-games/releases/tag/game-065-v1.0.0) |
| All 100 games | [j2me-100-games-v1.0.0.zip](https://github.com/agneay/j2me-100-games/releases/download/v1.0.0/j2me-100-games-v1.0.0.zip) |

**Browser Demo:** [play in your browser](https://agneay.github.io/j2me-100-games/games/hill-climb/). The browser demo is the same Java source compiled to JavaScript with TeaVM and run on the project's MIDP reference runtime; it is a convenience preview, not the original Java ME version. For the authentic experience install the JAR on a phone or in an emulator.

Installation help: [docs/installing.md](../../docs/installing.md) and [docs/emulator.md](../../docs/emulator.md).

## Technical details

| | |
|---|---|
| MIDlet class | `hillclimb.HillClimbMIDlet` |
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
tools/build-game.sh 065
```

Output: `games/065-hill-climb/dist/HillClimb.jar` and `.jad`. See [docs/building.md](../../docs/building.md).

---

Part of [J2ME 100 Games](../../README.md). Original game, released under the [MIT License](../../LICENSE).
