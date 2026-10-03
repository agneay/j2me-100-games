package dragstrip;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Drag Strip - MIDlet entry point. */
public class DragStripMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new DragGame();
    }
}
