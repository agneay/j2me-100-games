package noughtsgrid;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Noughts Grid - MIDlet entry point. */
public class NoughtsGridMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new NoughtsGame();
    }
}
