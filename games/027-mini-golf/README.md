# Mini Golf

<img src="media/icon.png" width="24" height="24" alt=""> **Sports** &middot; Game 027 of 100 &middot; v1.0.0

> Nine top-down crazy-golf holes with bank shots, bumpers, sand traps and water hazards.

<p><img src="media/title.png" alt="Mini Golf title screen" width="176"> <img src="media/play.png" alt="Mini Golf gameplay" width="176"> <img src="media/demo.gif" alt="Mini Golf gameplay animation" width="176"></p>

<sub>Screenshots at 176x208, captured from the real JAR on the project's reference runtime.</sub>

## About

Aim with 4/6 (fine-tune with 1/3), stop the swinging power meter with 5 and watch the ball carom around the course. Walls bounce, bumpers kick the ball back harder, sand drags it to a crawl and water costs a penalty stroke. Hit the cup too hard and the ball skips straight over it. Par 29 for the nine holes; your best round is saved.

**Objective:** Complete all nine holes in as few strokes as possible.

## Controls

| Key | Action |
|---|---|
| 4/6 | Aim (hold) |
| 1/3 | Fine aim |
| 5 | Start / stop power meter |
| * | Pause menu |

Every game also supports: `5` to select in menus, `*` to pause, `0` on the title screen to toggle sound, and `#` on the title screen for help. Joystick/D-pad and the 2/4/6/8 keys are interchangeable.

## Download

| File | Link |
|---|---|
| JAR (install this) | [MiniGolf.jar](https://github.com/agneay/j2me-100-games/releases/download/game-027-v1.0.0/MiniGolf.jar) |
| JAD (descriptor for OTA install) | [MiniGolf.jad](https://github.com/agneay/j2me-100-games/releases/download/game-027-v1.0.0/MiniGolf.jad) |
| Release page | [game-027-v1.0.0](https://github.com/agneay/j2me-100-games/releases/tag/game-027-v1.0.0) |
| All 100 games | [j2me-100-games-v1.0.0.zip](https://github.com/agneay/j2me-100-games/releases/download/v1.0.0/j2me-100-games-v1.0.0.zip) |

**Browser Demo:** [play in your browser](https://agneay.github.io/j2me-100-games/games/mini-golf/). The browser demo is the same Java source compiled to JavaScript with TeaVM and run on the project's MIDP reference runtime; it is a convenience preview, not the original Java ME version. For the authentic experience install the JAR on a phone or in an emulator.

Installation help: [docs/installing.md](../../docs/installing.md) and [docs/emulator.md](../../docs/emulator.md).

## Technical details

| | |
|---|---|
| MIDlet class | `minigolf.MiniGolfMIDlet` |
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
tools/build-game.sh 027
```

Output: `games/027-mini-golf/dist/MiniGolf.jar` and `.jad`. See [docs/building.md](../../docs/building.md).

---

Part of [J2ME 100 Games](../../README.md). Original game, released under the [MIT License](../../LICENSE).
