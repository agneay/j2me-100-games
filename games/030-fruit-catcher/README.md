# Fruit Catcher

<img src="media/icon.png" width="24" height="24" alt=""> **Arcade** &middot; Game 030 of 100 &middot; v1.0.0

> Slide your basket to catch falling fruit, chain combos and dodge the bombs.

<p><img src="media/title.png" alt="Fruit Catcher title screen" width="176"> <img src="media/play.png" alt="Fruit Catcher gameplay" width="176"> <img src="media/demo.gif" alt="Fruit Catcher gameplay animation" width="176"></p>

<sub>Screenshots at 176x208, captured from the real JAR on the project's reference runtime.</sub>

## About

A quick-fire catch game with pixel-art fruit. Apples, bananas, grapes and melons are worth more the rarer they are, golden stars are worth 100, and catching in a row builds a multiplier. Magnets widen the basket, clocks slow time, and bombs cost one of your three hearts. Drop ten pieces of fruit and it's over. The pace rises every 20 seconds.

**Objective:** Score as many points as possible without catching three bombs or dropping ten fruits.

## Controls

| Key | Action |
|---|---|
| 4/Left | Move basket left |
| 6/Right | Move basket right |
| * | Pause menu |

Every game also supports: `5` to select in menus, `*` to pause, `0` on the title screen to toggle sound, and `#` on the title screen for help. Joystick/D-pad and the 2/4/6/8 keys are interchangeable.

## Download

| File | Link |
|---|---|
| JAR (install this) | [FruitCatcher.jar](https://github.com/agneay/j2me-100-games/releases/download/game-030-v1.0.0/FruitCatcher.jar) |
| JAD (descriptor for OTA install) | [FruitCatcher.jad](https://github.com/agneay/j2me-100-games/releases/download/game-030-v1.0.0/FruitCatcher.jad) |
| Release page | [game-030-v1.0.0](https://github.com/agneay/j2me-100-games/releases/tag/game-030-v1.0.0) |
| All 100 games | [j2me-100-games-v1.0.0.zip](https://github.com/agneay/j2me-100-games/releases/download/v1.0.0/j2me-100-games-v1.0.0.zip) |

**Browser Demo:** [play in your browser](https://agneay.github.io/j2me-100-games/games/fruit-catcher/). The browser demo is the same Java source compiled to JavaScript with TeaVM and run on the project's MIDP reference runtime; it is a convenience preview, not the original Java ME version. For the authentic experience install the JAR on a phone or in an emulator.

Installation help: [docs/installing.md](../../docs/installing.md) and [docs/emulator.md](../../docs/emulator.md).

## Technical details

| | |
|---|---|
| MIDlet class | `fruitcatcher.FruitCatcherMIDlet` |
| Platform | Java ME, CLDC 1.0, MIDP 2.0 |
| JAR size | 17.9 KB |
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
tools/build-game.sh 030
```

Output: `games/030-fruit-catcher/dist/FruitCatcher.jar` and `.jad`. See [docs/building.md](../../docs/building.md).

---

Part of [J2ME 100 Games](../../README.md). Original game, released under the [MIT License](../../LICENSE).
