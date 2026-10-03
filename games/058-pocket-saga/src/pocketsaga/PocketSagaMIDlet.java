package pocketsaga;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Pocket Saga - MIDlet entry point. */
public class PocketSagaMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new SagaGame();
    }
}
