# Validation report

Generated 2026-10-03 17:22 by `tools/validate.py`.

| Metric | Value |
|---|---|
| Total games | 100 |
| Buildable (JAR + JAD) | 100 |
| JAR files | 100 |
| JAD files | 100 |
| Per-game READMEs | 100 |
| Reference-runtime tested (5 resolutions) | 100 |
| Emulator tested (MicroEmulator 2.0.4) | 100 |
| Real-hardware tested | 0 |
| Per-game GitHub Releases | not yet published |
| Milestone releases | not yet published |
| Failed builds | none |

## Errors

None.

## Warnings

None.

## Known limitations

* No game has been tested on real phone hardware yet.
* Emulator testing is an automated smoke test (random input) on MicroEmulator 2.0.4 only.
* Hard AI levels in Keypad Chess and Keypad Checkers may take several seconds per move on slow phones.
* Sound uses `Manager.playTone`; phones without tone support simply stay silent.
