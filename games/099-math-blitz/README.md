# Math Blitz

<img src="media/icon.png" width="24" height="24" alt=""> **Experimental** &middot; Game 099 of 100 &middot; v1.0.0

> Rapid-fire mental arithmetic typed straight on the number pad, checked the instant you enter the last digit.

<p><img src="media/title.png" alt="Math Blitz title screen" width="176"> <img src="media/play.png" alt="Math Blitz gameplay" width="176"> <img src="media/demo.gif" alt="Math Blitz gameplay animation" width="176"></p>

<sub>Screenshots at 176x208, captured from the real JAR on the project's reference runtime.</sub>

## About

The number pad was made for this. Sums, differences, products and divisions appear one after another and you type the answer directly; there is no confirm key, because the answer is checked the moment you have typed enough digits. Sprint mode gives you 60 seconds, with every correct answer adding a second. Survival puts a shrinking timer on each question and gives you three lives. Streaks raise the difficulty and mistakes ease it back down.

**Objective:** Solve as many problems as possible before time or lives run out.

**Modes:** Sprint, Survival

## Controls

| Key | Action |
|---|---|
| 0-9 | Type answer |
| # | Clear typed digits |
| * | Pause menu |

Every game also supports: `5` to select in menus, `*` to pause, `0` on the title screen to toggle sound, and `#` on the title screen for help. Joystick/D-pad and the 2/4/6/8 keys are interchangeable.

## Download

| File | Link |
|---|---|
| JAR (install this) | [MathBlitz.jar](https://github.com/agneay/j2me-100-games/releases/download/game-099-v1.0.0/MathBlitz.jar) |
| JAD (descriptor for OTA install) | [MathBlitz.jad](https://github.com/agneay/j2me-100-games/releases/download/game-099-v1.0.0/MathBlitz.jad) |
| Release page | [game-099-v1.0.0](https://github.com/agneay/j2me-100-games/releases/tag/game-099-v1.0.0) |
| All 100 games | [j2me-100-games-v1.0.0.zip](https://github.com/agneay/j2me-100-games/releases/download/v1.0.0/j2me-100-games-v1.0.0.zip) |

**Browser Demo:** [play in your browser](https://agneay.github.io/j2me-100-games/games/math-blitz/). The browser demo is the same Java source compiled to JavaScript with TeaVM and run on the project's MIDP reference runtime; it is a convenience preview, not the original Java ME version. For the authentic experience install the JAR on a phone or in an emulator.

Installation help: [docs/installing.md](../../docs/installing.md) and [docs/emulator.md](../../docs/emulator.md).

## Technical details

| | |
|---|---|
| MIDlet class | `mathblitz.MathBlitzMIDlet` |
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
tools/build-game.sh 099
```

Output: `games/099-math-blitz/dist/MathBlitz.jar` and `.jad`. See [docs/building.md](../../docs/building.md).

---

Part of [J2ME 100 Games](../../README.md). Original game, released under the [MIT License](../../LICENSE).
