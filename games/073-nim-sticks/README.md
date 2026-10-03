# Nim Sticks

<img src="media/icon.png" width="24" height="24" alt=""> **Board** &middot; Game 073 of 100 &middot; v1.0.0

> The ancient take-away game: remove sticks from one row per turn - take the last (or avoid it in misere).

<p><img src="media/title.png" alt="Nim Sticks title screen" width="176"> <img src="media/play.png" alt="Nim Sticks gameplay" width="176"> <img src="media/demo.gif" alt="Nim Sticks gameplay animation" width="176"></p>

<sub>Screenshots at 176x208, captured from the real JAR on the project's reference runtime.</sub>

## About

Rows of matchsticks, two players, one rule: take any number of sticks from a single row. Normal play rewards taking the final stick; misere play punishes it. The standard CPU blunders now and then, while the Expert CPU plays the mathematically perfect nim-sum strategy - you can still win, but only from a winning position and without a single slip. Boards are the classic 1-3-5-7 or randomly dealt.

**Objective:** Normal: take the last stick. Misere: force the CPU to take it.

**Modes:** Normal vs CPU, Misere vs CPU, Normal vs Expert, Misere vs Expert

## Controls

| Key | Action |
|---|---|
| 2/8 | Choose row |
| 4/6 | Choose how many |
| 5 | Take sticks |
| * | Pause menu |

Every game also supports: `5` to select in menus, `*` to pause, `0` on the title screen to toggle sound, and `#` on the title screen for help. Joystick/D-pad and the 2/4/6/8 keys are interchangeable.

## Download

| File | Link |
|---|---|
| JAR (install this) | [NimSticks.jar](https://github.com/agneay/j2me-100-games/releases/download/game-073-v1.0.0/NimSticks.jar) |
| JAD (descriptor for OTA install) | [NimSticks.jad](https://github.com/agneay/j2me-100-games/releases/download/game-073-v1.0.0/NimSticks.jad) |
| Release page | [game-073-v1.0.0](https://github.com/agneay/j2me-100-games/releases/tag/game-073-v1.0.0) |
| All 100 games | [j2me-100-games-v1.0.0.zip](https://github.com/agneay/j2me-100-games/releases/download/v1.0.0/j2me-100-games-v1.0.0.zip) |

**Browser Demo:** [play in your browser](https://agneay.github.io/j2me-100-games/games/nim-sticks/). The browser demo is the same Java source compiled to JavaScript with TeaVM and run on the project's MIDP reference runtime; it is a convenience preview, not the original Java ME version. For the authentic experience install the JAR on a phone or in an emulator.

Installation help: [docs/installing.md](../../docs/installing.md) and [docs/emulator.md](../../docs/emulator.md).

## Technical details

| | |
|---|---|
| MIDlet class | `nimsticks.NimSticksMIDlet` |
| Platform | Java ME, CLDC 1.0, MIDP 2.0 |
| JAR size | 17.2 KB |
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
tools/build-game.sh 073
```

Output: `games/073-nim-sticks/dist/NimSticks.jar` and `.jad`. See [docs/building.md](../../docs/building.md).

---

Part of [J2ME 100 Games](../../README.md). Original game, released under the [MIT License](../../LICENSE).
