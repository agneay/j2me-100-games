# Security Policy

## Scope

The games are offline MIDlets. They use only the screen, the keypad, `Manager.playTone` and a small RMS
record store. They do not use networking, messaging, the file system, the camera or location APIs.

Security-relevant areas of this repository:

* the build and bootstrap tooling (`tools/`), which downloads pinned, SHA-256-checked artifacts from Maven
  Central
* the GitHub Actions workflows (`.github/workflows/`)
* the website and its JavaScript (`website/`)
* the JAR/JAD files published in Releases

## Supported versions

Only the latest release of each game and the latest website are supported.

## Reporting a vulnerability

Please **do not** open a public issue. Use GitHub's private reporting:
[report a vulnerability](https://github.com/agneay/j2me-100-games/security/advisories/new).

Include what you found, how to reproduce it and its impact. You can expect an acknowledgement within a week.
Fixes are released as new versions, with credit to the reporter if they wish.

## Verifying downloads

Every game JAR is built reproducibly (fixed timestamps) from the tagged source. You can rebuild any game
yourself with `tools/build-game.sh NNN` and compare the JAR with the released file. Builds with the same JDK
version are byte-for-byte identical; a different JDK can encode the generated icon PNG differently, so compare
the class files if the JDK differs.
