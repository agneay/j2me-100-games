# Contributing

Thanks for helping keep button-phone gaming alive. Contributions of every size are welcome.

## Most wanted: real-device reports

No game has been confirmed on physical hardware yet. If you have a Java phone, install a few games
([docs/installing.md](docs/installing.md)) and open a
[device report](https://github.com/agneay/j2me-100-games/issues/new?template=device-report.yml).
Please only report what you actually tried. Confirmed reports go into `docs/real-device-reports.json`.

Reports from emulators the project has not tested yet (J2ME Loader, KEmulator, FreeJ2ME, the Java ME SDK)
are also very useful.

## Bug reports

Use the bug template. Include the game, the screen size or phone/emulator, what you pressed and what
happened. A screenshot helps.

## Pull requests

1. Fork and create a branch.
2. Set up: `python tools/bootstrap.py` (JDK 17+ and Python 3.8+ required).
3. Make your change. For game code follow [docs/development.md](docs/development.md) and
   [docs/game-design-guidelines.md](docs/game-design-guidelines.md).
4. Before you push:

   ```bash
   python tools/build_all.py <ids>          # or tools/build-all.sh
   python tools/run_tests.py <ids>
   python tools/microemu_check.py <ids>
   python tools/gen_docs.py --no-media      # if game.properties changed
   python tools/validate.py
   ```

5. Look at the test screenshots in `games/<game>/build/test/` at all five sizes.
6. Open the PR with a short description and, for visual changes, before/after screenshots.

CI builds every game and runs both test suites and the validator on each pull request.

## New games

* Must be original (see the guidelines) and different in mechanics from existing games.
* One game per pull request, in `games/<next id>-<slug>/`, with `game.properties` filled in.
* MIT-licensed, no third-party assets.

## Code style

* Java 1.3 language level, CLDC 1.0 / MIDP 2.0 APIs only, no floating point.
* Match the surrounding code: 4-space indents, short methods, comments that explain *why*.
* Python tools: standard library only.

## License

By contributing you agree that your contribution is licensed under the MIT License of this repository.
