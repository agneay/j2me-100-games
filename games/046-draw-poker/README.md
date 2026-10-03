# Draw Poker

<img src="media/icon.png" width="24" height="24" alt=""> **Card** &middot; Game 046 of 100 &middot; v1.0.0

> Jacks-or-better video poker: hold cards with keys 1-5, draw once, and get paid by the table.

<p><img src="media/title.png" alt="Draw Poker title screen" width="176"> <img src="media/play.png" alt="Draw Poker gameplay" width="176"> <img src="media/demo.gif" alt="Draw Poker gameplay animation" width="176"></p>

<sub>Screenshots at 176x208, captured from the real JAR on the project's reference runtime.</sub>

## About

Classic five-card draw video poker. Bet one to five credits, hold the cards you want with the number keys 1-5 (one key per card, left to right), and draw replacements once. A full pay table is always on screen and lights up the winning hand, from jacks or better up to a 4000-credit royal flush at maximum bet. Grow 100 credits into 1000 to break the bank.

**Objective:** Grow your 100 starting credits to 1000 without going broke.

## Controls

| Key | Action |
|---|---|
| 4/6 | Change bet |
| 5 | Deal |
| 1-5 | Hold / release that card |
| 0/# | Draw |
| * | Pause menu |

Every game also supports: `5` to select in menus, `*` to pause, `0` on the title screen to toggle sound, and `#` on the title screen for help. Joystick/D-pad and the 2/4/6/8 keys are interchangeable.

## Download

| File | Link |
|---|---|
| JAR (install this) | [DrawPoker.jar](https://github.com/agneay/j2me-100-games/releases/download/game-046-v1.0.0/DrawPoker.jar) |
| JAD (descriptor for OTA install) | [DrawPoker.jad](https://github.com/agneay/j2me-100-games/releases/download/game-046-v1.0.0/DrawPoker.jad) |
| Release page | [game-046-v1.0.0](https://github.com/agneay/j2me-100-games/releases/tag/game-046-v1.0.0) |
| All 100 games | [j2me-100-games-v1.0.0.zip](https://github.com/agneay/j2me-100-games/releases/download/v1.0.0/j2me-100-games-v1.0.0.zip) |

**Browser Demo:** [play in your browser](https://agneay.github.io/j2me-100-games/games/draw-poker/). The browser demo is the same Java source compiled to JavaScript with TeaVM and run on the project's MIDP reference runtime; it is a convenience preview, not the original Java ME version. For the authentic experience install the JAR on a phone or in an emulator.

Installation help: [docs/installing.md](../../docs/installing.md) and [docs/emulator.md](../../docs/emulator.md).

## Technical details

| | |
|---|---|
| MIDlet class | `drawpoker.DrawPokerMIDlet` |
| Platform | Java ME, CLDC 1.0, MIDP 2.0 |
| JAR size | 19.6 KB |
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
tools/build-game.sh 046
```

Output: `games/046-draw-poker/dist/DrawPoker.jar` and `.jad`. See [docs/building.md](../../docs/building.md).

---

Part of [J2ME 100 Games](../../README.md). Original game, released under the [MIT License](../../LICENSE).
