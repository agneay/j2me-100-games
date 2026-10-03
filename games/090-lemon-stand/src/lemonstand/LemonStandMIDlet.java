package lemonstand;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Lemon Stand - MIDlet entry point. */
public class LemonStandMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new LemonGame();
    }
}
