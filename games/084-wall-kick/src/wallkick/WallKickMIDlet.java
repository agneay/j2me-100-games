package wallkick;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Wall Kick - MIDlet entry point. */
public class WallKickMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new KickGame();
    }
}
