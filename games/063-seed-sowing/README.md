# Seed Sowing

<img src="media/icon.png" width="24" height="24" alt=""> **Board** &middot; Game 063 of 100 &middot; v1.0.0

> A count-and-capture mancala-family game: sow seeds round the board, earn free turns and capture across.

<p><img src="media/title.png" alt="Seed Sowing title screen" width="176"> <img src="media/play.png" alt="Seed Sowing gameplay" width="176"> <img src="media/demo.gif" alt="Seed Sowing gameplay animation" width="176"></p>

<sub>Screenshots at 176x208, captured from the real JAR on the project's reference runtime.</sub>

## About

One of the world's oldest kinds of board game, played with the classic store-and-capture rules. Press 1-6 to sow one of your pits and watch the seeds drop one at a time; landing in your store earns another turn and landing in an empty pit of your own captures the pit opposite. The CPU plans ahead with an alpha-beta search that understands free turns, or play a friend on one phone.

**Objective:** Finish with more seeds in your store than your opponent.

**Modes:** 4 seeds vs CPU, 6 seeds vs CPU, 4 seeds 2 players

## Controls

| Key | Action |
|---|---|
| 1-6 | Sow that pit |
| 4/6 | Choose pit |
| 5 | Sow chosen pit |
| * | Pause menu |

Every game also supports: `5` to select in menus, `*` to pause, `0` on the title screen to toggle sound, and `#` on the title screen for help. Joystick/D-pad and the 2/4/6/8 keys are interchangeable.

## Download

| File | Link |
|---|---|
| JAR (install this) | [SeedSowing.jar](https://github.com/agneay/j2me-100-games/releases/download/game-063-v1.0.0/SeedSowing.jar) |
| JAD (descriptor for OTA install) | [SeedSowing.jad](https://github.com/agneay/j2me-100-games/releases/download/game-063-v1.0.0/SeedSowing.jad) |
| Release page | [game-063-v1.0.0](https://github.com/agneay/j2me-100-games/releases/tag/game-063-v1.0.0) |
| All 100 games | [j2me-100-games-v1.0.0.zip](https://github.com/agneay/j2me-100-games/releases/download/v1.0.0/j2me-100-games-v1.0.0.zip) |

**Browser Demo:** [play in your browser](https://agneay.github.io/j2me-100-games/games/seed-sowing/). The browser demo is the same Java source compiled to JavaScript with TeaVM and run on the project's MIDP reference runtime; it is a convenience preview, not the original Java ME version. For the authentic experience install the JAR on a phone or in an emulator.

Installation help: [docs/installing.md](../../docs/installing.md) and [docs/emulator.md](../../docs/emulator.md).

## Technical details

| | |
|---|---|
| MIDlet class | `seedsowing.SeedSowingMIDlet` |
| Platform | Java ME, CLDC 1.0, MIDP 2.0 |
| JAR size | 18.5 KB |
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
tools/build-game.sh 063
```

Output: `games/063-seed-sowing/dist/SeedSowing.jar` and `.jad`. See [docs/building.md](../../docs/building.md).

---

Part of [J2ME 100 Games](../../README.md). Original game, released under the [MIT License](../../LICENSE).
