# Race Home

<img src="media/icon.png" width="24" height="24" alt=""> **Board** &middot; Game 083 of 100 &middot; v1.0.0

> A cross-and-circle dice race: roll a six to leave base, race round the board and knock rivals back home.

<p><img src="media/title.png" alt="Race Home title screen" width="176"> <img src="media/play.png" alt="Race Home gameplay" width="176"> <img src="media/demo.gif" alt="Race Home gameplay animation" width="176"></p>

<sub>Screenshots at 176x208, captured from the real JAR on the project's reference runtime.</sub>

## About

The family dice race in pocket form. Each player has three tokens; a six brings one out of base and earns another roll, tokens race once round the 28-square track and then up their own home lane, and landing exactly on a rival sends it back to base - except on the starred safe squares. Play one or three CPU opponents who capture greedily, play safe when they can and always bring new tokens out on a six.

**Objective:** Be first to get all three of your tokens home.

**Modes:** vs 1 CPU, vs 3 CPUs

## Controls

| Key | Action |
|---|---|
| 5 | Roll / move the selected token |
| 4/6 | Choose token |
| * | Pause menu |

Every game also supports: `5` to select in menus, `*` to pause, `0` on the title screen to toggle sound, and `#` on the title screen for help. Joystick/D-pad and the 2/4/6/8 keys are interchangeable.

## Download

| File | Link |
|---|---|
| JAR (install this) | [RaceHome.jar](https://github.com/agneay/j2me-100-games/releases/download/game-083-v1.0.0/RaceHome.jar) |
| JAD (descriptor for OTA install) | [RaceHome.jad](https://github.com/agneay/j2me-100-games/releases/download/game-083-v1.0.0/RaceHome.jad) |
| Release page | [game-083-v1.0.0](https://github.com/agneay/j2me-100-games/releases/tag/game-083-v1.0.0) |
| All 100 games | [j2me-100-games-v1.0.0.zip](https://github.com/agneay/j2me-100-games/releases/download/v1.0.0/j2me-100-games-v1.0.0.zip) |

**Browser Demo:** [play in your browser](https://agneay.github.io/j2me-100-games/games/race-home/). The browser demo is the same Java source compiled to JavaScript with TeaVM and run on the project's MIDP reference runtime; it is a convenience preview, not the original Java ME version. For the authentic experience install the JAR on a phone or in an emulator.

Installation help: [docs/installing.md](../../docs/installing.md) and [docs/emulator.md](../../docs/emulator.md).

## Technical details

| | |
|---|---|
| MIDlet class | `racehome.RaceHomeMIDlet` |
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
tools/build-game.sh 083
```

Output: `games/083-race-home/dist/RaceHome.jar` and `.jad`. See [docs/building.md](../../docs/building.md).

---

Part of [J2ME 100 Games](../../README.md). Original game, released under the [MIT License](../../LICENSE).
