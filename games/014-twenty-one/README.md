# Twenty-One

<img src="media/icon.png" width="24" height="24" alt=""> **Card** &middot; Game 014 of 100 &middot; v1.0.0

> Play the dealer at the classic 21 card game: hit, stand or double, and grow 100 chips into 1000.

<p><img src="media/title.png" alt="Twenty-One title screen" width="176"> <img src="media/play.png" alt="Twenty-One gameplay" width="176"> <img src="media/demo.gif" alt="Twenty-One gameplay animation" width="176"></p>

<sub>Screenshots at 176x208, captured from the real JAR on the project's reference runtime.</sub>

## About

A clean table game of twenty-one dealt from a four-deck shoe. Hit, stand or double down with the keypad, the dealer draws to 17, and a natural 21 pays 3 to 2. Start with 100 chips; reach 1000 to beat the house, or go broke trying. Your best peak bankroll is saved. Splitting and insurance are not included to keep the game simple on small screens.

**Objective:** Grow your bankroll from 100 to 1000 chips.

## Controls

| Key | Action |
|---|---|
| 4/6 | Change bet / choose action |
| 2/8 | Double / halve bet |
| 5 | Deal / confirm |
| 1 | Hit |
| 3 | Stand |
| 9 | Double down |
| * | Pause menu |

Every game also supports: `5` to select in menus, `*` to pause, `0` on the title screen to toggle sound, and `#` on the title screen for help. Joystick/D-pad and the 2/4/6/8 keys are interchangeable.

## Download

| File | Link |
|---|---|
| JAR (install this) | [TwentyOne.jar](https://github.com/agneay/j2me-100-games/releases/download/game-014-v1.0.0/TwentyOne.jar) |
| JAD (descriptor for OTA install) | [TwentyOne.jad](https://github.com/agneay/j2me-100-games/releases/download/game-014-v1.0.0/TwentyOne.jad) |
| Release page | [game-014-v1.0.0](https://github.com/agneay/j2me-100-games/releases/tag/game-014-v1.0.0) |
| All 100 games | [j2me-100-games-v1.0.0.zip](https://github.com/agneay/j2me-100-games/releases/download/v1.0.0/j2me-100-games-v1.0.0.zip) |

**Browser Demo:** [play in your browser](https://agneay.github.io/j2me-100-games/games/twenty-one/). The browser demo is the same Java source compiled to JavaScript with TeaVM and run on the project's MIDP reference runtime; it is a convenience preview, not the original Java ME version. For the authentic experience install the JAR on a phone or in an emulator.

Installation help: [docs/installing.md](../../docs/installing.md) and [docs/emulator.md](../../docs/emulator.md).

## Technical details

| | |
|---|---|
| MIDlet class | `twentyone.TwentyOneMIDlet` |
| Platform | Java ME, CLDC 1.0, MIDP 2.0 |
| JAR size | 20.0 KB |
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
tools/build-game.sh 014
```

Output: `games/014-twenty-one/dist/TwentyOne.jar` and `.jad`. See [docs/building.md](../../docs/building.md).

---

Part of [J2ME 100 Games](../../README.md). Original game, released under the [MIT License](../../LICENSE).
