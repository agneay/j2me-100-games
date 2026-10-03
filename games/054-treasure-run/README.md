# Treasure Run

<img src="media/icon.png" width="24" height="24" alt=""> **Platformer** &middot; Game 054 of 100 &middot; v1.0.0

> An endless temple runner: jump pits and crates, slide under beams and scoop up gems as the pace builds.

<p><img src="media/title.png" alt="Treasure Run title screen" width="176"> <img src="media/play.png" alt="Treasure Run gameplay" width="176"> <img src="media/demo.gif" alt="Treasure Run gameplay animation" width="176"></p>

<sub>Screenshots at 176x208, captured from the real JAR on the project's reference runtime.</sub>

## About

Your explorer sprints automatically; you decide when to jump, double-jump and slide. Pits, crates and low stone beams come thicker and faster the further you get, gems arc over the obstacles to tempt risky jumps, and a rare shield orb absorbs one hit. Score is distance plus ten per gem.

**Objective:** Run as far as you can without falling or crashing.

## Controls

| Key | Action |
|---|---|
| 2/5 | Jump (again in the air to double jump) |
| 8 | Slide |
| * | Pause menu |

Every game also supports: `5` to select in menus, `*` to pause, `0` on the title screen to toggle sound, and `#` on the title screen for help. Joystick/D-pad and the 2/4/6/8 keys are interchangeable.

## Download

| File | Link |
|---|---|
| JAR (install this) | [TreasureRun.jar](https://github.com/agneay/j2me-100-games/releases/download/game-054-v1.0.0/TreasureRun.jar) |
| JAD (descriptor for OTA install) | [TreasureRun.jad](https://github.com/agneay/j2me-100-games/releases/download/game-054-v1.0.0/TreasureRun.jad) |
| Release page | [game-054-v1.0.0](https://github.com/agneay/j2me-100-games/releases/tag/game-054-v1.0.0) |
| All 100 games | [j2me-100-games-v1.0.0.zip](https://github.com/agneay/j2me-100-games/releases/download/v1.0.0/j2me-100-games-v1.0.0.zip) |

**Browser Demo:** [play in your browser](https://agneay.github.io/j2me-100-games/games/treasure-run/). The browser demo is the same Java source compiled to JavaScript with TeaVM and run on the project's MIDP reference runtime; it is a convenience preview, not the original Java ME version. For the authentic experience install the JAR on a phone or in an emulator.

Installation help: [docs/installing.md](../../docs/installing.md) and [docs/emulator.md](../../docs/emulator.md).

## Technical details

| | |
|---|---|
| MIDlet class | `treasurerun.TreasureRunMIDlet` |
| Platform | Java ME, CLDC 1.0, MIDP 2.0 |
| JAR size | 17.8 KB |
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
tools/build-game.sh 054
```

Output: `games/054-treasure-run/dist/TreasureRun.jar` and `.jad`. See [docs/building.md](../../docs/building.md).

---

Part of [J2ME 100 Games](../../README.md). Original game, released under the [MIT License](../../LICENSE).
