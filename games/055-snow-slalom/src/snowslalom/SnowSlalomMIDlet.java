package snowslalom;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Snow Slalom - MIDlet entry point. */
public class SnowSlalomMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new SlalomGame();
    }
}
