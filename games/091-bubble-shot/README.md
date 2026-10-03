# Bubble Shot

<img src="media/icon.png" width="24" height="24" alt=""> **Arcade** &middot; Game 091 of 100 &middot; v1.0.0

> Aim, bank off the walls and pop groups of three matching bubbles before the ceiling pushes down.

<p><img src="media/title.png" alt="Bubble Shot title screen" width="176"> <img src="media/play.png" alt="Bubble Shot gameplay" width="176"> <img src="media/demo.gif" alt="Bubble Shot gameplay animation" width="176"></p>

<sub>Screenshots at 176x208, captured from the real JAR on the project's reference runtime.</sub>

## About

A classic aim-and-fire bubble popper built for the keypad. Sweep the launcher with 4 and 6, fire with 5 and swap with the next bubble using 0. Match three or more of a colour to pop them, and cut loose any bubbles hanging below for a bigger bonus. Every few missed shots the ceiling pushes down a new row; let the cluster reach the dashed line and it is over. Relaxed mode uses four colours and a slower ceiling.

**Objective:** Clear every bubble from the board.

**Modes:** Relaxed, Classic

## Controls

| Key | Action |
|---|---|
| 4/6 | Aim launcher |
| 5 | Fire |
| 0 | Swap with next bubble |
| * | Pause menu |

Every game also supports: `5` to select in menus, `*` to pause, `0` on the title screen to toggle sound, and `#` on the title screen for help. Joystick/D-pad and the 2/4/6/8 keys are interchangeable.

## Download

| File | Link |
|---|---|
| JAR (install this) | [BubbleShot.jar](https://github.com/agneay/j2me-100-games/releases/download/game-091-v1.0.0/BubbleShot.jar) |
| JAD (descriptor for OTA install) | [BubbleShot.jad](https://github.com/agneay/j2me-100-games/releases/download/game-091-v1.0.0/BubbleShot.jad) |
| Release page | [game-091-v1.0.0](https://github.com/agneay/j2me-100-games/releases/tag/game-091-v1.0.0) |
| All 100 games | [j2me-100-games-v1.0.0.zip](https://github.com/agneay/j2me-100-games/releases/download/v1.0.0/j2me-100-games-v1.0.0.zip) |

**Browser Demo:** [play in your browser](https://agneay.github.io/j2me-100-games/games/bubble-shot/). The browser demo is the same Java source compiled to JavaScript with TeaVM and run on the project's MIDP reference runtime; it is a convenience preview, not the original Java ME version. For the authentic experience install the JAR on a phone or in an emulator.

Installation help: [docs/installing.md](../../docs/installing.md) and [docs/emulator.md](../../docs/emulator.md).

## Technical details

| | |
|---|---|
| MIDlet class | `bubbleshot.BubbleShotMIDlet` |
| Platform | Java ME, CLDC 1.0, MIDP 2.0 |
| JAR size | 18.6 KB |
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
tools/build-game.sh 091
```

Output: `games/091-bubble-shot/dist/BubbleShot.jar` and `.jad`. See [docs/building.md](../../docs/building.md).

---

Part of [J2ME 100 Games](../../README.md). Original game, released under the [MIT License](../../LICENSE).
