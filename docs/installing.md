# Installing on a phone

Each game is a standard MIDP 2.0 application: one `.jar` (the program) and one `.jad` (a small text
descriptor). Download them from the game's release page, [download.md](download.md), or the all-games
ZIP.

> These games have not yet been tested on real phone hardware by the maintainers (see
> [verification.md](verification.md)). The steps below are the standard ways to install Java ME
> applications. If you try a game on a phone, please file a device report so others know what works.

## What your phone needs

* Java ME with **MIDP 2.0** (CLDC 1.0 or 1.1). Most Java-capable feature phones from about 2004 onward
  qualify; look for "Java", "MIDP 2.0" or "J2ME" in the phone's specifications.
* A free heap of roughly 512 KB is a comfortable minimum. The JARs are 10-50 KB.
* Any screen from 128x128 up; layouts are designed for 128x128, 128x160, 176x208, 176x220 and 240x320.

## Method 1: copy the JAR over USB or a memory card

1. Download the `.jar` (you usually do not need the `.jad` for this method).
2. Connect the phone in mass-storage mode or put its memory card in a card reader.
3. Copy the JAR to a folder the phone scans for applications, for example:
   * Nokia Series 40: any folder; open it with the phone's file manager, or `Gallery` / `My files`.
   * Sony Ericsson: `/MSSEMC/Media files/other/` or the phone's `Other` folder in the file manager.
   * Samsung and LG: varies; many models install from the file manager or need a PC suite.
4. Disconnect, browse to the file on the phone and select it. Accept the installation prompt.
5. The game appears in the phone's Applications or Games folder.

## Method 2: Bluetooth or infrared

1. Pair the phone with your computer.
2. Send the `.jar` file to the phone.
3. Open the received file from the phone's inbox and choose to install it.

Some phones only accept JARs over Bluetooth if a matching `.jad` is sent as well; send the JAD first,
then the JAR.

## Method 3: over the air (OTA) from a web server

1. Put both files in the same folder on a web server that serves:
   * `.jad` as `text/vnd.sun.j2me.app-descriptor`
   * `.jar` as `application/java-archive`
2. Open the **JAD** URL in the phone's browser. The phone reads `MIDlet-Jar-URL` (a relative path to the
   JAR) and downloads the JAR.

GitHub Release download links redirect and are served over modern HTTPS, which old phone browsers usually
cannot handle. For OTA, host the files yourself on plain HTTP.

## Method 4: vendor PC suites

Nokia PC Suite / Ovi Suite, Sony Ericsson PC Suite, Samsung PC Studio and similar tools have an
"install application" function that accepts a JAR or JAD.

## Permissions and storage

The games use only:

* the screen and keypad
* `Manager.playTone` for sound effects (switch sound off on the title screen with `0`)
* a small RMS record store named `gk` for settings and best scores

They use no network, messaging, camera, file system or location APIs, so phones should not show
security prompts beyond the usual "unsigned application" warning at install time.

## Troubleshooting

| Message | Meaning |
|---|---|
| "Invalid application" / "File corrupt" | The JAR was damaged in transfer, or a JAD from a different build was used. Re-download both. |
| "JAR size mismatch" | The JAD's `MIDlet-Jar-Size` does not match the JAR. Use files from the same release. |
| "Application not supported" | The phone is MIDP 1.0 only. These games need MIDP 2.0. |
| Out of memory | Close other Java applications; try a lighter game. |
| No sound | The phone may not support `playTone`, or sound is off in the game or in the phone's profile. |

To test without a phone, see [emulator.md](emulator.md).
