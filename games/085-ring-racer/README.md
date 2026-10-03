# Ring Racer

<img src="media/icon.png" width="24" height="24" alt=""> **Racing** &middot; Game 085 of 100 &middot; v1.0.0

> Fly a behind-the-ship space race through a course of rings - every ring adds time to the clock.

<p><img src="media/title.png" alt="Ring Racer title screen" width="176"> <img src="media/play.png" alt="Ring Racer gameplay" width="176"> <img src="media/demo.gif" alt="Ring Racer gameplay animation" width="176"></p>

<sub>Screenshots at 176x208, captured from the real JAR on the project's reference runtime.</sub>

## About

A pseudo-3D flying racer drawn with integer perspective: rings and mines rush towards you out of a starfield and you steer so each ring passes around your ship. Rings add two seconds and build a streak multiplier, mines knock five seconds off, boost trades control for speed, and the course spreads out and speeds up the further you get.

**Objective:** Fly through as many rings as possible before the clock runs out.

## Controls

| Key | Action |
|---|---|
| 2/4/6/8 | Steer |
| 5 (hold) | Boost |
| * | Pause menu |

Every game also supports: `5` to select in menus, `*` to pause, `0` on the title screen to toggle sound, and `#` on the title screen for help. Joystick/D-pad and the 2/4/6/8 keys are interchangeable.

## Download

| File | Link |
|---|---|
| JAR (install this) | [RingRacer.jar](https://github.com/agneay/j2me-100-games/releases/download/game-085-v1.0.0/RingRacer.jar) |
| JAD (descriptor for OTA install) | [RingRacer.jad](https://github.com/agneay/j2me-100-games/releases/download/game-085-v1.0.0/RingRacer.jad) |
| Release page | [game-085-v1.0.0](https://github.com/agneay/j2me-100-games/releases/tag/game-085-v1.0.0) |
| All 100 games | [j2me-100-games-v1.0.0.zip](https://github.com/agneay/j2me-100-games/releases/download/v1.0.0/j2me-100-games-v1.0.0.zip) |

**Browser Demo:** [play in your browser](https://agneay.github.io/j2me-100-games/games/ring-racer/). The browser demo is the same Java source compiled to JavaScript with TeaVM and run on the project's MIDP reference runtime; it is a convenience preview, not the original Java ME version. For the authentic experience install the JAR on a phone or in an emulator.

Installation help: [docs/installing.md](../../docs/installing.md) and [docs/emulator.md](../../docs/emulator.md).

## Technical details

| | |
|---|---|
| MIDlet class | `ringracer.RingRacerMIDlet` |
| Platform | Java ME, CLDC 1.0, MIDP 2.0 |
| JAR size | 16.7 KB |
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
tools/build-game.sh 085
```

Output: `games/085-ring-racer/dist/RingRacer.jar` and `.jad`. See [docs/building.md](../../docs/building.md).

---

Part of [J2ME 100 Games](../../README.md). Original game, released under the [MIT License](../../LICENSE).
