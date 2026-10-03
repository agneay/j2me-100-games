# Arena Champion

<img src="media/icon.png" width="24" height="24" alt=""> **RPG** &middot; Game 068 of 100 &middot; v1.0.0

> Gladiator duels: read high and low wind-ups, block to stun, punish the opening, and upgrade between six bouts.

<p><img src="media/title.png" alt="Arena Champion title screen" width="176"> <img src="media/play.png" alt="Arena Champion gameplay" width="176"> <img src="media/demo.gif" alt="Arena Champion gameplay animation" width="176"></p>

<sub>Screenshots at 176x208, captured from the real JAR on the project's reference runtime.</sub>

## About

Timing-based arena combat. Each opponent telegraphs attacks high or low; block to match and they are stunned for a double-damage counter. Quick strikes are safe, heavy strikes hit three times harder and interrupt wind-ups but drain stamina. Later gladiators wind up faster and feint. After every win pick an upgrade: strength, armour, stamina or vitality.

**Objective:** Defeat all six gladiators to become Arena Champion.

## Controls

| Key | Action |
|---|---|
| 2 | Block high |
| 8 | Block low |
| 5 | Quick strike |
| 6 | Heavy strike |
| * | Pause menu |

Every game also supports: `5` to select in menus, `*` to pause, `0` on the title screen to toggle sound, and `#` on the title screen for help. Joystick/D-pad and the 2/4/6/8 keys are interchangeable.

## Download

| File | Link |
|---|---|
| JAR (install this) | [ArenaChampion.jar](https://github.com/agneay/j2me-100-games/releases/download/game-068-v1.0.0/ArenaChampion.jar) |
| JAD (descriptor for OTA install) | [ArenaChampion.jad](https://github.com/agneay/j2me-100-games/releases/download/game-068-v1.0.0/ArenaChampion.jad) |
| Release page | [game-068-v1.0.0](https://github.com/agneay/j2me-100-games/releases/tag/game-068-v1.0.0) |
| All 100 games | [j2me-100-games-v1.0.0.zip](https://github.com/agneay/j2me-100-games/releases/download/v1.0.0/j2me-100-games-v1.0.0.zip) |

**Browser Demo:** [play in your browser](https://agneay.github.io/j2me-100-games/games/arena-champion/). The browser demo is the same Java source compiled to JavaScript with TeaVM and run on the project's MIDP reference runtime; it is a convenience preview, not the original Java ME version. For the authentic experience install the JAR on a phone or in an emulator.

Installation help: [docs/installing.md](../../docs/installing.md) and [docs/emulator.md](../../docs/emulator.md).

## Technical details

| | |
|---|---|
| MIDlet class | `arenachampion.ArenaChampionMIDlet` |
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
tools/build-game.sh 068
```

Output: `games/068-arena-champion/dist/ArenaChampion.jar` and `.jad`. See [docs/building.md](../../docs/building.md).

---

Part of [J2ME 100 Games](../../README.md). Original game, released under the [MIT License](../../LICENSE).
