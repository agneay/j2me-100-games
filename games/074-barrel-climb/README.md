# Barrel Climb

<img src="media/icon.png" width="24" height="24" alt=""> **Platformer** &middot; Game 074 of 100 &middot; v1.0.0

> Climb five girders by ladder while barrels roll down at you - jump them for points.

<p><img src="media/title.png" alt="Barrel Climb title screen" width="176"> <img src="media/play.png" alt="Barrel Climb gameplay" width="176"> <img src="media/demo.gif" alt="Barrel Climb gameplay animation" width="176"></p>

<sub>Screenshots at 176x208, captured from the real JAR on the project's reference runtime.</sub>

## About

A single-screen climbing platformer. Barrels are thrown from the top, roll along each girder, drop off its open end and occasionally tumble straight down a ladder - including the one you're on. Walk, hop and climb your way to the top girder; every barrel you leap scores 100 and each stage the barrels roll faster and more often. Ladder positions change every stage.

**Objective:** Reach the top girder on every stage without getting hit.

## Controls

| Key | Action |
|---|---|
| 4/6 | Walk |
| 2/8 | Climb up / down ladders |
| 5 | Jump |
| * | Pause menu |

Every game also supports: `5` to select in menus, `*` to pause, `0` on the title screen to toggle sound, and `#` on the title screen for help. Joystick/D-pad and the 2/4/6/8 keys are interchangeable.

## Download

| File | Link |
|---|---|
| JAR (install this) | [BarrelClimb.jar](https://github.com/agneay/j2me-100-games/releases/download/game-074-v1.0.0/BarrelClimb.jar) |
| JAD (descriptor for OTA install) | [BarrelClimb.jad](https://github.com/agneay/j2me-100-games/releases/download/game-074-v1.0.0/BarrelClimb.jad) |
| Release page | [game-074-v1.0.0](https://github.com/agneay/j2me-100-games/releases/tag/game-074-v1.0.0) |
| All 100 games | [j2me-100-games-v1.0.0.zip](https://github.com/agneay/j2me-100-games/releases/download/v1.0.0/j2me-100-games-v1.0.0.zip) |

**Browser Demo:** [play in your browser](https://agneay.github.io/j2me-100-games/games/barrel-climb/). The browser demo is the same Java source compiled to JavaScript with TeaVM and run on the project's MIDP reference runtime; it is a convenience preview, not the original Java ME version. For the authentic experience install the JAR on a phone or in an emulator.

Installation help: [docs/installing.md](../../docs/installing.md) and [docs/emulator.md](../../docs/emulator.md).

## Technical details

| | |
|---|---|
| MIDlet class | `barrelclimb.BarrelClimbMIDlet` |
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
tools/build-game.sh 074
```

Output: `games/074-barrel-climb/dist/BarrelClimb.jar` and `.jad`. See [docs/building.md](../../docs/building.md).

---

Part of [J2ME 100 Games](../../README.md). Original game, released under the [MIT License](../../LICENSE).
