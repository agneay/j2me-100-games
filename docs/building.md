# Building

Each game is an independent Ant project that shares the build logic in `tools/ant/game-build.xml` and the
framework in `common/src/gamekit`. You can build one game or all 100.

## Requirements

| Tool | Version | Notes |
|---|---|---|
| JDK | 17 or newer | Runs Ant, the compiler and the test tools. |
| Python | 3.8 or newer | Bootstrap, parallel builds, tests, docs and website generators. |
| Internet (first run) | | `tools/bootstrap.py` downloads the toolchain from Maven Central. |

You do **not** need the Sun/Oracle Wireless Toolkit, ProGuard or a separate preverifier.

## Toolchain

`python tools/bootstrap.py` downloads these into `tools/lib/` and checks each file's SHA-256:

* Apache Ant 1.10.15
* Eclipse Compiler for Java (ECJ) 3.26.0
* CLDC 1.0 and MIDP 2.0 API stubs (`org.microemu:cldcapi10` / `midpapi20` 2.0.4)
* MicroEmulator 2.0.4 (used only by `tools/microemu_check.py`)
* TeaVM 0.10.2 and its dependencies (used only for the browser demos; skip with `--no-teavm`)

### How the MIDP build works

ECJ compiles each game with `-source 1.3 -target cldc1.1` against the CLDC/MIDP stubs as the boot class path:

* `-target cldc1.1` makes ECJ write Java 1.1-format class files (version 45.3) with the `StackMap`
  attribute that CLDC's split verifier needs. In other words the classes come out **preverified**, so no
  separate `preverify` step is needed.
* Compiling against the stubs instead of the JDK means any use of an API that a phone does not have
  (collections, `String.format`, floating point helpers, `Integer.bitCount`...) is a compile error.
* The JAD declares `CLDC-1.0` and `MIDP-2.0`. The code uses no floating point, so CLDC 1.0 phones are
  supported.

The JAR is built with a fixed timestamp so rebuilds are byte-for-byte reproducible, and the JAD's
`MIDlet-Jar-Size` is computed from the finished JAR.

## Build commands

Linux / macOS / Git Bash:

```bash
python3 tools/bootstrap.py
tools/build-all.sh              # all games, in parallel (tools/build_all.py)
tools/build-all.sh --ant        # all games, sequentially with plain Ant
tools/build-game.sh 042         # one game
tools/build-game.sh 042 test    # one game + reference-runtime smoke test
tools/package.sh                # release/games/*.jar|jad + release/j2me-100-games-v1.0.0.zip
```

Windows (cmd or PowerShell):

```bat
python tools\bootstrap.py
tools\build-all.cmd
tools\build-game.cmd 042
tools\package.cmd
```

Plain Ant (after bootstrapping) from the repository root:

```bash
tools/ant.sh            # = ant dist
tools/ant.sh test       # build + reference-runtime test of every game
tools/ant.sh media      # screenshots and GIFs
tools/ant.sh web        # browser demos (needs a Java 17-21 runtime, see below)
tools/ant.sh package
```

Output for game `NNN-slug` is written to `games/NNN-slug/dist/<Jar>.jar` and `<Jar>.jad`.

## Tests and validation

```bash
python tools/run_tests.py --jobs 8     # reference runtime, 5 screen sizes  -> reports/test-report.json
python tools/microemu_check.py         # MicroEmulator 2.0.4 smoke test     -> reports/microemu-report.json
python tools/validate.py               # repository checks                  -> reports/validation-report.md
python tools/gen_docs.py               # regenerate READMEs, docs/download.md, docs/verification.md
```

See [verification.md](verification.md) for what each test does and does not prove.

## Browser demos

`tools/ant.sh web` (or `python tools/build_all.py --target web`) compiles each game together with the
reference runtime to JavaScript using TeaVM. TeaVM 0.10 cannot read JDK 22+ class files, so the TeaVM step
runs on a separate Java 17-21 runtime: set `TEAVM_JAVA=/path/to/java` or unpack a JRE 21 into `tools/jre21/`.
If your default `java` is already 17-21, nothing needs configuring.

## Website

```bash
python tools/gen_docs.py
python tools/gen_site.py      # writes website/_site
python -m http.server -d website/_site 8000
```

## Troubleshooting

| Symptom | Fix |
|---|---|
| `The type java.util.ArrayList is not accessible` or similar | You used an API that is not in CLDC 1.0 / MIDP 2.0. Use `java.util.Vector`/`Hashtable` or plain arrays. |
| `Unsupported class file major version` during `web` | TeaVM is running on JDK 22+. Set `TEAVM_JAVA` to a Java 21 runtime. |
| Phone says "Invalid JAR" or "JAR size mismatch" | Copy the JAR and JAD from the same build; the JAD records the exact JAR size. |
| `python3: not found` on Windows | Use `python`, or the `.cmd` scripts. |
