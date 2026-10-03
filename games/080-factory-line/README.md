# Factory Line

<img src="media/icon.png" width="24" height="24" alt=""> **Simulation** &middot; Game 080 of 100 &middot; v1.0.0

> Run a sorting line: open the right chute with keys 1-3 as each coloured part rolls past.

<p><img src="media/title.png" alt="Factory Line title screen" width="176"> <img src="media/play.png" alt="Factory Line gameplay" width="176"> <img src="media/demo.gif" alt="Factory Line gameplay animation" width="176"></p>

<sub>Screenshots at 176x208, captured from the real JAR on the project's reference runtime.</sub>

## About

A frantic factory sim. Red, green and blue parts (and grey scrap) ride a conveyor past three chutes leading to colour-coded bins. Keys 1, 2 and 3 toggle the chute gates; a part over an open gate drops straight in, and scrap must ride to the end of the belt. Correct sorts build a combo, wrong bins earn strikes, and the belt keeps getting faster.

**Objective:** Sort as many parts as possible before making three mistakes.

## Controls

| Key | Action |
|---|---|
| 1 | Toggle red chute |
| 2 | Toggle green chute |
| 3 | Toggle blue chute |
| * | Pause menu |

Every game also supports: `5` to select in menus, `*` to pause, `0` on the title screen to toggle sound, and `#` on the title screen for help. Joystick/D-pad and the 2/4/6/8 keys are interchangeable.

## Download

| File | Link |
|---|---|
| JAR (install this) | [FactoryLine.jar](https://github.com/agneay/j2me-100-games/releases/download/game-080-v1.0.0/FactoryLine.jar) |
| JAD (descriptor for OTA install) | [FactoryLine.jad](https://github.com/agneay/j2me-100-games/releases/download/game-080-v1.0.0/FactoryLine.jad) |
| Release page | [game-080-v1.0.0](https://github.com/agneay/j2me-100-games/releases/tag/game-080-v1.0.0) |
| All 100 games | [j2me-100-games-v1.0.0.zip](https://github.com/agneay/j2me-100-games/releases/download/v1.0.0/j2me-100-games-v1.0.0.zip) |

**Browser Demo:** [play in your browser](https://agneay.github.io/j2me-100-games/games/factory-line/). The browser demo is the same Java source compiled to JavaScript with TeaVM and run on the project's MIDP reference runtime; it is a convenience preview, not the original Java ME version. For the authentic experience install the JAR on a phone or in an emulator.

Installation help: [docs/installing.md](../../docs/installing.md) and [docs/emulator.md](../../docs/emulator.md).

## Technical details

| | |
|---|---|
| MIDlet class | `factoryline.FactoryLineMIDlet` |
| Platform | Java ME, CLDC 1.0, MIDP 2.0 |
| JAR size | 16.9 KB |
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
tools/build-game.sh 080
```

Output: `games/080-factory-line/dist/FactoryLine.jar` and `.jad`. See [docs/building.md](../../docs/building.md).

---

Part of [J2ME 100 Games](../../README.md). Original game, released under the [MIT License](../../LICENSE).
