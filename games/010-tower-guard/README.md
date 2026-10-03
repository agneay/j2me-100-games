# Tower Guard

<img src="media/icon.png" width="24" height="24" alt=""> **Strategy** &middot; Game 010 of 100 &middot; v1.0.0

> Build and upgrade Arrow, Cannon and Frost towers along the road to hold back 20 waves of monsters.

<p><img src="media/title.png" alt="Tower Guard title screen" width="176"> <img src="media/play.png" alt="Tower Guard gameplay" width="176"> <img src="media/demo.gif" alt="Tower Guard gameplay animation" width="176"></p>

<sub>Screenshots at 176x208, captured from the real JAR on the project's reference runtime.</sub>

## About

A compact tower defence designed around a cursor and the 5 key. Three tower types each have three upgrade levels, monsters come in normal, fast and armoured varieties, every fifth wave ends with a boss, and you can call waves early with # for bonus gold. Three maps: Meadow, Canyon and the long Spiral.

**Objective:** Survive all 20 waves without losing your 20 lives.

**Modes:** Meadow, Canyon, Spiral

## Controls

| Key | Action |
|---|---|
| 2/4/6/8 | Move cursor |
| 5 | Build / upgrade / sell menu |
| 4/6 | Choose in menus |
| 0 | Close menu |
| # | Call next wave early |
| * | Pause menu |

Every game also supports: `5` to select in menus, `*` to pause, `0` on the title screen to toggle sound, and `#` on the title screen for help. Joystick/D-pad and the 2/4/6/8 keys are interchangeable.

## Download

| File | Link |
|---|---|
| JAR (install this) | [TowerGuard.jar](https://github.com/agneay/j2me-100-games/releases/download/game-010-v1.0.0/TowerGuard.jar) |
| JAD (descriptor for OTA install) | [TowerGuard.jad](https://github.com/agneay/j2me-100-games/releases/download/game-010-v1.0.0/TowerGuard.jad) |
| Release page | [game-010-v1.0.0](https://github.com/agneay/j2me-100-games/releases/tag/game-010-v1.0.0) |
| All 100 games | [j2me-100-games-v1.0.0.zip](https://github.com/agneay/j2me-100-games/releases/download/v1.0.0/j2me-100-games-v1.0.0.zip) |

**Browser Demo:** [play in your browser](https://agneay.github.io/j2me-100-games/games/tower-guard/). The browser demo is the same Java source compiled to JavaScript with TeaVM and run on the project's MIDP reference runtime; it is a convenience preview, not the original Java ME version. For the authentic experience install the JAR on a phone or in an emulator.

Installation help: [docs/installing.md](../../docs/installing.md) and [docs/emulator.md](../../docs/emulator.md).

## Technical details

| | |
|---|---|
| MIDlet class | `towerguard.TowerGuardMIDlet` |
| Platform | Java ME, CLDC 1.0, MIDP 2.0 |
| JAR size | 19.8 KB |
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
tools/build-game.sh 010
```

Output: `games/010-tower-guard/dist/TowerGuard.jar` and `.jad`. See [docs/building.md](../../docs/building.md).

---

Part of [J2ME 100 Games](../../README.md). Original game, released under the [MIT License](../../LICENSE).
