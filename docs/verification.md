# Verification

This page states exactly what has and has not been checked. It is regenerated from the test reports by `tools/gen_docs.py`; nothing on it is written by hand.

## Levels

| Level | Count | Method |
|---|---|---|
| Build verified | 100 / 100 | Source compiled with ECJ (`-source 1.3 -target cldc1.1`, which emits preverified class files) against the CLDC 1.0 and MIDP 2.0 API stubs, so any call outside those APIs fails the build. JAR and JAD generated, with MIDlet-Jar-Size matching the JAR. |
| Reference runtime | 100 / 100 | `tools/run_tests.py`: the real JAR runs on the project's own headless MIDP implementation (emu/core) at 128x128, 128x160, 176x208, 176x220 and 240x320 with a scripted plus random key sequence. Fails on any uncaught exception, a crashed game loop, a stalled loop or a blank screen. |
| Emulator verified | 100 / 100 | `tools/microemu_check.py`: the JAD/JAR is loaded by the independent MicroEmulator 2.0.4 in headless mode (default 176x220 device). The game is started with 5 and fed random keypad input for 6 seconds. Fails on exceptions, a crashed loop or a blank screen; screenshots are rendered by MicroEmulator itself. Errors from a missing sound device on the host (CI machines have none) are ignored. |
| Real device verified | 0 / 100 | A person ran the game on physical hardware and reported it. |

### What this does not prove

* Automated input is random, so a full playthrough of every game has not been verified by a machine.
* Both runtimes are desktop Java. Real phones differ in heap size (some have well under 1 MB), CPU speed, fonts, key codes and sound support. The board-game AIs on their hardest settings may think for several seconds on slow phones.
* No game has been run in Sun/Oracle WTK, the Nokia SDK emulators, KEmulator or J2ME Loader by the maintainers yet; those are good next steps and reports are welcome.

## Per-game status

