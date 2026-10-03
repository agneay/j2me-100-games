# Slide Fifteen

<img src="media/icon.png" width="24" height="24" alt=""> **Puzzle** &middot; Game 031 of 100 &middot; v1.0.0

> The sliding tile puzzle: put the numbers back in order by sliding tiles into the gap.

<p><img src="media/title.png" alt="Slide Fifteen title screen" width="176"> <img src="media/play.png" alt="Slide Fifteen gameplay" width="176"> <img src="media/demo.gif" alt="Slide Fifteen gameplay animation" width="176"></p>

<sub>Screenshots at 176x208, captured from the real JAR on the project's reference runtime.</sub>

## About

The pocket puzzle that predates phones, in three sizes: the quick 3x3 Eight, the classic 4x4 Fifteen and the brain-melting 5x5 Twenty-four. Shuffles are generated from legal moves, so every board is solvable. Tiles slide smoothly, rows are colour-coded and tiles already in their home square glow. Your lowest move count for each size is saved.

**Objective:** Restore the tiles to numerical order in as few moves as possible.

**Modes:** 4x4 Fifteen, 3x3 Eight, 5x5 Twenty-four

## Controls

| Key | Action |
|---|---|
| 2/Up | Slide tile up |
| 8/Down | Slide tile down |
| 4/Left | Slide tile left |
| 6/Right | Slide tile right |
| * | Pause menu |

Every game also supports: `5` to select in menus, `*` to pause, `0` on the title screen to toggle sound, and `#` on the title screen for help. Joystick/D-pad and the 2/4/6/8 keys are interchangeable.

## Download

| File | Link |
|---|---|
| JAR (install this) | [SlideFifteen.jar](https://github.com/agneay/j2me-100-games/releases/download/game-031-v1.0.0/SlideFifteen.jar) |
| JAD (descriptor for OTA install) | [SlideFifteen.jad](https://github.com/agneay/j2me-100-games/releases/download/game-031-v1.0.0/SlideFifteen.jad) |
| Release page | [game-031-v1.0.0](https://github.com/agneay/j2me-100-games/releases/tag/game-031-v1.0.0) |
| All 100 games | [j2me-100-games-v1.0.0.zip](https://github.com/agneay/j2me-100-games/releases/download/v1.0.0/j2me-100-games-v1.0.0.zip) |

**Browser Demo:** [play in your browser](https://agneay.github.io/j2me-100-games/games/slide-fifteen/). The browser demo is the same Java source compiled to JavaScript with TeaVM and run on the project's MIDP reference runtime; it is a convenience preview, not the original Java ME version. For the authentic experience install the JAR on a phone or in an emulator.

Installation help: [docs/installing.md](../../docs/installing.md) and [docs/emulator.md](../../docs/emulator.md).

## Technical details

| | |
|---|---|
| MIDlet class | `slidefifteen.SlideFifteenMIDlet` |
| Platform | Java ME, CLDC 1.0, MIDP 2.0 |
| JAR size | 16.5 KB |
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
tools/build-game.sh 031
```

Output: `games/031-slide-fifteen/dist/SlideFifteen.jar` and `.jad`. See [docs/building.md](../../docs/building.md).

---

Part of [J2ME 100 Games](../../README.md). Original game, released under the [MIT License](../../LICENSE).
