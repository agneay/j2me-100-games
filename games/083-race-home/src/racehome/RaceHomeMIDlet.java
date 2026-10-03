package racehome;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Race Home - MIDlet entry point. */
public class RaceHomeMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new RaceGame();
    }
}
