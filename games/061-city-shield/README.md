# City Shield

<img src="media/icon.png" width="24" height="24" alt=""> **Arcade** &middot; Game 061 of 100 &middot; v1.0.0

> Aim a crosshair and launch interceptors to protect six cities from falling, splitting warheads.

<p><img src="media/title.png" alt="City Shield title screen" width="176"> <img src="media/play.png" alt="City Shield gameplay" width="176"> <img src="media/demo.gif" alt="City Shield gameplay animation" width="176"></p>

<sub>Screenshots at 176x208, captured from the real JAR on the project's reference runtime.</sub>

## About

Missile defence for the keypad. Glide the crosshair with the direction keys (or diagonally with 1/3/7/9) and press 5: the nearest of three bases with ammo fires an interceptor that bursts exactly where you aimed. Blasts chain through anything that flies into them, later waves bring splitting warheads and faster descents, and every wave pays a bonus for surviving cities and unspent ammo.

**Objective:** Survive as many waves as possible without losing all six cities.

## Controls

| Key | Action |
|---|---|
| 2/4/6/8 | Move crosshair |
| 1/3/7/9 | Move diagonally |
| 5 | Fire interceptor |
| * | Pause menu |

Every game also supports: `5` to select in menus, `*` to pause, `0` on the title screen to toggle sound, and `#` on the title screen for help. Joystick/D-pad and the 2/4/6/8 keys are interchangeable.

## Download

| File | Link |
|---|---|
| JAR (install this) | [CityShield.jar](https://github.com/agneay/j2me-100-games/releases/download/game-061-v1.0.0/CityShield.jar) |
| JAD (descriptor for OTA install) | [CityShield.jad](https://github.com/agneay/j2me-100-games/releases/download/game-061-v1.0.0/CityShield.jad) |
| Release page | [game-061-v1.0.0](https://github.com/agneay/j2me-100-games/releases/tag/game-061-v1.0.0) |
| All 100 games | [j2me-100-games-v1.0.0.zip](https://github.com/agneay/j2me-100-games/releases/download/v1.0.0/j2me-100-games-v1.0.0.zip) |

**Browser Demo:** [play in your browser](https://agneay.github.io/j2me-100-games/games/city-shield/). The browser demo is the same Java source compiled to JavaScript with TeaVM and run on the project's MIDP reference runtime; it is a convenience preview, not the original Java ME version. For the authentic experience install the JAR on a phone or in an emulator.

Installation help: [docs/installing.md](../../docs/installing.md) and [docs/emulator.md](../../docs/emulator.md).

## Technical details

| | |
|---|---|
| MIDlet class | `cityshield.CityShieldMIDlet` |
| Platform | Java ME, CLDC 1.0, MIDP 2.0 |
| JAR size | 18.1 KB |
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
tools/build-game.sh 061
```

Output: `games/061-city-shield/dist/CityShield.jar` and `.jad`. See [docs/building.md](../../docs/building.md).

---

Part of [J2ME 100 Games](../../README.md). Original game, released under the [MIT License](../../LICENSE).
