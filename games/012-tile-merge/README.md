# Tile Merge

<img src="media/icon.png" width="24" height="24" alt=""> **Puzzle** &middot; Game 012 of 100 &middot; v1.0.0

> Slide numbered tiles around the board and merge equal pairs until you build the target tile.

<p><img src="media/title.png" alt="Tile Merge title screen" width="176"> <img src="media/play.png" alt="Tile Merge gameplay" width="176"> <img src="media/demo.gif" alt="Tile Merge gameplay animation" width="176"></p>

<sub>Screenshots at 176x208, captured from the real JAR on the project's reference runtime.</sub>

## About

The sliding number puzzle where equal tiles combine into their sum. Tiles glide smoothly, merged tiles pulse, and one undo per move rescues a slip of the thumb. Three boards: the classic 4x4 race to 2048, a tight 3x3 to 256 and a roomy 5x5 to 4096.

**Objective:** Create a tile with the target number (2048 on 4x4) before the board fills up.

**Modes:** 4x4 to 2048, 3x3 to 256, 5x5 to 4096

## Controls

| Key | Action |
|---|---|
| 2/Up | Slide up |
| 4/Left | Slide left |
| 6/Right | Slide right |
| 8/Down | Slide down |
| 0 | Undo last move |
| * | Pause menu |

Every game also supports: `5` to select in menus, `*` to pause, `0` on the title screen to toggle sound, and `#` on the title screen for help. Joystick/D-pad and the 2/4/6/8 keys are interchangeable.

## Download

| File | Link |
|---|---|
| JAR (install this) | [TileMerge.jar](https://github.com/agneay/j2me-100-games/releases/download/game-012-v1.0.0/TileMerge.jar) |
| JAD (descriptor for OTA install) | [TileMerge.jad](https://github.com/agneay/j2me-100-games/releases/download/game-012-v1.0.0/TileMerge.jad) |
| Release page | [game-012-v1.0.0](https://github.com/agneay/j2me-100-games/releases/tag/game-012-v1.0.0) |
| All 100 games | [j2me-100-games-v1.0.0.zip](https://github.com/agneay/j2me-100-games/releases/download/v1.0.0/j2me-100-games-v1.0.0.zip) |

**Browser Demo:** [play in your browser](https://agneay.github.io/j2me-100-games/games/tile-merge/). The browser demo is the same Java source compiled to JavaScript with TeaVM and run on the project's MIDP reference runtime; it is a convenience preview, not the original Java ME version. For the authentic experience install the JAR on a phone or in an emulator.

Installation help: [docs/installing.md](../../docs/installing.md) and [docs/emulator.md](../../docs/emulator.md).

## Technical details

| | |
|---|---|
| MIDlet class | `tilemerge.TileMergeMIDlet` |
| Platform | Java ME, CLDC 1.0, MIDP 2.0 |
| JAR size | 17.5 KB |
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
tools/build-game.sh 012
```

Output: `games/012-tile-merge/dist/TileMerge.jar` and `.jad`. See [docs/building.md](../../docs/building.md).

---

Part of [J2ME 100 Games](../../README.md). Original game, released under the [MIT License](../../LICENSE).
