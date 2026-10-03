# Lake Fishing

<img src="media/icon.png" width="24" height="24" alt=""> **Simulation** &middot; Game 028 of 100 &middot; v1.0.0

> Cast from the dock, strike at the bite and play the fish on a tension meter. Six species, one golden rarity.

<p><img src="media/title.png" alt="Lake Fishing title screen" width="176"> <img src="media/play.png" alt="Lake Fishing gameplay" width="176"> <img src="media/demo.gif" alt="Lake Fishing gameplay animation" width="176"></p>

<sub>Screenshots at 176x208, captured from the real JAR on the project's reference runtime.</sub>

## About

A relaxed one-button fishing sim. Cast distance decides depth, and depth decides what bites: minnows in the shallows, bass and pike further out, catfish in the deep and a very rare golden carp anywhere. Strike too early and you spook the fish; reel too hard and the line snaps; let it go slack and the fish throws the hook. The sky turns to sunset over a four-minute day, and your record catch is saved.

**Objective:** Score as many points as possible in a four-minute day (or ten casts).

**Modes:** Day trip (4 min), Ten casts

## Controls

| Key | Action |
|---|---|
| 5 | Swing / cast / strike |
| 5 (hold) | Reel in |
| * | Pause menu |

Every game also supports: `5` to select in menus, `*` to pause, `0` on the title screen to toggle sound, and `#` on the title screen for help. Joystick/D-pad and the 2/4/6/8 keys are interchangeable.

## Download

| File | Link |
|---|---|
| JAR (install this) | [LakeFishing.jar](https://github.com/agneay/j2me-100-games/releases/download/game-028-v1.0.0/LakeFishing.jar) |
| JAD (descriptor for OTA install) | [LakeFishing.jad](https://github.com/agneay/j2me-100-games/releases/download/game-028-v1.0.0/LakeFishing.jad) |
| Release page | [game-028-v1.0.0](https://github.com/agneay/j2me-100-games/releases/tag/game-028-v1.0.0) |
| All 100 games | [j2me-100-games-v1.0.0.zip](https://github.com/agneay/j2me-100-games/releases/download/v1.0.0/j2me-100-games-v1.0.0.zip) |

**Browser Demo:** [play in your browser](https://agneay.github.io/j2me-100-games/games/lake-fishing/). The browser demo is the same Java source compiled to JavaScript with TeaVM and run on the project's MIDP reference runtime; it is a convenience preview, not the original Java ME version. For the authentic experience install the JAR on a phone or in an emulator.

Installation help: [docs/installing.md](../../docs/installing.md) and [docs/emulator.md](../../docs/emulator.md).

## Technical details

| | |
|---|---|
| MIDlet class | `lakefishing.LakeFishingMIDlet` |
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
tools/build-game.sh 028
```

Output: `games/028-lake-fishing/dist/LakeFishing.jar` and `.jad`. See [docs/building.md](../../docs/building.md).

---

Part of [J2ME 100 Games](../../README.md). Original game, released under the [MIT License](../../LICENSE).
