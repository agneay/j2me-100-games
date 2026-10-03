package motorush;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Moto Rush - MIDlet entry point. */
public class MotoRushMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new MotoGame();
    }
}
