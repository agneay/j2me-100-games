# Gem Swap

<img src="media/icon.png" width="24" height="24" alt=""> **Puzzle** &middot; Game 041 of 100 &middot; v1.0.0

> Swap neighbouring gems to line up three or more, setting off cascading chain reactions.

<p><img src="media/title.png" alt="Gem Swap title screen" width="176"> <img src="media/play.png" alt="Gem Swap gameplay" width="176"> <img src="media/demo.gif" alt="Gem Swap gameplay animation" width="176"></p>

<sub>Screenshots at 176x208, captured from the real JAR on the project's reference runtime.</sub>

## About

A match-three puzzle with six differently shaped gems, so colours stay readable even on washed-out screens. Gems animate as they swap and fall, cascades multiply your score, invalid swaps bounce back, a hint glows if you hesitate and the board reshuffles itself when no moves remain. Play 30 Moves for strategy or 90 Seconds for speed.

**Objective:** Score as many points as possible within 30 moves or 90 seconds.

**Modes:** 30 Moves, 90 Seconds

## Controls

| Key | Action |
|---|---|
| 2/4/6/8 | Move cursor |
| 5 | Pick a gem |
| 2/4/6/8 (after 5) | Swap in that direction |
| 0 | Cancel |
| * | Pause menu |

Every game also supports: `5` to select in menus, `*` to pause, `0` on the title screen to toggle sound, and `#` on the title screen for help. Joystick/D-pad and the 2/4/6/8 keys are interchangeable.

## Download

| File | Link |
|---|---|
| JAR (install this) | [GemSwap.jar](https://github.com/agneay/j2me-100-games/releases/download/game-041-v1.0.0/GemSwap.jar) |
| JAD (descriptor for OTA install) | [GemSwap.jad](https://github.com/agneay/j2me-100-games/releases/download/game-041-v1.0.0/GemSwap.jad) |
| Release page | [game-041-v1.0.0](https://github.com/agneay/j2me-100-games/releases/tag/game-041-v1.0.0) |
| All 100 games | [j2me-100-games-v1.0.0.zip](https://github.com/agneay/j2me-100-games/releases/download/v1.0.0/j2me-100-games-v1.0.0.zip) |

**Browser Demo:** [play in your browser](https://agneay.github.io/j2me-100-games/games/gem-swap/). The browser demo is the same Java source compiled to JavaScript with TeaVM and run on the project's MIDP reference runtime; it is a convenience preview, not the original Java ME version. For the authentic experience install the JAR on a phone or in an emulator.

Installation help: [docs/installing.md](../../docs/installing.md) and [docs/emulator.md](../../docs/emulator.md).

## Technical details

| | |
|---|---|
| MIDlet class | `gemswap.GemSwapMIDlet` |
| Platform | Java ME, CLDC 1.0, MIDP 2.0 |
| JAR size | 18.0 KB |
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
tools/build-game.sh 041
```

Output: `games/041-gem-swap/dist/GemSwap.jar` and `.jad`. See [docs/building.md](../../docs/building.md).

---

Part of [J2ME 100 Games](../../README.md). Original game, released under the [MIT License](../../LICENSE).
