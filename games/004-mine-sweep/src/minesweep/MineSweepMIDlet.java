package minesweep;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Mine Sweep - MIDlet entry point. */
public class MineSweepMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new MineGame();
    }
}
