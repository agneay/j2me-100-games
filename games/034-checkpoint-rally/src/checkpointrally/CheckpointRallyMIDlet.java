package checkpointrally;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Checkpoint Rally - MIDlet entry point. */
public class CheckpointRallyMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new RallyGame();
    }
}
