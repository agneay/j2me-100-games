package tinycity;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Tiny City - MIDlet entry point. */
public class TinyCityMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new CityGame();
    }
}
