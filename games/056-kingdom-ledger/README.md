# Kingdom Ledger

<img src="media/icon.png" width="24" height="24" alt=""> **Strategy** &middot; Game 056 of 100 &middot; v1.0.0

> Rule a small kingdom for ten years by deciding how much land to trade, grain to feed and acres to sow.

<p><img src="media/title.png" alt="Kingdom Ledger title screen" width="176"> <img src="media/play.png" alt="Kingdom Ledger gameplay" width="176"> <img src="media/demo.gif" alt="Kingdom Ledger gameplay animation" width="176"></p>

<sub>Screenshots at 176x208, captured from the real JAR on the project's reference runtime.</sub>

## About

A resource-management classic in the spirit of the earliest computer strategy games, rebuilt for a keypad. Each year set three numbers - land to buy or sell, grain to feed your people and acres to sow - then read the royal report of harvests, rat raids, starvation, newcomers and the occasional plague. Starve too many in one year and you are overthrown; survive ten years and you are judged on land per person and lives lost.

**Objective:** Survive ten years with a growing, well-fed population and plenty of land per person.

## Controls

| Key | Action |
|---|---|
| 2/8 | Choose decision |
| 4/6 | Change by 1 (land) or 10 (grain) |
| 1/3 | Change by 10x |
| 5 or # | End the year / continue |
| * | Pause menu |

Every game also supports: `5` to select in menus, `*` to pause, `0` on the title screen to toggle sound, and `#` on the title screen for help. Joystick/D-pad and the 2/4/6/8 keys are interchangeable.

## Download

| File | Link |
|---|---|
| JAR (install this) | [KingdomLedger.jar](https://github.com/agneay/j2me-100-games/releases/download/game-056-v1.0.0/KingdomLedger.jar) |
| JAD (descriptor for OTA install) | [KingdomLedger.jad](https://github.com/agneay/j2me-100-games/releases/download/game-056-v1.0.0/KingdomLedger.jad) |
| Release page | [game-056-v1.0.0](https://github.com/agneay/j2me-100-games/releases/tag/game-056-v1.0.0) |
| All 100 games | [j2me-100-games-v1.0.0.zip](https://github.com/agneay/j2me-100-games/releases/download/v1.0.0/j2me-100-games-v1.0.0.zip) |

**Browser Demo:** [play in your browser](https://agneay.github.io/j2me-100-games/games/kingdom-ledger/). The browser demo is the same Java source compiled to JavaScript with TeaVM and run on the project's MIDP reference runtime; it is a convenience preview, not the original Java ME version. For the authentic experience install the JAR on a phone or in an emulator.

Installation help: [docs/installing.md](../../docs/installing.md) and [docs/emulator.md](../../docs/emulator.md).

## Technical details

| | |
|---|---|
| MIDlet class | `kingdomledger.KingdomLedgerMIDlet` |
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
tools/build-game.sh 056
```

Output: `games/056-kingdom-ledger/dist/KingdomLedger.jar` and `.jad`. See [docs/building.md](../../docs/building.md).

---

Part of [J2ME 100 Games](../../README.md). Original game, released under the [MIT License](../../LICENSE).
