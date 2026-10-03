# Spot Kick

<img src="media/icon.png" width="24" height="24" alt=""> **Sports** &middot; Game 016 of 100 &middot; v1.0.0

> A football penalty shoot-out where the goal is mapped onto your keypad: press 1-9 to place your shot or pick your dive.

<p><img src="media/title.png" alt="Spot Kick title screen" width="176"> <img src="media/play.png" alt="Spot Kick gameplay" width="176"> <img src="media/demo.gif" alt="Spot Kick gameplay animation" width="176"></p>

<sub>Screenshots at 176x208, captured from the real JAR on the project's reference runtime.</sub>

## About

Five kicks each, then sudden death. When shooting, choose one of nine target zones with the matching number key and stop the power meter in the green - blast it and it may sail over the bar, scuff it and the keeper reaches it easily. When saving, watch the run-up and press a number to dive. Keepers get sharper at Pro and Legend level.

**Objective:** Score more penalties than the CPU in the shoot-out.

**Modes:** Amateur, Pro, Legend

## Controls

| Key | Action |
|---|---|
| 1-9 | Aim shot / choose dive zone |
| 2/4/6/8 | Move aim (joystick) |
| 5 | Confirm aim / stop power bar |
| * | Pause menu |

Every game also supports: `5` to select in menus, `*` to pause, `0` on the title screen to toggle sound, and `#` on the title screen for help. Joystick/D-pad and the 2/4/6/8 keys are interchangeable.

## Download

| File | Link |
|---|---|
| JAR (install this) | [SpotKick.jar](https://github.com/agneay/j2me-100-games/releases/download/game-016-v1.0.0/SpotKick.jar) |
| JAD (descriptor for OTA install) | [SpotKick.jad](https://github.com/agneay/j2me-100-games/releases/download/game-016-v1.0.0/SpotKick.jad) |
| Release page | [game-016-v1.0.0](https://github.com/agneay/j2me-100-games/releases/tag/game-016-v1.0.0) |
| All 100 games | [j2me-100-games-v1.0.0.zip](https://github.com/agneay/j2me-100-games/releases/download/v1.0.0/j2me-100-games-v1.0.0.zip) |

**Browser Demo:** [play in your browser](https://agneay.github.io/j2me-100-games/games/spot-kick/). The browser demo is the same Java source compiled to JavaScript with TeaVM and run on the project's MIDP reference runtime; it is a convenience preview, not the original Java ME version. For the authentic experience install the JAR on a phone or in an emulator.

Installation help: [docs/installing.md](../../docs/installing.md) and [docs/emulator.md](../../docs/emulator.md).

## Technical details

| | |
|---|---|
| MIDlet class | `spotkick.SpotKickMIDlet` |
| Platform | Java ME, CLDC 1.0, MIDP 2.0 |
| JAR size | 18.6 KB |
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
tools/build-game.sh 016
```

Output: `games/016-spot-kick/dist/SpotKick.jar` and `.jad`. See [docs/building.md](../../docs/building.md).

---

Part of [J2ME 100 Games](../../README.md). Original game, released under the [MIT License](../../LICENSE).
