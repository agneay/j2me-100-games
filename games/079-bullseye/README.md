# Bullseye

<img src="media/icon.png" width="24" height="24" alt=""> **Sports** &middot; Game 079 of 100 &middot; v1.0.0

> Target archery: steer a swaying sight, hold your breath to steady it and allow for the wind.

<p><img src="media/title.png" alt="Bullseye title screen" width="176"> <img src="media/play.png" alt="Bullseye gameplay" width="176"> <img src="media/demo.gif" alt="Bullseye gameplay animation" width="176"></p>

<sub>Screenshots at 176x208, captured from the real JAR on the project's reference runtime.</sub>

## About

Twelve arrows across three distances - 30, 50 and 70 metres. The sight drifts with your breathing and sways more at long range; hold 0 to steady it, but your breath runs out and the sway gets worse while you recover. A wind flag shows how far each arrow will drift, so aim off into the wind. Ten-ring scoring with every arrow hole left in the target face.

**Objective:** Score as many points as possible with twelve arrows (maximum 120).

## Controls

| Key | Action |
|---|---|
| 2/4/6/8 | Aim |
| 5 | Shoot |
| 0 (hold) | Hold breath to steady the aim |
| * | Pause menu |

Every game also supports: `5` to select in menus, `*` to pause, `0` on the title screen to toggle sound, and `#` on the title screen for help. Joystick/D-pad and the 2/4/6/8 keys are interchangeable.

## Download

| File | Link |
|---|---|
| JAR (install this) | [Bullseye.jar](https://github.com/agneay/j2me-100-games/releases/download/game-079-v1.0.0/Bullseye.jar) |
| JAD (descriptor for OTA install) | [Bullseye.jad](https://github.com/agneay/j2me-100-games/releases/download/game-079-v1.0.0/Bullseye.jad) |
| Release page | [game-079-v1.0.0](https://github.com/agneay/j2me-100-games/releases/tag/game-079-v1.0.0) |
| All 100 games | [j2me-100-games-v1.0.0.zip](https://github.com/agneay/j2me-100-games/releases/download/v1.0.0/j2me-100-games-v1.0.0.zip) |

**Browser Demo:** [play in your browser](https://agneay.github.io/j2me-100-games/games/bullseye/). The browser demo is the same Java source compiled to JavaScript with TeaVM and run on the project's MIDP reference runtime; it is a convenience preview, not the original Java ME version. For the authentic experience install the JAR on a phone or in an emulator.

Installation help: [docs/installing.md](../../docs/installing.md) and [docs/emulator.md](../../docs/emulator.md).

## Technical details

| | |
|---|---|
| MIDlet class | `bullseye.BullseyeMIDlet` |
| Platform | Java ME, CLDC 1.0, MIDP 2.0 |
| JAR size | 17.0 KB |
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
tools/build-game.sh 079
```

Output: `games/079-bullseye/dist/Bullseye.jar` and `.jad`. See [docs/building.md](../../docs/building.md).

---

Part of [J2ME 100 Games](../../README.md). Original game, released under the [MIT License](../../LICENSE).
