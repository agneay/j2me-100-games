package lostkeep;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Lost Keep - MIDlet entry point. */
public class LostKeepMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new KeepGame();
    }
}
