# Checkpoint Rally

<img src="media/icon.png" width="24" height="24" alt=""> **Racing** &middot; Game 034 of 100 &middot; v1.0.0

> A top-down rally time trial: steer a free-rotating car through numbered checkpoints over two laps.

<p><img src="media/title.png" alt="Checkpoint Rally title screen" width="176"> <img src="media/play.png" alt="Checkpoint Rally gameplay" width="176"> <img src="media/demo.gif" alt="Checkpoint Rally gameplay animation" width="176"></p>

<sub>Screenshots at 176x208, captured from the real JAR on the project's reference runtime.</sub>

## About

Rally driving viewed from above on a scrolling 40x40 stage. The car rotates smoothly through 256 headings with speed-sensitive steering; dirt road is fast, grass and water slow you down and trees stop you dead. An on-screen arrow always points to the next checkpoint. Two stages - Forest and Lakeside - each with a saved best time.

**Objective:** Pass every checkpoint in order and complete two laps in the fastest time.

**Modes:** Forest stage, Lakeside stage

## Controls

| Key | Action |
|---|---|
| 2/5 | Accelerate |
| 8 | Brake / reverse |
| 4/6 | Steer |
| * | Pause menu |

Every game also supports: `5` to select in menus, `*` to pause, `0` on the title screen to toggle sound, and `#` on the title screen for help. Joystick/D-pad and the 2/4/6/8 keys are interchangeable.

## Download

| File | Link |
|---|---|
| JAR (install this) | [CheckpointRally.jar](https://github.com/agneay/j2me-100-games/releases/download/game-034-v1.0.0/CheckpointRally.jar) |
| JAD (descriptor for OTA install) | [CheckpointRally.jad](https://github.com/agneay/j2me-100-games/releases/download/game-034-v1.0.0/CheckpointRally.jad) |
| Release page | [game-034-v1.0.0](https://github.com/agneay/j2me-100-games/releases/tag/game-034-v1.0.0) |
| All 100 games | [j2me-100-games-v1.0.0.zip](https://github.com/agneay/j2me-100-games/releases/download/v1.0.0/j2me-100-games-v1.0.0.zip) |

**Browser Demo:** [play in your browser](https://agneay.github.io/j2me-100-games/games/checkpoint-rally/). The browser demo is the same Java source compiled to JavaScript with TeaVM and run on the project's MIDP reference runtime; it is a convenience preview, not the original Java ME version. For the authentic experience install the JAR on a phone or in an emulator.

Installation help: [docs/installing.md](../../docs/installing.md) and [docs/emulator.md](../../docs/emulator.md).

## Technical details

| | |
|---|---|
| MIDlet class | `checkpointrally.CheckpointRallyMIDlet` |
| Platform | Java ME, CLDC 1.0, MIDP 2.0 |
| JAR size | 18.1 KB |
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
tools/build-game.sh 034
```

Output: `games/034-checkpoint-rally/dist/CheckpointRally.jar` and `.jad`. See [docs/building.md](../../docs/building.md).

---

Part of [J2ME 100 Games](../../README.md). Original game, released under the [MIT License](../../LICENSE).
