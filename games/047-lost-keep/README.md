# Lost Keep

<img src="media/icon.png" width="24" height="24" alt=""> **RPG** &middot; Game 047 of 100 &middot; v1.0.0

> A graphical text adventure: explore a ruined keep, solve item puzzles and restore the tower's gem - no typing needed.

<p><img src="media/title.png" alt="Lost Keep title screen" width="176"> <img src="media/play.png" alt="Lost Keep gameplay" width="176"> <img src="media/demo.gif" alt="Lost Keep gameplay animation" width="176"></p>

<sub>Screenshots at 176x208, captured from the real JAR on the project's reference runtime.</sub>

## About

A classic illustrated adventure redesigned for a keypad. Every room is drawn on screen, and instead of typing commands you pick from a menu that only lists what makes sense right now: look, go somewhere, take what you see, or use what you carry. Ten rooms, seven items and a chain of puzzles involving a hungry hound, a locked library, a dark cellar and a ghostly knight. Fewer moves earns a better score.

**Objective:** Find the stolen gem and return it to the statue at the top of the tower.

## Controls

| Key | Action |
|---|---|
| 2/8 | Choose an action |
| 5 | Perform the action |
| * | Pause menu |

Every game also supports: `5` to select in menus, `*` to pause, `0` on the title screen to toggle sound, and `#` on the title screen for help. Joystick/D-pad and the 2/4/6/8 keys are interchangeable.

## Download

| File | Link |
|---|---|
| JAR (install this) | [LostKeep.jar](https://github.com/agneay/j2me-100-games/releases/download/game-047-v1.0.0/LostKeep.jar) |
| JAD (descriptor for OTA install) | [LostKeep.jad](https://github.com/agneay/j2me-100-games/releases/download/game-047-v1.0.0/LostKeep.jad) |
| Release page | [game-047-v1.0.0](https://github.com/agneay/j2me-100-games/releases/tag/game-047-v1.0.0) |
| All 100 games | [j2me-100-games-v1.0.0.zip](https://github.com/agneay/j2me-100-games/releases/download/v1.0.0/j2me-100-games-v1.0.0.zip) |

**Browser Demo:** [play in your browser](https://agneay.github.io/j2me-100-games/games/lost-keep/). The browser demo is the same Java source compiled to JavaScript with TeaVM and run on the project's MIDP reference runtime; it is a convenience preview, not the original Java ME version. For the authentic experience install the JAR on a phone or in an emulator.

Installation help: [docs/installing.md](../../docs/installing.md) and [docs/emulator.md](../../docs/emulator.md).

## Technical details

| | |
|---|---|
| MIDlet class | `lostkeep.LostKeepMIDlet` |
| Platform | Java ME, CLDC 1.0, MIDP 2.0 |
| JAR size | 20.4 KB |
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
tools/build-game.sh 047
```

Output: `games/047-lost-keep/dist/LostKeep.jar` and `.jad`. See [docs/building.md](../../docs/building.md).

---

Part of [J2ME 100 Games](../../README.md). Original game, released under the [MIT License](../../LICENSE).
