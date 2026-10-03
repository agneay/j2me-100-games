# Retro Snake

<img src="media/icon.png" width="24" height="24" alt=""> **Arcade** &middot; Game 001 of 100 &middot; v1.0.0

> Steer a growing snake around the field, eat apples and never bite your own tail.

<p><img src="media/title.png" alt="Retro Snake title screen" width="176"> <img src="media/play.png" alt="Retro Snake gameplay" width="176"> <img src="media/demo.gif" alt="Retro Snake gameplay animation" width="176"></p>

<sub>Screenshots at 176x208, captured from the real JAR on the project's reference runtime.</sub>

## About

The keypad classic, rebuilt from scratch. Every apple makes the snake longer and a little faster, and every fifth apple spawns a fading gold star worth a big bonus. Choose Classic (deadly walls), Wrap (walls wrap around) or Maze (inner walls) on the title screen. Turns are buffered so quick double-taps are never lost.

**Objective:** Eat as many apples as possible without hitting yourself (or a wall in Classic and Maze modes).

**Modes:** Classic, Wrap, Maze

## Controls

| Key | Action |
|---|---|
| 2/Up | Turn up |
| 4/Left | Turn left |
| 6/Right | Turn right |
| 8/Down | Turn down |
| * | Pause menu |

Every game also supports: `5` to select in menus, `*` to pause, `0` on the title screen to toggle sound, and `#` on the title screen for help. Joystick/D-pad and the 2/4/6/8 keys are interchangeable.

## Download

| File | Link |
|---|---|
| JAR (install this) | [RetroSnake.jar](https://github.com/agneay/j2me-100-games/releases/download/game-001-v1.0.0/RetroSnake.jar) |
| JAD (descriptor for OTA install) | [RetroSnake.jad](https://github.com/agneay/j2me-100-games/releases/download/game-001-v1.0.0/RetroSnake.jad) |
| Release page | [game-001-v1.0.0](https://github.com/agneay/j2me-100-games/releases/tag/game-001-v1.0.0) |
| All 100 games | [j2me-100-games-v1.0.0.zip](https://github.com/agneay/j2me-100-games/releases/download/v1.0.0/j2me-100-games-v1.0.0.zip) |

**Browser Demo:** [play in your browser](https://agneay.github.io/j2me-100-games/games/retro-snake/). The browser demo is the same Java source compiled to JavaScript with TeaVM and run on the project's MIDP reference runtime; it is a convenience preview, not the original Java ME version. For the authentic experience install the JAR on a phone or in an emulator.

Installation help: [docs/installing.md](../../docs/installing.md) and [docs/emulator.md](../../docs/emulator.md).

## Technical details

| | |
|---|---|
| MIDlet class | `retrosnake.RetroSnakeMIDlet` |
| Platform | Java ME, CLDC 1.0, MIDP 2.0 |
| JAR size | 17.3 KB |
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
tools/build-game.sh 001
```

Output: `games/001-retro-snake/dist/RetroSnake.jar` and `.jad`. See [docs/building.md](../../docs/building.md).

---

Part of [J2ME 100 Games](../../README.md). Original game, released under the [MIT License](../../LICENSE).
