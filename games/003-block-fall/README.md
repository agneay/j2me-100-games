# Block Fall

<img src="media/icon.png" width="24" height="24" alt=""> **Puzzle** &middot; Game 003 of 100 &middot; v1.0.0

> Rotate and drop falling four-block pieces to clear rows, in endless Marathon or a 40-line Sprint.

<p><img src="media/title.png" alt="Block Fall title screen" width="176"> <img src="media/play.png" alt="Block Fall gameplay" width="176"> <img src="media/demo.gif" alt="Block Fall gameplay animation" width="176"></p>

<sub>Screenshots at 176x208, captured from the real JAR on the project's reference runtime.</sub>

## About

A falling-blocks puzzle tuned for a phone keypad: delayed auto-shift on 4 and 6, rotation in both directions, a ghost piece showing where the block lands, hard drop on 0 or #, and a fair shuffled bag so you never wait forever for a long piece. Marathon speeds up every ten lines; Sprint 40 is a race against the clock.

**Objective:** Clear rows by filling them completely. Marathon: survive and score. Sprint 40: clear 40 lines in the shortest time.

**Modes:** Marathon, Sprint 40

## Controls

| Key | Action |
|---|---|
| 4/6 | Move left / right |
| 8 | Soft drop |
| 2/5 | Rotate clockwise |
| 1 | Rotate anticlockwise |
| 0/# | Hard drop |
| * | Pause menu |

Every game also supports: `5` to select in menus, `*` to pause, `0` on the title screen to toggle sound, and `#` on the title screen for help. Joystick/D-pad and the 2/4/6/8 keys are interchangeable.

## Download

| File | Link |
|---|---|
| JAR (install this) | [BlockFall.jar](https://github.com/agneay/j2me-100-games/releases/download/game-003-v1.0.0/BlockFall.jar) |
| JAD (descriptor for OTA install) | [BlockFall.jad](https://github.com/agneay/j2me-100-games/releases/download/game-003-v1.0.0/BlockFall.jad) |
| Release page | [game-003-v1.0.0](https://github.com/agneay/j2me-100-games/releases/tag/game-003-v1.0.0) |
| All 100 games | [j2me-100-games-v1.0.0.zip](https://github.com/agneay/j2me-100-games/releases/download/v1.0.0/j2me-100-games-v1.0.0.zip) |

**Browser Demo:** [play in your browser](https://agneay.github.io/j2me-100-games/games/block-fall/). The browser demo is the same Java source compiled to JavaScript with TeaVM and run on the project's MIDP reference runtime; it is a convenience preview, not the original Java ME version. For the authentic experience install the JAR on a phone or in an emulator.

Installation help: [docs/installing.md](../../docs/installing.md) and [docs/emulator.md](../../docs/emulator.md).

## Technical details

| | |
|---|---|
| MIDlet class | `blockfall.BlockFallMIDlet` |
| Platform | Java ME, CLDC 1.0, MIDP 2.0 |
| JAR size | 18.4 KB |
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
tools/build-game.sh 003
```

Output: `games/003-block-fall/dist/BlockFall.jar` and `.jad`. See [docs/building.md](../../docs/building.md).

---

Part of [J2ME 100 Games](../../README.md). Original game, released under the [MIT License](../../LICENSE).
