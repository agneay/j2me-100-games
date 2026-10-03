# Supported devices

> **Status:** no game has been tested on real hardware yet. Everything below describes what the games are
> *designed* for and what *should* work based on the platform requirements. Confirmed results will be
> listed in [verification.md](verification.md) as device reports arrive.

## Platform requirements

| Requirement | Value |
|---|---|
| Configuration | CLDC 1.0 (CLDC 1.1 also fine) |
| Profile | MIDP 2.0 |
| Optional APIs | none (no JSR-135 players, no JSR-75, no Bluetooth, no M3G) |
| Floating point | not used |
| JAR size | about 10-50 KB per game |
| Heap | roughly 512 KB free recommended; most games use far less |
| Input | ITU-T 12-key keypad and/or a 4-way joystick with centre select |

## Screen sizes

Every game reads `getWidth()`/`getHeight()` and lays itself out at runtime. The automated tests run each
game at these five sizes:

| Resolution | Typical phones of the era |
|---|---|
| 128x128 | Early colour Nokia Series 40 and many low-cost models |
| 128x160 | Many Samsung, LG, Motorola and Sony Ericsson models |
| 176x208 | Nokia Series 60 (2nd edition) |
| 176x220 | Sony Ericsson K-series and W-series, many Samsung models |
| 240x320 | Later Series 40, Sony Ericsson, Samsung, LG and most QVGA phones |

Other sizes between and around these should generally work, but only the five above are tested.

## Device families that should be compatible

These are families with MIDP 2.0 support, listed so you can tell whether a phone is in scope. **Not
tested.**

* Nokia Series 40 2nd edition and later; Series 60 2nd edition and later
* Sony Ericsson Java Platform 3 (JP-3) and later
* Samsung, LG, Motorola and other MIDP 2.0 feature phones
* Modern KaiOS and Android devices through J2ME Loader (see [emulator.md](emulator.md))

## Known risks on real hardware

* **Speed:** the game loop targets 20 frames per second. On slow early phones some action games may run
  slower than intended. The chess and checkers AIs on their hardest settings search deeply and may take
  several seconds per move.
* **Keys:** the joystick is read through `getGameAction`, which phones implement differently. The number
  keys 2/4/6/8/5 always work as a fallback.
* **Fonts:** layouts measure the phone's own fonts, but a few phones have unusually large fonts that can
  crowd the smallest screens.
* **Sound:** effects use `Manager.playTone`; phones without tone generation stay silent.
* **Full screen:** games call `setFullScreenMode(true)`. Phones that ignore it show a slightly smaller canvas,
  which the layout handles.

## Reporting a device

Open a [device report](https://github.com/agneay/j2me-100-games/issues/new?template=device-report.yml)
with the phone model, firmware if known, how you installed, which games you tried, and what happened.
Confirmed reports are added to `docs/real-device-reports.json`, and the docs and website are regenerated.
