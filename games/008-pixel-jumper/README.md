# Pixel Jumper

<img src="media/icon.png" width="24" height="24" alt=""> **Platformer** &middot; Game 008 of 100 &middot; v1.0.0

> Run, jump and stomp through eight scrolling levels of pits, spikes, springs and walking blobs.

<p><img src="media/title.png" alt="Pixel Jumper title screen" width="176"> <img src="media/play.png" alt="Pixel Jumper gameplay" width="176"> <img src="media/demo.gif" alt="Pixel Jumper gameplay animation" width="176"></p>

<sub>Screenshots at 176x208, captured from the real JAR on the project's reference runtime.</sub>

## About

A side-scrolling platformer with variable-height jumps, coyote time and jump buffering so the keypad feels responsive. Levels come from a seeded generator that only builds gaps and ledges within jumping range, so all eight stages are the same every time you play and always beatable. Collect 100 coins for an extra life.

**Objective:** Reach the flag at the end of all eight levels.

## Controls

| Key | Action |
|---|---|
| 4/6 | Run left / right |
| 2/5 | Jump (hold for higher) |
| 1/3 | Jump left / right |
| 8 | Drop through wooden platforms |
| * | Pause menu |

Every game also supports: `5` to select in menus, `*` to pause, `0` on the title screen to toggle sound, and `#` on the title screen for help. Joystick/D-pad and the 2/4/6/8 keys are interchangeable.

## Download

| File | Link |
|---|---|
| JAR (install this) | [PixelJumper.jar](https://github.com/agneay/j2me-100-games/releases/download/game-008-v1.0.0/PixelJumper.jar) |
| JAD (descriptor for OTA install) | [PixelJumper.jad](https://github.com/agneay/j2me-100-games/releases/download/game-008-v1.0.0/PixelJumper.jad) |
| Release page | [game-008-v1.0.0](https://github.com/agneay/j2me-100-games/releases/tag/game-008-v1.0.0) |
| All 100 games | [j2me-100-games-v1.0.0.zip](https://github.com/agneay/j2me-100-games/releases/download/v1.0.0/j2me-100-games-v1.0.0.zip) |

**Browser Demo:** [play in your browser](https://agneay.github.io/j2me-100-games/games/pixel-jumper/). The browser demo is the same Java source compiled to JavaScript with TeaVM and run on the project's MIDP reference runtime; it is a convenience preview, not the original Java ME version. For the authentic experience install the JAR on a phone or in an emulator.

Installation help: [docs/installing.md](../../docs/installing.md) and [docs/emulator.md](../../docs/emulator.md).

## Technical details

| | |
|---|---|
| MIDlet class | `pixeljumper.PixelJumperMIDlet` |
| Platform | Java ME, CLDC 1.0, MIDP 2.0 |
| JAR size | 22.2 KB |
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
tools/build-game.sh 008
```

Output: `games/008-pixel-jumper/dist/PixelJumper.jar` and `.jad`. See [docs/building.md](../../docs/building.md).

---

Part of [J2ME 100 Games](../../README.md). Original game, released under the [MIT License](../../LICENSE).
