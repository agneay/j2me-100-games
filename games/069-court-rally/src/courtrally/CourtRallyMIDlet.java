package courtrally;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Court Rally - MIDlet entry point. */
public class CourtRallyMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new TennisGame();
    }
}
