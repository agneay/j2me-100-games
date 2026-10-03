package dominoline;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Domino Line - MIDlet entry point. */
public class DominoLineMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new DominoGame();
    }
}
