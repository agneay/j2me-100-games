package siegelanes;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Siege Lanes - MIDlet entry point. */
public class SiegeLanesMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new SiegeGame();
    }
}
