# Light Trails

<img src="media/icon.png" width="24" height="24" alt=""> **Arcade** &middot; Game 051 of 100 &middot; v1.0.0

> Light-cycle duels: leave walls of light behind you and be the last rider standing.

<p><img src="media/title.png" alt="Light Trails title screen" width="176"> <img src="media/play.png" alt="Light Trails gameplay" width="176"> <img src="media/demo.gif" alt="Light Trails gameplay animation" width="176"></p>

<sub>Screenshots at 176x208, captured from the real JAR on the project's reference runtime.</sub>

## About

Arena survival against one or three AI riders. Every bike paints a permanent wall as it moves; the AI chooses turns by measuring how much open space each direction leaves it, so it fights for territory rather than driving randomly. Turns are buffered so quick taps register, and a rechargeable boost lets you cut opponents off. First to three round wins takes the match.

**Objective:** Win three rounds by being the last light cycle still riding.

**Modes:** 1 rival, 3 rivals

## Controls

| Key | Action |
|---|---|
| 2/4/6/8 | Turn |
| 5 (hold) | Boost |
| * | Pause menu |

Every game also supports: `5` to select in menus, `*` to pause, `0` on the title screen to toggle sound, and `#` on the title screen for help. Joystick/D-pad and the 2/4/6/8 keys are interchangeable.

## Download

| File | Link |
|---|---|
| JAR (install this) | [LightTrails.jar](https://github.com/agneay/j2me-100-games/releases/download/game-051-v1.0.0/LightTrails.jar) |
| JAD (descriptor for OTA install) | [LightTrails.jad](https://github.com/agneay/j2me-100-games/releases/download/game-051-v1.0.0/LightTrails.jad) |
| Release page | [game-051-v1.0.0](https://github.com/agneay/j2me-100-games/releases/tag/game-051-v1.0.0) |
| All 100 games | [j2me-100-games-v1.0.0.zip](https://github.com/agneay/j2me-100-games/releases/download/v1.0.0/j2me-100-games-v1.0.0.zip) |

**Browser Demo:** [play in your browser](https://agneay.github.io/j2me-100-games/games/light-trails/). The browser demo is the same Java source compiled to JavaScript with TeaVM and run on the project's MIDP reference runtime; it is a convenience preview, not the original Java ME version. For the authentic experience install the JAR on a phone or in an emulator.

Installation help: [docs/installing.md](../../docs/installing.md) and [docs/emulator.md](../../docs/emulator.md).

## Technical details

| | |
|---|---|
| MIDlet class | `lighttrails.LightTrailsMIDlet` |
| Platform | Java ME, CLDC 1.0, MIDP 2.0 |
| JAR size | 17.6 KB |
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
tools/build-game.sh 051
```

Output: `games/051-light-trails/dist/LightTrails.jar` and `.jad`. See [docs/building.md](../../docs/building.md).

---

Part of [J2ME 100 Games](../../README.md). Original game, released under the [MIT License](../../LICENSE).
