package tenpin;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Ten Pin - MIDlet entry point. */
public class TenPinMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new BowlingGame();
    }
}
