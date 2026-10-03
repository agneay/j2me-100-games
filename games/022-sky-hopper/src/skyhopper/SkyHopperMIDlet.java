package skyhopper;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Sky Hopper - MIDlet entry point. */
public class SkyHopperMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new HopperGame();
    }
}
