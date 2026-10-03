# Pocket Saga

<img src="media/icon.png" width="24" height="24" alt=""> **RPG** &middot; Game 058 of 100 &middot; v1.0.0

> A tiny console-style RPG: roam the overworld, fight random battles, shop in towns and defeat the Dark Knight.

<p><img src="media/title.png" alt="Pocket Saga title screen" width="176"> <img src="media/play.png" alt="Pocket Saga gameplay" width="176"> <img src="media/demo.gif" alt="Pocket Saga gameplay animation" width="176"></p>

<sub>Screenshots at 176x208, captured from the real JAR on the project's reference runtime.</sub>

## About

A complete pocket role-playing adventure. Walk a scrolling overworld of plains, forests, rivers and mountains where monsters grow tougher the further you roam. Battles are turn-based with Attack, Magic (heal or fire), Potions and Run. Two towns offer an inn and a shop for potions and weapon and armour upgrades, a cave hides the Hero Sword behind three fights in a row, and the Dark Knight waits in the castle to the north-east.

**Objective:** Grow strong enough to defeat the Dark Knight in his castle.

## Controls

| Key | Action |
|---|---|
| 2/4/6/8 | Walk / choose option |
| 5 | Confirm |
| * | Pause menu |

Every game also supports: `5` to select in menus, `*` to pause, `0` on the title screen to toggle sound, and `#` on the title screen for help. Joystick/D-pad and the 2/4/6/8 keys are interchangeable.

## Download

| File | Link |
|---|---|
| JAR (install this) | [PocketSaga.jar](https://github.com/agneay/j2me-100-games/releases/download/game-058-v1.0.0/PocketSaga.jar) |
| JAD (descriptor for OTA install) | [PocketSaga.jad](https://github.com/agneay/j2me-100-games/releases/download/game-058-v1.0.0/PocketSaga.jad) |
| Release page | [game-058-v1.0.0](https://github.com/agneay/j2me-100-games/releases/tag/game-058-v1.0.0) |
| All 100 games | [j2me-100-games-v1.0.0.zip](https://github.com/agneay/j2me-100-games/releases/download/v1.0.0/j2me-100-games-v1.0.0.zip) |

**Browser Demo:** [play in your browser](https://agneay.github.io/j2me-100-games/games/pocket-saga/). The browser demo is the same Java source compiled to JavaScript with TeaVM and run on the project's MIDP reference runtime; it is a convenience preview, not the original Java ME version. For the authentic experience install the JAR on a phone or in an emulator.

Installation help: [docs/installing.md](../../docs/installing.md) and [docs/emulator.md](../../docs/emulator.md).

## Technical details

| | |
|---|---|
| MIDlet class | `pocketsaga.PocketSagaMIDlet` |
| Platform | Java ME, CLDC 1.0, MIDP 2.0 |
| JAR size | 20.7 KB |
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
tools/build-game.sh 058
```

Output: `games/058-pocket-saga/dist/PocketSaga.jar` and `.jad`. See [docs/building.md](../../docs/building.md).

---

Part of [J2ME 100 Games](../../README.md). Original game, released under the [MIT License](../../LICENSE).
