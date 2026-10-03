package blockfall;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Block Fall - MIDlet entry point. */
public class BlockFallMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new BlockFallGame();
    }
}
