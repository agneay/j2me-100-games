# Brick Buster

<img src="media/icon.png" width="24" height="24" alt=""> **Arcade** &middot; Game 006 of 100 &middot; v1.0.0

> Bounce the ball off your paddle to smash eight hand-built walls of bricks, with power-up capsules.

<p><img src="media/title.png" alt="Brick Buster title screen" width="176"> <img src="media/play.png" alt="Brick Buster gameplay" width="176"> <img src="media/demo.gif" alt="Brick Buster gameplay animation" width="176"></p>

<sub>Screenshots at 176x208, captured from the real JAR on the project's reference runtime.</sub>

## About

A brick breaker with eight original layouts, multi-hit and steel bricks, and four capsule power-ups (wide paddle, multi-ball, slow ball and extra life). The return angle depends on where the ball meets the paddle, and the second time through the levels the ball is faster.

**Objective:** Clear every breakable brick on each level without losing all three balls.

## Controls

| Key | Action |
|---|---|
| 4/Left | Move paddle left |
| 6/Right | Move paddle right |
| 5/Fire | Launch ball |
| * | Pause menu |

Every game also supports: `5` to select in menus, `*` to pause, `0` on the title screen to toggle sound, and `#` on the title screen for help. Joystick/D-pad and the 2/4/6/8 keys are interchangeable.

## Download

| File | Link |
|---|---|
| JAR (install this) | [BrickBuster.jar](https://github.com/agneay/j2me-100-games/releases/download/game-006-v1.0.0/BrickBuster.jar) |
| JAD (descriptor for OTA install) | [BrickBuster.jad](https://github.com/agneay/j2me-100-games/releases/download/game-006-v1.0.0/BrickBuster.jad) |
| Release page | [game-006-v1.0.0](https://github.com/agneay/j2me-100-games/releases/tag/game-006-v1.0.0) |
| All 100 games | [j2me-100-games-v1.0.0.zip](https://github.com/agneay/j2me-100-games/releases/download/v1.0.0/j2me-100-games-v1.0.0.zip) |

**Browser Demo:** [play in your browser](https://agneay.github.io/j2me-100-games/games/brick-buster/). The browser demo is the same Java source compiled to JavaScript with TeaVM and run on the project's MIDP reference runtime; it is a convenience preview, not the original Java ME version. For the authentic experience install the JAR on a phone or in an emulator.

Installation help: [docs/installing.md](../../docs/installing.md) and [docs/emulator.md](../../docs/emulator.md).

## Technical details

| | |
|---|---|
| MIDlet class | `brickbuster.BrickBusterMIDlet` |
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
tools/build-game.sh 006
```

Output: `games/006-brick-buster/dist/BrickBuster.jar` and `.jad`. See [docs/building.md](../../docs/building.md).

---

Part of [J2ME 100 Games](../../README.md). Original game, released under the [MIT License](../../LICENSE).
