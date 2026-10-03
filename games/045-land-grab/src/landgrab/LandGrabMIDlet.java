package landgrab;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Land Grab - MIDlet entry point. */
public class LandGrabMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new LandGame();
    }
}
