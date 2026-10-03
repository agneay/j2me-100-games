# Star Defender

<img src="media/icon.png" width="24" height="24" alt=""> **Arcade** &middot; Game 011 of 100 &middot; v1.0.0

> Defend the planet from a marching alien formation, hiding behind shields that crumble under fire.

<p><img src="media/title.png" alt="Star Defender title screen" width="176"> <img src="media/play.png" alt="Star Defender gameplay" width="176"> <img src="media/demo.gif" alt="Star Defender gameplay animation" width="176"></p>

<sub>Screenshots at 176x208, captured from the real JAR on the project's reference runtime.</sub>

## About

A fixed-shooter with an original cast of pixel aliens. The formation marches faster as it shrinks and drops lower each time it hits a wall, shields erode block by block from both sides, a mystery ship drifts across the top now and then, and every new wave starts a little closer to the ground.

**Objective:** Destroy every alien in each wave before the formation reaches the ground. Three lives.

## Controls

| Key | Action |
|---|---|
| 4/6 | Move ship |
| 5/2 | Fire |
| * | Pause menu |

Every game also supports: `5` to select in menus, `*` to pause, `0` on the title screen to toggle sound, and `#` on the title screen for help. Joystick/D-pad and the 2/4/6/8 keys are interchangeable.

## Download

| File | Link |
|---|---|
| JAR (install this) | [StarDefender.jar](https://github.com/agneay/j2me-100-games/releases/download/game-011-v1.0.0/StarDefender.jar) |
| JAD (descriptor for OTA install) | [StarDefender.jad](https://github.com/agneay/j2me-100-games/releases/download/game-011-v1.0.0/StarDefender.jad) |
| Release page | [game-011-v1.0.0](https://github.com/agneay/j2me-100-games/releases/tag/game-011-v1.0.0) |
| All 100 games | [j2me-100-games-v1.0.0.zip](https://github.com/agneay/j2me-100-games/releases/download/v1.0.0/j2me-100-games-v1.0.0.zip) |

**Browser Demo:** [play in your browser](https://agneay.github.io/j2me-100-games/games/star-defender/). The browser demo is the same Java source compiled to JavaScript with TeaVM and run on the project's MIDP reference runtime; it is a convenience preview, not the original Java ME version. For the authentic experience install the JAR on a phone or in an emulator.

Installation help: [docs/installing.md](../../docs/installing.md) and [docs/emulator.md](../../docs/emulator.md).

## Technical details

| | |
|---|---|
| MIDlet class | `stardefender.StarDefenderMIDlet` |
| Platform | Java ME, CLDC 1.0, MIDP 2.0 |
| JAR size | 18.7 KB |
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
tools/build-game.sh 011
```

Output: `games/011-star-defender/dist/StarDefender.jar` and `.jad`. See [docs/building.md](../../docs/building.md).

---

Part of [J2ME 100 Games](../../README.md). Original game, released under the [MIT License](../../LICENSE).
