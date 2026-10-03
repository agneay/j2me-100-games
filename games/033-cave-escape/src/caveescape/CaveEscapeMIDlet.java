package caveescape;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Cave Escape - MIDlet entry point. */
public class CaveEscapeMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new CaveGame();
    }
}
