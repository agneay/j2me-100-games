# Running in an emulator

No phone? Any of these will run the JAR/JAD files. Each listing says honestly whether the maintainers
have run the games with that emulator.

| Emulator | Platform | Tested by the project |
|---|---|---|
| MicroEmulator 2.0.4 | Windows, macOS, Linux (Java) | **Yes**, automated smoke test of all 100 games (`tools/microemu_check.py`) |
| Project reference runtime | Any JDK 17+ | **Yes**, automated tests of all 100 games at 5 resolutions (`tools/run_tests.py`) |
| J2ME Loader | Android | Not yet |
| KEmulator / KEmulator nnmod | Windows | Not yet |
| FreeJ2ME | Java (desktop, libretro) | Not yet |
| Oracle Java ME SDK / Sun WTK 2.5.2 | Windows, Linux | Not yet |

Reports for the untested emulators are welcome: open an issue with the result.

## MicroEmulator (desktop)

MicroEmulator is a Java SE implementation of MIDP 2.0. Version 2.0.4 is on Maven Central; the bootstrap
script downloads it to `tools/lib/microemu/microemulator-2.0.4.jar`.

Interactive window:

```bash
java -jar tools/lib/microemu/microemulator-2.0.4.jar games/001-retro-snake/dist/RetroSnake.jad
```

Then use the on-screen keypad or your keyboard (number keys, arrow keys, Enter for fire). MicroEmulator is
old; if the window misbehaves on a new JDK, run it with a Java 8-21 runtime.

Headless smoke test of every game, as the project's CI does:

```bash
python tools/bootstrap.py --no-teavm
tools/build-all.sh
python tools/microemu_check.py --jobs 4
```

MicroEmulator's URL handling breaks on paths with spaces. The check script copies each game to a temporary
folder first; do the same if you launch it by hand.

## J2ME Loader (Android)

1. Install J2ME Loader from F-Droid or the Play Store.
2. Copy the `.jar` to the Android device.
3. In J2ME Loader tap **+**, pick the JAR, and adjust screen size if you like (176x208 or 240x320 are good
   choices).
4. Use the on-screen keypad. The `*` key pauses; `5` is fire.

## KEmulator (Windows)

Open KEmulator, choose **Midlet > Load jar**, and pick the JAR. Set the screen size under
**View > Options** (try 176x220 or 240x320).

## FreeJ2ME

```bash
java -jar freej2me.jar file:///path/to/RetroSnake.jar 240 320
```

## Oracle Java ME SDK / Sun Wireless Toolkit

Choose **File > Open JAD**, pick the JAD, and run it on the `DefaultColorPhone` skin (240x320) or
`QwertyDevice`.

## Project reference runtime

The project includes its own small MIDP implementation (`emu/core`) used for automated testing and, compiled
to JavaScript, for the browser demos. It is a test tool and not a full emulator, but you can use it to
capture screenshots:

```bash
java -cp tools/build/emu-desktop.jar emu.desktop.Harness --jad games/001-retro-snake/dist/RetroSnake.jad \
     --out /tmp/shots --sizes 240x320 --script "w500 shot:title 5 w2000 shot:play"
```

## Key mapping reminders

| Phone key | Typical keyboard key |
|---|---|
| 2 / 4 / 6 / 8 | arrow keys or numpad 8/4/6/2 |
| 5 / fire | Enter or numpad 5 |
| `*` | `*` (pause) |
| `#` | `#` |
| soft keys | F1 / F2 in most emulators |
