# Sea Strike

<img src="media/icon.png" width="24" height="24" alt=""> **Strategy** &middot; Game 024 of 100 &middot; v1.0.0

> Hunt the enemy's hidden fleet on an 8x8 sea before their admiral finds yours.

<p><img src="media/title.png" alt="Sea Strike title screen" width="176"> <img src="media/play.png" alt="Sea Strike gameplay" width="176"> <img src="media/demo.gif" alt="Sea Strike gameplay animation" width="176"></p>

<sub>Screenshots at 176x208, captured from the real JAR on the project's reference runtime.</sub>

## About

A hidden-fleet naval duel. Shuffle your fleet until you like the layout, then trade shots with a CPU admiral. Cadet fires at random, Captain hunts around every hit, and Admiral also sweeps the sea in a checkerboard and follows ship lines. Fewer shots means a higher score.

**Objective:** Sink all five enemy ships before your own fleet is destroyed.

**Modes:** Cadet, Captain, Admiral

## Controls

| Key | Action |
|---|---|
| 2/4/6/8 | Aim |
| 5 | Fire / set sail |
| # | Shuffle your fleet (before the battle) |
| * | Pause menu |

Every game also supports: `5` to select in menus, `*` to pause, `0` on the title screen to toggle sound, and `#` on the title screen for help. Joystick/D-pad and the 2/4/6/8 keys are interchangeable.

## Download

| File | Link |
|---|---|
| JAR (install this) | [SeaStrike.jar](https://github.com/agneay/j2me-100-games/releases/download/game-024-v1.0.0/SeaStrike.jar) |
| JAD (descriptor for OTA install) | [SeaStrike.jad](https://github.com/agneay/j2me-100-games/releases/download/game-024-v1.0.0/SeaStrike.jad) |
| Release page | [game-024-v1.0.0](https://github.com/agneay/j2me-100-games/releases/tag/game-024-v1.0.0) |
| All 100 games | [j2me-100-games-v1.0.0.zip](https://github.com/agneay/j2me-100-games/releases/download/v1.0.0/j2me-100-games-v1.0.0.zip) |

**Browser Demo:** [play in your browser](https://agneay.github.io/j2me-100-games/games/sea-strike/). The browser demo is the same Java source compiled to JavaScript with TeaVM and run on the project's MIDP reference runtime; it is a convenience preview, not the original Java ME version. For the authentic experience install the JAR on a phone or in an emulator.

Installation help: [docs/installing.md](../../docs/installing.md) and [docs/emulator.md](../../docs/emulator.md).

## Technical details

| | |
|---|---|
| MIDlet class | `seastrike.SeaStrikeMIDlet` |
| Platform | Java ME, CLDC 1.0, MIDP 2.0 |
| JAR size | 18.3 KB |
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
tools/build-game.sh 024
```

Output: `games/024-sea-strike/dist/SeaStrike.jar` and `.jad`. See [docs/building.md](../../docs/building.md).

---

Part of [J2ME 100 Games](../../README.md). Original game, released under the [MIT License](../../LICENSE).
