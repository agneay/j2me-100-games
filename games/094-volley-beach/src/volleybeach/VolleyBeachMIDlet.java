package volleybeach;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Volley Beach - MIDlet entry point. */
public class VolleyBeachMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new VolleyGame();
    }
}
