# Domino Line

<img src="media/icon.png" width="24" height="24" alt=""> **Board** &middot; Game 087 of 100 &middot; v1.0.0

> Draw dominoes against the CPU with a double-six set: match the open ends, draw when stuck, go out first.

<p><img src="media/title.png" alt="Domino Line title screen" width="176"> <img src="media/play.png" alt="Domino Line gameplay" width="176"> <img src="media/demo.gif" alt="Domino Line gameplay animation" width="176"></p>

<sub>Screenshots at 176x208, captured from the real JAR on the project's reference runtime.</sub>

## About

A clean game of draw dominoes. The highest double opens, tiles must match one of the line's two open ends, and if you can't play you draw from the boneyard. Press 1 or 3 to choose which end, or 5 to let the game pick the end that fits; playable tiles light up in your hand. The CPU dumps its heaviest tiles first. Going out scores the pips left in your opponent's hand; a blocked game goes to the lighter hand.

**Objective:** Play all your dominoes before the CPU does.

## Controls

| Key | Action |
|---|---|
| 4/6 | Choose tile |
| 1 | Play on left end |
| 3 | Play on right end |
| 5 | Play on any fitting end |
| # | Draw / pass |
| * | Pause menu |

Every game also supports: `5` to select in menus, `*` to pause, `0` on the title screen to toggle sound, and `#` on the title screen for help. Joystick/D-pad and the 2/4/6/8 keys are interchangeable.

## Download

| File | Link |
|---|---|
| JAR (install this) | [DominoLine.jar](https://github.com/agneay/j2me-100-games/releases/download/game-087-v1.0.0/DominoLine.jar) |
| JAD (descriptor for OTA install) | [DominoLine.jad](https://github.com/agneay/j2me-100-games/releases/download/game-087-v1.0.0/DominoLine.jad) |
| Release page | [game-087-v1.0.0](https://github.com/agneay/j2me-100-games/releases/tag/game-087-v1.0.0) |
| All 100 games | [j2me-100-games-v1.0.0.zip](https://github.com/agneay/j2me-100-games/releases/download/v1.0.0/j2me-100-games-v1.0.0.zip) |

**Browser Demo:** [play in your browser](https://agneay.github.io/j2me-100-games/games/domino-line/). The browser demo is the same Java source compiled to JavaScript with TeaVM and run on the project's MIDP reference runtime; it is a convenience preview, not the original Java ME version. For the authentic experience install the JAR on a phone or in an emulator.

Installation help: [docs/installing.md](../../docs/installing.md) and [docs/emulator.md](../../docs/emulator.md).

## Technical details

| | |
|---|---|
| MIDlet class | `dominoline.DominoLineMIDlet` |
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
tools/build-game.sh 087
```

Output: `games/087-domino-line/dist/DominoLine.jar` and `.jad`. See [docs/building.md](../../docs/building.md).

---

Part of [J2ME 100 Games](../../README.md). Original game, released under the [MIT License](../../LICENSE).
