package memorycards;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Memory Cards - MIDlet entry point. */
public class MemoryCardsMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new MemoryGame();
    }
}
