# Memory Cards

<img src="media/icon.png" width="24" height="24" alt=""> **Card** &middot; Game 077 of 100 &middot; v1.0.0

> Turn over cards two at a time and find all the matching pairs - solo or in a duel against a forgetful CPU.

<p><img src="media/title.png" alt="Memory Cards title screen" width="176"> <img src="media/play.png" alt="Memory Cards gameplay" width="176"> <img src="media/demo.gif" alt="Memory Cards gameplay animation" width="176"></p>

<sub>Screenshots at 176x208, captured from the real JAR on the project's reference runtime.</sub>

## About

The classic pairs memory game played with a deck of cards. Solo modes on a 4x4 or 5x6 layout reward finishing in the fewest turns. In Duel mode you and the CPU alternate, a match earns another go, and the CPU remembers only some of the cards it has seen - so it is beatable, but it will pounce on any pair it knows.

**Objective:** Find every matching pair (solo: in the fewest turns; duel: more pairs than the CPU).

**Modes:** Solo 4x4, Solo 5x6, Duel vs CPU

## Controls

| Key | Action |
|---|---|
| 2/4/6/8 | Move cursor |
| 5 | Turn over card |
| * | Pause menu |

Every game also supports: `5` to select in menus, `*` to pause, `0` on the title screen to toggle sound, and `#` on the title screen for help. Joystick/D-pad and the 2/4/6/8 keys are interchangeable.

## Download

| File | Link |
|---|---|
| JAR (install this) | [MemoryCards.jar](https://github.com/agneay/j2me-100-games/releases/download/game-077-v1.0.0/MemoryCards.jar) |
| JAD (descriptor for OTA install) | [MemoryCards.jad](https://github.com/agneay/j2me-100-games/releases/download/game-077-v1.0.0/MemoryCards.jad) |
| Release page | [game-077-v1.0.0](https://github.com/agneay/j2me-100-games/releases/tag/game-077-v1.0.0) |
| All 100 games | [j2me-100-games-v1.0.0.zip](https://github.com/agneay/j2me-100-games/releases/download/v1.0.0/j2me-100-games-v1.0.0.zip) |

**Browser Demo:** [play in your browser](https://agneay.github.io/j2me-100-games/games/memory-cards/). The browser demo is the same Java source compiled to JavaScript with TeaVM and run on the project's MIDP reference runtime; it is a convenience preview, not the original Java ME version. For the authentic experience install the JAR on a phone or in an emulator.

Installation help: [docs/installing.md](../../docs/installing.md) and [docs/emulator.md](../../docs/emulator.md).

## Technical details

| | |
|---|---|
| MIDlet class | `memorycards.MemoryCardsMIDlet` |
| Platform | Java ME, CLDC 1.0, MIDP 2.0 |
| JAR size | 19.1 KB |
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
tools/build-game.sh 077
```

Output: `games/077-memory-cards/dist/MemoryCards.jar` and `.jad`. See [docs/building.md](../../docs/building.md).

---

Part of [J2ME 100 Games](../../README.md). Original game, released under the [MIT License](../../LICENSE).
