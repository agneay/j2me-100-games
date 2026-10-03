package islanddig;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Island Dig - MIDlet entry point. */
public class IslandDigMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new IslandGame();
    }
}