| # | Game | Build | Reference runtime (5 sizes) | MicroEmulator | Real device | Level |
|---|---|---|---|---|---|---|
| 001 | Retro Snake | yes | pass (5/5) | pass | not tested | Emulator verified |
| 002 | Pixel Paddle | yes | pass (5/5) | pass | not tested | Emulator verified |
| 003 | Block Fall | yes | pass (5/5) | pass | not tested | Emulator verified |
| 004 | Mine Sweep | yes | pass (5/5) | pass | not tested | Emulator verified |
| 005 | Keypad Chess | yes | pass (5/5) | pass | not tested | Emulator verified |
| 006 | Brick Buster | yes | pass (5/5) | pass | not tested | Emulator verified |
| 007 | Number Place | yes | pass (5/5) | pass | not tested | Emulator verified |
| 008 | Pixel Jumper | yes | pass (5/5) | pass | not tested | Emulator verified |
| 009 | Traffic Dash | yes | pass (5/5) | pass | not tested | Emulator verified |
| 010 | Tower Guard | yes | pass (5/5) | pass | not tested | Emulator verified |
| 011 | Star Defender | yes | pass (5/5) | pass | not tested | Emulator verified |
| 012 | Tile Merge | yes | pass (5/5) | pass | not tested | Emulator verified |
| 013 | Four Drop | yes | pass (5/5) | pass | not tested | Emulator verified |
| 014 | Twenty-One | yes | pass (5/5) | pass | not tested | Emulator verified |
| 015 | Dungeon Quest | yes | pass (5/5) | pass | not tested | Emulator verified |
| 016 | Spot Kick | yes | pass (5/5) | pass | not tested | Emulator verified |
| 017 | Pocket Farm | yes | pass (5/5) | pass | not tested | Emulator verified |
| 018 | Echo Tones | yes | pass (5/5) | pass | not tested | Emulator verified |
| 019 | Rock Blaster | yes | pass (5/5) | pass | not tested | Emulator verified |
| 020 | Crate Pusher | yes | pass (5/5) | pass | not tested | Emulator verified |
| 021 | Noughts Grid | yes | pass (5/5) | pass | not tested | Emulator verified |
| 022 | Sky Hopper | yes | pass (5/5) | pass | not tested | Emulator verified |
| 023 | Moto Rush | yes | pass (5/5) | pass | not tested | Emulator verified |
| 024 | Sea Strike | yes | pass (5/5) | pass | not tested | Emulator verified |
| 025 | Klondike | yes | pass (5/5) | pass | not tested | Emulator verified |
| 026 | Monster Duel | yes | pass (5/5) | pass | not tested | Emulator verified |
| 027 | Mini Golf | yes | pass (5/5) | pass | not tested | Emulator verified |
| 028 | Lake Fishing | yes | pass (5/5) | pass | not tested | Emulator verified |
| 029 | Multitap Rush | yes | pass (5/5) | pass | not tested | Emulator verified |
| 030 | Fruit Catcher | yes | pass (5/5) | pass | not tested | Emulator verified |
| 031 | Slide Fifteen | yes | pass (5/5) | pass | not tested | Emulator verified |
| 032 | Flip Disc | yes | pass (5/5) | pass | not tested | Emulator verified |
| 033 | Cave Escape | yes | pass (5/5) | pass | not tested | Emulator verified |
| 034 | Checkpoint Rally | yes | pass (5/5) | pass | not tested | Emulator verified |
| 035 | Box Claim | yes | pass (5/5) | pass | not tested | Emulator verified |
| 036 | Hi-Lo Cards | yes | pass (5/5) | pass | not tested | Emulator verified |
| 037 | Island Dig | yes | pass (5/5) | pass | not tested | Emulator verified |
| 038 | Ten Pin | yes | pass (5/5) | pass | not tested | Emulator verified |
| 039 | Corner Shop | yes | pass (5/5) | pass | not tested | Emulator verified |
| 040 | Mole Bonk | yes | pass (5/5) | pass | not tested | Emulator verified |
| 041 | Gem Swap | yes | pass (5/5) | pass | not tested | Emulator verified |
| 042 | Keypad Checkers | yes | pass (5/5) | pass | not tested | Emulator verified |
| 043 | Ladder Loot | yes | pass (5/5) | pass | not tested | Emulator verified |
| 044 | Drag Strip | yes | pass (5/5) | pass | not tested | Emulator verified |
| 045 | Land Grab | yes | pass (5/5) | pass | not tested | Emulator verified |
| 046 | Draw Poker | yes | pass (5/5) | pass | not tested | Emulator verified |
| 047 | Lost Keep | yes | pass (5/5) | pass | not tested | Emulator verified |
| 048 | Hoop Shot | yes | pass (5/5) | pass | not tested | Emulator verified |
| 049 | Deep Mine | yes | pass (5/5) | pass | not tested | Emulator verified |
| 050 | Orbit Jump | yes | pass (5/5) | pass | not tested | Emulator verified |
| 051 | Light Trails | yes | pass (5/5) | pass | not tested | Emulator verified |
| 052 | Pixel Logic | yes | pass (5/5) | pass | not tested | Emulator verified |
| 053 | Five Stones | yes | pass (5/5) | pass | not tested | Emulator verified |
| 054 | Treasure Run | yes | pass (5/5) | pass | not tested | Emulator verified |
| 055 | Snow Slalom | yes | pass (5/5) | pass | not tested | Emulator verified |
| 056 | Kingdom Ledger | yes | pass (5/5) | pass | not tested | Emulator verified |
| 057 | Cell Solitaire | yes | pass (5/5) | pass | not tested | Emulator verified |
| 058 | Pocket Saga | yes | pass (5/5) | pass | not tested | Emulator verified |
| 059 | Pocket Cricket | yes | pass (5/5) | pass | not tested | Emulator verified |
| 060 | Diner Rush | yes | pass (5/5) | pass | not tested | Emulator verified |
| 061 | City Shield | yes | pass (5/5) | pass | not tested | Emulator verified |
| 062 | Switch Grid | yes | pass (5/5) | pass | not tested | Emulator verified |
| 063 | Seed Sowing | yes | pass (5/5) | pass | not tested | Emulator verified |
| 064 | Gravity Flip | yes | pass (5/5) | pass | not tested | Emulator verified |
| 065 | Hill Climb | yes | pass (5/5) | pass | not tested | Emulator verified |
| 066 | Galaxy Conquest | yes | pass (5/5) | pass | not tested | Emulator verified |
| 067 | Eights Shed | yes | pass (5/5) | pass | not tested | Emulator verified |
| 068 | Arena Champion | yes | pass (5/5) | pass | not tested | Emulator verified |
| 069 | Court Rally | yes | pass (5/5) | pass | not tested | Emulator verified |
| 070 | Tiny City | yes | pass (5/5) | pass | not tested | Emulator verified |
| 071 | Road Hopper | yes | pass (5/5) | pass | not tested | Emulator verified |
| 072 | Pipe Flow | yes | pass (5/5) | pass | not tested | Emulator verified |
| 073 | Nim Sticks | yes | pass (5/5) | pass | not tested | Emulator verified |
| 074 | Barrel Climb | yes | pass (5/5) | pass | not tested | Emulator verified |
| 075 | Pixel Racer | yes | pass (5/5) | pass | not tested | Emulator verified |
| 076 | Grid Tactics | yes | pass (5/5) | pass | not tested | Emulator verified |
| 077 | Memory Cards | yes | pass (5/5) | pass | not tested | Emulator verified |
| 078 | Deep Crawl 3D | yes | pass (5/5) | pass | not tested | Emulator verified |
| 079 | Bullseye | yes | pass (5/5) | pass | not tested | Emulator verified |
| 080 | Factory Line | yes | pass (5/5) | pass | not tested | Emulator verified |
| 081 | Copter Cave | yes | pass (5/5) | pass | not tested | Emulator verified |
| 082 | Code Breaker | yes | pass (5/5) | pass | not tested | Emulator verified |
| 083 | Race Home | yes | pass (5/5) | pass | not tested | Emulator verified |
| 084 | Wall Kick | yes | pass (5/5) | pass | not tested | Emulator verified |
| 085 | Ring Racer | yes | pass (5/5) | pass | not tested | Emulator verified |
| 086 | Siege Lanes | yes | pass (5/5) | pass | not tested | Emulator verified |
| 087 | Domino Line | yes | pass (5/5) | pass | not tested | Emulator verified |
| 088 | Spell Deck | yes | pass (5/5) | pass | not tested | Emulator verified |
| 089 | Puck Rush | yes | pass (5/5) | pass | not tested | Emulator verified |
| 090 | Lemon Stand | yes | pass (5/5) | pass | not tested | Emulator verified |
| 091 | Bubble Shot | yes | pass (5/5) | pass | not tested | Emulator verified |
| 092 | Laser Mirrors | yes | pass (5/5) | pass | not tested | Emulator verified |
| 093 | Peg Jump | yes | pass (5/5) | pass | not tested | Emulator verified |
| 094 | Volley Beach | yes | pass (5/5) | pass | not tested | Emulator verified |
| 095 | Pet Pal | yes | pass (5/5) | pass | not tested | Emulator verified |
| 096 | Space Trader | yes | pass (5/5) | pass | not tested | Emulator verified |
| 097 | Rail Switcher | yes | pass (5/5) | pass | not tested | Emulator verified |
| 098 | Morse Signal | yes | pass (5/5) | pass | not tested | Emulator verified |
| 099 | Math Blitz | yes | pass (5/5) | pass | not tested | Emulator verified |
| 100 | Cell Garden | yes | pass (5/5) | pass | not tested | Emulator verified |

Report generated on 2026-10-03 (MicroEmulator report) from `reports/test-report.json` and `reports/microemu-report.json`.
