# Game design guidelines

The bar for a game in this collection: it is original, it is genuinely different from the other games,
and it feels good on a 12-key keypad on a small screen.

## Originality

* Every game is written from scratch for this project: code, graphics, sounds, names and level data.
* Classic *genres* and public-domain games (chess, draughts, solitaire, dominoes, peg solitaire, nim, mancala)
  are fine. Copying a specific commercial game's names, characters, level layouts, graphics, music or
  distinctive trade dress is not.
* Use descriptive names. `tools/validate.py` rejects names that contain well-known game trademarks.
* No third-party assets, fonts or code inside the JARs.

## Every game has

* A title screen with title art, the game name, a menu (Play, mode selection if any, How to play, Sound,
  Exit) and the best score. This comes from `gamekit.Game`.
* Help pages that explain the goal and list the controls.
* A pause menu on `*` with Resume, Restart, How to play, Sound and Main menu.
* A game-over or victory screen with the score, a "new best" marker and *play again*.
* Sensible defaults: the first mode is the friendliest one.

## Controls

* Movement on the joystick **and** 2/4/6/8. Fire/confirm on the joystick centre **and** 5.
* `*` is always pause. `#` and `0` are free for game actions during play; on the title screen `#` opens help
  and `0` toggles sound.
* Prefer the number keys for direct choices (lanes 1-3, card slots, switches 1-7) where it makes play faster;
  it is the one thing a keypad does better than a touch screen.
* Never require two keys at once. Many phones cannot report simultaneous key presses.
* Holding a direction should repeat or keep moving where that is natural (`held` bits).

## Screen and layout

* Lay out from `W`, `H` and font metrics. Test every game at 128x128, 128x160, 176x208, 176x220 and 240x320
  and look at every screenshot: overlapping HUD text at 128 pixels wide is the most common bug.
* Keep the HUD to one line at the top, and use short labels on narrow screens (`W < 160`).
* High contrast, chunky shapes; most of these screens are small and not very sharp.

## Performance

* Logic runs at 20 ticks per second. Keep `update()` and `draw()` cheap; draw only what changed when a scene
  is static and expensive.
* No allocation inside the loop. Precompute tables.
* Integer and fixed-point maths only.
* AIs must stay responsive: bound search depth and time, and offer an easier mode that answers quickly.

## Difficulty and length

* A first round should be understandable within 10 seconds and finished within a few minutes.
* Offer two or three modes when the game benefits from it, and let the challenge ramp up within a round.
* Procedural content must be checked to be solvable (for example the generators in Number Place, Laser
  Mirrors and Island Dig verify their output).

## Sound

* Short `Sfx` tones only, used for feedback: click, good, bad, win, lose. Sound must never be required to play,
  with the deliberate exception of experimental audio games, which must still show visual cues.

## Making a game distinct

Before adding a game, check the catalogue: if it plays like an existing game with a new theme, change the
core mechanic, not the skin. The eleven categories are Arcade, Puzzle, Platformer, Racing, Strategy, Board,
Card, RPG, Sports, Simulation and Experimental; experimental games should use the keypad in a way the others
do not.
