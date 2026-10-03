# Traffic Dash

<img src="media/icon.png" width="24" height="24" alt=""> **Racing** &middot; Game 009 of 100 &middot; v1.0.0

> Weave through highway traffic at speed, grab fuel cans and chain near-misses for a bigger score.

<p><img src="media/title.png" alt="Traffic Dash title screen" width="176"> <img src="media/play.png" alt="Traffic Dash gameplay" width="176"> <img src="media/demo.gif" alt="Traffic Dash gameplay animation" width="176"></p>

<sub>Screenshots at 176x208, captured from the real JAR on the project's reference runtime.</sub>

## About

A lane-switching highway racer. Traffic drives slower than you, some cars signal and change lanes, fuel drains constantly and is topped up by green cans, and brushing past cars builds a near-miss combo. The spawner always leaves at least one lane open, so every crash is avoidable. Rush Hour mode doubles the traffic.

**Objective:** Drive as far as you can without running out of fuel or crashing three times.

**Modes:** Normal, Rush hour

## Controls

| Key | Action |
|---|---|
| 4/6 | Change lane |
| 2 | Accelerate |
| 8 | Brake |
| * | Pause menu |

Every game also supports: `5` to select in menus, `*` to pause, `0` on the title screen to toggle sound, and `#` on the title screen for help. Joystick/D-pad and the 2/4/6/8 keys are interchangeable.

## Download

| File | Link |
|---|---|
| JAR (install this) | [TrafficDash.jar](https://github.com/agneay/j2me-100-games/releases/download/game-009-v1.0.0/TrafficDash.jar) |
| JAD (descriptor for OTA install) | [TrafficDash.jad](https://github.com/agneay/j2me-100-games/releases/download/game-009-v1.0.0/TrafficDash.jad) |
| Release page | [game-009-v1.0.0](https://github.com/agneay/j2me-100-games/releases/tag/game-009-v1.0.0) |
| All 100 games | [j2me-100-games-v1.0.0.zip](https://github.com/agneay/j2me-100-games/releases/download/v1.0.0/j2me-100-games-v1.0.0.zip) |

**Browser Demo:** [play in your browser](https://agneay.github.io/j2me-100-games/games/traffic-dash/). The browser demo is the same Java source compiled to JavaScript with TeaVM and run on the project's MIDP reference runtime; it is a convenience preview, not the original Java ME version. For the authentic experience install the JAR on a phone or in an emulator.

Installation help: [docs/installing.md](../../docs/installing.md) and [docs/emulator.md](../../docs/emulator.md).

## Technical details

| | |
|---|---|
| MIDlet class | `trafficdash.TrafficDashMIDlet` |
| Platform | Java ME, CLDC 1.0, MIDP 2.0 |
| JAR size | 18.3 KB |
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
tools/build-game.sh 009
```

Output: `games/009-traffic-dash/dist/TrafficDash.jar` and `.jad`. See [docs/building.md](../../docs/building.md).

---

Part of [J2ME 100 Games](../../README.md). Original game, released under the [MIT License](../../LICENSE).
