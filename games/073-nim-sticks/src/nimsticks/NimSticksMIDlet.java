package nimsticks;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Nim Sticks - MIDlet entry point. */
public class NimSticksMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new NimGame();
    }
}
