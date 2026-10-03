# Morse Signal

<img src="media/icon.png" width="24" height="24" alt=""> **Experimental** &middot; Game 098 of 100 &middot; v1.0.0

> Turn the 5 key into a telegraph key: tap out words in Morse code or read a flashing signal lamp against the clock.

<p><img src="media/title.png" alt="Morse Signal title screen" width="176"> <img src="media/play.png" alt="Morse Signal gameplay" width="176"> <img src="media/demo.gif" alt="Morse Signal gameplay animation" width="176"></p>

<sub>Screenshots at 176x208, captured from the real JAR on the project's reference runtime.</sub>

## About

A playful Morse code trainer built around the phone keypad. In Send mode the 5 key becomes a telegraph key - a quick tap is a dot, a longer press is a dash, and a short pause ends the letter. Key whole words letter by letter while the code for each letter is shown, and build combos for clean sending. In Receive mode a lamp flashes and beeps a letter; pick the right one from four choices with keys 1-4, answering before the signal ends for a bonus. Each round lasts 90 seconds.

**Objective:** Score as many points as possible in 90 seconds.

**Modes:** Send, Receive

## Controls

| Key | Action |
|---|---|
| 5 | Telegraph key (tap = dot, hold = dash) |
| 1-4 | Choose letter (Receive) |
| 0 | Replay signal (Receive) |
| * | Pause menu |

Every game also supports: `5` to select in menus, `*` to pause, `0` on the title screen to toggle sound, and `#` on the title screen for help. Joystick/D-pad and the 2/4/6/8 keys are interchangeable.

## Download

| File | Link |
|---|---|
| JAR (install this) | [MorseSignal.jar](https://github.com/agneay/j2me-100-games/releases/download/game-098-v1.0.0/MorseSignal.jar) |
| JAD (descriptor for OTA install) | [MorseSignal.jad](https://github.com/agneay/j2me-100-games/releases/download/game-098-v1.0.0/MorseSignal.jad) |
| Release page | [game-098-v1.0.0](https://github.com/agneay/j2me-100-games/releases/tag/game-098-v1.0.0) |
| All 100 games | [j2me-100-games-v1.0.0.zip](https://github.com/agneay/j2me-100-games/releases/download/v1.0.0/j2me-100-games-v1.0.0.zip) |

**Browser Demo:** [play in your browser](https://agneay.github.io/j2me-100-games/games/morse-signal/). The browser demo is the same Java source compiled to JavaScript with TeaVM and run on the project's MIDP reference runtime; it is a convenience preview, not the original Java ME version. For the authentic experience install the JAR on a phone or in an emulator.

Installation help: [docs/installing.md](../../docs/installing.md) and [docs/emulator.md](../../docs/emulator.md).

## Technical details

| | |
|---|---|
| MIDlet class | `morsesignal.MorseSignalMIDlet` |
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
tools/build-game.sh 098
```

Output: `games/098-morse-signal/dist/MorseSignal.jar` and `.jad`. See [docs/building.md](../../docs/building.md).

---

Part of [J2ME 100 Games](../../README.md). Original game, released under the [MIT License](../../LICENSE).
