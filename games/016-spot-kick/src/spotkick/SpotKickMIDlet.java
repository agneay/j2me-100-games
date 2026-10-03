package spotkick;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Spot Kick - MIDlet entry point. */
public class SpotKickMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new SpotKickGame();
    }
}
