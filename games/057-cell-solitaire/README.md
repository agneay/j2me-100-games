# Cell Solitaire

<img src="media/icon.png" width="24" height="24" alt=""> **Card** &middot; Game 057 of 100 &middot; v1.0.0

> The all-cards-face-up patience game with four free cells - nearly every deal is winnable.

<p><img src="media/title.png" alt="Cell Solitaire title screen" width="176"> <img src="media/play.png" alt="Cell Solitaire gameplay" width="176"> <img src="media/demo.gif" alt="Cell Solitaire gameplay animation" width="176"></p>

<sub>Screenshots at 176x208, captured from the real JAR on the project's reference runtime.</sub>

## About

A free-cell style solitaire with full multi-card moves: the game works out how many cards you can move at once from your empty free cells and columns, so you never have to shuffle a run by hand. Safe cards fly to the foundations automatically, # sends any card home, and long columns squeeze to fit small screens. Your fastest solve is saved.

**Objective:** Move all 52 cards to the four foundations, in suit from Ace to King.

## Controls

| Key | Action |
|---|---|
| 4/6 | Move cursor |
| 2/8 | Change row / grow or shrink run |
| 5 | Pick up / drop |
| # | Send card to foundation |
| 0 | Cancel |
| * | Pause menu |

Every game also supports: `5` to select in menus, `*` to pause, `0` on the title screen to toggle sound, and `#` on the title screen for help. Joystick/D-pad and the 2/4/6/8 keys are interchangeable.

## Download

| File | Link |
|---|---|
| JAR (install this) | [CellSolitaire.jar](https://github.com/agneay/j2me-100-games/releases/download/game-057-v1.0.0/CellSolitaire.jar) |
| JAD (descriptor for OTA install) | [CellSolitaire.jad](https://github.com/agneay/j2me-100-games/releases/download/game-057-v1.0.0/CellSolitaire.jad) |
| Release page | [game-057-v1.0.0](https://github.com/agneay/j2me-100-games/releases/tag/game-057-v1.0.0) |
| All 100 games | [j2me-100-games-v1.0.0.zip](https://github.com/agneay/j2me-100-games/releases/download/v1.0.0/j2me-100-games-v1.0.0.zip) |

**Browser Demo:** [play in your browser](https://agneay.github.io/j2me-100-games/games/cell-solitaire/). The browser demo is the same Java source compiled to JavaScript with TeaVM and run on the project's MIDP reference runtime; it is a convenience preview, not the original Java ME version. For the authentic experience install the JAR on a phone or in an emulator.

Installation help: [docs/installing.md](../../docs/installing.md) and [docs/emulator.md](../../docs/emulator.md).

## Technical details

| | |
|---|---|
| MIDlet class | `cellsolitaire.CellSolitaireMIDlet` |
| Platform | Java ME, CLDC 1.0, MIDP 2.0 |
| JAR size | 19.7 KB |
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
tools/build-game.sh 057
```

Output: `games/057-cell-solitaire/dist/CellSolitaire.jar` and `.jad`. See [docs/building.md](../../docs/building.md).

---

Part of [J2ME 100 Games](../../README.md). Original game, released under the [MIT License](../../LICENSE).
