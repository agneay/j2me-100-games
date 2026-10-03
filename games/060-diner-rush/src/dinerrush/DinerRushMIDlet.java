package dinerrush;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Diner Rush - MIDlet entry point. */
public class DinerRushMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new DinerGame();
    }
}
