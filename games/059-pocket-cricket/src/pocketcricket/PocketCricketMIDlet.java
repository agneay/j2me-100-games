package pocketcricket;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Pocket Cricket - MIDlet entry point. */
public class PocketCricketMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new CricketGame();
    }
}
